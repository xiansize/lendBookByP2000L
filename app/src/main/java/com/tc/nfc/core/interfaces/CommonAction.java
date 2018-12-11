package com.tc.nfc.core.interfaces;

import android.app.Activity;
import android.content.Context;

import com.tc.nfc.core.listener.ActionCallbackListener;

/**
 * Created by tangjiarao on 16/6/23.
 */
public interface CommonAction {
    /**
     * 判断手机是否有NFC功能
     * @return
     */
    public int isHasNFC();

    /**
     * 判断用户是否有写入操作
     * 返回true则创建相应文件夹与文件
     * 否则提示错误
     */
    public void checkWritePermissions(Activity activity);

    public void getServerVersion(ActionCallbackListener<Integer> listener);

    public void checkIsExit(Activity activity,long exitTime);
}
