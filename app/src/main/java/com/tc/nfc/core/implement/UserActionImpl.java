package com.tc.nfc.core.implement;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;


import com.tc.api.VersionSetting;
import com.tc.nfc.api.implement.UserApiImpl;
import com.tc.nfc.api.interfaces.UserApi;
import com.tc.nfc.core.interfaces.UserAction;
import com.tc.nfc.core.listener.ActionCallbackListener;
import com.tc.nfc.util.Constant;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by tangjiarao on 16/6/23.
 */
public class UserActionImpl extends BaseActionImpl implements UserAction {

    private Context context;
    private UserApi mUserApi;

    public UserActionImpl(Context context) {
        super(context);
        this.context = context;
        this.mUserApi = new UserApiImpl();
    }


    @Override
    public void getUserInfo(final ActionCallbackListener<JSONObject> listener) {

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {

                return mUserApi.getUserInfo(context);
            }

            @Override
            protected void onPostExecute(String result) {
                if (listener != null && result != null) {

                    try {

                        Log.d("test","获取读者信息"+result);
                        JSONObject jsonObject =new JSONObject(result);

                        //南山图书馆的解析json方法
                        if(VersionSetting.IS_SHENZHEN_LIB) {

                            jsonObject = jsonObject.getJSONObject("map");
                            listener.onSuccess(jsonObject);


                        }else if(VersionSetting.IS_CONNECT_INTERLIB3){

                            if (UserActionImpl.super.isNoToken(jsonObject)) {
                                listener.onFailure("登录", "账号验证过期，请重新登录..");
                                return;
                            }

                            Constant.readerName = jsonObject.getString("rdname");
                            Constant.readMoney = jsonObject.getString("rdsex");
                            Constant.rdstartdate = jsonObject.getString("rdstartdate");
                            Constant.rdenddate = jsonObject.getString("rdenddate");
                            Constant.rdlibname = jsonObject.getString("rdlibname");
                            listener.onSuccess(jsonObject);


                        }else{

                            if (!UserActionImpl.super.isLogin(jsonObject)) {
                                listener.onFailure("登录", "账号验证过期，请重新登录..");
                                return;
                            }

                            jsonObject = jsonObject.getJSONObject("reader");
                            listener.onSuccess(jsonObject);
                        }


                    } catch (JSONException e) {

                        listener.onFailure("ERROR", "连接服务器失败");
                    }
                }
                else{
                    Log.d("JSON","ERR2");
                    listener.onFailure("ERROR", "连接服务器失败");
                }
            }
        }.execute();
    }

    @Override
    public void getReaderInfo(final String readerId,final ActionCallbackListener<JSONObject> listener) {

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {

                return mUserApi.getReaderInfo(readerId);
            }

            @Override
            protected void onPostExecute(String result) {
                if (listener != null && result != null) {
                    Log.d("test","getReaderInfo"+result);

                    JSONObject jsonObject=null;
                    try {

                        jsonObject =new JSONObject(result);

                        if(VersionSetting.IS_SHENZHEN_LIB) {

                            jsonObject = jsonObject.getJSONObject("map");
                            listener.onSuccess(jsonObject);

                        }else if(VersionSetting.IS_CONNECT_INTERLIB3){

                            if (UserActionImpl.super.isNoToken(jsonObject)) {
                                listener.onFailure("ERROR", "账号验证过期，请重新登录..");
                                return;
                            }
                            if(jsonObject.getString("success").equals("true")){

                                listener.onSuccess(jsonObject);

                            }else{

                                listener.onFailure("验证失败", "验证失败");

                            }


                        }else{

                            if (!UserActionImpl.super.isLogin(jsonObject)) {
                                listener.onFailure("ERROR", "账号验证过期，请重新登录..");
                                return;
                            }
                            jsonObject = jsonObject.getJSONObject("reader");
                            listener.onSuccess(jsonObject);

                        }

                    } catch (JSONException e) {

                        if(VersionSetting.IS_SHENZHEN_LIB) {
                            listener.onFailure("验证失败", "验证失败");

                        }else if(VersionSetting.IS_CONNECT_INTERLIB3){
                            listener.onFailure("验证失败", "验证失败");

                        }else{
                            try {
                                if (jsonObject.getString("message").equals("验证失败"))
                                    listener.onFailure("验证失败", "验证失败");
                            } catch (JSONException e1) {
                                listener.onFailure("ERROR", "连接服务器失败");
                            }
                        }

                    }
                }
                else{
                    listener.onFailure("ERROR", "连接服务器失败");

                }
            }
        }.execute();
    }
}
