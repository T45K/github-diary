package io.github.t45k.githubDiary

import android.annotation.SuppressLint
import android.content.Context

/**
 * Provides application context for platform-specific code that needs Context.
 * Must be initialized in Application or MainActivity before use.
 */
@SuppressLint("StaticFieldLeak")
object AndroidContextProvider {
    lateinit var applicationContext: Context
        private set

    fun init(context: Context) {
        applicationContext = context.applicationContext
    }
}
