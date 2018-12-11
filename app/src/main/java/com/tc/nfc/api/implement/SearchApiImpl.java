package com.tc.nfc.api.implement;


import com.tc.api.VersionSetting;
import com.tc.nfc.api.interfaces.SearchApi;
import com.tc.nfc.api.utils.NetworkHelp;
import com.tc.nfc.util.Constant;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tangjiarao on 16/6/23.
 */
public class SearchApiImpl implements SearchApi {

    private final String TAG = "SearchApiImpl";


    @Override
    public String bookSearch(String text, String page) {

        if(VersionSetting.IS_CONNECT_INTERLIB3){
            Map<String, String> params =new HashMap<>();
            params.put("token", Constant.TOKEN);
            params.put("libcode",VersionSetting.LIBCODE);
            params.put("queryparam","title");
            params.put("queryvalue",text);
            params.put("page",page);
            return NetworkHelp.sendDataByPost(params,"UTF-8", VersionSetting.URL_INTERLIB3_OPEN+"/service/book/searchbib");

        }else{

            Map<String, String> params =new HashMap<>();
            params.put("q",text);
            params.put("searchWay","title");
            params.put("rows","10");
            params.put("page",page);
            params.put("return_fmt","json");
            params.put("view", "json");
            return NetworkHelp.sendDataByPost(params,"UTF-8", VersionSetting.baseUrl3 +"/search");

        }
    }

}
