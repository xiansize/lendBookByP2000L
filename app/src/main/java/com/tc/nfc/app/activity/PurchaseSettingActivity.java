package com.tc.nfc.app.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.jaredrummler.materialspinner.MaterialSpinner;
import com.kyleduo.switchbutton.SwitchButton;
import com.tc.nfc.R;
import com.tc.nfc.app.view.HintDialog;
import com.tc.nfc.core.listener.ActionCallbackListener;
import com.tc.nfc.model.TotalPurchase;
import com.tc.nfc.util.DBManager;
import com.tc.nfc.util.DatabaseHelper;
import com.tc.nfc.util.DownLoadUtil;
import com.tc.nfc.util.NFCApplication;

import java.util.Map;

public class PurchaseSettingActivity extends BaseActivity implements View.OnClickListener
        ,CompoundButton.OnCheckedChangeListener{
    public NFCApplication mNFCApplication;

    private LinearLayout backImage;
    private ScrollView content;
    private EditText purchaseSettingEt1,purchaseSettingEt2;
    private TextView totalCopies,totalPrice,totalAcqPrice,totalSearchNum,
            todayCopies,todayPrice,todayAcqPrice,todaySearchNum,downPurchaseData,uploadPurchaseData,
            localBook, serverBook,localOrder,serverOrder;
    private SwitchButton switchBtn1,switchBtn2,switchBtn3,switchBtn4,switchBtn5;
    private MaterialSpinner spinner;
    private RelativeLayout spinnerLayout;
    private LinearLayout purchaseSettingL2,purchaseSettingL3;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private HintDialog mDialog = null;
    private DBManager dbManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        backImage = (LinearLayout)findViewById(R.id.ll_activity_title_backpress);
        backImage.setOnClickListener(this);
        content =(ScrollView)findViewById(R.id.scrollView);
        purchaseSettingEt1 =(EditText)findViewById(R.id.purchaseSettingEt1);
        purchaseSettingEt2 =(EditText)findViewById(R.id.purchaseSettingEt2);
        switchBtn1 =(SwitchButton)findViewById(R.id.sb_text1);
        switchBtn2 =(SwitchButton)findViewById(R.id.sb_text2);
        switchBtn3 =(SwitchButton)findViewById(R.id.sb_text3);
        switchBtn4 =(SwitchButton)findViewById(R.id.sb_text4);
        switchBtn5 =(SwitchButton)findViewById(R.id.sb_text5);
        spinner = (MaterialSpinner)findViewById(R.id.spinner);
        spinnerLayout =(RelativeLayout)findViewById(R.id.spinnerLayout);
        purchaseSettingL2 =(LinearLayout)findViewById(R.id.purchaseSettingL2);
        purchaseSettingL3 =(LinearLayout)findViewById(R.id.purchaseSettingL3);
        spinner.setItems("公共馆", "高校馆");

        totalCopies =(TextView)findViewById(R.id.totalCopies);
        totalPrice =(TextView)findViewById(R.id.totalPrice);
        totalAcqPrice =(TextView)findViewById(R.id.totalAcqPrice);
        totalSearchNum =(TextView)findViewById(R.id.totalSearchNum);
        todayCopies =(TextView)findViewById(R.id.todayCopies);
        todayPrice =(TextView)findViewById(R.id.todayPrice);
        todayAcqPrice =(TextView)findViewById(R.id.todayAcqPrice);
        todaySearchNum =(TextView)findViewById(R.id.todaySearchNum);
        downPurchaseData =(TextView)findViewById(R.id.downPurchaseData);
        localBook =(TextView)findViewById(R.id.localBook);
        serverBook =(TextView)findViewById(R.id.serverBook);
        localOrder =(TextView)findViewById(R.id.localOrder);
        serverOrder =(TextView)findViewById(R.id.serverOrder);
        uploadPurchaseData =(TextView)findViewById(R.id.uploadPurchaseData);
        dbManager =new DBManager(this);

        localOrder.setText(String.valueOf(dbManager.getCountsOfTable()));
        initView();
        setListener();

    }



    @Override
    protected void initContentView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_purchase_setting);
        mNFCApplication =(NFCApplication)this.getApplication();
    }

    private void initView() {

    }
    public void setListener(){

        DownLoadUtil.setListener(new DownLoadUtil.DownLoadCallbackListener() {
            @Override
            public void downLoadResult(Boolean result, String message) {
                if (result){

                    DownLoadUtil.getFilePathReport(dbManager);

                    //导入数据
                }else{


                }
            }

            @Override
            public void sendProgress(String persent,int progress) {

            }

            @Override
            public void sendItemCount(String count) {
                localBook.setText(count);
                Log.d("XX",count+"");
            }

            @Override
            public void sendUploadResult(Boolean result,String message) {
                if (result){
                    createDialog("提示", "上传成功", R.layout.dialog_hint_layout);
                    if (dbManager.clearDataByTable(DatabaseHelper.TABLE_PURCHASE)>0){
                        localOrder.setText(String.valueOf(dbManager.getCountsOfTable()));
                    }

                }else{
                    createDialog("提示", message.equals("")?"上传失败":message, R.layout.dialog_hint_layout);
                }
            }
        });

        content.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                //purchaseSettingEt1.setCursorVisible(false);//失去光标
                purchaseSettingEt1.clearFocus();
                purchaseSettingEt2.clearFocus();
                //purchaseSettingEt2.setCursorVisible(false);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                return false;
            }
        });

        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {

            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {

            }
        });
        downPurchaseData.setOnClickListener(this);
        uploadPurchaseData.setOnClickListener(this);
        switchBtn1.setOnCheckedChangeListener(this);
        switchBtn2.setOnCheckedChangeListener(this);
        switchBtn3.setOnCheckedChangeListener(this);
        switchBtn4.setOnCheckedChangeListener(this);
        switchBtn5.setOnCheckedChangeListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.ll_activity_title_backpress:
                if (!purchaseSettingEt1.getText().toString().equals("")){
                    String libCode =purchaseSettingEt1.getText().toString();
                    editor.putString("codeString", libCode);
                    editor.commit();
                }else{
                    editor.putString("codeString", "");
                    editor.commit();
                }

                finish();
                overridePendingTransition(R.anim.slide_left_ins, R.anim.slide_right_outs);
                break;

            case R.id.downPurchaseData:

                //判断是否存在SDK
                if (DownLoadUtil.isSdCardExist()){

                    DownLoadUtil.downLoad("http://192.168.0.169:28085/ATMCenter_test/service/bibliosMiddle/downData?imei=76UBBL422D27&type=2",
                            DownLoadUtil.PURCHASE_DIRECTORY_NAME,DownLoadUtil.PURCHASE_FilE_NAME);
                }else{
                   //toast
                }
                break;

            case R.id.uploadPurchaseData:
                Log.d("XX", "上传");
                createDialog("上传方式", "请选择上传方式:", R.layout.dialog_choice_layout);

                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch(buttonView.getId()){
            case R.id.sb_text1:
                Log.d("XX", isChecked + "");
                break;
            case R.id.sb_text2:
                Log.d("XX", isChecked + "");

                break;
            case R.id.sb_text3:
                Log.d("XX", isChecked + "");
                if (isChecked){
                    switchBtn4.setChecked(false);
                    purchaseSettingL2.setVisibility(View.VISIBLE);
                    purchaseSettingL3.setVisibility(View.INVISIBLE);
                    getServerData();
                }
                 
//                editor.putBoolean("isOffline", isChecked);
//                editor.commit();
                break;
            case R.id.sb_text4:
                if (isChecked){
                    switchBtn3.setChecked(false);
                    spinnerLayout.setVisibility(View.VISIBLE);
                    purchaseSettingL2.setVisibility(View.INVISIBLE);
                    purchaseSettingL3.setVisibility(View.VISIBLE);
                    getTotalPurchaseData();
                }
                else{
                    spinnerLayout.setVisibility(View.GONE);
                    purchaseSettingL2.setVisibility(View.VISIBLE);
                    purchaseSettingL3.setVisibility(View.INVISIBLE);
                    getServerData();
                }
                Log.d("XX", isChecked + "");
//                editor.putBoolean("isOnline", isChecked);
//                editor.commit();
                break;

            case R.id.sb_text5:

        }
    }

    public void getServerData() {
        localBook.setText(String.valueOf(dbManager.getCountOfReports()));
        mNFCApplication.getmPurchaseAction().getServerData("76UBBL422D27", new ActionCallbackListener<Map<String, String>>() {
            @Override
            public void onSuccess(Map<String, String> data) {
                serverBook.setText(data.get("offLineDataCount"));
                serverOrder.setText(data.get("bibliosOrderDataCount"));
            }

            @Override
            public void onFailure(String errorEvent, String message) {

            }
        });
    }

    public void getTotalPurchaseData() {
        mNFCApplication.getmPurchaseAction().getTotalPurchaseData("76UBBL422D27", new ActionCallbackListener<TotalPurchase>() {
            @Override
            public void onSuccess(TotalPurchase data) {
                totalCopies.setText(data.getTotalCopies());
                totalPrice.setText(data.getTotalPrice());
                totalAcqPrice.setText(data.getTotalAcqPrice());
                totalSearchNum.setText(data.getNowBatchnoSearchTotalNum());
                todayCopies.setText(data.getTodayCopies());
                todayPrice.setText(data.getTodayPrice());
                todayAcqPrice.setText(data.getTodayAcqPrice());
                todaySearchNum.setText(data.getTodaySearchNum());
            }

            @Override
            public void onFailure(String errorEvent, String message) {

            }
        });
    }


    public void onResume(){

        sp = getSharedPreferences("purchaseSetting", Context.MODE_WORLD_READABLE);
        editor = sp.edit();

        switchBtn1.setChecked(sp.getBoolean("isShowImage", false));
        switchBtn2.setChecked(sp.getBoolean("isPromptsound", false));
        switchBtn3.setChecked(sp.getBoolean("isOffline", false));
        switchBtn4.setChecked(sp.getBoolean("isOnline", false));
        switchBtn5.setChecked(sp.getBoolean("NotDataShow", false));
        purchaseSettingEt1.setText(sp.getString("codeString", ""));
        if (!sp.getString("fazhi", "").equals("")||!sp.getString("fazhi", "").equals("0")){
            purchaseSettingEt2.setText(sp.getString("fazhi", ""));
        }

        spinner.setSelectedIndex(sp.getInt("isPubLib", 0));

        super.onResume();

    }

    public void onPause(){
        editor.putBoolean("isShowImage", switchBtn1.isChecked());
        editor.putBoolean("isPromptsound", switchBtn2.isChecked());
        editor.putBoolean("isOffline", switchBtn3.isChecked());
        editor.putBoolean("isOnline", switchBtn4.isChecked());
        editor.putBoolean("NotDataShow", switchBtn5.isChecked());
        editor.putInt("isPubLib",spinner.getSelectedIndex());
        if (purchaseSettingEt2.getText().toString().equals("")||purchaseSettingEt2.getText()==null) {
            editor.putString("fazhi", "0");

        }else{
            editor.putString("fazhi", purchaseSettingEt2.getText().toString());
        }


        editor.commit();
        super.onPause();
    }

    /**
     * 创建弹框
     * @param title
     * @param message
     */
    private void createDialog(String title,String message,final int layout){
        if(mDialog==null){
            mDialog = new HintDialog(this,title,message);
            mDialog.showDialog(layout, new HintDialog.IHintDialog() {

                @Override
                public void showWindowDetail(Window window, String title,
                                             final String message) {
                    TextView msg = (TextView) window.findViewById(R.id.message);
                    msg.setText(message);
                    switch(layout){
                        case R.layout.dialog_choice_layout:
                            TextView update = (TextView) window.findViewById(R.id.positiveButton);
                            TextView add = (TextView) window.findViewById(R.id.negativeButton);
                            update.setText("重新上传");
                            add.setText("追加上传");
                            update.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Log.d("XX", "重新上传");
                                    DownLoadUtil.upload(dbManager, "http://192.168.0.169:28085/ATMCenter_test/service/bibliosMiddle/syncBibliosOrderData", "2");
                                    mDialog.dismissDialog();
                                    mDialog =null;
                                }
                            });
                            add.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Log.d("XX","追加上传");
                                    DownLoadUtil.upload(dbManager, "http://192.168.0.169:28085/ATMCenter_test/service/bibliosMiddle/syncBibliosOrderData", "1");
                                    mDialog.dismissDialog();
                                    mDialog =null;
                                }
                            });
                            break;

                        case R.layout.dialog_hint_layout:
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
}
