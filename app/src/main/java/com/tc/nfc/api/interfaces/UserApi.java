package com.tc.nfc.api.interfaces;

import android.content.Context;

import org.json.JSONObject;

/**
 * Created by tangjiarao on 16/6/23.
 */
public interface UserApi {

    public String getUserInfo(Context context);
    public String getReaderInfo(String readerId);
}
