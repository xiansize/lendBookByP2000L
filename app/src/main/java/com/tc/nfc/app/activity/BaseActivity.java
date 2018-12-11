package com.tc.nfc.app.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.iflytek.sunflower.FlowerCollector;
import com.tc.api.VersionSetting;
import com.tc.nfc.R;
import com.tc.nfc.api.utils.NetworkHelp;
import com.tc.nfc.core.task.ThreadPoolManager;
import com.tc.nfc.model.ReturnBookResult;
import com.tc.nfc.util.Constant;
import com.tc.nfc.util.LoginBroadCastReceiver;
import com.tc.nfc.util.MD5Util;
import com.tc.nfc.util.NFCApplication;
import com.tc.nfc.app.view.HintDialog;
import com.tc.nfc.core.listener.ActionCallbackListener;
import com.tc.nfc.app.utils.nfcA.NfcCommon;
import com.tc.nfc.util.UpdateAppUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.logging.Handler;

/**
 * Created by tangjiarao on 16/8/12.
 */
public abstract class BaseActivity extends FragmentActivity {

    private final String TAG ="BaseActivity";

    private final String DEVICE_MODAL = null;

    public NFCApplication mNFCApplication;
    private Dialog dialogs;
    private HintDialog mDialog = null;
    private ProgressDialog m_progressDlg;
    public BaseActivity() {}


    /**
     * OnCreate
     * @param savedInstanceState
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBase();
        initContentView(savedInstanceState);
    }

    /**
     * 初始化UI，setContentView等
     * @param savedInstanceState
     */
    protected abstract void initContentView(Bundle savedInstanceState);

    /**
     * 可能全屏或者没有ActionBar等
     */
    private void setBase() {
        //去除toolbar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //顶部浸入式
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //底部浸入式
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        mNFCApplication =(NFCApplication)this.getApplication();

        mNFCApplication.getCommonAction().checkWritePermissions(this);
        mNFCApplication.addActivity(this);
        //注册广播接收器
        //registerReceiver();

        Log.d("BASE", getTopActivity(this));

    }

    /**
     *
     * @param enable
     */
    protected void addLeftMenu(boolean enable) {
        // 如果你的项目有侧滑栏可以处理此方法
        if (enable) { // 是否能有侧滑栏

        } else {

        }
    }

    /**
     * 周期发送信息给广播进行重连操作
     */
    private void RepeatLogin() {
        Intent intent = new Intent(this, LoginBroadCastReceiver.class);
        intent.setAction("repeat");
        PendingIntent sender = PendingIntent.getBroadcast(this,0,intent,0);

        long firstTime = SystemClock.elapsedRealtime() + VersionSetting.REPEAT_TIME*1000;
        AlarmManager aManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        //周期不停的发送广播
        aManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, VersionSetting.REPEAT_TIME*1000, sender);

    }

    /**
     * 获取版本信息
     */
    public String getDEVICE_MODAL() {
        return Build.MODEL;
    }

    /**
     * 横竖屏
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        try {
            super.onRestoreInstanceState(savedInstanceState);
        } catch (Exception e) {
        }
    }

    /**
     * 获取屏幕宽高
     */
    protected void getWindowData() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        Constant.displayWidth = displayMetrics.widthPixels;
        Constant.displayHeight = displayMetrics.heightPixels;
    }

    /**
     * 接收nfc读取信息
     * @param intent
     */
    protected void onNewIntent(Intent intent) {

        super.onNewIntent(intent);
    }

    /**
     * 获取服务器版本号
     */
    protected void updateApp() {

        UpdateAppUtil.getServerVersion(mNFCApplication, new ActionCallbackListener<Integer>() {
            @Override
            public void onSuccess(Integer data) {
                if (data!= UpdateAppUtil.getAppVersion(BaseActivity.this)){
                    if (NetworkHelp.isWifi(BaseActivity.this)){
                        if (dialogs==null)
                        	createDialog("版本更新","有版本更新，是否更新版本");
                    }
                    else {
                        if (dialogs==null)
                        	createDialog("版本更新","有版本更新，当前不在wifi状态，是否更新版本");
                    }

                }
                //版本不需要更新操作
                else{
                    Log.d(TAG, "same");
                }
            }

            @Override
            public void onFailure(String errorEvent, String message) {}
        });
    }

    /**
     * 下载app
     * @param title
     * @param message
     */
    private void createDialog(String title,String message){
        m_progressDlg =  new ProgressDialog(this);
        m_progressDlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        // 设置ProgressDialog 的进度条是否不明确 false 就是不设置为不明确
        m_progressDlg.setIndeterminate(false);
		if(mDialog==null&&Constant.isNeedUpdate==true){
			mDialog = new HintDialog(this,title,message);
			mDialog.showDialog(R.layout.dialog_normals_layout, new HintDialog.IHintDialog(){

				@Override
				public void showWindowDetail(Window window, String title,
						String message) {
					TextView OKTextView = (TextView) window.findViewById(R.id.positiveButton);
					TextView CancleTextView =(TextView)window.findViewById(R.id.negativeButton);
					TextView messageTextView = (TextView) window.findViewById(R.id.message);
					TextView titleTextView = (TextView) window.findViewById(R.id.title_textview);
					OKTextView.setOnClickListener(new View.OnClickListener(){
						@Override
						public void onClick(View v) {
							m_progressDlg.setTitle("正在下载");
                            m_progressDlg.setMessage("请稍候...");
                            UpdateAppUtil.downNewApp(VersionSetting.GET_SERVER_IP, m_progressDlg);
                            UpdateAppUtil.getAllFiles(new File("/sdcard/newApp"));
                            mDialog.dismissDialog();
						}

					});
					CancleTextView.setOnClickListener(new View.OnClickListener(){
						@Override
						public void onClick(View v) {
							mDialog.dismissDialog();
                            Constant.isNeedUpdate=false;
						}
					});
					messageTextView.setText(message);
					titleTextView.setText(title);
				}
			});
		}
	}


    /**
     * 注册广播接收器
     */
