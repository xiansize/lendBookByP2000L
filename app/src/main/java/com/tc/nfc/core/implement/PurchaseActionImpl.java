package com.tc.nfc.core.implement;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;

import com.tc.nfc.api.implement.PurchaseApiImpl;
import com.tc.nfc.api.implement.SearchApiImpl;
import com.tc.nfc.api.interfaces.PurchaseApi;
import com.tc.nfc.api.interfaces.SearchApi;
import com.tc.nfc.api.utils.NetworkHelp;
import com.tc.nfc.core.interfaces.PurchaseAction;
import com.tc.nfc.core.interfaces.SearchAction;
import com.tc.nfc.core.listener.ActionCallbackListener;
import com.tc.nfc.model.Book;
import com.tc.nfc.model.LibPurchase;
import com.tc.nfc.model.PurchaseNum;
import com.tc.nfc.model.SearchBookResult;
import com.tc.nfc.model.TotalPurchase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by tangjiarao on 16/6/23.
 */
public class PurchaseActionImpl extends BaseActionImpl implements PurchaseAction {

    private Context context;
    private PurchaseApiImpl mPurchaseApiImpl;


    public PurchaseActionImpl(Context context) {
        super(context);
        this.context = context;
        this.mPurchaseApiImpl = new PurchaseApiImpl();

    }

    @Override
    public void getBookSingle(final String isbn, final String bookrecno, final String IMEI, final ActionCallbackListener<List<Book>> listener) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {

                return mPurchaseApiImpl.getBookSingle(isbn, bookrecno, IMEI);
            }

