package site.duqian.ndk;

import android.graphics.Bitmap;

public class GifHandler {
    //    ndkGif  native  结构体的地址
//
//    存放在 java   为了方便传参
    private long gifAddr;

    /*static {
        System.loadLibrary("native-lib");
    }*/
    public GifHandler(String path) {
//        加载  信使  GifFileType
        this.gifAddr = loadPath(path);
    }

    public int getWidth() {
        return getWidth(gifAddr);
    }

    public int getHeight() {
        return getHeight(gifAddr);
    }

    public int updateFrame(Bitmap bitmap) {
        return updateFrame(gifAddr, bitmap);
    }

    //初始化   调用
    private native long loadPath(String path);

    public native int getWidth(long ndkGif);

    public native int getHeight(long ndkGif);

    //    隔一段事件 调用一次
    public native int updateFrame(long ndkGif, Bitmap bitmap);

    // TODO: 2020-02-01 退出页面要释放资源
    public native void release();

}
