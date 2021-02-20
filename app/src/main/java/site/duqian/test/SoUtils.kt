package site.duqian.test

import android.content.Context
import android.content.res.AssetManager
import android.os.Build
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream

/**
 * Description:So工具类
 * @author 杜小菜,Created on 2/10/21 - 8:04 PM.
 * E-mail:duqian2010@gmail.com
 */
object SoUtils {

    fun getSoSourcePath(): String {
        var cpuArchType: String = getCpuArchType()
        if (TextUtils.isEmpty(cpuArchType)) {
            cpuArchType = "armeabi-v7a"//"arm64-v8a"
        }
        val rootSdcard = Environment.getExternalStorageDirectory().absolutePath

        //写死测试
        //cpuArchType = "armeabi-v7a"//"arm64-v8a"

        val soFrom = "$rootSdcard/libs/$cpuArchType/"
        val file = File(soFrom)
        if (!file.exists()) {
            file.mkdirs()
        }
        Log.d("dq-so", "cpuArchType=$cpuArchType,soFrom=$soFrom")
        return soFrom
    }

    fun getCpuArchType(): String {
        var arch = ""
        try {
            val clazz = Class.forName("android.os.SystemProperties")
            val get = clazz.getDeclaredMethod(
                "get", *arrayOf<Class<*>>(
                    String::class.java
                )
            )
            arch = get.invoke(clazz, *arrayOf<Any>("ro.product.cpu.abi")) as String
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        if (TextUtils.isEmpty(arch)) {
            arch = Build.CPU_ABI //可能不准确？
        }
        Log.d("dq getCpuArchType", "arch $arch")
        return arch
    }

    fun copyAssetsDirectory(context: Context, fromAssetPath: String, toPath: String): Boolean {
        return try {
            val assetManager = context.assets
            val files = context.assets.list(fromAssetPath)
            if (isExist(toPath)) {
                //deleteFile(toPath);//很危险
            } else {
                File(toPath).mkdirs()
            }
            var res = true
            for (file in files!!) res = if (file.contains(".")) {
                res and copyAssetFile(
                    assetManager,
                    "$fromAssetPath/$file", "$toPath/$file"
                )
            } else {
                res and copyAssetsDirectory(
                    context,
                    "$fromAssetPath/$file", "$toPath/$file"
                )
            }
            res
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun isExist(path: String): Boolean {
        if (TextUtils.isEmpty(path)) {
            return false
        }
        return try {
            val file = File(path)
            file.exists()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 删除文件或文件夹(包括目录下的文件)
     *
     * @param filePath
     */
    fun deleteFile(filePath: String): Boolean {
        if (TextUtils.isEmpty(filePath)) {
            return false
        }
        if (Environment.getExternalStorageDirectory().absolutePath == filePath) {
            return false //防止直接删除了sdcard根目录
        }
        try {
            val f = File(filePath)
            if (f.exists() && f.isDirectory) {
                val delFiles = f.listFiles()
                if (delFiles != null && delFiles.isNotEmpty()) {
                    for (i in delFiles.indices) {
                        deleteFile(delFiles[i].absolutePath)
                    }
                }
            }
            f.delete()
        } catch (e: Exception) {
            return false
        }
        return true
    }

    private fun copyAssetFile(
        assetManager: AssetManager?,
        fromAssetPath: String?,
        toPath: String?
    ): Boolean {
        if (assetManager == null || TextUtils.isEmpty(fromAssetPath) || TextUtils.isEmpty(toPath)) {
            return false
        }
        var bis: BufferedInputStream? = null
        var fos: FileOutputStream? = null
        try {
            val file = File(toPath)
            file.delete()
            file.parentFile.mkdirs()
            val inputStream = assetManager.open(fromAssetPath!!)
            bis = BufferedInputStream(inputStream)
            fos = FileOutputStream(toPath)
            val buf = ByteArray(1024)
            var read: Int
            while (bis.read(buf).also { read = it } != -1) {
                fos.write(buf, 0, read)
            }
            fos.flush()
            fos.close()
            bis.close()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                bis!!.close()
                fos!!.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return false
    }
}