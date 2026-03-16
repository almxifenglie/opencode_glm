from sqlalchemy import Column, String, Float, DateTime, Integer, Text
from sqlalchemy.sql import func
from app.database import Base


class Fund(Base):
    __tablename__ = "funds"
    
    code = Column(String(10), primary_key=True, index=True)
    name = Column(String(50), nullable=False)
    fund_type = Column(String(20), nullable=False)
    exchange = Column(String(10), nullable=True)
    scale = Column(Float, nullable=True)
    index_code = Column(String(10), nullable=True)
    index_name = Column(String(50), nullable=True)
    manager = Column(String(50), nullable=True)
    establish_date = Column(String(20), nullable=True)
    created_at = Column(DateTime, server_default=func.now())
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now())


class QDIIData(Base):
    __tablename__ = "qdii_data"
    
    id = Column(Integer, primary_key=True, autoincrement=True)
    code = Column(String(10), index=True, nullable=False)
    name = Column(String(50), nullable=False)
    price = Column(Float, nullable=True)
    change_pct = Column(Float, nullable=True)
    premium_rate = Column(Float, nullable=True)
    nav_t2 = Column(Float, nullable=True)
    nav_t1 = Column(Float, nullable=True)
    purchase_fee = Column(Float, nullable=True)
    redeem_fee = Column(Float, nullable=True)
    management_fee = Column(Float, nullable=True)
    volume = Column(Float, nullable=True)
    amount = Column(Float, nullable=True)
    market_share = Column(Float, nullable=True)
    source = Column(String(20), default="jisilu")
    update_time = Column(DateTime, server_default=func.now())


class LOFData(Base):
    __tablename__ = "lof_data"
    
    id = Column(Integer, primary_key=True, autoincrement=True)
    code = Column(String(10), index=True, nullable=False)
    name = Column(String(50), nullable=False)
    price = Column(Float, nullable=True)
    change_pct = Column(Float, nullable=True)
    nav_t1 = Column(Float, nullable=True)
    nav_estimate = Column(Float, nullable=True)
    premium_rate = Column(Float, nullable=True)
    index_change_pct = Column(Float, nullable=True)
    volume = Column(Float, nullable=True)
    amount = Column(Float, nullable=True)
    high = Column(Float, nullable=True)
    low = Column(Float, nullable=True)
    open = Column(Float, nullable=True)
    source = Column(String(20), default="eastmoney")
    update_time = Column(DateTime, server_default=func.now())


class PurchaseStatus(Base):
    __tablename__ = "purchase_status"
    
    id = Column(Integer, primary_key=True, autoincrement=True)
    code = Column(String(10), index=True, nullable=False)
    name = Column(String(50), nullable=False)
    purchase_status = Column(String(20), nullable=True)
    redeem_status = Column(String(20), nullable=True)
    purchase_limit = Column(Float, nullable=True)
    daily_limit = Column(Float, nullable=True)
    min_purchase = Column(Float, nullable=True)
    purchase_fee = Column(Float, nullable=True)
    redeem_fee = Column(Float, nullable=True)
    source = Column(String(20), default="eastmoney")
    update_time = Column(DateTime, server_default=func.now())


class NavHistory(Base):
    __tablename__ = "nav_history"
    
    id = Column(Integer, primary_key=True, autoincrement=True)
    code = Column(String(10), index=True, nullable=False)
    nav_date = Column(String(20), nullable=False)
    nav = Column(Float, nullable=True)
    acc_nav = Column(Float, nullable=True)
    change_pct = Column(Float, nullable=True)
    source = Column(String(20), default="eastmoney")
    created_at = Column(DateTime, server_default=func.now())


class IndexData(Base):
    __tablename__ = "index_data"
    
    id = Column(Integer, primary_key=True, autoincrement=True)
    code = Column(String(10), index=True, nullable=False)
    name = Column(String(50), nullable=False)
    price = Column(Float, nullable=True)
    change_pct = Column(Float, nullable=True)
    update_time = Column(DateTime, server_default=func.now())
