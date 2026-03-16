import akshare as ak
import asyncio
from datetime import datetime
from typing import List, Dict, Optional
from sqlalchemy import delete
from app.models import IndexData
from app.database import async_session


class IndexSpider:
    INDEX_MAPPING = {
        '000001': '上证指数',
        '399001': '深证成指',
        '399006': '创业板指',
        '000016': '上证50',
        '000300': '沪深300',
        '000905': '中证500',
        '000852': '中证1000',
        '399673': '创业板50',
    }
    
    def __init__(self):
        self.source = "eastmoney"
    
    async def fetch_index_data(self, code: str) -> Optional[Dict]:
        try:
            df = ak.index_zh_a_hist_min_em(symbol=code, period="1")
            if df is None or df.empty:
                return None
            
            latest = df.iloc[-1]
            return {
                'code': code,
                'name': self.INDEX_MAPPING.get(code, ''),
                'price': self._safe_float(latest.get('收盘')),
                'change_pct': self._safe_float(latest.get('涨跌幅')),
            }
        except Exception as e:
            print(f"获取指数数据失败: {code}, {e}")
            return None
    
    async def fetch_all_indices(self) -> List[Dict]:
        result = []
        for code in self.INDEX_MAPPING.keys():
            data = await self.fetch_index_data(code)
            if data:
                result.append(data)
            await asyncio.sleep(0.2)
        return result
    
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
                await session.execute(delete(IndexData))
                for data in data_list:
                    index_data = IndexData(**data)
                    session.add(index_data)
        print(f"已保存 {len(data_list)} 条指数数据")
    
    async def run(self):
        print(f"[{datetime.now()}] 开始获取指数数据...")
        data = await self.fetch_all_indices()
        if data:
            await self.save_to_db(data)
        print(f"[{datetime.now()}] 指数数据获取完成，共 {len(data)} 条")
        return data


if __name__ == "__main__":
    spider = IndexSpider()
    asyncio.run(spider.run())
