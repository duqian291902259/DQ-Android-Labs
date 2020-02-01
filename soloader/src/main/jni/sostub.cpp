#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring

JNICALL
//site.duqian.soloader.MainActivity.getStringFromCPP
Java_site_duqian_soloader_MainActivity_getStringFromCPP(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "soloader from 杜小菜,2020";
    return env->NewStringUTF(hello.c_str());
}
