package site.duqian.test

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor
import java.io.File

/**
 * Des:自定义Application，实现各类sdk的初始化
 * Created by 杜小菜 on 2021/1/25
 * E-mail:duqian2010@gmail.com
 **/
class DQApplication : Application() {
    var flutterEngine: FlutterEngine? = null
    lateinit var mContext: Context
    private var mHasCopyFlutterSo = false
    private val mUIHandler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }

    companion object {
        lateinit var mApplication: DQApplication
    }

    override fun onCreate() {
        super.onCreate()
        mContext = this
        mApplication = this
        var hasWritePermission = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hasWritePermission = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED

        }
        if (BuildConfig.DEBUG) {// TODO: 2/20/21 debug环境不去除flutter的so
            initFlutterEngine() //测试环境，release包才有对应的libflutter.so测试so动态加载，
            return
        }
        if (hasWritePermission) {
            copyFlutterSo()
        } else {
            //todo 请（手动）给应用赋予sdcard权限后再试
        }
    }

    private fun initFlutterEngine() {
        try {
            flutterEngine = FlutterEngine(mContext)

            // Start executing Dart code to pre-warm the FlutterEngine.
            flutterEngine!!.dartExecutor.executeDartEntrypoint(
                DartExecutor.DartEntrypoint.createDefault()
            )

            // Cache the FlutterEngine to be used by FlutterActivity.
            FlutterEngineCache
                .getInstance()
                .put("dq_engine_id", flutterEngine)
            Log.d("dq-log", "FlutterEngineCache")
        } catch (e: Exception) {
            //Error while initializing the Dart VM: Precompiled runtime requires a precompiled snapshot
            Log.d("dq-log", "FlutterEngineCache $e")
        }
    }

    private val sdcardLibDir = Environment.getExternalStorageDirectory().absolutePath // + "/libs";

    @Synchronized
    fun copyFlutterSo() {
        //将so拷贝到指定路径，并加载
        //val rootLibDir = "${this.filesDir.absolutePath}/libs/"
        val rootLibDir = "$sdcardLibDir/libs"
        val cpuArchType: String = SoUtils.getCpuArchType()
        val soTest = "$rootLibDir/$cpuArchType/libflutter.so"
        val exists = File(soTest).exists()
        if (exists) {
            loadSoAndInit()
            return
        }

        //简单搞个后台线程,copy so,如果copy会失败， 请手动将assets目录的libs文件夹，拷贝到sdcard根目录
        Thread {
            val isSoExist = SoUtils.copyAssetsDirectory(this, "libs", rootLibDir)
            Log.d("dq-so", "rootLibDir=$rootLibDir，copy from assets $isSoExist")
            if (isSoExist) {
                loadSoAndInit()
            }
        }.start()
    }

    private fun loadSoAndInit() {
        val soFrom: String = SoUtils.getSoSourcePath()
        //注入so路径，如果清除了的话。没有清除可以不用每次注入
        val loadSoFile = SoFileLoadManager.loadSoFile(this, soFrom)
        mHasCopyFlutterSo = loadSoFile

        if (mHasCopyFlutterSo) {
            mUIHandler.post {
                initFlutterEngine() //Methods marked with @UiThread must be executed on the main thread.
            }
        }
        Log.d("dq-so", "loadSoFile soFrom2=$soFrom,mHasCopyFlutterSo=$mHasCopyFlutterSo")
    }

}