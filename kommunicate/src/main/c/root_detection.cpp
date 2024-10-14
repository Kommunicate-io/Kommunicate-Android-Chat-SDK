#include <jni.h>
#include <unistd.h>
#include <sys/stat.h>
#include <android/log.h>
#include <stdio.h>

#define LOG_TAG "NativeRootDetection"
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

// List of root files and paths
const char *rootPaths[] = {
        "/system/app/Superuser.apk",
        "/sbin/su",
        "/system/bin/su",
        "/system/xbin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/data/local/su",
        "/su/bin/su"
};

// Check if any root files exist
bool checkForSUFiles() {
    for (int i = 0; i < sizeof(rootPaths) / sizeof(rootPaths[0]); ++i) {
        struct stat fileStat;
        if (stat(rootPaths[i], &fileStat) == 0) {
            LOGI("Root file found: %s", rootPaths[i]);
            return true;
        }
    }
    return false;
}

// Check for "su" command execution
bool canExecuteSUCommand() {
    FILE *process = popen("which su", "r");
    if (process != nullptr) {
        char buffer[128];
        if (fgets(buffer, sizeof(buffer), process) != nullptr) {
            LOGI("SU command executed: %s", buffer);
            pclose(process);
            return true;
        }
        pclose(process);
    }
    return false;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_io_kommunicate_nativeLibs_RootDetection_isDeviceRooted(JNIEnv *env, jclass clazz) {
    return checkForSUFiles() || canExecuteSUCommand();
}