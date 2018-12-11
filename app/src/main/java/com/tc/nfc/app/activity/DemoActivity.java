package com.tc.nfc.app.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.tc.nfc.R;
import com.tc.nfc.app.utils.ToastUtil;
import com.tc.nfc.core.listener.ActionCallbackListener;
import com.tc.nfc.util.NFCApplication;


public class DemoActivity extends Activity implements View.OnClickListener{
    private Button   btnTransfor;
    private TextView tvDisplay;
    private EditText etShow;
    private NFCApplication nfcApplication;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        init();
    }

    //初始化，点击事件
    private void init() {
        etShow = (EditText) findViewById(R.id.etTransfor);
        tvDisplay = (TextView) findViewById(R.id.tvTransfor);
        btnTransfor = (Button) findViewById(R.id.btnTransfor);
        btnTransfor.setOnClickListener(this);

        nfcApplication = (NFCApplication) getApplication();

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnTransfor:
                nfcApplication.getLoginAction().login("111111", "111111", new ActionCallbackListener<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        ToastUtil.showToastLongTime(DemoActivity.this,"succeed");
                    }

                    @Override
                    public void onFailure(String errorEvent, String message) {
                        ToastUtil.showToastLongTime(DemoActivity.this,"failed");
                    }
                });
                break;

        }
    }
}
