package com.tc.nfc.app.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tc.api.VersionSetting;
import com.tc.nfc.R;
import com.tc.nfc.app.adapter.BookLoanRecyclerAdapter;
import com.tc.nfc.app.utils.ToastUtil;
import com.tc.nfc.app.utils.nfcV.NfcVUtil;
import com.tc.nfc.app.view.HintDialog;
import com.tc.nfc.app.view.SlideFromBottomPopup;
import com.tc.nfc.util.Constant;
import com.tc.nfc.util.NFCApplication;
import com.tc.nfc.app.view.LoadingView;
import com.tc.nfc.app.view.RecycleViewDivider;
import com.tc.nfc.core.listener.ActionCallbackListener;
import com.tc.nfc.util.PrinterUtil;
import com.tomergoldst.tooltips.ToolTip;
import com.tomergoldst.tooltips.ToolTipsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tplusr.scanlibrary.zxing.android.CaptureActivity;


public class ReturnActivity extends BaseActivity implements View.OnClickListener{

    private final String TAG ="ReturnActivity";
    public NFCApplication mNFCApplication;
    private ReturnActivity that;
    private RelativeLayout relativeLayout;
    private LinearLayout layout = null;
    private View view;
    private TextView titleTV,operaTV;
    private ImageView imageView;
    private LinearLayout backImage,scan;

    //setting
    private LinearLayout llPrinter = null;

    private static final int REQUEST_CODE_SCAN = 0x0000;

    //图书条形码
    private String barcode;

    //数据与适配器、列表
    private List<Map<String, Object>> mdatas;
    private RecyclerView recyclerView;
    private BookLoanRecyclerAdapter recycleAdapter;

    //messageWindow管理
    ToolTipsManager mToolTipsManager;
    ToolTip.Builder builder=null;
    //上一被选择按钮的数据
    private Map<String, Object> lastData =null;

    //loading
    private LoadingView loadingView =null;

    //标志是否正在还书，如果正在还书，则即使扫描到条码也不操作
    private boolean IS_DO_ACTIO=false;

