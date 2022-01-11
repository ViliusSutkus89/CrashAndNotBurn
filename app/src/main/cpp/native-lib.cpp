#include <jni.h>
#include <string>
#include <android/log.h>

extern "C"
JNIEXPORT void JNICALL
Java_com_viliussutkus89_crashandnotburn_RemoteCrashWorker_crash(JNIEnv *env, jclass remoteCrashWorker) {
    __android_log_print(ANDROID_LOG_ERROR, "crash", "raising SIGSEGV");
    raise(SIGSEGV);
    __android_log_print(ANDROID_LOG_ERROR, "crash", "SIGSEGV raised");

    __android_log_print(ANDROID_LOG_ERROR, "crash", "calling abort");
    abort();
    __android_log_print(ANDROID_LOG_ERROR, "crash", "abort called");
}
