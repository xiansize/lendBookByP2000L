package com.tc.nfc.api.interfaces;

import com.tc.nfc.core.task.ThreadPoolManager;
import com.tc.nfc.core.task.ThreadPoolTaskBitmap;
import com.tc.nfc.core.task.ThreadPoolTaskLog;

/**
 * Created by xiansize on 2017/4/7.
 */
public interface LogApi {

    /**
     *
     * @param user
     * @param password
     * @param machineId
     * @param logLevel
     * @param content
     */
    public String uploadMachineLog(String user,String password,String machineId,String logLevel,String content);


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
    public String uploadBookLog(String user,String password,String machineId,String readCode,String readerName,String bookBarcode,String bookTitle,String type,String fee,String borrowdate,String shoulddate,String renewdate);

    /**
     *
     * @param user
     * @param password
     * @param machineId
     * @param logType
     * @param logData1
     */
    public String uploadWorkStationLog(String user,String password,String machineId,String logType,String logData1,String logData2);


    /**
     *
     * @param user
     * @param password
     * @param machineId
     * @param logType
     * @param logData1
     * @param logData2
     * @param poolManager
     * @param callBack
     */
    public void uploadInventoryLog(String user, String password, String machineId, String logType, String logData1, String logData2, ThreadPoolManager poolManager, ThreadPoolTaskLog.CallBack callBack);


    /**
     * 提交日志的时候需要进行的注册
     * @param userId            用户账号（设备号）
     * @param userName          用户名（设备名）
     * @param userPassword      密码
     * @param mType             类型 201：工作站，202，自助机 203：办证机，204：盘点仪 205：安全门
     * @param libcode           分馆代码（非必需填）
     * @param borrowStatus      202的时候，必须填明是否有权限借书：1：有、0：没
     * @param returnStatus      202的时候，必须填明是否有权限还书：1：有、0：没
     * @param renewStatus       202的时候，必须填明是否有权限续借：1：有、0：没
     */
    public String uploadLogLogin(String userId,String userName,String userPassword,String mType,String libcode,String borrowStatus,String returnStatus,String renewStatus);

}
