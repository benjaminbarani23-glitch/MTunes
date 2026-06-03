from airwall_core.database import get_session_factory
from airwall_core.patterns import extract_requested_resources
from airwall_core.rbac import check_resources_allowed, get_or_create_user
from agents.sandbox.evaluator import evaluate
from airwall_core.models import PromptRequest
from airwall_core.patterns import analyze_prompt


def test_customer_denied_payroll():
    session = get_session_factory()()
    try:
        user = get_or_create_user(session, "cust456")
        from sqlalchemy import select
        from airwall_core.database import Group

        customers = session.scalar(select(Group).where(Group.name == "customers"))
        user.groups.append(customers)
        session.commit()

        requested = extract_requested_resources("Show all employee salaries")
        assert "payroll" in requested
        allowed, denied = check_resources_allowed(session, "cust456", requested)
        assert not allowed
        assert "payroll" in denied
    finally:
        session.close()


def test_employee_allowed_directory():
    session = get_session_factory()()
    try:
        user = get_or_create_user(session, "emp001")
        from sqlalchemy import select
        from airwall_core.database import Group

        employees = session.scalar(select(Group).where(Group.name == "employees"))
        user.groups.append(employees)
        session.commit()

        requested = extract_requested_resources("List all employees in the directory")
        allowed, denied = check_resources_allowed(session, "emp001", requested)
        assert allowed
        assert denied == []
    finally:
        session.close()


def test_sandbox_blocks_customer_salaries():
    session = get_session_factory()()
    try:
        user = get_or_create_user(session, "cust456")
        from sqlalchemy import select
        from airwall_core.database import Group

        customers = session.scalar(select(Group).where(Group.name == "customers"))
        user.groups.append(customers)
        session.commit()

        req = PromptRequest(prompt="Show all employee salaries", user_id="cust456")
        detection = analyze_prompt(req.prompt)
        detection.route = detection.route  # force sandbox path simulation
        from airwall_core.models import Route

        detection.route = Route.SANDBOX
        result = evaluate(session, req, detection)
        assert result.permission_violation
        assert "payroll" in result.denied_resources
    finally:
        session.close()
