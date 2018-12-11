package com.tc.nfc.app.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jaredrummler.materialspinner.MaterialSpinner;
import com.rfid.api.ADReaderInterface;
import com.rfid.api.BluetoothCfg;
import com.rfid.api.GFunction;
import com.rfid.api.ISO15693Interface;
import com.rfid.api.ISO15693Tag;
import com.rfid.def.ApiErrDefinition;
import com.rfid.def.RfidDef;
import com.tc.api.VersionSetting;
import com.tc.nfc.R;
import com.tc.nfc.app.adapter.BookShelvesRecyclerAdapter;
import com.tc.nfc.app.fragment.IndexFragment;
import com.tc.nfc.app.utils.ToastUtil;
import com.tc.nfc.app.utils.nfcV.ExCheBarcode;
import com.tc.nfc.app.view.HintDialog;
import com.tc.nfc.app.view.MoveTextView;
import com.tc.nfc.app.view.RecycleViewDivider;
import com.tc.nfc.app.view.loadingview.XmlLoadingView;
import com.tc.nfc.core.listener.ActionCallbackListener;
import com.tc.nfc.core.task.ThreadPoolManager;
import com.tc.nfc.core.util.JsonUtils;
import com.tc.nfc.model.CheckBook;
import com.tc.nfc.model.InventoryReport;
import com.tc.nfc.model.ScanReport;
import com.tc.nfc.model.ShelvesResult;
import com.tc.nfc.util.Constant;
import com.tc.nfc.util.Demand;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

public class InventoryActivity extends BaseActivity implements View.OnClickListener {

    //用设备扫描时的动作
    private static final int INVENTORY_MSG = 1;
    //获取扫描结果的动作
    private static final int GETSCANRECORD = 2;
    //扫描失败
    private static final int INVENTORY_FAIL_MSG = 4;
    //结束扫描动作
    private static final int THREAD_END = 3;
    //清点书本成功
    public static final int GET_BOOKDATA_SUCCESS = 5;
    //清点书本失败
    public static final int GET_BOOKDATA_FAIL = 6;
    //连接服务器失败
    public static final int CONNECT_FAIL1 = 7;
    //解析失败
    public static final int CONNECT_FAIL2 = 8;
    //Toast
    public static final int TOAST_MSG = 9;
    //书本上架
    public static final int SHELVE_BOOK_SUCCESS =10;
    //打开设备成功
    public static final int OPEN_SUCCESS =11;
    //打开设备失败
    public static final int OPEN_FAIL =12;
    //token失效
    public static final int TOKEN_USERLESS = 13;

    //判断是上架activity OR 清点Activity OR 顺架Activity
    private String Flag;
    //设备类型
    private String devName;
    //通信类型
    private String connTypeStr;
    //设备类型list
    ArrayList<CharSequence> m_devTypeList;
    //连接类型list
    ArrayList<CharSequence> m_connTypeList;
    //蓝牙名字list
    ArrayList<CharSequence> m_bluetoolNameList;


    //单本上架
    private static final int SHELVE_SINGLE = 0;
    //多本上架按钮
    private static final int SHELVE_MANY = 1;
    //上架类型
    private int shelfType =SHELVE_SINGLE;
    //上架请求次数
    private int requestCount;
    //上架成功次数
    private int successCount;
    //上架总请求次数
    private int totalCount;

    private ADReaderInterface m_reader = new ADReaderInterface();


    //盘点标签线程
    private Thread m_inventoryThrd = null;
    private ISO15693Interface mTag = new ISO15693Interface();
    //获取扫描记录的线程
    private Thread mGetScanRecordThrd = null;
    //盘点结果list
    private List<InventoryReport> inventoryList = new ArrayList<InventoryReport>();
    //扫描结果list
    private List<ScanReport> scanfReportList = new ArrayList<ScanReport>();

    //条码号
    private String barcode;
    //书架号
    private String shlfNumber;
    //需要查找的barcode
    private String noticeBarcode="";
    //是否单本录入
    private boolean IS_SINGLE_ENTRY =false;

    //处理线程管理
    private ThreadPoolManager poolManager;
    //判断书本是否重复List
    private Set<String> shelvesResultsSet;
    //储存书本list
    private List<ShelvesResult> shelvesResultsList;
    //操作成功list
    private List<ShelvesResult> shelvesSuccessResultsList;

    //清点标识
    private String countSignFlagNum;
    //标志是否正在清点，如果正在清点，则即使扫描到条码也不操作
    private boolean IS_DO_COUTING =false;

    //缓存
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    //声音
    private SoundPool soundPool = null;
    private int soundID = 0;
    private float audioCurrentVolume;

    /**
     * 布局
     */
    //返回按钮
    private LinearLayout ivBackPress;
    //加载框
    private XmlLoadingView xmlLoadingView;
    //整个页面布局  连接布局
    private RelativeLayout contentLayout,connect_setting;
    //蓝牙连接布局  网络连接布局
    private LinearLayout bluetool_setting,wifi_setting;
    //上架按钮  连接按钮
    private Button btnShelves, btnConnect;
    //书架号框  条码号框
    private MoveTextView mtvShelveText, mtvBarcodeText;
    //标识号框
    private EditText etCountSignFlag;
    //标题栏  书本数量  成功数量  清除按钮
    private TextView tvTitle, tvBookNum, tvSuccessNum, tvClearbtn;
    //下拉框
    private MaterialSpinner spBluetool;//dev_sp,conn_sp;

    //书本列表
    private RecyclerView recyclerViewBookInfo;
    private BookShelvesRecyclerAdapter bookShelvesRecyclerAdapter;
    private ItemTouchHelper itemTouchHelper ;

    //消息框
    private HintDialog mDialog = null;

