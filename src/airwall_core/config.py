from functools import lru_cache
from pathlib import Path

from pydantic_settings import BaseSettings, SettingsConfigDict

ROOT = Path(__file__).resolve().parents[2]
DATA_DIR = ROOT / "data"
PATTERNS_SEED = DATA_DIR / "patterns" / "seed.yaml"


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8", extra="ignore")

    database_url: str = "postgresql+psycopg://airwall:airwall@localhost:5432/airwall"
    redis_url: str = "redis://localhost:6379/0"
    airwall_api_key: str = "dev-api-key-change-me"
    airwall_admin_api_key: str = "dev-admin-key-change-me"
    risk_allow_max: int = 39
    risk_sandbox_max: int = 79
    log_full_prompts: bool = False
    production_llm_url: str = ""
    production_llm_api_key: str = ""
    sandbox_llm_url: str = ""


@lru_cache
def get_settings() -> Settings:
    return Settings()
