package com.tc.nfc.core.implement;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.tech.NfcV;
import android.os.AsyncTask;
import android.util.Log;

import com.tc.api.VersionSetting;
import com.tc.nfc.api.implement.BaseApiImpl;
import com.tc.nfc.api.implement.CommonApiImpl;
import com.tc.nfc.api.interfaces.BaseApi;
import com.tc.nfc.api.interfaces.CommonApi;
import com.tc.nfc.core.interfaces.BaseAction;
import com.tc.nfc.core.interfaces.CommonAction;
import com.tc.nfc.core.interfaces.LogAction;
import com.tc.nfc.core.listener.ActionCallbackListener;
import com.tc.nfc.core.util.JsonUtils;
import com.tc.nfc.util.Constant;
import com.tc.nfc.util.MD5Util;
import com.tc.nfc.util.NFCApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tangjiarao on 16/6/23.
 */
public class BaseActionImpl implements BaseAction {

    private BaseApi mBaseApi;
    private Context context;
    public BaseActionImpl(Context context) {
        this.context = context;
        this.mBaseApi = new BaseApiImpl();
    }
    /**
     * 用户是否登录
     */
    public boolean isLogin(JSONObject jsonObject) {
        boolean flag=false;
        try {
            flag =jsonObject.getString("message").contains("重新登录")?false:true;
            return flag;
        } catch (JSONException e) {
            Log.d("ERROR","BaseActionImpl的JSONException");
            e.printStackTrace();
            return flag;
        }
    }

    /**
     * 访问interlib3的时候检查有没有token
     * @param jsonObject
     * @return
     */
    public  boolean isNoToken(JSONObject jsonObject){
        boolean flag=false;
        try {
            flag = JsonUtils.getMessageFromJson(jsonObject).contains("凭证已过期")?true:false;
            return flag;
        } catch (Exception e) {
            Log.d("ERROR","检查token的时候异常");
            e.printStackTrace();
            return flag;
        }
    }

    @Override
    public void getImage(final String isbnGroups,final ActionCallbackListener<Map<String,String>> listener) {

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {

                return mBaseApi.getImage(isbnGroups);
            }

            @Override
            protected void onPostExecute(String result) {

                if (listener != null && result != null) {
                    Log.d("json", result);

                    try {
                        JSONObject jsonObject =new JSONObject(
                                result.substring(result.indexOf("(") + 1, result.lastIndexOf(")")));//打印中间字符

                        JSONArray jsonArray =jsonObject.getJSONArray("result");

                        Map<String,String> map = new HashMap<String, String>();

                        for (int i=0;i<jsonArray.length();i++){
                            JSONObject jsonObj =jsonArray.getJSONObject(i);
                            map.put(jsonObj.get("isbn").toString(),jsonObj.get("coverlink").toString());
                        }
                        listener.onSuccess(map);

                    } catch (JSONException e) {
                        Log.d("ERROR","BaseActionImpl : "+"JSONException");
                        e.printStackTrace();
                        listener.onFailure("ERROR", "连接服务器失败");

                    }catch(StringIndexOutOfBoundsException e){
                        e.printStackTrace();
                    }

                }
                else{
                    listener.onFailure("ERROR", "连接服务器失败");
                }
            }
        }.execute();

    }


}
