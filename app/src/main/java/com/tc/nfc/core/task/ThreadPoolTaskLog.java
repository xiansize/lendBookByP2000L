package com.tc.nfc.core.task;

import com.tc.api.VersionSetting;
import com.tc.nfc.api.utils.NetworkHelp;

/**
 * Created by xiansize on 2017/4/11.
 */
public class ThreadPoolTaskLog extends ThreadPoolTask{

    private String TAG = "ThreadPoolTaskLog";

    private String user;
    private String password;
    private String machineId;
    private String logType;
    private String logData1;
    private String logData2;
    private CallBack callBack;

    public ThreadPoolTaskLog(String url, String user, String password, String machineId, String logType, String logData1, String logData2, CallBack callBack) {
        super(url);
        this.user = user;
        this.password = password;
        this.machineId = machineId;
        this.logType = logType;
        this.logData1 = logData1;
        this.logData2 = logData2;
        this.callBack = callBack;
    }


    @Override
    public void run() {

        String result = NetworkHelp.uploadOperationLog(2, getURL(),user,password,machineId,null,null,null,null,null,null,null,logType,logData1,logData2,null,null,null,null);
        if(result != null && !result.equals("") && callBack != null){
            callBack.onReady(result);
        }
    }

    public interface CallBack {
        public void onReady(String result);
    }
}
