package com.tc.nfc.app.activity;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.tc.api.VersionSetting;
import com.tc.nfc.R;
import com.tc.nfc.app.utils.ToastUtil;
import com.tc.nfc.util.Constant;

public class SettingActivity extends BaseActivity implements View.OnClickListener{

    private LinearLayout llLoginout, llBackPress, llOboutBorrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        llOboutBorrow = (LinearLayout) findViewById(R.id.llOboutBorrow);
        llLoginout = (LinearLayout)findViewById(R.id.loginout);
        llBackPress =(LinearLayout)findViewById(R.id.ll_activity_title_backpress);
        llOboutBorrow.setOnClickListener(this);
        llBackPress.setOnClickListener(this);
        llLoginout.setOnClickListener(this);

    }

    @Override
    protected void initContentView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_setting);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.loginout:
                //提交退出登陆日志
                Constant.JSESSIONID = null;
                Constant.readerId=null;
                Constant.readerName=null;
                finish();
                login();
                break;
            case R.id.ll_activity_title_backpress:
                finish();
                overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
                break;
            case R.id.llOboutBorrow:
                ToastUtil.showToastShortTime(this,"借书详情可询问工作人员");
        }
    }
}
