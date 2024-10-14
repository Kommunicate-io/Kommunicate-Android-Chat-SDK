#include <jni.h>
#include <stdio.h>
#include <sys/mman.h>
#include <android/log.h>
#include <string.h>

#define LOG_TAG "FridaDetection"
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C"
JNIEXPORT jboolean JNICALL
Java_io_kommunicate_nativeLibs_FridaDetection_detectFridaByLibrary(JNIEnv *env, jclass clazz) {
    FILE *maps = fopen("/proc/self/maps", "r");
    if (!maps) {
        return JNI_FALSE;
    }

    char line[256];
    while (fgets(line, sizeof(line), maps)) {
        if (strstr(line, "libfrida-gadget.so")) {
            LOGI("Frida Gadget detected in memory!");
            fclose(maps);
            return JNI_TRUE;
        }
    }

    fclose(maps);
    return JNI_FALSE;
}