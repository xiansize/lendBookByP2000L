package com.tc.nfc.core.implement;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.tc.nfc.api.implement.PictureApiImpl;
import com.tc.nfc.api.interfaces.PictureApi;
import com.tc.nfc.api.utils.NetworkHelp;
import com.tc.nfc.core.interfaces.PictureAction;
import com.tc.nfc.core.listener.ActionCallbackListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by xiansize on 2017/4/1.
 */
public class PictureActionImpl implements PictureAction {
    private final String TAG = "PictureActionImpl";

    private PictureApi mPictureApi;
    private Context context;

    public PictureActionImpl(Context context) {
        this.context = context;
        this.mPictureApi = new PictureApiImpl();
    }

    @Override
    public void isFacePicture(final String loginId, final String password, final String appId, final String baseImg64, final ActionCallbackListener<Void> listener) {

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                return mPictureApi.isFacePicture(loginId,password,appId,baseImg64);
            }

            @Override
            protected void onPostExecute(String result) {
                if(listener != null && result != null && !result.equals("")){
                    Log.d(TAG,"检测照片："+result.toString());
                    try {
                        JSONObject json = new JSONObject(result);
                        if(json.getString("success").equals("true")){

                            listener.onSuccess(null);

                        }else{
                            listener.onFailure("ERROR",json.getString("message"));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    Log.d(TAG,"result为空"+result.toString());
                }

            }
        }.execute();
    }

    @Override
    public void RecordFacePictureFirst(final String loginId, final String password, final String appId, final String baseImg64, final String globalLibId, final String userLib, final String userCode, final ActionCallbackListener<Void> listener) {

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                return mPictureApi.RecordFacePictureFirst(loginId,password,appId,baseImg64,globalLibId,userLib,userCode);
            }

            @Override
            protected void onPostExecute(String result) {
                if(listener != null && result != null && !result.equals("")){

                    try {
                        JSONObject json = new JSONObject(result);
                        if(json.getString("success").equals("true")){
                            listener.onSuccess(null);
                        }else{
                            listener.onFailure("ERROR",json.getString("message"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    Log.d(TAG,"result为空:"+result.toString());
                }

            }
        }.execute();
    }

    @Override
    public void RecordFacePictureAgain(final String loginId, final String password, final String appId, final String baseImg64, final String globalLibId, final String userRecNo, final ActionCallbackListener<Void> listener) {

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                return mPictureApi.RecordFacePictureAgain(loginId,password,appId,baseImg64,globalLibId,userRecNo);
            }

            @Override
            protected void onPostExecute(String result) {
                if(listener != null && result != null && !result.equals("")){

                    try {
                        JSONObject json = new JSONObject(result);
                        if(json.getString("success").equals("true")){
                            listener.onSuccess(null);
                        }else{
                            listener.onFailure("ERROR",json.getString("message"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    Log.d(TAG,"result为空:"+result.toString());
                }

            }
        }.execute();
    }

    @Override
    public void deleteFacePicture(final String loginId, final String password, final String appId, final String userRecNo, final String faceIds, final ActionCallbackListener<Void> listener) {

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                return mPictureApi.deleteFacePicture(loginId,password,appId,userRecNo,faceIds);
            }

            @Override
            protected void onPostExecute(String result) {
                if(listener != null && result != null && !result.equals("")){

                    try {
                        JSONObject json = new JSONObject(result);
                        if(json.getString("success").equals("true")){
                            listener.onSuccess(null);
                        }else{
                            listener.onFailure("ERROR",json.getString("message"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    Log.d(TAG,"result为空:"+result.toString());
                }
            }
        }.execute();
    }

    @Override
    public void faceIdentify(final String loginId, final String password, final String appId, final String baseImg64, final String globalLidId, final ActionCallbackListener<JSONObject> listener) {
        new AsyncTask<Void,Void,String>(){

            @Override
            protected String doInBackground(Void... params) {
                return mPictureApi.faceIdentify(loginId,password,appId,baseImg64,globalLidId);
            }

            @Override
            protected void onPostExecute(String result) {
                if(listener != null && result != null & !result.equals("")){
                    Log.d("test","识别后："+result);
                    try {
                        JSONObject json = new JSONObject(result);

                        if(json.getString("success").equals("true")){

                            JSONObject jsonObject = json.getJSONObject("map");
                            listener.onSuccess(jsonObject);
                        }else{
                            listener.onFailure("ERROR",json.getString("message"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }

        }.execute();
    }


    @Override
    public void getFace(final String faceId, ActionCallbackListener<Void> listener) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                return mPictureApi.getFace(faceId);
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
            }
        }.execute();
    }

    @Override
    public void deletePerson(final String loginId, final String password, final String appId, final String globalLidId, final String userCode, ActionCallbackListener<Void> listener) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                return mPictureApi.deletePerson(loginId,password,appId,globalLidId,userCode);
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
            }

        }.execute();
    }
}
