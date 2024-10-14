#include <jni.h>
#include <dirent.h>
#include <stdio.h>
#include <string.h>
#include <sys/types.h>
#include <unistd.h>
#include <android/log.h>
#include <bits/ctype_inlines.h>

#define LOG_TAG "FridaDetection"
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C"
JNIEXPORT jboolean JNICALL
Java_io_kommunicate_nativeLibs_FridaDetection_isFridaDetectedNative(JNIEnv *env, jclass clazz) {
    const char *knownFridaProcesses[] = { "frida-server", "frida-helper", "gum-js-loop" };
    DIR *dir;
    struct dirent *entry;
    char filename[256];

    if ((dir = opendir("/proc")) != NULL) {
        while ((entry = readdir(dir)) != NULL) {
            if (isdigit(entry->d_name[0])) { // Look for process directories (numeric)
                snprintf(filename, sizeof(filename), "/proc/%s/cmdline", entry->d_name);
                FILE *f = fopen(filename, "r");
                if (f) {
                    char cmdline[256];
                    fgets(cmdline, sizeof(cmdline), f);
                    fclose(f);

                    // Compare against known Frida processes
                    for (int i = 0; i < sizeof(knownFridaProcesses) / sizeof(knownFridaProcesses[0]); i++) {
                        if (strstr(cmdline, knownFridaProcesses[i]) != NULL) {
                            LOGI("Frida process detected: %s", cmdline);
                            closedir(dir);
                            return JNI_TRUE;
                        }
                    }
                }
            }
        }
        closedir(dir);
    }
    return JNI_FALSE;
}