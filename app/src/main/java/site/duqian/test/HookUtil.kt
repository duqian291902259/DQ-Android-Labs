package site.duqian.test

import android.util.Log

/**
 * Description:hook工具类
 * @author n20241 Created by 杜小菜 on 2021/12/7 - 15:50 .
 * E-mail: duqian2010@gmail.com
 */
class HookUtil {
    /*fun hookFlutterAppSoPath() {
        try {
            val clazz = Class.forName("io.flutter.embedding.engine.loader.FlutterApplicationInfo")
            DexposedBridge.hookAllConstructors(clazz, object : XC_MethodHook() {
                @Throws(Throwable::class)
                protected fun beforeHookedMethod(param: MethodHookParam) {
                    super.beforeHookedMethod(param)
                    val args: Array<Any> = param.args
                    if (args != null && args.size > 0) {
                        for (`object` in args) {
                            Log.d("dq-hook", "beforeHookedMethod param=$`object`")
                        }
                    }
                    //设置本地so路径
                }

                @Throws(Throwable::class)
                protected fun afterHookedMethod(param: MethodHookParam) {
                    super.afterHookedMethod(param)
                    val args: Array<Any> = param.args
                    if (args != null && args.size > 0) {
                        for (`object` in args) {
                            Log.d("dq-hook", "afterHookedMethod param2=$`object`")
                        }
                    }
                }
            })
        } catch (e: Exception) {
            Log.e("dq-hook", "hookFlutterAppSoPath error :%s", e)
        }
    }*/
}