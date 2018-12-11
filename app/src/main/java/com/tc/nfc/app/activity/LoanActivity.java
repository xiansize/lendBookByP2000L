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
import com.tc.nfc.util.Constant;
import com.tc.nfc.util.NFCApplication;
import com.tc.nfc.app.view.LoadingView;
import com.tc.nfc.app.view.RecycleViewDivider;
import com.tc.nfc.app.view.SlideFromBottomPopup;
import com.tc.nfc.core.listener.ActionCallbackListener;
import com.tc.nfc.util.PrinterUtil;
import com.tomergoldst.tooltips.ToolTip;
import com.tomergoldst.tooltips.ToolTipsManager;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import tplusr.scanlibrary.zxing.android.CaptureActivity;

public class LoanActivity extends BaseActivity implements View.OnClickListener{

    private final String TAG ="LoanActivity";

    public NFCApplication mNFCApplication;

    private View view;
    private LoanActivity that;
    //加载loadingView、messageWindow的层
    private RelativeLayout contentLayout;
    //加载列表recycleView的层
    private LinearLayout layout = null;
    private TextView titleTV,operaTV;
    private ImageView operaImage;
    //返回按钮
    private LinearLayout backImage,scan;
    //loading
    private LoadingView loadingView =null;

    //setting
    private LinearLayout llPrinter = null;

    //数据与适配器、列表
    private List<Map<String, Object>> mdatas;
    private RecyclerView recyclerView;
    private BookLoanRecyclerAdapter recycleAdapter;


    private static final int REQUEST_CODE_SCAN = 0x0000;
    //图书条形码
    private String barcode;
    //messageWindow管理
    ToolTipsManager mToolTipsManager;
    ToolTip.Builder builder=null;
    //上一被选择按钮的数据
    private Map<String, Object> lastData =null;
    //标志是否正在借书，如果正在借书，则即使扫描到条码也不操作
    private boolean IS_DO_ACTIO=false;

    //弹窗提醒
    HintDialog hintDialog;


//    /**
//     * 多本录入附加
//     */
//    private LinearLayout bluetool_setting;
//
//    //用设备扫描时的动作
//    private static final int INVENTORY_MSG = 1;
//    //扫描失败
//    private static final int INVENTORY_FAIL_MSG = 4;
//    //结束扫描动作
//    private static final int THREAD_END = 3;
//    //打开设备成功
//    public static final int OPEN_SUCCESS =11;
//    //打开设备失败
//    public static final int OPEN_FAIL =12;
//    //是否单本录入
//    private boolean IS_SINGLE =false;
//    //下拉框
//    private MaterialSpinner bluetool_sp;
//    //蓝牙名字list
//    ArrayList<CharSequence> m_bluetoolNameList;
//    //连接按钮
//    private Button conn_btn;
//
//    private ADReaderInterface m_reader = new ADReaderInterface();
//    //盘点标签线程
//    private Thread m_inventoryThrd = null;
//    private ISO15693Interface mTag = new ISO15693Interface();
//    //盘点结果list
//    private List<InventoryReport> inventoryList = new ArrayList<InventoryReport>();




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();

    }



    @Override
    protected void initContentView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_scan);
        mNFCApplication =(NFCApplication)this.getApplication();

        //开始读卡，读到卡就进行借书的操作
        if(VersionSetting.IS_P2000L_DEVICE){
            NfcVUtil.setReadCardRun(true);
            p2000LReadCard();
        }
    }

    /**
     * 初始化
     */
    public void initViews(){

        if(VersionSetting.IS_P2000L_DEVICE && VersionSetting.IS_PRINTER){
            llPrinter = (LinearLayout) findViewById(R.id.ll_activity_title_setting);
            llPrinter.setVisibility(View.VISIBLE);
            llPrinter.setOnClickListener(this);
        }

        titleTV =(TextView)findViewById(R.id.tv_title_activity);
        titleTV.setText(R.string.loanbook);
        operaTV =(TextView)findViewById(R.id.operaText);
        operaTV.setText(R.string.scanbook_operaTV);
        operaImage=(ImageView)findViewById(R.id.operaImage);
        operaImage.setImageDrawable(getResources().getDrawable(R.drawable.checkbook_icon));
        backImage = (LinearLayout)findViewById(R.id.ll_activity_title_backpress);
        backImage.setOnClickListener(this);
//        scan = (LinearLayout)findViewById(R.id.scan);
//        scan.setOnClickListener(this);
//        scan.setVisibility(View.VISIBLE);
        layout =(LinearLayout)findViewById(R.id.trendLayout);
        contentLayout =(RelativeLayout)findViewById(R.id.contentLayout);
        mdatas =new ArrayList<>();
        that=this;
        mToolTipsManager = new ToolTipsManager();
        loadingView =new LoadingView(this,"正在借书，请勿移动手机...");


//        //是管理员才显示设备连接界面
//        if (VersionSetting.IS_ADMIN){
//            bluetool_setting =(LinearLayout)findViewById(R.id.bluetool_setting);
//            bluetool_setting.setVisibility(View.VISIBLE);
//            conn_btn =(Button)findViewById(R.id.conn_btn);
//            bluetool_sp = (MaterialSpinner)findViewById(R.id.bluetool_sp);
//            ShowSetting();
//            conn_btn.setOnClickListener(this);
//        }
//
    }


