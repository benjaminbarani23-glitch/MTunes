import json
from dataclasses import dataclass

from sqlalchemy import select
from sqlalchemy.orm import Session

from airwall_core.database import Group, User


@dataclass
class EffectivePermission:
    resource_type: str
    action: str

    def key(self) -> str:
        return f"{self.action}:{self.resource_type}"


def get_or_create_user(session: Session, external_id: str, display_name: str | None = None) -> User:
    user = session.scalar(select(User).where(User.external_id == external_id))
    if user:
        return user
    user = User(external_id=external_id, display_name=display_name)
    session.add(user)
    session.flush()
    return user


def get_user_groups(session: Session, external_id: str) -> list[Group]:
    user = session.scalar(select(User).where(User.external_id == external_id))
    if not user:
        return []
    return list(user.groups)


def get_effective_permissions(session: Session, external_id: str) -> set[str]:
    groups = get_user_groups(session, external_id)
    perms: set[str] = set()
    for group in groups:
        for perm in group.permissions:
            perms.add(f"{perm.action}:{perm.resource_type}")
    return perms


def user_has_permission(session: Session, external_id: str, resource_type: str, action: str = "read") -> bool:
    return f"{action}:{resource_type}" in get_effective_permissions(session, external_id)


def check_resources_allowed(
    session: Session, external_id: str, requested_resources: list[str], action: str = "read"
) -> tuple[bool, list[str]]:
    if not requested_resources:
        return True, []
    effective = get_effective_permissions(session, external_id)
    denied = [r for r in requested_resources if f"{action}:{r}" not in effective]
    return len(denied) == 0, denied


def get_group_names(session: Session, external_id: str) -> list[str]:
    return [g.name for g in get_user_groups(session, external_id)]


def permissions_to_json(perms: set[str]) -> str:
    return json.dumps(sorted(perms))
