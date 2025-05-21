package com.even.zining.inherit.sound.znet.lo;

import androidx.annotation.Keep;

/**
 * 
 * Description:
 **/
@Keep
public class FnnA {

    static {
        try {
            System.loadLibrary("nwfnn");
        } catch (Exception e) {

        }
    }
	//////注意:透明页面的onDestroy方法加上: (this.getWindow().getDecorView() as ViewGroup).removeAllViews()
	//////  override fun onDestroy() {
    //////    (this.getWindow().getDecorView() as ViewGroup).removeAllViews()
    //////    super.onDestroy()
    //////}
    @Keep
    public static native void IntIn(Object context);//1.传应用context.(在主进程里面初始化一次)
    @Keep
    public static native void aKig(Object context);//1.传透明Activity对象(在透明页面onCreate调用).
    @Keep
    public static native void Mkgj(int idex);

}
