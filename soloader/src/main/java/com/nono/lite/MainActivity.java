package com.nono.lite;

/**
 * description:第三方so库定义
 *
 * @author 杜小菜 Created on 2019-05-06 - 20:37.
 * E-mail:duqian2010@gmail.com
 */

public class MainActivity {

    /**
     * 用于测试的so，copy到sdcard后加载，native方法定义
     */
    public native String getStringFromNative();

}