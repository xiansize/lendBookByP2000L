package com.tc.nfc.api.implement;

import android.util.Log;

import com.tc.api.VersionSetting;
import com.tc.nfc.api.interfaces.LoginApi;
import com.tc.nfc.api.utils.NetworkHelp;
import com.tc.nfc.util.Constant;
import com.tc.nfc.util.MD5Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by tangjiarao on 16/6/23.
 */
public class LoginApiImpl implements LoginApi {

    private final String TAG = "LoginApiImpl";

    @Override
    public String login(String uid, String password) {


        if(VersionSetting.IS_SHENZHEN_LIB){

           Map<String,String> map = new HashMap<String,String>();
           map.put("cardId",uid);
           map.put("password",password);
           return NetworkHelp.sendDataByPost(map,"UTF-8",VersionSetting.baseUrl+"/reader/login");

        }else if(VersionSetting.IS_CONNECT_INTERLIB3){

            //首先通过ip和密钥访问获得到token
            String token = NetworkHelp.getDataByGet("UTF-8",VersionSetting.URL_INTERLIB3_OPEN + "/service/barcode/token?appid="+VersionSetting.INTERLIB3_APP_ID+"&secret="+VersionSetting.INTERLIB3_APP_SECRET);
            try {
                JSONObject jsonObject = new JSONObject(token);
                if(jsonObject.getString("success").equals("true")){

                    JSONArray jsonArray = jsonObject.getJSONArray("messagelist");
                    JSONObject json = jsonArray.getJSONObject(0);
                    Constant.TOKEN =  json.getString("token");
                }else{

                    Log.d(TAG,"获取token失败");
                    return "";
                }
            } catch (JSONException e) {
                Log.d(TAG,"获取token异常");
            }

            //获得到token就进行读者验证的登陆
            Map<String, String> map = new HashMap<String, String>();
            map.put("token", Constant.TOKEN);
            map.put("rdid", uid);
            map.put("rdpasswd",password);
            map.put("opuser",VersionSetting.OPERATOR_USER);
            return NetworkHelp.sendDataByPost(map, "UTF-8", VersionSetting.URL_INTERLIB3_OPEN +"/service/reader/searchreader");


        }else{

            JSONObject json = new JSONObject();
            try {
                Constant.uid =uid;
                json.put("rdid", Constant.uid);
                String[] keycode = Constant.getKeyCode(Constant.uid);
                json.put("keyCode", keycode[0]);
                json.put("time", keycode[1]);
                json.put("password", password);
                json.put("action","login");

                Log.d("test","提交:"+json.toString());
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
    public String loginAdmin(String uid, String password) {
        //深圳南山图书馆
        if(VersionSetting.IS_SHENZHEN_LIB){
            Map<String,String> map = new HashMap<String,String>();
            map.put("userId",uid);
            map.put("password",password);

            return NetworkHelp.sendDataByPost(map,"UTF-8",VersionSetting.baseUrl+"/login");

            //连接interlib3
        }else if(VersionSetting.IS_CONNECT_INTERLIB3){

            //先获取开放接口的token
            //首先通过ip和密钥访问获得到token
            String token = NetworkHelp.getDataByGet("UTF-8",VersionSetting.URL_INTERLIB3_OPEN + "/service/barcode/token?appid="+VersionSetting.INTERLIB3_APP_ID+"&secret="+VersionSetting.INTERLIB3_APP_SECRET);
            try {
                JSONObject jsonObject = new JSONObject(token);
                if(jsonObject.getString("success").equals("true")){

                    JSONArray jsonArray = jsonObject.getJSONArray("messagelist");
                    JSONObject json = jsonArray.getJSONObject(0);
                    Constant.TOKEN =  json.getString("token");
                }else{

                    Log.d(TAG,"获取token失败");
                    return "";
                }
            } catch (JSONException e) {
                Log.d(TAG,"获取token异常");
            }

            Map<String, String> map = new HashMap<String, String>();
            map.put("loginid",uid);
            map.put("password", MD5Util.getMD5Security(password));
            return NetworkHelp.sendDataByPost(map, "UTF-8", VersionSetting.URL_INTERLIB3_OPEN +"/login/opLOGIN");



        }else {
            //其它图书馆
            JSONObject json = new JSONObject();
            try {
                Constant.uid = uid;
                json.put("rdid", Constant.uid);
                String[] keycode = Constant.getKeyCode(Constant.uid);
                json.put("keyCode", keycode[0]);
                json.put("time", keycode[1]);
                json.put("password", password);
                json.put("action", "adminLOGIN");
                json.put("loginurl", VersionSetting.baseUrl2 + "/login/opLOGIN".replace("//", "#").replace("/", "@"));

                //判断interlib的版本，如果是interlib3，版本为2，否则版本为1
                if('3' == VersionSetting.baseUrl2.charAt(VersionSetting.baseUrl2.length()-1)){
                    VersionSetting.interlibVersion = 2;
                }
                json.put("interlibVersion", VersionSetting.interlibVersion);

                return NetworkHelp.doJsonPost(VersionSetting.baseUrl, json.toString());
            } catch (JSONException e) {

                Log.d(TAG, "JSONException");
                return null;
            } catch (Exception e) {

                Log.d(TAG, "IOException");
                return null;
            }
        }
    }




}
