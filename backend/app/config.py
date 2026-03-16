from pydantic_settings import BaseSettings
from typing import Optional


class Settings(BaseSettings):
    app_name: str = "LOF/QDII 套利监控系统"
    app_version: str = "1.0.0"
    debug: bool = True
    
    database_url: str = "sqlite+aiosqlite:///./data/funds.db"
    
    scheduler_interval_minutes: int = 60
    
    cors_origins: list = ["*"]
    
    class Config:
        env_file = ".env"


settings = Settings()
