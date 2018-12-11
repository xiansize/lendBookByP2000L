package com.tc.nfc.app.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.iflytek.speech.setting.IatSettings;
import com.iflytek.speech.util.JsonParser;
import com.iflytek.sunflower.FlowerCollector;
import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;
import com.tc.api.VersionSetting;
import com.tc.nfc.R;
import com.tc.nfc.app.activity.BaseActivity;
import com.tc.nfc.app.activity.LoginActivity;
import com.tc.nfc.app.adapter.BookSearchAdapter;
import com.tc.nfc.app.adapter.HoldingAdapter;
import com.tc.nfc.app.utils.BitmapCache;
import com.tc.nfc.app.utils.ToastUtil;
import com.tc.nfc.app.view.HintDialog;
import com.tc.nfc.app.view.LoadingView;
import com.tc.nfc.app.view.pullRefresh.XListView;
import com.tc.nfc.core.listener.ActionCallbackListener;
import com.tc.nfc.model.BookHolding;
import com.tc.nfc.model.SearchBookResult;
import com.tc.nfc.util.NFCApplication;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SearchFragment extends Fragment implements View.OnClickListener,XListView.IXListViewListener{

	private final String TAG ="SearchFragment";
	public NFCApplication mNFCApplication;

	//状态参数，用来区分当前执行步骤
	private final int PREPARE_STATE =0;
	private final int LOADING_STATE =1;
	private final int COMPLETE_STATE =2;

	/**
	 * 语音
	 */
	// 语音听写对象
	private SpeechRecognizer mIat;
	// 语音听写UI
	private RecognizerDialog mIatDialog;
	// 用HashMap存储听写结果
	private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
	private SharedPreferences mSharedPreferences;
	// 引擎类型
	private String mEngineType = SpeechConstant.TYPE_CLOUD;


	private View view;
	private RelativeLayout contentLayout;
	private LinearLayout backImage;
	private TextView titleTV;

	//搜索框对象
	private SearchBox search;
	//listview
	private XListView mListView;

	//loadingview
	private LoadingView loadingView =null;
	//消息框
	private HintDialog mDialog = null;
	//馆藏信息listview
	private ListView holdingLv;


	//搜索框text
	private String searchText="";
	//初始页面
	private int page =1;
	//图书列表
	private List<SearchBookResult> listData;
	private BookSearchAdapter bookSearchAdapter;
	//馆藏列表
	private List<BookHolding> bookHoldingList;
	private HoldingAdapter holdingAdapter;
	//图片加载
	private ImageLoader mImageLoader;
	private ImageLoader.ImageListener listener;

	//标志是否正在加载数据，如果正在加载，则不能再加载
	private Boolean IS_DO_ACTIO =false;
	//是否加载更多
	private Boolean IS_LOAD_MORE =false;
	//储存当前书页书目的isbn，以便显示对应图片
	private StringBuffer isbnSb = new StringBuffer();


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


		if (view==null){
			view = inflater.inflate(R.layout.fragment_booksearch, container, false);


			// 初始化识别无UI识别对象
			// 使用SpeechRecognizer对象，可根据回调消息自定义界面；
			mIat = SpeechRecognizer.createRecognizer(this.getActivity(), mInitListener);

			// 初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
			// 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
			mIatDialog = new RecognizerDialog(this.getActivity(), mInitListener);

			mSharedPreferences = getActivity().getSharedPreferences(IatSettings.PREFER_NAME,
					Activity.MODE_PRIVATE);

			mNFCApplication =(NFCApplication)this.getActivity().getApplication();


			initView();
			setListener();

		}

		return view;
	}

	public void initView(){


		titleTV =(TextView)view.findViewById(R.id.tv_title_activity);
		titleTV.setText("查书易");
		//获取布局view
		contentLayout=(RelativeLayout)view.findViewById(R.id.content);
		backImage = (LinearLayout)view.findViewById(R.id.ll_activity_title_backpress);
		backImage.setVisibility(View.INVISIBLE);
		/**
		 * 初始化搜索框
		 */
		search = (SearchBox) view.findViewById(R.id.searchbox);
		search.enableVoiceRecognition(this);

		//取消动画
		search.setAnimateDrawerLogo(false);
		//设置语音监听
		search.setMicOnclickListener(this);
		//设置搜索按钮监听
		search.setSearchOnClickListener(this);
		/**
		 * 初始化ListView
		 */
		mListView = (XListView) view.findViewById(R.id.list_view);
		//关闭下拉刷新
		mListView.setPullRefreshEnable(false);
		//打开上拉加载
		mListView.setPullLoadEnable(true);
		mListView.setAutoLoadEnable(true);

		//图书list
		listData=new ArrayList<SearchBookResult>();

		//加载图片线程
		RequestQueue mQueue = Volley.newRequestQueue(this.getActivity());
		mImageLoader = new ImageLoader(mQueue, new BitmapCache());

		//馆藏list
		bookHoldingList =new ArrayList<>();
		resetBookHoldingList();
		holdingAdapter =new HoldingAdapter(this.getActivity(),bookHoldingList);
	}

	/**
	 * 重置馆藏信息
	 */
	public void resetBookHoldingList(){
		bookHoldingList.clear();
		BookHolding bookHolding = new BookHolding();
		bookHolding.setRecno("正在查找..");
		bookHoldingList.add(bookHolding);
	}

	@Override
	public void onPause() {
		super.onPause();
	}
	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1234 && resultCode == getActivity().RESULT_OK) {
			ArrayList<String> matches = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			search.populateEditText(matches.get(0));
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 参数设置
	 *
	 * @return
	 */
	public void setParam() {
		// 清空参数
		mIat.setParameter(SpeechConstant.PARAMS, null);

		// 设置听写引擎
		mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
		// 设置返回结果格式
		mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

		String lag = mSharedPreferences.getString("iat_language_preference",
				"mandarin");
		if (lag.equals("en_us")) {
			// 设置语言
			mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
		} else {
			// 设置语言
			mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
			// 设置语言区域
			mIat.setParameter(SpeechConstant.ACCENT, lag);
		}

		// 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
		mIat.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "4000"));

		// 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
		mIat.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "1000"));

		// 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
		mIat.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "0"));

		// 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
		// 注：AUDIO_FORMAT参数语记需要更新版本才能生效
		mIat.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
		mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/iat.wav");
		Log.d("test", "search:"+Environment.getExternalStorageDirectory()+"/msc/iat.wav");
	}

	/**
	 * 初始化监听器。
	 */
	private InitListener mInitListener = new InitListener() {

		public void onInit(int code) {
			Log.d(TAG, "SpeechRecognizer init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
				showTip("初始化失败，错误码：" + code);
			}
		}
	};

	/**
	 * 听写UI监听器
	 */
	private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
		public void onResult(RecognizerResult results, boolean isLast) {

			if(!isLast) {
				Log.d(TAG,"VOICE_RESULT  "+isLast);
				printResult(results);
			}
		}

		/**
		 * 识别回调错误.
		 */
		public void onError(SpeechError error) {
			showTip(error.getPlainDescription(true));
		}

	};

	/**
	 * 显示toast
	 * @param str
	 */
	private void showTip(final String str) {
		ToastUtil.showToastShortTime(getActivity(),str);
	}

	/**
	 * 听写监听器。
	 */
	private RecognizerListener mRecognizerListener = new RecognizerListener() {

		public void onBeginOfSpeech() {
			// 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
			Log.d(TAG,"START");
			showTip("开始说话");
		}

		public void onError(SpeechError error) {
			// Tips：
			// 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
			// 如果使用本地功能（语记）需要提示用户开启语记的录音权限。
			showTip(error.getPlainDescription(true));
		}

		public void onEndOfSpeech() {
			// 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
			Log.d(TAG,"FINISH");
			showTip("结束说话");
		}

		public void onResult(RecognizerResult results, boolean isLast) {
			Log.d(TAG,"RESULT");
			Log.d(TAG, results.getResultString());
			printResult(results);

			if (isLast) {
				// TODO 最后的结果
			}

		}

		public void onVolumeChanged(int volume, byte[] data) {
			showTip("当前正在说话，音量大小：" + volume);
			Log.d(TAG, "返回音频数据：" + data.length);
		}

		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
			// 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
			// 若使用本地能力，会话id为null
			//	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
			//		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
			//		Log.d(TAG, "session id =" + sid);
			//	}
		}
	};
	/**
	 * 获取语音结果
	 * @param results
	 */
	private void printResult(RecognizerResult results) {
		String text = JsonParser.parseIatResult(results.getResultString());

		String sn = null;
		// 读取json结果中的sn字段
		try {
			JSONObject resultJson = new JSONObject(results.getResultString());
			sn = resultJson.optString("sn");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		mIatResults.put(sn, text);

		StringBuffer resultBuffer = new StringBuffer();
		for (String key : mIatResults.keySet()) {
			resultBuffer.append(mIatResults.get(key));
		}

		Log.d(TAG, resultBuffer.toString());
		search.setSearchText(resultBuffer.toString());
		stateAction(PREPARE_STATE);
	}
	int ret = 0; // 函数调用返回值
	@Override
	public void onClick(View v) {

		switch(v.getId()){

			// 开始听写
			// 如何判断一次听写结束：OnResult isLast=true 或者 onError
			case R.id.overflow:
				Log.d(TAG,"ONCLICK");
				FlowerCollector.onEvent(this.getActivity(), "iat_recognize");

				mIatResults.clear();

				// 设置参数
				setParam();
				boolean isShowDialog = mSharedPreferences.getBoolean(
						getString(R.string.pref_key_iat_show), true);
				if (isShowDialog) {
					// 显示听写对话框
					mIatDialog.setListener(mRecognizerDialogListener);
					mIatDialog.show();
					showTip(getString(R.string.text_begin));
				} else {
					// 不显示听写对话框
					ret = mIat.startListening(mRecognizerListener);
					if (ret != ErrorCode.SUCCESS) {
						showTip("听写失败,错误码：" + ret);
					} else {
						showTip(getString(R.string.text_begin));
					}
				}

				break;

			/**
			 * 搜索按钮操作
			 */
			case R.id.search_btn:
				closeInput();
				stateAction(PREPARE_STATE);

				break;

		}
	}
	/**
	 * listview下拉刷新
	 */
	public void onRefresh() {

	}

	/**
	 * listview加载更多操作
	 */
	public void onLoadMore() {
		page++;
		IS_LOAD_MORE=true;
		stateAction(LOADING_STATE);
	}

	/**
	 * 输入框及软键盘监听操作
	 */
	public void setListener(){

		search.setSearchListener(new SearchBox.SearchListener() {
			@Override
			public void onSearchOpened() {
			}

			@Override
			public void onSearchCleared() {

			}

			@Override
			public void onSearchClosed() {

			}

			@Override
			public void onSearchTermChanged(String term) {
				if (term != null && !term.equals("")) {
					search.setSearchBtnEnable(true);
				} else {
					search.setSearchBtnEnable(false);
				}
			}

			/**
			 * 软键盘搜索键操作
			 * @param result
			 */
			public void onSearch(String result) {
				stateAction(PREPARE_STATE);
			}

			/**
			 * 点击缓存数据操作
			 * @param result
			 */
			public void onResultClick(SearchResult result) {
				stateAction(PREPARE_STATE);
			}
		});


	}

	/**
	 * 不同状态动作
	 * @param currentState 状态参数
	 */
	public void stateAction(int currentState){
		switch(currentState){
			case PREPARE_STATE:

				//获取搜索框内容
				searchText=search.getSearchText();


				//如果搜索框不为空&&不是在执行搜书操作
				if (!searchText.equals("")&&!IS_DO_ACTIO) {
					//书页重置为1
					page=1;
					//不是加载更多操作
					IS_LOAD_MORE=false;
					//打开上拉监听
					mListView.setXListViewListener(this);
					//清空数据并刷新
					listData.clear();
					if (bookSearchAdapter!=null){
						bookSearchAdapter.notifyDataSetChanged();
					}
					stateAction(LOADING_STATE);
				}
				break;
			case LOADING_STATE:
				//1.操作flag为true
				IS_DO_ACTIO = true;
				//2.view显示
				if (!IS_LOAD_MORE){
					appearShade();
				}
				//3.搜索图书，网络操作
				searchBook();
				break;
			case COMPLETE_STATE:
				//1.重新显示书目列表或者刷新书目列表
				//如果适配器为空就新建适配器
				if (bookSearchAdapter == null) {
					bookSearchAdapter = new BookSearchAdapter(this.getActivity(), listData);
					mListView.setAdapter(bookSearchAdapter);
					bookSearchAdapter.setBookSearchAdapterListener(new BookSearchAdapter.BookSearchAdapterListener() {
						@Override
						public void onClick(SearchBookResult t) {

							resetBookHoldingList();
							createDialog(t);
							mNFCApplication.getBookAction().bookHolding(t.getBookrecno(), new ActionCallbackListener<List<BookHolding>>() {
								@Override
								public void onSuccess(List<BookHolding> data) {

									bookHoldingList.clear();
									bookHoldingList.addAll(data);
									holdingAdapter.setListData(bookHoldingList);
									holdingAdapter.notifyDataSetChanged();
								}

								@Override
								public void onFailure(String errorEvent, String message) {
									//账号验证过期，跳到登陆的界面
									if(message.contains("账号验证过期")){
										ToastUtil.showToastShortTime(getActivity(),message);
										getActivity().finish();
										Intent intent = new Intent(getActivity(), LoginActivity.class);
										startActivity(intent);
									}
								}
							});
						}
					});
				}
				//重新绑定数据
				bookSearchAdapter.setListData(listData);
				//更新数据
				bookSearchAdapter.notifyDataSetChanged();

				//2.取消加载动画
				mListView.stopLoadMore();
				disappearShade();

				//3.操作flag为false
				IS_DO_ACTIO=false;
				break;
		}
	}

	/**
	 * 搜书操作
	 */
	public void searchBook(){

		mNFCApplication.getmSearchAction().bookSearch(searchText, String.valueOf(page), new ActionCallbackListener<List<SearchBookResult>>() {
			@Override
			public void onSuccess(List<SearchBookResult> data) {
				listData = data;
				stateAction(COMPLETE_STATE);
				for (SearchBookResult searchBookResult : data) {
					if (isbnSb.length() == 0)
						isbnSb.append(searchBookResult.getIsbn());
					else
						isbnSb.append("," + searchBookResult.getIsbn());
				}
				getImage();
			}

			@Override
			public void onFailure(String errorEvent, String message) {
				stateAction(COMPLETE_STATE);
				if (errorEvent.equals("没有数据")) {

					mListView.setLoadMoreText();
				} else {
					ToastUtil.showToastShortTime(getActivity(),message);
					Log.d("search","search:"+message);
				}

				//账号验证过期，重新登陆
				if(message.contains("账号验证过期")){
					ToastUtil.showToastShortTime(getActivity(),message);
					getActivity().finish();
					Intent intent = new Intent(getActivity(), LoginActivity.class);
					startActivity(intent);
				}

			}
		});

	}

	public void getImage(){
		mNFCApplication.getmSearchAction().getImage(isbnSb.toString(), new ActionCallbackListener<Map<String, String>>() {
			@Override
			public void onSuccess(Map<String, String> data) {

				bookSearchAdapter.setImageUrl(data);
				bookSearchAdapter.notifyDataSetChanged();
			}

			@Override
			public void onFailure(String errorEvent, String message) {

			}
		});
	}

	/**
	 * 查看馆藏弹框
	 * @param searchBookResult
	 */
	private void createDialog(final SearchBookResult searchBookResult){
		if(mDialog==null) {
			mDialog = new HintDialog(this.getActivity(), "LALA", "LALA");
			mDialog.showDialog(R.layout.dialog_holding_layout, new HintDialog.IHintDialog() {

				@Override
				public void showWindowDetail(Window window, String title, final String message) {
					ImageView image = (ImageView)window.findViewById(R.id.image);
					ImageView cancel =(ImageView)window.findViewById(R.id.cancel);
					TextView tv1= (TextView) window.findViewById(R.id.tv_title_activity);
					TextView tv2 = (TextView) window.findViewById(R.id.barcode);
					TextView tv3 = (TextView) window.findViewById(R.id.classno);
					tv1.setText(searchBookResult.getTitle());
					tv2.setText("书目记录号: " + searchBookResult.getBookrecno());
					tv3.setText("分类号: "+searchBookResult.getClassno());

					//将架位号改成条码号
					if(VersionSetting.IS_CONNECT_INTERLIB3){
						TextView tvBarcode = (TextView) window.findViewById(R.id.tv_window_barcode);
						tvBarcode.setText("条码号");
					}

					listener = ImageLoader.getImageListener(image, R.drawable.bookbg,R.drawable.bookbg);
					holdingLv =(ListView)window.findViewById(R.id.holding_lv);
					holdingLv.setAdapter(holdingAdapter);
					try{

						mImageLoader.get(searchBookResult.getImageUrl(), listener);
					}catch (NullPointerException e){
						image.setImageResource(R.drawable.bookbg);
						Log.d(TAG,"NullPointerException: "+"由于map中的某些键值对为空值引发的错误");
					}

					cancel.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mDialog.dismissDialog();
							mDialog = null;
						}
					});

				}

			});

		}
	}




	/**
	 * 打开覆盖层加载loading
	 */
	public void appearShade() {

		if (loadingView == null) {
			loadingView = new LoadingView(this.getActivity(),"正在查询...");
		}
		loadingView.startAppearAnimation();
		try{
			contentLayout.addView(loadingView);
		}catch (Exception e){
			e.printStackTrace();
			contentLayout.removeView(loadingView);
		}

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

				}

				@Override
				public void onAnimationRepeat(Animation animation) {

				}
			});
		}
	}

	/**
	 * 关闭软键盘
	 */
	public void closeInput(){
		View view = this.getActivity().getWindow().peekDecorView();
		if (view != null) {
			InputMethodManager inputmanger = (InputMethodManager) this.getActivity()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputmanger.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}


}
