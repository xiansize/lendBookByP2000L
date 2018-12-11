package com.tc.nfc.api.implement;

import android.util.Log;


import com.tc.api.VersionSetting;
import com.tc.nfc.api.interfaces.BookApi;
import com.tc.nfc.api.utils.NetworkHelp;
import com.tc.nfc.app.utils.nfcV.NfcVUtil;
import com.tc.nfc.core.task.ThreadPoolManager;
import com.tc.nfc.core.task.ThreadPoolTaskBitmap;
import com.tc.nfc.util.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by tangjiarao on 16/6/23.
 */
public class BookApiImpl implements BookApi {

    private final String TAG = "BookApiImpl";


    @Override
    public String P2000LGetBarcode() {
        return NfcVUtil.p2000LGetBarcode();
    }

    @Override
    public String isBookCanLoadn(String readerId,String barcode) {

        if(VersionSetting.IS_SHENZHEN_LIB){
            Map<String,String> map = new HashMap<String, String>();
            map.put("barcode",barcode);

            return NetworkHelp.sendDataByPost(map,"utf-8",VersionSetting.baseUrl+"/book/lookup");

        }else if(VersionSetting.IS_CONNECT_INTERLIB3){

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("success","true");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject.toString();//直接连接到interlib3开放接口没有这方法

        }else{
            JSONObject json = new JSONObject();
            try {
                String rdid;
                json.put("action", "checkIsCanLoan");
                json.put("barcode", barcode);
                if (readerId!=null){
                    rdid=Constant.uid;
                    json.put("rdid", readerId);
                }else{
                    rdid = Constant.reader.getString("rdid");
                    json.put("rdid", rdid);
                }
                String[] keycode = Constant.getKeyCode(rdid);
                json.put("keyCode", keycode[0]);
                json.put("time", keycode[1]);
                return NetworkHelp.doJsonPost(VersionSetting.baseUrl, json.toString());

            }catch (JSONException e) {

                return null;
            } catch (Exception e) {

                return null;
            }
        }


    }

    @Override
    public String loanBook(String readerId,String barcode) {

        if(VersionSetting.IS_SHENZHEN_LIB){
            Map<String,String> map = new HashMap<String, String>();
            map.put("cardId",readerId);
            map.put("barcode",barcode);
            return NetworkHelp.sendDataByPost(map,"utf-8",VersionSetting.baseUrl+"/book/checkout");

            //直接连接到interlib3开放接口
        }else if(VersionSetting.IS_CONNECT_INTERLIB3){
            Map<String,String> map = new HashMap<String,String>();
            map.put("token",Constant.TOKEN);
            map.put("rdid",readerId);
            map.put("barcode",barcode);
            //1,借出，附件随书借出,如果有附件，附件一起借出; 2 借出，如果有附件，返回附件信息，不对该本图书进行借出，等第二次请求该标记为1时，再借出; 3,借出，附件不随书借出，直接借阅该本图书
            map.put("attachmentloanflag","1");
            if(Constant.uid != null){
                VersionSetting.OPERATOR_USER = Constant.uid;
            }
            map.put("opuser",VersionSetting.OPERATOR_USER);
            return NetworkHelp.sendDataByPost(map, "utf-8", VersionSetting.URL_INTERLIB3_OPEN+"/service/barcode/loanbook");


        }else{
            JSONObject json = new JSONObject();
            try {
                String rdid ;
                json.put("action", "lendBook");
                json.put("barcode", barcode);
                if (readerId!=null){
                    rdid=Constant.uid;
                    json.put("rdid", readerId);
                }else{
                    rdid = Constant.reader.getString("rdid");
                    json.put("rdid", rdid);
                }
                String[] keycode = Constant.getKeyCode(rdid);
                json.put("keyCode", keycode[0]);
                json.put("time", keycode[1]);
                return NetworkHelp.doJsonPost(VersionSetting.baseUrl, json.toString());
            }catch (JSONException e) {
                Log.d(TAG,"JSONException");
                return null;
            } catch (Exception e) {
                Log.d(TAG,"IOException");
                return null;
            }
        }
    }


