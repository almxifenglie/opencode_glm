from pydantic import BaseModel
from typing import Optional, List
from datetime import datetime


class FundBase(BaseModel):
    code: str
    name: str
    fund_type: str
    exchange: Optional[str] = None
    scale: Optional[float] = None
    index_code: Optional[str] = None
    index_name: Optional[str] = None


class FundResponse(FundBase):
    created_at: Optional[datetime] = None
    updated_at: Optional[datetime] = None

    class Config:
        from_attributes = True


class QDIIDataResponse(BaseModel):
    code: str
    name: str
    price: Optional[float] = None
    change_pct: Optional[float] = None
    premium_rate: Optional[float] = None
    nav_t2: Optional[float] = None
    nav_t1: Optional[float] = None
    purchase_fee: Optional[float] = None
    redeem_fee: Optional[float] = None
    volume: Optional[float] = None
    amount: Optional[float] = None
    source: str = "jisilu"
    update_time: Optional[datetime] = None

    class Config:
        from_attributes = True


class LOFDataResponse(BaseModel):
    code: str
    name: str
    price: Optional[float] = None
    change_pct: Optional[float] = None
    nav_t1: Optional[float] = None
    nav_estimate: Optional[float] = None
    premium_rate: Optional[float] = None
    index_change_pct: Optional[float] = None
    volume: Optional[float] = None
    amount: Optional[float] = None
    source: str = "eastmoney"
    update_time: Optional[datetime] = None

    class Config:
        from_attributes = True


class PurchaseStatusResponse(BaseModel):
    code: str
    name: str
    purchase_status: Optional[str] = None
    redeem_status: Optional[str] = None
    purchase_limit: Optional[float] = None
    daily_limit: Optional[float] = None
    purchase_fee: Optional[float] = None
    redeem_fee: Optional[float] = None
    source: str = "eastmoney"
    update_time: Optional[datetime] = None

    class Config:
        from_attributes = True


class RankingItem(BaseModel):
    code: str
    name: str
    price: Optional[float] = None
    change_pct: Optional[float] = None
    premium_rate: Optional[float] = None
    nav_t1: Optional[float] = None
    nav_estimate: Optional[float] = None
    purchase_status: Optional[str] = None
    purchase_limit: Optional[float] = None
    volume: Optional[float] = None
    amount: Optional[float] = None
    scale: Optional[float] = None
    source: Optional[str] = None
    update_time: Optional[datetime] = None


class RankingResponse(BaseModel):
    total: int
    items: List[RankingItem]
    source: str
    update_time: Optional[datetime] = None
    page: int = 1
    page_size: int = 20
    total_pages: int = 1


class FundDetailResponse(BaseModel):
    code: str
    name: str
    fund_type: str
    exchange: Optional[str] = None
    scale: Optional[float] = None
    index_code: Optional[str] = None
    index_name: Optional[str] = None
    price: Optional[float] = None
    change_pct: Optional[float] = None
    nav_t1: Optional[float] = None
    nav_estimate: Optional[float] = None
    premium_rate: Optional[float] = None
    purchase_status: Optional[str] = None
    redeem_status: Optional[str] = None
    purchase_limit: Optional[float] = None
    purchase_fee: Optional[float] = None
    redeem_fee: Optional[float] = None
    volume: Optional[float] = None
    amount: Optional[float] = None
    update_time: Optional[datetime] = None


class NavHistoryResponse(BaseModel):
    code: str
    nav_date: str
    nav: Optional[float] = None
    acc_nav: Optional[float] = None
    change_pct: Optional[float] = None

    class Config:
        from_attributes = True


class ApiResponse(BaseModel):
    code: int = 200
    message: str = "success"
    data: Optional[dict] = None
