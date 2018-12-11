package com.tc.nfc.core.implement;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.tc.api.VersionSetting;
import com.tc.nfc.api.implement.LogApiImpl;
import com.tc.nfc.api.interfaces.LogApi;
import com.tc.nfc.core.interfaces.BaseAction;
import com.tc.nfc.core.interfaces.LogAction;
import com.tc.nfc.core.task.ThreadPoolManager;
import com.tc.nfc.core.task.ThreadPoolTaskLog;

/**
 * Created by xiansize on 2017/4/7.
 */
public class LogActionImpl extends BaseActionImpl implements LogAction{
    private String TAG = "LogActionImpl";
    private Context context;
    private LogApi logApi;

    public LogActionImpl(Context context) {
        super(context);
        this.context = context;
        logApi = new LogApiImpl();
    }

    @Override
    public void uploadMachineLog(final String user, final String password, final String machineId, final String logLevel, final String content) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                return logApi.uploadMachineLog(user,password,machineId,logLevel,content);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
            }
        }.execute();
    }

    @Override
    public void uploadBookLog(final String user, final String password, final String machineId, final String readCode, final String readerName, final String bookBarcode, final String bookTitle, final String type, final String fee, final String borrowdate, final String shoulddate, final String renewdate) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                return logApi.uploadBookLog(user,password,machineId,readCode,readerName,bookBarcode,bookTitle,type,fee,borrowdate,shoulddate,renewdate);
            }

            @Override
            protected void onPostExecute(String s) {
                Log.d(TAG,"借还日志:"+s);
                super.onPostExecute(s);
            }
        }.execute();
    }


    @Override
    public void uploadWorkStationLog(final String user, final String password, final String machineId, final String logType, final String logData1, final String logData2) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                return logApi.uploadWorkStationLog(user,password,machineId,logType,logData1,logData2);
            }

            @Override
            protected void onPostExecute(String s) {

                super.onPostExecute(s);
            }
        }.execute();
    }

    @Override
    public void uploadInventoryLog(String user, String password, String machineID, String logType, String logData1, String logData2, ThreadPoolManager poolManager, Handler handler) {
        logApi.uploadInventoryLog(user, password, machineID, logType, logData1, logData2, poolManager, new ThreadPoolTaskLog.CallBack() {
            @Override
            public void onReady(String result) {
                if(result != null){
                    Log.d(TAG,"提交盘点日志返回的结果:"+result);
                }
            }
        });

    }

    @Override
    public void uploadLogLogin(final String userId, final String userName, final String userPassword, final String mType, final String libcode, final String borrowStatus, final String returnStatus, final String renewStatus) {

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {

                return logApi.uploadLogLogin(userId,userName,userPassword,mType,libcode,borrowStatus,returnStatus,renewStatus);
            }

            @Override
            protected void onPostExecute(String s) {
                Log.d("test","日志："+s);
                super.onPostExecute(s);
            }
        }.execute();

    }
}