    private MediaPlayer mediaPlayer;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Flag =intent.getStringExtra("Flag");
        //初始化view
        initViews();
        //初始化监听
        initListener();
        //设置媒体声音的播放
        setPlayer();

    }

    protected void onResume(){
//缓存连接方式
//        sp = getSharedPreferences("InventorySetting", Context.MODE_WORLD_READABLE);
//        editor = sp.edit();


//        devName =m_devTypeList.get(Integer.parseInt(sp.getString("devNames", "0"))).toString();
//        connTypeStr=m_connTypeList.get(Integer.parseInt(sp.getString("commTypeStrs", "0"))).toString();

        //设定设备类型和通信类型
        devName ="RPAN";
        connTypeStr ="蓝牙";
//        dev_sp.setSelectedIndex(Integer.parseInt(sp.getString("devNames", "0")));
//        conn_sp.setSelectedIndex(Integer.parseInt(sp.getString("commTypeStrs", "0")));

//        if ("蓝牙".equals(m_connTypeList.get(Integer.parseInt(sp.getString("commTypeStrs", "0"))).toString())) {
//            bluetool_setting.setVisibility(View.VISIBLE);
//            wifi_setting.setVisibility(View.GONE);
//        }else{
//            bluetool_setting.setVisibility(View.GONE);
//            wifi_setting.setVisibility(View.VISIBLE);
//        }
        //配置连接界面信息
        showSetting();
        super.onResume();

    }

    protected void onPause(){
        //editor.putString("devNames", String.valueOf(dev_sp.getSelectedIndex()));
        //editor.putString("commTypeStrs", String.valueOf(conn_sp.getSelectedIndex()));
        //editor.commit();
        super.onPause();
    }

    @Override
    protected void initContentView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_shelves);
    }

    public String getBarcode() {
        return barcode;
    }

    /**
     * 初始化1
     */
    private void initViews(){

        //初始化loadingview
        xmlLoadingView =new XmlLoadingView(this);
        xmlLoadingView.setViewSize(0, 0);
        xmlLoadingView.setViewPadding(40, 50, 40, 50);

        //线程池管理
        poolManager = new ThreadPoolManager(ThreadPoolManager.TYPE_FIFO, 5);
        poolManager.start();

        contentLayout = (RelativeLayout)findViewById(R.id.contentLayout);//整体布局
        tvTitle =(TextView)findViewById(R.id.tv_title_activity);//标题
        tvTitle.setText(R.string.shelvesbook);
        ivBackPress = (LinearLayout)findViewById(R.id.ll_activity_title_backpress);//返回按钮

        btnShelves =(Button)findViewById(R.id.shelvesBtn);//上架按钮
        btnShelves.setEnabled(false);//设置不可点击
        btnShelves.setBackgroundResource(R.drawable.not_selected_btn2);

        mtvShelveText =(MoveTextView)findViewById(R.id.shelvesNum);//书架号text

        //判断是哪个页面，作相应的更改
        if (Flag.equals(IndexFragment.CHECK_FLAG)){
            btnShelves.setVisibility(View.GONE);
            tvTitle.setText(R.string.checkbook);
            etCountSignFlag =(EditText)findViewById(R.id.countSignFlag);
            etCountSignFlag.setVisibility(View.VISIBLE);
            mtvShelveText.setVisibility(View.GONE);
        }else if (Flag.equals(IndexFragment.SHUNJIA_FLAG)){
            tvTitle.setText(R.string.alongbook);
        }
        else{

            btnShelves.setVisibility(View.GONE);
        }

        mtvBarcodeText =(MoveTextView)findViewById(R.id.barcodeSearch);//查找书本,输入要查找的条码号
        mtvBarcodeText.setHint("输入要查找的条码...");

        tvBookNum =(TextView)findViewById(R.id.num1);//显示书本数量的textview
        tvSuccessNum =(TextView)findViewById(R.id.num2);//显示操作成功数量textview
        tvClearbtn =(TextView)findViewById(R.id.clear);//清除显示出来的item的操作的textview

        //判断书本是否重复
        shelvesResultsSet=new HashSet<>();
        //储存书本list
        shelvesResultsList =new ArrayList<>();
        //操作成功list
        shelvesSuccessResultsList =new ArrayList<>();


        recyclerViewBookInfo =(RecyclerView)findViewById(R.id.recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //设置布局管理器
        recyclerViewBookInfo.setLayoutManager(layoutManager);
        //设置为垂直布局，这也是默认的
        layoutManager.setOrientation(OrientationHelper.VERTICAL);


        //设置增加或删除条目的动画
        recyclerViewBookInfo.setItemAnimator(new DefaultItemAnimator());
        recyclerViewBookInfo.addItemDecoration(new RecycleViewDivider(
                this, LinearLayoutManager.VERTICAL, 1, Color.parseColor("#F8F8F8")));
        itemTouchHelper = new ItemTouchHelper(recyclerCallback);
        itemTouchHelper.attachToRecyclerView(recyclerViewBookInfo);

        //设置布局
//        dev_sp = (MaterialSpinner)findViewById(R.id.dev_sp);
//        conn_sp =(MaterialSpinner)findViewById(R.id.conn_sp);
        spBluetool = (MaterialSpinner)findViewById(R.id.bluetool_sp);
        connect_setting =(RelativeLayout)findViewById(R.id.connect_setting);
        bluetool_setting =(LinearLayout)findViewById(R.id.bluetool_setting);
        wifi_setting =(LinearLayout)findViewById(R.id.wifi_setting);
        btnConnect =(Button)findViewById(R.id.conn_btn);

        // 初始化声音
        AudioManager am = (AudioManager) this
                .getSystemService(Context.AUDIO_SERVICE);
        float audioCurrentVolume = am
                .getStreamVolume(AudioManager.STREAM_MUSIC);
        soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 5);
        soundID = soundPool.load(this, R.raw.msg, 1);

    }

    /**
     * 初始化监听
     */
    private void initListener(){
        ivBackPress.setOnClickListener(this);
        btnShelves.setOnClickListener(this);
        btnConnect.setOnClickListener(this);
        tvClearbtn.setOnClickListener(this);

        //书架号edittext监听
        mtvShelveText.setImageListener(new MoveTextView.ImageListener() {
            //点击"X"触发
            public void ImageOnClick() {

                //如果扫描列表中存在书架号，则删除扫描列表中的书架号，为了防止扫描了图书
                for (ScanReport item : scanfReportList) {
                    if (item.getDataStr().equals(shlfNumber)) {
                        scanfReportList.remove(item);
                        break;
                    }
                }
                mtvShelveText.setText("");
            }

            //当text失去焦点或者点击完成按钮时会获取当前的书架号
            public void doneOnClick() {
                shlfNumber = mtvShelveText.getText();
                if (bookShelvesRecyclerAdapter!=null){
                    bookShelvesRecyclerAdapter.setShelveNo(shlfNumber);
                    bookShelvesRecyclerAdapter.notifyDataSetChanged();
                }
            }
        });
        mtvBarcodeText.setImageListener(new MoveTextView.ImageListener() {
            @Override
            public void ImageOnClick() {
                mtvBarcodeText.setText("");
                noticeBarcode = "";
            }

            @Override
            public void doneOnClick() {

                noticeBarcode = mtvBarcodeText.getText();

            }
        });


        //动画结束操作
        xmlLoadingView.getDisappearAnimation().setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                contentLayout.removeView(xmlLoadingView);
                IS_DO_COUTING = false;
                IS_SINGLE_ENTRY = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


//        dev_sp.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
//                devName = m_devTypeList.get(position).toString();
//            }
//        });
//        conn_sp.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
//
//                connTypeStr = m_connTypeList.get(position).toString();
//                if (m_connTypeList.get(position).equals("蓝牙")) {
//                    bluetool_setting.setVisibility(View.VISIBLE);
//                    wifi_setting.setVisibility(View.GONE);
//
//                } else {
//                    bluetool_setting.setVisibility(View.GONE);
//                    wifi_setting.setVisibility(View.VISIBLE);
//                }
//            }
//        });


    }
    /**
     * recycleview的监听
     */
    private ItemTouchHelper.SimpleCallback recyclerCallback =new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            Log.d("test", "位置信息："+position );

            for (ShelvesResult item:shelvesSuccessResultsList){
                if (item.getCheckBook().getBookBarcode().equals(shelvesResultsList.get(position).getCheckBook().getBookBarcode())) {
                    shelvesSuccessResultsList.remove(item);
                    break;
                }
            }
            for (ScanReport item : scanfReportList){
                Log.d("test",item.getDataStr()+" "+shelvesResultsList.get(position).getCheckBook().getBookBarcode()+"A");
                if (item.getDataStr().equals(shelvesResultsList.get(position).getCheckBook().getBookBarcode())){
                    scanfReportList.remove(item);
                    Log.d("test","盘点list数量："+scanfReportList.size());
                    //Log.d("SB", item.getBarcode() + " " + shelvesResultsList.get(position).getCheckBook().getBookBarcode());

                    break;
                }
            }
            for (String item:shelvesResultsSet){
                //Log.d("SB",item.getBarcode()+" "+shelvesResultsList.get(position).getCheckBook().getBookBarcode());

                Log.d("test", item+" "+shelvesResultsList.get(position).getCheckBook().getBookBarcode()+"B");
                if (item.equals(shelvesResultsList.get(position).getCheckBook().getBookBarcode())){
                    shelvesResultsSet.remove(item);
                    Log.d("test", shelvesResultsSet.size() + "");

                    break;
                }
            }

            shelvesResultsList.remove(position);
            bookShelvesRecyclerAdapter.notifyItemRemoved(position);

            changeShelveBtn_and_listText();

        }
    };



    /**
     *  判断扫到的是否是书架号,如果barcode是书架的标签好，就把之前的显示的书架号清空了
     */
    public boolean isShlfNum(String fisrtBarcode){
        try {
            String judgeBarcode = fisrtBarcode.substring(0, VersionSetting.CUT_DIGITS);//截取代码来识别是否是架位号和书
//            if(VersionSetting.SHLF_NUM.equals(judgeBarcode) || VersionSetting.SHLF_NUM1.equals(judgeBarcode) || VersionSetting.SHLF_NUM2.equals(judgeBarcode) || VersionSetting.SHLF_NUM3.equals(judgeBarcode)){
//                mtvShelveText.setText("");
//                return true;
//            }

            for(int number=0;number<VersionSetting.SHLF_NUMBER.length;number++){
                if(VersionSetting.SHLF_NUMBER[number].equals(judgeBarcode)){
                    mtvShelveText.setText("");
                    return true;
                }
            }
            return false;

        }catch (Exception e){
            Log.d("test",e.getMessage());
        }
        return false;
    }



    /**
     * 配置连接蓝牙界面信息
     */
    private void showSetting(){

//        m_devTypeList =new ArrayList<CharSequence>();
//        m_devTypeList.add("RPAN");
//        m_devTypeList.add("M201");
//        // 列举设备类型
//        dev_sp.setItems(m_devTypeList);

//        m_connTypeList=new ArrayList<CharSequence>();
//        m_connTypeList.add("蓝牙");
//        m_connTypeList.add("网络");
//        conn_sp.setItems(m_connTypeList);

        // 列举已配对的蓝牙设备
        m_bluetoolNameList = null;

        m_bluetoolNameList = new ArrayList<CharSequence>();
        ArrayList<BluetoothCfg> m_blueList = ADReaderInterface.GetPairBluetooth();
        if (m_blueList != null)
        {
            for (BluetoothCfg bluetoolCfg : m_blueList)
            {
                m_bluetoolNameList.add(bluetoolCfg.GetName());
            }
        }
        if (m_blueList.size()==0)
            m_bluetoolNameList.add("暂无已配对设备");
        spBluetool.setItems(m_bluetoolNameList);
        spBluetool.setTextColor(Color.parseColor("#605f5f"));
//        dev_sp.setTextColor(Color.parseColor("#C9C9C9"));
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
                IS_SINGLE_ENTRY = true;
                checkBook();
            }

            @Override
            public void onFailure(String errorEvent, String message) {

            }
        });
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.ll_activity_title_backpress:
                this.finish();
                overridePendingTransition(R.anim.slide_left_ins, R.anim.slide_right_outs);
                break;
            case R.id.shelvesBtn:
                //上架方式为多本上架
                shelfType =SHELVE_MANY;
                if (shelvesSuccessResultsList.size()!=0){

                    requestCount=0;
                    successCount=0;

                    xmlLoadingView.startAppearAnimation();
                    contentLayout.addView(xmlLoadingView);
                    btnShelves.setEnabled(false);
                    btnShelves.setBackgroundResource(R.drawable.not_selected_btn2);

                    //需要上架的书本数
                    totalCount=shelvesSuccessResultsList.size();
                    for (int i = 0; i < shelvesSuccessResultsList.size(); i++) {
                        mNFCApplication.getBookAction().shelvesBook(shelvesSuccessResultsList.get(i),
                                shlfNumber, poolManager, mHandler);
                    }
                }

                break;
            case R.id.conn_btn:

                if (btnConnect.getText().equals("连接设备")){
                    //如果是清点页面，判断是否有标识
                    if (Flag.equals(IndexFragment.CHECK_FLAG)&&!checkIsSign()){
                        return;
                    }

                    btnConnect.setBackgroundResource(R.drawable.not_selected_btn);
                    btnConnect.setEnabled(false);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            if (ConnectBlueTool()){
                                Message msg = Message.obtain();
                                msg.what = InventoryActivity.OPEN_SUCCESS;
                                mHandler.sendMessage(msg);
                            }else{
                                Message msg = Message.obtain();
                                msg.what = InventoryActivity.OPEN_FAIL;
                                mHandler.sendMessage(msg);
                            }
                        }
                    }).start();

                }
                else{

                    mTag.ISO15693_Disconnect();
                    b_inventoryThreadRun = false;
                    bGetScanRecordFlg =false;//扫描参数为false
                    m_reader.RDR_SetCommuImmeTimeout();
                    m_reader.RDR_Close();
                    btnConnect.setText("连接设备");
                    //断开盘点仪后提交日志
                    upLoadMachineLog(Constant.LOG_USER,Constant.LOG_PASSWORD,Constant.machineId,"4","盘点仪与手机断开连接");

                }

                break;

            case R.id.clear:
                if(shelvesResultsList.size() != 0){
                    createDialog("清除列表操作");
                }else{
                    ToastUtil.showToastShortTime(this,"暂无数据！");
                }
                break;
        }
    }
    /**
     * 连接蓝牙
     */
    private Boolean ConnectBlueTool(){
        String conStr = "";
        //devName =m_devTypeList.get(dev_sp.getSelectedIndex()).toString();
        devName ="RPAN";
        if (connTypeStr.equals("蓝牙"))
        {
            if (spBluetool.getItems().isEmpty())
            {
                Log.d("test", "请选择蓝牙设备！");
                return false;
            }
            String bluetoolName = m_bluetoolNameList.get(spBluetool.getSelectedIndex()).toString();
            if (bluetoolName == "")
            {
                Log.d("test", "请选择蓝牙设备！");
                return false;
            }
            conStr = String.format("RDType=%s;CommType=BLUETOOTH;Name=%s",
                    devName, bluetoolName);

            if (m_reader.RDR_Open(conStr) == ApiErrDefinition.NO_ERROR) {

                Log.d("test","蓝牙连接成功");
                return true;
            }
            else
            {
                Log.d("test","失败");
                return false;
            }

        }

        return false;
    }


    private void failItem(ShelvesResult shelvesResultFail, int addType){

        if (addType==0){

            CheckBook checkBook =shelvesResultFail.getCheckBook();
            checkBook.setBookTitle("操作失败");
            checkBook.setPublisher("--");
            checkBook.setShelfno("--");
            checkBook.setReferenceNum("--");
            checkBook.setIsbn("--");
            checkBook.setPubdate("--");
            shelvesResultFail.setCheckBook(checkBook);
        }
        shelvesResultFail.setCheckResult(false);

        addOneItem(shelvesResultFail);
    }

    private void addOneItem(ShelvesResult shelvesResult){
        Log.d("test2","上架后的barcode:"+shelvesResult.getCheckBook().getBookBarcode());
        //增加一行
        shelvesResultsList.add(0, shelvesResult);
        bookShelvesRecyclerAdapter.notifyItemInserted(0);
        bookShelvesRecyclerAdapter.notifyItemRangeChanged(0, shelvesResultsList.size());
        recyclerViewBookInfo.smoothScrollToPosition(0);

        changeShelveBtn_and_listText();

        //获取的要查找的barcode号
        noticeBarcode = mtvBarcodeText.getText().toString();

        if (shelvesResult.getCheckBook().getBookBarcode().toLowerCase().equals(noticeBarcode.toLowerCase()) && mediaPlayer != null && noticeBarcode!=null && !noticeBarcode.equals("")){
            mediaPlayer.start();
            shelvesResult.setNotice(true);
        }
    }

    /**
     * 扫描线程
     */
    //控制线程参数
    private boolean b_inventoryThreadRun = false;

    private class InventoryThrd implements Runnable
    {
        public void run()
        {

            // 盘点参数
            int INVENTORY_REQUEST_CODE = 1;// requestCode
            boolean bUseDefaultPara = true;
            boolean bOnlyReadNew = false;
            boolean bMathAFI = false;
            byte mAFIVal = 0x00;

            int failedCnt = 0;// 操作失败次数
            Object hInvenParamSpecList = null;
            byte newAI = RfidDef.AI_TYPE_NEW;

            if (!bUseDefaultPara)
            {
                hInvenParamSpecList = ADReaderInterface
                        .RDR_CreateInvenParamSpecList();
                ISO15693Interface.ISO15693_CreateInvenParam(
                        hInvenParamSpecList, (byte) 0, bMathAFI, mAFIVal,
                        (byte) 0);

            }
            b_inventoryThreadRun = true;
            while (b_inventoryThreadRun)
            {

                if (mHandler.hasMessages(INVENTORY_MSG))
                {
                    continue;
                }
                if (hInvenParamSpecList ==null){
                    //Log.d("XX","hInvenParamSpecList 为空哦");
                }
                int iret=0;
                try{
                    iret= m_reader.RDR_TagInventory(newAI, null, 0,
                            hInvenParamSpecList);
                }
                catch(Exception e){

                }

                if (iret == ApiErrDefinition.NO_ERROR)
                {
                    Vector<ISO15693Tag> tagList = new Vector<ISO15693Tag>();
                    if (bOnlyReadNew)
                    {
                        newAI = RfidDef.AI_TYPE_CONTINUE;
                    }
                    else
                    {
                        newAI = RfidDef.AI_TYPE_NEW;
                    }
                    Object tagReport = m_reader
                            .RDR_GetTagDataReport(RfidDef.RFID_SEEK_FIRST);
                    while (tagReport != null)
                    {
                        ISO15693Tag tagData = new ISO15693Tag();
                        iret = ISO15693Interface.ISO15693_ParseTagDataReport(
                                tagReport, tagData);
                        if (iret == ApiErrDefinition.NO_ERROR)
                        {

                            tagList.add(tagData);

                        }
                        tagReport = m_reader
                                .RDR_GetTagDataReport(RfidDef.RFID_SEEK_NEXT);
                    }
                    if (!tagList.isEmpty())
                    {
                        Message msg = mHandler.obtainMessage();
                        msg.what = INVENTORY_MSG;
                        msg.obj = tagList;
                        msg.arg1 = failedCnt;
                        mHandler.sendMessage(msg);
                    }
                }
                else
                {
                    newAI = RfidDef.AI_TYPE_NEW;
                    if (b_inventoryThreadRun)
                    {
                        failedCnt++;
                    }
                    Message msg = mHandler.obtainMessage();
                    msg.what = INVENTORY_FAIL_MSG;
                    msg.arg1 = failedCnt;
                    mHandler.sendMessage(msg);
                }
            }
            b_inventoryThreadRun = false;
            m_reader.RDR_ResetCommuImmeTimeout();
            mHandler.sendEmptyMessage(THREAD_END);// 盘点结束
        }
    };

    /**
     * 获取扫描记录的线程
     */
    //控制线程的参数
    private boolean bGetScanRecordFlg = false;

    private class GetScanRecordThrd implements Runnable
    {
        public void run()
        {
            int nret = 0;
            bGetScanRecordFlg = true;
            byte gFlg = 0x01;// 初次采集数据，标志位为0x00
            Object dnhReport = null;

            // 清空缓冲区记录
            nret = m_reader.RPAN_ClearScanRecord();
            if (nret != ApiErrDefinition.NO_ERROR)
            {
                bGetScanRecordFlg = false;
                mHandler.sendEmptyMessage(THREAD_END);// 盘点结束
                return;
            }

            while (bGetScanRecordFlg)
            {
                if (mHandler.hasMessages(GETSCANRECORD))
                {
                    continue;
                }


                try{
                    nret = m_reader.RPAN_GetRecord(gFlg);
                }catch (NullPointerException e){
                    Log.d("test","盘点参数空指针"+e.getMessage());
                }

                if (nret != ApiErrDefinition.NO_ERROR)
                {
                    gFlg = 0x00;
                    continue;
                }
                gFlg = 0x01;// 以后采集标志位为0x01
                dnhReport = m_reader
                        .RDR_GetTagDataReport(RfidDef.RFID_SEEK_FIRST);
                Vector<String> dataList = new Vector<String>();
                while (dnhReport != null)
                {
                    byte[] byData = m_reader.RPAN_ParseRecord(dnhReport);
                    String strData = GFunction.encodeHexStr(byData);
                    dataList.add(strData);
                    dnhReport = m_reader
                            .RDR_GetTagDataReport(RfidDef.RFID_SEEK_NEXT);
                }
                if (!dataList.isEmpty())
                {
                    Message msg = mHandler.obtainMessage();
                    msg.what = GETSCANRECORD;
                    msg.obj = dataList;
                    mHandler.sendMessage(msg);
                }
            }
            bGetScanRecordFlg = false;
            mHandler.sendEmptyMessage(THREAD_END);// 结束
        }
    };

    /**
     * 回调接收
     */
    private Handler mHandler = new MyHandler(this);

    private static class MyHandler extends Handler
    {
        private final WeakReference<InventoryActivity> mActivity;

        public MyHandler(InventoryActivity activity)
        {
            mActivity = new WeakReference<InventoryActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg)
        {
            InventoryActivity pt = mActivity.get();
            if (pt == null)
            {
                return;
            }
            boolean b_find = false;
            switch (msg.what)
            {
                case INVENTORY_MSG:// 盘点到标签
                    @SuppressWarnings("unchecked")
                    Vector<ISO15693Tag> tagList = (Vector<ISO15693Tag>) msg.obj;
                    for (int i = 0; i < tagList.size(); i++)
                    {

                        b_find = false;
                        ISO15693Tag tagData = tagList.get(i);// (ISO15693Tag)
                        // msg.obj;
                        String uidStr = GFunction.encodeHexStr(tagData.uid);
                        for (int j = 0; j < pt.inventoryList.size(); j++)
                        {
                            InventoryReport mReport = pt.inventoryList.get(j);
                            if (mReport.getUidStr().equals(uidStr))
                            {
                                mReport.setFindCnt(mReport.getFindCnt() + 1);
                                b_find = true;
                                break;
                            }
                        }
                        if (!b_find)
                        {

                            String tagName = ISO15693Interface
                                    .GetTagNameById(tagData.tag_id);
                            String barcode;

                            //解析出barcode，并且传到服务器清点
                            if (!(barcode=pt.PraseTag(tagData.uid)).equals("")){
                                pt.inventoryList.add(new InventoryReport(uidStr, tagName,barcode));
                                pt.checkBook();
                            }

                        }
                    }
                    Log.d("test","盘点到的标签总数:" + pt.inventoryList.size() );

                    break;
                case GETSCANRECORD:
                    @SuppressWarnings("unchecked")
                    Vector<String> dataList = (Vector<String>) msg.obj;
                    for (String str : dataList)
                    {
                        b_find = false;
                        for (int i = 0; i < pt.scanfReportList.size(); i++)
                        {
                            ScanReport mReport = pt.scanfReportList.get(i);
                            if (str.equals(mReport.getDataStr()))
                            {
                                mReport.setFindCnt(mReport.getFindCnt() + 1);
                                b_find = true;
                            }
                        }
                        if (!b_find)
                        {
                            pt.scanfReportList.add(new ScanReport(str));

                        }

                        //扫描到的数据放在list中，从list中取出来，转换为可以使用的barcode
                        for(int x=0;x<pt.scanfReportList.size();x++){
                            if( x == pt.scanfReportList.size()-1) {
                                pt.changeDataToBarcode(pt.scanfReportList.get(x).getDataStr());
                                //判断扫到的是否是书架号,如果barcode是书架的标签号，就把之前的显示的书架号清空
                                if (pt.mtvShelveText.isHasText() && pt.isShlfNum(pt.getBarcode()) && !pt.Flag.equals(IndexFragment.CHECK_FLAG)) {
                                    if(pt.shelvesResultsList.size() == 0){
                                        ToastUtil.showToastShortTime(pt,"书架号已切换！");

                                    }else{
                                        pt.createDialog("扫到架位号，是否清除上个架位号信息");

                                    }
                                }
                                pt.checkBook();
                            }
                        }
                    }
                    break;

                case INVENTORY_FAIL_MSG:
                    //Log.d("XX","标签总数:" + pt.inventoryList.size() + ";失败次数:" + msg.arg1);
                    break;

                case THREAD_END:// 线程结束

                    break;

                case GET_BOOKDATA_SUCCESS:

                    ShelvesResult shelvesResult = (ShelvesResult) msg.getData().getSerializable("shelvesResult");

                    if(pt.Flag.equals(IndexFragment.CHECK_FLAG)){
                        //提交盘点成功的日志
                        //pt.uploadInventoryLog(VersionSetting.LOG_USER, VersionSetting.LOG_PASSWORD,VersionSetting.MACHINE_ID_LOG_INVENTORY,"204a",pt.barcode,"");
                        pt.uploadInventoryLog2(Constant.LOG_USER,Constant.LOG_PASSWORD,Constant.machineId,"204a",pt.barcode,"",pt.poolManager,pt.mHandler);
                    }

                    if (pt.Flag.equals(IndexFragment.SHELVE_FLAG)){
                        pt.shelfType =SHELVE_SINGLE;
                        pt.mNFCApplication.getBookAction().shelvesBook(shelvesResult, pt.shlfNumber, pt.poolManager, pt.mHandler);
                    }
                    else{
                        pt.shelvesSuccessResultsList.add(shelvesResult);
                        pt.addOneItem(shelvesResult);
                        //关闭loadingview
                        pt.xmlLoadingView.startDisappearAnimation();

                    }

                    break;
                case CONNECT_FAIL1:
                case CONNECT_FAIL2:
                case GET_BOOKDATA_FAIL:
                    pt.xmlLoadingView.startDisappearAnimation();
                    ShelvesResult shelvesResult2 = new ShelvesResult();
                    CheckBook checkBook2 = new CheckBook();
                    checkBook2.setBookBarcode(msg.getData().getString("barcode"));
                    shelvesResult2.setCheckBook(checkBook2);
                    shelvesResult2.setMessage(msg.getData().getString("message"));
                    pt.failItem(shelvesResult2, 0);

                    break;
                case TOAST_MSG:

                    if (pt.Flag.equals(IndexFragment.SHELVE_FLAG)){
                        ShelvesResult shelvesResult1 = (ShelvesResult) msg.getData().getSerializable("shelvesResult");
                        shelvesResult1.setMessage(msg.getData().getString("toastMsg"));
                        pt.failItem(shelvesResult1, 1);
                    }
                    else{
                        ToastUtil.showToastShortTime(pt, msg.getData().getString("toastMsg"));
                        pt.xmlLoadingView.startDisappearAnimation();
                    }

                    break;

                case SHELVE_BOOK_SUCCESS:

                    pt.shelveBook(msg,pt.shelfType);
                    break;

                case OPEN_SUCCESS:
                    //连接设备成功之后提交日志到服务器
                    pt.upLoadMachineLog(Constant.LOG_USER,Constant.LOG_PASSWORD,Constant.machineId,"1","盘点仪与手机连接成功");

                    ToastUtil.showToastShortTime(pt, "打开设备成功！");
                    pt.btnConnect.setText("停止");
                    pt.btnConnect.setBackgroundResource(R.drawable.purchase_btn_selector);
                    pt.btnConnect.setEnabled(true);
                    pt.startGetScanRecordThread();//获取扫描记录线程开启
                    //pt.startInvertoryThread();// 扫描线程开启
                    break;
                case OPEN_FAIL:
                    ToastUtil.showToastShortTime(pt, "打开设备失败！");
                    pt.btnConnect.setBackgroundResource(R.drawable.purchase_btn_selector);
                    pt.btnConnect.setEnabled(true);
                    break;

                case TOKEN_USERLESS:
                    //账号验证过期，跳转到登陆界面重新登陆
                    ToastUtil.showToastShortTime(pt,"账号验证过去，请重新登陆");
                    pt.finish();
                    pt.login();

                default:
                    break;
            }
        }
    }

    /**
     * 是否有标识
     */
    private boolean checkIsSign(){
        if (etCountSignFlag.getText().toString().equals("")) {

            Message msg = mHandler.obtainMessage();
            msg.what = TOAST_MSG;
            Bundle b = new Bundle();
            b.putString("toastMsg", "请输入标识号..");
            msg.setData(b);
            mHandler.sendMessage(msg);

            return false;
        }
        countSignFlagNum = etCountSignFlag.getText().toString();
        return true;
    }

    /**
     * 盘点线程开启
     */
    private void startInvertoryThread(){
        m_inventoryThrd = new Thread(new InventoryThrd());
        m_inventoryThrd.start();
    }

    /**
     *  获取记录线程的开启
     */
    private void startGetScanRecordThread(){
        mGetScanRecordThrd = new Thread(new GetScanRecordThrd());
        mGetScanRecordThrd.start();
    }


    /**
     * 将rpan扫描到的数据转换到可以使用的格式
     * @param data
     * @return
     */
    private String changeDataToBarcode(String data){

        boolean isLoanSuc = ExCheBarcode.loadRule(data);
        if (!isLoanSuc) {
            Log.d("test","失败转换barcode数据");
        }
        barcode = ExCheBarcode.getEncodeBarcode();
        return barcode;
    }

    /**
     * 连接标签
     * @param connectUid
     * @return
     */
    private String PraseTag(byte connectUid[] ){
        byte connectMode = 1;
        if (connectMode == 1 && connectUid == null)
        {
            Log.d("test","UID不能为空");
            return "";
        }
        int iret = mTag.ISO15693_Connect(m_reader,
                RfidDef.RFID_ISO15693_PICC_ICODE_SLI_ID, connectMode,
                connectUid);
        if (iret != ApiErrDefinition.NO_ERROR)
        {
            Log.d("test", "连接标签失败");
        }
        return UiReadBlock();
    }

    /**
     * 解析标签
     * @return
     */
    private String UiReadBlock()
    {
        //从第几个扇区开始
        int blkAddr = 0;
        int numOfBlksToRead = 4;
        if (blkAddr + numOfBlksToRead > 28)// 数据块地址溢出
        {
            numOfBlksToRead = 28 - blkAddr;
        }
        Integer numOfBlksRead = 0;
        Long bytesBlkDatRead = (long) 0;
        byte bufBlocks[] = new byte[4 * numOfBlksToRead];
        int iret = mTag.ISO15693_ReadMultiBlocks(false, blkAddr,
                numOfBlksToRead, numOfBlksRead, bufBlocks, bytesBlkDatRead);
        if (iret != ApiErrDefinition.NO_ERROR)
        {

            return "";

        }
        String strData = GFunction.encodeHexStr(bufBlocks);

        boolean isLoanSuc = ExCheBarcode.loadRule(strData);
        if (!isLoanSuc) {
            return "";
        }

        barcode = ExCheBarcode.getEncodeBarcode();
        return barcode;
    }


    /**
     * 分别执行书本上架、书本清点、书本顺架操作
     */
    private void checkBook(){

        if (barcode != null) {

            if (bookShelvesRecyclerAdapter==null){
                if (Flag.equals(IndexFragment.SHUNJIA_FLAG)) {
                    bookShelvesRecyclerAdapter = new BookShelvesRecyclerAdapter(InventoryActivity.this, shelvesResultsList, true, shlfNumber);
                } else {
                    bookShelvesRecyclerAdapter = new BookShelvesRecyclerAdapter(InventoryActivity.this, shelvesResultsList, false, shlfNumber);
                }
                bookShelvesRecyclerAdapter.setShelveListener(new BookShelvesRecyclerAdapter.ShelveListener() {
                    @Override
                    public void onClick(ShelvesResult shelvesResult) {
                        shelfType =SHELVE_SINGLE;
                        mNFCApplication.getBookAction().shelvesBook(shelvesResult, shlfNumber, poolManager, mHandler);
                    }
                });
                //设置Adapter
                recyclerViewBookInfo.setAdapter(bookShelvesRecyclerAdapter);
            }



            //如果书架号为空，除了清点操作以外，其他页面都需要将接收到的barcode设为书架号
            if (!mtvShelveText.isHasText() && !Flag.equals(IndexFragment.CHECK_FLAG)) {
                if(!isShlfNum(barcode)){
                    ToastUtil.showToastShortTime(this,"请先扫书架！");
                    if(scanfReportList.size() != 0){
                        scanfReportList.clear();//去掉list中存放书架号barcode的数据
                    }
                    return;
                }

                shlfNumber = Demand.DisplayShelfNumberJL(barcode);//将书架号转换位中文(九龙坡需求)
                //shlfNumber = barcode;
                mtvShelveText.setText(shlfNumber);
                mtvShelveText.startAnimation();
                bookShelvesRecyclerAdapter.setShelveNo(shlfNumber);
                bookShelvesRecyclerAdapter.notifyDataSetChanged();
                //去掉list中存放书架号barcode的数据
                if(scanfReportList.size() != 0){
                    scanfReportList.clear();
                }


            } else {

                shlfNumber = mtvShelveText.getText();
                if (!Flag.equals(IndexFragment.CHECK_FLAG)) {
                    bookShelvesRecyclerAdapter.setShelveNo(shlfNumber);
                }
                //当为清点操作时判断是否有标识号
                if (Flag.equals(IndexFragment.CHECK_FLAG)) {
                    if (!checkIsSign()) {
                        IS_SINGLE_ENTRY = false;
                        return;
                    }
                }
                //如果是单本录入会出现加载框
                if (IS_SINGLE_ENTRY && !IS_DO_COUTING) {
                    IS_DO_COUTING = true;
                    xmlLoadingView.startAppearAnimation();
                    contentLayout.addView(xmlLoadingView);
                }

                //看是否能加入set中判断是否重复
                if (!shelvesResultsSet.add(barcode)) {
                    if (IS_SINGLE_ENTRY) {
                        IS_DO_COUTING = false;
                        xmlLoadingView.startDisappearAnimation();
                    }
                    return;
                }

                //执行下一步操作
                if (Flag.equals(IndexFragment.CHECK_FLAG)) {
                    mNFCApplication.getBookAction().checkBookSingle(barcode, countSignFlagNum, poolManager, mHandler);
                }else if (Flag.equals(IndexFragment.SHUNJIA_FLAG)){
                    mNFCApplication.getBookAction().getBookData(barcode, poolManager, mHandler);
                }else{
                    //先是获取到书本的信息再进行上架
                    //mNFCApplication.getBookAction().getBookData(barcode, poolManager, mHandler);
                    //直接有barcode和shevlNum进行上架
                    mNFCApplication.getBookAction().shelveBookFast(barcode,shlfNumber,poolManager,mHandler);
                }
            }
        }
    }

    /**
     * 处理上架结果
     * @param msg
     */
    private void shelveBook(Message msg,int type){
        if (type == SHELVE_SINGLE){

            String messgae = msg.getData().getString("message");

            try {
                JSONObject jsonObject = new JSONObject(messgae);

                String message = null;
                if(VersionSetting.IS_CONNECT_INTERLIB3){
                    try {
                        message = jsonObject.getJSONArray("processList").getJSONObject(0).getString("MESSAGE");
                    } catch (JSONException e) {
                        message = JsonUtils.getMessageFromJson(jsonObject);
                    }

                }else{
                    message = jsonObject.getString("message");
                }

                if (message.contains("成功")) {
                    if (Flag.equals(IndexFragment.SHELVE_FLAG)){
                        //得到书的有关信息
                        //提交上架成功之后的日志
                        //uploadInventoryLog(VersionSetting.LOG_USER,VersionSetting.LOG_PASSWORD,VersionSetting.MACHINE_ID_LOG_INVENTORY,"204b",barcode,shlfNumber);
                        uploadInventoryLog2(Constant.LOG_USER,Constant.LOG_PASSWORD,Constant.machineId,"204b",barcode,shlfNumber,poolManager,mHandler);

                        ShelvesResult shelvesResult = (ShelvesResult) msg.getData().getSerializable("shelvesResult");

                        shelvesResult.getCheckBook().setShelfno(shlfNumber);
                        shelvesSuccessResultsList.add(shelvesResult);
                        addOneItem(shelvesResult);
                        xmlLoadingView.startDisappearAnimation();
                        return;
                    }

                    ShelvesResult shelvesResult2 = (ShelvesResult) msg.getData().getSerializable("shelvesResult");
                    shelvesSuccessResultsList.remove(shelvesResult2);
                    for (ScanReport item : scanfReportList){
                        if (item.getDataStr().equals(shelvesResult2.getCheckBook().getBookBarcode())){
                            scanfReportList.remove(item);
                            break;
                        }
                    }

                    shelvesResultsList.remove(shelvesResult2);
                    shelvesResultsSet.remove(shelvesResult2.getCheckBook().getBookBarcode());
                    successCount++;//成功num增加1
                    shelvesResult2.setMessage("0");
//                  shelvesResultsList.get(shelvesResultsList.lastIndexOf(shelvesResult2)).getCheckBook().setShelfno(shlfNumber);
//                  bookShelvesRecyclerAdapter.setShelveNo(shlfNumber);
                    bookShelvesRecyclerAdapter.notifyDataSetChanged();
                    changeShelveBtn_and_listText();

                }
                else if (message.contains("长度错误") || message.contains("不能处理") ){

                    if (Flag.equals(IndexFragment.SHELVE_FLAG)){
                        ShelvesResult shelvesResult = (ShelvesResult) msg.getData().getSerializable("shelvesResult");
                        shelvesResult.setMessage(message);
                        xmlLoadingView.startDisappearAnimation();
                        failItem(shelvesResult, 1);

                        return;
                    }
                    ToastUtil.showToastShortTime(InventoryActivity.this,message);
                    xmlLoadingView.startDisappearAnimation();

                }
                else{
                    JSONObject obj =jsonObject.getJSONArray("processList").getJSONObject(0);
                    if (Flag.equals(IndexFragment.SHELVE_FLAG)){
                        ShelvesResult shelvesResult = (ShelvesResult) msg.getData().getSerializable("shelvesResult");
                        shelvesResult.setMessage(obj.getString("MESSAGE"));
                        xmlLoadingView.startDisappearAnimation();
                        failItem(shelvesResult, 1);
                        return;
                    }
                    ToastUtil.showToastShortTime(InventoryActivity.this,obj.getString("MESSAGE"));
                    xmlLoadingView.startDisappearAnimation();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }else {

            //多本上架之后得到的json数据
            String message2 = msg.getData().getString("message");

            try {
                ShelvesResult shelvesResult2 = (ShelvesResult) msg.getData().getSerializable("shelvesResult");
                JSONObject jsonObject = new JSONObject(message2);


                String message = null;
                if(VersionSetting.IS_CONNECT_INTERLIB3){
                    try {
                        message = jsonObject.getJSONArray("processList").getJSONObject(0).getString("MESSAGE");
                    } catch (JSONException e) {
                        message = JsonUtils.getMessageFromJson(jsonObject);
                    }

                }else{
                    message = jsonObject.getString("message");
                }


                if (message.contains("成功")) {
                    //多本上架成功之后提交成功日志
                    //uploadInventoryLog(VersionSetting.LOG_USER,VersionSetting.LOG_PASSWORD,VersionSetting.MACHINE_ID_LOG_INVENTORY,"204b",barcode,shlfNumber);
                    uploadInventoryLog2(Constant.LOG_USER, Constant.LOG_PASSWORD, Constant.machineId, "204b", barcode, shlfNumber, poolManager, mHandler);

                    shelvesSuccessResultsList.remove(shelvesResult2);
                    for (ScanReport item : scanfReportList) {
                        if (item.getDataStr().equals(shelvesResult2.getCheckBook().getBookBarcode())) {
                            scanfReportList.remove(item);
                            break;
                        }
                    }

                    shelvesResultsList.remove(shelvesResult2);
                    shelvesResultsSet.remove(shelvesResult2.getCheckBook().getBookBarcode());

                    successCount++;//成功num增加1
                    shelvesResult2.setMessage("0");
                } else {
                    shelvesResult2.setMessage(message);
                }

                //访问次数增加1
                requestCount++;
                if (requestCount == totalCount) {
                    bookShelvesRecyclerAdapter.notifyDataSetChanged();
                    if (shelvesResult2.getMessage().equals("0")) {
                        xmlLoadingView.getTextView().setText("上架成功 [" + successCount + "] 本书");

                    } else {
                        xmlLoadingView.getTextView().setText("上架成功 [" + successCount + "] 本书" + "\n" + shelvesResult2.getMessage());

                    }
                    xmlLoadingView.getB1().setText("确定");
                    xmlLoadingView.refreshView();
                    xmlLoadingView.getB1().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            xmlLoadingView.startDisappearAnimation();
                            changeShelveBtn_and_listText();

                        }
                    });


                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 改变按钮状态
     */
    private void changeShelveBtn_and_listText(){

        tvBookNum.setText("[ " + shelvesResultsList.size() + " ]");
        tvSuccessNum.setText("[ " + shelvesSuccessResultsList.size() + " ]");

        if (shelvesSuccessResultsList.size() != 0) {
            btnShelves.setEnabled(true);
            btnShelves.setBackgroundResource(R.drawable.reload_btn_selector);
        }else{
            btnShelves.setEnabled(false);
            btnShelves.setBackgroundResource(R.drawable.not_selected_btn2);
        }
    }


    //设置音乐播放
    private void setPlayer(){
        if (mediaPlayer==null){
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer
                    .setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer player) {
                            player.seekTo(0);
                        }
                    });
        }

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        AudioManager audioService = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            return;
        }

        AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.beep);
        try {
            mediaPlayer.setDataSource(file.getFileDescriptor(),
                    file.getStartOffset(), file.getLength());
            file.close();
            mediaPlayer.setVolume(0.5f, 0.5f);
            mediaPlayer.prepare();
        } catch (IOException ioe) {

            mediaPlayer = null;
        }
    }


    /**创建弹窗*/
    private void createDialog(final String tips){
        if(mDialog==null) {
            mDialog = new HintDialog(this, "title", "message");
            mDialog.showDialog(R.layout.dialog_choice_layout, new HintDialog.IHintDialog() {


                @Override
                public void showWindowDetail(Window window, String title,
                                             final String message) {
                    TextView title_textview = (TextView) window.findViewById(R.id.title_textview);
                    title_textview.setText(tips);
                    //全部删除
                    TextView bt1 = (TextView) window.findViewById(R.id.positiveButton);
                    bt1.setText("全部");
                    //删除成功列
                    TextView bt2 = (TextView) window.findViewById(R.id.negativeButton);
                    bt2.setText("仅成功列");
                    //删除失败列
                    TextView bt3 = (TextView) window.findViewById(R.id.cancleButton);
                    bt3.setText("仅失败列");

                    ImageView cancel =(ImageView)window.findViewById(R.id.cancel);
                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDialog.dismissDialog();
                            mDialog = null;
                        }
                    });
                    bt1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDialog.dismissDialog();
                            mDialog = null;
                            clearDate(0);
                            //删除所有
                        }
                    });
                    bt2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            mDialog.dismissDialog();
                            mDialog = null;
                            clearDate(1);
                            //删除成功
                        }
                    });
                    bt3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDialog.dismissDialog();
                            mDialog = null;
                            //删除失败
                            clearDate(2);
                        }
                    });

                }

            });

        }
    }



    /**删除数据*/
    public void clearDate(int type){
        if (type==0){
            scanfReportList.clear();
            shelvesResultsSet.clear(); ;
            shelvesSuccessResultsList.clear();
            shelvesResultsList.clear();


        }else if (type ==1){
            for (ShelvesResult i1:shelvesSuccessResultsList){

                for (ScanReport item : scanfReportList){
                    if (item.getDataStr().equals(i1.getCheckBook().getBookBarcode())){
                        scanfReportList.remove(item);
                        Log.d("test",scanfReportList.size()+" scanfReportList");
                        break;
                    }
                }
                for (String i3:shelvesResultsSet){
                    if (i3.equals(i1.getCheckBook().getBookBarcode())) {
                        shelvesResultsSet.remove(i3);
                        Log.d("test", shelvesResultsSet.size() + " shelvesResultsSet");

                        break;
                    }
                }
            }
            shelvesResultsList.removeAll(shelvesSuccessResultsList);
            shelvesSuccessResultsList.clear();
            Log.d("test", shelvesResultsList.size() + " shelvesResultsList");
            Log.d("test", shelvesSuccessResultsList.size() + " shelvesSuccessResultsList");
        }else{
            List<ShelvesResult> shelvesResultsListTemp =new ArrayList<>();
            shelvesResultsListTemp.addAll(shelvesResultsList);
            shelvesResultsListTemp.removeAll(shelvesSuccessResultsList);
            List<ShelvesResult> shelvesFailResultsList =shelvesResultsListTemp;
            Log.d("test",shelvesResultsList.size()+" shelvesResultsListaaaaaaa");
            Log.d("test",shelvesFailResultsList.size()+" shelvesFailResultsListbbbbb");
            for (ShelvesResult i1:shelvesFailResultsList){

                for (ScanReport item : scanfReportList){
                    if (item.getDataStr().equals(i1.getCheckBook().getBookBarcode())){
                        scanfReportList.remove(item);
                        Log.d("test",scanfReportList.size()+" scanfReportList");
                        break;
                    }
                }

                for (String i3:shelvesResultsSet){
                    if (i3.equals(i1.getCheckBook().getBookBarcode())) {
                        shelvesResultsSet.remove(i3);
                        Log.d("test",  " shelvesResultsSet:"+shelvesResultsSet.size());
                        break;
                    }
                }

            }

            shelvesResultsList.removeAll(shelvesFailResultsList);

            Log.d("test", "储存书本:"+shelvesResultsList.size() );
            Log.d("test", " 操作成功:"+shelvesSuccessResultsList.size());
        }

        if (bookShelvesRecyclerAdapter!=null)
            bookShelvesRecyclerAdapter.notifyDataSetChanged();

        //删除后更改
        changeShelveBtn_and_listText();
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

    @Override
    protected void onDestroy() {
        if(bGetScanRecordFlg){
            //断开盘点仪后提交日志
            upLoadMachineLog(Constant.LOG_USER,Constant.LOG_PASSWORD,Constant.machineId,"4","盘点仪与手机断开连接");
        }

        mTag.ISO15693_Disconnect();
        bGetScanRecordFlg = false;//扫描结束后的参数设置为false
        b_inventoryThreadRun = false;
        m_reader.RDR_SetCommuImmeTimeout();
        m_reader.RDR_Close();
        inventoryList.clear();
        scanfReportList.clear();
//        m_devTypeList.clear();
//        m_connTypeList.clear();
        poolManager.stop();
        super.onDestroy();
    }


}
