package com.tc.nfc.api.implement;


import com.tc.nfc.api.interfaces.BaseApi;
import com.tc.nfc.api.utils.NetworkHelp;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tangjiarao on 16/6/23.
 */
public class BaseApiImpl implements BaseApi {

    private final String TAG = "BaseApiImpl";

    @Override
    public String getImage(String isbnGroups) {

        Map<String, String> params =new HashMap<>();
        params.put("glc","SELFACS");
        params.put("cmdACT","getImages");
        params.put("isbns",isbnGroups);
        params.put("callback","showCovers");

        return NetworkHelp.sendDataByPost(params,"UTF-8","http://api.interlib.com.cn/interlibopac/websearch/metares");
    }
}
