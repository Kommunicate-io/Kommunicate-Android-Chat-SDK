package io.kommunicate.nativeLibs

object RootDetection {
    init {
        System.loadLibrary("root_detection"); // Load native library
    }

    @JvmStatic
    external fun isDeviceRooted(): Boolean
}
