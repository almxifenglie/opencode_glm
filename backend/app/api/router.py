from fastapi import APIRouter, Query, HTTPException
from sqlalchemy import select, desc, and_, or_, func
from sqlalchemy.ext.asyncio import AsyncSession
from typing import Optional, List
import math

from app.database import async_session
from app.models import QDIIData, LOFData, PurchaseStatus, Fund, IndexData
from app.schemas import (
    RankingResponse, RankingItem, FundDetailResponse, 
    PurchaseStatusResponse, NavHistoryResponse, ApiResponse
)
from app.services.lof_spider import LOFSpider
from app.services.qdii_spider import QDIISpider
from app.services.purchase_spider import PurchaseSpider
from app.services.premium_calc import PremiumCalculator

router = APIRouter()


def calculate_total_pages(total: int, page_size: int) -> int:
    return math.ceil(total / page_size) if total > 0 else 1


@router.get("/qdii/ranking", response_model=RankingResponse)
async def get_qdii_ranking(
    min_premium: float = Query(0, description="最小溢价率"),
    max_premium: float = Query(100, description="最大溢价率"),
    page: int = Query(1, ge=1, description="页码"),
    page_size: int = Query(20, ge=1, le=100, description="每页条数"),
    purchase_open: bool = Query(True, description="只显示开放申购"),
    source: str = Query("jisilu", description="数据源")
):
    async with async_session() as session:
        base_query = select(QDIIData).where(
            and_(
                QDIIData.premium_rate >= min_premium,
                QDIIData.premium_rate <= max_premium,
            )
        )
        
        if purchase_open:
            subquery = select(PurchaseStatus.code).where(
                PurchaseStatus.purchase_status == '开放'
            )
            base_query = base_query.where(QDIIData.code.in_(subquery))
        
        count_query = select(func.count()).select_from(base_query.subquery())
        total = (await session.execute(count_query)).scalar()
        
        query = base_query.order_by(desc(QDIIData.premium_rate))
        query = query.offset((page - 1) * page_size).limit(page_size)
        
        result = await session.execute(query)
        items = result.scalars().all()
        
        purchase_query = select(PurchaseStatus)
        purchase_result = await session.execute(purchase_query)
        purchase_dict = {p.code: p for p in purchase_result.scalars().all()}
        
        ranking_items = []
        for item in items:
            purchase = purchase_dict.get(item.code)
            ranking_items.append(RankingItem(
                code=item.code,
                name=item.name,
                price=item.price,
                change_pct=item.change_pct,
                premium_rate=item.premium_rate,
                nav_t1=item.nav_t1,
                purchase_status=purchase.purchase_status if purchase else None,
                purchase_limit=purchase.purchase_limit if purchase else None,
                volume=item.volume,
                amount=item.amount,
                source=item.source,
                update_time=item.update_time,
            ))
        
        return RankingResponse(
            total=total,
            items=ranking_items,
            source=source,
            update_time=items[0].update_time if items else None,
            page=page,
            page_size=page_size,
            total_pages=calculate_total_pages(total, page_size),
        )


@router.get("/lof/ranking", response_model=RankingResponse)
async def get_lof_ranking(
    min_premium: float = Query(-50, description="最小溢价率"),
    max_premium: float = Query(100, description="最大溢价率"),
    page: int = Query(1, ge=1, description="页码"),
    page_size: int = Query(20, ge=1, le=100, description="每页条数"),
    purchase_open: bool = Query(True, description="只显示开放申购")
):
    async with async_session() as session:
        base_query = select(LOFData).where(
            and_(
                LOFData.premium_rate >= min_premium,
                LOFData.premium_rate <= max_premium,
            )
        )
        
        if purchase_open:
            subquery = select(PurchaseStatus.code).where(
                PurchaseStatus.purchase_status == '开放'
            )
            base_query = base_query.where(LOFData.code.in_(subquery))
        
        count_query = select(func.count()).select_from(base_query.subquery())
        total = (await session.execute(count_query)).scalar()
        
        query = base_query.order_by(desc(LOFData.premium_rate))
        query = query.offset((page - 1) * page_size).limit(page_size)
        
        result = await session.execute(query)
        items = result.scalars().all()
        
        purchase_query = select(PurchaseStatus)
        purchase_result = await session.execute(purchase_query)
        purchase_dict = {p.code: p for p in purchase_result.scalars().all()}
        
        ranking_items = []
        for item in items:
            purchase = purchase_dict.get(item.code)
            ranking_items.append(RankingItem(
                code=item.code,
                name=item.name,
                price=item.price,
                change_pct=item.change_pct,
                premium_rate=item.premium_rate,
                nav_t1=item.nav_t1,
                nav_estimate=item.nav_estimate,
                purchase_status=purchase.purchase_status if purchase else None,
                purchase_limit=purchase.purchase_limit if purchase else None,
                volume=item.volume,
                amount=item.amount,
                source=item.source,
                update_time=item.update_time,
            ))
        
        return RankingResponse(
            total=total,
            items=ranking_items,
            source="eastmoney",
            update_time=items[0].update_time if items else None,
            page=page,
            page_size=page_size,
            total_pages=calculate_total_pages(total, page_size),
        )


