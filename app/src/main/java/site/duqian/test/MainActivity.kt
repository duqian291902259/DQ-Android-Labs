package site.duqian.test

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.android.FlutterActivityLaunchConfigs
import kotlinx.coroutines.*
import java.io.File
import java.util.concurrent.atomic.AtomicLong

/**
 * 测试主线程卡顿，丢帧，自定义环形进度条控件，阴影效果
 * by duqian2010@gmail.com on 2021-01
 */
class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "dq-kotlin "
    }

    private val sdcardLibDir = Environment.getExternalStorageDirectory().absolutePath // + "/libs";

    private lateinit var mProgressView: ProgressView
    private lateinit var mTvDemo1: View
    private var mProgress = 1f
    private var mUIUtils = UIUtils()
    private val mHandler: Handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (mProgress <= 100) {
                SystemClock.sleep(500) //测试主线程卡顿
                mProgressView.setProgress(mProgress)
                mProgress++
            } else {
                mProgress = 1f
            }
            sendEmptyMessageDelayed(100, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        applyForPermissions()
        val handlerLogger = mUIUtils.HandlerLogger()
        mHandler.looper.setMessageLogging(handlerLogger)
        mUIUtils.frameMonitor()
        testUI()
        testFlutter()
        testKotlin()

        copyFlutterSo()
    }

    private fun applyForPermissions() { //申请sdcard读写权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val hasWritePermission = ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
            val hasReadPermission = ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasWritePermission || !hasReadPermission) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ), 1000
                )
            }
        }
    }


    private var mHasCopyFlutterSo = false

    private fun copyFlutterSo() {
        //将so拷贝到指定路径，并加载
        //val rootLibDir = "${this.filesDir.absolutePath}/libs/"
        val rootLibDir = "$sdcardLibDir/libs"
        //val cpuArchType = "armeabi-v7a"//"arm64-v8a"

        val cpuArchType: String = SoUtils.getCpuArchType()
        val soTest = "$rootLibDir/$cpuArchType/app.so"
        //val soTest = "$rootLibDir/$cpuArchType/libflutter.so"
        //注入flutter的本地so路径
        val soRootDir = "$rootLibDir/$cpuArchType/"

        /*val installNativeLibraryPath = LoadLibraryUtil.installNativeLibraryPath(
            this.classLoader,
            soRootDir
        )*/

        val exists = File(soTest).exists()
        if (exists) {
            val soFrom: String = SoUtils.getSoSourcePath()
            //注入so路径，如果清除了的话。没有清除可以不用每次注入
            SoFileLoadManager.loadSoFile(this, soFrom)
            Log.d("dq-so", "soFrom=$soFrom")
        }
        try {
            //System.loadLibrary("flutter") //系统方法也能正常加载
        } catch (e: Exception) {
            Log.d("dq-so", "loadLibrary  error $e")
        }

        //简单搞个后台线程,copy so,如果copy会失败， 请手动将assets目录的libs文件夹，拷贝到sdcard根目录
        Thread {
            val isSoExist = SoUtils.copyAssetsDirectory(this, "libs", rootLibDir)
            Log.d("dq-so", "rootLibDir=$rootLibDir，copy from assets $isSoExist")
            mHasCopyFlutterSo = isSoExist
        }.start()
    }

    private fun testKotlin() {
        val startTimeMillis = System.currentTimeMillis()
        println("$TAG Start $startTimeMillis ${Thread.currentThread().name}")
        // 主线程启动一个协程
        GlobalScope.launch() {//Dispatchers.Main
            println("$TAG Hello  ${Thread.currentThread().name}")
            delay(1000)
            //同步执行
            ioTest()
        }

        println("$TAG main  ${Thread.currentThread().name}")

        Thread.sleep(500) // 等待 2 秒钟
        println("$TAG Stop duration=${System.currentTimeMillis() - startTimeMillis}")
    }

    private suspend fun ioTest() {
        withContext(Dispatchers.IO) {
            val startTimeMillis = System.currentTimeMillis()

            println("$TAG 挂起函数，子线程执行 $startTimeMillis")
            val c = AtomicLong()
            for (i in 1..1_000_000L) {//廉价的携程
                //GlobalScope.launch(Dispatchers.IO) {
                c.addAndGet(i)
                //}
            }
            println("$TAG Stop ${c.get()} duration2=${System.currentTimeMillis() - startTimeMillis}")
        }
    }

    private fun testUI() {
        mProgressView = findViewById(R.id.progressView2)
        mTvDemo1 = findViewById<TextView>(R.id.tv_demo1)

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            Snackbar.make(view, "Just a demo from duqian2010@gmail.com", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        mHandler.postDelayed({
            mProgressView.setProgress(mProgress)
            mProgress++
            //Log.d("dq-pb", "progress1=$mProgress")
            mHandler.sendEmptyMessageDelayed(100, 1000)
        }, 3000)
    }

    private fun testFlutter() {
        mTvDemo1.setOnClickListener {
            /*startActivity(//跳转到指定的flutter页面,很慢
                FlutterActivity
                    .withNewEngine()
                    .initialRoute("dq_flutter_page")
                    .backgroundMode(FlutterActivityLaunchConfigs.BackgroundMode.transparent)
                    .build(this)
            )*/
            startActivity(//使用指定的渲染引擎，预先初始化了engine，很快
                FlutterActivity
                    .withCachedEngine("dq_engine_id")
                    .build(this)
            )
            startActivity(Intent(this, FlutterActivity::class.java))
            //startActivity(FlutterActivity.createDefaultIntent(this))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                //openFeaturePage()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openFeaturePage() {
        val intent = Intent()
        intent.setClassName(this, "com.duqian.progress.MainActivity")
        intent.setPackage("site.duqian.dynamic_feature")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeCallbacksAndMessages(null)
    }
}
