package site.duqian.soloader

import android.content.Context
import android.util.Log
import site.duqian.soloader.SoFileLoadManager
import site.duqian.soloader.LoadLibraryUtil
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.Exception
import java.util.*

/**
 * Description:读写和加载指定路径下so
 *
 * @author Dusan, Created on 2019/3/15 - 18:19.
 * E-mail:duqian2010@gmail.com
 */
object SoFileLoadManager {
    /**
     * 加载 so 文件(直接指定你so下载的路径即可)
     *
     * @param fromPath 下载的so，存放到sdcard的目录，拷贝私有目录，非必须，但是建议直接下载到私有目录路，这样做，不需要读写sdcard，
     */
    @JvmStatic
    fun loadSoFile(context: Context, fromPath: String) {
        try {
            //File dir = new File(fromPath);
            val dir = context.getDir("libs", Context.MODE_PRIVATE)
            if (!isLoadSoFile(dir)) {
                //拷贝非必须，只做演示，注入的路径可以是sdcard的，也可以是app私有目录存储，建议app的files目录，安全
                copy(fromPath, dir.absolutePath)
            }
            LoadLibraryUtil.installNativeLibraryPath(context.applicationContext.classLoader, dir)
        } catch (throwable: Throwable) {
            Log.e("dq-so", "loadSoFile error " + throwable.message)
        }
    }

    /**
     * 判断 so 文件是否存在
     */
    private fun isLoadSoFile(dir: File): Boolean {
        val currentFiles: Array<File>? = dir.listFiles()
        var hasSoLib = false
        if (currentFiles == null) {
            return false
        }
        for (currentFile in currentFiles) {
            // TODO: 2019/3/15 补充加校验so的逻辑。
            if (currentFile.name.toLowerCase(Locale.getDefault()).contains("duqian")) {
                hasSoLib = true
            }
        }
        return hasSoLib
    }

    /**
     * @param fromFile 指定的下载目录
     * @param toFile   应用的包路径
     * @return
     */
    private fun copy(fromFile: String, toFile: String): Int {
        val root = File(fromFile)
        if (!root.exists()) {
            return -1
        }
        //如果存在,则获取当前目录下的全部文件
        val currentFiles = root.listFiles()
        val targetDir = File(toFile)
        //创建目录
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }
        if (currentFiles != null && currentFiles.isNotEmpty()) {
            //遍历要复制该目录下的全部文件
            for (currentFile in currentFiles) {
                if (currentFile.isDirectory) {
                    //如果当前项为子目录 进行递归
                    copy(currentFile.path + "/", toFile + currentFile.name + "/")
                } else {
                    //如果当前项为文件则进行文件拷贝
                    if (currentFile.name.endsWith(".so")) {
                        val id = copySdcardFile(
                            currentFile.path,
                            toFile + File.separator + currentFile.name
                        )
                    }
                }
            }
        }
        return 0
    }

    /**
     * 文件拷贝,要复制的目录下的所有非子目录(文件夹)文件拷贝
     *
     * @param fromFile 源文件路径
     * @param toFile   目标文件路径
     * @return
     */
    private fun copySdcardFile(fromFile: String?, toFile: String?): Int {
        return try {
            val fosfrom = FileInputStream(fromFile)
            val fosto = FileOutputStream(toFile)
            val baos = ByteArrayOutputStream()
            val buffer = ByteArray(1024)
            var len = -1
            while (fosfrom.read(buffer).also { len = it } != -1) {
                baos.write(buffer, 0, len)
            }
            // 从内存到写入到具体文件
            fosto.write(baos.toByteArray())
            // 关闭文件流
            baos.close()
            fosto.close()
            fosfrom.close()
            0
        } catch (ex: Exception) {
            -1
        }
    }
}