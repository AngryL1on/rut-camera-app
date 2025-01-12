package dev.angryl1on.cameraapp

import android.app.Application
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig

class RutCameraApp : Application(), CameraXConfig.Provider {

    /**
     * Provides the [CameraXConfig] used by the CameraX library in this application.
     * Uses the default configuration provided by [Camera2Config].
     *
     * @return A [CameraXConfig] instance with the default configuration.
     */
    override fun getCameraXConfig(): CameraXConfig {
        return CameraXConfig.Builder.fromConfig(Camera2Config.defaultConfig())
            .build()
    }
}
