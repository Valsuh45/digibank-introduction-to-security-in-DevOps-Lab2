#!/usr/bin/env python3
"""Exercise Workshop 1 over real HTTP using new synthetic fixtures on each run.

Run against a disposable development database; this creates two customers/accounts
and one successful transfer. Uses only the Python standard library.
"""
import argparse
import json
from datetime import datetime, timezone
from decimal import Decimal
from pathlib import Path
from urllib.error import HTTPError
from urllib.parse import quote
from urllib.request import Request, urlopen
from uuid import uuid4


def verify(base_url, checks):
    def request(method, path, expected=200, body=None, content_type="application/json"):
        payload = None if body is None else json.dumps(body).encode()
        req = Request(base_url.rstrip("/") + path, data=payload, method=method,
                      headers={"Content-Type": content_type})
        try:
            response = urlopen(req, timeout=20)
        except HTTPError as error:
            response = error
        with response:
            status, raw = response.status, response.read().decode()
        if status != expected:
            raise AssertionError(f"{method} {path}: expected {expected}, got {status}")
        checks.append({"method": method, "path": path, "status": status})
        if path == "/" or path.startswith("/swagger-ui"):
            return raw
        result = json.loads(raw, parse_float=Decimal)
        if path.startswith("/api/"):
            assert result["success"] is (expected < 400), path
            assert result.get("timestamp"), path
            if expected >= 400:
                assert not any(value in raw for value in
                               ("stackTrace", "org.hibernate", "java.lang", "SQLException")), path
        return result

    def data(path):
        return request("GET", path)["data"]

    assert "swagger" in request("GET", "/").lower()
    assert "swagger" in request("GET", "/swagger-ui/index.html").lower()
    assert request("GET", "/actuator/health")["status"] == "UP"
    api = request("GET", "/v3/api-docs")
    assert api["info"]["title"] == "DigiBank API"
    for resource in ("customers", "accounts", "transfers"):
        assert f"/api/v1/{resource}" in api["paths"]

    def assert_record(actual, expected):
        # PostgreSQL stores timestamps at microsecond precision; creation responses
        # can still contain the original nanoseconds. Compare the persisted contract.
        assert actual.keys() == expected.keys()
        for key, value in expected.items():
            if key in ("createdAt", "executionDate"):
                delta = datetime.fromisoformat(actual[key]) - datetime.fromisoformat(value)
                assert abs(delta.total_seconds()) <= 0.000001, key
            else:
                assert actual[key] == value, key

    suffix = uuid4().hex[:16]
    accounts = []
    customers = []
    for index, balance in enumerate((100, 25)):
        customer_body = {"firstName": "Workshop", "lastName": "Verification",
                         "email": f"w1-{suffix}-{index}@example.com",
                         "identityNumber": f"W1-{suffix}-{index}"}
        customer = request("POST", "/api/v1/customers", 201, customer_body)["data"]
        assert "identityNumber" not in customer
        customers.append(customer)
        assert_record(data(f"/api/v1/customers/{customer['id']}"), customer)
        assert_record(data("/api/v1/customers/email/" + quote(customer["email"], safe="")), customer)
        request("POST", "/api/v1/customers", 400, customer_body)
        account = request("POST", "/api/v1/accounts", 201,
                          {"customerId": customer["id"], "accountType": "SAVINGS",
                           "initialBalance": balance})["data"]
        assert account["status"] == "ACTIVE" and account["currency"] == "XAF"
        assert_record(data(f"/api/v1/accounts/{account['id']}"), account)
        assert_record(data(f"/api/v1/accounts/number/{account['accountNumber']}"), account)
        listed = data(f"/api/v1/accounts/customer/{customer['id']}")
        assert len(listed) == 1
        assert_record(listed[0], account)
        accounts.append(account)

    source, target = accounts
    transfer_body = {"sourceAccountNumber": source["accountNumber"],
                     "targetAccountNumber": target["accountNumber"], "amount": 10,
                     "description": "Workshop 1 HTTP verification"}
    transfer = request("POST", "/api/v1/transfers", 201, transfer_body)["data"]
    assert transfer["status"] == "SUCCESS" and transfer["amount"] == 10
    persisted = data(f"/api/v1/transfers/{transfer['id']}")
    assert_record(persisted, transfer)
    transfer = persisted

    def snapshot():
        return [(data(f"/api/v1/accounts/{a['id']}")["balance"],
                 data(f"/api/v1/transfers/account/{a['accountNumber']}")) for a in accounts]

    state = snapshot()
    assert [entry[0] for entry in state] == [Decimal("90"), Decimal("35")]
    assert all(transfer in entry[1] for entry in state)
    for amount in (0, -1, 0.001, 1000):
        request("POST", "/api/v1/transfers", 400, {**transfer_body, "amount": amount})
        assert snapshot() == state, "Rejected transfer changed balances or history"
    request("POST", "/api/v1/transfers", 400,
            {**transfer_body, "targetAccountNumber": source["accountNumber"]})
    assert snapshot() == state
    request("POST", "/api/v1/accounts", 404,
            {"customerId": 9223372036854775807, "accountType": "SAVINGS", "initialBalance": 0})
    assert data("/api/v1/accounts/customer/9223372036854775807") == []
    request("GET", "/api/v1/customers/not-a-number", 400)
    request("DELETE", "/api/v1/customers", 405)
    request("POST", "/api/v1/customers", 415, {}, "text/plain")
    request("GET", "/api/v1/does-not-exist", 404)
    return {"customers": customers, "accounts": accounts, "transfer": transfer,
            "balancesAfter": [entry[0] for entry in state],
            "rejectedTransfersLeaveBalancesAndHistoryUnchanged": True}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--base-url", default="http://localhost:8080")
    parser.add_argument("--output", type=Path, default=Path("target/workshop1-http.json"))
    args = parser.parse_args()
    report = {"startedAt": datetime.now(timezone.utc).isoformat(), "checks": [], "passed": False}
    try:
        report["observations"] = verify(args.base_url, report["checks"])
        report["passed"] = True
    except Exception as error:
        report["error"] = str(error)
        raise
    finally:
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(json.dumps(report, indent=2, default=str) + "\n")
    print(f"Workshop 1 HTTP verification passed: {len(report['checks'])} requests; {args.output}")


if __name__ == "__main__":
    main()
