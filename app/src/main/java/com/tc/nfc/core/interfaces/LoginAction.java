package com.tc.nfc.core.interfaces;

import com.tc.nfc.core.listener.ActionCallbackListener;

/**
 * Created by tangjiarao on 16/6/23.
 */
public interface LoginAction {

    /**
     * 读者登录
     * @param uid
     * @param pass
     * @param listener
     */
    public void login(String uid, String pass,ActionCallbackListener<Void> listener);

    /**
     * 馆员登录
     * @param uid
     * @param pass
     * @param listener
     */
    public void loginAdmin(String uid,String pass,ActionCallbackListener<Void> listener);
}