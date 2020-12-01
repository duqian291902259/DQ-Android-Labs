package site.duqian.soloader;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.*;
import java.lang.reflect.Method;
import java.util.Locale;

/**
 * Description:工具类
 *
 * @author Dusan, Created on 2019/3/14 - 20:26.
 * E-mail:duqian2010@gmail.com
 */
public class SoUtils {

    public static String getSoSourcePath() {
        String cpuArchType = getCpuArchType();
        if (!TextUtils.isEmpty(cpuArchType)) {
            cpuArchType = cpuArchType.toLowerCase();
        }
        final String rootSdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
        String soFrom = rootSdcard + "/libs/" + cpuArchType + "/";
        final File file = new File(soFrom);
        if (!file.exists()) {
            file.mkdirs();
        }
        Log.d("dq", "soFrom=" + soFrom);
        return soFrom;
    }

    public static boolean isX86Phone() {
        final String archType = getCpuArchType();
        return !TextUtils.isEmpty(archType) && "x86".equals(archType.toLowerCase());
    }

    public static String getCpuArchType() {
        String arch = "";
        try {
            Class<?> clazz = Class.forName("android.os.SystemProperties");
            Method get = clazz.getDeclaredMethod("get", new Class[]{String.class});
            arch = (String) get.invoke(clazz, new Object[]{"ro.product.cpu.abi"});
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(arch)) {
            arch = Build.CPU_ABI;//可能不准确？
        }
        Log.d("dq getCpuArchType", "arch " + arch);
        return arch;
    }

