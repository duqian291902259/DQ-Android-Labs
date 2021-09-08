package site.duqian.test

import android.content.Context
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * Description:Jacoco工具类
 *
 * @author n20241 Created by 杜小菜 on 2021/9/8 - 11:55 . E-mail: duqian2010@gmail.com
 */
object CCJacocoHelper {
    private const val TAG = "CCJacocoHelper"

    //ec文件的路径
    private val DEFAULT_COVERAGE_ROOT_DIR =
        Environment.getExternalStorageDirectory().absolutePath + "/Android/data/com.netease.cc/coverage/"

    /**
     * 生成ec文件
     *
     * @param isNew 是否重新创建ec文件
     */
    fun generateEcFile(context: Context?, isNew: Boolean) {
        Thread().run {
            var out: OutputStream? = null
            //todo-dq 按照时间戳命名?
            val fileName = "cc_jacoco_${System.currentTimeMillis()}.ec"
            var rootDir = context?.externalCacheDir?.absolutePath + File.separator
            if (TextUtils.isEmpty(rootDir)) {
                rootDir = DEFAULT_COVERAGE_ROOT_DIR;
            }
            val path = rootDir + fileName
            Log.d(TAG, "generateEcFile path is $path")

            val mCoverageFile = File(path)
            try {
                File(rootDir).mkdirs()
                if (isNew && mCoverageFile.exists()) {
                    Log.d(TAG, "delete old ec file")
                    mCoverageFile.delete()
                }
                if (!mCoverageFile.exists()) {
                    mCoverageFile.createNewFile()
                }
                out = FileOutputStream(mCoverageFile.path, true)
                val agent = Class.forName("org.jacoco.agent.rt.RT")
                    .getMethod("getAgent")
                    .invoke(null)
                if (agent != null) {
                    out.write(
                        agent.javaClass.getMethod(
                            "getExecutionData",
                            Boolean::class.javaPrimitiveType
                        )
                            .invoke(agent, false) as ByteArray
                    )
                } else {
                    Log.e(TAG, "generateEcFile agent is null")
                }
            } catch (e: Exception) {
                Log.e(TAG, "generateEcFile error=$e")
            } finally {
                try {
                    out?.close()
                } catch (e: Exception) {
                }
            }
        }
    }
}