package com.tc.nfc.core.interfaces;

import android.os.Handler;

import com.tc.nfc.core.task.ThreadPoolManager;

/**
 * Created by xiansize on 2017/4/7.
 */
public interface LogAction {

    /**
     *
     * @param user
     * @param password
     * @param machineId
     * @param logLevel
     * @param content
     */
    public void uploadMachineLog(String user,String password,String machineId,String logLevel,String content);


    /**
     *
     * @param user
     * @param password
     * @param machineId
     * @param readCode
     * @param readerName
     * @param bookBarcode
     * @param bookTitle
     * @param type
     */
    public void uploadBookLog(String user,String password,String machineId,String readCode,String readerName,String bookBarcode,String bookTitle,String type,String fee,String borrowdate,String shoulddate,String renewdate);

    /**
     *
     * @param user
     * @param password
     * @param machineId
     * @param logType
     * @param logData1
     */
    public void uploadWorkStationLog(String user,String password,String machineId,String logType,String logData1,String logData2);

    /**
     *
     * @param user
     * @param password
     * @param machineID
     * @param logType
     * @param logData1
     * @param logData2
     * @param poolManager
     * @param handler
     */
    public void uploadInventoryLog(String user, String password,String machineID,String logType,String logData1,String logData2, ThreadPoolManager poolManager, Handler handler);

    /**
     *
     * @param userId
     * @param userName
     * @param userPassword
     * @param mType
     * @param libcode
     * @param borrowStatus
     * @param returnStatus
     * @param renewStatus
     */
    public void uploadLogLogin(String userId,String userName,String userPassword,String mType,String libcode,String borrowStatus,String returnStatus,String renewStatus);


}
