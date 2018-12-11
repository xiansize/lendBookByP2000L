package com.tc.nfc.app.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tc.api.VersionSetting;
import com.tc.nfc.R;
import com.tc.nfc.app.utils.ToastUtil;
import com.tc.nfc.util.Constant;

public class InterfaceActivity extends BaseActivity implements View.OnClickListener{
    private EditText etCardCenter,etInterlib,etOpac,etLog,etMachineID;//填写的地址
    private Button btnCommit ,btnCommitMachine;
    private LinearLayout llBackpress;
    private TextView tvTitle;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interface);
        Init();//初始化

    }

    @Override
    protected void initContentView(Bundle savedInstanceState) {

    }




    private void Init() {
        llBackpress = (LinearLayout) findViewById(R.id.ll_activity_title_backpress);
        llBackpress.setOnClickListener(this);

        tvTitle = (TextView) findViewById(R.id.tv_title_activity);
        tvTitle.setText("设置");

        etCardCenter = (EditText) findViewById(R.id.etCardCenter);
        etInterlib = (EditText) findViewById(R.id.etInterlib);
        etOpac = (EditText) findViewById(R.id.etOpac);
        etLog = (EditText) findViewById(R.id.etLog);

        etMachineID = (EditText) findViewById(R.id.etMachineID);
        etMachineID.setHint("当前设备号："+Constant.machineId);

        btnCommit = (Button) findViewById(R.id.btnCommit);
        btnCommit.setOnClickListener(this);

        btnCommitMachine = (Button) findViewById(R.id.btnCommitMachineID);
        btnCommitMachine.setOnClickListener(this);


        //初始化sp
        sharedPreferences = getSharedPreferences("ADDRESS_INTERFACE", Context.MODE_PRIVATE);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnCommit:
                takeDataToSp();
                changeInterfacePath();
                ToastUtil.showToastShortTime(this,"提交成功");
                break;

            case R.id.btnCommitMachineID:
                if(!etMachineID.getText().toString().equals("")){
                    Constant.machineId = etMachineID.getText().toString();
                    takeMachineIdToSP();
                    ToastUtil.showToastShortTime(this,"提交成功");

                }else{
                    ToastUtil.showToastShortTime(this,"提交不能为空");
                }

                break;

            case R.id.ll_activity_title_backpress:
                finish();
                break;
        }
    }



    /**缓存接口地址数据*/
    private void takeDataToSp(){
        //在welcome界面取出
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if(!etCardCenter.getText().toString().equals("")){
            editor.putString("cardcenter",etCardCenter.getText().toString());
        }
        if(!etInterlib.getText().toString().equals("")){
            editor.putString("interlib",etInterlib.getText().toString());
        }
        if(!etOpac.getText().toString().equals("")){
            editor.putString("opac",etOpac.getText().toString());
        }
        if(!etLog.getText().toString().equals("")){
            editor.putString("log",etLog.getText().toString());
        }

        editor.commit();
    }

    /**
     * 保存设备号到sp中
     */
    private void takeMachineIdToSP(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("machineID",etMachineID.getText().toString());
        editor.commit();
    }

    /**
     * 改写接口地址
     */
    private void changeInterfacePath(){
        if(!etCardCenter.getText().toString().equals("")){
            VersionSetting.baseUrl = "http://"+etCardCenter.getText().toString()+"/cardcenter/appInterface";
        }
        if(!etInterlib.getText().toString().equals("")){
            VersionSetting.baseUrl2 = "http://"+etInterlib.getText().toString();
        }
        if(!etOpac.getText().toString().equals("")){
            VersionSetting.baseUrl3 = "http://"+etOpac.getText().toString();
        }
        if(!etLog.getText().toString().equals("")){
            VersionSetting.PATH_LOG_MACHINE = "http://"+etLog.getText().toString()+"/ATMCenter/service/machine/log/save";
            VersionSetting.PATH_LOG_UPDATE_BOOK = "http://"+etLog.getText().toString()+"/ATMCenter/service/uploadBooksLog";
            VersionSetting.PATH_LOG_WORKSTATION = "http://"+etLog.getText().toString()+"/ATMCenter/service/workstation/save";
            VersionSetting.PATH_LOG_LOGIN = "http://"+etLog.getText().toString()+"/ATMCenter/service/cardMachine/save";

        }

    }
}
