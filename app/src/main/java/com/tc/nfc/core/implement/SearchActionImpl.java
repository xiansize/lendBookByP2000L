package com.tc.nfc.core.implement;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.tc.api.VersionSetting;
import com.tc.nfc.api.implement.SearchApiImpl;
import com.tc.nfc.api.interfaces.SearchApi;
import com.tc.nfc.core.interfaces.SearchAction;
import com.tc.nfc.core.listener.ActionCallbackListener;
import com.tc.nfc.model.SearchBookResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by tangjiarao on 16/6/23.
 */
public class SearchActionImpl extends BaseActionImpl implements SearchAction {

    private Context context;
    private SearchApi mSearchApi;
    private List<SearchBookResult> listData;
    private SharedPreferences sp ;
    public SearchActionImpl(Context context) {
        super(context);
        this.context = context;
        this.mSearchApi = new SearchApiImpl();
        listData=new ArrayList<SearchBookResult>();
        sp = context.getSharedPreferences("search_history", 0);
    }

    @Override
    public void bookSearch(final String text, final String page, final ActionCallbackListener<List<SearchBookResult>> listener) {

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {

                return mSearchApi.bookSearch(text,page);
            }

            @Override
            protected void onPostExecute(String result) {


                if (listener != null && result != null) {

                    try {

                        JSONObject jsonObject = new JSONObject(result);
                        JSONArray jsonArray = null;

                        if (SearchActionImpl.super.isNoToken(jsonObject)) {
                            listener.onFailure("登录", "账号验证过期，请重新登录..");
                            return;
                        }

                        if(VersionSetting.IS_CONNECT_INTERLIB3){
                            jsonArray = jsonObject.getJSONArray("pagelist");
                        }else{
                            jsonArray = jsonObject.getJSONArray("bookList");
                        }

                        if (jsonArray.length()!=0){
                            for (int i=0;i<jsonArray.length();i++){
                                JSONObject bookObj = new JSONObject(jsonArray.getJSONObject(i).toString());
                                SearchBookResult b = new SearchBookResult();
                                b.setBookrecno(bookObj.getString("bookrecno"));
                                b.setTitle(bookObj.getString("title"));
                                b.setAuthor(bookObj.getString("author"));
                                b.setClassno(bookObj.getString("classno"));
                                b.setIsbn(bookObj.getString("isbn").replace("-", ""));
                                b.setPage(bookObj.getString("page"));
                                b.setPrice(bookObj.getString("price"));
                                b.setPublisher(bookObj.getString("publisher"));
                                b.setPubdate(bookObj.getString("pubdate"));
                                b.setSubject(bookObj.getString("subject"));
                                b.setBooktype(bookObj.getString("booktype"));

                                listData.add(b);
                            }
                            listener.onSuccess(listData);

                        }
                        else{
                            listener.onFailure("没有数据", "没有数据");
                        }

                    }catch (JSONException e) {
                        listener.onFailure("ERROR", "连接服务器失败:"+e.getMessage());
                    }
                }
                else{
                    listener.onFailure("ERROR", "连接服务器失败");
                }
            }
        }.execute();

    }


    public void getImage(String isbnGroups, ActionCallbackListener<Map<String, String>> listener){
        super.getImage(isbnGroups,listener);
    }

}
