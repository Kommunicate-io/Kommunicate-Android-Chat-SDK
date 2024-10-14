#include <jni.h>
#include <dlfcn.h>
#include <stdio.h>
#include <android/log.h>
#include <sys/mman.h>
#include <unistd.h>
#include <string.h>

#define LOG_TAG "FridaDetection"
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C"
JNIEXPORT jboolean JNICALL
Java_io_kommunicate_nativeLibs_FridaDetection_checkForHooking(JNIEnv *env, jclass clazz) {
    void *handle = dlopen("libc.so", RTLD_NOW);
    if (!handle) {
        return JNI_FALSE;
    }

    // Get the original address of 'open'
    void *orig_open = dlsym(handle, "open");
    if (!orig_open) {
        dlclose(handle);
        return JNI_FALSE;
    }

    // Check if the memory page containing 'open' is executable (normal) or writable (hooked)
    size_t page_size = sysconf(_SC_PAGESIZE);
    void *page_start = (void *)((uintptr_t)orig_open & ~(page_size - 1));
    if (mprotect(page_start, page_size, PROT_READ | PROT_EXEC) == 0) {
        LOGI("No hooks detected for 'open'");
    } else {
        LOGI("Hook detected on 'open'");
        dlclose(handle);
        return JNI_TRUE;
    }

    dlclose(handle);
    return JNI_FALSE;
}