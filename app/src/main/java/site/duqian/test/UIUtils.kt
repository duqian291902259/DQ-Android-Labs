package site.duqian.test

import android.os.Build
import android.util.Log
import android.util.Printer
import android.view.Choreographer

/**
 * Des:
 * Created by 杜小菜 on 2021/1/25
 * E-mail:duqian2010@gmail.com
 **/
class UIUtils {
    inner class HandlerLogger : Printer {
        //打印log，可以统计时长，超过xxxms认为卡顿
        private var mLastHandleTime = 0L

        override fun println(x: String?) {
            //Dispatching to Handler (android.view.Choreographer$FrameHandler) {11822ed} android.view.Choreographer$FrameDisplayEventReceiver@2394522: 0
            val diff = System.currentTimeMillis() - mLastHandleTime
            Log.d("dq-log", "handler const time= $diff")
            if (diff>300){
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
                    Log.e("dq-log", "droppedFrames $droppedFrames")
                }
                mLastFrameTime = frameTimeNanos
                Choreographer.getInstance().postFrameCallback(this)
            }
        })
    }
}