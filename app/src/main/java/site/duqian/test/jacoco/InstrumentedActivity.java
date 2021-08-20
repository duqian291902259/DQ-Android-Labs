package site.duqian.test.jacoco;

import android.util.Log;

import site.duqian.test.MainActivity;

/**
 * Description:代理activity
 * @author n20241 Created by 杜小菜 on 2021/8/20 - 11:53 .
 * E-mail: duqian2010@gmail.com
 */
public class InstrumentedActivity extends MainActivity {
    public static String TAG = "IntrumentedActivity";
    private FinishListener mListener;

    public void setFinishListener(FinishListener listener) {
        mListener = listener;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG + ".InstrumentedActivity", "onDestroy()");
        super.finish();
        if (mListener != null) {
            mListener.onActivityFinished();
        }
    }
}
