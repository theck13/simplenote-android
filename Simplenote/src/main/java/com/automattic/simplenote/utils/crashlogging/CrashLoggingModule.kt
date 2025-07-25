package com.automattic.simplenote.utils.crashlogging

import android.app.Application
import com.automattic.android.tracks.crashlogging.CrashLogging
import com.automattic.android.tracks.crashlogging.CrashLoggingDataProvider
import com.automattic.android.tracks.crashlogging.CrashLoggingProvider
import com.automattic.simplenote.utils.locale.ContextBasedLocaleProvider
import com.automattic.simplenote.utils.locale.LocaleProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CrashLoggingModule {
    companion object {
        @Provides
        @Singleton
        fun provideCrashLogging(
            application: Application,
            crashLoggingDataProvider: CrashLoggingDataProvider
        ): CrashLogging {
            return CrashLoggingProvider.createInstance(
                application,
                crashLoggingDataProvider,
                CoroutineScope(Dispatchers.Default)
            )
        }
    }

    @Binds
    abstract fun bindCrashLoggingDataProvider(dataProvider: SimplenoteCrashLoggingDataProvider): CrashLoggingDataProvider

    @Binds
    abstract fun bindLocaleProvider(contextBasedLocaleProvider: ContextBasedLocaleProvider): LocaleProvider
}
