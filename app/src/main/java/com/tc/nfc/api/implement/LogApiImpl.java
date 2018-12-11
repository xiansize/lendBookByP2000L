package com.tc.nfc.api.implement;

import com.tc.api.VersionSetting;
import com.tc.nfc.api.interfaces.LogApi;
import com.tc.nfc.api.utils.NetworkHelp;
import com.tc.nfc.core.task.ThreadPoolManager;
import com.tc.nfc.core.task.ThreadPoolTaskLog;
import com.tc.nfc.util.Constant;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xiansize on 2017/4/7.
 */
public class LogApiImpl implements LogApi{

    private String TAG = "LogApiImpl";


    @Override
    public String uploadMachineLog(String user, String password, String machineId, String logLevel, String content) {
        return NetworkHelp.uploadOperationLog(0, VersionSetting.PATH_LOG_MACHINE,user,password,machineId,logLevel,content,null,null,null,null,null,null,null,null,null,null,null,null);
    }

    @Override
    public String uploadBookLog(String user, String password, String machineId, String readCode, String readerName, String bookBarcode, String bookTitle, String type, String fee, String borrowdate, String shoulddate, String renewdate) {
        return NetworkHelp.uploadOperationLog(1,VersionSetting.PATH_LOG_UPDATE_BOOK,user,password,machineId,null,null,readCode,readerName,bookBarcode,bookTitle,type,null,null,null,fee,borrowdate,shoulddate,renewdate);
    }


    @Override
    public String uploadWorkStationLog(String user, String password, String machineId, String logType, String logData1,String logData2) {
        return NetworkHelp.uploadOperationLog(2,VersionSetting.PATH_LOG_WORKSTATION,user,password,machineId,null,null,null,null,null,null,null,logType,logData1,logData2,null,null,null,null);
    }

    @Override
    public void uploadInventoryLog(String user, String password, String machineId, String logType, String logData1, String logData2, ThreadPoolManager poolManager, ThreadPoolTaskLog.CallBack callBack) {
        poolManager.addAsyncTask(new ThreadPoolTaskLog(VersionSetting.PATH_LOG_WORKSTATION,user,password,machineId,logType,logData1,logData2,callBack));
    }

    @Override
    public String uploadLogLogin(String userId, String userName, String userPassword, String mType, String libcode, String borrowStatus, String returnStatus, String renewStatus) {
        Map<String,String> map = new HashMap<String,String>();
        map.put("user", Constant.LOG_USER);
        map.put("passwd",userPassword);
        map.put("machineno",userId);
        map.put("name",userName);
        map.put("mtype",mType);
        map.put("libcode",libcode);
        map.put("borrowstatus",borrowStatus);
        map.put("returnstatus",returnStatus);
        map.put("renewstatus",renewStatus);


        return NetworkHelp.sendDataByPost(map,"UTF-8",VersionSetting.PATH_LOG_LOGIN);
        //NetworkHelp.uploadOperationLog(3,VersionSetting.PATH_LOG_LOGIN,userId,userPassword,userName,null,null,null,null,null,null,mType,borrowStatus,returnStatus,renewStatus);
    }
}