    //弹窗提醒
    HintDialog hintDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();

    }

    @Override
    protected void initContentView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_scan);
        mNFCApplication =(NFCApplication)this.getApplication();

        //开始进行子线线程读卡
        if(VersionSetting.IS_P2000L_DEVICE && VersionSetting.IS_PRINTER){
            NfcVUtil.setReadCardRun(true);
            p2000LReadCard();
        }

    }

    /**
     * 初始化
     */
    public void initViews(){
        if(VersionSetting.IS_P2000L_DEVICE){
            llPrinter = (LinearLayout) findViewById(R.id.ll_activity_title_setting);
            llPrinter.setVisibility(View.VISIBLE);
            llPrinter.setOnClickListener(this);
        }

        titleTV =(TextView)findViewById(R.id.tv_title_activity);
        titleTV.setText(R.string.returnbook);
        operaTV =(TextView)findViewById(R.id.operaText);
        operaTV.setText(R.string.scanbook_operaTV);
        imageView=(ImageView)findViewById(R.id.operaImage);
        imageView.setImageDrawable(getResources().getDrawable(R.drawable.checkbook_icon));
        backImage = (LinearLayout)findViewById(R.id.ll_activity_title_backpress);
        scan = (LinearLayout)findViewById(R.id.ll_activity_title_scan);
        backImage.setOnClickListener(this);
//        scan.setVisibility(View.VISIBLE);
        scan.setOnClickListener(this);
        layout =(LinearLayout)findViewById(R.id.trendLayout);
        relativeLayout =(RelativeLayout)findViewById(R.id.contentLayout);
        mdatas =new ArrayList<>();
        that=this;
        mToolTipsManager = new ToolTipsManager();

        loadingView =new LoadingView(this,"正在还书,请勿移动手机...");
    }


    //p2000l读卡
    private void p2000LReadCard(){

        mNFCApplication.getBookAction().p2000LGetBarcode(new ActionCallbackListener<String>() {
            @Override
            public void onSuccess(String data) {
                Log.d(TAG,"data："+data);
                barcode = data;
                if (barcode != null && !IS_DO_ACTIO) {
                    //执行借书操作
                    returnBook();
                }
            }

            @Override
            public void onFailure(String errorEvent, String message) {
                if(NfcVUtil.isReadCardRun() == true) {
                    ToastUtil.showToastShortTime(ReturnActivity.this, message);
                }else{
                    ToastUtil.showToastShortTime(ReturnActivity.this, "已退出还书");
                }
            }
        });

    }



    /**
     * 接受NFC请求
     * @param intent
     */
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        mNFCApplication.getBookAction().getNFCBarcode(intent, new ActionCallbackListener<String>() {
            @Override
            public void onSuccess(String data) {
                barcode =data;
                if (barcode !=null&&!IS_DO_ACTIO){
                    //执行还书操作
                    returnBook();
                }
            }

            @Override
            public void onFailure(String errorEvent, String message) {
                ToastUtil.showToastShortTime(ReturnActivity.this,message);
            }
        });
    }


    public void setListener(){
        recycleAdapter.setAdapterListener(new BookLoanRecyclerAdapter.CheckOnClickListener() {

            public void onClick(View v, int position, Map<String, Object> data) {
                //改变按钮选中状态
                Boolean isCheck = !(boolean) data.get("isCheck");

                closeMessageWindow();
                //判断当前按钮状态，如果为true被点击则新建message框
                if (isCheck) {

                    builder = new ToolTip.Builder(that, v, relativeLayout
                            , data.get("message").toString(), ToolTip.POSITION_ABOVE);
                    builder.setGravity(ToolTip.GRAVITY_CENTER);
                    builder.setAlign(ToolTip.ALIGN_RIGHT);

                    mToolTipsManager.show(builder.build());
                    data.put("isCheck", isCheck);
                    lastData = data;
                }
            }

            //当adapter被刷新时关闭message框并且改变状态
            public void reFresh() {
                closeMessageWindow();
            }
        });
    }

    public void returnBook(){
        //显示加载层
        appearShade();
        //还书
        mNFCApplication.getBookAction().returnBook(barcode, new ActionCallbackListener<Map<String, Object>>() {

            @Override
            public void onSuccess(Map<String, Object> data) {
                if(VersionSetting.IS_UPLOAD_LOG && data.get("LoanResult").toString().equals(true) ) {
                    //提交还书日志
                    uploadRetureBookLog(Constant.LOG_USER,Constant.LOG_PASSWORD,Constant.machineId, Constant.readerId, Constant.readerName, barcode, data, "2");
                }
                //生出列表view
                changeView();
                //6.判断该图书条码号是否已经存在在当前列表中，否则增加一行
                isExistData(data);
                //7.关闭加载层，完成还书
                disappearShade();
            }

            @Override
            public void onFailure(String errorEvent, String message) {
                ToastUtil.showToastShortTime(ReturnActivity.this,message);
                disappearShade();
                //账号验证过去，重新登陆
                if(message.contains("账号验证过期")){
                    finish();
                    login();
                }
            }
        });
    }

    /**
     * 生成recyclerView
     */
    public void changeView(){

        if(view==null){
            layout.removeView(operaTV);
            layout.removeView(imageView);

            LayoutInflater flater = LayoutInflater.from(this);

            view = flater.inflate(R.layout.view_list, null);
            recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);

            recycleAdapter= new BookLoanRecyclerAdapter(this, mdatas, Constant.RETURN_ACTION);

            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            //设置布局管理器
            recyclerView.setLayoutManager(layoutManager);
            //设置为垂直布局，这也是默认的
            layoutManager.setOrientation(OrientationHelper.VERTICAL);
            //设置Adapter
            recyclerView.setAdapter(recycleAdapter);
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    closeMessageWindow();
                }
            });
            //设置增加或删除条目的动画
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.addItemDecoration(new RecycleViewDivider(
                    this, LinearLayoutManager.VERTICAL, 30, Color.parseColor("#F8F8F8")));

            layout.addView(view);
            setListener();

        }

    }

    /**
     * 判断借阅号是否存在
     * @param data
     * @return
     */
    public void isExistData(Map<String, Object> data){

        for (int i=0;i<mdatas.size();i++){

            if (mdatas.get(i).get("barcode").equals(data.get("barcode"))) {
                mdatas.set(i, data);
                recycleAdapter.notifyDataSetChanged();
                closeMessageWindow();
                return;
            }
        }
        addData(data);
    }

    /**
     * 为列表增加一行数据
     * @param data
     */
    public void addData( Map<String, Object> data) {

        mdatas.add(0,data);
        recycleAdapter.notifyItemInserted(0);
        recycleAdapter.notifyItemRangeChanged(0, data.size());
        recyclerView.smoothScrollToPosition(0);
    }

    /**
     * 关闭MessageWindow
     */
    public void closeMessageWindow(){
        //如果该页面上有message框则关闭，并改变打开message框的data的按钮状态为false
        if (builder != null) {
            mToolTipsManager.findAndDismiss(builder.build().getAnchorView());
            //改变打开message框的data的按钮状态
            lastData.put("isCheck", false);
            builder = null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ll_activity_title_backpress:
                //直接连列interlib3的处理
                if(VersionSetting.IS_CONNECT_INTERLIB3){

                    if (!VersionSetting.IS_ADMIN){
                        startActivity(MainActivity.class);
                        this.finish();
                        overridePendingTransition(R.anim.slide_left_ins, R.anim.slide_right_outs);
                    }else {
                        new SlideFromBottomPopup(ReturnActivity.this,this).showPopupWindow();
                    }

                }else {
                    this.finish();
                    overridePendingTransition(R.anim.slide_left_ins, R.anim.slide_right_outs);
                }
                break;

            case R.id.other_btn:
                startActivity(MainActivity.class);
                this.finish();
                break;

            case R.id.change_btn:

                startActivity(CheckReaderActivity.class);
                this.finish();
                break;

            case R.id.ll_activity_title_scan:
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
            case R.id.ll_activity_title_setting:
                //打印操作
                int num = 0;
                for(int x=0;x<mdatas.size();x++){
                    if(mdatas.get(x).get("LoanResult").equals(true)){
                        num++;
                    }
                }
                if(num>0){
                    showHintDialog("提示","成功归还"+"["+num+"]"+"本书，是否打印凭条？");

                }else{
                    ToastUtil.showToastShortTime(this,"没有书籍归还成功");
                }
                break;
        }
    }

    /**
     * 取消loading
     */
    public void disappearShade(){

        loadingView.startDisappearAnimation();
        loadingView.getDisappearAnimation().setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                relativeLayout.removeView(loadingView);
                operaTV.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.VISIBLE);
                IS_DO_ACTIO=false;
                if(VersionSetting.IS_P2000L_DEVICE){
                    p2000LReadCard();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {


            }
        });
    }

    public void appearShade(){
        IS_DO_ACTIO=true;
        operaTV.setVisibility(View.INVISIBLE);
        imageView.setVisibility(View.INVISIBLE);
        relativeLayout.addView(loadingView);
        loadingView.startAppearAnimation();
    }

    /**
     *
     * @param title
     * @param tipMessage
     */
    private void showHintDialog(String title, final String tipMessage){
        if(hintDialog == null){
            hintDialog = new HintDialog(this,title,tipMessage);
        }
        hintDialog.showDialog(R.layout.dialog_normals_layout, new HintDialog.IHintDialog() {
            @Override
            public void showWindowDetail(Window window, String title, String message) {
                final TextView tvMessage = (TextView) window.findViewById(R.id.message);
                tvMessage.setText(tipMessage);

                final TextView tvSure = (TextView) window.findViewById(R.id.positiveButton);
                final TextView tvCannel = (TextView) window.findViewById(R.id.negativeButton);

                tvCannel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        hintDialog.dismissDialog();
                    }
                });

                tvSure.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PrinterUtil.printer(mdatas,null,TAG);
                        hintDialog.dismissDialog();

                    }
                });
            }
        });
    }


    /**
     * 跳转页面
     * @param activityClass 目标页面Class
     */
    public void startActivity(Class activityClass){
        Intent intent = new Intent();
        intent.putExtra("readerAction", Constant.RETURN_ACTION);
        intent.setClass(this, activityClass);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_left_ins, R.anim.slide_right_outs);
    }

    //点击回退按钮
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        //连接interlib3接口的处理
        if(VersionSetting.IS_CONNECT_INTERLIB3){

            if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
                if (!VersionSetting.IS_ADMIN){
                    if (!IS_DO_ACTIO){
                        this.finish();
                        overridePendingTransition(R.anim.slide_left_ins, R.anim.slide_right_outs);
                    }
                }else {
                    new SlideFromBottomPopup(this,this).showPopupWindow();
                }
                return true;
            } else {
                return super.onKeyDown(keyCode, event);
            }

        }else {

            if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
                startActivity(MainActivity.class);
                this.finish();
                return true;
            } else {
                return super.onKeyDown(keyCode, event);
            }
        }

    }


    //扫码回调接收
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
            if (data != null) {
                Bundle b=data.getExtras(); //data为B中回传的Intent
                String str=b.getString("codedContent");//str即为回传的值
                Log.d("XX", str);
                barcode =str;
                if (barcode !=null&&!IS_DO_ACTIO){
                    //3.执行还书操作
                    returnBook();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        if(VersionSetting.IS_P2000L_DEVICE){
            NfcVUtil.setP2000LOldBarcode("");
            NfcVUtil.setReadCardRun(false);
        }
        super.onDestroy();
    }




}
