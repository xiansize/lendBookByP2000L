package com.tc.nfc.api.interfaces;

/**
 * Created by xiansize on 2017/4/1.
 */
public interface PictureApi {

    /**
     *
     * @param loginId
     * @param password
     * @param appId
     * @param baseImg64
     * @return
     */
    public String isFacePicture(String loginId,String password,String appId,String baseImg64);

    /**
     *
     * @param loginId
     * @param password
     * @param appId
     * @param baseImg64
     * @param globalLibId
     * @param userLib
     * @param userCode
     * @return
     */
    public String RecordFacePictureFirst(String loginId,String password,String appId,String baseImg64,String globalLibId,String userLib,String userCode);

    /**
     *
     * @param loginId
     * @param password
     * @param appId
     * @param baseImg64
     * @param globalLibId
     * @param userRecNo
     * @return
     */
    public String RecordFacePictureAgain(String loginId,String password,String appId,String baseImg64,String globalLibId,String userRecNo);

    /**
     *
     * @param loginId
     * @param password
     * @param appId
     * @param userRecNo
     * @param faceIds
     * @return
     */
    public String deleteFacePicture(String loginId,String password,String appId,String userRecNo,String faceIds);


    /**
     *
     * @param loginId
     * @param password
     * @param appId
     * @param baseImg64
     * @param globalLidId
     * @return
     */
    public String faceIdentify(String loginId,String password,String appId,String baseImg64,String globalLidId);

    /**
     *
     * @param faceId
     * @return
     */
    public String getFace(String faceId);

    /**
     *
     * @param loginId
     * @param password
     * @param appId
     * @param globalLidId
     * @param userCode
     * @return
     */
    public String deletePerson(String loginId,String password,String appId,String globalLidId,String userCode);
}
