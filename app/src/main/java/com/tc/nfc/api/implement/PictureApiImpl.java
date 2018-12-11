package com.tc.nfc.api.implement;

import com.tc.api.VersionSetting;
import com.tc.nfc.api.interfaces.PictureApi;
import com.tc.nfc.api.utils.NetworkHelp;

/**
 * Created by xiansize on 2017/4/1.
 */
public class PictureApiImpl implements PictureApi{
    private final String TAG = "PictureApiImpl";

    @Override
    public String isFacePicture(String loginId, String password, String appId, String baseImg64) {

        return NetworkHelp.checkFacePicConnPost(0,VersionSetting.CHEKC_FACE_PICTURE,loginId,password,appId,baseImg64,null,null,null,null,null);
    }

    @Override
    public String RecordFacePictureFirst(String loginId, String password, String appId, String baseImg64, String globalLibId, String userLib, String userCode) {
        return NetworkHelp.checkFacePicConnPost(1,VersionSetting.SCAN_FACE_FIRST,loginId,password,appId,baseImg64,globalLibId,userLib,userCode,null,null);
    }

    @Override
    public String RecordFacePictureAgain(String loginId, String password, String appId, String baseImg64, String globalLibId, String userRecNo) {
        return NetworkHelp.checkFacePicConnPost(2,VersionSetting.SCAN_FACE_AGAIN,loginId,password,appId,baseImg64,globalLibId,null,null,userRecNo,null);
    }

    @Override
    public String deleteFacePicture(String loginId, String password, String appId, String userRecNo, String faceIds) {
        return NetworkHelp.checkFacePicConnPost(3,VersionSetting.DELETE_SCAN_FACE,loginId,password,null,null,null,null,null,userRecNo,faceIds);
    }

    @Override
    public String faceIdentify(String loginId, String password, String appId, String baseImg64, String globalLidId) {
        return NetworkHelp.checkFacePicConnPost(4,VersionSetting.FACE_IDENTIFY,loginId,password,appId,baseImg64,globalLidId,null,null,null,null);
    }

    @Override
    public String getFace(String faceId) {
        return NetworkHelp.checkFacePicConnPost(5,VersionSetting.GET_FACE,null,null,null,null,null,null,null,null,faceId);
    }

    @Override
    public String deletePerson(String loginId, String password, String appId, String globalLidId, String userCode) {
        return NetworkHelp.checkFacePicConnPost(6,VersionSetting.DELETE_SCAN_FACE_USER,loginId,password,appId,null,globalLidId,null,userCode,null,null);
    }
}
