
#HOOK：用一个新的函数替代掉原来的函数，为所欲为。

1，Python环境,官网下载，安装，添加path到系统环境变量，直接打开命令行，执行：
pip install frida-tools

2，安装frida工具后，需要root的android机，或者用x86模拟器。需要在手机上以Root权限运行一个叫frida-server
下载对应cpu架构的frida-server，https://github.com/frida/frida/releases，比如模拟器下载frida-server-14.2.8-android-x86.xz

3，启动frida-server
下载后解压出来一个没后缀的文件，名字改成frida-server，然后在这个文件所在的目录下打开命令行（Windows下为Shift+鼠标右键，选择“在此处打开CMD/PowerShell”），
执行以下命令启动client端的frida-server服务：

adb root
adb push frida-server /data/local/tmp/ 
adb shell "chmod 755 /data/local/tmp/frida-server"
adb shell "/data/local/tmp/frida-server &"


3,测试frida测试是否有效：frida-ps

4，执行hook js，注意安装apk后启动应用，看下应用的进程。此处为site.duqian.dq_ndk
frida -U site.duqian.dq_ndk -l Hook.js --no-pause

5,执行成功：
 Frida 14.2.8 - A world-class dynamic instrumentation toolkit
   | (_| |
    > _  |   Commands:
   /_/ |_|       help      -> Displays the help system
   . . . .       object?   -> Display information about 'object'
   . . . .       exit/quit -> Exit
   . . . .
   . . . .   More info at https://www.frida.re/docs/home/

[Android Emulator 5554::site.duqian.dq_ndk]-> stringFromJNI onEnter...
create_stdstr onEnter...
create_stdstr 参数一: duqian
create_stdstr 参数二: 4
create_stdstr onLeave...
stringFromJNI onLeave...
