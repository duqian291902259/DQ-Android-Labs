#include <jni.h>
#include <string>
#include <malloc.h>
#include <cstring>
#include "gif_lib.h"
#include <android/log.h>
#include <android/bitmap.h>

#define  LOG_TAG    "duqian"
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define LOGI(msg)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, msg)
#define LOGD(msg)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, msg)

#define  argb(a, r, g, b) ( ((a) & 0xff) << 24 ) | ( ((b) & 0xff) << 16 ) | ( ((g) & 0xff) << 8 ) | ((r) & 0xff)

typedef struct GifBean {
//播放帧数  第几帧
    int current_frame;
    int total_frame;
//    延迟时间数组  长度不确定    malloc  帧  155   计算
    int *dealys;

} GifBean;

//绘制一张图片
void drawFrame(GifFileType *gifFileType, GifBean *gifBean, AndroidBitmapInfo info, void *pixels) {

//  当前帧
    SavedImage savedImage = gifFileType->SavedImages[gifBean->current_frame];
    //整幅图片的首地址
    int *px = (int *) pixels;
    //LOGE("gif  drawFrame1=%d  ", (*px));

    int pointPixel;
    GifImageDesc frameInfo = savedImage.ImageDesc;
    GifByteType gifByteType;//压缩数据
//    rgb数据     压缩工具
    ColorMapObject *colorMapObject = frameInfo.ColorMap;
//    Bitmap 往下便宜
    px = (int *) ((char *) px + info.stride * frameInfo.Top);
    //LOGE("gif  drawFrame2=%d  ", (*px));

    //    每一行的首地址
    int *line;
    for (int y = frameInfo.Top; y < frameInfo.Top + frameInfo.Height; ++y) {
        line = px;
        for (int x = frameInfo.Left; x < frameInfo.Left + frameInfo.Width; ++x) {
//            拿到每一个坐标的位置  索引    ---》  数据
            pointPixel = (y - frameInfo.Top) * frameInfo.Width + (x - frameInfo.Left);
//            索引   rgb   LZW压缩  字典   （）缓存在一个字典
//解压
            gifByteType = savedImage.RasterBits[pointPixel];
            //fixed by dq:Cause: null pointer dereference
            if (colorMapObject) {
                GifColorType gifColorType = colorMapObject->Colors[gifByteType];
                line[x] = argb(255, gifColorType.Red, gifColorType.Green, gifColorType.Blue);
            } else {
                //LOGE("gif drawFrame2 null pointer dereference, =%d  ", (*px));
            }
        }
        px = (int *) ((char *) px + info.stride);
        //LOGE("gif  drawFrame3=%d  ", (*px));
    }
}

extern "C"
JNIEXPORT jlong JNICALL
Java_site_duqian_ndk_GifHandler_loadPath(JNIEnv *env, jobject instance, jstring path_) {
    const char *path = env->GetStringUTFChars(path_, 0);
    int err;
    GifFileType *gifFileType = DGifOpenFileName(path, &err);
    DGifSlurp(gifFileType);
//new GifBean
    GifBean *gifBean = (GifBean *) malloc(sizeof(GifBean));
    //    清空内存地址  32 000000000
    memset(gifBean, 0, sizeof(GifBean));
    gifFileType->UserData = gifBean;
//    初始化数组
    gifBean->dealys = (int *) malloc(sizeof(int) * gifFileType->ImageCount);
    memset(gifBean->dealys, 0, sizeof(int) * gifFileType->ImageCount);
//延迟事件  读取
//    Delay Time - 单位1/100秒，如果值不为1，表示暂停规定的时间后再继续往下处理数据流
//    获取时间
    gifFileType->UserData = gifBean;
    gifBean->current_frame = 0;
    gifBean->total_frame = gifFileType->ImageCount;
    ExtensionBlock *ext;
    for (int i = 0; i < gifFileType->ImageCount; ++i) {
        SavedImage frame = gifFileType->SavedImages[i];
        for (int j = 0; j < frame.ExtensionBlockCount; ++j) {
            if (frame.ExtensionBlocks[j].Function == GRAPHICS_EXT_FUNC_CODE) {
                ext = &frame.ExtensionBlocks[j];
                break;
            }
        }
        if (ext) {
//	Delay Time - 单位1/100秒   1s/100
            int frame_delay = 10 * (ext->Bytes[1] | (ext->Bytes[2] << 8));//ms
            LOGE("gif 时间  %d   ", frame_delay);
            gifBean->dealys[i] = frame_delay;
        }
    }
    LOGE("gif  长度大小    %d  ", gifFileType->ImageCount);

    env->ReleaseStringUTFChars(path_, path);
    return (jlong) gifFileType;
}

extern "C"
JNIEXPORT jint JNICALL
Java_site_duqian_ndk_GifHandler_getWidth(JNIEnv *env, jobject instance, jlong ndkGif) {

    GifFileType *gifFileType = (GifFileType *) ndkGif;
    LOGE("gif  getWidth=%d  ", gifFileType->SWidth);
    return gifFileType->SWidth;
}

extern "C"
JNIEXPORT jint JNICALL
Java_site_duqian_ndk_GifHandler_getHeight(JNIEnv *env, jobject instance, jlong ndkGif) {
    GifFileType *gifFileType = (GifFileType *) ndkGif;
    LOGE("gif  getWidth=%d  ", gifFileType->SHeight);
    return gifFileType->SHeight;
}
//FFmpeg 渲染   1   没有 2   NDK  BItmap  window
extern "C"
JNIEXPORT jint JNICALL
Java_site_duqian_ndk_GifHandler_updateFrame(JNIEnv *env, jobject instance, jlong ndkGif,
                                            jobject bitmap) {


    GifFileType *gifFileType = (GifFileType *) ndkGif;
    GifBean *gifBean = (GifBean *) gifFileType->UserData;
    AndroidBitmapInfo info;
//    入参  出出参对象

//    像素数组
    AndroidBitmap_getInfo(env, bitmap, &info);
//    空的 gif --Bitmap
    void *pixels;
    AndroidBitmap_lockPixels(env, bitmap, &pixels);
    //LOGE("dq-gif drawFrame pixels  ", bitmap);
    try {
        drawFrame(gifFileType, gifBean, info, pixels);
    } catch (...) {
        LOGE("dq-gif drawFrame  ", ndkGif);
    }

    gifBean->current_frame += 1;
    if (gifBean->current_frame >= gifBean->total_frame - 1) {
        gifBean->current_frame = 0;
        LOGE("dq-gif 重新过来  %d  ", gifBean->current_frame);
    }

    AndroidBitmap_unlockPixels(env, bitmap);
    LOGE("gif current_frame=%d  ", gifBean->current_frame);

    return gifBean->dealys[gifBean->current_frame];
}

extern "C" {
void *create_stdstr(char *data, int size) {
    std::string *s = new std::string();
    (*s).assign(data, size);
    return s;
}

JNIEXPORT jstring JNICALL
Java_site_duqian_ndk_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++,杜小菜";
    char *hello2 = "duqian";
    LOGE("gif Hello from C++,杜小菜666", env);

    create_stdstr(hello2,sizeof(hello2));

    LOGI("gif Hello from C++");
    return env->NewStringUTF(hello.c_str());
    //return env->NewStringUTF(static_cast<const char *>(create_stdstr("abcd", 3)));
}


JNIEXPORT void JNICALL
Java_site_duqian_ndk_GifHandler_release(JNIEnv *env, jobject thiz) {
    LOGE("//todo 退出页面要释放资源");
}

}