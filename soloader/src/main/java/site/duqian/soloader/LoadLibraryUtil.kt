package site.duqian.soloader

import android.annotation.TargetApi
import android.os.Build
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.IOException
import java.lang.reflect.Field
import java.util.*

/**
 * Description:动态加载so文件的核心，注入so路径到nativeLibraryDirectories数组第一个位置，会优先从这个位置查找so
 * 更多姿势，请参考开源库动态更新so的黑科技，仅供学习交流
 *
 * @author Dusan, Created on 2019/3/15 - 18:17.
 * E-mail:duqian2010@gmail.com
 */
object LoadLibraryUtil {
    private val TAG = LoadLibraryUtil::class.java.simpleName + "-duqian"
    private var lastSoDir: File? = null

    /**
     * 清除so路径，实际上是设置一个无效的path，用户测试无so库的情况
     *
     * @param classLoader
     */
    @JvmStatic
    fun clearSoPath(classLoader: ClassLoader?) {
        try {
            val testDirNoSo = Environment.getExternalStorageDirectory().absolutePath + "/duqian/"
            File(testDirNoSo).mkdirs()
            installNativeLibraryPath(classLoader, testDirNoSo)
        } catch (throwable: Throwable) {
            Log.e(TAG, "dq clear path error$throwable")
            throwable.printStackTrace()
        }
    }

    @Synchronized
    @Throws(Throwable::class)
    fun installNativeLibraryPath(classLoader: ClassLoader?, folderPath: String?): Boolean {
        return installNativeLibraryPath(classLoader, File(folderPath))
    }

    @Synchronized
    @Throws(Throwable::class)
    fun installNativeLibraryPath(classLoader: ClassLoader?, folder: File?): Boolean {
        if (classLoader == null || folder == null || !folder.exists()) {
            Log.e(TAG, "classLoader or folder is illegal $folder")
            return false
        }
        val sdkInt = Build.VERSION.SDK_INT
        val aboveM = sdkInt == 25 && previousSdkInt != 0 || sdkInt > 25
        if (aboveM) {
            V25.install(classLoader, folder)
        } else if (sdkInt >= 23) {
            V23.install(classLoader, folder)
        } else if (sdkInt >= 14) {
            V14.install(classLoader, folder)
        }
        lastSoDir = folder
        return true
    }

    /**
     * fuck部分机型删了该成员属性，兼容
     *
     * @return 被厂家删了返回1，否则正常读取
     */
    @get:TargetApi(Build.VERSION_CODES.M)
    private val previousSdkInt: Int
        private get() {
            try {
                return Build.VERSION.PREVIEW_SDK_INT
            } catch (ignore: Throwable) {
            }
            return 1
        }

    private object V23 {
        @Throws(Throwable::class)
        fun install(classLoader: ClassLoader, folder: File) {
            val pathListField = ReflectUtil.findField(classLoader, "pathList")
            val dexPathList = pathListField[classLoader]
            val nativeLibraryDirectories: Field? =
                ReflectUtil.findField(dexPathList, "nativeLibraryDirectories")
            var libDirs = nativeLibraryDirectories?.get(dexPathList) as MutableList<File>?

            //去重
            if (libDirs == null) {
                libDirs = ArrayList(2)
            }
            val libDirIt = libDirs.iterator()
            while (libDirIt.hasNext()) {
                val libDir = libDirIt.next()
                if (folder == libDir || folder == lastSoDir) {
                    libDirIt.remove()
                    Log.d(TAG, "dq libDirIt.remove() " + folder.absolutePath)
                    break
                }
            }
            libDirs.add(0, folder)
            val systemNativeLibraryDirectories: Field? =
                ReflectUtil.findField(dexPathList, "systemNativeLibraryDirectories")
            var systemLibDirs = systemNativeLibraryDirectories?.get(dexPathList) as List<File>?

            //判空
            if (systemLibDirs == null) {
                systemLibDirs = ArrayList(2)
            }
            Log.d(TAG, "dq systemLibDirs,size=" + systemLibDirs.size)
            val makePathElements = ReflectUtil.findMethod(
                dexPathList,
                "makePathElements",
                MutableList::class.java,
                File::class.java,
                MutableList::class.java
            )
            val suppressedExceptions = ArrayList<IOException>()
            libDirs.addAll(systemLibDirs)
            val elements = makePathElements.invoke(
                dexPathList,
                libDirs,
                null,
                suppressedExceptions
            ) as Array<Any>?
            val nativeLibraryPathElements =
                ReflectUtil.findField(dexPathList, "nativeLibraryPathElements")
            nativeLibraryPathElements.isAccessible = true
            nativeLibraryPathElements[dexPathList] = elements
        }
    }

    /**
     * 把自定义的native库path插入nativeLibraryDirectories最前面，即使安装包libs目录里面有同名的so，也优先加载指定路径的外部so
     */
    private object V25 {
        @Throws(Throwable::class)
        fun install(classLoader: ClassLoader, folder: File) {
            val pathListField = ReflectUtil.findField(classLoader, "pathList")
            val dexPathList = pathListField[classLoader]
            val nativeLibraryDirectories: Field? =
                ReflectUtil.findField(dexPathList, "nativeLibraryDirectories")
            var libDirs = nativeLibraryDirectories?.get(dexPathList) as MutableList<File>?
            //去重
            if (libDirs == null) {
                libDirs = ArrayList(2)
            }
            val libDirIt = libDirs.iterator()
            while (libDirIt.hasNext()) {
                val libDir = libDirIt.next()
                if (folder == libDir || folder == lastSoDir) {
                    libDirIt.remove()
                    Log.d(TAG, "dq libDirIt.remove()" + folder.absolutePath)
                    break
                }
            }
            libDirs.add(0, folder)
            //system/lib
            val systemNativeLibraryDirectories: Field? =
                ReflectUtil.findField(dexPathList, "systemNativeLibraryDirectories")
            var systemLibDirs = systemNativeLibraryDirectories?.get(dexPathList) as List<File>?

            //判空
            if (systemLibDirs == null) {
                systemLibDirs = ArrayList(2)
            }
            Log.d(TAG, "dq systemLibDirs,size=" + systemLibDirs.size)
            val makePathElements =
                ReflectUtil.findMethod(dexPathList, "makePathElements", MutableList::class.java)
            libDirs.addAll(systemLibDirs)
            val elements = makePathElements.invoke(dexPathList, libDirs) as Array<Any>?
            val nativeLibraryPathElements =
                ReflectUtil.findField(dexPathList, "nativeLibraryPathElements")
            nativeLibraryPathElements.isAccessible = true
            nativeLibraryPathElements[dexPathList] = elements
        }
    }

    private object V14 {
        @Throws(Throwable::class)
        fun install(classLoader: ClassLoader, folder: File) {
            val pathListField = ReflectUtil.findField(classLoader, "pathList")
            val dexPathList = pathListField[classLoader]
            ReflectUtil.expandFieldArray(dexPathList, "nativeLibraryDirectories", arrayOf(folder))
        }
    }
}