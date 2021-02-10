package site.duqian.test

import android.app.Application
import android.content.Context
import android.util.Log
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
    override fun onCreate() {
        super.onCreate()
        mContext = this

        /*Observable.create<String> { emitter ->
            Log.d("dq-log", "subscribe: " + Thread.currentThread().name)
            emitter.onNext("hello")
            emitter.onNext("杜小菜")
        }.subscribeOn(Schedulers.newThread())
            .subscribe { s ->
                Log.d("dq-log", "initFlutterEngine " + Thread.currentThread().name + "#Next: " + s)
            }*/

        copyFlutterSo()

        if (mHasCopyFlutterSo) {
            //initFlutterEngine() //Methods marked with @UiThread must be executed on the main thread.
        }
    }

    private var mHasCopyFlutterSo = false

    //nativeLibraryDirectories=[/data/user/0/site.duqian.so.test/files/libs/arm64-v8a/libflutter.so,
    // /data/app/site.duqian.so.test-2/lib/x86, /data/app/site.duqian.so.
    private fun copyFlutterSo() {
        //将so拷贝到指定路径，并加载
        val rootLibDir = mContext.filesDir.absolutePath
        val soTest = "$rootLibDir/libs/x86_64/libflutter.so" //x86
        //注入flutter的本地so路径
        val installNativeLibraryPath =
            LoadLibraryUtil.installNativeLibraryPath(mContext.classLoader, soTest)
        if (File(soTest).exists()) {
            mHasCopyFlutterSo = installNativeLibraryPath
            Log.d("dq-so", "rootLibDir=$rootLibDir，mHasCopyFlutterSo=$mHasCopyFlutterSo")
        } else {
            Log.d("dq-so", "so not exist")

        }
        if (File("/data/user/0/site.duqian.so.test/files/libs/x86_64/libflutter.so").exists()) {
            mHasCopyFlutterSo = installNativeLibraryPath
            Log.d("dq-so", "rootLibDir=$rootLibDir，mHasCopyFlutterSo2222=$mHasCopyFlutterSo")
        } else {
            Log.d("dq-so", "so not exist222")
        }
    }


    private fun initFlutterEngine() {
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
    }

}