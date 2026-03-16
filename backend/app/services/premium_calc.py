import akshare as ak
import asyncio
from datetime import datetime
from typing import Dict, Optional, List
from sqlalchemy import select
from app.models import LOFData, QDIIData, IndexData, PurchaseStatus
from app.database import async_session


class PremiumCalculator:
    LOF_INDEX_MAPPING = {
        '161116': '399971',  # 中证传媒
        '161122': '399971',  # 传媒ETF
        '161123': '399971',  
        '161124': '399967',  # 中证军工
        '161125': '399967',
        '161126': '399973',  # 国防军工
        '161714': '399437',  # 国证有色金属
        '161815': '399394',  # 国证有色金属
        '162411': 'CL',      # 华宝油气 - 原油
        '163406': '000300',  # 沪深300
        '165520': 'SPX',     # 纳斯达克100
        '159941': 'NDX',     # 纳指ETF
        '159942': 'NDX',
        '513050': 'HKEI',    # 港股相关
        '513100': 'NDX',     # 纳指100
        '513130': 'HKEI',    # 恒生科技
        '513180': '000300',  # 沪深300ETF
        '513500': 'SPX',     # 标普500
        '513520': 'NDX',     # 纳指100
        '513660': 'HKEI',    # 恒生ETF
        '501018': 'GDCE.GC', # 黄金
        '501029': 'COMEX.SI',# 白银
    }
    
    async def get_index_change(self, index_code: str) -> Optional[float]:
        async with async_session() as session:
            result = await session.execute(
                select(IndexData).where(IndexData.code == index_code)
            )
            index_data = result.scalar_one_or_none()
            if index_data:
                return index_data.change_pct
        return None
    
    def calculate_premium(self, price: float, nav: float) -> Optional[float]:
        if not price or not nav or nav == 0:
            return None
        return round((price - nav) / nav * 100, 2)
    
    async def calculate_lof_premium(
        self, 
        code: str, 
        price: float, 
        nav_t1: float,
        index_code: Optional[str] = None
    ) -> Dict:
        result = {
            'code': code,
            'price': price,
            'nav_t1': nav_t1,
            'nav_estimate': None,
            'premium_rate': None,
            'index_change_pct': None,
        }
        
        if not nav_t1 or nav_t1 == 0:
            return result
        
        premium_t1 = self.calculate_premium(price, nav_t1)
        
        if index_code:
            index_change = await self.get_index_change(index_code)
            if index_change is not None:
                result['index_change_pct'] = index_change
                nav_estimate = nav_t1 * (1 + index_change / 100)
                result['nav_estimate'] = round(nav_estimate, 4)
                result['premium_rate'] = self.calculate_premium(price, nav_estimate)
                return result
        
        result['nav_estimate'] = nav_t1
        result['premium_rate'] = premium_t1
        return result
    
    async def update_lof_premium(self):
        async with async_session() as session:
            result = await session.execute(select(LOFData))
            lof_list = result.scalars().all()
            
            updated_count = 0
            for lof in lof_list:
                if lof.nav_t1 and lof.nav_t1 > 0:
                    index_code = self.LOF_INDEX_MAPPING.get(lof.code)
                    calc_result = await self.calculate_lof_premium(
                        lof.code, 
                        lof.price, 
                        lof.nav_t1,
                        index_code
                    )
                    lof.nav_estimate = calc_result['nav_estimate']
                    lof.premium_rate = calc_result['premium_rate']
                    lof.index_change_pct = calc_result['index_change_pct']
                    updated_count += 1
            
            await session.commit()
            print(f"已更新 {updated_count} 条LOF溢价率数据")
    
    async def update_all_premium(self):
        print(f"[{datetime.now()}] 开始计算溢价率...")
        await self.update_lof_premium()
        print(f"[{datetime.now()}] 溢价率计算完成")


if __name__ == "__main__":
    calculator = PremiumCalculator()
    asyncio.run(calculator.update_all_premium())