//    private  void registerReceiver(){
//        IntentFilter filter=new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
//        MyReceiver myReceiver=new MyReceiver();
//        this.registerReceiver(myReceiver, filter);
//    }

    /**
     * 接收网络状态广播消息
     */
//    public class MyReceiver extends BroadcastReceiver {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            ConnectivityManager connectivityManager=(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//            NetworkInfo  mobNetInfo=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
//            NetworkInfo  wifiNetInfo=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//
//            if (!mobNetInfo.isConnected() && !wifiNetInfo.isConnected()) {
//                Toast.makeText(context, "网络状态不可用", Toast.LENGTH_SHORT).show();
//            }else {
//                dialogs=null;
//                //获取服务器版本
//                Log.d(TAG,"MyReceiver");
//                UpdateAppUtil.getServerVersion(context);
//            }
//        }  //如果无网络连接activeInfo为null
//    }

    protected void onResume() {
        super.onResume();
        FlowerCollector.onResume(this);
        NfcCommon.enableNfcForegroundDispatch(this);
        if(VersionSetting.IS_REPEAT_LOGIN){
            RepeatLogin();//周期性重连操作
        }

    }


    protected void onPause() {
        super.onPause();
        FlowerCollector.onPause(this);
        NfcCommon.disableNfcForegroundDispatch(this);

    }

    protected void onStart(){
        super.onStart();
    }

    protected void onStop(){
        super.onStop();
    }

    protected void onDestroy(){
        super.onDestroy();
        mNFCApplication.removeActivity(this);
    }

    // 跳转到登录页面
    public void login() {
        Intent intent = new Intent();
        intent.setClass(this, LoginActivity.class);
        startActivity(intent);
    }

    /**
     * 获取栈顶activity
     * @param context
     * @return
     */
    String getTopActivity(Activity context)
    {
        ActivityManager manager = (ActivityManager)context.getSystemService(ACTIVITY_SERVICE) ;
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1) ;
        if(runningTaskInfos != null){
            return Arrays.toString(runningTaskInfos.toArray());
        }
        else{
            return null ;
        }

    }

    /**
     * 上传设备运行情况
     * @param userName
     * @param password
     * @param machineId
     * @param logLevel      运行情况(1:正常，2:缓慢，3卡机，4断开)
     * @param content
     */
    public void upLoadMachineLog(String userName,String password,String machineId,String logLevel,String content){
        mNFCApplication.getmLogAction().uploadMachineLog(userName, MD5Util.getMD5Security(password),machineId,logLevel,content);
    }

    /**
     * 借书的日志
     * @param user
     * @param password
     * @param machineId
     * @param readCode
     * @param readerName
     * @param bookBarcode
     * @param map
     * @param type          1代表借、2代表还、3代表续。借书时间(格式：yyyy-MM-dd hh:mm:ss) 应归还时间(格式：yyyy-MM-dd hh:mm:ss)
     */
    public void uploadLoanBookLog(String user, String password, String machineId, String readCode, String readerName, String bookBarcode, Map<String, Object> map, String type){
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            mNFCApplication.getmLogAction().uploadBookLog(user, MD5Util.getMD5Security(password),machineId,readCode,readerName,bookBarcode,map.get("title").toString(),type,"",df.format(new Date()),map.get("returnDate").toString()+" 23:59:59","");
           }catch (NullPointerException e){
            Log.d(TAG,"借还书的空指针异常："+e.getMessage());
        }
    }

    /**
     * 借书的日志
     * @param user
     * @param password
     * @param machineId
     * @param readCode
     * @param readerName
     * @param bookBarcode
     * @param map
     * @param type          1代表借、2代表还、3代表续。借书时间(格式：yyyy-MM-dd hh:mm:ss) 应归还时间(格式：yyyy-MM-dd hh:mm:ss)
     */
    public void uploadRetureBookLog(String user, String password, String machineId, String readCode, String readerName, String bookBarcode, Map<String, Object> map, String type){
        try {
            //TODO 需要从map中获取读者借书的时间
            //mNFCApplication.getmLogAction().uploadBookLog(user, MD5Util.getMD5Security(password),machineId,readCode,readerName,bookBarcode,map.get("title").toString(),type,"",map.get("").toString()+" 12:00:00",map.get("returnDate").toString()+" 23:59:59","");
        }catch (NullPointerException e){
            Log.d(TAG,"借还书的空指针异常："+e.getMessage());
        }
    }


    /**
     * 续借
     * @param list
     * @param user
     * @param password
     * @param machineId
     * @param readCode
     * @param readerName
     * @param type          1代表借、2代表还、3代表续。借书时间(格式：yyyy-MM-dd hh:mm:ss) 应归还时间(格式：yyyy-MM-dd hh:mm:ss)
     */
    public void uploadReLoanBookLog(List<? extends ReturnBookResult> list, String user, String password, String machineId, String readCode, String readerName, String type){

        for (ReturnBookResult returnBookResult: list){

            if(returnBookResult.isReturnResult()){
                mNFCApplication.getmLogAction().uploadBookLog(user,MD5Util.getMD5Security(password), machineId,readCode,readerName,returnBookResult.getBook().getBookBarcode(),returnBookResult.getBook().getBookTitle(),type,"",returnBookResult.getBook().getLoanDate()+" 12:00:00",returnBookResult.getBook().getReturnDate()+" 23:59:59","");

            }
        }

    }


    /**
     * 盘点和上架成功之后提交的日志
     * @param user
     * @param password
     * @param machineId
     * @param logType       204a：盘点，204b：上架
     * @param logData1      盘点书的条形码 OR 上架书的条形码
     * @param logData2      204b上架时必填：上架的柜号(书上架时要记录书架的柜号)
     */
    public void uploadInventoryLog(String user, String password, String machineId, String logType, String logData1, String logData2){
        mNFCApplication.getmLogAction().uploadWorkStationLog(user,MD5Util.getMD5Security(password),machineId,logType,logData1,logData2);

    }

    /**
     *
     * @param user
     * @param password
     * @param machineID
     * @param logType
     * @param logData1
     * @param logData2
     * @param poolManager
     * @param handler
     */
    public void uploadInventoryLog2(String user, String password,String machineID,String logType,String logData1,String logData2, ThreadPoolManager poolManager, android.os.Handler handler){
        mNFCApplication.getmLogAction().uploadInventoryLog(user,MD5Util.getMD5Security(password),machineID,logType,logData1,logData2,poolManager,handler);
    }

    /**
     *
     * @param machineID
     * @param machineName
     * @param userPassword
     * @param mType
     * @param libcode
     * @param borrowStatus
     * @param returnStatus
     * @param renewStatus
     */
    public void upLoadLogLogin(String machineID,String machineName,String userPassword,String mType,String libcode,String borrowStatus,String returnStatus,String renewStatus){
        mNFCApplication.getmLogAction().uploadLogLogin(machineID,machineName,MD5Util.getMD5Security(userPassword),mType,libcode,borrowStatus,returnStatus,renewStatus);
    }


}
