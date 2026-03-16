import akshare as ak
import asyncio
from datetime import datetime
from typing import List, Dict, Optional
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, delete
from app.models import LOFData, Fund, NavHistory
from app.database import async_session


class LOFSpider:
    def __init__(self):
        self.source = "sina"
    
    async def fetch_lof_spot(self) -> List[Dict]:
        try:
            df = ak.fund_etf_category_sina(symbol="LOF基金")
            if df is None or df.empty:
                print("获取LOF实时行情数据为空")
                return []
            records = df.to_dict('records')
            return self._parse_lof_data(records)
        except Exception as e:
            print(f"获取LOF实时行情失败: {e}")
            return []
    
    def _parse_lof_data(self, records: List[Dict]) -> List[Dict]:
        result = []
        for item in records:
            try:
                code_raw = str(item.get('代码', ''))
                if code_raw.startswith('sz') or code_raw.startswith('sh'):
                    code = code_raw[2:]
                else:
                    code = code_raw.zfill(6)
                
                record = {
                    'code': code,
                    'name': item.get('名称', ''),
                    'price': self._safe_float(item.get('最新价')),
                    'change_pct': self._safe_float(item.get('涨跌幅')),
                    'volume': self._safe_float(item.get('成交量')),
                    'amount': self._safe_float(item.get('成交额')),
                    'high': self._safe_float(item.get('最高')),
                    'low': self._safe_float(item.get('最低')),
                    'open': self._safe_float(item.get('今开')),
                    'source': self.source,
                }
                result.append(record)
            except Exception as e:
                print(f"解析LOF数据失败: {e}, 数据: {item}")
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
    
    async def fetch_lof_nav(self, code: str) -> Optional[Dict]:
        try:
            df = ak.fund_open_fund_info_em(symbol=code, indicator="单位净值走势")
            if df is None or df.empty:
                return None
            
            latest = df.iloc[-1]
            
            return {
                'code': code,
                'nav_date': str(latest.get('净值日期', '')) if '净值日期' in df.columns else '',
                'nav': self._safe_float(latest.get('单位净值')) if '单位净值' in df.columns else None,
                'acc_nav': self._safe_float(latest.get('累计净值')) if '累计净值' in df.columns else None,
                'change_pct': self._safe_float(latest.get('日增长率')) if '日增长率' in df.columns else None,
            }
        except Exception as e:
            return None
    
    async def fetch_lof_nav_list(self, codes: List[str]) -> Dict[str, float]:
        result = {}
        total = len(codes)
        print(f"开始获取 {total} 只LOF的净值数据...")
        
        for i, code in enumerate(codes):
            try:
                nav_data = await self.fetch_lof_nav(code)
                if nav_data and nav_data.get('nav'):
                    result[code] = nav_data['nav']
                    if (i + 1) % 50 == 0:
                        print(f"已获取 {i + 1}/{total} 只LOF净值")
                await asyncio.sleep(0.05)
            except Exception as e:
                continue
        
        print(f"成功获取 {len(result)} 只LOF净值数据")
        return result
    
    async def save_to_db(self, data_list: List[Dict], nav_dict: Dict[str, float] = None):
        async with async_session() as session:
            async with session.begin():
                await session.execute(delete(LOFData))
                
                for data in data_list:
                    code = data['code']
                    if nav_dict and code in nav_dict:
                        data['nav_t1'] = nav_dict[code]
                    
                    lof_data = LOFData(**data)
                    session.add(lof_data)
                    
                    fund = await session.get(Fund, code)
                    if not fund:
                        fund = Fund(
                            code=code,
                            name=data['name'],
                            fund_type='LOF',
                        )
                        session.add(fund)
        print(f"已保存 {len(data_list)} 条LOF数据")
    
    async def run(self, fetch_nav: bool = True):
        print(f"[{datetime.now()}] 开始获取LOF数据...")
        
        data = await self.fetch_lof_spot()
        if not data:
            print(f"[{datetime.now()}] LOF数据获取失败")
            return []
        
        nav_dict = {}
        if fetch_nav:
            codes = [d['code'] for d in data]
            nav_dict = await self.fetch_lof_nav_list(codes)
        
        await self.save_to_db(data, nav_dict)
        
        print(f"[{datetime.now()}] LOF数据获取完成，共 {len(data)} 条，净值 {len(nav_dict)} 条")
        return data


if __name__ == "__main__":
    spider = LOFSpider()
    asyncio.run(spider.run())
