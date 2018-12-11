package com.tc.nfc.app.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tc.api.VersionSetting;
import com.tc.nfc.R;
import com.tc.nfc.app.adapter.ReloadAdapter;
import com.tc.nfc.app.utils.ToastUtil;
import com.tc.nfc.util.Constant;
import com.tc.nfc.util.NFCApplication;
import com.tc.nfc.app.view.LoadingView;
import com.tc.nfc.app.view.SmoothCheckBox;
import com.tc.nfc.core.listener.ActionCallbackListener;
import com.tc.nfc.model.Book;
import com.tc.nfc.model.ReturnBookResult;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ReLoanActivity extends BaseActivity implements  OnClickListener {
	private final String TAG ="ReLoanActivity";

	public NFCApplication mNFCApplication;


	private ReLoanActivity that;
	//续借列表
	private ListView reloadListView;
	private ReloadAdapter reloadAdapter;
	//加载view
	private ImageView animationIV;
	//没有数据时显示的text 标题 显示已选数量text
	private TextView nodata,title, checkedNumText;
	//全选textView
	private TextView all_check_btn;
	//续借btn
	private Button reloan_btn;
	//全选btn
	private SmoothCheckBox allselect_cb;
	//返回
	private LinearLayout backImage;
	//加载loadingView的层
	private RelativeLayout contentLayout;
	private LoadingView loadingView;

	//续借按钮组
	private RelativeLayout bt_group;

	//是否显示选择按钮，进入续借页面时会显示，进入已借页面的
	private int isShowBookCheck;

	//判断是否全选
	private boolean IS_ALL_CHECK=false;

	//已经选中的数量
	private int checknum =0;
	//判断当前是点击全选按钮取消全选text还是点击某item取消全选text
	private boolean isReduce=false;

	private StringBuffer ISBNList = new StringBuffer();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initView();

	}
	@Override
	protected void initContentView(Bundle savedInstanceState) {
		setContentView(R.layout.activity_reloan);
		mNFCApplication =(NFCApplication)this.getApplication();
	}

	public void onResume() {
		super.onResume();

	}

	public void onPause() {
		super.onPause();
	}

	public void initView(){

		mNFCApplication =(NFCApplication)this.getApplication();
		title = (TextView) findViewById(R.id.tv_title_activity);
		reloadListView = (ListView) findViewById(R.id.reloanList);
		animationIV = (ImageView) findViewById(R.id.animationIV);
		animationIV.setBackgroundResource(R.drawable.load_animation_1);
		nodata = (TextView) findViewById(R.id.nodata);
		checkedNumText =(TextView)findViewById(R.id.checkedNum);
		checkedNumText.setText("已选 [" + 0 + "]");
		AnimationDrawable AniDraw = (AnimationDrawable) animationIV.getBackground();
		AniDraw.start();
		that = this;
		bt_group =(RelativeLayout)findViewById(R.id.button_group);
		isShowBookCheck=getIntent().getIntExtra("OWN", 0);
		if (isShowBookCheck == 1) {
			bt_group.setVisibility(View.GONE);
			title.setText(R.string.ownbook);
		}else {

			title.setText(R.string.renewbook);
			loadingView = new LoadingView(this, "正在续借...");
		}
		reloan_btn =(Button)findViewById(R.id.reloadBtn);
		all_check_btn=(TextView)findViewById(R.id.allCheckBtn);
		allselect_cb=(SmoothCheckBox)findViewById(R.id.allselect_cb);
		allselect_cb.isWorkOnClickListener(false);
		backImage = (LinearLayout)findViewById(R.id.ll_activity_title_backpress);
		contentLayout=(RelativeLayout)findViewById(R.id.content);

		backImage.setOnClickListener(this);


		loanBookData(Constant.readerId);
	}

	/**
	 * 获取已借书目数据
	 */
	public void loanBookData(String readerId){


		mNFCApplication.getBookAction().loanBookData(readerId, new ActionCallbackListener<List<Book>>() {

			@Override
			public void onSuccess(final List<Book> bookList) {

				for (Book book:bookList){

                    if (ISBNList.length()==0)
						ISBNList.append(book.getIsbn());
					else
						ISBNList.append(","+book.getIsbn());
				}
				getImage();
				animationIV.setVisibility(View.GONE);
				disappearShade();

				if (bookList.size() == 0) {

					nodata.setText("没有数据");
					reloadListView.setVisibility(View.GONE);
				}

                Log.d("test","书本数量:"+bookList.size());

				if (isShowBookCheck==1) {
                    reloadAdapter = new ReloadAdapter(that, bookList, false);
                }else {
                    reloadAdapter = new ReloadAdapter(that, bookList, true);
                }

				reloadListView.setAdapter(reloadAdapter);

                //查询借阅的界面进行条码监听
                if(isShowBookCheck != 1) {

                    reloadListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            Book book = (Book) ((ListView) parent).getAdapter().getItem(position);
                            book.setIsCheck(!book.getIsCheck());
                            SmoothCheckBox checkBox = (SmoothCheckBox) view.findViewById(R.id.checkbox);
                            checkBox.setChecked(book.getIsCheck(), true);
                            if (book.getIsCheck()) {
                                checkedNumText.setText("已选 [" + (++checknum) + "]");
                            } else {
                                checkedNumText.setText("已选 [" + (--checknum) + "]");
                            }
                            if (allselect_cb.isChecked()) {
                                isReduce = true;
                                allselect_cb.setChecked(false);
                            }

                        }
                    });


                    reloadAdapter.setChecknumlistener(new ReloadAdapter.ChangeCheckedNumListener() {
                        @Override
                        public void change(boolean isAdd) {
                            if (isAdd) {
                                checkedNumText.setText("已选 [" + (++checknum) + "]");
                            } else {
                                checkedNumText.setText("已选 [" + (--checknum) + "]");
                            }
                            if (allselect_cb.isChecked()) {
                                isReduce = true;
                                allselect_cb.setChecked(false);

                            }
                        }
                    });

                }

				reloan_btn.setEnabled(true);
				reloan_btn.setOnClickListener(that);
				all_check_btn.setOnClickListener(that);
				allselect_cb.isWorkOnClickListener(true);
				reloan_btn.setTextColor(Color.parseColor("#ffffff"));
				all_check_btn.setTextColor(Color.parseColor("#605f5f"));
				checkedNumText.setTextColor(Color.parseColor("#605f5f"));
				allselect_cb.setOnCheckedChangeListener(new SmoothCheckBox.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(SmoothCheckBox checkBox, boolean isChecked) {

						IS_ALL_CHECK = isChecked;
						if (IS_ALL_CHECK) {

							checknum =bookList.size();
							all_check_btn.setText("取消全选");

						} else {
							if (!isReduce){
								checknum =0;
							}
							all_check_btn.setText("全选");
						}
						checkedNumText.setText("已选 [" + checknum + "]");
						if (!isReduce){
							List<Book> bookList = reloadAdapter.getListData();

							for (int i = 0; i < bookList.size(); i++) {
								bookList.get(i).setIsCheck(IS_ALL_CHECK);
							}
							reloadAdapter.setListData(bookList);
							reloadAdapter.notifyDataSetChanged();
						}
						isReduce=false;

					}
				});

			}

			public void onFailure(String errorEvent, String message) {
                ToastUtil.showToastShortTime(ReLoanActivity.this,message);
				animationIV.setVisibility(View.GONE);
				disappearShade();
			}

		});
	}
	public void reloanBookMany(String readerId){
		appearShade();
		mNFCApplication.getBookAction().reloanBookMany(readerId, getReloanList(), new ActionCallbackListener<List<ReturnBookResult>>() {

			@Override
			public void onSuccess(List<ReturnBookResult> data) {

                //提交续借日志
				if(VersionSetting.IS_UPLOAD_LOG) {
					uploadReLoanBookLog(data, Constant.LOG_USER,Constant.LOG_PASSWORD,Constant.userName, Constant.readerId, Constant.readerName, "3");
				}

                disappearShade();
				Intent intent = new Intent();
				intent.putExtra("list", (Serializable) data);
				intent.setClass(that, ReLoanResultActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);

			}

			@Override
			public void onFailure(String errorEvent, String message) {
				disappearShade();
                //账号验证过去，重新登陆
                if(message.contains("账号验证过期")){
                    finish();
                    login();
                }
			}
		});
	}
	public void getImage(){
		mNFCApplication.getmSearchAction().getImage(ISBNList.toString(), new ActionCallbackListener<Map<String, String>>() {
			@Override
			public void onSuccess(Map<String, String> data) {

				reloadAdapter.setImageUrl(data);
				reloadAdapter.notifyDataSetChanged();
			}

			@Override
			public void onFailure(String errorEvent, String message) {

			}
		});
	}
	
	public void onClick(View v) {

		switch (v.getId()) {
			case R.id.reloadBtn:
				if (getReloanList().size()!=0)
					reloanBookMany(Constant.readerId);
				break;
			case R.id.ll_activity_title_backpress:
				startActivity(MainActivity.class);
				finish();
				overridePendingTransition(R.anim.slide_left_ins, R.anim.slide_right_outs);
				break;

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
		overridePendingTransition(R.anim.slide_left_ins, R.anim.slide_right_outs);

	}

	/**
	 * 打开覆盖层加载loading
	 */
	public void appearShade(){

		reloan_btn.setEnabled(false);
		all_check_btn.setEnabled(false);
		allselect_cb.isWorkOnClickListener(false);
		contentLayout.addView(loadingView);
		loadingView.startAppearAnimation();
	}

	/**
	 * 取消loading，关闭覆盖层
	 */
	public void disappearShade(){

		if (loadingView!=null){
			loadingView.startDisappearAnimation();
			loadingView.getDisappearAnimation().setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					contentLayout.removeView(loadingView);
					reloan_btn.setEnabled(true);
					all_check_btn.setEnabled(true);
					allselect_cb.isWorkOnClickListener(true);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}
			});
		}

	}

	public List<Book> getReloanList(){
		List<Book> bookList= reloadAdapter.getListData();
		List<Book> newBookList= new ArrayList<>();
		for (int i=0;i<bookList.size();i++){
			if (bookList.get(i).getIsCheck()){
				newBookList.add(bookList.get(i));
			}
		}
		return newBookList;
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			startActivity(MainActivity.class);
			finish();
			return true;
		} else
			return super.onKeyDown(keyCode, event);
	}

}
