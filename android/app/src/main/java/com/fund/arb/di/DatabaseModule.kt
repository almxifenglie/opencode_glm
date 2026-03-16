package com.fund.arb.di

import android.content.Context
import androidx.room.Room
import com.fund.arb.data.local.dao.*
import com.fund.arb.data.local.db.AppDatabase
import com.fund.arb.data.repository.FundRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "fund_arb.db"
        ).build()
    }
    
    @Provides
    fun provideFundDataDao(database: AppDatabase): FundDataDao {
        return database.fundDataDao()
    }
    
    @Provides
    fun providePremiumHistoryDao(database: AppDatabase): PremiumHistoryDao {
        return database.premiumHistoryDao()
    }
    
    @Provides
    fun provideNavHistoryDao(database: AppDatabase): NavHistoryDao {
        return database.navHistoryDao()
    }
    
    @Provides
    fun provideWatchlistDao(database: AppDatabase): WatchlistDao {
        return database.watchlistDao()
    }
    
    @Provides
    @Singleton
    fun provideFundRepository(
        fundDataDao: FundDataDao,
        premiumHistoryDao: PremiumHistoryDao,
        navHistoryDao: NavHistoryDao,
        watchlistDao: WatchlistDao
    ): FundRepository {
        return FundRepository(fundDataDao, premiumHistoryDao, navHistoryDao, watchlistDao)
    }
}
