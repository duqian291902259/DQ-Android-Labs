package site.duqian.soloader;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.getkeepsafe.relinker.ReLinker;
import site.duqian.so.loader.R;

import java.io.File;

/**
 * description:1，so动态加载demo，2，用于测试google官方android app bundle方案
 *
 * @author 杜小菜 Created on 2019-05-07 - 11:03.
 * E-mail:duqian2010@gmail.com
 */
public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1000;
    private final String sdcardLibDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/libs";
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.context = this;
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        isSoExist = false;
        applyForPermissions();

        //1，如果没有so就加载，肯定报错
        loadLibrary();

        findViewById(R.id.btn_load_so).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //2，copy so，测试so动态加载
                applyForPermissions();
                startLoadSoFromLocalPath();
            }
        });
        findViewById(R.id.btn_clear_so).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //3，清除自定义的so路径，杀进程退出app，再重新进入加载必定失败
                clearSoFileAndPath();
                loadLibrary();
                restartApp();
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //4，此处测试随安装包安装的so库，无需动态加载，apk安装时就有的
                String msg = showResult();
                Snackbar.make(view, "test so loader " + msg, Snackbar.LENGTH_SHORT).setAction("Action", null).show();
            }
        });
    }


    private void startLoadSoFromLocalPath() {
        //确保so本地存在
        if (isSoExist) {
            realLoadSoFile();
        } else {
            copyAssetsFile();
        }

    }

    private void realLoadSoFile() {
        String soFrom = SoUtils.getSoSourcePath();
        //注入so路径，如果清除了的话。没有清除可以不用每次注入
        SoFileLoadManager.loadSoFile(context, soFrom);
        //加载so库
        loadLibrary();
    }

    private void clearSoFileAndPath() {
        LoadLibraryUtil.clearSoPath(getClassLoader());
        final boolean delete = SoUtils.deleteFile(sdcardLibDir);
        String privateDir = context.getDir("libs", Context.MODE_PRIVATE).getAbsolutePath();
        final boolean delete2 = SoUtils.deleteFile(privateDir);
        Log.d("dq", "delete all so=" + delete + ",delete private dir=" + delete2);
        isSoExist = !delete;
    }


    private void applyForPermissions() {//申请sdcard读写权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
            }
        }
    }

    private String showResult() {
        System.loadLibrary("sostub");
        String msg = getStringFromCPP();
        Log.d("dq", "showResult=" + msg);
        ToastUtil.toastShort(context, "getStringFromC++=" + msg);
        return msg;
    }

    private boolean isSoExist = false;

    private void copyAssetsFile() {//将so拷贝到sdcard
        final String soTest = sdcardLibDir + "/x86/libnonostub.so";
        if (new File(soTest).exists()) {
            isSoExist = true;
            return;
        }
        ToastUtil.toastShort(context, "copying so from Assets ");

        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                isSoExist = SoUtils.copyAssetsDirectory(context, "libs", sdcardLibDir);
                Log.d("dq", "sdcardLibDir=" + sdcardLibDir + "，copy from assets " + isSoExist);
                if (isSoExist) {
                    realLoadSoFile();
                }
            }
        });
    }


    /**
     * 在应用启动的时动态注入本地so路径path，待后期so准备好了，可以安全加载。
     * 加载注入path的so文件，以下几种加载的方式都可以
     * 1，使用第三方库ReLinker，有so加载成功或者失败的回调，没有找到so也不会崩溃
     * 2，System.loadLibrary("nonostub"); //系统方法也能正常加载，无法try catch住异常
     * 3，System.load("sdcard getAbsolutePath"); //对应abi的so完整路径也能加载，无法try catch住异常
     * 4，使用LocalSoHelper可以拷贝so文件并load
     */
    private void loadLibrary() {
        //System.loadLibrary("nonostub");//系统方法也能正常加载，无法try catch住异常
        ReLinker.loadLibrary(this, "nonostub", new ReLinker.LoadListener() {
            @Override
            public void success() {
                String msg = new com.nono.lite.MainActivity().getStringFromNative();
                Log.d("dq", "getNativeResult=" + msg);
                ToastUtil.toastShort(context, "from noonstub.so=" + msg);
            }

            @Override
            public void failure(Throwable t) {
                Log.d("dq", "load so failed " + t.toString());
                ToastUtil.toastShort(context, "load so failed " + t.toString());
            }
        });
    }

    public native String getStringFromCPP();//本类native方法

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    ToastUtil.toast(context, "no permissions for rw sdcard");
                    return;
                }
            }
        }
    }

    /**
     * 为了重启，也是蛮拼的，所有家伙都上
     */
    private void restartApp() {
        try {
            final String packageName = getPackageName();//"site.duqian.so.loader"
            Intent k = context.getPackageManager().getLaunchIntentForPackage(packageName);
            if (k != null) {
                k.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            }
            context.startActivity(k);

            android.os.Process.killProcess(android.os.Process.myPid());
            //System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            ToastUtil.toast(context, "contact me. duqian2010@gmail.com");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
