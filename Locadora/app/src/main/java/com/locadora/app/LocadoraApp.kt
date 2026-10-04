package com.locadora.app

import android.app.Application

class LocadoraApp : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
