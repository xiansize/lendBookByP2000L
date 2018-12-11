package com.tc.nfc.app.activity;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tc.api.VersionSetting;
import com.tc.nfc.R;
import com.tc.nfc.app.utils.ToastUtil;
import com.tc.nfc.app.view.HintDialog;
import com.tc.nfc.core.listener.ActionCallbackListener;
import com.tc.nfc.util.NFCApplication;

import org.json.JSONException;
import org.json.JSONObject;

public class NoCardInputActivity extends BaseActivity implements View.OnClickListener{

    private NFCApplication mNFCApplication;

    private LinearLayout llBackPress;
    private TextView tvTitle;
    private EditText etReaderId,etReaderPassword;
    private Button btnNoCardInput;
    private HintDialog alertDialog;

    @Override
    protected void initContentView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_no_card_input);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();

    }


    private void initView() {
        mNFCApplication = (NFCApplication) getApplication();

        llBackPress = (LinearLayout) findViewById(R.id.ll_activity_title_backpress);
        llBackPress.setVisibility(View.GONE);

        tvTitle = (TextView) findViewById(R.id.tv_title_activity);
        tvTitle.setText("无卡输入");

        etReaderId = (EditText) findViewById(R.id.etNoCardInputReaderId);
        etReaderPassword = (EditText) findViewById(R.id.etNoCardInputReaderPassword);
        if(VersionSetting.IS_HONGKOU)
            etReaderPassword.setVisibility(View.INVISIBLE);

        btnNoCardInput = (Button) findViewById(R.id.btnNoCardInput);
        btnNoCardInput.setOnClickListener(this);


    }


    /**
     * 验证读者
     * @param readerId
     * @param readerPassword
     */
    private void comfirmReaderInfo(final String readerId, String readerPassword) {
        mNFCApplication.getLoginAction().login(readerId, readerPassword, new ActionCallbackListener<Void>() {
            @Override
            public void onSuccess(Void data) {
                alertDialog.dismissDialog();
                Intent intent = new Intent();
                intent.putExtra("ReadId",readerId);
                setResult(RESULT_OK,intent);
                finish();
            }

            @Override
            public void onFailure(String errorEvent, String message) {
                alertDialog.dismissDialog();
                ToastUtil.showToastShortTime(NoCardInputActivity.this,message);
            }
        });
    }



    //验证读者
    private void judgeReaderInfo(String readerId){
        Intent intent = new Intent();
        intent.putExtra("ReadId",readerId);
        setResult(RESULT_OK,intent);
        finish();
    }








    /**
     * 只需要输入读者的账号
     * @param readerId
     */
    private void comfirmReaderInfo(final String readerId){

        mNFCApplication.getUserAction().getReaderInfo(readerId, new ActionCallbackListener<JSONObject>() {
            @Override
            public void onSuccess(JSONObject data) {

                try {
                    alertDialog.dismissDialog();
                    if(data.getString("success").equals("true")){
                        Intent intent = new Intent();
                        intent.putExtra("ReadId",readerId);
                        setResult(RESULT_OK,intent);
                        finish();
                    }else{
                        alertDialog.dismissDialog();
                        ToastUtil.showToastShortTime(NoCardInputActivity.this,"验证失败");
                    }

                } catch (JSONException e) {
                    alertDialog.dismissDialog();
                    ToastUtil.showToastShortTime(NoCardInputActivity.this,"验证失败");
                }
            }

            @Override
            public void onFailure(String errorEvent, String message) {
                alertDialog.dismissDialog();
                ToastUtil.showToastShortTime(NoCardInputActivity.this,message);
            }
        });
    }




    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnNoCardInput:
//                showAlertDialog();
                String readerId = etReaderId.getText().toString().trim();
                judgeReaderInfo(readerId);
//                comfirmReaderInfo(readerId);
                break;
        }
    }



    /**
     * 显示进度
     */
    private void showAlertDialog(){
        if(alertDialog == null)
            alertDialog = new HintDialog(this,"","");

        alertDialog.showDialog(R.layout.dialog_login_progress_layout, new HintDialog.IHintDialog() {
            @Override
            public void showWindowDetail(Window window, String title, String message) {
                TextView tvTitle = (TextView) window.findViewById(R.id.title_textview);
                tvTitle.setText("操作中");
            }
        });

    }



    @Override
    public void onBackPressed() {
        finish();
    }



}
