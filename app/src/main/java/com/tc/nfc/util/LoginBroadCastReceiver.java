package com.tc.nfc.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.tc.api.VersionSetting;
import com.tc.nfc.core.listener.ActionCallbackListener;

/**
 * Created by xiansize on 2017/3/31.
 */
public class LoginBroadCastReceiver extends BroadcastReceiver{
    private NFCApplication nfcApplication;
    SharedPreferences sp;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("repeat")){
            //ToastUtil.showToastShortTime(context,"重复操作");
            //Log.d("BroadCastTest","重复操作");
            if(VersionSetting.IS_SHENZHEN_LIB){
                Init(context);
            }
        }
    }

    private void Init(final Context context) {
        nfcApplication = (NFCApplication)context.getApplicationContext();
        sp = context.getSharedPreferences("USER_REPEAT_INFO",Context.MODE_PRIVATE);
        if(VersionSetting.IS_ADMIN){
            nfcApplication.getLoginAction().loginAdmin(sp.getString("etUid", ""), sp.getString("etPassword", ""), new ActionCallbackListener<Void>() {
                @Override
                public void onSuccess(Void data) {
                    //ToastUtil.showToastShortTime(context,"重登成功");
                    Log.d("BroadCastTest","馆员重登成功");
                }

                @Override
                public void onFailure(String errorEvent, String message) {
                    //ToastUtil.showToastShortTime(context,"重登失败："+message);
                    Log.d("BroadCastTest","馆员重登失败:"+message);
                }
            });

        }else{
             nfcApplication.getLoginAction().login(sp.getString("etUid", ""), sp.getString("etPassword", ""), new ActionCallbackListener<Void>() {
                 @Override
                 public void onSuccess(Void data) {
                     Log.d("BroadCastTest","读者重登成功");
                 }

                 @Override
                 public void onFailure(String errorEvent, String message) {
                     Log.d("BroadCastTest","读者重登失败："+message);
                 }
             });
        }
    }
}
