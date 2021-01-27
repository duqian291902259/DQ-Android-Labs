function jstring2Str(jstring) {
    var ret;
    Java.perform(function () {
        var String = Java.use("java.lang.String");
        ret = Java.cast(jstring, String);
    });
    return ret;
}

Interceptor.attach(Module.findExportByName("libnative-lib.so", "Java_site_duqian_ndk_MainActivity_stringFromJNI"), {
    onEnter: function (args) {
        console.log("stringFromJNI onEnter...");

    },
    onLeave: function (retval) {
        console.log("stringFromJNI onLeave...");
        //step1: 获取返回值
        console.log("stringFromJNI 函数返回old值:", jstring2Str(retval));
        //step2: 修改返回值
        var env = Java.vm.getEnv();
        var jstring = env.newStringUtf("new hello");
        retval.replace(ptr(jstring));
        console.log("stringFromJNI 函数返回new值:", jstring2Str(retval));
    }
});

Interceptor.attach(Module.findExportByName("libnative-lib.so", "create_stdstr"), {
    onEnter: function (args) {
        console.log("create_stdstr onEnter...");
        //step3: 打印参数
        console.log("create_stdstr 参数一:", args[0].readUtf8String());
        console.log("create_stdstr 参数二:", args[1].toInt32());
        //step4: 修改参数
        args[1] = ptr(2);
        this.buf = Memory.alloc(1024);
        Memory.writeUtf8String(this.buf, "hello");
        args[0] = this.buf;
    },
    onLeave: function (aretval) {
        console.log("create_stdstr onLeave...");
        //step5: 获取返回值
        console.log("create_stdstr 函数返回值:", aretval.readUtf8String());
    }
});
