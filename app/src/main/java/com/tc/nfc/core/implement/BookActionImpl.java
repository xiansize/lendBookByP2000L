package com.tc.nfc.core.implement;

import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcV;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.tc.api.VersionSetting;
import com.tc.nfc.api.implement.BookApiImpl;
import com.tc.nfc.api.interfaces.BookApi;
import com.tc.nfc.app.activity.InventoryActivity;
import com.tc.nfc.app.utils.nfcA.NfcAUtil;
import com.tc.nfc.app.utils.nfcV.NfcVUtil;
import com.tc.nfc.core.interfaces.BookAction;
import com.tc.nfc.core.listener.ActionCallbackListener;
import com.tc.nfc.core.task.ThreadPoolManager;
import com.tc.nfc.core.task.ThreadPoolTaskBitmap;
import com.tc.nfc.core.util.BookSaxHandler;
import com.tc.nfc.core.util.JsonUtils;
import com.tc.nfc.model.Book;
import com.tc.nfc.model.BookHolding;
import com.tc.nfc.model.CheckBook;
import com.tc.nfc.model.ReturnBookResult;
import com.tc.nfc.app.utils.nfcA.NfcCommon;
import com.tc.nfc.model.ShelvesResult;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by tangjiarao on 16/6/23.
 * 该类是检查是否可以借书、借书、还书、加载已借图书、续借书的业务逻辑层
 */
public class BookActionImpl extends BaseActionImpl implements BookAction {

    private final String TAG = "BookActionImpl";
    private Context context;
    private BookApi mBookApi;
    private List<Book> bookList;
    private List<ReturnBookResult> bookResultList;
    private static final Handler mHandler = new Handler();
    private Boolean flag=false;
    private ShelvesResult shelvesResult;
    private Boolean is_otherLibBook =false;
    private Timer timer;
    private TimerTask task;


    //设置3s延迟
    private final int SECONDS=8;
    private int time=SECONDS;


    public BookActionImpl(Context context) {
        super(context);
        this.context = context;
        this.mBookApi = new BookApiImpl();

    }


    @Override
    public void p2000LGetBarcode(final ActionCallbackListener<String> listener) {

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                return mBookApi.P2000LGetBarcode();
            }

