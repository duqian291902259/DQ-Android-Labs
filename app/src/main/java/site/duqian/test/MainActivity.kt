package site.duqian.test

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.SystemClock
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicLong

/**
 * 测试主线程卡顿，丢帧，自定义环形进度条控件，阴影效果
 * by duqian2010@gmail.com on 2021-01
 */
class MainActivity : AppCompatActivity() {

    private lateinit var mProgressView: ProgressView
    private lateinit var mProgressView2: ProgressView
    private var mProgress = 1f
    private var mUIUtils = UIUtils()
    private val mHandler: Handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (mProgress <= 100) {
                mProgressView.setProgress(mProgress)
                SystemClock.sleep(500) //测试主线程卡顿
                mProgressView2.setProgress(mProgress)
                mProgress++
            } else {
                mProgress = 1f
            }
            sendEmptyMessageDelayed(100, 200)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val handlerLogger = mUIUtils.HandlerLogger()
        mHandler.looper.setMessageLogging(handlerLogger)
        mUIUtils.frameMonitor()
        testUI()
        testKotlin()
    }

    companion object {
        const val TAG = "dq-kotlin "
    }

    private fun testKotlin() {
        val c = AtomicLong()
        val startTimeMillis = System.currentTimeMillis()
        println("$TAG Start $c $startTimeMillis ${Thread.currentThread().name}")
        // 启动一个协程
        GlobalScope.launch() {//Dispathcers.Main
            println("$TAG Hello  ${Thread.currentThread().name}")
            delay(1000)
            //同步执行
            ioTest()
        }

        println("$TAG main  ${Thread.currentThread().name}")

        for (i in 1..1_000_000L) {//廉价的携程
            GlobalScope.launch {
                c.addAndGet(i)
            }
        }
        Thread.sleep(500) // 等待 2 秒钟
        println("$TAG Stop ${c.get()} duration=${System.currentTimeMillis() - startTimeMillis}")
    }

    private suspend fun ioTest() {
        withContext(Dispatchers.IO) {
            println("$TAG 挂起函数，子线程执行")
        }
    }

    private fun testUI() {
        mProgressView = findViewById(R.id.progressView)
        mProgressView2 = findViewById(R.id.progressView2)

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            Snackbar.make(view, "Just a demo from duqian2010@gmail.com", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        mHandler.postDelayed({
            mProgressView.setProgress(mProgress)
            mProgressView2.setProgress(mProgress)
            mProgress++
            //Log.d("dq-pb", "progress1=$mProgress")
            mHandler.sendEmptyMessageDelayed(100, 1000)
        }, 1000)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                openFeaturePage()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openFeaturePage() {
        val intent = Intent()
        intent.setClassName(this, "com.duqian.progress.MainActivity")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeCallbacksAndMessages(null)
    }
}
