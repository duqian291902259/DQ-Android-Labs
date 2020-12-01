package site.duqian.soloader;

import android.content.Context;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;

/**
 * Description:使用System.load，动态加载指定路径的so文件，对大型项目来说不适用，建议采用路径注入的方式完美适配已有工程的so动态下发
 *
 * @author Dusan, Created on 2019/3/15 - 16:29.
 * E-mail:duqian2010@gmail.com
 */
public class LocalSoHelper {
    private static final String TARGET_LIBS_NAME = "test_libs";

    private static volatile LocalSoHelper instance;

    private WeakReference<Context> weakReference;

    private LocalSoHelper(Context context) {
        weakReference = new WeakReference<>(context);
    }

    public static LocalSoHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (LocalSoHelper.class) {
                if (instance == null) {
                    instance = new LocalSoHelper(context);
                }
            }
        }
        return instance;
    }

    /**
     * 加载so文件
     */
    public void loadSo(LoadListener loadListener) {
        File dir = getTargetDir();
        File[] currentFiles;
        currentFiles = dir.listFiles();
        if (currentFiles == null || currentFiles.length == 0) {
            if (loadListener != null) {
                loadListener.failed();
            }
        }
        for (int i = 0; i < currentFiles.length; i++) {
            System.load(currentFiles[i].getAbsolutePath());
        }
        if (loadListener != null) {
            loadListener.finish();
        }
    }

    /**
     * @param fromFile 指定的本地目录
     * @param isCover  true覆盖原文件即删除原有文件后拷贝新的文件进来
     * @return
     */
    public void copySo(String fromFile, boolean isCover, CopyListener copyListener) {
        //要复制的文件目录
        File[] currentFiles;
        File root = new File(fromFile);
        //如同判断SD卡是否存在或者文件是否存在,如果不存在则 return出去
        if (!root.exists()) {
            if (copyListener != null) {
                copyListener.failed();
            }
            return;
        }
        //如果存在则获取当前目录下的全部文件并且填充数组
        currentFiles = root.listFiles();

        //目标目录
        File targetDir = getTargetDir();
        //创建目录
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        } else {
            //删除全部老文件
            if (isCover) {
                for (File file : targetDir.listFiles()) {
                    file.delete();
                }
            }

        }
        boolean isCopy = false;
        //遍历要复制该目录下的全部文件
        for (int i = 0; i < currentFiles.length; i++) {
            final File currentFile = currentFiles[i];
            final String currentFileName = currentFile.getName();
            if (currentFileName.contains(".so")) {
                boolean copy = copySdcardFile(currentFile.getPath(), targetDir.toString() + File.separator + currentFileName);
                if (copy) {
                    isCopy = copy;
                }
            }
        }
        if (copyListener != null && isCopy) {
            copyListener.finish();
        }
    }

    private File getTargetDir() {
        return weakReference.get().getDir(TARGET_LIBS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * 文件拷贝(要复制的目录下的所有非文件夹的文件拷贝)
     *
     * @param fromFile
     * @param toFile
     * @return
     */
    private static boolean copySdcardFile(String fromFile, String toFile) {
        try {
            FileInputStream fosfrom = new FileInputStream(fromFile);
            FileOutputStream fosto = new FileOutputStream(toFile);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = -1;
            while ((len = fosfrom.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            // 从内存到写入到具体文件
            fosto.write(baos.toByteArray());
            // 关闭文件流
            baos.close();
            fosto.close();
            fosfrom.close();
            return true;
        } catch (Exception e) {
            Log.d("dq-so", "copySdcardFile error " + e);
        }
        return false;
    }

    /**
     * copy完成后回调接口
     */
    public interface CopyListener {
        //其实方法返回boolean也成
        void finish();

        void failed();
    }

    /**
     * load完成后回调接口
     */
    public interface LoadListener {
        //其实方法返回boolean也成
        void finish();

        void failed();
    }

}