@router.get("/all/ranking", response_model=RankingResponse)
async def get_all_ranking(
    min_premium: float = Query(0, description="最小溢价率"),
    page: int = Query(1, ge=1, description="页码"),
    page_size: int = Query(20, ge=1, le=100, description="每页条数"),
    purchase_open: bool = Query(True, description="只显示开放申购"),
    fund_type: str = Query("all", description="基金类型: all/qdii/lof")
):
    async with async_session() as session:
        all_items = []
        
        purchase_query = select(PurchaseStatus)
        purchase_result = await session.execute(purchase_query)
        purchase_dict = {p.code: p for p in purchase_result.scalars().all()}
        
        if fund_type in ["all", "qdii"]:
            qdii_query = select(QDIIData).where(
                QDIIData.premium_rate >= min_premium
            ).order_by(desc(QDIIData.premium_rate))
            qdii_result = await session.execute(qdii_query)
            qdii_items = qdii_result.scalars().all()
            
            for item in qdii_items:
                purchase = purchase_dict.get(item.code)
                if purchase_open and (not purchase or purchase.purchase_status != '开放'):
                    continue
                all_items.append({
                    'data': item,
                    'purchase': purchase,
                    'type': 'qdii'
                })
        
        if fund_type in ["all", "lof"]:
            lof_query = select(LOFData).where(
                LOFData.premium_rate >= min_premium
            ).order_by(desc(LOFData.premium_rate))
            lof_result = await session.execute(lof_query)
            lof_items = lof_result.scalars().all()
            
            for item in lof_items:
                purchase = purchase_dict.get(item.code)
                if purchase_open and (not purchase or purchase.purchase_status != '开放'):
                    continue
                all_items.append({
                    'data': item,
                    'purchase': purchase,
                    'type': 'lof'
                })
        
        all_items.sort(key=lambda x: x['data'].premium_rate if x['data'].premium_rate else -999, reverse=True)
        
        total = len(all_items)
        start = (page - 1) * page_size
        end = start + page_size
        paged_items = all_items[start:end]
        
        ranking_items = []
        for item_data in paged_items:
            item = item_data['data']
            purchase = item_data['purchase']
            item_type = item_data['type']
            
            ranking_items.append(RankingItem(
                code=item.code,
                name=item.name,
                price=item.price,
                change_pct=item.change_pct,
                premium_rate=item.premium_rate,
                nav_t1=item.nav_t1,
                nav_estimate=item.nav_estimate if hasattr(item, 'nav_estimate') else None,
                purchase_status=purchase.purchase_status if purchase else None,
                purchase_limit=purchase.purchase_limit if purchase else None,
                volume=item.volume,
                amount=item.amount,
                source=f"{item_type}/{item.source}",
                update_time=item.update_time,
            ))
        
        return RankingResponse(
            total=total,
            items=ranking_items,
            source="mixed",
            update_time=ranking_items[0].update_time if ranking_items else None,
            page=page,
            page_size=page_size,
            total_pages=calculate_total_pages(total, page_size),
        )


@router.post("/refresh/{code}", response_model=ApiResponse)
async def refresh_fund_data(code: str):
    async with async_session() as session:
        fund = await session.get(Fund, code)
        if not fund:
            raise HTTPException(status_code=404, detail="基金不存在")
        
        fund_type = fund.fund_type
        
    try:
        if fund_type == 'LOF':
            lof_spider = LOFSpider()
            nav_data = await lof_spider.fetch_lof_nav(code)
            
            async with async_session() as session:
                lof = await session.get(LOFData, code)
                if lof and nav_data and nav_data.get('nav'):
                    lof.nav_t1 = nav_data['nav']
                    
                    calc = PremiumCalculator()
                    index_code = calc.LOF_INDEX_MAPPING.get(code)
                    calc_result = await calc.calculate_lof_premium(
                        code, lof.price, nav_data['nav'], index_code
                    )
                    lof.nav_estimate = calc_result['nav_estimate']
                    lof.premium_rate = calc_result['premium_rate']
                    
                    await session.commit()
                    
                    return ApiResponse(message=f"基金 {code} 数据刷新成功")
        
        elif fund_type == 'QDII':
            return ApiResponse(message=f"QDII基金 {code} 需要完整刷新")
        
        return ApiResponse(message=f"基金 {code} 刷新完成")
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"刷新失败: {str(e)}")


