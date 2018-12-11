package com.tc.nfc.core.interfaces;

import com.tc.nfc.core.listener.ActionCallbackListener;

import org.json.JSONObject;

/**
 * Created by xiansize on 2017/4/1.
 */
public interface PictureAction {


    /**
     *
     * @param loginId       登陆id
     * @param password      登陆密码
     * @param appId          应用的id
     * @param baseImg64     图片base64编码
     * @param listener
     * @return
     */
    public void isFacePicture(String loginId,String password,String appId,String baseImg64,ActionCallbackListener<Void> listener);

    /**
     *
     * @param loginId       登陆id
     * @param password      登陆密码
     * @param appId         应用id
     * @param baseImg64     base64编码的二进制图片数据
     * @param globalLibId   全局馆代码
     * @param userLib       用户馆
     * @param userCode      读者证号或者操作员代码
     * @param listener
     */
    public void RecordFacePictureFirst(String loginId,String password,String appId,String baseImg64,String globalLibId,String userLib,String userCode,ActionCallbackListener<Void> listener);

    /**
     *
     * @param loginId       登陆账号
     * @param password      登陆密码
     * @param appId         应用id
     * @param baseImg64     base64编码的二进制图片数据
     * @param globalLibId   全局馆代码
     * @param userRecNo     用户记录号（中心识别后获取）
     * @param listener
     * @return
     */
    public void RecordFacePictureAgain(String loginId,String password,String appId,String baseImg64,String globalLibId,String userRecNo,ActionCallbackListener<Void> listener);

    /**
     *
     * @param loginId       登陆id
     * @param password      登陆密码
     * @param appId         应用id
     * @param userRecNo    用户记录号（中心识别后获取）
     * @param faceIds       多个图片faceid,用~m~分隔
     * @param listener
     * @return
     */
    public void deleteFacePicture(String loginId,String password,String appId,String userRecNo,String faceIds,ActionCallbackListener<Void> listener);


    /**
     *
     * @param loginId
     * @param password
     * @param appId
     * @param baseImg64
     * @param globalLidId
     * @param listener
     * @return
     */
    public void faceIdentify(String loginId,String password,String appId,String baseImg64,String globalLidId,ActionCallbackListener<JSONObject> listener);


    /**
     *
     * @param faceId
     * @return
     */
    public void getFace(String faceId,ActionCallbackListener<Void> listener);

    /**
     *
     * @param loginId
     * @param password
     * @param appId
     * @param globalLidId
     * @param userCode
     * @param listener
     * @return
     */
    public void deletePerson(String loginId,String password,String appId,String globalLidId,String userCode,ActionCallbackListener<Void> listener);
}
