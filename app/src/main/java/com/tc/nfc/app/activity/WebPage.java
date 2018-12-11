package com.tc.nfc.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.tc.nfc.R;
import com.tc.nfc.util.AndroidBug5497Workaround;
import com.tc.nfc.util.NFCApplication;
import com.uzmap.pkg.openapi.ExternalActivity;
import com.uzmap.pkg.openapi.Html5EventListener;
import com.uzmap.pkg.openapi.WebViewProvider;

/**
 * Created by tangjiarao on 16/9/23.
 */
public class WebPage extends ExternalActivity {
	private NFCApplication mNFCApplication;
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
		//顶部浸入式
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		AndroidBug5497Workaround.assistActivity(this);
		mNFCApplication =(NFCApplication)this.getApplication();
		mNFCApplication.addActivity(this);

		bindSomething();

    }
    private void bindSomething(){
		//设置一个监听，监听来自Html5页面发送出来的事件
		//此处我们监听"abc"事件，监听Html5页面通过api.sendEvent发出"abc"事件时
		addHtml5EventListener(new Html5EventListener("submitData") {
			@Override
			public void onReceive(WebViewProvider provider, Object obj) {

				Intent intent = new Intent();
				intent.putExtra("startUrl", "file:///android_asset/widget/html/winb.html");
				intent.setClass(WebPage.this, WebPage3.class);
				intent.putExtra("Data", obj.toString());

				startActivity(intent);
				overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);


			}
		});
		addHtml5EventListener(new Html5EventListener("backButton") {
			@Override
			public void onReceive(WebViewProvider provider, Object extra) {

				finish();
				overridePendingTransition(R.anim.slide_left_ins, R.anim.slide_right_outs);
			}
		});
	}
	protected void onDestroy(){
		super.onDestroy();
		Log.d("BASE", "DE");
		mNFCApplication.removeActivity(this);
	}
}
