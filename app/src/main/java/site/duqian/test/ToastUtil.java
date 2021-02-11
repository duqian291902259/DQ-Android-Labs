package site.duqian.test;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

/**
 * ToastUtil:线程安全的Toast
 *
 * @author duqian, Created on 2017/5/20 - 11:51.
 * E-mail:duqian2010@gmail.com
 */
public class ToastUtil {
    private static boolean isLong = false;

    public static void toastLong(final Context context, final String... args) {
        isLong = true;
        toast(context, args);
    }

    public static void toastShort(final Context context, final String... args) {
        isLong = false;
        toast(context, args);
    }

    public static void toast(final Context context, final String... args) {
        if (context == null || args == null) {
            return;
        }
        if (isMainThread()) {
            makeText(context, args);
            return;
        }
        //子线程looper
        Looper.prepare();
        makeText(context, args);
        Looper.loop();
    }

    private static void makeText(Context context, String... args) {
        StringBuilder sb = new StringBuilder();
        String temp = "";
        for (Object obj : args) {
            if (obj != null) {
                temp = obj.toString();
            } else {
                temp = "null";
            }
            sb.append(temp);
        }
        if (isLong) {
            Toast.makeText(context, sb.toString(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, sb.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private static boolean isMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }
}
