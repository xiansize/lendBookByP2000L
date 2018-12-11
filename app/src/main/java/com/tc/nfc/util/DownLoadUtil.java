package com.tc.nfc.util;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.tc.nfc.model.Purchase;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

/**
 * Created by tangjiarao on 16/6/2.
 * 该类是版本更新辅助类
 */
public class DownLoadUtil {


    public interface DownLoadCallbackListener {
        void downLoadResult(Boolean result,String message);

        void sendProgress(String persent,int Progress);

        void sendItemCount(String count);

        void sendUploadResult(Boolean result,String message);
    }
    private static DownLoadCallbackListener listener;
    private static Context mContext;
    public static final String  PURCHASE_DIRECTORY_NAME = "/download";
    public static final String  PURCHASE_FilE_NAME = "puchase.txt";
    public static final String  DIRECTORY_NAME = "/newApp";
    public static final String  File_NAME = "NewVersion.apk";
    public static final String  TAG = "DownLoadUtil";


    /**
     * 创建文件路径
     */
    public static File getDirectory(String directoryName){

        File file = new File(Environment.getExternalStorageDirectory() + PURCHASE_DIRECTORY_NAME);
        //如果该路径不存在，则创建文件夹
        if (!file.exists()) {
            file.mkdir();
        }
        return file;
    }

