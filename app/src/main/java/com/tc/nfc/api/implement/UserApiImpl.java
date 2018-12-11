package com.tc.nfc.api.implement;

import android.content.Context;
import android.util.Log;


import com.tc.api.VersionSetting;
import com.tc.nfc.api.interfaces.UserApi;
import com.tc.nfc.api.utils.NetworkHelp;
import com.tc.nfc.util.Constant;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by tangjiarao on 16/6/23.
 */
public class UserApiImpl implements UserApi {

    private final String TAG = "UserApiImpl";
    @Override
    public String getUserInfo(Context context) {

        if(VersionSetting.IS_SHENZHEN_LIB){
            Map<String,String> map = new HashMap<>();
            map.put("cardId",Constant.readerId);
            return NetworkHelp.sendDataByPost(map,"UTF-8",VersionSetting.baseUrl+"/reader/seach");

        }else if(VersionSetting.IS_CONNECT_INTERLIB3){
            Map<String, String> map = new HashMap<>();
            map.put("token",Constant.TOKEN);
            map.put("rdid", Constant.readerId);
            return NetworkHelp.sendDataByPost(map, "UTF-8", VersionSetting.URL_INTERLIB3_OPEN+"/service/reader/searchreader");


        }else{
            JSONObject json = new JSONObject();
            try {
                String rdid = Constant.readerId;
                json.put("action", "readerInfo");
                json.put("password", "");
                json.put("cardtype", "");
                json.put("rdid", rdid);
                String[] keycode = Constant.getKeyCode(rdid);
                json.put("keyCode", keycode[0]);
                json.put("time", keycode[1]);
                //获取得到从网络上得到的reader数据
                return NetworkHelp.doJsonPost(VersionSetting.baseUrl, json.toString());

            } catch (JSONException e) {
                Log.d(TAG,"JSONException");
                return null;
            } catch (Exception e) {
                Log.d(TAG,"IOException");
                return null;
            }

        }


    }

    @Override
    public String getReaderInfo(String readerId) {

        if(VersionSetting.IS_SHENZHEN_LIB){
            Map<String,String> map = new HashMap<>();
            map.put("cardId",readerId);
            return NetworkHelp.sendDataByPost(map,"UTF-8",VersionSetting.baseUrl+"/reader/seach");

        }else if(VersionSetting.IS_CONNECT_INTERLIB3){

            Map<String, String> map = new HashMap<>();
            map.put("token",Constant.TOKEN);
            map.put("rdid", readerId);
            return NetworkHelp.sendDataByPost(map, "UTF-8", VersionSetting.URL_INTERLIB3_OPEN+"/service/reader/searchreader");


        }else{

            JSONObject json = new JSONObject();
            try {
                json.put("action", "readerInfo");
                json.put("password", "");
                json.put("cardtype", "");
                json.put("rdid", readerId);
                //String[] keycode = Constant.getKeyCode(Constant.reader.getString("rdid"));
                String[] keycode = Constant.getKeyCode(Constant.uid);
                json.put("keyCode", keycode[0]);
                json.put("time", keycode[1]);
                Log.d("XX", json.toString());
                return NetworkHelp.doJsonPost(VersionSetting.baseUrl, json.toString());
            } catch (JSONException e) {
                Log.d(TAG,"JSONException");
                return null;
            } catch (Exception e) {
                Log.d(TAG,"IOException");
                return null;
            }
        }
    }
}
