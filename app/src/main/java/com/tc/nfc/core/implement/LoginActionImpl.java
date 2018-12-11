package com.tc.nfc.core.implement;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;


import com.tc.api.VersionSetting;
import com.tc.nfc.api.implement.LoginApiImpl;
import com.tc.nfc.api.interfaces.LoginApi;
import com.tc.nfc.core.util.JsonUtils;
import com.tc.nfc.util.Constant;
import com.tc.nfc.core.interfaces.LoginAction;
import com.tc.nfc.core.listener.ActionCallbackListener;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by tangjiarao on 16/6/23.
 */
public class LoginActionImpl implements LoginAction {
    private final String TAG = "LoginActionImpl";
    private Context context;
    private LoginApi mLoginApi;

    public LoginActionImpl(Context context) {
        this.context = context;
        this.mLoginApi = new LoginApiImpl();
    }


    @Override
    public void login(final String uid, final String pass, final ActionCallbackListener<Void> listener) {

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {

                return mLoginApi.login(uid, pass);

            }

            @Override
            protected void onPostExecute(String result) {
                if (listener != null && result != null&&!result.equals("")) {

                    Log.d(TAG,"登陆:"+result);

                    try {

                        JSONObject json = new JSONObject(result);

                        if (json.getString("success").equals("true")) {

                            //南山图书馆的获取json解析方法;（办证押金）和（预付款）作为：原来的（馆际已借）和（积分）
                            if(VersionSetting.IS_SHENZHEN_LIB) {

                                json = json.getJSONObject("map");
                                //获取读者的相关信息放在Constant中
                                Constant.readerId = json.getString("cardId");
                                Constant.readerName = json.getString("userName");
                                Constant.readMoney = json.getString("Fine");
                                Constant.gbLoanedBook = json.getString("CardReplacementCharge");

                                try {
                                    Constant.loanedBook = json.getString("LoanedItemCountValue");
                                } catch (JSONException e) {
                                    Constant.loanedBook = "0";
                                }

                                Constant.score = json.getString("ServiceCharge");
                                listener.onSuccess(null);

                            }else if(VersionSetting.IS_CONNECT_INTERLIB3){

                                listener.onSuccess(null);

                            }else{

                                Constant.reader = json.getJSONObject("reader");
                                //获取读者的相关信息放在Constant中
                                Constant.readerId= Constant.reader.getString("rdid");
                                Constant.readerName = Constant.reader.getString("rdname");
                                Constant.readMoney = Constant.reader.getString("money");
                                Constant.gbLoanedBook = Constant.reader.getString("gb_loanedNum");
                                //Constant.maxGbLoanedBook =Constant.reader.getString("gb_MaxLoanNum");//馆际最多借书
                                Constant.loanedBook = Constant.reader.getString("loanedNum");
                                //Constant.maxLoanedBook = Constant.reader.getString("maxLoanNum");//本馆最多借书
                                Constant.score = Constant.reader.getString("score");
                                listener.onSuccess(null);
                            }

                        } else {

                            if(VersionSetting.IS_CONNECT_INTERLIB3){

                                listener.onFailure("ERROR", JsonUtils.getMessageFromJson(json));

                            }else {
                                listener.onFailure("ERROR", json.getString("message"));
                                Log.d("test", "失败信息：" + json.getString("message"));
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        listener.onFailure("ERROR", "连接服务器失败:"+e.getMessage());
                    }
                }
                else{

                    listener.onFailure("ERROR", "连接服务器失败");
                }
            }
        }.execute();

    }

    @Override
    public void loginAdmin(final String uid, final String pass,final ActionCallbackListener<Void> listener) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {

                return mLoginApi.loginAdmin(uid, pass);
            }

            @Override
            protected void onPostExecute(String result) {
                if (listener != null && result != null&&!result.equals("")) {


                    try {
                        JSONObject json =new JSONObject(result);

                        //深圳图书馆
                        if(VersionSetting.IS_SHENZHEN_LIB) {

                            if (json.getString("success").equals("true")) {

                                Constant.uid = uid;
                                listener.onSuccess(null);

                            } else {

                                listener.onFailure("ERROR", json.getString("message"));
                            }

                            //interlib3开放接口
                        }else if(VersionSetting.IS_CONNECT_INTERLIB3){


                            if(Constant.TOKEN.equals("")){
                                listener.onFailure("ERROR", "登陆失败：Token异常");
                                return;
                            }

                            if(json.getString("success").equals("true")){
                                Log.d(TAG,"登陆馆员后的token:"+Constant.TOKEN);
                                Constant.uid =uid;
                                listener.onSuccess(null);

                            }else{

                                listener.onFailure("ERROR", json.getString("message"));
                                Log.d(TAG,"登陆馆员后的token:"+Constant.TOKEN);
                            }


                        }else{
                            if (json.getString("success").equals("false")){
                                listener.onFailure("ERROR", json.getString("message"));

                            }
                            else {

                                Constant.uid =uid;
                                Constant.sessionId =json.getString("interlib_session");

                                listener.onSuccess(null);
                            }

                        }

                    }catch (Exception e) {
                        e.printStackTrace();
                        Log.d("test",TAG+" Exception:"+e.toString());
                        listener.onFailure("ERROR", "连接服务器异常");
                    }
                }
                else{
                    listener.onFailure("ERROR", "连接服务器失败");
                }
            }
        }.execute();

    }

}