    @Override
    public String returnBook(String barcode) {

        if(VersionSetting.IS_SHENZHEN_LIB){
            Map<String,String> map = new HashMap<String, String>();
            map.put("barcode",barcode);
            return NetworkHelp.sendDataByPost(map,"utf-8",VersionSetting.baseUrl+"/book/checkin");

        }else if(VersionSetting.IS_CONNECT_INTERLIB3){

            Map<String,String> map = new HashMap<String,String>();
            map.put("token",Constant.TOKEN);
            map.put("rdid",Constant.readerId);
            map.put("barcode",barcode);
            if(Constant.uid != null){
                VersionSetting.OPERATOR_USER = Constant.uid;
            }
            map.put("opuser",VersionSetting.OPERATOR_USER);
            return NetworkHelp.sendDataByPost(map, "utf-8", VersionSetting.URL_INTERLIB3_OPEN+"/service/barcode/returnbook");



        }else{
            JSONObject json = new JSONObject();
            try {
                String rdid = Constant.uid;
                json.put("action", "returnBook");
                json.put("rdid", rdid);
                json.put("barcode", barcode);
                String[] keycode = Constant.getKeyCode(rdid);
                json.put("keyCode", keycode[0]);
                json.put("time", keycode[1]);
                NetworkHelp.setAction(true);
                return NetworkHelp.doJsonPost(VersionSetting.baseUrl, json.toString());
            }catch (JSONException e) {

                Log.d(TAG,"JSONException");
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG,"IOException");
                return null;
            }
        }
    }

    @Override
    public String loanBookData(String readerId) {

        if(VersionSetting.IS_SHENZHEN_LIB){

            JSONObject jsonAll = null;
            //先是通过barcode号去查到读者借的书的书名
            Map<String,String> map = new HashMap<String, String>();
            map.put("cardId",readerId);
            String barcodeData = NetworkHelp.sendDataByPost(map,"UTF-8",VersionSetting.baseUrl+"/reader/seach");
            try {
                JSONObject jsonObject = new JSONObject(barcodeData);
                jsonObject = jsonObject.getJSONObject("map");
                int bookLength = Integer.parseInt(jsonObject.getString("LoanedItemCountValue"));//读者接的书的数量
                JSONArray bookitems = jsonObject.getJSONArray("bookitems");//得到读者借的书的barcode json数组

                //将得到书名和barcode号放到json中
                jsonAll = new JSONObject();
                //json数组
                JSONArray jsonArray = new JSONArray();
                //json
                JSONObject jsonBook = new JSONObject();

                if(bookLength == 0){
                    jsonAll.put("success","false");//为false没有数据
                    return jsonAll.toString();
                }


                for(int i = 0;i<bookLength;i++) {

                    jsonBook.put("barcode",bookitems.get(i));
                    //将得到的barcode号去查询书本的title
                    map.put("barcode",bookitems.get(i).toString());
                    String returnData = NetworkHelp.sendDataByPost(map, "UTF-8", VersionSetting.baseUrl + "/book/lookup");
                    JSONObject json = new JSONObject(returnData);
                    json = json.getJSONObject("map");

                    jsonBook.put("title",json.getString("title"));

                    jsonArray.put(i,jsonBook);

                }

                jsonAll.put("success","true");
                jsonAll.put("book",jsonArray);

                return jsonAll.toString();//重新组成一个json

            } catch (JSONException e) {

                Log.d("test",e.getMessage());

            }

            return "";

        }else if(VersionSetting.IS_CONNECT_INTERLIB3){

            Map<String,String> map = new HashMap<String,String>();
            map.put("token",Constant.TOKEN);
            map.put("rdid",readerId);
            return NetworkHelp.sendDataByPost(map, "utf-8", VersionSetting.URL_INTERLIB3_OPEN+"/service/barcode/currentloan");


        }else{


            JSONObject json = new JSONObject();
            try {
                String rdid;
                //json.put("action", "myloan");
                Map<String, String> params =new HashMap<>();
                if (readerId!=null){
                    rdid =readerId;
//                json.put("rdid", readerId);
//                rdid=Constant.uid;
//                params.put("rdid",readerId);
                }else{
                    rdid = Constant.reader.getString("rdid");
//                json.put("rdid", rdid);
//                params.put("rdid",rdid);
                }
//            String[] keycode = Constant.getKeyCode(rdid);
//            json.put("keyCode", keycode[0]);
//            json.put("time", keycode[1]);
//            return NetworkHelp.doJsonPost(VersionSetting.baseUrl, json.toString());
                return NetworkHelp.getDataByGet("utf-8", VersionSetting.baseUrl3 +"/webservice/loanWebservice/getCurrentLoanList?rdid="+rdid+"&password=&doPage=false&pageSize=20&toPage=1");
            }
            catch (JSONException e) {

                Log.d(TAG, "JSONException");
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG,"IOException");
                return null;
            }

        }

    }

    @Override
    public String reloanBook(String readerId,String barcode) {

        if(VersionSetting.IS_SHENZHEN_LIB){
            Map<String,String> map = new HashMap<String, String>();
            map.put("cardId",readerId);
            map.put("barcode",barcode);
            return NetworkHelp.sendDataByPost(map,"utf-8",VersionSetting.baseUrl+"/book/renew");

        }else if(VersionSetting.IS_CONNECT_INTERLIB3){

            Map<String,String> map = new HashMap<String,String>();
            map.put("token",Constant.TOKEN);
            map.put("rdid",readerId);
            map.put("barcode",barcode);
            if(Constant.uid != null){
                VersionSetting.OPERATOR_USER = Constant.uid;
            }
            map.put("opuser",VersionSetting.OPERATOR_USER);
            return NetworkHelp.sendDataByPost(map, "utf-8", VersionSetting.URL_INTERLIB3_OPEN+"/service/barcode/renewbook");


        }else {
            JSONObject json = new JSONObject();
            try {
                String rdid ;
                json.put("action", "renewBook");
                json.put("barcode", barcode);

                if (readerId!=null){
                    rdid=Constant.uid;
                    json.put("rdid", readerId);

                }else{
                    rdid = Constant.reader.getString("rdid");
                    json.put("rdid", rdid);
                }
                String[] keycode = Constant.getKeyCode(rdid);
                json.put("keyCode", keycode[0]);
                json.put("time", keycode[1]);
                return NetworkHelp.doJsonPost(VersionSetting.baseUrl, json.toString());
            } catch (JSONException e) {
                Log.d(TAG,"JSONException");
                return null;
            } catch (Exception e) {

                Log.d(TAG,"IOException");
                return null;
            }
        }


    }

    //书本清点
    public void checkBookSingle(String barcode, String countSignFlagNum,ThreadPoolManager poolManager,ThreadPoolTaskBitmap.CallBack callBack){
        if(VersionSetting.IS_CONNECT_INTERLIB3) {
            Map<String, String> map = new HashMap<>();
            map.put("token",Constant.TOKEN);
            map.put("barcode", barcode);
            map.put("countsignflag", countSignFlagNum);
            map.put("opuser", Constant.uid);
            poolManager.addAsyncTask(new ThreadPoolTaskBitmap(VersionSetting.URL_INTERLIB3_OPEN + "/service/hold/assetcountreturnmeta", callBack, map));

        }else{
            Map<String, String> map = new HashMap<>();
            map.put("barcode", barcode);
            map.put("countSignFlag", countSignFlagNum);
            poolManager.addAsyncTask(new ThreadPoolTaskBitmap(VersionSetting.baseUrl2 + "/asset/assetRRC/assetCountReturnMeta", callBack, map));

        }
    }


    //书本上架
    @Override
    public void shelvesBook(String barcode, String shelvesNum, ThreadPoolManager poolManager, ThreadPoolTaskBitmap.CallBack callBack) {
        if(VersionSetting.IS_CONNECT_INTERLIB3) {
            Map<String, String> map = new HashMap<>();
            map.put("token",Constant.TOKEN);
            map.put("barcode",barcode);
            map.put("selfno",shelvesNum);
            map.put("opuser",Constant.uid);
            poolManager.addAsyncTask(new ThreadPoolTaskBitmap(VersionSetting.URL_INTERLIB3_OPEN + "/service/hold/holdselfnomanage", callBack, map));

        }else{
            Map<String, String> map = new HashMap<>();
            map.put("action", "bookShelf");
            map.put("barcode", barcode);
            map.put("selfno", shelvesNum);
            poolManager.addAsyncTask(new ThreadPoolTaskBitmap(VersionSetting.baseUrl2 + "/asset/holdShelfno/holdSelfnoManage", callBack, map));
        }
    }


    //获取书本信息
    @Override
    public void getBookData(String barcode, ThreadPoolManager poolManager, ThreadPoolTaskBitmap.CallBack callBack) {
        if(VersionSetting.IS_CONNECT_INTERLIB3){
            Map<String, String> map = new HashMap<>();
            map.put("token",Constant.TOKEN);
            map.put("barcode", barcode);
            poolManager.addAsyncTask(new ThreadPoolTaskBitmap(VersionSetting.URL_INTERLIB3_OPEN + "/service/purchase/barcodeCollection", callBack, map));

        }else {

            Map<String, String> map = new HashMap<>();
            map.put("barcode", barcode);
            poolManager.addAsyncTask(new ThreadPoolTaskBitmap(VersionSetting.baseUrl2 + "/service/purchase/barcodeCollection", callBack, map));
        }
    }

    @Override
    public void addBookLog(String barcode, ThreadPoolManager poolManager, ThreadPoolTaskBitmap.CallBack callBack) {
        Map<String,String> map =new HashMap<>();
        map.put("barcode", barcode);
        poolManager.addAsyncTask(new ThreadPoolTaskBitmap(VersionSetting.baseUrl2 + "/service/purchase/recordUserLog", callBack, map));
    }

    @Override
    public String showBookInfo(String readerId,String barcode) {
        JSONObject json = new JSONObject();
        try {
            String rdid ;
            json.put("action", "showBookInfo");
            json.put("barcode", barcode);
            if (readerId!=null){
                rdid=Constant.uid;
                json.put("rdid", readerId);

            }else{
                rdid = Constant.reader.getString("rdid");
                json.put("rdid", rdid);
            }
            String[] keycode = Constant.getKeyCode(rdid);
            json.put("keyCode", keycode[0]);
            json.put("time", keycode[1]);
            return NetworkHelp.doJsonPost(VersionSetting.baseUrl, json.toString());
        } catch (JSONException e) {
            Log.d(TAG,"JSONException");
            return null;
        } catch (Exception e) {

            Log.d(TAG,"IOException");
            return null;
        }
    }

    @Override
    public String bookHolding(String bookrecno) {

        if(VersionSetting.IS_CONNECT_INTERLIB3){

            return NetworkHelp.sendDataByPost(null, "UTF-8", VersionSetting.URL_INTERLIB3_OPEN+"/service/barcode/queryholding"+"?token="+Constant.TOKEN+"&bookrecno="+bookrecno);
        }else {

            return NetworkHelp.sendDataByPost(null, "UTF-8", VersionSetting.baseUrl3 +"/api/holding/" + bookrecno);
        }
    }

}
