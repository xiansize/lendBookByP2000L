package com.tc.nfc.core.util;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by xiansize on 2017/5/19.
 */
public class JsonUtils {

    /**
     *
     * @param jsonObject
     * @return
     */
    public static String getMessageFromJson(JSONObject jsonObject){
        String message = "";
        try {
            JSONArray jsonArray = jsonObject.getJSONArray("messagelist");
            JSONObject json = jsonArray.getJSONObject(0);
            message = json.getString("message");

        } catch (JSONException e) {
            Log.d("test","访问interlib3接口的时候获取的message异常");
        }

        return message;
    }


}
