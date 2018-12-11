package com.tc.nfc.app.activity;

import java.util.List;


import com.tc.api.VersionSetting;
import com.tc.nfc.R;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends BaseActivity {
	private static final String TAG = "MainActivity";
	private LayoutInflater layoutInflater;
	private FragmentTabHost tabHost;
	private int currentTab = 0;
	private long mStartTime=0;
	private static final String DECODED_CONTENT_KEY = "codedContent";
	private static final String DECODED_BITMAP_KEY = "codedBitmap";
	private static final int REQUEST_CODE_SCAN = 0x0000;
	private String qrCoded;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		super.updateApp();

		initViews();
		tabHost.setCurrentTab(getIntent().getIntExtra("currentTab", 0));


	}

	@Override
	protected void initContentView(Bundle savedInstanceState) {
		setContentView(R.layout.activity_main);
	}


	public Fragment getVisibleFragment() {
		FragmentManager fragmentManager = MainActivity.this.getSupportFragmentManager();
		List<Fragment> fragments = fragmentManager.getFragments();
		Fragment fragment1 = null;
		for (Fragment fragment : fragments) {
			Log.d("JSON", fragment.getTag());
			if (fragment != null && fragment.isVisible()) {
				break;
			}
		}
		return null;
	}

	/**
	 * 初始化界面
	 */
	private void initViews() {

		layoutInflater = LayoutInflater.from(this);

		initFragments();

	}

	/**
	 * 初始化fragments
	 */
	private void initFragments() {

		tabHost = (FragmentTabHost) findViewById(R.id.maintab);
		tabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
		int count = VersionSetting.mFragmentArray.length;
		for (int i = 0; i < count; i++) {

			TabSpec tabSpec = tabHost.newTabSpec(VersionSetting.mTextArray[i]).setIndicator(getTabItemView(i));
			tabHost.addTab(tabSpec, VersionSetting.mFragmentArray[i], null);

		}
		tabHost.setCurrentTab(currentTab);
	}

	/**
	 * 初始化tab ICON和数据
	 *
	 * @param index
	 * @return
	 */
	private View getTabItemView(int index) {
		View view = layoutInflater.inflate(R.layout.footer_tab, null);
		ImageView imageView = (ImageView) view.findViewById(R.id.imageview);
		TextView textView = (TextView) view.findViewById(R.id.textview);
		imageView.setImageResource(VersionSetting.mImageArray[index]);
		textView.setText(VersionSetting.mTextArray[index]);

		return view;
	}

	/**
	 * 重写返回按钮，点击返回按钮时，回到主页面，但activity不销毁。
	 * @param keyCode
	 * @param event
	 * @return
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
//			Intent intent = new Intent(Intent.ACTION_MAIN);
//			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			intent.addCategory(Intent.CATEGORY_HOME);
//
//			startActivity(intent);

			//当点击的时候计算2次点击的时间差，如果在2秒之内点击2次，就退出
			long currentTimeMillis = System.currentTimeMillis();
			if ((currentTimeMillis - mStartTime) < 2000) {
				super.mNFCApplication.clearAllActivity();
				android.os.Process.killProcess(android.os.Process.myPid());
			} else {
				Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
				mStartTime = System.currentTimeMillis();    //记录点击后的时间
			}
			return true;
		} else
			return super.onKeyDown(keyCode, event);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// 扫描二维码/条码回传
		if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
			if (data != null) {

				qrCoded = data.getStringExtra(DECODED_CONTENT_KEY);
				Intent intent = new Intent(MainActivity.this, WebPage.class);
				intent.putExtra("startUrl","file:///android_asset/widget/html/winc.html");
				startActivity(intent);
				overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
			}
		}
	}


}