package com.tc.nfc.core.listener;

import org.json.JSONException;

/**
 * Created by tangjiarao on 16/6/23.
 */
public interface ActionCallbackListener<T> {
    /**
     * 成功时调用
     *
     * @param data 返回的数据
     */
    public void onSuccess(T data);

    /**
     * 失败时调用
     *
     * @param errorEvemt 错误码
     * @param message    错误信息
     */
    public void onFailure(String errorEvent, String message);
}