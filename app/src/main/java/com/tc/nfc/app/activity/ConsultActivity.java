package com.tc.nfc.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tc.nfc.R;
import com.tc.nfc.app.adapter.BookConsultRecyclerAdapter;
import com.tc.nfc.app.utils.ToastUtil;
import com.tc.nfc.app.view.loadingview.XmlLoadingView;
import com.tc.nfc.core.task.ThreadPoolManager;
import com.tc.nfc.model.ShelvesResult;
import com.tc.nfc.util.NFCApplication;
import com.tc.nfc.app.view.RecycleViewDivider;
import com.tc.nfc.core.listener.ActionCallbackListener;
import com.tomergoldst.tooltips.ToolTip;
import com.tomergoldst.tooltips.ToolTipsManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class ConsultActivity extends BaseActivity implements View.OnClickListener{

    private final String TAG ="LoanActivity";

    private View view;
    private ConsultActivity that;
    //加载loadingView、messageWindow的层
    private RelativeLayout contentLayout;
    //加载列表recycleView的层
    private LinearLayout layout = null;
    private TextView titleTV,operaTV;
    private ImageView operaImage;

    private XmlLoadingView xmlLoadingView;
    //返回按钮
    private LinearLayout backImage;
    //图书条形码
    private String barcode;
    //判断重复list
    private Set<String> shelvesResultsSet ;

    //数据与适配器、列表
    private List<ShelvesResult> mdatas;
    private RecyclerView recyclerView;
    private BookConsultRecyclerAdapter recycleAdapter;
    private ItemTouchHelper itemTouchHelper ;
    //messageWindow管理
    ToolTipsManager mToolTipsManager;
    ToolTip.Builder builder=null;
    //上一被选择按钮的数据
    private ShelvesResult lastData =null;

    //标志是否正在借书，如果正在借书，则即使扫描到条码也不操作
    private boolean IS_DO_ACTIO=false;

    private ThreadPoolManager poolManager;
    private Timer timer;
    private TimerTask task;
    //设置3s延迟
    private final int SECONDS=2;
    private int time=SECONDS;

    private ShelvesResult currentShelvesResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
    }

    @Override
    protected void initContentView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_scan);
        mNFCApplication =(NFCApplication)this.getApplication();
    }

    /**
     * 初始化
     */
    public void initViews(){

        titleTV =(TextView)findViewById(R.id.tv_title_activity);
        titleTV.setText(R.string.previewbook);
        operaTV =(TextView)findViewById(R.id.operaText);
        operaTV.setText(R.string.scanbook_operaTV);
        operaImage=(ImageView)findViewById(R.id.operaImage);
        operaImage.setImageDrawable(getResources().getDrawable(R.drawable.checkbook_icon));
        backImage = (LinearLayout)findViewById(R.id.ll_activity_title_backpress);
        backImage.setOnClickListener(this);
        layout =(LinearLayout)findViewById(R.id.trendLayout);
        contentLayout =(RelativeLayout)findViewById(R.id.contentLayout);
        mdatas =new ArrayList<>();
        that=this;
        mToolTipsManager = new ToolTipsManager();
        //初始化loadingview
        xmlLoadingView =new XmlLoadingView(this);
        xmlLoadingView.setViewSize(0, 0);
        xmlLoadingView.setViewPadding(40, 50, 40, 50);
        xmlLoadingView.setText("正在查询图书..    ");
        //动画结束操作
        xmlLoadingView.getDisappearAnimation().setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                contentLayout.removeView(xmlLoadingView);
                IS_DO_ACTIO = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


        //线程池管理
        poolManager = new ThreadPoolManager(ThreadPoolManager.TYPE_FIFO, 5);
        poolManager.start();

        //判断书本是否重复
        shelvesResultsSet=new HashSet<>();

    }

    /**
     * 接受NFC请求
     * @param intent
     */
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        //2.解析获取Barcode
        mNFCApplication.getBookAction().getNFCBarcode(intent, new ActionCallbackListener<String>() {
            @Override
            public void onSuccess(String data) {

                barcode = data;
                if (barcode != null && !IS_DO_ACTIO) {
                    IS_DO_ACTIO = true;
                    xmlLoadingView.startAppearAnimation();
                    contentLayout.addView(xmlLoadingView);

                    //看是否能加入set中判断是否重复
                    if (!shelvesResultsSet.add(barcode)) {

                        xmlLoadingView.refreshView();
                        setTimer();
                        xmlLoadingView.getTextView().setText("该条码已经录入：\n" + barcode);
                        xmlLoadingView.getB1().setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                xmlLoadingView.startDisappearAnimation();
                                clearTimer();
                            }
                        });
                        return;
                    }
                    mNFCApplication.getBookAction().getBookData(barcode, poolManager, handler);
                }
            }

            @Override
            public void onFailure(String errorEvent, String message) {

            }
        });
    }


    public void setListener(){
        recycleAdapter.setAdapterListener(new BookConsultRecyclerAdapter.CheckOnClickListener() {


            @Override
            public void onClick(View v, int position, ShelvesResult shelvesResult) {
                //改变按钮选中状态
                Boolean isCheck = !shelvesResult.getCheckResult();
                closeMessageWindow();
                //判断当前按钮状态，如果为true被点击则新建message框
                if (isCheck) {

                    builder = new ToolTip.Builder(that, v, contentLayout
                            , shelvesResult.getMessage(), ToolTip.POSITION_ABOVE);

                    builder.setGravity(ToolTip.GRAVITY_CENTER);
                    builder.setAlign(ToolTip.ALIGN_RIGHT);
                    mToolTipsManager.show(builder.build());
                    shelvesResult.setCheckResult(isCheck);
                    lastData = shelvesResult;
                }
            }

            //当adapter被刷新时关闭message框并且改变状态
            public void reFresh() {
                closeMessageWindow();
            }
        });
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
            Log.d("XX",mdatas.get(position).getCheckBook().getBookBarcode());
            shelvesResultsSet.remove(mdatas.get(position).getCheckBook().getBookBarcode());
            mdatas.remove(position);
            recycleAdapter.notifyItemRemoved(position);

        }
    };

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

            recycleAdapter= new BookConsultRecyclerAdapter(this, mdatas);

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
            itemTouchHelper = new ItemTouchHelper(recyclerCallback);
            itemTouchHelper.attachToRecyclerView(recyclerView);

            layout.addView(view);
            setListener();

        }

    }
    public Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                //查询成功
                case 1:
                    xmlLoadingView.startDisappearAnimation();
                    currentShelvesResult = (ShelvesResult) msg.getData().getSerializable("shelvesResult");
                    //插入日志
                    mNFCApplication.getBookAction().addBookLog(barcode, poolManager, handler);
                    if (currentShelvesResult.getCheckBook().getIsbn()!=null) {
                        //获取图片
                        getImage(currentShelvesResult.getCheckBook().getIsbn().replace("-", ""));
                    }


                    break;
                //查询失败
                case 2:

                    xmlLoadingView.refreshView();

                    setTimer();
                    String messgae = msg.getData().getString("message");
                    String barcode =msg.getData().getString("barcode");
                    shelvesResultsSet.remove(barcode);
                    xmlLoadingView.getTextView().setText(messgae);
                    xmlLoadingView.getB1().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            xmlLoadingView.startDisappearAnimation();
                            clearTimer();
                        }
                    });
                    break;

                //计时器结束时回调
                case 3:
                    xmlLoadingView.getB1().setText(time +"s 后返回");

                    //如果倒计时结束
                    if (time==0){
                        xmlLoadingView.startDisappearAnimation();
                        clearTimer();
                        return;
                    }
                    time--;
                    break;

                case 4:
                    currentShelvesResult.setCheckResult(msg.getData().getBoolean("result"));
                    currentShelvesResult.setMessage(msg.getData().getString("message"));
                    changeView();
                    addData(currentShelvesResult);

                    break;

                case 6:

                    ToastUtil.showToastShortTime(ConsultActivity.this, "连接服务器失败");
                    String barcode1 =msg.getData().getString("barcode");
                    shelvesResultsSet.remove(barcode1);
                    xmlLoadingView.startDisappearAnimation();

                    break;
            }
        }
    };

    public void getImage(final String isbn){
        mNFCApplication.getmSearchAction().getImage(isbn, new ActionCallbackListener<Map<String, String>>() {
            @Override
            public void onSuccess(Map<String, String> data) {

                currentShelvesResult.getCheckBook().setImageUrl(data.get(isbn));
            }

            @Override
            public void onFailure(String errorEvent, String message) {

            }
        });
    }

    public void addData(ShelvesResult shelvesResult) {

        mdatas.add(0,shelvesResult);
        recycleAdapter.notifyItemInserted(0);
        recycleAdapter.notifyItemRangeChanged(0, mdatas.size());
        recyclerView.smoothScrollToPosition(0);
        //关闭loadingview
        xmlLoadingView.startDisappearAnimation();
    }

    /**
     * 关闭MessageWindow
     */
    public void closeMessageWindow(){
        //如果该页面上有message框则关闭，并改变打开message框的data的按钮状态为false
        if (builder != null) {
            mToolTipsManager.findAndDismiss(builder.build().getAnchorView());
            //改变打开message框的data的按钮状态
            lastData.setCheckResult(false);
            builder = null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ll_activity_title_backpress:
                //显示窗口

//                startActivity(MainActivity.class);
                this.finish();
                overridePendingTransition(R.anim.slide_left_ins, R.anim.slide_right_outs);
                break;

        }
    }
    /**
     * 跳转页面
     * @param activityClass 目标页面Class
     */
    public void startActivity(Class<? extends Activity> activityClass){
        Intent intent = new Intent();
        intent.setClass(this, activityClass);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_left_ins, R.anim.slide_right_outs);
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
                message.what = 3;
                handler.sendMessage(message);
            }
        };
        //延时0s后执行，1s执行一次
        timer.schedule(task, 0, 1000);
    }

    /**
     * 清空计时器
     */
    private void clearTimer(){
        //清空计时器
        if (timer!=null&task!=null){
            timer.cancel();
            task.cancel();
            timer=null;
            task=null;
        }
        //重新设置倒计时3秒
        time=SECONDS;
    }

    public boolean findBarcodeResult(String barcode){
        for (ShelvesResult shelvesResult : mdatas){
            if (shelvesResult.getCheckBook().getBookBarcode().equals(barcode)&shelvesResult.getCheckResult()){
                return true;
            }
        }
       return false;
    }

}
