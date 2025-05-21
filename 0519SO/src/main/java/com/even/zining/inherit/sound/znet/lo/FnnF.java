package com.even.zining.inherit.sound.znet.lo;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.Keep;

@Keep
public class FnnF extends Handler {
    public FnnF() {

    }
    @Override
    public void handleMessage(Message message) {
        int r0 = message.what;
        FnnA.Mkgj(r0);
    }
}

