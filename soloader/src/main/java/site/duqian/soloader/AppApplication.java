package site.duqian.soloader;

import android.app.Application;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.File;

/**
 * description:应用初始化时，注入so本地路径
 *
 * @author Dusan Created on 2019/3/15 - 18:26.
 * E-mail:duqian2010@gmail.com
 */
public class AppApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            initBugly();
            //动态加载x86的so文件，提前注入so本地路径，只需要注入一次，后续copy或者下载完so文件后再加载。demo方便测试才后续再次注入
            dynamicSo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initBugly() {
        CrashReport.initCrashReport(getApplicationContext(), "93f2b80d3c", false);
    }

    private void dynamicSo() {
        String soFrom = SoUtils.getSoSourcePath();
        if (!new File(soFrom).exists()) {
            ToastUtil.toast(this, "哈哈，本地so文件不存在，" + soFrom);
        }
        SoFileLoadManager.loadSoFile(this, soFrom);
    }
}
