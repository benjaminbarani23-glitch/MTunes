import typer
from rich.console import Console
from rich.table import Table
from sqlalchemy import select

from airwall_core.database import (
    Group,
    Permission,
    PolicyAuditLog,
    User,
    get_session_factory,
    init_db,
)
from airwall_core.rbac import get_effective_permissions, get_or_create_user
from airwall_core.seed import run_seed

app = typer.Typer(name="airwall", help="Airwall admin CLI — groups, permissions, users")
groups_app = typer.Typer(help="Manage groups")
permissions_app = typer.Typer(help="Manage group permissions")
users_app = typer.Typer(help="Manage users")
policy_app = typer.Typer(help="Policy utilities")

app.add_typer(groups_app, name="groups")
app.add_typer(permissions_app, name="permissions")
app.add_typer(users_app, name="users")
app.add_typer(policy_app, name="policy")

console = Console()


def _session():
    init_db()
    return get_session_factory()()


def _log_policy(session, actor: str, action: str, detail: str) -> None:
    session.add(PolicyAuditLog(actor=actor, action=action, detail=detail))


@groups_app.command("list")
def groups_list():
    session = _session()
    try:
        rows = session.scalars(select(Group)).all()
        table = Table(title="Groups")
        table.add_column("ID")
        table.add_column("Name")
        table.add_column("Description")
        for g in rows:
            table.add_row(str(g.id), g.name, g.description or "")
        console.print(table)
    finally:
        session.close()


@groups_app.command("create")
def groups_create(name: str, description: str = ""):
    session = _session()
    try:
        if session.scalar(select(Group).where(Group.name == name)):
            console.print("[red]Group already exists[/red]")
            raise typer.Exit(1)
        session.add(Group(name=name, description=description or None))
        _log_policy(session, "cli", "group.create", name)
        session.commit()
        console.print(f"[green]Created group[/green] {name}")
    finally:
        session.close()


@groups_app.command("delete")
def groups_delete(name: str):
    session = _session()
    try:
        group = session.scalar(select(Group).where(Group.name == name))
        if not group:
            console.print("[red]Group not found[/red]")
            raise typer.Exit(1)
        session.delete(group)
        _log_policy(session, "cli", "group.delete", name)
        session.commit()
        console.print(f"[yellow]Deleted group[/yellow] {name}")
    finally:
        session.close()


@permissions_app.command("list")
def permissions_list(group: str):
    session = _session()
    try:
        g = session.scalar(select(Group).where(Group.name == group))
        if not g:
            console.print("[red]Group not found[/red]")
            raise typer.Exit(1)
        for p in g.permissions:
            console.print(f"{p.action}:{p.resource_type}")
    finally:
        session.close()


@permissions_app.command("grant")
def permissions_grant(group: str, permission: str):
    session = _session()
    try:
        if ":" not in permission:
            console.print("[red]Permission format: action:resource (e.g. read:payroll)[/red]")
            raise typer.Exit(1)
        action, resource = permission.split(":", 1)
        g = session.scalar(select(Group).where(Group.name == group))
        if not g:
            console.print("[red]Group not found[/red]")
            raise typer.Exit(1)
        perm = session.scalar(
            select(Permission).where(
                Permission.action == action, Permission.resource_type == resource
            )
        )
        if not perm:
            perm = Permission(action=action, resource_type=resource)
            session.add(perm)
            session.flush()
        if perm not in g.permissions:
            g.permissions.append(perm)
        _log_policy(session, "cli", "permission.grant", f"{group} {permission}")
        session.commit()
        console.print(f"[green]Granted[/green] {permission} to {group}")
    finally:
        session.close()


@permissions_app.command("revoke")
def permissions_revoke(group: str, permission: str):
    session = _session()
    try:
        action, resource = permission.split(":", 1)
        g = session.scalar(select(Group).where(Group.name == group))
        if not g:
            console.print("[red]Group not found[/red]")
            raise typer.Exit(1)
        perm = session.scalar(
            select(Permission).where(
                Permission.action == action, Permission.resource_type == resource
            )
        )
        if perm and perm in g.permissions:
            g.permissions.remove(perm)
        _log_policy(session, "cli", "permission.revoke", f"{group} {permission}")
        session.commit()
        console.print(f"[yellow]Revoked[/yellow] {permission} from {group}")
    finally:
        session.close()


@users_app.command("create")
def users_create(external_id: str, display_name: str = ""):
    session = _session()
    try:
        get_or_create_user(session, external_id, display_name or None)
        _log_policy(session, "cli", "user.create", external_id)
        session.commit()
        console.print(f"[green]User[/green] {external_id}")
    finally:
        session.close()


@users_app.command("add-group")
def users_add_group(external_id: str, group: str):
    session = _session()
    try:
        user = get_or_create_user(session, external_id)
        g = session.scalar(select(Group).where(Group.name == group))
        if not g:
            console.print("[red]Group not found[/red]")
            raise typer.Exit(1)
        if g not in user.groups:
            user.groups.append(g)
        _log_policy(session, "cli", "user.add_group", f"{external_id} -> {group}")
        session.commit()
        console.print(f"Added {external_id} to {group}")
    finally:
        session.close()


@users_app.command("remove-group")
def users_remove_group(external_id: str, group: str):
    session = _session()
    try:
        user = session.scalar(select(User).where(User.external_id == external_id))
        if not user:
            console.print("[red]User not found[/red]")
            raise typer.Exit(1)
        g = session.scalar(select(Group).where(Group.name == group))
        if g and g in user.groups:
            user.groups.remove(g)
        _log_policy(session, "cli", "user.remove_group", f"{external_id} -> {group}")
        session.commit()
        console.print(f"Removed {external_id} from {group}")
    finally:
        session.close()


@users_app.command("show")
def users_show(external_id: str):
    session = _session()
    try:
        user = session.scalar(select(User).where(User.external_id == external_id))
        if not user:
            console.print("[red]User not found[/red]")
            raise typer.Exit(1)
        console.print(f"User: {user.external_id} ({user.display_name or '-'})")
        console.print("Groups:", ", ".join(g.name for g in user.groups) or "(none)")
        perms = get_effective_permissions(session, external_id)
        console.print("Effective permissions:")
        for p in sorted(perms):
            console.print(f"  - {p}")
    finally:
        session.close()


@policy_app.command("seed")
def policy_seed():
    run_seed()
    console.print("[green]Seeded groups, permissions, and patterns[/green]")


@policy_app.command("reload")
def policy_reload():
    console.print("[green]Policy cache reload (no-op in MVP; permissions read from DB each request)[/green]")


if __name__ == "__main__":
    app()
