package com.automattic.simplenote.utils.crashlogging

import com.automattic.android.tracks.crashlogging.CrashLoggingDataProvider
import com.automattic.android.tracks.crashlogging.CrashLoggingUser
import com.automattic.android.tracks.crashlogging.EventLevel
import com.automattic.android.tracks.crashlogging.ExtraKnownKey
import com.automattic.android.tracks.crashlogging.PerformanceMonitoringConfig
import com.automattic.android.tracks.crashlogging.ReleaseName
import com.automattic.simplenote.BuildConfig
import com.automattic.simplenote.Simplenote
import com.automattic.simplenote.utils.locale.LocaleProvider
import com.simperium.client.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import java.util.Locale
import javax.inject.Inject

class SimplenoteCrashLoggingDataProvider @Inject constructor(
    app: Simplenote,
    private val localeProvider: LocaleProvider,
) : CrashLoggingDataProvider {

    override val buildType = BuildConfig.BUILD_TYPE
    override val enableCrashLoggingLogs = false
    override val sentryDSN: String = BuildConfig.SENTRY_DSN

    override val locale: Locale?
        get() = localeProvider.provideLocale()

    override val releaseName: ReleaseName = if (BuildConfig.DEBUG) {
        ReleaseName.SetByApplication(DEBUG_RELEASE_NAME)
    } else {
        ReleaseName.SetByTracksLibrary
    }

    override val user: Flow<CrashLoggingUser?> = MutableStateFlow(app.simperium?.user?.toCrashLoggingUser())

    override val applicationContextProvider: Flow<Map<String, String>> = emptyFlow()

    override val performanceMonitoringConfig: PerformanceMonitoringConfig
        get() = PerformanceMonitoringConfig.Disabled

    override fun crashLoggingEnabled(): Boolean {
        return Simplenote.analyticsIsEnabled()
    }

    override fun extraKnownKeys(): List<ExtraKnownKey> {
        return emptyList()
    }

    override fun provideExtrasForEvent(
        currentExtras: Map<ExtraKnownKey, String>,
        eventLevel: EventLevel
    ): Map<ExtraKnownKey, String> {
        return emptyMap()
    }

    override fun shouldDropWrappingException(module: String, type: String, value: String): Boolean {
        return false
    }

    private fun User.toCrashLoggingUser(): CrashLoggingUser? {
        if (userId.isNullOrEmpty()) return null

        return CrashLoggingUser(
            userID = userId,
            email = email.orEmpty(),
            username = ""
        )
    }

    companion object {
        const val DEBUG_RELEASE_NAME = "debug"
    }
}
