package ua.syt0r.kanji.di

import android.app.ActivityManager
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.media3.exoplayer.ExoPlayer
import androidx.work.WorkManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import ua.syt0r.kanji.AndroidMainBuildConfig
import ua.syt0r.kanji.core.AndroidThemeManager
import ua.syt0r.kanji.core.BuildConfig
import ua.syt0r.kanji.core.app_data.AppDataDatabaseProvider
import ua.syt0r.kanji.core.app_data.AppDataDatabaseProviderAndroid
import ua.syt0r.kanji.core.backup.AndroidPlatformFileHandler
import ua.syt0r.kanji.core.backup.PlatformFileHandler
import ua.syt0r.kanji.core.logger.LoggerConfiguration
import ua.syt0r.kanji.core.notification.ReminderNotificationContract
import ua.syt0r.kanji.core.notification.ReminderNotificationHandleScheduledActionUseCase
import ua.syt0r.kanji.core.notification.ReminderNotificationManager
import ua.syt0r.kanji.core.notification.ReminderNotificationScheduler
import ua.syt0r.kanji.core.theme_manager.ThemeManager
import ua.syt0r.kanji.core.tts.AndroidKanaTtsManager
import ua.syt0r.kanji.core.tts.KanaTtsManager
import ua.syt0r.kanji.core.tts.Neural2BKanaVoiceData
import ua.syt0r.kanji.core.user_data.AndroidUserDataDatabaseManager
import ua.syt0r.kanji.core.user_data.practice.db.UserDataDatabaseManager
import ua.syt0r.kanji.core.user_data.preferences.DataStoreUserPreferencesManager
import ua.syt0r.kanji.core.user_data.preferences.UserPreferencesManager

actual val platformComponentsModule: Module = module {

    factory { LoggerConfiguration(isEnabled = BuildConfig.DEBUG) }

    factory { ExoPlayer.Builder(androidContext()).build() }

    factory<KanaTtsManager> {
        AndroidKanaTtsManager(
            player = get(),
            voiceData = Neural2BKanaVoiceData(AndroidMainBuildConfig.kanaVoiceAssetName)
        )
    }

    single<AppDataDatabaseProvider> {
        AppDataDatabaseProviderAndroid(
            context = androidContext()
        )
    }

    single<UserDataDatabaseManager> {
        AndroidUserDataDatabaseManager(
            context = androidContext()
        )
    }

    factory<PlatformFileHandler> {
        AndroidPlatformFileHandler(
            contentResolver = androidContext().contentResolver
        )
    }

    single<UserPreferencesManager> {
        val dataStore = PreferenceDataStoreFactory.create(
            migrations = DataStoreUserPreferencesManager.DefaultMigrations,
            produceFile = { androidContext().preferencesDataStoreFile("preferences") }
        )
        DataStoreUserPreferencesManager(
            dataStore = dataStore,
            migrations = DataStoreUserPreferencesManager.DefaultMigrations
        )
    }

    single<ThemeManager> {
        AndroidThemeManager(userPreferencesRepository = get())
    }

    factory<WorkManager> { WorkManager.getInstance(androidContext()) }
    factory<NotificationManagerCompat> { NotificationManagerCompat.from(androidContext()) }
    factory<ActivityManager> { androidContext().getSystemService<ActivityManager>()!! }

    factory<ReminderNotificationContract.Scheduler> {
        ReminderNotificationScheduler(
            workManger = get(),
            timeUtils = get()
        )
    }

    factory<ReminderNotificationContract.Manager> {
        ReminderNotificationManager(
            context = androidContext(),
            notificationManager = get()
        )
    }

    factory<ReminderNotificationContract.HandleScheduledActionUseCase> {
        ReminderNotificationHandleScheduledActionUseCase(
            activityManager = get(),
            letterSrsManager = get(),
            vocabSrsManager = get(),
            notificationManager = get(),
            repository = get(),
            scheduler = get(),
            analyticsManager = get()
        )
    }

}