@router.get("/fund/{code}", response_model=FundDetailResponse)
async def get_fund_detail(code: str):
    async with async_session() as session:
        fund = await session.get(Fund, code)
        if not fund:
            raise HTTPException(status_code=404, detail="基金不存在")
        
        qdii = await session.get(QDIIData, code)
        lof = await session.get(LOFData, code)
        purchase = await session.get(PurchaseStatus, code)
        
        if qdii:
            return FundDetailResponse(
                code=fund.code,
                name=fund.name,
                fund_type=fund.fund_type,
                exchange=fund.exchange,
                scale=fund.scale,
                index_code=fund.index_code,
                index_name=fund.index_name,
                price=qdii.price,
                change_pct=qdii.change_pct,
                nav_t1=qdii.nav_t1,
                premium_rate=qdii.premium_rate,
                purchase_status=purchase.purchase_status if purchase else None,
                redeem_status=purchase.redeem_status if purchase else None,
                purchase_limit=purchase.purchase_limit if purchase else None,
                purchase_fee=qdii.purchase_fee,
                redeem_fee=qdii.redeem_fee,
                volume=qdii.volume,
                amount=qdii.amount,
                update_time=qdii.update_time,
            )
        elif lof:
            return FundDetailResponse(
                code=fund.code,
                name=fund.name,
                fund_type=fund.fund_type,
                exchange=fund.exchange,
                scale=fund.scale,
                index_code=fund.index_code,
                index_name=fund.index_name,
                price=lof.price,
                change_pct=lof.change_pct,
                nav_t1=lof.nav_t1,
                nav_estimate=lof.nav_estimate,
                premium_rate=lof.premium_rate,
                purchase_status=purchase.purchase_status if purchase else None,
                redeem_status=purchase.redeem_status if purchase else None,
                purchase_limit=purchase.purchase_limit if purchase else None,
                volume=lof.volume,
                amount=lof.amount,
                update_time=lof.update_time,
            )
        else:
            return FundDetailResponse(
                code=fund.code,
                name=fund.name,
                fund_type=fund.fund_type,
                exchange=fund.exchange,
                scale=fund.scale,
                index_code=fund.index_code,
                index_name=fund.index_name,
                purchase_status=purchase.purchase_status if purchase else None,
                redeem_status=purchase.redeem_status if purchase else None,
                purchase_limit=purchase.purchase_limit if purchase else None,
            )


@router.get("/purchase/{code}", response_model=PurchaseStatusResponse)
async def get_purchase_status(code: str):
    async with async_session() as session:
        purchase = await session.get(PurchaseStatus, code)
        if not purchase:
            raise HTTPException(status_code=404, detail="申购状态信息不存在")
        
        return PurchaseStatusResponse(
            code=purchase.code,
            name=purchase.name,
            purchase_status=purchase.purchase_status,
            redeem_status=purchase.redeem_status,
            purchase_limit=purchase.purchase_limit,
            daily_limit=purchase.daily_limit,
            purchase_fee=purchase.purchase_fee,
            redeem_fee=purchase.redeem_fee,
            source=purchase.source,
            update_time=purchase.update_time,
        )


@router.get("/search")
async def search_fund(keyword: str = Query(..., min_length=1)):
    async with async_session() as session:
        query = select(Fund).where(
            or_(
                Fund.code.contains(keyword),
                Fund.name.contains(keyword),
            )
        ).limit(20)
        
        result = await session.execute(query)
        items = result.scalars().all()
        
        return {
            "total": len(items),
            "items": [
                {"code": item.code, "name": item.name, "type": item.fund_type}
                for item in items
            ]
        }


@router.get("/index")
async def get_index_data():
    async with async_session() as session:
        result = await session.execute(select(IndexData))
        items = result.scalars().all()
        return {
            "total": len(items),
            "items": [
                {
                    "code": item.code,
                    "name": item.name,
                    "price": item.price,
                    "change_pct": item.change_pct,
                    "update_time": item.update_time,
                }
                for item in items
            ]
        }
