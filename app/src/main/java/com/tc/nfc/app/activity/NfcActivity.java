package com.tc.nfc.app.activity;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.nfc.NfcAdapter;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.iflytek.sunflower.FlowerCollector;
import com.tc.nfc.util.Constant;

public abstract class NfcActivity extends Activity {

    private PendingIntent pendingIntent;
    private NfcAdapter nfcAdapter;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;
    public NfcActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBase();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        initContentView(savedInstanceState);
        nfcScan();

    }

    private void nfcScan() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
//        if (nfcAdapter == null) {
//            System.out.println("没NFC");
//            toast = Toast.makeText(this, "手机不支持NFC", Toast.LENGTH_SHORT);
//            toast.show();
//            return;
//        } else if (!nfcAdapter.isEnabled()) {
//
//            return;
//        }
        pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        try {
            ndef.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
        mFilters = new IntentFilter[] { ndef };
        mTechLists = new String[][] { new String[] { NfcV.class.getName() } };
        Log.d("JSON","NFC已启动");
    }

    // 初始化UI，setContentView等
    protected abstract void initContentView(Bundle savedInstanceState);

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }


    // 可能全屏或者没有ActionBar等
    private void setBase() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 例
    }

    protected void addLeftMenu(boolean enable) {
        // 如果你的项目有侧滑栏可以处理此方法
        if (enable) { // 是否能有侧滑栏

        } else {

        }
    }

    // 横竖屏切换，键盘等
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        try {
            super.onRestoreInstanceState(savedInstanceState);
        } catch (Exception e) {
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, mFilters, mTechLists);
//        nfcAdapter.enableForegroundDispatch(
//                this, pendingIntent, null, new String[][]{
//                        new String[]{NfcA.class.getName()}});
        FlowerCollector.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
        FlowerCollector.onPause(this);
    }
    // 跳转到登录页面
    public void login() {
        Intent intent = new Intent();
        intent.setClass(this, LoginActivity.class);
        startActivity(intent);
    }

}
