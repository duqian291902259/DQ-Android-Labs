package site.duqian.test

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.DisplayMetrics
import android.util.Log
import android.util.Printer
import android.view.Choreographer

/**
 * Des:UI工具类
 * Created by 杜小菜 on 2021/1/25
 * E-mail:duqian2010@gmail.com
 **/
class UIUtils {
    companion object {
        private const val TAG = "dq-UIUtils"
    }

    inner class HandlerLogger : Printer {
        //打印log，可以统计时长，超过xxxms认为卡顿
        private var mLastHandleTime = 0L
        override fun println(x: String?) {
            //Dispatching to Handler (android.view.Choreographer$FrameHandler) {11822ed} android.view.Choreographer$FrameDisplayEventReceiver@2394522: 0
            val diff = System.currentTimeMillis() - mLastHandleTime
            //Log.d("dq-log", "handler const time= $diff")
            if (diff > 300) {
                Log.e("dq-log", "Please check task ,ui time = $diff")
            }
            mLastHandleTime = System.currentTimeMillis()
        }
    }

    private var mLastFrameTime = 0L

    fun frameMonitor() {//帧率检测
        Choreographer.getInstance().postFrameCallback(object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                //Log.d("dq-log", "doFrame $frameTimeNanos")
                val diff = (frameTimeNanos - mLastFrameTime) / 1_000_000
                val frameCountPerSecond = 1000 * 1f / 60
                if (diff > frameCountPerSecond) {
                    val droppedFrames = diff / frameCountPerSecond
                    Log.d("log", "droppedFrames $droppedFrames")
                }
                mLastFrameTime = frameTimeNanos
                Choreographer.getInstance().postFrameCallback(this)
            }
        })
    }

    /**
     * 小米6手机，设备dpi为3，480
     * 原始图片，72*72*4=20736，图片放在hdpi目录中，密度值240
     * 实际加载，144*144*4=82944，设备密度值480，宽高都放大了2倍，实际内存放大了4倍
     */
    fun testPngSize(resources: Resources) {
        try {
            //xxhdpi中放了test_size.png后，width=72,height=72,memory=20736
            //xxhdpi中删除，放在hdpi目录后获取：width=144,height=144,memory=82944
            val bitmap: Bitmap? =
                BitmapFactory.decodeResource(resources, R.mipmap.test_size, null)
            Log.d(TAG, "width=" + bitmap?.width + ",height=" + bitmap?.height + ",memory=" + bitmap?.byteCount)
            val metrics: DisplayMetrics = resources.displayMetrics
            Log.d(TAG, "the density=" + metrics.density)
        } catch (e: Exception) {
            Log.e(TAG, "testPngSize error = $e")
        }
    }
}