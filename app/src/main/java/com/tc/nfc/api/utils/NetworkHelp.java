package com.tc.nfc.api.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;


import com.tc.nfc.util.Constant;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * 网络连接类
 */
public class NetworkHelp {
    private static boolean flag=false;
    private static final String TAG ="NetworkHelp";
    private static final int TIMEOUT_MILLIONS = 12000;
    /**
     * 判断网络是否连接
     *
     * @param context
     * @return
     *
     */
    public static boolean isConnected(Context context)
    {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (null != connectivity)
        {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (null != info && info.isConnected())
            {
                if (info.getState() == NetworkInfo.State.CONNECTED)
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断是否是wifi连接
     */
    public static boolean isWifi(Context context)
    {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivity == null)
            return false;

        return connectivity.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI;
    }

    /**
     * 获取网络类型
     * @param context
     * @return
     */
    public static int getNetType(Context context)
    {
        int netType = -1;
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if(networkInfo==null)
        {
            return netType;
        }
        int nType = networkInfo.getType();
        if(nType==ConnectivityManager.TYPE_MOBILE)
        {
            if(networkInfo.getExtraInfo().toLowerCase().equals("cmnet"))
            {
                netType = 3;
            }
            else
            {
                netType = 2;
            }
        }
        else if(nType==ConnectivityManager.TYPE_WIFI)
        {
            netType = 1;
        }
        return netType;
    }

    /**
     * 获取wifi名字
     * @return
     */
    public static String getConnectWifiSsid(Context context){
        WifiManager wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        return wifiInfo.getSSID();
    }

    public static void setAction(boolean FLAG){
        flag=FLAG;
    }

    /**
     * 业务请求
     * @param url
     * @param param
     * @return
     * @throws IOException
     */
    public static String doJsonPost(String url, String param) throws IOException {

        DefaultHttpClient client = new DefaultHttpClient();

        client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 8000);
        client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 8000);
        if (flag){

            client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20000);
            client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 20000);
        }
        HttpPost post = new HttpPost(url);
        String response = null;
        StringEntity stringEntity = new StringEntity(param);
        stringEntity.setContentEncoding("UTF-8");
        stringEntity.setContentType("application/json");
        post.setEntity(stringEntity);

        //获取cookies
        if (null != Constant.JSESSIONID) {
            post.setHeader("Cookie", "JSESSIONID=" + Constant.JSESSIONID);
        }
        HttpResponse httpResponse = client.execute(post);
        if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

            Log.d(TAG,"请求成功1");
            HttpEntity entity = httpResponse.getEntity();
            response = EntityUtils.toString(entity);// 返回json格式：

            CookieStore cookieStore = client.getCookieStore();
            List<Cookie> cookies = cookieStore.getCookies();
            for (int i = 0; i < cookies.size(); i++) {
                if ("JSESSIONID".equals(cookies.get(i).getName())) {
                    System.out.println(cookies.get(i).getValue());
                    Constant.JSESSIONID = cookies.get(i).getValue();
                    break;
                }
            }

        }
        flag=false;
        System.out.println("状态：" + httpResponse.getStatusLine().getStatusCode());
        return response;
    }
    /**
     * Get funtion
     * @param encode
     * @param path
     * @return
     */
    public static String getDataByGet(String encode, String path){

        URL url =null;

        HttpURLConnection connection =null;

        InputStream inptStream =null;

        int responseCode;

        try {
            url = new URL(path);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(TIMEOUT_MILLIONS);
            connection.setConnectTimeout(TIMEOUT_MILLIONS);
            connection.setDoInput(true);
            connection.setUseCaches(false);

            responseCode = connection.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK) {
                inptStream = connection.getInputStream();

                Log.d(TAG,"请求成功2");
                return dealResponseResult(inptStream,encode);

            }

        } catch (IOException e) {
            return "err: " + e.getMessage().toString();
        } finally {
            try {
                if (connection != null) {
                    connection.disconnect();
                }
                if (inptStream != null) {
                    inptStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    /**
     * */
    public static String sendDataByPost(Map<String, String> params, String encode, String path) {

        URL url=null;

        HttpURLConnection connection = null;

        OutputStream outputStream = null;

        InputStream inputStream = null;

        int responseCode;

        byte [] data = getRequestData(params, encode).toString().getBytes();

        try {
            url = new URL(path);
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(TIMEOUT_MILLIONS);
            connection.setReadTimeout(TIMEOUT_MILLIONS);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", String.valueOf(data.length));

            outputStream = connection.getOutputStream();
            outputStream.write(data, 0, data.length);

            responseCode = connection.getResponseCode();

            if (responseCode == 200) {

                Log.d(TAG,"请求成功3");
                inputStream = connection.getInputStream();
                return dealResponseResult(inputStream, encode);
            }else{
                Log.d(TAG,"响应码："+responseCode);
            }
        } catch (Exception e) {

        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return "";
    }

    /**
     * 连接interlib的方法*/
    public static String sendDataByPost2(Map<String, String> params, String encode, String path) {

        URL url=null;

        HttpURLConnection connection = null;

        OutputStream outputStream = null;

        InputStream inputStream = null;

        int responseCode;

        byte []data = getRequestData(params, encode).toString().getBytes();

        try {
            url = new URL(path);

            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(TIMEOUT_MILLIONS);
            connection.setReadTimeout(TIMEOUT_MILLIONS);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", String.valueOf(data.length));
            if (Constant.sessionId!=null){
                connection.setRequestProperty("Cookie", Constant.sessionId);
            }

            outputStream = connection.getOutputStream();
            outputStream.write(data, 0, data.length);

            responseCode = connection.getResponseCode();

            if (responseCode == 200) {

                inputStream = connection.getInputStream() ;
                if (connection.getHeaderField("Set-Cookie")!=null){
                    String session_value = connection.getHeaderField("Set-Cookie");
                    String[] sessionId = session_value.split(";");
                    Constant.sessionId= sessionId[0];
                }
                return dealResponseResult(inputStream, encode);
            }
        } catch (Exception e) {

        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return "";
    }


    /**
     *
     * @param connType      请求地址方式 0,检测图片; 1,首次录脸; 2,多次录脸; 3,删除人脸; 4,识别人脸; 5,显示人脸; 6,删除用户;
     * @param urlPath       连接地址
     * @param loginId       登录账号
     * @param password      登录密码
     * @param appId         应用id
     * @param baseImg64     base64编码的二进制图片数据
     * @param globalLibId   全局馆代码
     * @param userLib       用户馆
     * @param userCode      读者证号或者操作员代码
     * @param userRecNo     用户记录号（中心识别后获取）
     * @param faceIds       多个图片faceid,用~m~分隔
     * @return
     */
    public static String checkFacePicConnPost (int connType,String urlPath,String loginId,String password,String appId,String baseImg64,String globalLibId,String userLib,String userCode,String userRecNo,String faceIds){
        //使用HttpURLConnection访问接口数据
        URL url= null;

        HttpURLConnection urlConnection = null;

        String requestBody = null;
        PrintWriter printWriter = null;

        InputStream inputStream = null;
        BufferedReader bufferedReader = null;

        String result;

                try {
                    url = new URL(urlPath);

                    urlConnection=(HttpURLConnection) url.openConnection();
                    urlConnection.setConnectTimeout(TIMEOUT_MILLIONS);
                    urlConnection.setReadTimeout(TIMEOUT_MILLIONS);
                    urlConnection.setRequestMethod("POST");
                    urlConnection.connect();
                    switch (connType){
                        //检查是否是人脸的照片
                        case 0: requestBody="loginid="+loginId+"&password="+password+"&appid="+appId+"&baseimg64="+baseImg64;
                            break;
                        //首次录脸
                        case 1: requestBody="loginid="+loginId+"&password="+password+"&appid="+appId+"&baseimg64="+baseImg64+"&globallibid="+globalLibId+"&userlib="+userLib+"&usercode="+userCode;
                            break;
                        //多次录脸
                        case 2: requestBody="loginid="+loginId+"&password="+password+"&appid="+appId+"&baseimg64="+baseImg64+"&globallibid="+globalLibId+"&userrecno="+userRecNo;
                            break;
                        //删除人脸
                        case 3: requestBody="loginid="+loginId+"&password="+password+"&appid="+appId+"&baseimg64="+baseImg64+"&userrecno="+userRecNo+"&faceids="+faceIds;
                            break;
                        //识别人脸
                        case 4:requestBody="loginid="+loginId+"&password="+password+"&appid="+appId+"&baseimg64="+baseImg64+"&globallibid="+globalLibId;
                            break;
                        //显示人脸
                        case 5: requestBody="faceid="+faceIds;
                            break;
                        //删除用户
                        case 6: requestBody="loginid="+loginId+"&password="+password+"&appid="+appId+"&baseimg64="+baseImg64+"&globallibid="+globalLibId+"&usercode="+userCode;
                            break;
                        default: requestBody = "";
                            break;
                    }

                    printWriter=new PrintWriter(urlConnection.getOutputStream());
                    printWriter.write(requestBody);
                    printWriter.flush();
                    printWriter.close();
                    int code=urlConnection.getResponseCode();
                    if(code == 200){
                        inputStream=urlConnection.getInputStream();
                        bufferedReader=new BufferedReader(new InputStreamReader(inputStream));
                        String liner;
                        StringBuffer buffer=new StringBuffer();
                        while ((liner=bufferedReader.readLine())!=null) {
                            buffer.append(liner);
                        }
                        result=buffer.toString();
                        return result;
                    }else{
                        Log.d(TAG," 网络获取数据异常:"+code);
                    }
                } catch (Exception e) {
                    Log.d(TAG,"捕获异常："+e.getMessage());
                    e.printStackTrace();
                }finally {
                    try {
                        if ( bufferedReader!= null) {
                            bufferedReader.close();
                        }
                        if(printWriter != null){
                            printWriter.close();
                        }
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        if (urlConnection != null) {
                            urlConnection.disconnect();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

        return "";
    }


    /**
     *
     * @param uploadType
     * @param urlPath
     * @param user
     * @param password
     * @param machineId
     * @param logLevel
     * @param content
     * @param readerCode
     * @param readerName
     * @param bookBarcode
     * @param bookTitle
     * @param type
     * @param logType
     * @param logData1
     * @param logData2
     * @param fee
     * @param borrowdate
     * @param shoulddate
     * @param renewdate
     * @return
     */
    public static String uploadOperationLog(int uploadType, String urlPath, String user, String password, String machineId, String logLevel, String content, String readerCode, String readerName, String bookBarcode, String bookTitle, String type, String logType, String logData1,String logData2,String fee,String borrowdate,String shoulddate,String renewdate){
        //使用HttpURLConnection访问接口数据
        URL url= null;

        HttpURLConnection urlConnection = null;

        String requestBody = null;
        PrintWriter printWriter = null;

        InputStream inputStream = null;
        BufferedReader bufferedReader = null;

        String result;

        try {
            url = new URL(urlPath);

            urlConnection=(HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(TIMEOUT_MILLIONS);
            urlConnection.setReadTimeout(TIMEOUT_MILLIONS);
            urlConnection.setRequestMethod("POST");
            urlConnection.connect();
            switch (uploadType){
                case 0: requestBody="user="+user+"&passwd="+password+"&machineid="+machineId+"&logLevel="+logLevel+"&content="+content;
                    break;
                case 1: requestBody="user="+user+"&passwd="+password+"&machineid="+machineId+"&readercode="+readerCode+"&readername="+readerName+"&bookbarcode="+bookBarcode+"&booktitle="+bookTitle+"&type="+type+"&fee="+fee+"&borrowdateStr="+borrowdate+"&shoulddateStr="+shoulddate+"&renewdateStr="+renewdate;
                    break;
                case 2: requestBody="user="+user+"&passwd="+password+"&machineid="+machineId+"&logtype="+logType+"&logData1="+logData1+"&logData2="+logData2;
                    break;
                case 3: requestBody="userId="+user+"&userName="+machineId+"&usrPasswd="+password+"&mtype="+type+"&libcode="+""+"&borrowstatus="+logData1+"&returnstatus="+logType+"&renewstatus="+logData2;
                     break;
                default: requestBody = "";
                    break;
            }
            Log.d("test","requestBody:"+requestBody);
            printWriter=new PrintWriter(urlConnection.getOutputStream());
            printWriter.write(requestBody);
            printWriter.flush();
            printWriter.close();
            int code=urlConnection.getResponseCode();
            Log.d("test","响应码："+code);
            if(code == 200){
                inputStream=urlConnection.getInputStream();
                bufferedReader=new BufferedReader(new InputStreamReader(inputStream));
                String liner;
                StringBuffer buffer=new StringBuffer();
                while ((liner=bufferedReader.readLine())!=null) {
                    buffer.append(liner);
                }
                result=buffer.toString();
                return result;
            }else{
                Log.d(TAG," 网络获取数据异常:"+code);
            }
        } catch (Exception e) {
            Log.d(TAG,"捕获异常："+e.getMessage());
            e.printStackTrace();
        }finally {
            try {
                if ( bufferedReader!= null) {
                    bufferedReader.close();
                }
                if(printWriter != null){
                    printWriter.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return "";
    }




    public static StringBuffer getRequestData(Map<String, String> params, String encode) {
        StringBuffer buffer = new StringBuffer();

        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {

                buffer.append(entry.getKey())
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), encode))
                        .append("&");
            }

            buffer.deleteCharAt(buffer.length() - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return buffer;
    }

    public static String dealResponseResult(InputStream inputStream, String encode) {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte [] data = new byte[1024];
        int lenngth = 0;

        try {
            while ((lenngth = inputStream.read(data)) != -1) {
                byteArrayOutputStream.write(data, 0, lenngth);
            }
            return new String(byteArrayOutputStream.toByteArray(), encode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


}
