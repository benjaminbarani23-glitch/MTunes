"""Bootstrap groups, permissions, and patterns."""

from sqlalchemy import select

from airwall_core.database import Group, Permission, Pattern, get_session_factory
from airwall_core.patterns import load_seed_patterns


DEFAULT_GROUPS = {
    "employees": {
        "description": "Internal staff",
        "permissions": [
            ("read", "employee_directory"),
            ("read", "internal_docs"),
        ],
    },
    "customers": {
        "description": "External customers",
        "permissions": [
            ("read", "public_faq"),
            ("read", "own_orders"),
        ],
    },
    "interns": {
        "description": "Intern access (limited employee data)",
        "permissions": [
            ("read", "employee_directory"),
        ],
    },
    "managers": {
        "description": "People managers",
        "permissions": [
            ("read", "employee_directory"),
            ("read", "internal_docs"),
            ("read", "payroll"),
        ],
    },
}


def _get_or_create_permission(session, action: str, resource_type: str) -> Permission:
    perm = session.scalar(
        select(Permission).where(
            Permission.action == action, Permission.resource_type == resource_type
        )
    )
    if perm:
        return perm
    perm = Permission(action=action, resource_type=resource_type)
    session.add(perm)
    session.flush()
    return perm


def _get_or_create_group(session, name: str, description: str | None) -> Group:
    group = session.scalar(select(Group).where(Group.name == name))
    if group:
        return group
    group = Group(name=name, description=description)
    session.add(group)
    session.flush()
    return group


def seed_rbac(session) -> None:
    for name, spec in DEFAULT_GROUPS.items():
        group = _get_or_create_group(session, name, spec["description"])
        for action, resource in spec["permissions"]:
            perm = _get_or_create_permission(session, action, resource)
            if perm not in group.permissions:
                group.permissions.append(perm)


def seed_patterns(session) -> None:
    existing = session.scalar(select(Pattern.id).limit(1))
    if existing:
        return
    for rule in load_seed_patterns():
        session.add(
            Pattern(
                category=rule.category,
                pattern_type="regex",
                content=rule.pattern.pattern,
                severity=rule.severity,
                active=True,
            )
        )


def run_seed() -> None:
    from airwall_core.database import init_db

    init_db()
    session = get_session_factory()()
    try:
        seed_rbac(session)
        seed_patterns(session)
        session.commit()
    finally:
        session.close()
