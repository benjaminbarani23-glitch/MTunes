import os

import pytest

os.environ.setdefault("DATABASE_URL", "sqlite:///:memory:")
os.environ.setdefault("AIRWALL_API_KEY", "test-key")


@pytest.fixture(autouse=True)
def _fresh_db():
    from airwall_core.config import get_settings

    get_settings.cache_clear()
    from airwall_core.database import Base, get_engine, reset_engine
    from airwall_core.seed import run_seed

    reset_engine()
    engine = get_engine()
    Base.metadata.drop_all(bind=engine)
    Base.metadata.create_all(bind=engine)
    run_seed()
    yield
    reset_engine()
