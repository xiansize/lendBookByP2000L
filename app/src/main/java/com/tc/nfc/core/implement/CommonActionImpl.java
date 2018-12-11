package com.tc.nfc.core.implement;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;


import com.tc.nfc.api.implement.CommonApiImpl;
import com.tc.nfc.api.interfaces.CommonApi;
import com.tc.nfc.app.utils.nfcA.NfcCommon;
import com.tc.nfc.core.interfaces.CommonAction;
import com.tc.nfc.core.listener.ActionCallbackListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by tangjiarao on 16/6/23.
 */
public class CommonActionImpl implements CommonAction {

    private final String TAG = "CommonActionImpl";
    private Context context;
    private CommonApi mCommonApi;
    private NfcAdapter nfcAdapter;

    public CommonActionImpl(Context context) {
        this.context = context;
        this.mCommonApi = new CommonApiImpl();
    }

    /**
     * 判断手机是否有NFC功能
     * @return
     * 1:手机不支持NFC
     * 2:NFC未开启
     *
     * 先判断是否支持NFC功能，再判断是否开启NFC
     */
    public int isHasNFC() {

        nfcAdapter = NfcAdapter.getDefaultAdapter(context);
        int flag= nfcAdapter==null?1:!nfcAdapter.isEnabled()?2:0;
        return flag;
    }

    /**
     * 判断用户是否有写入操作
     * 返回true则调用initFolders方法创建相应文件夹与调用copyStdKeysFilesIfNecessary方法复制文件
     * 否则提示错误
     */
    public void checkWritePermissions(Activity activity){
        if (NfcCommon.hasWritePermissionToExternalStorage(activity)) {
            initFolders();
        } else {

            // Request the permission.
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    /**
     * Create the directories needed by MCT and clean out the tmp folder.
     * 创建文件夹
     */
    private void initFolders() {
        if (NfcCommon.isExternalStorageWritableErrorToast(context)) {
            // Create keys directory.
            File path = new File(Environment.getExternalStoragePublicDirectory(
                    NfcCommon.HOME_DIR) + "/" + NfcCommon.KEYS_DIR);

            if (!path.exists() && !path.mkdirs()) {
                // Could not create directory.
                Log.e("json", "Error while creating '" + NfcCommon.HOME_DIR
                        + "/" + NfcCommon.KEYS_DIR + "' directory.");
                return;
            }

            // Create std. key file if there is none.
            copyStdKeysFilesIfNecessary();
        }
    }

    /**
     * Copy the standard key files ({@link NfcCommon#STD_KEYS} and
     * {@link NfcCommon#STD_KEYS_EXTENDED}) form assets to {@link NfcCommon#KEYS_DIR}.
     * Key files are simple text files. Any plain text editor will do the trick.
     * All key and dump data from this App is stored in

     * there after App uninstallation.
     * @see NfcCommon#KEYS_DIR
     * @see NfcCommon#HOME_DIR
     * @see NfcCommon#copyFile(InputStream, OutputStream)
     */
    private void copyStdKeysFilesIfNecessary() {
        File std = new File(Environment.getExternalStoragePublicDirectory(
                NfcCommon.HOME_DIR) + "/" + NfcCommon.KEYS_DIR, NfcCommon.STD_KEYS);
        File extended = new File(Environment.getExternalStoragePublicDirectory(
                NfcCommon.HOME_DIR) + "/" + NfcCommon.KEYS_DIR,
                NfcCommon.STD_KEYS_EXTENDED);
        AssetManager assetManager = context.getAssets();

        if (!std.exists()) {
            // Copy std.keys.
            try {
                InputStream in = assetManager.open(
                        NfcCommon.KEYS_DIR + "/" + NfcCommon.STD_KEYS);

                OutputStream out = new FileOutputStream(std);

                NfcCommon.copyFile(in, out);

                in.close();
                out.flush();
                out.close();
            } catch(IOException e) {

                Log.e("JSON", "Error while copying 'std.keys' from assets "
                        + "to external storage.");
            }
        }

        if (!extended.exists()) {
            // Copy extended-std.keys.
            try {

                InputStream in = assetManager.open(
                        NfcCommon.KEYS_DIR + "/" + NfcCommon.STD_KEYS_EXTENDED);
                OutputStream out = new FileOutputStream(extended);
                NfcCommon.copyFile(in, out);
                in.close();
                out.flush();
                out.close();
            } catch(IOException e) {
                Log.e("ERROR", "Error while copying 'extended-std.keys' "
                        + "from assets to external storage.");
            }
        }
    }


    /**
     * 获取服务器版本号
     * @param listener
     */
    public void getServerVersion(final ActionCallbackListener listener) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {

                return mCommonApi.getServerVersion();
            }

            @Override
            protected void onPostExecute(String result) {
                Log.d("json", result);

                if (listener != null && result != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        int serverVersonCode = Integer.parseInt(jsonObject.getString("newVersion"));

                        listener.onSuccess(serverVersonCode);
                    } catch (JSONException e) {
                        Log.d("ERROR","CommonActionImpl :"+e.toString());
                        e.printStackTrace();
                    }
                }
                else{
                    listener.onFailure("ERROR", "连接服务器失败");
                }
            }
        }.execute();
    }
    public void checkIsExit(Activity activity,long exitTime){
//        long exitTime = 0;
        if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(context, "再按一次退出程序",
                        Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                activity.finish();
                System.exit(0);
            }
    }
}