            @Override
            protected void onPostExecute(String result) {

                if(!result.equals("") && result != null) {
                    listener.onSuccess(result);
                }else{
                    listener.onFailure("ERROR","读条码失败");
                }
            }
        }.execute();
    }



    public void getNFCBarcode(Intent intent, ActionCallbackListener<String> listener) {
        int cardType= NfcCommon.checkCardType(intent, context);
        if (cardType==1){
            String resultStr = "";
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            NfcVUtil.mNfcv = NfcV.get(tag);
            try {
                //resultStr = NfcVUtil.readBarcode(intent);
                resultStr = NfcVUtil.checkBarcode();

                if (!NfcVUtil.isEmpty(resultStr)) {
                    Log.d("test","检查书本是否存在"+resultStr);
                    listener.onSuccess(resultStr);
                }
            } catch (Exception e) {

                e.printStackTrace();
                Log.d("ERROR", "BookActionImpl " + e.toString() + " :解析type1 卡的时候出现错误");
                listener.onSuccess(null);
            }
        }else if (cardType==2){
            NfcCommon.treatAsNewTag(intent, context);
            NfcAUtil.onCreateKeyMap(context,listener);
        }
    }


    /**
     * 判断图书是否可借
     * @param readerId 读者id
     * @param barcode  图书条形码
     * @param listener 回调
     */
    public void isBookCanLoadn(final String readerId,final String barcode,final ActionCallbackListener< Map<String, Object>> listener) {

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {

                return mBookApi.isBookCanLoadn(readerId, barcode);
            }

            @Override
            protected void onPostExecute(String result) {
                Log.d(TAG,"书本可借？："+result);

                if (listener != null && result != null) {
                    Log.d("isBookCanLoad", result);
                    try {

                        JSONObject jsonObject = new JSONObject(result);

                        if(VersionSetting.IS_SHENZHEN_LIB){

                            //有此条码，可借书
                            if(jsonObject.getString("success").equals("true")){

                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {

                                        try {
                                            Log.d("json","在改标识位的时候卡住了1");
                                            flag = NfcVUtil.writeAFI(true);
                                            Log.d("json","是否改了标识"+flag);
                                        } catch (IOException e) {
                                            flag =false;
                                            Log.d("json","是否改了标识"+flag);
                                            e.printStackTrace();
                                        }
                                        //执行完回调
                                        mHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                //图书编号不为空且已经修改标识位
                                                if(!barcode.isEmpty()&&flag){
                                                    listener.onSuccess(null);
                                                }else{
                                                    listener.onFailure("ERROR", "请在借书结束后才移动手机");
//
                                                }
                                            }
                                        });
                                    }
                                }).start();


                            }else{
                                //没有此条码
                                listener.onSuccess(judgeLoanResultForNanShan(barcode,jsonObject,0));

                            }

                            //连接到itnerlib3的接口,直接返回null值，执行
                        }else if(VersionSetting.IS_CONNECT_INTERLIB3){


                            new Thread(new Runnable() {
                                @Override
                                public void run() {

                                    try {

                                        if(VersionSetting.IS_P2000L_DEVICE){
                                            flag = NfcVUtil.p2000LWriteAFI(true);
                                        }else {
                                            flag = NfcVUtil.writeAFI(true);
                                        }
                                        Log.d("json","关闭标志位"+flag);
                                    } catch (IOException e) {
                                        flag =false;
                                        Log.d("json","关闭标志位"+flag);
                                        e.printStackTrace();
                                    }
                                    //执行完回调
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            //图书编号不为空且已经修改标识位
                                            if(!barcode.isEmpty()&&flag){
                                                listener.onSuccess(null);
                                            }else{
                                                listener.onFailure("ERROR", "请在借书结束后才移动手机");
//
                                            }
                                        }
                                    });
                                }
                            }).start();


                        }else{
                            if (!BookActionImpl.super.isLogin(jsonObject)) {
                                listener.onFailure("登录", "账号验证过期，请重新登录..");

                                return;
                            }
                            //图书可借
                            if (jsonObject.getString("success").equals("true")) {

//                                    try {
//                                        Log.d("json","在改标识位的时候卡住了:可借");
//                                        if(VersionSetting.IS_P2000L_DEVICE){
//                                            flag = NfcVUtil.p2000LWriteAFI(true);
//                                        }else {
//                                            flag = NfcVUtil.writeAFI(true);
//                                        }
//                                        Log.d("json","是否改了标识:可借"+flag);
//                                    } catch (IOException e) {
//                                        flag =false;
//                                        Log.d("json","是否改了标识:可借"+flag);
//                                        e.printStackTrace();
//                                    }
//
//                                    if(!barcode.isEmpty()&&flag){
//                                        listener.onSuccess(null);
//                                    }else{
//                                        listener.onFailure("ERROR", "请在借书结束后才移动手机");
//                                    }

                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {

                                            try {
                                                Log.d("json","在改标识位的时候卡住了:可借");
                                                if(VersionSetting.IS_P2000L_DEVICE){
                                                    flag = NfcVUtil.p2000LWriteAFI(true);
                                                }else {
                                                    flag = NfcVUtil.writeAFI(true);
                                                }
                                                Log.d("json","是否改了标识:可借"+flag);
                                            } catch (IOException e) {
                                                flag =false;
                                                Log.d("json","是否改了标识"+flag);
                                                e.printStackTrace();
                                            }
                                            //执行完回调
                                            mHandler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    //图书编号不为空且已经修改标识位
                                                    if(!barcode.isEmpty()&&flag){
                                                        listener.onSuccess(null);
                                                    }else{
                                                        listener.onFailure("ERROR", "请在借书结束后才移动手机");
//
                                                    }
                                                }
                                            });
                                        }
                                    }).start();

                                //外馆书先借后改
                            } else if (jsonObject.getString("message").contains("没有找到")) {
                                is_otherLibBook=true;
                                listener.onSuccess(null);
                            }
                            //图书不可借
                            else{

                                listener.onSuccess(judgeLoanResult(barcode, jsonObject,0));
                            }
                        }
                    }catch (JSONException e) {
                        listener.onFailure("ERROR", "连接服务器失败");
                    }
                }
                else{

                    listener.onFailure("ERROR", "连接服务器失败");
                }
            }
        }.execute();
    }


    /**
     * 借书操作
     * @param readerId  读者id
     * @param barcode   图书条形码
     * @param listener  回调
     */
    public void loanBook(final String readerId,final String barcode,final ActionCallbackListener< Map<String, Object>> listener) {

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {

                return mBookApi.loanBook(readerId, barcode);
            }

            @Override
            protected void onPostExecute(String result) {
                Log.d(TAG,"借书："+result);

                if (listener != null && result != null) {

                    try {
                        final JSONObject jsonObject = new JSONObject(result);

                        //南山图书馆的操作
                        if(VersionSetting.IS_SHENZHEN_LIB){

                            if(jsonObject.getString("success").equals("true")){//借书成功

                                listener.onSuccess(judgeLoanResultForNanShan(barcode, jsonObject,0));

                            }else{//借书失败

                                //把安全位改回来
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {

                                        try {
                                            Log.d("json","在改标识位的时候卡住了1");
                                            flag = NfcVUtil.writeAFI(false);
                                            Log.d("json","是否改了标识"+flag);
                                        } catch (IOException e) {
                                            flag =false;
                                            Log.d("json","是否改了标识"+flag);
                                            e.printStackTrace();
                                        }
                                        //执行完回调
                                        mHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                //图书编号不为空且已经修改标识位
                                                if(!barcode.isEmpty()&&flag){
                                                    listener.onSuccess(judgeLoanResultForNanShan(barcode, jsonObject, 0));
                                                }else{
                                                    listener.onFailure("ERROR", "借书失败");
//
                                                }
                                            }
                                        });
                                    }
                                }).start();

                            }

                            //直接连接interlib3开放接口
                        }else if(VersionSetting.IS_CONNECT_INTERLIB3){

                            if (BookActionImpl.super.isNoToken(jsonObject)) {
                                listener.onFailure("登录", "账号验证过期，请重新登录..");
                                return;
                            }
                            //借书成功
                            if(jsonObject.getString("success").equals("true")){

                                listener.onSuccess(judgeLoanResultForInterlib(barcode, jsonObject,1));

                            }else{

                                //借书失败，把安全位改回来
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {

                                        try {
                                            Log.d("json","在改标识位的时候卡住了3");
                                            if(VersionSetting.IS_P2000L_DEVICE){
                                                flag = NfcVUtil.p2000LWriteAFI(false);
                                            }else {
                                                flag = NfcVUtil.writeAFI(false);
                                            }
                                            Log.d("json","打开标志位"+flag);
                                        } catch (IOException e) {
                                            flag =false;
                                            Log.d("json","是否改了标识"+flag);
                                            e.printStackTrace();
                                        }
                                        //执行完回调
                                        mHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (flag){
                                                    listener.onSuccess(judgeLoanResultForInterlib(barcode, jsonObject,1));
                                                }else{
                                                    listener.onFailure("ERROR", "请在借书结束后才移动手机");
                                                }
                                            }
                                        });
                                    }
                                }).start();

                            }



                        }else{

                            if (!BookActionImpl.super.isLogin(jsonObject)) {
                                listener.onFailure("登录", "账号验证过期，请重新登录..");
                                return;
                            }
                            //借书失败
                            if (jsonObject.getString("success").equals("false") && !NfcVUtil.isEmpty(jsonObject.getString("message"))) {

                                is_otherLibBook=false;
                                listener.onSuccess(judgeLoanResult(barcode, jsonObject, 0));

                            }
                            //借书成功
                            else {
                                Log.d("XX","是否外馆"+is_otherLibBook);
                                //如果是外馆书就先修改标识位
                                if (is_otherLibBook){

                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {

                                            try {
                                                Log.d("json","在改标识位的时候卡住了3");
                                                if(VersionSetting.IS_P2000L_DEVICE){
                                                    flag = NfcVUtil.p2000LWriteAFI(true);

                                                }else{
                                                    flag = NfcVUtil.writeAFI(true);
                                                }
                                                Log.d("json","是否改了标识"+flag);
                                            } catch (IOException e) {
                                                flag =false;
                                                Log.d("json","是否改了标识"+flag);
                                                e.printStackTrace();
                                            }
                                            //执行完回调
                                            mHandler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (flag){
                                                        listener.onSuccess(judgeLoanResult(barcode, jsonObject,1));
                                                    }else{
                                                        listener.onSuccess(judgeLoanResult(barcode, jsonObject,-1));
                                                    }

                                                    is_otherLibBook=false;
                                                }
                                            });
                                        }
                                    }).start();
                                }

                                listener.onSuccess(judgeLoanResult(barcode, jsonObject,0));

                            }
                        }
                    } catch (JSONException e) {

                        e.printStackTrace();
//                        try {
//                            NfcVUtil.writeAFI(false);
//                        } catch (IOException e1) {
//                            e.printStackTrace();
//                        }

                        listener.onFailure("ERROR", "连接服务器失败" + e.getMessage());
                    }
                }
                else{
                    listener.onFailure("ERROR", "连接服务器失败");
                }
            }
        }.execute();

    }

    @Override
    public void returnBook(final String barcode,final ActionCallbackListener<Map<String, Object>> listener) {

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {

                return mBookApi.returnBook(barcode);
            }

            @Override
            protected void onPostExecute(String result) {
                Log.d(TAG,"还书："+result);

                if (listener != null && result != null) {
                    Log.d("json",result);

                    try {
                        final JSONObject jsonObject = new JSONObject(result);

                        if(VersionSetting.IS_CONNECT_INTERLIB3){
                            if (BookActionImpl.super.isNoToken(jsonObject)) {
                                listener.onFailure("登录", "账号验证过期，请重新登录..");
                                return;
                            }

                        }else {
                            if (!BookActionImpl.super.isLogin(jsonObject)) {
                                listener.onFailure("登录", "账号验证过期，请重新登录..");
                                return;
                            }
                        }

                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                try {
                                    Log.d("json","在改标识位的时候卡住了3");
                                    if(VersionSetting.IS_P2000L_DEVICE) {
                                        flag=NfcVUtil.p2000LWriteAFI(false);
                                    }else{
                                        flag = NfcVUtil.writeAFI(false);
                                    }
                                    Log.d("json","是否改了标识"+flag);
                                } catch (IOException e) {
                                    flag =false;

                                    e.printStackTrace();
                                }
                                //执行完回调
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        //图书编号不为空且已经修改标识位
                                        if(!barcode.isEmpty()&&flag){

                                            if(VersionSetting.IS_SHENZHEN_LIB){//南山图书馆的还书处理

                                                listener.onSuccess(judgeReturnResultForNanShan(barcode,jsonObject));

                                            }else if(VersionSetting.IS_CONNECT_INTERLIB3){

                                                listener.onSuccess(judgeReturnResultForInterlib(barcode, jsonObject));

                                            }else{

                                                listener.onSuccess(judgeReturnResult(barcode, jsonObject));
                                            }

                                        }else{
                                            listener.onFailure("ERROR", "请在还书结束后才移动手机");
                                        }
                                    }
                                });
                            }
                        }).start();
