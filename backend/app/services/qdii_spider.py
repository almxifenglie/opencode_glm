import akshare as ak
import asyncio
from datetime import datetime
from typing import List, Dict, Optional
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, delete
from app.models import QDIIData, Fund
from app.database import async_session


class QDIISpider:
    def __init__(self):
        self.source = "jisilu"
    
    async def fetch_qdii_index(self) -> List[Dict]:
        try:
            df = ak.qdii_e_index_jsl()
            records = df.to_dict('records')
            return self._parse_qdii_data(records, "指数型QDII")
        except Exception as e:
            print(f"获取QDII指数数据失败: {e}")
            return []
    
    async def fetch_qdii_hk(self) -> List[Dict]:
        try:
            df = ak.qdii_a_index_jsl()
            records = df.to_dict('records')
            return self._parse_qdii_data(records, "港股QDII")
        except Exception as e:
            print(f"获取港股QDII数据失败: {e}")
            return []
    
    async def fetch_qdii_commodity(self) -> List[Dict]:
        try:
            df = ak.qdii_e_comm_jsl()
            records = df.to_dict('records')
            return self._parse_qdii_data(records, "商品QDII")
        except Exception as e:
            print(f"获取商品QDII数据失败: {e}")
            return []
    
    def _parse_qdii_data(self, records: List[Dict], fund_sub_type: str) -> List[Dict]:
        result = []
        for item in records:
            try:
                premium_rate = item.get('溢价率', 0)
                if isinstance(premium_rate, str):
                    premium_rate = premium_rate.replace('%', '').replace('—', '0')
                    try:
                        premium_rate = float(premium_rate)
                    except:
                        premium_rate = 0
                
                record = {
                    'code': str(item.get('代码', '')).zfill(6),
                    'name': item.get('名称', ''),
                    'price': self._safe_float(item.get('现价')),
                    'change_pct': self._safe_float(item.get('涨幅')),
                    'premium_rate': premium_rate,
                    'nav_t2': self._safe_float(item.get('T-2净值')),
                    'nav_t1': self._safe_float(item.get('T-1净值')),
                    'purchase_fee': self._safe_float(item.get('申购费')),
                    'redeem_fee': self._safe_float(item.get('赎回费')),
                    'management_fee': self._safe_float(item.get('托管费')),
                    'volume': self._safe_float(item.get('成交量')),
                    'amount': self._safe_float(item.get('成交额')),
                    'market_share': self._safe_float(item.get('场内份额')),
                    'source': self.source,
                    'fund_sub_type': fund_sub_type,
                }
                result.append(record)
            except Exception as e:
                print(f"解析QDII数据失败: {e}, 数据: {item}")
                continue
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
    
    async def fetch_all_qdii(self) -> List[Dict]:
        tasks = [
            self.fetch_qdii_index(),
            self.fetch_qdii_hk(),
            self.fetch_qdii_commodity(),
        ]
        results = await asyncio.gather(*tasks)
        all_data = []
        for data in results:
            all_data.extend(data)
        return all_data
    
    async def save_to_db(self, data_list: List[Dict]):
        async with async_session() as session:
            async with session.begin():
                await session.execute(delete(QDIIData))
                
                for data in data_list:
                    qdii_data = QDIIData(**{k: v for k, v in data.items() if k != 'fund_sub_type'})
                    session.add(qdii_data)
                    
                    fund = await session.get(Fund, data['code'])
                    if not fund:
                        fund = Fund(
                            code=data['code'],
                            name=data['name'],
                            fund_type='QDII',
                        )
                        session.add(fund)
        print(f"已保存 {len(data_list)} 条QDII数据")
    
    async def run(self):
        print(f"[{datetime.now()}] 开始获取QDII数据...")
        data = await self.fetch_all_qdii()
        if data:
            await self.save_to_db(data)
        print(f"[{datetime.now()}] QDII数据获取完成，共 {len(data)} 条")
        return data


if __name__ == "__main__":
    spider = QDIISpider()
    asyncio.run(spider.run())
