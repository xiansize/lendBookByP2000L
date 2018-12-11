package com.tc.nfc.app.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.basewin.utils.BCDHelper;
import com.pos.sdk.card.PosCardManager;
import com.pos.sdk.utils.PosByteArray;
import com.tc.api.VersionSetting;
import com.tc.nfc.R;

import com.tc.nfc.app.utils.StringToOther;
import com.tc.nfc.app.utils.ToastUtil;
import com.tc.nfc.app.utils.nfcV.ExCheBarcode;
import com.tc.nfc.app.utils.nfcV.NfcVUtil;
import com.tc.nfc.util.Constant;
import com.tc.nfc.util.NFCApplication;
import com.tc.nfc.app.view.LoadingView;
import com.tc.nfc.core.listener.ActionCallbackListener;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import tplusr.scanlibrary.zxing.android.CaptureActivity;

/**
 * 录入读者信息页面
 * 状态:
 * PREPARE_STATE:录入前准备
 * LOADING_STATE:录入中
 * COMPLETE_STATE:录入完成
 */
public class CheckReaderActivity extends BaseActivity implements View.OnClickListener{

    private final String TAG ="CheckReaderActivity";
    public NFCApplication mNFCApplication;
    private FrameLayout layout = null;
    private View view;
    private TextView titleTV,operaTV;
    private ImageView imageView;
    private LinearLayout backImage, llScan;
    private RelativeLayout rlNoCardInput;
    private static final int REQUEST_CODE_SCAN = 0x0000;
    //图书条形码
    private String mBarcode;
    private String mReaderName;
    private String mReaderId;
    //标志是否正在验证读者，如果正在验证读者，则即使扫描到条码也不操作
    private Boolean IS_DO_ACTION =false;

    //无卡输入
    private EditText etInput;
    private Button btnInput;

    //loadingView
    private LoadingView loadingView =null;
    //记录用户即将进入操作
    private int readerAction;
    //设置3s延迟
    private final int SECONDS=3;
    private int time=SECONDS;

    private Button loanBtn;
    private Timer timer;
    private TimerTask task;
    //状态
    private final int PREPARE_STATE =0;
    private final int LOADING_STATE =1;
    private final int COMPLETE_STATE =2;

    //请求码
    private final static int NO_CARD_REQUEST_CODE = 0x0002;

    //P2000L设备读卡的使用变量
    private Thread mReadCardThread;
    private boolean readerCardRun = true;//控制循环
    private String oldBarcode = "";

