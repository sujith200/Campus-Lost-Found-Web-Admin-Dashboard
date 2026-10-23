package com.campus.lostfound

import android.app.Application
import androidx.core.content.edit
import com.campus.lostfound.data.CampusApi

object Session {
    private const val PREFS = "campus_session"
    private const val KEY = "token"

    @Volatile
    var token: String? = null

    fun load(app: Application) {
        token = app.getSharedPreferences(PREFS, 0).getString(KEY, null)
    }

    fun save(app: Application, t: String?) {
        token = t
        app.getSharedPreferences(PREFS, 0).edit {
            if (t == null) remove(KEY) else putString(KEY, t)
        }
    }
}

class CampusApp : Application() {
    val api: CampusApi by lazy { ApiModule.createApi() }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Session.load(this)
    }

    companion object {
        lateinit var instance: CampusApp
            private set
    }
}