            @Override
            protected void onPostExecute(String result) {

                if (listener != null && result != null) {

                    Log.d("json",result);
                    try {
                        List<Book> bookList =new ArrayList<Book>();
                        JSONObject jsonObject =new JSONObject(result);
                        if (jsonObject.getBoolean("success")){
                            Iterator iterator = jsonObject.getJSONObject("map").keys();
                            String key="";
                            while(iterator.hasNext()){
                                key = (String)iterator.next();
                                break;
                            }
                            JSONArray books =jsonObject.getJSONObject("map").getJSONArray(key);
                            for (int i=0;i<books.length();i++){
                                Book book =new Book();
                                book.setBookTitle(books.getJSONObject(i).getString("title"));
                                book.setIsbn(books.getJSONObject(i).getString("isbn"));
                                book.setAuthor(books.getJSONObject(i).getString("author"));
                                book.setPrice(books.getJSONObject(i).getString("priceStr"));
                                book.setPublisher(books.getJSONObject(i).getString("publisher"));
                                book.setBookDate(books.getJSONObject(i).getString("pubdate"));
                                book.setClassNo(books.getJSONObject(i).getString("classno"));
                                book.setBookrecno(books.getJSONObject(i).getString("bookrecno"));
                                bookList.add(book);
                            }
                            listener.onSuccess(bookList);
                        }
                    } catch (JSONException e) {
                        listener.onFailure("ERROR", "连接服务器失败");
                        e.printStackTrace();

                    }


                }
                else{
                    listener.onFailure("ERROR", "连接服务器失败");
                }
            }
        }.execute();
    }

    @Override
    public void getPurchaseData(final String bookrecno,final ActionCallbackListener<PurchaseNum> listener) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {

                return mPurchaseApiImpl.getPurchaseData(bookrecno);
            }

            @Override
            protected void onPostExecute(String result) {

                if (listener != null && result != null) {
                    Log.d("json",result);

                    try {
                        JSONObject jsonObject =new JSONObject(result);
                        JSONObject map =jsonObject.getJSONObject("map");
                        PurchaseNum purchaseNum = new PurchaseNum();
                        purchaseNum.setHoldingSum(map.getString("holdingSum"));
                        purchaseNum.setOrderSum(map.getString("orderSum"));
                        purchaseNum.setCirculateSum(map.getString("circulateSum"));
                        purchaseNum.setCommendSum(map.getString("commendSum"));
                        purchaseNum.setCopiesSumByBatchno(map.getString("copiesSumByBatchno"));
                        listener.onSuccess(purchaseNum);
                    } catch (JSONException e) {
                        listener.onFailure("ERROR", "连接服务器失败");
                        e.printStackTrace();
                    }

                }
                else{
                    listener.onFailure("ERROR", "连接服务器失败");
                }
            }
        }.execute();
    }

    @Override
    public void getOtherLibPurchaseData(final String isbn,final String isPubLib, final ActionCallbackListener<List<LibPurchase>> listener) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {

                return mPurchaseApiImpl.getOtherLibPurchaseData(isbn, isPubLib);
            }

            @Override
            protected void onPostExecute(String result) {

                if (listener != null && result != null) {
                    Log.d("json",result);
                    List<LibPurchase> libParchaseList = new ArrayList();

                    try {
                        JSONObject jsonObject =new JSONObject(result);
                        JSONArray jsonArray =jsonObject.getJSONArray("rows");
                        LibPurchase libPurchase ;
                        if (jsonArray.length()!=0) {
                            for (int i=0;i<jsonArray.length();i++){
                                libPurchase = new LibPurchase();
                                libPurchase.setLibName(jsonArray.getJSONObject(i).getString("name0"));
                                libPurchase.setQuantity(jsonArray.getJSONObject(i).getString("holdCount0"));
                                libParchaseList.add(libPurchase);
                            }
                            listener.onSuccess(libParchaseList);
                        }
                    } catch (JSONException e) {
                        listener.onFailure("ERROR", "连接服务器失败");
                        e.printStackTrace();
                    }

                }
                else{
                    listener.onFailure("ERROR", "连接服务器失败");
                }
            }
        }.execute();
    }

    @Override
    public void getTotalPurchaseData(final String IMEI,final ActionCallbackListener<TotalPurchase> listener) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {

                return mPurchaseApiImpl.getTotalPurchaseData(IMEI);
            }

            @Override
            protected void onPostExecute(String result) {

                if (listener != null && result != null&& !result.equals("")) {
                    Log.d("json",result);

                    try {
                        JSONObject jsonObject =new JSONObject(result);
                        JSONObject map =jsonObject.getJSONObject("map");
                        JSONObject orderInfo =map.getJSONObject("orderInfo");
                        JSONObject orderInfoToday =map.getJSONObject("orderInfoToday");
                        TotalPurchase totalPurchase = new TotalPurchase();
                        try{
                            totalPurchase.setTotalCopies(orderInfo.getString("TOTALCOPIES"));
                        }catch(Exception e){
                            totalPurchase.setTotalCopies("--");
                        }
                        try{
                            totalPurchase.setTotalPrice(orderInfo.getString("TOTALPRICE"));
                        }catch(Exception e){
                            totalPurchase.setTotalPrice("--");
                        }
                        try{
                            totalPurchase.setTotalAcqPrice(orderInfo.getString("TOTALACQPRICE"));
                        }catch(Exception e){
                            totalPurchase.setTotalAcqPrice("--");
                        }
                        try{
                            totalPurchase.setNowBatchnoSearchTotalNum(map.getString("nowBatchnoSearchTotalNum"));
                        }catch(Exception e){
                            totalPurchase.setNowBatchnoSearchTotalNum("--");
                        }
                        try{
                            totalPurchase.setTodayCopies(orderInfoToday.getString("TOTALCOPIES"));
                        }catch(Exception e){
                            totalPurchase.setTodayCopies("--");
                        }
                        try{
                            totalPurchase.setTodayPrice(orderInfoToday.getString("TOTALPRICE"));
                        }catch(Exception e){
                            totalPurchase.setTodayPrice("--");
                        }
                        try{
                            totalPurchase.setTodayAcqPrice(orderInfoToday.getString("TOTALACQPRICE"));
                        }catch(Exception e){
                            totalPurchase.setTodayAcqPrice("--");
                        }
                        try{
                            totalPurchase.setTodaySearchNum(map.getString("todaySearchNum"));
                        }catch(Exception e){
                            totalPurchase.setTodaySearchNum("--");
                        }
                        listener.onSuccess(totalPurchase);

                    } catch (JSONException e) {
                        listener.onFailure("ERROR", "连接服务器失败");
                        e.printStackTrace();
                    }

                }
                else{
                    listener.onFailure("ERROR", "连接服务器失败");
                }
            }
        }.execute();
    }

    @Override
    public void getServerData(final String imei,final ActionCallbackListener<Map<String, String>> listener) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {

                return mPurchaseApiImpl.getServerData(imei);
            }

            @Override
            protected void onPostExecute(String result) {

                if (listener != null && result != null&& !result.equals("")) {
                    Log.d("json",result);

                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        String offLineDataCount =jsonObject.getJSONObject("map").getString("offLineDataCount");
                        String bibliosOrderDataCount =jsonObject.getJSONObject("map").getString("bibliosOrderDataCount");

                        Map  map = new HashMap();
                        map.put("offLineDataCount",offLineDataCount);
                        map.put("bibliosOrderDataCount",bibliosOrderDataCount);
                        listener.onSuccess(map);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
                else{
                    listener.onFailure("ERROR", "连接服务器失败");
                }
            }
        }.execute();
    }

    @Override
    public void purchaseBookOnline(final Book book,final String copies, final String action,final String orderLibLocal, final ActionCallbackListener<Boolean> listener) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {

                return mPurchaseApiImpl.purchaseBookOnline(book, copies, action,orderLibLocal);
            }

            @Override
            protected void onPostExecute(String result) {

                if (listener != null && result != null&& !result.equals("")) {
                    Log.d("json",result);
                    try {
                        JSONObject jsonObject =new JSONObject(result);

                        listener.onSuccess(jsonObject.getBoolean("success"));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                else{
                    listener.onFailure("ERROR", "连接服务器失败");
                }
            }
        }.execute();
    }

}
