import akshare as ak
import asyncio
from datetime import datetime
from typing import List, Dict, Optional
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, delete
from app.models import PurchaseStatus, Fund
from app.database import async_session


class PurchaseSpider:
    def __init__(self):
        self.source = "eastmoney"
    
    async def fetch_purchase_status(self) -> List[Dict]:
        try:
            df = ak.fund_purchase_em()
            if df is None or df.empty:
                print("获取申购状态数据为空")
                return []
            records = df.to_dict('records')
            return self._parse_purchase_data(records)
        except Exception as e:
            print(f"获取申购状态失败: {e}")
            return []
    
    def _parse_purchase_data(self, records: List[Dict]) -> List[Dict]:
        result = []
        for item in records:
            try:
                purchase_status = item.get('申购状态', '')
                redeem_status = item.get('赎回状态', '')
                
                purchase_limit = self._parse_limit(item.get('申购累计限额'))
                daily_limit = self._parse_limit(item.get('日累计限额'))
                
                record = {
                    'code': str(item.get('基金代码', '')).zfill(6),
                    'name': item.get('基金简称', ''),
                    'purchase_status': '开放' if '开放' in str(purchase_status) or '正常' in str(purchase_status) else '暂停',
                    'redeem_status': '开放' if '开放' in str(redeem_status) or '正常' in str(redeem_status) else '暂停',
                    'purchase_limit': purchase_limit,
                    'daily_limit': daily_limit,
                    'min_purchase': self._safe_float(item.get('起购金额')),
                    'purchase_fee': self._safe_float(item.get('申购费率')),
                    'redeem_fee': self._safe_float(item.get('赎回费率')),
                    'source': self.source,
                }
                result.append(record)
            except Exception as e:
                print(f"解析申购数据失败: {e}, 数据: {item}")
                continue
        return result
    
    def _parse_limit(self, value) -> Optional[float]:
        if value is None:
            return None
        value_str = str(value).strip()
        if not value_str or value_str in ['-', '—', '不限', '无限制']:
            return None
        if '万' in value_str:
            value_str = value_str.replace('万', '')
            try:
                return float(value_str)
            except:
                return None
        if '亿' in value_str:
            value_str = value_str.replace('亿', '')
            try:
                return float(value_str) * 10000
            except:
                return None
        return self._safe_float(value_str)
    
    def _safe_float(self, value) -> Optional[float]:
        if value is None:
            return None
        if isinstance(value, (int, float)):
            return float(value)
        if isinstance(value, str):
            value = value.replace('%', '').replace('—', '').replace(',', '').strip()
            if not value:
                return None
            try:
                return float(value)
            except:
                return None
        return None
    
    async def save_to_db(self, data_list: List[Dict]):
        async with async_session() as session:
            async with session.begin():
                await session.execute(delete(PurchaseStatus))
                
                for data in data_list:
                    purchase_data = PurchaseStatus(**data)
                    session.add(purchase_data)
        print(f"已保存 {len(data_list)} 条申购状态数据")
    
    async def run(self):
        print(f"[{datetime.now()}] 开始获取申购状态数据...")
        data = await self.fetch_purchase_status()
        if data:
            await self.save_to_db(data)
        print(f"[{datetime.now()}] 申购状态数据获取完成，共 {len(data)} 条")
        return data


if __name__ == "__main__":
    spider = PurchaseSpider()
    asyncio.run(spider.run())
