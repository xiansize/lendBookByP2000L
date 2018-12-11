package com.tc.nfc.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.RelativeLayout;

import com.tc.nfc.R;
import com.tc.nfc.app.view.loadingview.XmlLoadingView;
import com.tc.nfc.util.AndroidBug5497Workaround;
import com.tc.nfc.util.NFCApplication;
import com.uzmap.pkg.openapi.ExternalActivity;
import com.uzmap.pkg.openapi.Html5EventListener;
import com.uzmap.pkg.openapi.WebViewProvider;

import org.json.JSONException;
import org.json.JSONObject;
import com.tc.nfc.util.email.SenderRunnable;


/**
 * Created by tangjiarao on 16/9/23.
 */
public class WebPage3 extends ExternalActivity {

    private Intent intent;
    private XmlLoadingView xmlLoadingView;
    private NFCApplication mNFCApplication;
    private boolean isDoAction=false;
    private RelativeLayout relativeLayout;
    private String phone,email;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //顶部浸入式
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        AndroidBug5497Workaround.assistActivity(this);
        mNFCApplication =(NFCApplication)this.getApplication();
        mNFCApplication.addActivity(this);

        intent = getIntent();
        praseData(intent.getStringExtra("Data"));

        xmlLoadingView =new XmlLoadingView(this);
        xmlLoadingView.setText("正在发送邮件..");
        xmlLoadingView.setViewSize(0, 0);
        xmlLoadingView.setViewPadding(40, 50, 40, 50);
        xmlLoadingView.getDisappearAnimation().setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        bindSomething();
    }
    private void bindSomething() {

        addHtml5EventListener(new Html5EventListener("backButton") {
            @Override
            public void onReceive(WebViewProvider provider, Object extra) {
                finish();
                overridePendingTransition(R.anim.slide_right_outs, R.anim.slide_left_ins);
            }
        });


        addHtml5EventListener(new Html5EventListener("sendEmail") {
            @Override
            public void onReceive(WebViewProvider provider, Object extra) {
                if (isDoAction)
                    return;
                isDoAction=true;
                if (relativeLayout==null){
                    //增加加载view
                    xmlLoadingView.startAppearAnimation();
                    relativeLayout =new RelativeLayout(WebPage3.this);
                    RelativeLayout.LayoutParams mParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
                    RelativeLayout.LayoutParams mParams2 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                    mParams2.addRule(RelativeLayout.CENTER_IN_PARENT);
                    relativeLayout.addView(xmlLoadingView, mParams2);
                    addContentView(relativeLayout,mParams);
                }
                else{
                    xmlLoadingView.setVisibility(View.VISIBLE);
                }


                //发送邮件
                SenderRunnable senderRunnable = new SenderRunnable(
                        "378376210@qq.com", "ymrpjlixjwsfcaci",handler);
                senderRunnable.setMail("Email: "+email+" Phone: "+phone,
                        extra.toString(), "378376210@qq.com", null);
                new Thread(senderRunnable).start();
            }
        });


        addHtml5EventListener(new Html5EventListener("success") {
            @Override
            public void onReceive(WebViewProvider provider, Object extra) {

                mNFCApplication.clearAllActivity();
                Intent intent =new Intent();
                intent.setClass(WebPage3.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_left_ins, R.anim.slide_right_outs);

            }
        });


    }
//    protected void onPageFinished(WebViewProvider provider, String url) {
//
//
//        JSONObject extra = new JSONObject();
//        try{
//            extra.put("value", intent.getStringExtra("phone"));
//        }catch(Exception e){
//            ;
//        }
//        //"fromNative"与assets/widget/html/wind.html页面的apiready中注册的监听相对应
//        sendEventToHtml5("fromNative", extra);
//    }
//    @Override
//    protected boolean onHtml5AccessRequest(WebViewProvider provider, UZModuleContext moduleContext) {
//        String name = moduleContext.optString("name");
//        //"requestEvent"与assets/widget/html/wind.html页面的发送请求相匹配
//        if("requestEvent".equals(name)){
//            JSONObject extra = new JSONObject();
//            Log.d("XX","HAHA");
//            try{
//                extra.put("value", "哈哈哈，我是来自Native的事件");
//            }catch(Exception e){
//                ;
//            }
//            //"fromNative"与assets/widget/html/wind.html页面的apiready中注册的监听相对应
//            sendEventToHtml5("fromNative", extra);
//            return true;
//        }
//        return false;
//    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            JSONObject extra = new JSONObject();
            try{

                switch (msg.what) {
                    case 1:
                        //发送成功
                        extra.put("result", true);
                        sendEventToHtml5("sendEmailResult", extra);

                        xmlLoadingView.setVisibility(View.GONE);
                        isDoAction=false;
                        break;
                    case 2:
                        //发送失败
                        extra.put("result", false);
                        sendEventToHtml5("sendEmailResult", extra);
                        xmlLoadingView.setVisibility(View.GONE);
                        isDoAction=false;
                        break;
                    default:

                        break;
                }
            }catch(Exception e){
                e.printStackTrace();
            }

        }
    };

    private void praseData(String data){
        Log.d("XX", data);
        try {
            JSONObject jsonObject =new JSONObject(data);
            phone = jsonObject.getString("phone");
            email = jsonObject.getString("email");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
