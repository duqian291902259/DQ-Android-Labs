package site.duqian.soloader;

import android.content.Context;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Description:读写和加载指定路径下so
 *
 * @author Dusan, Created on 2019/3/15 - 18:19.
 * E-mail:duqian2010@gmail.com
 */
public class SoFileLoadManager {

    /**
     * 加载 so 文件(直接指定你so下载的路径即可)
     *
     * @param fromPath 下载的so，存放到sdcard的目录，拷贝私有目录，非必须，但是建议直接下载到私有目录路，这样做，不需要读写sdcard，
     */
    public static void loadSoFile(Context context, String fromPath) {
        try {
            //File dir = new File(fromPath);
            File dir = context.getDir("libs", Context.MODE_PRIVATE);
            if (!isLoadSoFile(dir)) {
                copy(fromPath, dir.getAbsolutePath());
            }
            LoadLibraryUtil.installNativeLibraryPath(context.getApplicationContext().getClassLoader(), dir);
        } catch (Throwable throwable) {
            Log.e("dq", "loadSoFile error " + throwable.getMessage());
        }
    }

    /**
     * 判断 so 文件是否存在
     */
    public static boolean isLoadSoFile(File dir) {
        File[] currentFiles;
        currentFiles = dir.listFiles();
        boolean hasSoLib = false;
        if (currentFiles == null) {
            return false;
        }
        for (int i = 0; i < currentFiles.length; i++) {
            // TODO: 2019/3/15 补充加校验so的逻辑。
            if (currentFiles[i].getName().toLowerCase().contains("duqian")) {
                hasSoLib = true;
            }
        }
        return hasSoLib;
    }

    /**
     * @param fromFile 指定的下载目录
     * @param toFile   应用的包路径
     * @return
     */
    public static int copy(String fromFile, String toFile) {
        File root = new File(fromFile);
        if (!root.exists()) {
            return -1;
        }
        //如果存在,则获取当前目录下的全部文件
        File[] currentFiles = root.listFiles();
        File targetDir = new File(toFile);
        //创建目录
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        if (currentFiles != null && currentFiles.length > 0) {
            //遍历要复制该目录下的全部文件
            for (File currentFile : currentFiles) {
                if (currentFile.isDirectory()) {
                    //如果当前项为子目录 进行递归
                    copy(currentFile.getPath() + "/", toFile + currentFile.getName() + "/");
                } else {
                    //如果当前项为文件则进行文件拷贝
                    if (currentFile.getName().endsWith(".so")) {
                        int id = copySdcardFile(currentFile.getPath(), toFile + File.separator + currentFile.getName());
                    }
                }
            }
        }
        return 0;
    }


    /**
     * 文件拷贝,要复制的目录下的所有非子目录(文件夹)文件拷贝
     *
     * @param fromFile 源文件路径
     * @param toFile   目标文件路径
     * @return
     */
    public static int copySdcardFile(String fromFile, String toFile) {
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
            return 0;
        } catch (Exception ex) {
            return -1;
        }
    }
}