//                        try {
//                            boolean isChangeNfc=NfcVUtil.writeAFI(false);
//                            Log.d("JSON","还书标识位修改:"+isChangeNfc);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }

//                        listener.onSuccess(judgeReturnResult(barcode, jsonObject));

                    } catch (Exception e) {
//                        if (result.trim().equals("{\"success\":\"false\",\"message\":\"\"}")){
//                            listener.onSuccess(judgeReturnResult(barcode, jsonObject));
//                        }
//                        else
//                        {
                            listener.onFailure("ERROR", "连接服务器失败"+e.getMessage());
//                        }
                    }
                }
                else{

                    listener.onFailure("ERROR", "连接服务器失败");
                }
            }
        }.execute();

    }

    @Override
    public void loanBookData(final String readerId,final ActionCallbackListener<List<Book>> listener) {

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {

                return mBookApi.loanBookData(readerId);
            }

            @Override
            protected void onPostExecute(String result) {

                if (listener != null && result != null) {


                    if(VersionSetting.IS_SHENZHEN_LIB){

                        bookList = new ArrayList<Book>();

                        Log.d("test1","读者借的书和barcode："+result);

                        try {

                            JSONObject jsonObject = new JSONObject(result);
                            JSONArray jsonArray = jsonObject.getJSONArray("book");

                            if(jsonObject.getString("success").equals("true")){

                                for(int x=0;x<jsonArray.length();x++){

                                    JSONObject bookJson = (JSONObject) jsonArray.get(x);
                                    Book book = new Book();
                                    book.setBookTitle(bookJson.getString("title"));
                                    book.setBookBarcode(bookJson.getString("barcode"));
                                    bookList.add(book);

                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                        listener.onSuccess(bookList);

                    }else if(VersionSetting.IS_CONNECT_INTERLIB3){

                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject(result);
                            if (BookActionImpl.super.isNoToken(jsonObject)) {
                                listener.onFailure("登录", "账号验证过期，请重新登录..");
                                return;
                            }

                            bookList = new ArrayList<Book>();
                            JSONArray jsonArray = jsonObject.getJSONArray("loanlist");
                            for(int x=0;x<jsonArray.length();x++){

                                Book b = new Book();
                                JSONObject jsonObj = jsonArray.getJSONObject(x);
                                b.setBookBarcode(jsonObj.getString("barcode"));

                                try {
                                    b.setIsbn(jsonObj.getString("isbn").replace("-",""));
                                } catch (JSONException e) {
                                    b.setIsbn("7541818569");
                                }
                                b.setBookTitle(jsonObj.getString("title"));
                                b.setAuthor(jsonObj.getString("author"));
                                b.setPublisher(jsonObj.getString("curlocalname"));//馆藏地点
                                b.setLoanDate(jsonObj.getString("loandate"));
                                b.setReturnDate(jsonObj.getString("returndate"));
                                b.setLoanCount(jsonObj.getString("rewnewcount"));
                                b.setIsCheck(false);
                                bookList.add(b);

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        listener.onSuccess(bookList);


                    }else{
                        //                    try {
                        bookList = new ArrayList<Book>();
                        //JSONObject jsonObject = new JSONObject(result);
//                        if (!BookActionImpl.super.isLogin(jsonObject)) {
//                            listener.onFailure("登录", "账号验证过期，请重新登录..");
//                            return;
//                        }

//                        if (jsonObject.getString("loanSize").equals("0")) {
//                            list = new JSONArray();
//                            Log.d("JSON",list.toString());
//                        } else {
//                            list = jsonObject.getJSONArray("loanList");
//                            Log.d("JSON", list.toString());
//                        }
//
//                        for (int i = 0; i < list.length(); i++) {
//                            Book b = new Book();
//                            JSONObject jsonObj = list.getJSONObject(i);
//                            b.setBookTitle(jsonObj.getString("title"));
//                            b.setBookBarcode(jsonObj.getString("barcode"));
//                            b.setBookDate(jsonObj.getString("returnDate"));
//                            b.setAuthor(jsonObj.getString("author"));
//                            b.setIsCheck(false);
//                            bookList.add(b);
//                        }

                        String[] books = result.split("<return>");
                        if (books.length>1) {
                            String xml = "<root>";
                            for (int i = books.length - 1; i >= 0; i--) {
                                if (i != 0 && i != books.length - 1) {

                                    xml = xml + "<return>" + books[i];
                                } else if (i == books.length - 1) {
                                    xml = xml + "<return>" + books[i].split("</return>")[0] + "</return>";

                                }

                            }
                            xml = xml + "</root>";
                            // 创建一个新的字符串
                            StringReader read = new StringReader(xml);
                            // 创建新的输入源SAX 解析器将使用 InputSource 对象来确定如何读取 XML 输入
                            InputSource source = new InputSource(read);
                            //使用工厂方法初始化SAXParserFactory变量spf
                            SAXParserFactory spf = SAXParserFactory.newInstance();
                            //通过SAXParserFactory得到SAXParser的实例
                            SAXParser sp = null;
                            try {
                                sp = spf.newSAXParser();
                                //通过SAXParser得到XMLReader的实例
                                XMLReader xr = sp.getXMLReader();
                                //初始化自定义的类MySaxHandler的变量msh，将beautyList传递给它，以便装载数据
                                BookSaxHandler msh = new BookSaxHandler(bookList);
                                //将对象msh传递给xr
                                xr.setContentHandler(msh);
                                //调用xr的parse方法解析输入流
                                xr.parse(source);
                            } catch (ParserConfigurationException e) {
                                e.printStackTrace();
                            } catch (SAXException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            Log.d("test", bookList.size() + ":已经借了的书数量");
                        }
                        listener.onSuccess(bookList);


                    }



//                    }
//                    catch (JSONException e) {
//                        e.printStackTrace();
//                    }
                }
                else{

                    listener.onFailure("ERROR", "连接服务器失败");
                }
            }
        }.execute();
    }



    public void reloanBook(final String readerId,final String barcode,final ActionCallbackListener<String> listener){

        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                    return mBookApi.reloanBook(readerId, barcode);

            }

            @Override
            protected void onPostExecute(String result) {
                if (listener != null && result != null) {
                    Log.d("json",result);
                    try {
                        listener.onSuccess(result);

                    } catch (Exception e) {

                        listener.onFailure("ERROR", "连接服务器失败"+e.getMessage());
                    }
                }
                else{

                    listener.onFailure("ERROR", "连接服务器失败");
                }
            }
        }.execute();
    }




    public void reloanBookMany(final String readerId,final List<Book> books,final ActionCallbackListener<List<ReturnBookResult>> listener) {

        bookResultList =new ArrayList<>();
        int i;
        for (i = 0; i < books.size(); i++) {

            final ReturnBookResult returnBookResult = new ReturnBookResult();
            final int iTemp = i;
            Log.d("test","续借的barcode："+books.get(iTemp).getBookBarcode());
            reloanBook(readerId, books.get(iTemp).getBookBarcode(), new ActionCallbackListener<String>() {
                @Override
                public void onSuccess(String data) {
                    try{
                        JSONObject jsonObject = new JSONObject(data);

                        if(VersionSetting.IS_CONNECT_INTERLIB3){

                            if (BookActionImpl.super.isNoToken(jsonObject)) {
                                listener.onFailure("登录", "账号验证过期，请重新登录..");
                                return;
                            }

                            returnBookResult.setBook(books.get(iTemp));
                            returnBookResult.setMessage(JsonUtils.getMessageFromJson(jsonObject));

                            if(jsonObject.getString("success").equals("true")){

                                returnBookResult.setReturnResult(true);
                                books.get(iTemp).setBookDate(jsonObject.getString("请在规定时间内归还"));

                            }else{

                                returnBookResult.setReturnResult(false);

                            }

                            bookResultList.add(returnBookResult);

                            if (bookResultList.size() == books.size()) {
                                listener.onSuccess(bookResultList);
                            }

                        }else {

                            if (!BookActionImpl.super.isLogin(jsonObject))
                                listener.onFailure("登录", "账号验证过期，请重新登录..");

                            returnBookResult.setBook(books.get(iTemp));
                            returnBookResult.setMessage(jsonObject.getString("message"));

                            if (isReload(data)) {

                                if (VersionSetting.IS_SHENZHEN_LIB) {//南山图书馆没有归还日期
                                    books.get(iTemp).setBookDate("--");
                                } else {
                                    books.get(iTemp).setBookDate(jsonObject.getString("returnDate"));
                                }
                                returnBookResult.setReturnResult(true);

                            } else {
                                returnBookResult.setReturnResult(false);
                            }
                            bookResultList.add(returnBookResult);

                            if (bookResultList.size() == books.size()) {
                                listener.onSuccess(bookResultList);
                            }

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(String errorEvent, String message) {
                    returnBookResult.setBook(books.get(iTemp));
                    returnBookResult.setMessage("网络不稳定请重试");
                    returnBookResult.setReturnResult(false);
                    bookResultList.add(returnBookResult);
                    if (bookResultList.size()==books.size()){
                        listener.onSuccess(bookResultList);
                    }
                }
            });
        }
    }



    public void showBookInfo(final String readerId, final String barcode, final ActionCallbackListener<Map<String, Object>> listener) {

        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                return mBookApi.showBookInfo(readerId, barcode);

            }

            @Override
            protected void onPostExecute(String result) {
                if (listener != null && result != null) {
                    Log.d("json",result);
//                    try {
//                        listener.onSuccess(result);
//
//                    } catch (Exception e) {
//
//                        listener.onFailure("ERROR", "连接服务器失败");
//                    }
                }
                else{

                    listener.onFailure("ERROR", "连接服务器失败");
                }
            }
        }.execute();
    }

    @Override
    public void bookHolding(final String bookrecno ,final ActionCallbackListener<List<BookHolding>> listener) {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                return mBookApi.bookHolding(bookrecno);

            }

            @Override
            protected void onPostExecute(String result) {
                if (listener != null && result != null) {
                    Log.d("json", result);
                    try {
                        JSONObject jsonObject =new JSONObject(result);
                        JSONArray jsonArray ;
                        List<BookHolding> bookHoldingList =new ArrayList<BookHolding>();
                        if ((jsonArray=jsonObject.getJSONArray("holdingList")).length()!=0){

                            for (int i=0; i<jsonArray.length();i++){
                                BookHolding bookHolding =new BookHolding();


                                if(VersionSetting.IS_CONNECT_INTERLIB3){

                                    String callno =jsonArray.getJSONObject(i).getString("callno");
                                    if (!callno.equals("null")&&callno!=null)
                                        bookHolding.setRecno(callno);

                                    String state =jsonArray.getJSONObject(i).getString("state");
                                    if (!state.equals("null")&&state!=null) {
                                        switch (state){
                                            case "1":  bookHolding.setState("编目");
                                                break;
                                            case "2":   bookHolding.setState("在馆");
                                                break;
                                            case "3":   bookHolding.setState("借出");
                                                break;
                                            case "4":   bookHolding.setState("丢失");
                                                break;
                                            case "5":   bookHolding.setState("剔除");
                                                break;
                                            case "6":   bookHolding.setState("交换");
                                                break;
                                            case "7":   bookHolding.setState("赠送");
                                                break;
                                            case "8":   bookHolding.setState("装订");
                                                break;
                                            case "9":   bookHolding.setState("锁定");
                                                break;
                                            case "10":   bookHolding.setState("预借");
                                                break;
                                            case "12":   bookHolding.setState("清点");
                                                break;
                                            case "13":   bookHolding.setState("闭架");
                                                break;
                                            case "14":   bookHolding.setState("修补");
                                                break;
                                            case "15":   bookHolding.setState("查找中");
                                                break;
                                            case "16":   bookHolding.setState("重复锁定");
                                                break;
                                            case "17":   bookHolding.setState("预览");
                                                break;
                                            default:
                                                break;
                                        }
                                    }

                                    String curlocal =jsonArray.getJSONObject(i).getString("curlibname");
                                    if (!curlocal.equals("null")&&curlocal!=null)
                                        bookHolding.setLib(curlocal);

                                    String barcode =jsonArray.getJSONObject(i).getString("barcode");
                                    if (!barcode.equals("null")&&barcode!=null)
                                        bookHolding.setShelfno(barcode);

                                    bookHoldingList.add(bookHolding);

                                }else{

                                    String callno =jsonArray.getJSONObject(i).getString("callno");
                                    if (!callno.equals("null")&&callno!=null)
                                        bookHolding.setRecno(callno);

                                    JSONObject localMap ;
                                    String curlocal;
                                    if ((localMap=jsonObject.getJSONObject("localMap"))!=null&&(curlocal =jsonArray.getJSONObject(i).getString("curlocal"))!=null){
                                        if (!localMap.getString(curlocal).equals("null")){
                                            bookHolding.setLib(localMap.getString(curlocal));
                                        }
                                    }

                                    String shelfno =jsonArray.getJSONObject(i).getString("shelfno");
                                    if (!shelfno.equals("null")&&shelfno!=null)
                                        bookHolding.setShelfno(shelfno);

                                    JSONObject holdStateMap ;
                                    String state;
                                    if ((holdStateMap = jsonObject.getJSONObject("holdStateMap"))!=null&&(state =jsonArray.getJSONObject(i).getString("state"))!=null){
                                        if (!holdStateMap.getJSONObject(state).getString("stateName").equals("null")){
                                            bookHolding.setState(holdStateMap.getJSONObject(state).getString("stateName"));
                                        }
                                    }
                                    bookHoldingList.add(bookHolding);
                                }

                                listener.onSuccess(bookHoldingList);
                            }
                        }else{
                            BookHolding bookHolding =new BookHolding();
                            bookHolding.setRecno("暂无馆藏");
                            bookHoldingList.add(bookHolding);
                            listener.onSuccess(bookHoldingList);
                        }
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


    //书本顺架
    @Override
    public void getBookData(final String barcode, ThreadPoolManager poolManager,final Handler handler) {

        mBookApi.getBookData(barcode, poolManager, new ThreadPoolTaskBitmap.CallBack() {
            @Override
            public void onReady(String result) {
                Log.d("json",result);
                if (result.length() != 0 && result != null) {
                    try {
                        //{"map":{"title":"中国中西医结合肾病杂","pubdate":"","isbn":"","barcode":"1234567890","callno":"I448.4/5","publisher":""}
                        // ,"message":"查询数据成功!","success":true,"title":"操作提示","url":""}
                        JSONObject jsonObject = new JSONObject(result);

                        if (jsonObject.getBoolean("success")) {

                            CheckBook checkBook = new CheckBook();
                            checkBook.setBookBarcode(barcode);

                            JSONObject map = jsonObject.getJSONObject("map");
                            checkBook.setBookTitle(map.getString("title"));
                            checkBook.setReferenceNum(map.getString("callno"));
                            checkBook.setPubdate(map.getString("pubdate"));
                            checkBook.setPublisher(map.getString("publisher"));
                            checkBook.setIsbn(map.getString("isbn"));
                            checkBook.setShelfno(map.getString("shelfno"));

                            shelvesResult = new ShelvesResult();
                            shelvesResult.setCheckResult(true);
                            shelvesResult.setCheckBook(checkBook);

                            Message msg = Message.obtain();
                            Bundle b = new Bundle();
                            b.putSerializable("shelvesResult", shelvesResult);
                            msg.setData(b);
                            msg.what =InventoryActivity.GET_BOOKDATA_SUCCESS;
                            handler.sendMessage(msg);
                        } else {
                            Message msg = Message.obtain();
                            Bundle b = new Bundle();
                            b.putString("message", "无此条码：\n " + barcode);
                            b.putString("barcode", barcode);
                            msg.setData(b);
                            msg.what = InventoryActivity.GET_BOOKDATA_FAIL;
                            handler.sendMessage(msg);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Message msg = Message.obtain();
                    Bundle b = new Bundle();
                    b.putString("barcode", barcode);
                    b.putString("message", "连接服务器失败！");
                    msg.what = InventoryActivity.CONNECT_FAIL1;
                    msg.setData(b);
                    handler.sendMessage(msg);
                }
            }
        });
    }

    //书本清点
    @Override
    public void checkBookSingle(final String barcode, final String countSignFlagNum,ThreadPoolManager poolManager,final Handler handler) {

        shelvesResult= null;
        mBookApi.checkBookSingle(barcode, countSignFlagNum, poolManager, new ThreadPoolTaskBitmap.CallBack() {
            @Override
            public void onReady(String result) {
                if (result.length() != 0 && result != null) {
                    Log.d("test",result);
                    try {
                        JSONObject jsonObject = new JSONObject(result);

                        //判断是否还有token
                        if (BookActionImpl.super.isNoToken(jsonObject)) {
                            Message message = handler.obtainMessage();
                            message.obj = "账号验证过期，请重新登录..";
                            message.what = InventoryActivity.TOKEN_USERLESS;
                            handler.sendMessage(message);
                        }


                        if (VersionSetting.IS_CONNECT_INTERLIB3 ? jsonObject.getString("success").equals("true") : jsonObject.getBoolean("success")) {

                            CheckBook checkBook = new CheckBook();
                            checkBook.setBookBarcode(barcode);

                            JSONObject biblio = null;

                            if(VersionSetting.IS_CONNECT_INTERLIB3){
                                biblio = jsonObject.getJSONObject("biblio");
                                checkBook.setShelfno(jsonObject.getJSONObject("holding").getString("shelfno").toString());
                                checkBook.setReferenceNum(jsonObject.getJSONObject("holding").getString("callno").toString());

                            }else {
                                biblio = jsonObject.getJSONObject("map").getJSONObject("biblio");
                                checkBook.setShelfno(jsonObject.getJSONObject("map").getJSONObject("holding").getString("shelfno").toString());
                                checkBook.setReferenceNum(jsonObject.getJSONObject("map").getJSONObject("holding").getString("callno").toString());
                            }

                            checkBook.setBookTitle(biblio.getString("title"));
                            checkBook.setIsbn(biblio.getString("isbn"));
                            checkBook.setPubdate(biblio.getString("pubdate"));
                            checkBook.setPublisher(biblio.getString("publisher"));

                            shelvesResult = new ShelvesResult();
                            shelvesResult.setCheckResult(true);
                            shelvesResult.setCheckBook(checkBook);

                            Message msg = Message.obtain();
                            Bundle b = new Bundle();
                            b.putSerializable("shelvesResult", shelvesResult);
                            msg.setData(b);

                            msg.what =InventoryActivity.GET_BOOKDATA_SUCCESS;
                            handler.sendMessage(msg);
                        } else {
                            //{"map":{},"message":"清点失败！无此条码:6","success":false,"title":"操作提示","url":""}
                            Message msg = Message.obtain();
                            Bundle b = new Bundle();
                            b.putString("barcode", barcode);
                            b.putString("message", "无此条码： " + barcode);
                            msg.setData(b);
                            msg.what = InventoryActivity.GET_BOOKDATA_FAIL;
                            handler.sendMessage(msg);
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                        Message msg = Message.obtain();
                        Bundle b = new Bundle();
                        b.putString("barcode", barcode);
                        b.putString("message", "解析json失败");
                        msg.setData(b);
                        //msg.what = 7;
                        msg.what = InventoryActivity.CONNECT_FAIL2;
                        handler.sendMessage(msg);

                    }
                } else {
                    Message msg = Message.obtain();
                    Bundle b = new Bundle();
                    b.putString("barcode", barcode);
                    b.putString("message", "连接服务器失败");
                    msg.what = InventoryActivity.CONNECT_FAIL1;
                    msg.setData(b);
                    handler.sendMessage(msg);
                }
            }
        });

    }

    //书本上架
    @Override
    public void shelvesBook(final ShelvesResult shelvesResult, String shelvesNum, ThreadPoolManager poolManager, final Handler handler) {
        mBookApi.shelvesBook(shelvesResult.getCheckBook().getBookBarcode(),
                shelvesNum, poolManager, new ThreadPoolTaskBitmap.CallBack() {
                    @Override
                    public void onReady(String result) {
                        if (result.length() != 0 && result != null) {

                            Log.d("test","盘点："+result);
                            try {
                                JSONObject jsonObject = new JSONObject(result);
                                //判断是否还有token
                                if (BookActionImpl.super.isNoToken(jsonObject)) {
                                    Message message = handler.obtainMessage();
                                    message.obj = "账号验证过期，请重新登录..";
                                    message.what = InventoryActivity.TOKEN_USERLESS;
                                    handler.sendMessage(message);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            Message msg = Message.obtain();
                            Bundle b = new Bundle();
                            b.putString("message", result);
                            b.putSerializable("shelvesResult", shelvesResult);
                            msg.setData(b);
                            msg.what = InventoryActivity.SHELVE_BOOK_SUCCESS;
                            handler.sendMessage(msg);


                        } else {
                            Message msg = Message.obtain();
                            Bundle b = new Bundle();
                            b.putString("toastMsg", "连接服务器失败");
                            b.putSerializable("shelvesResult", shelvesResult);
                            msg.setData(b);
                            msg.what = InventoryActivity.TOAST_MSG;
                            handler.sendMessage(msg);
                        }

                    }
                });
    }


    //直接上架
    @Override
    public void shelveBookFast(final String barcode, final String shelvesNum, ThreadPoolManager poolManager, final Handler handler) {
        mBookApi.shelvesBook(barcode, shelvesNum, poolManager, new ThreadPoolTaskBitmap.CallBack() {
            @Override
            public void onReady(String result) {
                if (result.length() != 0 && result != null) {

                    Log.d("test","盘点快的："+result);

                    CheckBook checkBook = new CheckBook();
                    checkBook.setBookBarcode(barcode);
                    JSONObject json = null;

                    try {
                        json = new JSONObject(result);

                        //判断是否还有token
                        if (BookActionImpl.super.isNoToken(json)) {
                            Message message = handler.obtainMessage();
                            message.obj = "账号验证过期，请重新登录..";
                            message.what = InventoryActivity.TOKEN_USERLESS;
                            handler.sendMessage(message);
                        }

                        if(VersionSetting.IS_CONNECT_INTERLIB3) {

                            checkBook.setReferenceNum("暂无");//上架得到的信息没有索引号

                            if (json.getString("success").equals("true")) {

                                JSONArray jsonArray = json.getJSONArray("datalist");
                                JSONObject map = jsonArray.getJSONObject(0);

                                checkBook.setBookTitle(map.getString("TITLE"));
                                checkBook.setShelfno(shelvesNum);

                            }

                        }else{
                            if (json.getString("message").contains("成功")) {

                                JSONObject map = json.getJSONObject("book");
                                checkBook.setBookTitle(map.getString("title"));
                                checkBook.setReferenceNum(map.getString("callno"));
                                checkBook.setShelfno(shelvesNum);
                            }
                        }
                    } catch (JSONException e) {

                        e.printStackTrace();
                        Message msg = Message.obtain();
                        Bundle b = new Bundle();
                        b.putString("barcode", barcode);
                        b.putString("message", "解析json失败");
                        msg.setData(b);
                        msg.what = InventoryActivity.CONNECT_FAIL2;
                        handler.sendMessage(msg);
                    }


                    shelvesResult = new ShelvesResult();
                    shelvesResult.setCheckResult(true);
                    shelvesResult.setCheckBook(checkBook);

                    Message msg = Message.obtain();
                    Bundle b = new Bundle();
                    b.putString("message", result);
                    b.putSerializable("shelvesResult", shelvesResult);
                    msg.setData(b);
                    msg.what = InventoryActivity.SHELVE_BOOK_SUCCESS;
                    handler.sendMessage(msg);


                } else {
                    Message msg = Message.obtain();
                    Bundle b = new Bundle();
                    b.putString("toastMsg", "连接服务器失败");
                    b.putSerializable("shelvesResult", shelvesResult);
                    msg.setData(b);
                    msg.what = InventoryActivity.TOAST_MSG;
                    handler.sendMessage(msg);
                }

            }
        });
    }

    public void addBookLog(final String barcode,ThreadPoolManager poolManager, final Handler handler){
        mBookApi.addBookLog(barcode, poolManager, new ThreadPoolTaskBitmap.CallBack() {
            @Override
            public void onReady(String result) {
                if (result.length() != 0 && result != null) {
                    //{"map":{},"message":"新增数据成功!","success":true,"title":"操作提示","url":""}
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        Message msg = Message.obtain();
                        msg.what = InventoryActivity.SHELVE_BOOK_SUCCESS;
                        Bundle b = new Bundle();
                        if (jsonObject.getBoolean("success")) {

                            b.putBoolean("result", true);
                            b.putString("message", "");
                        } else {

                            b.putBoolean("result", false);
                            b.putString("message", "插入日志失败");

                        }
                        msg.setData(b);
                        handler.sendMessage(msg);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                } else {
                    Message msg = Message.obtain();
                    msg.what = 4;
                    Bundle b = new Bundle();
                    b.putBoolean("result", false);
                    b.putString("message", "连接服务器失败");
                    handler.sendMessage(msg);
                }
            }
        });
    }


    /**
     * 判断续借是否成功
     * @param result
     * @return
     */
    private boolean isReload(String result){
        try {
            JSONObject json = new JSONObject(result);
            if (json.getString("success").equals("true")) {

                return true;
            } else {
                return false;
            }
        } catch (JSONException e) {
            return false;
        }
    }

    /**
     * 判断借书结果
     * @param barcode
     * @param json
     */
    public Map<String, Object> judgeLoanResult(String barcode, JSONObject json,int isChangeSafe){

        Map<String, Object> data =new HashMap<>();

        try{
            data.put("isChangeSafe",isChangeSafe);
            data.put("barcode", barcode);
            data.put("isCheck",false);
            if (json.getString("success").equals("true")){

                //排除馆际借还，示例json:{"success":"true","barcode":"0359307","message":"该读者不能在馆际借还该类图书。请去: [YNUL]续借! ]] "}
                if(json.getString("message").contains("不能在馆际借还")){
                    data.put("LoanResult",false);
                    data.put("message",json.getString("message"));
                }

                //{"success":"true","barcode":"04400696200829","message":"读者已经续借图书[04400696200829]2次, 不能再续借! ]] 条码[04400696200829]续借后的应还时间小于当前应还时间，不需要续借! ]] "}
                else if (json.getString("message").contains("不能再续借")){
                    data.put("LoanResult",false);
                    data.put("title",json.getString("title"));
                    data.put("returnDate", json.getString("returnDate"));
                    data.put("message","条形码:"+barcode+" 的图书已借，暂不能续借");
                }
                else {

                    data.put("LoanResult",true);
                    try{
                        data.put("title",json.getString("title"));
                        String[] sourceStrArray = json.getString("returnDate").split(",");
                        data.put("returnDate", sourceStrArray[0]);

                    }catch (JSONException e){
                        data.put("title","");
                        data.put("returnDate", "");
                    }
                    try{
                        data.put("message",json.getString("message"));
                    }
                    catch (NullPointerException e){
                        data.put("message","借书成功");
                    }
                }
            }
            else{
                data.put("LoanResult", false);
                data.put("title","操作失败");
                data.put("barcode", barcode);
                data.put("returnDate","--");
                data.put("message",json.getString("message"));


            }

            return data;
        }catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * 判断还书结果
     * @param barcode
     * @param json
     */
    public Map<String, Object> judgeReturnResult(String barcode, JSONObject json){

        Map<String, Object> data =new HashMap<>();

        try{
            data.put("barcode", barcode);
            data.put("isCheck",false);
            data.put("isChangeSafe",0);
            if (!json.getString("message").equals(""))
                data.put("message",json.getString("message"));
            else{
                data.put("message","条形码["+barcode+"]为空，不能还回!");
            }
            if (json.getString("success").equals("true")){

                //{"success":"true","message":"图书 Java 2编程起步[04400696200827]已于:20160718归还 应还日期：2016-09-16"}
                data.put("LoanResult",true);
                data.put("title",json.getString("title"));
                if (json.getString("returnDate").equals("")){
                    data.put("returnDate","本书在馆");
                }else{
                    data.put("returnDate",json.getString("returnDate"));
                }

            }
            else{
                //{"success":"false","message":"条码[0359307]无对应外借记录,不能进行还回！"}
                data.put("LoanResult", false);

            }

        }catch (JSONException e) {
            data.put("title","");
            data.put("returnDate", "");
            e.printStackTrace();
        }
        return data;
    }


    /**
     * 判断借书结果(南山图书馆借书结果处理)
     * @param barcode
     * @param json
     */
    public Map<String, Object> judgeLoanResultForNanShan(String barcode, JSONObject json,int isChangeSafe){

        Map<String, Object> data =new HashMap<>();

        try{
            data.put("isChangeSafe",isChangeSafe);
            data.put("barcode", barcode);
            data.put("isCheck",false);

            //借书成功
            if (json.getString("success").equals("true")) {

                JSONObject jsonObject = json.getJSONObject("map");
                data.put("title",jsonObject.getString("title"));
                data.put("returnDate",jsonObject.getString("DateDue"));
                data.put("LoanResult",true);
                data.put("message",json.getString("message"));

            }else{

                if(json.getString("message").contains("借出")){

                    JSONObject jsonObject = json.getJSONObject("map");
                    data.put("title",jsonObject.getString("title"));
                }
                data.put("LoanResult",false);
                data.put("message",json.getString("message"));

            }

        }catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        Log.d("test1","借书返回数据集："+data.toString());
        return data;
    }

    /**
     * 判断还书结果(南山图书馆还书结果处理)
     * @param barcode
     * @param json
     */
    public Map<String, Object> judgeReturnResultForNanShan(String barcode, JSONObject json){

        Map<String, Object> data =new HashMap<>();

        try{
            data.put("barcode", barcode);
            data.put("isCheck",false);
            data.put("isChangeSafe",0);
            if (!json.getString("message").equals(""))
                data.put("message",json.getString("message"));
            else{
                data.put("message","条形码["+barcode+"]为空，不能还回!");
            }
            if (json.getString("success").equals("true")){

                //{"success":"true","message":"图书 Java 2编程起步[04400696200827]已于:20160718归还 应还日期：2016-09-16"}
                data.put("LoanResult",true);

                JSONObject jsonObject = json.getJSONObject("map");
                data.put("title",jsonObject.getString("title"));

                //还书没有应还日期的返回json值
                data.put("returnDate","--");

            }
            else{
                //{"success":"false","message":"条码[0359307]无对应外借记录,不能进行还回！"}
                data.put("LoanResult", false);

            }

        }catch (JSONException e) {
            data.put("title","");
            data.put("returnDate", "");
            e.printStackTrace();
        }
        return data;
    }

    /**
     * 判断借书结果
     * @param barcode
     * @param json
     */
    public Map<String, Object> judgeLoanResultForInterlib(String barcode, JSONObject json,int isChangeSafe){

        Map<String, Object> data =new HashMap<>();

        try{
            data.put("isChangeSafe",isChangeSafe);
            data.put("barcode", barcode);
            data.put("isCheck",false);

            try {
                JSONObject jsonObject = json.getJSONObject("newloanbook");
                data.put("title",jsonObject.getString("title"));
            } catch (JSONException e) {
                data.put("title","操作失败");
            }

            if (json.getString("success").equals("true")){

                data.put("returnDate","请在规定日期内归还");
                data.put("LoanResult",true);


            }else{

                data.put("LoanResult", false);
                data.put("returnDate","--");
                data.put("message", JsonUtils.getMessageFromJson(json));
            }

            return data;
        }catch (JSONException e) {
            data.put("LoanResult", false);
            data.put("title","操作失败");
            data.put("returnDate","--");
            data.put("message", "没有此条码！");
            return data;
        }

    }

    /**
     * 判断还书结果
     * @param barcode
     * @param json
     */
    public Map<String, Object> judgeReturnResultForInterlib(String barcode, JSONObject json){

        Map<String, Object> data =new HashMap<>();

        try{
            data.put("barcode", barcode);
            data.put("isCheck",false);
            data.put("isChangeSafe",0);
            data.put("message",JsonUtils.getMessageFromJson(json));

            try {
                JSONObject jsonObject = json.getJSONObject("newreturnbook");
                data.put("title",jsonObject.getString("title"));
            } catch (JSONException e) {
                data.put("title","操作失败");
            }

            if (json.getString("success").equals("true")){

                data.put("LoanResult",true);
                data.put("returnDate","于今日已归还");
            }
            else{

                data.put("LoanResult", false);
                data.put("returnDate","--");
            }

        }catch (JSONException e) {
            data.put("title","操作失败");
            data.put("returnDate", "");
            data.put("message","条形码["+barcode+"]为空，不能还回!");
            e.printStackTrace();
        }
        return data;
    }

    /**
     * 设置计时器
     */
    private void setTimer() {

        //重新设置计时器
        timer = new Timer(true);
        task = new TimerTask(){
            public void run() {
                Message message = new Message();
                message.what = 3;
                handler.sendMessage(message);
            }
        };
        //延时0s后执行，1s执行一次
        timer.schedule(task, 0, 1000);
    }

    /**
     * 清空计时器
     */
    private void clearTimer(){
        //清空计时器
        if (timer!=null&task!=null){
            timer.cancel();
            task.cancel();
            timer=null;
            task=null;
        }
        //重新设置倒计时3秒
        time=SECONDS;
    }
    /**
     * 由于加载图书信息通过子线程加载，这里需要用handler返回到主线程
     * 不能使用回调
     */
    public Handler handler = new Handler(){
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){

                //计时器结束时回调
                case 3:
                    Log.d("XX",time+" TIME");
                    //如果倒计时结束
                    if (time==0){
                        flag=false;
                        clearTimer();
                        return;
                    }
                    time--;
                    break;

            }
        }
    };



}
