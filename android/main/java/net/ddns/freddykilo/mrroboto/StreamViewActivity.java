package net.ddns.freddykilo.mrroboto;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

public class StreamViewActivity extends Activity {

    Intent motionTracker;
    WebView leftEye;
    WebView rightEye;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream_view);
        leftEye = (WebView) findViewById(R.id.leftEye);
        rightEye = (WebView) findViewById(R.id.rightEye);
        setUpWebView(leftEye, "file:///android_asset/html/leftEye.html");
        setUpWebView(rightEye, "file:///android_asset/html/rightEye.html");
        startMotionTracker();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        leftEye.destroy();
        rightEye.destroy();
        stopService(motionTracker);
    }

    private void setUpWebView(WebView webView, String url) {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.loadUrl(url);
    }

    private void startMotionTracker() {
        motionTracker = new Intent(this, MotionTracker.class);
        startService(motionTracker);
    }
}