    /**
     * 是否存在sd卡
     * @return
     */
    public static boolean isSdCardExist() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }
    /**
     * 获取目标路径下的文件
     * @param root
     */
    public static void getAllFiles(File root){

        File files[] = root.listFiles();

        if(files != null)
            for(File f:files){

                if(f.isDirectory()){
                    getAllFiles(f);
                }
                else{
                    Log.d(TAG, f.getName());

                }
            }
    }
    public static void downLoad(final String path, final String directoryName,final String fileName) {

        new Thread() {
            public void run() {
                URL url = null;
                FileOutputStream fos = null;
                BufferedInputStream bis = null;
                HttpURLConnection connection = null;

                try {
                    url = new URL(path);
                    connection = (HttpURLConnection) url.openConnection();

                    //不能获取服务器响应
                    if (HttpURLConnection.HTTP_OK != connection.getResponseCode()) {
                        Message message = Message.obtain();
                        message.what = 1;
                        handler.sendMessage(message);
                    }
                    else{
                        //获取网络输入流
                        bis = new BufferedInputStream(connection.getInputStream());
                        //文件大小
                        int length = connection.getContentLength();

//                        progressDialog.setMax((int)length);
                        //缓冲区大小
                        byte[] buf = new byte[2048];
                        int size =0;

                        //获取存储文件的路径，在该路径下新建一个文件为写入流作准备
                        File cfile = new File(getDirectory(directoryName).getPath(), fileName);
                        //如果不存在则新建文件
                        if (!cfile.exists()) {
                            cfile.createNewFile();
                        }
                        //将流与文件绑定
                        fos = new FileOutputStream(cfile);

                        //记录进度条
                        int count=0;
                        //保存文件
                        while ((size = bis.read(buf)) != -1) {
                            fos.write(buf, 0, size);
                            count += size;
                            Log.d("XX",count+" "+length+" ");
                            if (length > 0) {
                                //这里的数后面加“D”是表明它是Double类型，否则相除的话取整，无法正常使用
                                double percent = (double)count / (double)length;
                                //获取格式化对象
//                              NumberFormat nt = NumberFormat.getPercentInstance();
//                              //设置百分数精确度2即保留两位小数
//                              nt.setMinimumFractionDigits(2);
//                              nt.format(percent);
                                Bundle bundle=new Bundle();
                                Message message=Message.obtain();
                                message.what=2;
                                bundle.putInt("progress", (int) (percent * 100));
                                message.setData(bundle);
                                handler.sendMessage(message);
                            }
                        }

                        Message message=Message.obtain();
                        message.what=4;
                        handler.sendMessage(message);


                    }

                } catch (Exception e) {
                    e.printStackTrace();

                    Message message=Message.obtain();
                    message.what=3;
                    handler.sendMessage(message);
                } finally {
                    try {
                        if (fos!= null) {
                            fos.close();
                        }
                        if (bis != null) {
                            bis.close();
                        }
                        if (connection!= null) {
                            connection.disconnect();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }.start();

    }

    public static void upload(final DBManager dbManager,final String path,final String uploadType){
        new Thread(){
            public void run() {

                try {

                    List<Purchase> list = new ArrayList<Purchase>();
                    list =  dbManager.getPurchaseThanId(uploadType);

                    //2.根据last_id获取需要同步的数据
                    if(!list.isEmpty()){

                        HttpPost httpPost = new HttpPost(path);
                        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
                        int step = 3123 + (int) Math.ceil(Math.random() * 513);
                        //int step = 100;
                        int i = 0;
                        String result = "";
                        if(list.size() < step){
                            for (Purchase purchase : list) {
                                nvps.add(new BasicNameValuePair("ISBN",purchase.getIsbn() == null?"":purchase.getIsbn()));
                                nvps.add(new BasicNameValuePair("orderNum",purchase.getOrderNum()+""));
                                nvps.add(new BasicNameValuePair("orderDate",purchase.getOrderDate()));
                                nvps.add(new BasicNameValuePair("imei",purchase.getImei()));
                                nvps.add(new BasicNameValuePair("orderLibLocal",purchase.getOrderLibLocal()));
                            }
                            //调用上传的方法
                            result = updateOrderHandle(httpPost,nvps);
                            //插入同步日志,记录这次同步的id
                            //MultifunctionalActivity.globalDB.savePurchaseLogId();
                            //3.将订购表的所有订购数据,上传状态改为1

                        }else{
                            //进行分批上传
                            for( ; i < list.size();i++){
                                Purchase purchase = list.get(i);
                                nvps.add(new BasicNameValuePair("ISBN",purchase.getIsbn() == null?"":purchase.getIsbn()));
                                nvps.add(new BasicNameValuePair("orderNum",purchase.getOrderNum()+""));
                                nvps.add(new BasicNameValuePair("orderDate",purchase.getOrderDate()));
                                nvps.add(new BasicNameValuePair("imei",purchase.getImei()));
                                if(i % step == 0){
                                    result = updateOrderHandle(httpPost,nvps);
                                    //	jsonArray.put(result);
                                    //	JSUtil.execCallback(pWebview, CallBackID, jsonArray, JSUtil.OK,false);
                                }
                            }
                            if(i % step != 0){
                                result = updateOrderHandle(httpPost,nvps);
                                //	jsonArray.put(result);
                                //	JSUtil.execCallback(pWebview, CallBackID, jsonArray, JSUtil.OK,false);
                            }
                            //3.插入同步日志,记录这次同步的id
                            //MultifunctionalActivity.globalDB.savePurchaseLogId();

                        }
                        if("true".equals(result)){
                            dbManager.updatePurchaseUploaded();

                            Message message=Message.obtain();
                            message.what=5;
                            handler.sendMessage(message);
                        }else{
                            Message message=Message.obtain();
                            message.what=6;
                            handler.sendMessage(message);
                        }

                    }else{
                        Message message=Message.obtain();
                        message.what=6;
                        Bundle bundle=new Bundle();
                        bundle.putString("message", "暂无可以上传数据!");
                        message.setData(bundle);
                        handler.sendMessage(message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }.start();
    }
    public static String updateOrderHandle(HttpPost httpPost,List<NameValuePair> nvps) throws Exception, Exception{
        httpPost.setEntity(new UrlEncodedFormEntity(nvps));
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = httpclient.execute(httpPost);
        int statuscode = response.getStatusLine().getStatusCode();
        if(statuscode == 200){//成功
            InputStream is = response.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String readLine = reader.readLine();
            JSONObject jsonObject = new JSONObject(readLine);
            String object = jsonObject.get("success")+"";

            nvps.clear();//成功后就清空
            return object;
        }else{

            return "false";
        }
    }

    private static Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (listener!=null){
                        listener.downLoadResult(false,"连接服务器失败");
                    }
                    break;
                case 2:
                    if (listener!=null) {
                        Log.d(TAG,msg.getData().getInt("progress")+"");
                        listener.sendProgress("", msg.getData().getInt("progress"));
                    }
                    break;
                case 3:
                    if (listener!=null) {
                        listener.downLoadResult(false, "下载服务器数据失败");
                    }
                    break;

                case 4:
                    if (listener!=null) {

                        listener.downLoadResult(true, "");
                    }
                    break;

                case 5:
                    if (listener!=null) {
                        listener.sendUploadResult(true,"");
                    }

                    break;

                case 6:
                    if (listener!=null) {
                        listener.sendUploadResult(false,msg.getData().getString("message", ""));
                    }
                    break;
                default:
                    break;
            }
        };
    };

    public static void setListener(DownLoadCallbackListener listener1){
        listener =listener1;
    }


    /**
     * 解析purchaseData
     *
     * @return
     */
    public static void getFilePathReport(DBManager dbManager) {
        dbManager.clearReportData();
        File file = null;

        try {

            file = new File(getDirectory(PURCHASE_DIRECTORY_NAME).getPath(), PURCHASE_FilE_NAME);
            BufferedReader br = new BufferedReader(new InputStreamReader(
                        new FileInputStream(file), "GBK"));
            String readline = "";
            String[] record;
            int count = 0;
            int step = 3123 + (int) Math.ceil(Math.random() * 513);
            List<String[]> reportList = new ArrayList<String[]>();
                try {

                    // 删除索引
                    dbManager.dropIsbnIdx();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                while ((readline = br.readLine()) != null) {
                    record = readline.split("\\t");

                    count++;
                    reportList.add(record);
                    if (count % step == 0) {

                        dbManager.insertReportList(reportList);
                        reportList = new ArrayList<String[]>();
                    }
                }
                if (count % step != 0) {
                    dbManager.insertReportList(reportList);

                }
                Log.d("XXX",dbManager.getCountOfReports()+"");
                listener.sendItemCount(String.valueOf(dbManager.getCountOfReports()));
                br.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // 重建索引
                try {
                    dbManager.createIsbnIdx();
                    // MultifunctionalActivity.globalDB.closeDB();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        // return importFlag;
    }
}
