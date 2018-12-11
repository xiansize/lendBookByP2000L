package com.tc.nfc.api.implement;


import com.tc.api.VersionSetting;
import com.tc.nfc.api.interfaces.CommonApi;
import com.tc.nfc.api.utils.NetworkHelp;


import java.util.HashMap;
import java.util.Map;

/**
 * Created by tangjiarao on 16/6/23.
 */
public class CommonApiImpl implements CommonApi {

    private final String TAG = "CommonApiImpl";

    /**
     * 获取服务器版本号
     * @return
     */
    public String getServerVersion() {

        Map<String, String> parameters = new HashMap<>();;
        parameters.put("interfaceName", VersionSetting.interfaceName);

        return NetworkHelp.sendDataByPost(parameters, "utf-8", VersionSetting.GET_SERVER_IP);

    }
}
