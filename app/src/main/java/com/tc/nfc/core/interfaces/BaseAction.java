package com.tc.nfc.core.interfaces;

import android.app.Activity;
import android.content.Context;

import com.tc.nfc.core.listener.ActionCallbackListener;

import org.json.JSONObject;

import java.util.Map;

/**
 * Created by tangjiarao on 16/6/23.
 */
public interface BaseAction {
    /**
     * 是否已经登录
     */
    public boolean isLogin(JSONObject jsonObject);

    /**
     * 获取书本封面
     * @param isbnGroups
     * @param listener
     */
    public void getImage(String isbnGroups,ActionCallbackListener<Map<String,String>> listener);
}