    public static boolean copyAssetsDirectory(Context context, String fromAssetPath, String toPath) {
        try {
            AssetManager assetManager = context.getAssets();
            String[] files = context.getAssets().list(fromAssetPath);
            if (isExist(toPath)) {
                //deleteFile(toPath);//很危险
            } else {
                new File(toPath).mkdirs();
            }
            boolean res = true;
            for (String file : files)
                if (file.contains(".")) {
                    res &= copyAssetFile(assetManager, fromAssetPath + "/" + file, toPath + "/" + file);
                } else {
                    res &= copyAssetsDirectory(context, fromAssetPath + "/" + file, toPath + "/" + file);
                }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isExist(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        boolean exist;
        try {
            File file = new File(path);
            exist = file.exists();
        } catch (Exception e) {
            exist = false;
        }
        return exist;
    }

    /**
     * 删除文件或文件夹(包括目录下的文件)
     *
     * @param filePath
     */
    public static boolean deleteFile(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }
        try {
            File f = new File(filePath);
            if (f.exists() && f.isDirectory()) {
                File[] delFiles = f.listFiles();
                if (delFiles != null && delFiles.length > 0) {
                    for (int i = 0; i < delFiles.length; i++) {
                        deleteFile(delFiles[i].getAbsolutePath());
                    }
                }
            }
            f.delete();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static boolean copyAssetFile(AssetManager assetManager, String fromAssetPath, String toPath) {
        if (assetManager == null || TextUtils.isEmpty(fromAssetPath) || TextUtils.isEmpty(toPath)) {
            return false;
        }
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        try {
            final File file = new File(toPath);
            file.delete();
            file.getParentFile().mkdirs();
            InputStream inputStream = assetManager.open(fromAssetPath);
            bis = new BufferedInputStream(inputStream);
            fos = new FileOutputStream(toPath);
            byte[] buf = new byte[1024];
            int read;
            while ((read = bis.read(buf)) != -1) {
                fos.write(buf, 0, read);
            }
            fos.flush();
            fos.close();
            bis.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                bis.close();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * /system/lib64/libart.so
     */
    private static boolean isART64(Context context) {
        final String fileName = "art";
        try {
            ClassLoader classLoader = context.getClassLoader();
            Class<?> cls = ClassLoader.class;
            Method method = cls.getDeclaredMethod("findLibrary", String.class);
            Object object = method.invoke(classLoader, fileName);
            if (object != null) {
                return ((String) object).contains("lib64");
            }
        } catch (Exception e) {
            //如果发生异常就用方法②
            return is64bitCPU();
        }

        return false;
    }

    private static boolean is64bitCPU() {
        String CPU_ABI = null;
        if (Build.VERSION.SDK_INT >= 21) {
            String[] CPU_ABIS = Build.SUPPORTED_ABIS;
            if (CPU_ABIS.length > 0) {
                CPU_ABI = CPU_ABIS[0];
            }
        } else {
            CPU_ABI = Build.CPU_ABI;
        }
        return CPU_ABI != null && CPU_ABI.contains("arm64");
    }


    /**
     * ELF文件头 e_indent[]数组文件类标识索引
     */
    private static final int EI_CLASS = 4;
    /**
     * ELF文件头 e_indent[EI_CLASS]的取值：ELFCLASS32表示32位目标
     */
    private static final int ELFCLASS32 = 1;
    /**
     * ELF文件头 e_indent[EI_CLASS]的取值：ELFCLASS64表示64位目标
     */
    private static final int ELFCLASS64 = 2;

    /**
     * The system property key of CPU arch type
     */
    private static final String CPU_ARCHITECTURE_KEY_64 = "ro.product.cpu.abilist64";

    /**
     * The system libc.so file path
     */
    private static final String SYSTEM_LIB_C_PATH = "/system/lib/libc.so";
    private static final String SYSTEM_LIB_C_PATH_64 = "/system/lib64/libc.so";
    private static final String PROC_CPU_INFO_PATH = "/proc/cpuinfo";


    private static String getSystemProperty(String key, String defaultValue) {
        String value = defaultValue;
        try {
            Class<?> clazz = Class.forName("android.os.SystemProperties");
            Method get = clazz.getMethod("get", String.class, String.class);
            value = (String) (get.invoke(clazz, key, ""));
        } catch (Exception e) {
            Log.d("dq getSystemProperty", "key = " + key + ", error = " + e.getMessage());
        }
        Log.d("dq getSystemProperty", key + " = " + value);
        return value;
    }

    /**
     * Read the first line of "/proc/cpuinfo" file, and check if it is 64 bit.
     */
    private static boolean isCPUInfo64() {
        File cpuInfo = new File(PROC_CPU_INFO_PATH);
        if (cpuInfo.exists()) {
            InputStream inputStream = null;
            BufferedReader bufferedReader = null;
            try {
                inputStream = new FileInputStream(cpuInfo);
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream), 512);
                String line = bufferedReader.readLine();
                if (line != null && line.length() > 0 && line.toLowerCase(Locale.US).contains("arch64")) {
                    return true;
                }
            } catch (Throwable t) {
            } finally {
                try {
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * Check if system libc.so is 32 bit or 64 bit
     */
    private static boolean isLibc64() {
        File libcFile = new File(SYSTEM_LIB_C_PATH);
        if (libcFile.exists()) {
            byte[] header = readELFHeadrIndentArray(libcFile);
            if (header != null && header[EI_CLASS] == ELFCLASS64) {
                Log.d("dq isLibc64()", SYSTEM_LIB_C_PATH + " is 64bit");
                return true;
            }
        }

        File libcFile64 = new File(SYSTEM_LIB_C_PATH_64);
        if (libcFile64.exists()) {
            byte[] header = readELFHeadrIndentArray(libcFile64);
            if (header != null && header[EI_CLASS] == ELFCLASS64) {
                Log.d("dq isLibc64()", SYSTEM_LIB_C_PATH_64 + " is 64bit");
                return true;
            }
        }

        return false;
    }

    /**
     * ELF文件头格式是固定的:文件开始是一个16字节的byte数组e_indent[16]
     * e_indent[4]的值可以判断ELF是32位还是64位
     */
    private static byte[] readELFHeadrIndentArray(File libFile) {
        if (libFile != null && libFile.exists()) {
            FileInputStream inputStream = null;
            try {
                inputStream = new FileInputStream(libFile);
                byte[] tempBuffer = new byte[16];
                int count = inputStream.read(tempBuffer, 0, 16);
                if (count == 16) {
                    return tempBuffer;
                }
            } catch (Throwable t) {
                Log.e("readELFHeadrIndentArray", "Error:" + t.toString());
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

}
