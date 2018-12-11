package com.tc.nfc.app.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.WindowManager;

import com.tc.api.VersionSetting;
import com.tc.nfc.R;
import com.tc.nfc.util.Constant;


public class WelcomeActivity extends BaseActivity {

    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);


        new CountDownTimer(1500,1000) {

            @Override
            public void onTick(long millisUntilFinished) {
            }
            @Override
            public void onFinish() {
                getInterfaceAddress();//取数据fromSP
                getMachineIdFromSP();//获取设备号fromSP
                Intent intent = new Intent();
                intent.setClass(WelcomeActivity.this, LoginActivity.class);
                startActivity(intent);

                int VERSION=Integer.parseInt(android.os.Build.VERSION.SDK);
                if(VERSION >= 5){
                    WelcomeActivity.this.overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
                }
                finish();
            }
        }.start();
    }

    @Override
    protected void initContentView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_welcome);
    }

    /**将sp中数据取出*/
    private void getInterfaceAddress(){
        sharedPreferences = getSharedPreferences("ADDRESS_INTERFACE", Context.MODE_PRIVATE);
        String cardcenter = sharedPreferences.getString("cardcenter","");
        String interlib = sharedPreferences.getString("interlib","");
        String opac = sharedPreferences.getString("opac","");
        String log = sharedPreferences.getString("log","");

        if(!cardcenter.equals("") ){
            VersionSetting.baseUrl = "http://"+cardcenter+"/cardcenter/appInterface";
        }

        if(!interlib.equals("")){
            VersionSetting.baseUrl2 = "http://"+interlib;
        }

        if(!opac.equals("")){
            VersionSetting.baseUrl3 = "http://"+opac;
        }


        if(!log.equals("")){
            //将得到的log的IP和端口逐个填写到地址中
            VersionSetting.PATH_LOG_MACHINE = "http://"+log+"/ATMCenter/service/machine/log/save";
            VersionSetting.PATH_LOG_UPDATE_BOOK = "http://"+log+"/ATMCenter/service/uploadBooksLog";
            VersionSetting.PATH_LOG_WORKSTATION = "http://"+log+"/ATMCenter/service/workstation/save";
            VersionSetting.PATH_LOG_LOGIN = "http://"+log+"/ATMCenter/service/cardMachine/save";

        }

    }


    /**
     * 从sp中获取到machineid号
     */
    private void getMachineIdFromSP(){
        sharedPreferences = getSharedPreferences("ADDRESS_INTERFACE", Context.MODE_PRIVATE);
        String machineID = sharedPreferences.getString("machineID","");

        if(!machineID.equals("")){
            Constant.machineId = machineID;
        }

    }
}