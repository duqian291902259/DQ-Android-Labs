---

# DQ-Android-Labs
Contains various magical modules with cool tech in the future.such as soloader....

New modules are being updated...

Github-->: [The best SoLoader for android](https://github.com/duqian291902259/DQ-Android-Labs)


### 1，SoLoader，android动态加载so库
#### 无需修改已有工程的so加载逻辑，支持so动态下发并安全加载的方案。

如果项目native库很多，还支持各种平台，为了减少apk size，so库动态下发，按需加载是不错的选择。比如x86库服务器下发，动态加载，瘦身效果将非常可观。但是采取常规load方式，改动有点大，底层jar包，第三库不好改加载路径吖。so follow me：

在应用启动的时,一次注入本地so路径path，待程序使用过程中so准备后安全加载。so库动态加载黑科技，安全可靠，线上验证，强烈推荐！注入路径后，加载so的姿势不变：
   
1，System.loadLibrary(soName); 无需改变系统load方法，注入路径后照常加载，推荐。

2，使用第三方库ReLinker，有so加载成功、失败的回调，安全加载不崩溃。
     
3，System.load(soAbsolutePath);传统方法指定so路径加载，不适合大项目和第三方lib，so下发加载不够灵活，不推荐。


<!-- more -->

### Quik Start

下载demo，使用Android Studio打开soloader工程。
把自定义的native库path插入nativeLibraryDirectories最前面，即使安装包libs目录里面有同名的so，也优先加载指定路径的外部so。可参考插件化、热更新开源库了解其思想，部分代码。

``` Java

private static void install(ClassLoader classLoader, File folder) throws Throwable {
            Field pathListField = ReflectUtil.findField(classLoader, "pathList");
            Object dexPathList = pathListField.get(classLoader);
            Field nativeLibraryDirectories = ReflectUtil.findField(dexPathList, "nativeLibraryDirectories");

            List<File> libDirs = (List<File>) nativeLibraryDirectories.get(dexPathList);
            //去重
            if (libDirs == null) {
                libDirs = new ArrayList<>(2);
            }
            final Iterator<File> libDirIt = libDirs.iterator();
            while (libDirIt.hasNext()) {
                final File libDir = libDirIt.next();
                if (folder.equals(libDir) || folder.equals(lastSoDir)) {
                    libDirIt.remove();
                    Log.d(TAG, "dq libDirIt.remove()" + folder.getAbsolutePath());
                    break;
                }
            }

            libDirs.add(0, folder);
            //system/lib
            Field systemNativeLibraryDirectories = ReflectUtil.findField(dexPathList, "systemNativeLibraryDirectories");
            List<File> systemLibDirs = (List<File>) systemNativeLibraryDirectories.get(dexPathList);

            //判空
            if (systemLibDirs == null) {
                systemLibDirs = new ArrayList<>(2);
            }
            Log.d(TAG, "dq systemLibDirs,size=" + systemLibDirs.size());

            Method makePathElements = ReflectUtil.findMethod(dexPathList, "makePathElements", List.class);
            libDirs.addAll(systemLibDirs);

            Object[] elements = (Object[]) makePathElements.invoke(dexPathList, libDirs);
            Field nativeLibraryPathElements = ReflectUtil.findField(dexPathList, "nativeLibraryPathElements");
            nativeLibraryPathElements.setAccessible(true);
            nativeLibraryPathElements.set(dexPathList, elements);
        }
```

### Thanks
Welcome to contact me: [duqian2010@gmail.com](http://www.duqian.site) or Wechat:dusan2010

<!-- more #pic_center=540x960-->

![soloader-screenshot](https://img-blog.csdnimg.cn/20190509161433295.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2R6c3cwMTE3,size_16,color_FFFFFF,t_70)
