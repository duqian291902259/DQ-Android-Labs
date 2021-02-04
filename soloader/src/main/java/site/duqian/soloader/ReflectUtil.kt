package site.duqian.soloader

import android.annotation.SuppressLint
import android.content.Context
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * Description:反射工具类,注入so路径
 *
 * @author Dusan, Created on 2019/3/15 - 18:19.
 * E-mail:duqian2010@gmail.com
 */
object ReflectUtil {
    @Throws(NoSuchFieldException::class)
    fun findField(instance: Any, name: String): Field {
        var clazz: Class<*>? = instance.javaClass
        while (clazz != null) {
            try {
                val field = clazz.getDeclaredField(name)
                if (!field.isAccessible) {
                    field.isAccessible = true
                }
                return field
            } catch (e: NoSuchFieldException) {
            }
            clazz = clazz.superclass
        }
        throw NoSuchFieldException("Field " + name + " not found in " + instance.javaClass)
    }

    @Throws(NoSuchFieldException::class)
    fun findField(originClazz: Class<*>, name: String): Field {
        var clazz: Class<*>? = originClazz
        while (clazz != null) {
            try {
                val field = clazz.getDeclaredField(name)
                if (!field.isAccessible) {
                    field.isAccessible = true
                }
                return field
            } catch (e: NoSuchFieldException) {
            }
            clazz = clazz.superclass
        }
        throw NoSuchFieldException("Field $name not found in $originClazz")
    }

    @Throws(NoSuchMethodException::class)
    fun findMethod(instance: Any, name: String, vararg parameterTypes: Class<*>?): Method {
        var clazz: Class<*>? = instance.javaClass
        while (clazz != null) {
            try {
                val method = clazz.getDeclaredMethod(name, *parameterTypes)
                if (!method.isAccessible) {
                    method.isAccessible = true
                }
                return method
            } catch (e: NoSuchMethodException) {
            }
            clazz = clazz.superclass
        }
        throw NoSuchMethodException(
            "Method "
                    + name
                    + " with parameters "
                    + listOf(*parameterTypes)
                    + " not found in " + instance.javaClass
        )
    }

    /**
     * 数组替换
     */
    @Throws(
        NoSuchFieldException::class,
        IllegalArgumentException::class,
        IllegalAccessException::class
    )
    fun expandFieldArray(instance: Any, fieldName: String, extraElements: Array<Any?>) {
        val jlrField = findField(instance, fieldName)
        val original = jlrField[instance] as Array<*>
        val combined = java.lang.reflect.Array.newInstance(
            original.javaClass.componentType,
            original.size + extraElements.size
        ) as Array<*>

        // NOTE: changed to copy extraElements first, for patch load first
        System.arraycopy(extraElements, 0, combined, 0, extraElements.size)
        System.arraycopy(original, 0, combined, extraElements.size, original.size)
        jlrField[instance] = combined
    }

    @Throws(
        NoSuchFieldException::class,
        IllegalArgumentException::class,
        IllegalAccessException::class
    )
    fun reduceFieldArray(instance: Any, fieldName: String, reduceSize: Int) {
        if (reduceSize <= 0) {
            return
        }
        val jlrField = findField(instance, fieldName)
        val original = jlrField[instance] as Array<*>
        val finalLength = original.size - reduceSize
        if (finalLength <= 0) {
            return
        }
        val combined = java.lang.reflect.Array.newInstance(
            original.javaClass.componentType,
            finalLength
        ) as Array<*>
        System.arraycopy(original, reduceSize, combined, 0, finalLength)
        jlrField[instance] = combined
    }

    @Throws(NoSuchMethodException::class)
    fun findConstructor(instance: Any, vararg parameterTypes: Class<*>?): Constructor<*> {
        var clazz: Class<*>? = instance.javaClass
        while (clazz != null) {
            try {
                val constructor = clazz.getDeclaredConstructor(*parameterTypes)
                if (!constructor.isAccessible) {
                    constructor.isAccessible = true
                }
                return constructor
            } catch (e: NoSuchMethodException) {
            }
            clazz = clazz.superclass
        }
        throw NoSuchMethodException(
            "Constructor"
                    + " with parameters "
                    + listOf(*parameterTypes)
                    + " not found in " + instance.javaClass
        )
    }

    @SuppressLint("PrivateApi")
    fun getActivityThread(context: Context?, activityThread: Class<*>?): Any? {
        var activityThread = activityThread
        return try {
            if (activityThread == null) {
                activityThread = Class.forName("android.app.ActivityThread")
            }
            val m = activityThread?.getMethod("currentActivityThread")
            m?.isAccessible = true
            var currentActivityThread = m?.invoke(null)
            if (currentActivityThread == null && context != null) {
                val mLoadedApk = context.javaClass.getField("mLoadedApk")
                mLoadedApk.isAccessible = true
                val apk = mLoadedApk[context]
                val mActivityThreadField = apk.javaClass.getDeclaredField("mActivityThread")
                mActivityThreadField.isAccessible = true
                currentActivityThread = mActivityThreadField[apk]
            }
            currentActivityThread
        } catch (ignore: Throwable) {
            null
        }
    }

    fun getValueOfStaticIntField(clazz: Class<*>, fieldName: String, defVal: Int): Int {
        return try {
            val field = findField(clazz, fieldName)
            field.getInt(null)
        } catch (thr: Throwable) {
            defVal
        }
    }
}