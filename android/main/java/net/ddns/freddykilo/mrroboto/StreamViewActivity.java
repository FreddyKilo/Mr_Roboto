package net.ddns.freddykilo.mrroboto;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

public class StreamViewActivity extends Activity {

    WebView leftEye;
    WebView rightEye;
    Intent motionTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream_view);
        setUpWebView();
        startMotionTracker();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        leftEye.destroy();
        rightEye.destroy();
        stopService(motionTracker);
    }

    private void setUpWebView() {
        leftEye = (WebView) findViewById(R.id.leftEye);
        rightEye = (WebView) findViewById(R.id.rightEye);
        leftEye.getSettings().setJavaScriptEnabled(true);
        rightEye.getSettings().setJavaScriptEnabled(true);
        leftEye.getSettings().setAllowFileAccessFromFileURLs(true);
        rightEye.getSettings().setAllowFileAccessFromFileURLs(true);
        leftEye.loadUrl("file:///android_asset/html/leftEye.html");
        rightEye.loadUrl("file:///android_asset/html/rightEye.html");
    }

    private void startMotionTracker() {
        motionTracker = new Intent(this, MotionTracker.class);
        startService(motionTracker);
    }
}
