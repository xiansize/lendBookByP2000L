package com.tc.nfc.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tc.nfc.R;
import com.tc.nfc.util.Constant;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonSettingActivity extends Activity implements View.OnFocusChangeListener,View.OnClickListener{

    private TextView titleTV;
    private EditText ipet,emailet,portet;
    private Button submit_bt;
    private RelativeLayout content,toolbar;
    private boolean EMAIL_FLG=false,IP_FLG=false,PORT_FLG=false;
    //返回按钮
    private LinearLayout backImage;
    private Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //顶部浸入式
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        setContentView(R.layout.activity_commonsetting);
        intent = getIntent();
        toolbar =(RelativeLayout)findViewById(R.id.ly_main_tab_bottom);
        if (intent.getBooleanExtra("HideToolBar",false)){
            toolbar.setVisibility(View.GONE);
        }
        backImage =(LinearLayout)findViewById(R.id.ll_activity_title_backpress);
        backImage.setOnClickListener(this);
        titleTV =(TextView)findViewById(R.id.tv_title_activity);
        titleTV.setText("配置");
        ipet =(EditText)findViewById(R.id.ipEditText);
        emailet =(EditText)findViewById(R.id.emailEditText);
        emailet.setVisibility(View.GONE);
        portet=(EditText)findViewById(R.id.portEditText);
        submit_bt =(Button)findViewById(R.id.submit_bt);
        content =(RelativeLayout)findViewById(R.id.content);

        SharedPreferences sp = getSharedPreferences("SP", MODE_WORLD_READABLE);
        if (!intent.getBooleanExtra("HideToolBar",false)){
            ipet.setText(sp.getString("IP", "none"));
            portet.setText(sp.getString("PORT", "none"));
            emailet.setText(sp.getString("EMAIL", "none"));
        }
        emailet.setOnFocusChangeListener(this);
        ipet.setOnFocusChangeListener(this);
        portet.setOnFocusChangeListener(this);

        content.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                content.setFocusable(true);
                content.setFocusableInTouchMode(true);
                content.requestFocus();

                return false;
            }
        });

        submit_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ip_validate();
                port_validate();
                email_validate();
                if (EMAIL_FLG==true&&IP_FLG==true&&PORT_FLG==true){


                    SharedPreferences sp = CommonSettingActivity.this.getSharedPreferences("SP", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("IP", ipet.getText().toString().trim());
                    editor.putString("PORT", portet.getText().toString().trim());
                    editor.putString("EMAIL", emailet.getText().toString().trim());
                    Constant.IP=ipet.getText().toString().trim();
                    Constant.PORT=portet.getText().toString().trim();
                    Constant.EMAIL=emailet.getText().toString().trim();
                    editor.commit();

                    if (intent.getBooleanExtra("HideToolBar",false)){
                        Intent intent = new Intent();
                        intent.setClass(CommonSettingActivity.this, LoginActivity.class);
                        startActivity(intent);
                        CommonSettingActivity.this.finish();
                        CommonSettingActivity.this.overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
                    }
                    else{
                        finish();
                        overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
                    }

                }
                else{

                    Toast toast = Toast.makeText(CommonSettingActivity.this, "请填写正确的配置信息..", Toast.LENGTH_SHORT);
                    toast.show();
                }

            }
        });

    }

    public Boolean validateEmail(String str){
        String regex = "^[\\w-]+(\\.[\\w-]+)*\\@([\\.\\w-]+)+$";
        boolean flg = Pattern.matches(regex, str);

        return flg;
    }

    public Boolean validateChinese(String str){

        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);

        return !m.find()&&!str.equals("");
    }

    public Boolean validatePort(String str){
        int port;
        try{
            port =Integer.parseInt(str);
        }catch (Exception e){
            port=0;
        }

        return port>1&&port<65535;
    }

    public void setBackground(EditText editText,boolean flg){
        if (flg)
            editText.setBackgroundResource(R.drawable.setting_et_bg);
        else
            editText.setBackgroundResource(R.drawable.setting_et_bg_error);
    }


    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (v.hasFocus() == false) {
            switch (v.getId()) {
                case R.id.ipEditText:
                    ip_validate();
                    break;
                case R.id.portEditText:
                    port_validate();
                    break;
                case R.id.emailEditText:
                    email_validate();
                    break;
            }
        }
    }

    private void email_validate(){

        if (validateEmail(emailet.getText().toString()))
            EMAIL_FLG =true;

        else
            EMAIL_FLG =false;
        setBackground(emailet,EMAIL_FLG);

    }
    private void ip_validate(){

        if (validateChinese(ipet.getText().toString()))
            IP_FLG =true;

        else
            IP_FLG =false;
        setBackground(ipet,IP_FLG);

    }
    private void port_validate(){

        if (validatePort(portet.getText().toString()))
            PORT_FLG =true;

        else
            PORT_FLG =false;
        setBackground(portet,PORT_FLG);

    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

            return true;
        } else
            return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.ll_activity_title_backpress:
                finish();
                overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
                break;

        }

    }
}
