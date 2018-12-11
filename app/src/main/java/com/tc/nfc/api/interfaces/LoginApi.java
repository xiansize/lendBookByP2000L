package com.tc.nfc.api.interfaces;

/**
 * Created by tangjiarao on 16/6/23.
 */
public interface LoginApi {

    public String login(String uid,String pass);

    public String loginAdmin(String uid,String pass);

}
