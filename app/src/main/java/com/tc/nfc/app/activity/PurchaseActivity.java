package com.tc.nfc.app.activity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tc.nfc.R;
import com.tc.nfc.app.adapter.BookItemAdapter;
import com.tc.nfc.app.adapter.LibPurchaseAdapter;
import com.tc.nfc.app.adapter.PurchaseAdapter;
import com.tc.nfc.app.view.HintDialog;
import com.tc.nfc.app.view.MaxListView;
import com.tc.nfc.core.listener.ActionCallbackListener;
import com.tc.nfc.model.Book;
import com.tc.nfc.model.LibPurchase;
import com.tc.nfc.model.PurchaseNum;
import com.tc.nfc.util.DBManager;
import com.tc.nfc.util.DatabaseHelper;
import com.tc.nfc.util.NFCApplication;
import com.tc.nfc.util.ScanBroadCastReceiver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PurchaseActivity extends BaseActivity implements View.OnClickListener{
    public NFCApplication mNFCApplication;

    //图书信息
    private ViewPager bookViewPager ;
    //订购list,其他馆藏list
    private MaxListView listView2,listView1;
    //返回按钮，配置按钮
    private LinearLayout backImage,setting;
    //消息框
    private HintDialog mDialog = null;
    private TextView library,purchase,loan,hotsell,tvTitle;

    private BookItemAdapter bookItemAdapter ;
    private PurchaseAdapter mPurchaseAdapter;
    private LibPurchaseAdapter mLibPurchaseAdapter;

    private List<Book> bookList;
    private List<Map<String,Object>> list1 ;
    private List<LibPurchase> list2;

    //广播接收
    private ScanBroadCastReceiver mScanBroadCastReceiver;
    private IntentFilter scanDataIntentFilter;

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private DBManager dbManager;

    //isbnList
    private StringBuffer ISBNList = new StringBuffer();
    //当前isbn
    private String ISBN =null;
    //是否公共馆
    private int isPublic=0;
    //当前显示的book
    private Book currentBook =null;
    //当前的订购信息
    private PurchaseNum currentPurchaseNum =null;
    //是否可以播放bee
    private Boolean shouldPlayBeep=true;
    private MediaPlayer mediaPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        listView1 =(MaxListView)findViewById(R.id.purchaseListview1);
        listView2 =(MaxListView)findViewById(R.id.purchaseListview2);
        bookViewPager =(ViewPager)findViewById(R.id.viewPager);
        backImage = (LinearLayout)findViewById(R.id.ll_activity_title_backpress);
        setting =(LinearLayout)findViewById(R.id.ll_activity_title_setting);
        tvTitle =(TextView)findViewById(R.id.tv_title_activity);
        tvTitle.setText(R.string.purchasebook);
        backImage.setOnClickListener(this);
        setting.setOnClickListener(this);
        init();
        bookItemAdapter =new BookItemAdapter(this,bookList);
        bookViewPager.setAdapter(bookItemAdapter);
        mPurchaseAdapter =new PurchaseAdapter(this,list1);
        mLibPurchaseAdapter =new LibPurchaseAdapter(this,list2);

        listView1.setAdapter(mPurchaseAdapter);
        listView2.setAdapter(mLibPurchaseAdapter);

        library =(TextView)findViewById(R.id.library);
        purchase =(TextView)findViewById(R.id.purchase);
        loan =(TextView)findViewById(R.id.loan);
        hotsell =(TextView)findViewById(R.id.hotsell);

        mScanBroadCastReceiver =new ScanBroadCastReceiver();
        scanDataIntentFilter = new IntentFilter();
        scanDataIntentFilter.addAction("com.android.scancontext");
        dbManager =new DBManager(this);

        setListener();
        setPlayer();


    }

    @Override
    protected void initContentView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_purchase);
        mNFCApplication =(NFCApplication)this.getApplication();

    }

    public void initViews(){

    }

    public void getImage(){
        mNFCApplication.getmSearchAction().getImage(ISBNList.toString(), new ActionCallbackListener<Map<String, String>>() {
            @Override
            public void onSuccess(Map<String, String> data) {

                bookItemAdapter.setImageUrl(data);
                bookItemAdapter.notifyDataSetChanged();

            }

            @Override
            public void onFailure(String errorEvent, String message) {

            }
        });
    }
    public void onResume(){
        Log.d("XX", "resume");

        sp = getSharedPreferences("purchaseSetting", Context.MODE_WORLD_READABLE);
        editor = sp.edit();

        if(sp.getBoolean("isShowImage", false)){

            bookItemAdapter.setShow(true);
        }
        else{
            bookItemAdapter.setShow(false);
        }
        isPublic =sp.getInt("isPubLib", 0);

        registerReceiver(mScanBroadCastReceiver, scanDataIntentFilter);

        Map map ;
        String libCode =sp.getString("codeString","");
        Log.d("XX", libCode);
        if (libCode.equals("")){
            list1.clear();
            map = new HashMap();
            map.put("libCode", "");
            map.put("num", 0);
            list1.add(map);
            mPurchaseAdapter.notifyDataSetChanged();

        }else{
            list1.clear();
            if (libCode.contains(";")){
                String[] libCodes =libCode.split(";");
                for (String code:libCodes){
                    map = new HashMap();
                    map.put("libCode", code);
                    map.put("num", 0);
                    list1.add(map);
                }
            }else{
                map = new HashMap();
                map.put("libCode", libCode);
                map.put("num", 0);
                list1.add(map);
            }
            mPurchaseAdapter.notifyDataSetChanged();
        }


        super.onResume();
    }

    public void onPause(){
        unregisterReceiver(mScanBroadCastReceiver);
        super.onPause();
    }


    private void setPlayer(){
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        AudioManager audioService = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            shouldPlayBeep = false;
        }
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer
                .setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer player) {
                        player.seekTo(0);
                    }
                });

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

    public void setListener(){

        bookViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                currentBook=bookList.get(position);
                getPurchaseData(currentBook.getBookrecno());


            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mScanBroadCastReceiver.setSendIsbnListener(new ScanBroadCastReceiver.sendIsbnListener() {
            @Override
            public void sendIsbn(String isbn) {
                reSetList();
                ISBN = isbn;
                if (ISBN != null) {

                    if (sp.getBoolean("isOnline", false)){
                        ISBNList.append(ISBN);

                        getImage();
                        getPurchaseBookSingle();
                        getOtherLibPurchaseData();

                    }
                    else{
                        Cursor book = dbManager.getReport(ISBN);

//                        if (book.getCount() > 0) {
//                            title.setText(book.getString(book.getColumnIndex("题名")));
//                            Isbn.setText(book.getString(book.getColumnIndex("ISBN")));
//                            author.setText(book.getString(book.getColumnIndex("作者")));
//                            price.setText(book.getString(book.getColumnIndex("价格")));
//                            publisher.setText(book.getString(book.getColumnIndex("出版社")));
//                            publishDate.setText(book.getString(book.getColumnIndex("出版日期")));
//                            classNo.setText(book.getString(book.getColumnIndex("分类号")));
//                        } else {
//                            Isbn.setText(ISBN);
//                        }
                    }

                }

            }
        });

        mPurchaseAdapter.setListener(new PurchaseAdapter.Listener() {
            @Override
            public void sureClick(int num, String codeLib) {
                if (num == 0) {
                    createDialog("提示", "订购数量不能为空!", R.layout.dialog_hint_layout, null, null, null,0);
                    return;
                }
                if(ISBN==null){
                    createDialog("提示", "未扫描书本或没有此书!", R.layout.dialog_hint_layout, null, null, null,0);
                    return;
                }
                if (currentPurchaseNum ==null){
                    createDialog("提示", "未有订购信息，请重新扫描!", R.layout.dialog_hint_layout, null, null, null,0);
                    return;
                }

                if (sp.getBoolean("isOnline", false)){

                    if (!currentPurchaseNum.getCopiesSumByBatchno().equals("0")){
                        createDialog("订购方式", "您已订购过此书，请选择订购方式。", R.layout.dialog_choice_layout, String.valueOf(num), "0", codeLib,1);

                    }
                    else{
                        Log.d("XX",String.valueOf(num)+" "+codeLib);
                        //在线
                        purchaseOnLine(String.valueOf(num), "", codeLib);
                    }


                }else if (!sp.getBoolean("isOnline", false)||sp.getBoolean("isOffline", true)){
                    //离线
                    purchaseOffLine(String.valueOf(num), codeLib);
                }


            }
        });

    }

    public void init(){
        list1 =new ArrayList<>();
        Map map =new HashMap();
        map.put("libCode", "");
        map.put("num", 0);


        list1.add(map);

        list2 =new ArrayList<>();
        LibPurchase mLibPurchase =new LibPurchase();
        mLibPurchase.setLibName("图书馆名");
        mLibPurchase.setQuantity("馆藏");
        list2.add(mLibPurchase);

        bookList = new ArrayList<>();

        Book book =new Book();
        book.setBookTitle("");
        book.setIsbn("");
        book.setAuthor("");
        book.setPrice("");
        book.setPublisher("");
        book.setBookDate("");
        book.setClassNo("");
        book.setBookrecno("");
        bookList.add(book);

    }


    public void getPurchaseBookSingle(){
        mNFCApplication.getmPurchaseAction().getBookSingle(ISBN, "", "76UBBL422D27", new ActionCallbackListener<List<Book>>() {
            @Override
            public void onSuccess(List<Book> data) {

                currentBook = data.get(0);
                getPurchaseData(currentBook.getBookrecno());

                bookList.clear();
                for (Book book : data) {

                    bookList.add(book);
                }
                bookItemAdapter.notifyDataSetChanged();
                bookViewPager.setAdapter(bookItemAdapter);

            }

            @Override
            public void onFailure(String errorEvent, String message) {

            }
        });
    }

    public void purchaseOnLine(String copies,String action,String orderLibLocal){
        mNFCApplication.getmPurchaseAction().purchaseBookOnline(currentBook, copies, action, orderLibLocal, new ActionCallbackListener<Boolean>() {
            @Override
            public void onSuccess(Boolean data) {
                if (data) {
                    createDialog("提示", "订购成功", R.layout.dialog_hint_layout, null, null, null,0);
                } else {
                    createDialog("提示", "订购失败", R.layout.dialog_hint_layout, null, null, null,0);
                }
            }

            @Override
            public void onFailure(String errorEvent, String message) {

            }
        });
    }
    public void purchaseOffLine(String copies,String orderLibLocal){


        Cursor purchase = dbManager.getPurchase(ISBN);

        if (purchase.getCount() > 0) {

            String oldNum = purchase.getString(purchase.getColumnIndex(DatabaseHelper.KEY_PURCHASE_NUMS));
            Log.d("XX", "oldNum" + oldNum);
            Log.d("XX", "CODELIB" + purchase.getString(purchase.getColumnIndex(DatabaseHelper.KEY_PURCHASE_ORDERLIBLOCAL)));

            createDialog("订购方式", "您已订购过此书，请选择订购方式。", R.layout.dialog_choice_layout, String.valueOf(copies), oldNum, orderLibLocal,-1);

        } else {
            if (dbManager.insertPurchase(ISBN, String.valueOf(copies), "76UBBL422D27", orderLibLocal) > 0) {
                createDialog("提示", "订购成功", R.layout.dialog_hint_layout, null, null, null,0);
            } else {
                createDialog("提示", "订购失败", R.layout.dialog_hint_layout, null, null, null, 0);
            }
        }
    }
    public void getPurchaseData(String bookrecno){
        if (bookrecno.equals("0")){
            library.setText("0");
            purchase.setText("0");
            loan.setText("0");
            hotsell.setText("0");

            if (sp.getBoolean("NotDataShow", false)&&shouldPlayBeep && mediaPlayer != null) {
                mediaPlayer.start();
            }
            return;
        }
        mNFCApplication.getmPurchaseAction().getPurchaseData(bookrecno, new ActionCallbackListener<PurchaseNum>() {
            @Override
            public void onSuccess(PurchaseNum data) {

                currentPurchaseNum = data;
                library.setText(data.getHoldingSum());
                purchase.setText(data.getCopiesSumByBatchno() + "/" + data.getOrderSum());
                loan.setText(data.getCirculateSum());
                hotsell.setText(data.getCommendSum());

                if (data.getHoldingSum().equals("0")&&data.getCopiesSumByBatchno().equals("0") &&sp.getBoolean("NotDataShow", false)){
                    if (shouldPlayBeep && mediaPlayer != null) {
                        mediaPlayer.start();
                    }
                }
                int fazhi;
                try {
                    fazhi = Integer.parseInt(data.getHoldingSum()) / Integer.parseInt(data.getHoldingSum());
                }catch (ArithmeticException e){
                    fazhi =0;
                }
                if (fazhi>Integer.parseInt(sp.getString("fazhi", "0"))&&sp.getBoolean("NotDataShow", false)){
                    if (shouldPlayBeep && mediaPlayer != null) {
                        mediaPlayer.start();
                    }
                }

            }


            @Override
            public void onFailure(String errorEvent, String message) {

            }
        });

    }


    public void getOtherLibPurchaseData(){
        mNFCApplication.getmPurchaseAction().getOtherLibPurchaseData(ISBN, String.valueOf(isPublic), new ActionCallbackListener<List<LibPurchase>>() {
            @Override
            public void onSuccess(List<LibPurchase> list) {
                for (LibPurchase libPurchase : list) {
                    list2.add(libPurchase);
                }
                mLibPurchaseAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String errorEvent, String message) {

            }
        });
    }

    public void reSetList(){
        //清空其他馆藏信息
        list2.clear();
        LibPurchase mLibPurchase =new LibPurchase();
        mLibPurchase.setLibName("图书馆名");
        mLibPurchase.setQuantity("馆藏");
        list2.add(mLibPurchase);

        //设置查询中的信息
        bookItemAdapter.setSearch(ISBN);

        library.setText("");
        purchase.setText("");
        loan.setText("");
        hotsell.setText("");

    }
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.ll_activity_title_backpress:

                finish();
                overridePendingTransition(R.anim.slide_left_ins, R.anim.slide_right_outs);
                break;

            case R.id.ll_fragment_title_setting:
                startActivity(PurchaseSettingActivity.class);
                break;

        }

    }
    /**
     * 创建弹框
     * @param title
     * @param message
     */
    private void createDialog(String title,String message,final int layout,final String num, final String oldNum,final String codeLib,final int isOnline){
        if(mDialog==null){
            mDialog = new HintDialog(this,title,message);
            mDialog.showDialog(layout, new HintDialog.IHintDialog() {

                @Override
                public void showWindowDetail(Window window, String title,
                                             final String message) {

                    switch(layout){
                        case R.layout.dialog_choice_layout:
                            TextView update = (TextView) window.findViewById(R.id.positiveButton);
                            TextView add = (TextView) window.findViewById(R.id.negativeButton);

                            update.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mDialog.dismissDialog();
                                    mDialog =null;
                                    if (isOnline>0){
                                        purchaseOnLine(String.valueOf(num), "update", codeLib);
                                    }else{
                                        if (dbManager.updatePurchase(ISBN, num, "76UBBL422D27", codeLib)) {
                                            createDialog("提示", "订购成功", R.layout.dialog_hint_layout, null, null, null,0);
                                        } else {
                                            createDialog("提示", "订购失败", R.layout.dialog_hint_layout, null, null, null,0);
                                        }
                                    }

                                }
                            });
                            add.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Log.d("XX","追加");
                                    mDialog.dismissDialog();
                                    mDialog =null;
                                    if (isOnline>0){
                                        purchaseOnLine(String.valueOf(num), "add", codeLib);
                                    }else{
                                        if (dbManager.updatePurchase(ISBN, String.valueOf(Integer.parseInt(num)+Integer.parseInt(oldNum)),"76UBBL422D27",codeLib)) {
                                            createDialog("提示", "订购成功", R.layout.dialog_hint_layout, null, null, null,0);
                                        }else{
                                            createDialog("提示", "订购失败", R.layout.dialog_hint_layout, null, null, null,0);
                                        }
                                    }



                                }
                            });
                            break;

                        case R.layout.dialog_hint_layout:
                            TextView msg = (TextView) window.findViewById(R.id.message);
                            msg.setText(message);
                            TextView ok = (TextView) window.findViewById(R.id.positiveButton);
                            ok.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    mDialog.dismissDialog();
                                    mDialog =null;
                                }

                            });
                            break;
                    }


                }
            });
        }
    }
    /**
     * 跳转页面
     * @param activityClass 目标页面Class
     */
    public void startActivity(Class activityClass){
        Intent intent = new Intent();
        intent.setClass(this, activityClass);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
    }



}
