package com.even.zining.inherit.sound.znet.lo;

import android.webkit.WebChromeClient;
import android.webkit.WebView;
import androidx.annotation.Keep;
import com.even.zining.inherit.sound.start.FnnStartFun;
import com.even.zining.inherit.sound.start.newfun.Logger;


@Keep
public class FnnW extends WebChromeClient {
    @Override
    public void onProgressChanged(WebView webView, int i10) {
        super.onProgressChanged(webView, i10);
        if (i10 == 100) {
            Logger.INSTANCE.showLog( "onPageStarted=url="+i10);
            FnnA.Mkgj(i10);
        }
    }
}
