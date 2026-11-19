package com.cafeuni.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Clase principal de la aplicación anotada con @HiltAndroidApp
 * para la inicialización automática del contenedor de dependencias Hilt.
 */
@HiltAndroidApp
class CafeUniApp : Application()
