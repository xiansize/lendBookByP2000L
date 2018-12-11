package com.tc.nfc.util;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.basewin.services.ServiceManager;
import com.iflytek.cloud.SpeechUtility;
import com.tc.api.VersionSetting;
import com.tc.nfc.R;
import com.tc.nfc.core.implement.BaseActionImpl;
import com.tc.nfc.core.implement.BookActionImpl;
import com.tc.nfc.core.implement.CommonActionImpl;
import com.tc.nfc.core.implement.LogActionImpl;
import com.tc.nfc.core.implement.LoginActionImpl;
import com.tc.nfc.core.implement.PictureActionImpl;
import com.tc.nfc.core.implement.PurchaseActionImpl;
import com.tc.nfc.core.implement.SearchActionImpl;
import com.tc.nfc.core.implement.UserActionImpl;
import com.tc.nfc.core.interfaces.BaseAction;
import com.tc.nfc.core.interfaces.BookAction;
import com.tc.nfc.core.interfaces.CommonAction;
import com.tc.nfc.core.interfaces.LogAction;
import com.tc.nfc.core.interfaces.LoginAction;
import com.tc.nfc.core.interfaces.PictureAction;
import com.tc.nfc.core.interfaces.PurchaseAction;
import com.tc.nfc.core.interfaces.SearchAction;
import com.tc.nfc.core.interfaces.UserAction;
import com.uzmap.pkg.openapi.APICloud;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tangjiarao on 16/6/23.
 */
public class NFCApplication extends Application {
    private static Context mAppContext;
    private BaseAction mBaseAction;
    private LoginAction mLoginAction;
    private CommonAction mCommonAction;
    private BookAction mBookAction;
    private UserAction mUserAction;
    private SearchAction mSearchAction;
    private PurchaseAction mPurchaseAction;
    private PictureAction mPictureAction;
    private LogAction mLogAction;

    //管理所有activity销毁
    List<Activity> mActivityList = new ArrayList<>();


    @Override
    public void onCreate() {
        super.onCreate();

        APICloud.initialize(this);//初始化APICloud，SDK中所有的API均需要初始化后方可调用执行
        mAppContext =getApplicationContext();
        mBaseAction = new BaseActionImpl(this);
        mLoginAction = new LoginActionImpl(this);
        mCommonAction = new CommonActionImpl(this);
        mBookAction = new BookActionImpl(this);
        mUserAction = new UserActionImpl(this);
        mSearchAction =new SearchActionImpl(this);
        mPurchaseAction = new PurchaseActionImpl(this);
        mPictureAction = new PictureActionImpl(this);
        mLogAction = new LogActionImpl(this);

        // 应用程序入口处调用，避免手机内存过小，杀死后台进程后通过历史intent进入Activity造成SpeechUtility对象为null
        // 如在Application中调用初始化，需要在Mainifest中注册该Applicaiton
        // 注意：此接口在非主进程调用会返回null对象，如需在非主进程使用语音功能，请增加参数：SpeechConstant.FORCE_LOGIN+"=true"
        // 参数间使用半角“,”分隔。
        // 设置你申请的应用appid,请勿在'='与appid之间添加空格及空转义符

        // 注意： appid 必须和下载的SDK保持一致，否则会出现10407错误

        SpeechUtility.createUtility(NFCApplication.this, "appid=" + getString(R.string.app_id));

        // 以下语句用于设置日志开关（默认开启），设置成false时关闭语音云SDK日志打印
        // Setting.setShowLog(false);

        //P2000设备
        if(VersionSetting.IS_P2000L_DEVICE){
            ServiceManager.getInstence().init(getApplicationContext());
        }

    }

    public LoginAction getLoginAction() {
        return mLoginAction;
    }
    public CommonAction getCommonAction() {return mCommonAction;}
    public BookAction getBookAction() {
        return mBookAction;
    }
    public UserAction getUserAction() {return mUserAction;}
    public SearchAction getmSearchAction() {return mSearchAction;}
    public BaseAction getmBaseAction() {
        return mBaseAction;
    }

    public PurchaseAction getmPurchaseAction() {
        return mPurchaseAction;
    }

    public PictureAction getmPictureAction(){
        return mPictureAction;
    }

    public LogAction getmLogAction(){
        return  mLogAction;
    }

    public void setmPurchaseAction(PurchaseAction mPurchaseAction) {
        this.mPurchaseAction = mPurchaseAction;
    }

    public static Context getmAppContext() {return mAppContext;}

    //提供一个添加activity的方法
    public void addActivity(Activity activity) {
        if (!mActivityList.contains(activity)) {
            mActivityList.add(activity);
            String re="";
            for (Activity b: mActivityList){
                re+=b.toString()+" ";

            }
            Log.d("BASE",re);
        }
    }

    //提供一个移除activity的方法
    public void removeActivity(Activity activity) {
        if (!mActivityList.contains(activity)) {
            mActivityList.remove(activity);
        }
    }

    //提供一个清空集合的方法
    public void clearAllActivity() {
        for (int i = 0; i< mActivityList.size(); i++) {
            Activity activity = mActivityList.get(i);
            activity.finish();
        }
        mActivityList.clear();
    }
    public void clearAllExceptNew() {
        for (int i = 0; i< mActivityList.size()-1; i++) {
            Activity activity = mActivityList.get(i);
            activity.finish();
        }
        mActivityList.clear();
    }
}