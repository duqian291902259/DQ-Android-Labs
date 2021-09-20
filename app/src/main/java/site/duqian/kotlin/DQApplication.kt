package site.duqian.kotlin

import android.app.Application
import android.content.Context
import android.util.Log
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers


/**
 * Des:自定义Application，实现各类sdk的初始化
 * Created by 杜小菜 on 2021/1/25
 * E-mail:duqian2010@gmail.com
 **/
class DQApplication : Application() {
    //var flutterEngine: FlutterEngine? = null
    lateinit var mContext: Context
    override fun onCreate() {
        super.onCreate()
        mContext = this

        Observable.create<String> { emitter ->
            Log.d("dq-log", "subscribe: " + Thread.currentThread().name)
            emitter.onNext("hello")
            emitter.onNext("杜小菜")
        }.subscribeOn(Schedulers.newThread())
            .subscribe { s ->
                Log.d("dq-log", "initFlutterEngine " + Thread.currentThread().name + "#Next: " + s)
            }

        //initFlutterEngine() //Methods marked with @UiThread must be executed on the main thread.

    }

    /*private fun initFlutterEngine() {
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
    }*/

}