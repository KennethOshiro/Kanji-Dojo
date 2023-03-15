package ua.syt0r.kanji.application

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import ua.syt0r.kanji.appModules
import ua.syt0r.kanji.flavorModule
import ua.syt0r.kanji.presentation.androidViewModelModule

class KanjiApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@KanjiApplication)
            loadKoinModules(appModules + androidViewModelModule + flavorModule)
        }
    }

}