    //键盘
    private InputMethodManager inputMethodManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
    }

    @Override
    protected void initContentView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_scan);
        mNFCApplication =(NFCApplication)this.getApplication();

        //开始读卡，读到卡就进行录入读者操作
        startReadCardThread();
    }



    public void initViews(){
        inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        readerAction=getIntent().getIntExtra("readerAction",0);
        titleTV =(TextView)findViewById(R.id.tv_title_activity);
        titleTV.setText("录入读者");
        operaTV =(TextView)findViewById(R.id.operaText);
        imageView=(ImageView)findViewById(R.id.operaImage);
        imageView.setImageDrawable(getResources().getDrawable(R.drawable.checkreader_icon));
        backImage = (LinearLayout)findViewById(R.id.ll_activity_title_backpress);
        llScan = (LinearLayout)findViewById(R.id.ll_activity_title_scan);
        llScan.setVisibility(View.VISIBLE);//让扫描二维码的图标展现出来，可以点击
        backImage.setOnClickListener(this);
        llScan.setOnClickListener(this);

        layout = (FrameLayout)findViewById(R.id.scanFrame);

        rlNoCardInput = (RelativeLayout) findViewById(R.id.rlNoCardInput);
        rlNoCardInput.setVisibility(View.VISIBLE);
        rlNoCardInput.setOnClickListener(this);

        btnInput = (Button) findViewById(R.id.btnNoCardCheck);
        etInput = (EditText) findViewById(R.id.etNoCardCheck);
//        btnInput.setVisibility(View.VISIBLE);
//        etInput.setVisibility(View.VISIBLE);
        btnInput.setOnClickListener(this);

    }



    /**
     * 接受并且处理NFC请求
     * @param intent
     */
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        mNFCApplication.getBookAction().getNFCBarcode(intent, new ActionCallbackListener<String>() {
            @Override
            public void onSuccess(String data) {
                mBarcode =data;
                if (mBarcode !=null&&!IS_DO_ACTION){

                    stateAction(LOADING_STATE);
                }
            }

            @Override
            public void onFailure(String errorEvent, String message) {
                Log.d(TAG,message);
            }
        });
    }



    /**
     * 不同状态动作
     * @param currentState 状态参数
     */
    public void stateAction(int currentState){
        switch(currentState){
            case PREPARE_STATE:
                //清空计时器
                if (timer!=null&task!=null){
                    timer.cancel();
                    task.cancel();
                    timer=null;
                    task=null;
                }
                //重新设置倒计时3秒
                time=SECONDS;
                break;
            case LOADING_STATE:
                //更改状态为正在录入
                IS_DO_ACTION = true;
                //显示loadingView
                appearShade(false,"");
                //录入操作
                changeReader();
                break;
            case COMPLETE_STATE:
                disappearShade();
                stateAction(PREPARE_STATE);
                break;
        }

    }


    /**
     * 切换用户
     */
    public void changeReader() {

        mNFCApplication.getUserAction().getReaderInfo(mBarcode,new ActionCallbackListener<JSONObject>() {

            @Override
            public void onSuccess(JSONObject data) {

                try {

                    //获取用户姓名和id
                    Log.d(TAG, "读者json：" + data.toString());
                    if(VersionSetting.IS_SHENZHEN_LIB){
                        mReaderName = data.getString("userName").toString();
                        mReaderId = data.getString("cardId").toString();

                    }else if(VersionSetting.IS_CONNECT_INTERLIB3){

                        mReaderName = data.getString("rdname").toString();
                        mReaderId = mBarcode;

                    }else {

                        mReaderName = data.getString("rdname").toString();
                        mReaderId = data.getString("rdid").toString();
                    }
                    appearShade(true,"");

                } catch (JSONException e) {
                    appearShade(false,"获取用户信息失败");
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(String errorEvent, String message) {
                appearShade(false,message);
                if (!message.equals("验证失败")){
                    ToastUtil.showToastShortTime(CheckReaderActivity.this,message);
                }
                //账号验证过期，跳转到登陆的界面重新登陆的界面
                if(message.contains("账号验证过期")){
                    finish();
                    login();
                }
            }
        });
    }








    //读读者卡
    private void startReadCardThread() {
        if(VersionSetting.IS_P2000L_DEVICE ){
            executeReadCard();
        }
    }


    /**
     * 打开覆盖层
     * 加载动画以及改变覆盖层上的内容
     * @param isCheckSuccess true为录入成功标志
     */
    public void appearShade(boolean isCheckSuccess,String message){
        if (loadingView==null){
            operaTV.setVisibility(View.INVISIBLE);
            imageView.setVisibility(View.INVISIBLE);
            rlNoCardInput.setVisibility(View.INVISIBLE);
            loadingView =new LoadingView(this,"正在验证读者...");
            loadingView.startAppearAnimation();
            layout.addView(loadingView);
        }
        else{

            loadingView.removeAllViews();
            LayoutInflater flater = LayoutInflater.from(this);

            view = flater.inflate(R.layout.view_checkreader_result, null);
            TextView bigTitle =(TextView)view.findViewById(R.id.bigTitle);
            TextView readerNameTv = (TextView)view.findViewById(R.id.readerName);
            TextView readerNumTv = (TextView)view.findViewById(R.id.readerNum);
            loanBtn =(Button)view.findViewById(R.id.loanBtn);
            Button recheckBtn =(Button)view.findViewById(R.id.recheckBtn);

            if (isCheckSuccess){
                bigTitle.setText("录入成功");
                readerNameTv.setText("姓名: " + mReaderName);
                readerNumTv.setText("读者证号: " + mReaderId);
                //记录此时的读者证信息
                saveUserData(mReaderId, mReaderName);

                //设置计时器
                setTimer();
                loanBtn.setOnClickListener(this);

            }else{
                if (message.equals("验证失败")){
                    bigTitle.setText("验证失败");
                }else{
                    bigTitle.setText("录入失败");
                }
                loanBtn.setVisibility(View.INVISIBLE);
            }
            recheckBtn.setOnClickListener(this);
            loadingView.refreshView(view);

        }
    }



    /**
     * 保存用户信息
     * @param readerId
     * @param readerName
     */
    private void saveUserData(String readerId,String readerName){
        Constant.readerId=readerId;
        Constant.readerName=readerName;
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
                message.what = 1;
                handler.sendMessage(message);
            }
        };
        //延时0s后执行，1s执行一次
        timer.schedule(task, 0, 1000);
    }

    /**
     * 计时回调
     */
    final Handler handler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    //修改对应操作text
                    changeTextContent();
                    //如果倒计时结束
                    if (time==0){
                        startToNextActivity();
                        timer.cancel();
                    }
                    time--;
                    break;
                case 1000:
                    if(mBarcode != null && !mBarcode.equals("") && !IS_DO_ACTION){
                        stateAction(LOADING_STATE);
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };

    /**
     * 改变text
     */
    private void changeTextContent(){
        if (readerAction== Constant.LOAN_ACTION) {
            loanBtn.setText(time + "s 后借书");
        }
        else if(readerAction== Constant.RETURN_ACTION){
            loanBtn.setText(time + "s 后还书");
        }
        else if (readerAction== Constant.RELOAN_ACTION) {
            loanBtn.setText(time + "s 后续借");
        }
        else if (readerAction ==Constant.OWN_ACTION){
            loanBtn.setText(time + "s 后查看借阅信息");
        }
    }


    /**
     * 跳转到下一个页面
     */
    private void startToNextActivity(){
        if (readerAction== Constant.LOAN_ACTION) {
            startActivity(LoanActivity.class, 0);
        }
        else if (readerAction== Constant.RETURN_ACTION) {

            startActivity(ReturnActivity.class, 0);

        }else if (readerAction== Constant.RELOAN_ACTION) {

            startActivity(ReLoanActivity.class, 0);

        }else if (readerAction== Constant.OWN_ACTION){

            Intent intent = new Intent();
            intent.setClass(this, ReLoanActivity.class);
            intent.putExtra("OWN",1);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
        }
        finish();
    }

    /**
     * 关闭覆盖层
     */
    public void disappearShade(){
        loadingView.startDisappearAnimation();
        loadingView.getDisappearAnimation().setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                layout.removeView(loadingView);
                loadingView = null;
                operaTV.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.VISIBLE);
                rlNoCardInput.setVisibility(View.VISIBLE);
                IS_DO_ACTION = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {


            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ll_activity_title_backpress:

                saveUserData(null, null);
                finish();
                overridePendingTransition(R.anim.slide_left_ins, R.anim.slide_right_outs);
                break;

            case R.id.recheckBtn:
                if(VersionSetting.IS_P2000L_DEVICE ){
                    readerCardRun = true;
                    oldBarcode = "";
                    executeReadCard();
                }
                stateAction(COMPLETE_STATE);
                break;

            case R.id.loanBtn:
                timer.cancel();
                startToNextActivity();
                break;

            case R.id.ll_activity_title_scan:
                if(VersionSetting.IS_P2000L_DEVICE ){
                    readerCardRun = false;
                    oldBarcode = "";
                }

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {

                    //申请WRITE_EXTERNAL_STORAGE权限
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0);

                }
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent();
                    intent.setClass(this, CaptureActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_SCAN);
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
                }
                break;
            case R.id.rlNoCardInput:
                if(VersionSetting.IS_P2000L_DEVICE ){
                    readerCardRun = false;
                    oldBarcode = "";
                }
                Intent intent = new Intent(this,NoCardInputActivity.class);
                startActivityForResult(intent,NO_CARD_REQUEST_CODE);
                break;
            case R.id.btnNoCardCheck:
                inputMethodManager.hideSoftInputFromInputMethod(etInput.getWindowToken(),0);
                String readerId = etInput.getText().toString().trim();
                mBarcode =readerId;
                if (mBarcode !=null&&!IS_DO_ACTION){

                    stateAction(LOADING_STATE);
                }
                break;
        }

    }

    /**
     * 跳转页面
     * @param activityClass 目标页面Class
     */
    public void startActivity(Class activityClass,int Flag){
        Intent intent = new Intent();
        intent.setClass(this, activityClass);
        startActivity(intent);
        if (Flag==1)
            overridePendingTransition(R.anim.slide_left_ins, R.anim.slide_right_outs);
        else
            overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
    }


    //按下返回按钮
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            saveUserData(null,null);
            startActivity(MainActivity.class,1);
            finish();
            return true;
        } else
            return super.onKeyDown(keyCode, event);
    }

    //回调接收
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
            if (data != null) {
                String str = data.getStringExtra("codedContent");
                Log.d("test","IntentScanValue："+str);
                if(str.contains(":")){
                    String[] str2 = str.split(":");
                    str = str2[0];
                }
                mBarcode =str;
                if (mBarcode !=null&&!IS_DO_ACTION){
                    stateAction(LOADING_STATE);
                }
            }
        }


        //输入读者账号密码进行验证
        if(requestCode == NO_CARD_REQUEST_CODE && resultCode == RESULT_OK){
            if(VersionSetting.IS_P2000L_DEVICE ){
                readerCardRun = false;
                oldBarcode = "";
            }

            mBarcode = data.getStringExtra("ReadId");
            Log.d(TAG,mBarcode);
            if (mBarcode !=null&&!IS_DO_ACTION){
                stateAction(LOADING_STATE);

            }
        }
    }


    //进行读读者卡的操作
    private void executeReadCard(){
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {

                return p2000LGetBarcode();
            }

            @Override
            protected void onPostExecute(String barcode) {

                if(!barcode.equals("") && !IS_DO_ACTION){

                    mBarcode = barcode;
                    stateAction(LOADING_STATE);

                }else{

                    Log.d("test","读读者证执行结束");
                }
            }

        }.execute();

    }

    //p2000l设备读卡
    private String p2000LGetBarcode(){

        String barcode = "";
        int ret = 0;

        while (readerCardRun) {
            try {
                PosCardManager mPosCardManager = PosCardManager.getDefault();
                PosByteArray b_data0 = new PosByteArray();
                ret = mPosCardManager.open(PosCardManager.POSCARD_READER_CATEGORY_PICC, null);
                Log.d("test3", "open:" + ret);

                ret = mPosCardManager.ViccCardTransmitCmd(0x05, new byte[]{0x02, 0x01}, b_data0);
                Log.d("test3", "ViccCardTransmitCmd:" + ret);
                if (ret != 0) {
                    mPosCardManager.close();
                    Log.d("test3", "ViccCardTransmitCmd:error  AND  close");

                } else {
                    String incer = StringToOther.bcdToString(b_data0.buffer).substring(4, StringToOther.bcdToString(b_data0.buffer).length());
                    byte[] sneddata = new byte[]{0x20, 0x25};
                    byte[] sendata = null;
                    sendata = StringToOther.byteMerger(sneddata, BCDHelper.StrToBCD(incer));
                    Log.d("test3", "b_data0:" + StringToOther.bcdToString(sendata));

                    //Select
                    ret = mPosCardManager.ViccCardTransmitCmd(0x05, sendata, b_data0);
                    Log.d("test3", "Select:" + ret);

                    if(ret == 0){

                        //读取前四个扇区数据
                        ret = mPosCardManager.ViccCardTransmitCmd(0x05, new byte[]{0x10, 0x23, 0x00, 0x03}, b_data0);
                        Log.d("test3", "ReadCardBlock:" + ret);

                        if(ret == 0 && b_data0.buffer != null) {
                            //得到barcdoe号
                            barcode = StringToOther.bcdToString(b_data0.buffer).substring(2);

                            //停止运行卡片
                            byte[] quiet = new byte[]{0x20, 0x02};
                            byte[] quietdata = StringToOther.byteMerger(quiet, BCDHelper.StrToBCD(incer));
                            ret = mPosCardManager.ViccCardTransmitCmd(0x05, quietdata, b_data0);
                            Log.d("test3", "stopRun:" + ret);
                            mPosCardManager.close();

                            Log.d("test3", "barcode:" + barcode);
                            boolean isLoanSuc = ExCheBarcode.loadRule(barcode);
                            if (!isLoanSuc) {
                                Log.d("test3", "失败转换barcode数据");
                            } else {
                                barcode = ExCheBarcode.getEncodeBarcode();
                                Log.d("test3", "barcode:" + barcode);
                            }

                            if (!barcode.equals(oldBarcode)) {
                                oldBarcode = barcode;
                                mPosCardManager.close();
                                return barcode;
                            }

                        }else{

                            //停止运行卡片
                            byte[] quiet = new byte[]{0x20, 0x02};
                            byte[] quietdata = StringToOther.byteMerger(quiet, BCDHelper.StrToBCD(incer));
                            ret = mPosCardManager.ViccCardTransmitCmd(0x05, quietdata, b_data0);
                            Log.d("test3", "stopRun:" + ret);
                            mPosCardManager.close();

                        }

                    }else{

                        //停止运行卡片
                        byte[] quiet = new byte[]{0x20, 0x02};
                        byte[] quietdata = StringToOther.byteMerger(quiet, BCDHelper.StrToBCD(incer));
                        ret = mPosCardManager.ViccCardTransmitCmd(0x05, quietdata, b_data0);
                        Log.d("test3", "stopRun:" + ret);
                        mPosCardManager.close();

                    }

                }

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return "";
    }



    @Override
    protected void onDestroy() {
        if(VersionSetting.IS_P2000L_DEVICE ){
            readerCardRun = false;
            oldBarcode = "";
        }
        super.onDestroy();
    }
}