//    /**
//     * 配置连接界面信息
//     */
//    private void ShowSetting(){
//
//
//        // 列举已配对的蓝牙设备
//        m_bluetoolNameList = null;
//
//        m_bluetoolNameList = new ArrayList<CharSequence>();
//        ArrayList<BluetoothCfg> m_blueList = ADReaderInterface.GetPairBluetooth();
//        if (m_blueList != null)
//        {
//            for (BluetoothCfg bluetoolCfg : m_blueList)
//            {
//                m_bluetoolNameList.add(bluetoolCfg.GetName());
//            }
//        }
//
//        bluetool_sp.setItems(m_bluetoolNameList);
//        bluetool_sp.setTextColor(Color.parseColor("#605f5f"));
//
//    }


    //p2000l读卡
    private void p2000LReadCard(){

        mNFCApplication.getBookAction().p2000LGetBarcode(new ActionCallbackListener<String>() {
            @Override
            public void onSuccess(String data) {
                Log.d(TAG,"data："+data);
                if (data != null && !IS_DO_ACTIO) {
                    //执行借书操作
                    barcode = data;
                    loanBook();
                }
            }

            @Override
            public void onFailure(String errorEvent, String message) {
                if(NfcVUtil.isReadCardRun() == true) {
                    ToastUtil.showToastShortTime(LoanActivity.this, message);
                }else{
                    ToastUtil.showToastShortTime(LoanActivity.this, "已退出借书");
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

        //解析获取Barcode
        mNFCApplication.getBookAction().getNFCBarcode(intent, new ActionCallbackListener<String>() {
            @Override
            public void onSuccess(String data) {
                barcode = data;
//              IS_SINGLE = true;
                if (barcode != null && !IS_DO_ACTIO) {
                    //执行借书操作
                    loanBook();
                }
            }

            @Override
            public void onFailure(String errorEvent, String message) {

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

                    builder = new ToolTip.Builder(that, v, contentLayout
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

    /**
     * 先判断是否可以借书，再进行借书操作
     */
    public void loanBook(){

        //只有读者模式下
//        if (IS_SINGLE)
        appearShade();

        mNFCApplication.getBookAction().isBookCanLoadn(Constant.readerId, barcode, new ActionCallbackListener<Map<String, Object>>() {
            //可借
            public void onSuccess(Map<String, Object> data) {
                //生出列表view
                changeView();
                if (data == null) {
                    //借书
                    mNFCApplication.getBookAction().loanBook(Constant.readerId, barcode, new ActionCallbackListener<Map<String, Object>>() {
                        @Override
                        public void onSuccess(Map<String, Object> data) {

                            //判断该图书条码号是否已经存在在当前列表中，否则增加一行
                            isExistData(data);
                            //提交借书日志
                            if(VersionSetting.IS_UPLOAD_LOG && data.get("LoanResult").toString().equals(true)) {

                                uploadLoanBookLog(Constant.LOG_USER,Constant.LOG_PASSWORD,Constant.userName, Constant.readerId, Constant.readerName, barcode, data, "1");
                            }
                            //关闭加载层，完成借书
                            disappearShade();
                        }

                        @Override
                        public void onFailure(String errorEvent, String message) {
                            ToastUtil.showToastShortTime(LoanActivity.this,message);
                            disappearShade();
                            //账号验证过期，重新登陆
                            if(message.contains("账号验证过期")){
                                finish();
                                login();
                            }
                        }
                    });
                } else {
                    isExistData(data);
                    disappearShade();
                }

            }

            //不可借
            public void onFailure(String errorEvent, String message) {
                ToastUtil.showToastShortTime(LoanActivity.this,message);
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
            layout.removeView(operaImage);
            LayoutInflater flater = LayoutInflater.from(this);

            view = flater.inflate(R.layout.view_list, null);
            recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);

            recycleAdapter= new BookLoanRecyclerAdapter(this, mdatas,Constant.LOAN_ACTION);

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
     * 判断借阅号是否存在于list表当中
     * @param data
     * @return true:更新旧item内容 false:增加新item
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
                //显示窗口

                if (!VersionSetting.IS_ADMIN){
                    startActivity(MainActivity.class);
                    this.finish();
                    overridePendingTransition(R.anim.slide_left_ins, R.anim.slide_right_outs);
                }else {
                    new SlideFromBottomPopup(LoanActivity.this,this).showPopupWindow();
                }
                break;

            case R.id.other_btn://对现在的读者进行其它项目的操作
                startActivity(MainActivity.class);
                this.finish();
                break;

            case R.id.change_btn:
                startActivity( CheckReaderActivity.class);
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
                    showHintDialog("提示","成功借取"+"["+num+"]"+"本书，是否打印凭条？");

                }else{
                    ToastUtil.showToastShortTime(this,"没有书籍借取成功");
                }
                break;

//            case R.id.conn_btn:
//
//                if (conn_btn.getText().equals("连接设备")){
//
//
//                    conn_btn.setBackgroundResource(R.drawable.not_selected_btn);
//                    conn_btn.setEnabled(false);
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//
//                            if (ConnectBlueTool()){
//                                Message msg = Message.obtain();
//                                msg.what = InventoryActivity.OPEN_SUCCESS;
//                                mHandler.sendMessage(msg);
//                            }else{
//                                Message msg = Message.obtain();
//                                msg.what = InventoryActivity.OPEN_FAIL;
//                                mHandler.sendMessage(msg);
//                            }
//                        }
//                    }).start();
//
//                }
//                else{
//
//                    mTag.ISO15693_Disconnect();
//                    b_inventoryThreadRun = false;
//                    m_reader.RDR_SetCommuImmeTimeout();
//                    m_reader.RDR_Close();
//                    conn_btn.setText("连接设备");
//                }
//
//                break;
        }
    }

//    /**
//     * 连接蓝牙
//     */
//    private Boolean ConnectBlueTool(){
//        String conStr = "";
//
//        String devName ="RPAN";
//        String connTypeStr ="蓝牙";
//        if (connTypeStr.equals("蓝牙"))
//        {
//            if (bluetool_sp.getItems().isEmpty())
//            {
//                Log.d("选择设备", "请选择蓝牙设备！");
//                return false;
//            }
//            String bluetoolName = m_bluetoolNameList.get(bluetool_sp.getSelectedIndex()).toString();
//            if (bluetoolName == "")
//            {
//                Log.d("选择设备", "请选择蓝牙设备！");
//                return false;
//            }
//            conStr = String.format("RDType=%s;CommType=BLUETOOTH;Name=%s",
//                    devName, bluetoolName);
//
//            if (m_reader.RDR_Open(conStr) == ApiErrDefinition.NO_ERROR) {
//
//                Log.d("XX","成功");
//                return true;
//            }
//            else
//            {
//                Log.d("XX","失败");
//                return false;
//            }
//
//
//        }
//
//        return false;
//    }

//    /**
//     * 回调接收
//     */
//    private Handler mHandler = new MyHandler(this);
//
//    private static class MyHandler extends Handler
//    {
//        private final WeakReference<LoanActivity> mActivity;
//
//        public MyHandler(LoanActivity activity)
//        {
//            mActivity = new WeakReference<LoanActivity>(activity);
//        }
//
//        @Override
//        public void handleMessage(Message msg)
//        {
//            LoanActivity pt = mActivity.get();
//            if (pt == null)
//            {
//                return;
//            }
//            boolean b_find = false;
//            switch (msg.what)
//            {
//                case INVENTORY_MSG:// 盘点到标签
//                    @SuppressWarnings("unchecked")
//                    Vector<ISO15693Tag> tagList = (Vector<ISO15693Tag>) msg.obj;
//                    for (int i = 0; i < tagList.size(); i++)
//                    {
//
//                        b_find = false;
//                        ISO15693Tag tagData = tagList.get(i);// (ISO15693Tag)
//                        // msg.obj;
//                        String uidStr = GFunction.encodeHexStr(tagData.uid);
//                        for (int j = 0; j < pt.inventoryList.size(); j++)
//                        {
//
//                            InventoryReport mReport = pt.inventoryList.get(j);
//                            if (mReport.getUidStr().equals(uidStr))
//                            {
//                                mReport.setFindCnt(mReport.getFindCnt() + 1);
//                                b_find = true;
//                                break;
//                            }
//                        }
//                        if (!b_find)
//                        {
//
//                            String tagName = ISO15693Interface
//                                    .GetTagNameById(tagData.tag_id);
//                            String barcode;
//
//                            //解析出barcode，并且传到服务器清点
//                            if (!(barcode=pt.PraseTag(tagData.uid)).equals("")){
//                                pt.inventoryList.add(new InventoryReport(uidStr, tagName,barcode));
//                                pt.loanBook();
//                            }
//
//
//                        }
//                    }
//                    //Log.d("XX","标签总数:" + pt.inventoryList.size() + ";失败次数:" + msg.arg1);
//
//                    break;
//                case INVENTORY_FAIL_MSG:
//                    //Log.d("XX","标签总数:" + pt.inventoryList.size() + ";失败次数:" + msg.arg1);
//                    break;
//
//                case THREAD_END:// 线程结束
//                    Log.d("XX","结束");
//                    break;
//
//                case OPEN_SUCCESS:
//                    Toast.makeText(pt, "打开设备成功！", Toast.LENGTH_SHORT).show();
//                    pt.conn_btn.setText("停止");
//                    pt.conn_btn.setBackgroundResource(R.drawable.purchase_btn_selector);
//                    pt.conn_btn.setEnabled(true);
//                    pt.Invertory();
//                    break;
//                case OPEN_FAIL:
//                    Toast.makeText(pt, "打开设备失败！", Toast.LENGTH_SHORT).show();
//                    pt.conn_btn.setBackgroundResource(R.drawable.purchase_btn_selector);
//                    pt.conn_btn.setEnabled(true);
//                    break;
//                default:
//                    break;
//            }
//        }
//    }

//    /**
//     * 扫描现场开启
//     */
//    private void Invertory(){
//        m_inventoryThrd = new Thread(new InventoryThrd());
//        m_inventoryThrd.start();
//    }

//    /**
//     * 连接标签
//     * @param connectUid
//     * @return
//     */
//    private String PraseTag(byte connectUid[] ){
//        byte connectMode = 1;
//        if (connectMode == 1 && connectUid == null)
//        {
//            Log.d("XX","UID不能为空");
//            return "";
//        }
//        int iret = mTag.ISO15693_Connect(m_reader,
//                RfidDef.RFID_ISO15693_PICC_ICODE_SLI_ID, connectMode,
//                connectUid);
//        if (iret != ApiErrDefinition.NO_ERROR)
//        {
//            Log.d("XX", "连接标签失败");
//        }
//        return UiReadBlock();
//    }

//    /**
//     * 解析标签
//     * @return
//     */
//    private String UiReadBlock()
//    {
//        //从第几个扇区开始
//        int blkAddr = 0;
//        int numOfBlksToRead = 4;
//        if (blkAddr + numOfBlksToRead > 28)// 数据块地址溢出
//        {
//            numOfBlksToRead = 28 - blkAddr;
//        }
//        Integer numOfBlksRead = 0;
//        Long bytesBlkDatRead = (long) 0;
//        byte bufBlocks[] = new byte[4 * numOfBlksToRead];
//        int iret = mTag.ISO15693_ReadMultiBlocks(false, blkAddr,
//                numOfBlksToRead, numOfBlksRead, bufBlocks, bytesBlkDatRead);
//        if (iret != ApiErrDefinition.NO_ERROR)
//        {
//            Log.d("XX", "读取数据块失败");
//            return "";
//
//        }
//        String strData = GFunction.encodeHexStr(bufBlocks);
//
//        boolean isLoanSuc = ExCheBarcode.loadRule(strData);
//        if (!isLoanSuc) {
//            Log.d("XX","失败");
//            return "";
//        }
//
//        barcode = ExCheBarcode.getEncodeBarcode();
//        Log.d("XX", "数据块内容:"+strData+" BARCODE"+barcode + " ");
//        return barcode;
//    }


    /**
     * 取消loading，关闭覆盖层
     */
    public void disappearShade(){

        loadingView.startDisappearAnimation();
        loadingView.getDisappearAnimation().setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //除去加载层
                contentLayout.removeView(loadingView);
                //重新显示
                operaTV.setVisibility(View.VISIBLE);
                operaImage.setVisibility(View.VISIBLE);
                //设置为未进行借书操作状态
                IS_DO_ACTIO = false;
//                IS_SINGLE = false;
                if(VersionSetting.IS_P2000L_DEVICE){
                    p2000LReadCard();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {


            }
        });
    }

    //private void



//    /**
//     * 扫描线程
//     */
//    //控制线程参数
//    private boolean b_inventoryThreadRun = false;
//
//    private class InventoryThrd implements Runnable
//    {
//        public void run()
//        {
//
//            // 盘点参数
//            int INVENTORY_REQUEST_CODE = 1;// requestCode
//            boolean bUseDefaultPara = true;
//            boolean bOnlyReadNew = false;
//            boolean bMathAFI = false;
//            byte mAFIVal = 0x00;
//
//            int failedCnt = 0;// 操作失败次数
//            Object hInvenParamSpecList = null;
//            byte newAI = RfidDef.AI_TYPE_NEW;
//
//            if (!bUseDefaultPara)
//            {
//                hInvenParamSpecList = ADReaderInterface
//                        .RDR_CreateInvenParamSpecList();
//                ISO15693Interface.ISO15693_CreateInvenParam(
//                        hInvenParamSpecList, (byte) 0, bMathAFI, mAFIVal,
//                        (byte) 0);
//
//            }
//            b_inventoryThreadRun = true;
//            while (b_inventoryThreadRun)
//            {
//
//                if (mHandler.hasMessages(INVENTORY_MSG))
//                {
//                    continue;
//                }
//                if (hInvenParamSpecList ==null){
//                    //Log.d("XX","hInvenParamSpecList 为空哦");
//                }
//                int iret=0;
//                try{
//                    iret= m_reader.RDR_TagInventory(newAI, null, 0,
//                            hInvenParamSpecList);
//                }
//                catch(Exception e){
//
//                }
//
//
//
//                if (iret == ApiErrDefinition.NO_ERROR)
//                {
//                    Vector<ISO15693Tag> tagList = new Vector<ISO15693Tag>();
//                    if (bOnlyReadNew)
//                    {
//                        newAI = RfidDef.AI_TYPE_CONTINUE;
//                    }
//                    else
//                    {
//                        newAI = RfidDef.AI_TYPE_NEW;
//                    }
//                    Object tagReport = m_reader
//                            .RDR_GetTagDataReport(RfidDef.RFID_SEEK_FIRST);
//                    while (tagReport != null)
//                    {
//
//                        ISO15693Tag tagData = new ISO15693Tag();
//                        iret = ISO15693Interface.ISO15693_ParseTagDataReport(
//                                tagReport, tagData);
//                        if (iret == ApiErrDefinition.NO_ERROR)
//                        {
//
//                            tagList.add(tagData);
//
//                        }
//                        tagReport = m_reader
//                                .RDR_GetTagDataReport(RfidDef.RFID_SEEK_NEXT);
//                    }
//                    if (!tagList.isEmpty())
//                    {
//                        Message msg = mHandler.obtainMessage();
//                        msg.what = INVENTORY_MSG;
//                        msg.obj = tagList;
//                        msg.arg1 = failedCnt;
//                        mHandler.sendMessage(msg);
//                    }
//                }
//                else
//                {
//                    newAI = RfidDef.AI_TYPE_NEW;
//                    if (b_inventoryThreadRun)
//                    {
//                        failedCnt++;
//                    }
//                    Message msg = mHandler.obtainMessage();
//                    msg.what = INVENTORY_FAIL_MSG;
//                    msg.arg1 = failedCnt;
//                    mHandler.sendMessage(msg);
//                }
//            }
//            b_inventoryThreadRun = false;
//            m_reader.RDR_ResetCommuImmeTimeout();
//            mHandler.sendEmptyMessage(THREAD_END);// 盘点结束
//        }
//    };
    /**
     * 打开覆盖层加载loading
     */
    public void appearShade(){
        //设置为正在借书状态
        IS_DO_ACTIO=true;
        //隐藏
        operaTV.setVisibility(View.INVISIBLE);
        operaImage.setVisibility(View.INVISIBLE);
        contentLayout.addView(loadingView);
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
        intent.setClass(this, activityClass);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_left_ins, R.anim.slide_right_outs);
    }


    //回退键
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (!VersionSetting.IS_ADMIN){
                if (!IS_DO_ACTIO){
                    this.finish();
                    overridePendingTransition(R.anim.slide_left_ins, R.anim.slide_right_outs);
                }
//                startActivity(MainActivity.class);
            }else {
                new SlideFromBottomPopup(this,this).showPopupWindow();
            }
            return true;
        } else
            return super.onKeyDown(keyCode, event);
    }


    //回调接收barcode
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
            if (data != null) {
                Bundle b=data.getExtras(); //data为B中回传的Intent
                String str=b.getString("codedContent");//str即为回传的值
                Log.d("test", str);
                barcode =str;
                if (barcode !=null&&!IS_DO_ACTIO){
                    //3.执行借书操作
                    loanBook();
                }
            }
        }
    }




//    protected void onDestroy() {
//
//        mTag.ISO15693_Disconnect();
//        b_inventoryThreadRun = false;
//        m_reader.RDR_SetCommuImmeTimeout();
//        m_reader.RDR_Close();
//        inventoryList.clear();
////        m_devTypeList.clear();
////        m_connTypeList.clear();
//        super.onDestroy();
//    }




    @Override
    protected void onDestroy() {
        if(VersionSetting.IS_P2000L_DEVICE){
            NfcVUtil.setP2000LOldBarcode("");
            NfcVUtil.setReadCardRun(false);
        }
        super.onDestroy();
    }
}
