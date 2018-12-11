package com.tc.nfc.app.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.iflytek.sunflower.FlowerCollector;
import com.tc.api.VersionSetting;
import com.tc.nfc.R;
import com.tc.nfc.app.adapter.ExampleAdapter;
import com.tc.nfc.app.utils.ToastUtil;
import com.tc.nfc.app.view.HintDialog;
import com.tc.nfc.core.listener.ActionCallbackListener;
import com.tc.nfc.util.NFCApplication;
import com.tc.nfc.app.view.AnimatedExpandableListView;
import com.tc.nfc.model.GroupItem;
import com.tc.nfc.model.ReturnBookResult;
import com.tc.nfc.util.PrinterUtil;
import com.tomergoldst.tooltips.ToolTip;
import com.tomergoldst.tooltips.ToolTipsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ReLoanResultActivity extends BaseActivity implements  OnClickListener {
	private final String TAG ="ReLoanResultActivity";

	public NFCApplication mNFCApplication;
	private Context context;
	private ReLoanResultActivity that;

	private TextView title;
	private LinearLayout backImage;
	private RelativeLayout contentLayout;
	private ExampleAdapter adapter;

	//messageWindow管理
	ToolTipsManager mToolTipsManager;
	ToolTip.Builder builder=null;

	//上一被选择按钮的数据
	private ReturnBookResult lastData =null;

	private AnimatedExpandableListView listView;
	private List<ReturnBookResult> listObj;
	private List<ReturnBookResult> sucResultList;
	private List<ReturnBookResult> falResultList;
	private StringBuffer ISBNList = new StringBuffer();

    //setting
    private LinearLayout llPrinter = null;

    //弹窗提醒
    HintDialog hintDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initView();
	}
	@Override
	protected void initContentView(Bundle savedInstanceState) {
		setContentView(R.layout.activity_reloan_result);
		mNFCApplication =(NFCApplication)this.getApplication();
	}

	public void onResume() {
		super.onResume();
		FlowerCollector.onResume(this);
		Log.d("test","reloanResult");
	}

	public void onPause() {
		super.onPause();
		FlowerCollector.onPause(this);
	}

	public void initView(){
        if(VersionSetting.IS_P2000L_DEVICE && VersionSetting.IS_PRINTER){
            llPrinter = (LinearLayout) findViewById(R.id.ll_activity_title_setting);
            llPrinter.setVisibility(View.VISIBLE);
            llPrinter.setOnClickListener(this);
        }

		mNFCApplication =(NFCApplication)this.getApplication();
		context = this.getApplicationContext();
		title = (TextView) findViewById(R.id.tv_title_activity);
		sucResultList=new ArrayList<>();
		falResultList=new ArrayList<>();
		title.setText(R.string.renewbook);
		backImage = (LinearLayout)findViewById(R.id.ll_activity_title_backpress);
		backImage.setOnClickListener(this);
		contentLayout = (RelativeLayout)findViewById(R.id.content);
		that = this;
		mToolTipsManager = new ToolTipsManager();

		listObj =  (ArrayList<ReturnBookResult>) getIntent().getSerializableExtra("list");
		if (listObj.size()!=0) {
			initData();
		}

		listView = (AnimatedExpandableListView) findViewById(R.id.listview);
		listView.setAdapter(adapter);

		if (adapter.getGroup(0).getItems().size() != 0) {
			listView.expandGroupWithAnimation(0);
		}
		if (adapter.getGroup(1).getItems().size() != 0){
			listView.expandGroupWithAnimation(1);
		}
		getImage();

		setListener();
	}

	public void setListener(){
		/**
		 * 当listView滑动时关闭消息框
		 */
		listView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView absListView, int i) {
				closeMessageWindow();
			}

			@Override
			public void onScroll(AbsListView absListView, int i, int i1, int i2) {

			}
		});
		adapter.setAdapterListener(new ExampleAdapter.CheckOnClickListener() {

			public void onClick(View v, int position, ReturnBookResult data) {
				//改变按钮选中状态
				Boolean isCheck = !data.getBook().getIsCheck();

				closeMessageWindow();
				//判断当前按钮状态，如果为true被点击则新建message框
				if (isCheck) {

//					builder = new ToolTip.Builder(that, v, relativeLayout
//							, data.get("message").toString(), ToolTip.POSITION_ABOVE);
					builder = new ToolTip.Builder(that, v, contentLayout
							, data.getMessage().replace("]]"," "), ToolTip.POSITION_ABOVE);//String去掉中括号
					builder.setGravity(ToolTip.GRAVITY_CENTER);
					builder.setAlign(ToolTip.ALIGN_RIGHT);

					mToolTipsManager.show(builder.build());
					data.getBook().setIsCheck(isCheck);
					lastData = data;
					Log.d("test","tip的数据："+data.getMessage());
				}
			}

			//当adapter被刷新时关闭message框并且改变状态
			public void reFresh() {
				closeMessageWindow();
			}
		});



		listView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

			public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

				if (adapter.getGroup(groupPosition).getItems().size() != 0) {
					if (listView.isGroupExpanded(groupPosition)) {
						listView.collapseGroupWithAnimation(groupPosition);

					} else {
						listView.expandGroupWithAnimation(groupPosition);
						adapter.notifyDataSetChanged();

					}
				}
				return true;
			}

		});
	}

	/**
	 * 初始化数据
	 */
	private void initData() {


		for (int i=0;i<listObj.size();i++){
			listObj.get(i).getBook().setIsCheck(false);
			if (ISBNList.length()==0)
				ISBNList.append(listObj.get(i).getBook().getIsbn());
			else
				ISBNList.append(","+listObj.get(i).getBook().getIsbn());

			Log.d("JSON", listObj.get(i).getMessage() + "BAYA");
			//如果续借成功就加入续借成功list
			if (listObj.get(i).isReturnResult()){
				sucResultList.add(listObj.get(i));

				//续借成功后，换了须还日期
                String message[] = listObj.get(i).getMessage().split("还书日期为");
                String newReturnData = message[1].replaceAll("]]","");
                sucResultList.get(i).getBook().setReturnDate(newReturnData);


			//加入续借失败list
			}else{
				falResultList.add(listObj.get(i));
			}
		}

		List<GroupItem> items = new ArrayList<GroupItem>();

		for(int i = 1; i < 3; i++) {
			GroupItem item = new GroupItem();


			if (i==1){
				if (sucResultList.size()!=0){
					for(int j = 0; j < sucResultList.size(); j++) {
						item.getItems().add(sucResultList.get(j));
					}
//					item.setBigTitle(sucResultList.get(0).getBook().getBookTitle()
//							+" 等 [" + sucResultList.size()+ "] 本图书续借成功");
					item.setBigTitle("  [" + sucResultList.size()+ "] 本图书续借成功");
				}
				else{
					item.setBigTitle("暂无图书续借成功");
				}

			}
			else{
				if (falResultList.size()!=0){
					for(int j = 0; j < falResultList.size(); j++) {

						item.getItems().add(falResultList.get(j));
					}
//					item.setBigTitle(falResultList.get(0).getBook().getBookTitle()
//							+" 等 [" + falResultList.size()+ "] 本图书续借失败");
					item.setBigTitle("  [" + falResultList.size()+ "] 本图书续借失败");
				}
				else {
					item.setBigTitle("暂无图书续借失败");
				}

			}

			items.add(item);
		}
		adapter = new ExampleAdapter(this);
		adapter.setData(items);

	}

	public void getImage(){
		mNFCApplication.getmSearchAction().getImage(ISBNList.toString(), new ActionCallbackListener<Map<String, String>>() {
			@Override
			public void onSuccess(Map<String, String> data) {
				Log.d("XX", data.toString());
				adapter.setImageUrl(data);
				adapter.notifyDataSetChanged();
			}

			@Override
			public void onFailure(String errorEvent, String message) {

			}
		});
	}
	/**
	 * 关闭MessageWindow
	 */
	public void closeMessageWindow(){
		//如果该页面上有message框则关闭，并改变打开message框的data的按钮状态为false
		if (builder != null) {
			mToolTipsManager.findAndDismiss(builder.build().getAnchorView());
			//改变打开message框的data的按钮状态
			lastData.getBook().setIsCheck(false);
			builder = null;
		}
	}

	public void onClick(View v) {

		switch (v.getId()) {

			case R.id.ll_activity_title_backpress:
//				startActivity(ReLoanActivity.class);
				this.finish();
				overridePendingTransition(R.anim.slide_left_ins, R.anim.slide_right_outs);
				break;
            case R.id.ll_activity_title_setting:
                //打印操作

                if(sucResultList.size()>0){
                    showHintDialog("提示","成功续借"+"["+sucResultList.size()+"]"+"本书，是否打印凭条？");

                }else{
                    ToastUtil.showToastShortTime(this,"没有书籍续借成功");
                }
                break;
		}

	}


    private void showHintDialog(String title, final String tipMessage){
        if(hintDialog == null){
            hintDialog = new HintDialog(this,title,tipMessage);
        }
        hintDialog.showDialog(R.layout.dialog_normals_layout, new HintDialog.IHintDialog() {
            @Override
            public void showWindowDetail(Window window, String title, String message) {
                final TextView tvMessage = (TextView) window.findViewById(R.id.message);
                tvMessage.setText(tipMessage);

                final TextView tvSure = (TextView) window.findViewById(R.id.positiveButton);
                final TextView tvCannel = (TextView) window.findViewById(R.id.negativeButton);

                tvCannel.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        hintDialog.dismissDialog();
                    }
                });

                tvSure.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PrinterUtil.printer(null,sucResultList,TAG);
                        hintDialog.dismissDialog();

                    }
                });
            }
        });
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

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			startActivity(ReLoanActivity.class);
			this.finish();
			return true;
		} else
			return super.onKeyDown(keyCode, event);
	}

}
