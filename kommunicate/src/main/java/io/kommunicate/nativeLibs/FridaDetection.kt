package io.kommunicate.nativeLibs

object FridaDetection {
    init {
        System.loadLibrary("root_detection")
    }

    @JvmStatic
    external fun isFridaDetectedNative(): Boolean

    @JvmStatic
    external fun detectFridaByLibrary(): Boolean

    @JvmStatic
    external fun checkForHooking(): Boolean
}