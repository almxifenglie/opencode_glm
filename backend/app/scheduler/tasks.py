from apscheduler.schedulers.asyncio import AsyncIOScheduler
from apscheduler.triggers.interval import IntervalTrigger
from datetime import datetime
import asyncio

from app.config import settings
from app.services.qdii_spider import QDIISpider
from app.services.lof_spider import LOFSpider
from app.services.purchase_spider import PurchaseSpider
from app.services.index_spider import IndexSpider
from app.services.premium_calc import PremiumCalculator


scheduler = AsyncIOScheduler()


async def fetch_all_data():
    print(f"\n{'='*50}")
    print(f"[{datetime.now()}] 开始定时数据更新...")
    print(f"{'='*50}")
    
    qdii_spider = QDIISpider()
    lof_spider = LOFSpider()
    purchase_spider = PurchaseSpider()
    index_spider = IndexSpider()
    premium_calc = PremiumCalculator()
    
    await asyncio.gather(
        qdii_spider.run(),
        lof_spider.run(),
        index_spider.run(),
    )
    
    await purchase_spider.run()
    
    await premium_calc.update_all_premium()
    
    print(f"\n{'='*50}")
    print(f"[{datetime.now()}] 数据更新完成")
    print(f"{'='*50}\n")


def setup_scheduler():
    scheduler.add_job(
        fetch_all_data,
        trigger=IntervalTrigger(minutes=settings.scheduler_interval_minutes),
        id='fetch_data',
        name='获取基金数据',
        replace_existing=True,
    )
    print(f"定时任务已设置: 每 {settings.scheduler_interval_minutes} 分钟执行一次")


async def start_scheduler():
    setup_scheduler()
    print("正在执行首次数据获取...")
    await fetch_all_data()
    scheduler.start()
    print("定时调度器已启动")


async def stop_scheduler():
    scheduler.shutdown()
    print("定时调度器已停止")
