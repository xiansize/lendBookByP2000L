package com.tc.nfc.app.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.holder.CBViewHolderCreator;
import com.bigkoo.convenientbanner.holder.Holder;
import com.tc.api.VersionSetting;
import com.tc.nfc.R;
import com.tc.nfc.app.activity.CheckReaderActivity;
import com.tc.nfc.app.activity.ConsultActivity;
import com.tc.nfc.app.activity.InventoryActivity;
import com.tc.nfc.app.activity.LoanActivity;
import com.tc.nfc.app.activity.PurchaseActivity;
import com.tc.nfc.app.activity.ReLoanActivity;
import com.tc.nfc.app.activity.ReturnActivity;
import com.tc.nfc.app.view.gridview.ButtonGridAdapter;
import com.tc.nfc.app.view.gridview.ButtonGridView;
import com.tc.nfc.util.Constant;

import java.util.ArrayList;
import java.util.List;

import tplusr.scanlibrary.zxing.android.CaptureActivity;


public class IndexFragment extends Fragment implements OnClickListener{

	private final String TAG = "IndexFragment";

	//上架标识
	public static final String SHELVE_FLAG = "0";
	//扫描失败
	public static final String CHECK_FLAG = "1";
	//结束扫描动作
	public static final String SHUNJIA_FLAG = "2";

	/**
	 * 基础功能List
	 */
	public List<Integer> functionImgList1 =new ArrayList<Integer>();
	public List<String> functionTextList1 =new ArrayList<String>();
	/**
	 * 需求功能List
	 */
	public List<Integer> functionImgList2 =new ArrayList<Integer>();
	public List<String> functionTextList2 =new ArrayList<String>();


	//缓存view
	private View view;

	private TextView title;
	//操作员模式下显示的读者menu
	private RelativeLayout l1DisplayReader;//读者信息展示的布局
	private TextView tvReaderId, tvReaderName, tvFunctionText,text1,text2;//读者信息名字和id，展示的布局的title，不同功能列表的title
	private ButtonGridView gridview1,gridview2;//不同功能的两个列表
	private Button logoutBt;//注销按钮

	//提交评论功能
	private LinearLayout scan;
	private static final int REQUEST_CODE_SCAN = 0x0000;

    //图片轮播功能
	private List<Integer> imageList;
    private ConvenientBanner<Integer> convenientBanner;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


		if(view==null){
			view = inflater.inflate(R.layout.fragment_index, container, false);
			initView();
			setListener();
		}
		return view;
	}

	@Override
	public void onStart() {
		super.onStart();

	}

	/**
	 * 初始化
	 */
	public void initView() {

		title = (TextView) view.findViewById(R.id.tv_title_activity);
		title.setText("图书管理");

		if (VersionSetting.IS_OPEN_LOAN){
			functionImgList1.add(R.drawable.lendbook_icon);
			functionTextList1.add("借书");
		}
		if (VersionSetting.IS_OPEN_RETURN){
			functionImgList1.add(R.drawable.returnbook_icon);
			functionTextList1.add("还书");
		}
		if (VersionSetting.IS_OPEN_RENEW){
			functionImgList1.add(R.drawable.relendbook_icon);
			functionTextList1.add("续借");
		}
		if (VersionSetting.IS_OPEN_LOAD){
			functionImgList1.add( R.drawable.ownbook_icon);
			functionTextList1.add("查询借阅");
		}

		if (VersionSetting.IS_OPEN_SHELF){
			functionImgList2.add(R.drawable.bookshelf_btn);
			functionTextList2.add("书本上架");
		}
		if (VersionSetting.IS_OPEN_CHECK){
			functionImgList2.add(R.drawable.checkbook_btn);
			functionTextList2.add("书本清点");
		}
		if (VersionSetting.IS_OPEN_SHUNJIA){
			functionImgList2.add(R.drawable.shunjia_btn);
			functionTextList2.add("书本顺架");
		}
		if (VersionSetting.IS_OPEN_YUELAN){
			functionImgList2.add(R.drawable.yuelan_btn);
			functionTextList2.add("书本阅览");
		}
		if (VersionSetting.IS_OPEN_CAIGOU){
			functionImgList2.add(R.drawable.yuelan_btn);
			functionTextList2.add("书本采购");
		}

		initFunction(functionImgList1, functionTextList1);
		initFunction(functionImgList2, functionTextList2);

		if (functionImgList1.size()!=0){
			gridview1 =(ButtonGridView) view.findViewById(R.id.gridview1);
			text1=(TextView)view.findViewById(R.id.text1);
			gridview1.setVisibility(View.VISIBLE);
			text1.setVisibility(View.VISIBLE);
			gridview1.setAdapter(new ButtonGridAdapter(this.getActivity(), functionTextList1, functionImgList1));
		}
		if (functionImgList2.size()!=0){
			gridview2 =(ButtonGridView) view.findViewById(R.id.gridview2);
			text2=(TextView)view.findViewById(R.id.text2);
			gridview2.setVisibility(View.VISIBLE);
			text2.setVisibility(View.VISIBLE);
			gridview2.setAdapter(new ButtonGridAdapter(this.getActivity(), functionTextList2, functionImgList2));
		}

		logoutBt=(Button)view.findViewById(R.id.logout);//注销按钮
		l1DisplayReader =(RelativeLayout)view.findViewById(R.id.l1);//读者展示信息的布局
		tvFunctionText =(TextView)view.findViewById(R.id.tvFunction);//读者信息框原始文字展示：功能列表
		tvReaderId =(TextView)view.findViewById(R.id.readerIdTv);
		tvReaderName =(TextView)view.findViewById(R.id.readerNameTv);
//		scan = (LinearLayout) view.findViewById(R.id.scan);
//		scan.setVisibility(View.VISIBLE);


        //图片的轮播
        if(VersionSetting.SLIDE_PICTURE && Constant.readerName == null && Constant.readerId == null){
            showConvenientBanner();
        }
	}



	/**
	 * 初始化功能列表
	 */
	private void initFunction(List<Integer> imgList,List<String> textList){
		if (imgList.size()>0&&imgList.size()<3){
			for (int i=imgList.size();i<3;i++){
				imgList.add(0);
				textList.add("");
			}

		}
		else if (imgList.size()>3){
			for (int i=imgList.size();i<6;i++){
				imgList.add(0);
				textList.add("");
			}

		}
	}
	/**
	 * 设置监听
	 */
	public void setListener() {

		logoutBt.setOnClickListener(this);
//		scan.setOnClickListener(this);

		if (gridview1!=null){
			gridview1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					String action = functionTextList1.get(position);

					switch (action) {
						case "借书":
							if (isExistReader(Constant.LOAN_ACTION)||!VersionSetting.IS_ADMIN)
								startActivity(LoanActivity.class);
							break;
						case  "还书":
							if(VersionSetting.IS_CONNECT_INTERLIB3) {
								if (isExistReader(Constant.RETURN_ACTION) || !VersionSetting.IS_ADMIN)
									startActivity(ReturnActivity.class);
							}else{
								startActivity(ReturnActivity.class);
							}
							break;

						case "续借":
							if (isExistReader(Constant.RELOAN_ACTION)||!VersionSetting.IS_ADMIN)
								startActivity(ReLoanActivity.class);
							break;
						case "查询借阅":
							if (isExistReader(Constant.OWN_ACTION)) {
								Intent intent = new Intent();
								intent.setClass(getActivity(), ReLoanActivity.class);
								intent.putExtra("OWN", 1);
								startActivity(intent);
								getActivity().overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
							}
							break;
					}
				}
			});
		}

		if (gridview2!=null){
			gridview2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					String action = functionTextList2.get(position);
					Intent mIntent;
					switch (action) {
						case "书本上架":
							mIntent = new Intent();
							mIntent.putExtra("Flag",SHELVE_FLAG);
							mIntent.setClass(IndexFragment.this.getActivity(), InventoryActivity.class);
							startActivity(mIntent);
							getActivity().overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);

							break;

						case "书本清点":
							mIntent = new Intent();
							mIntent.putExtra("Flag",CHECK_FLAG);
							mIntent.setClass(IndexFragment.this.getActivity(), InventoryActivity.class);
							startActivity(mIntent);
							getActivity().overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);

							break;

						case "书本顺架":
							mIntent = new Intent();
							mIntent.putExtra("Flag",SHUNJIA_FLAG);
							mIntent.setClass(IndexFragment.this.getActivity(), InventoryActivity.class);
							startActivity(mIntent);
							getActivity().overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
							break;

						case "书本阅览":
							mIntent = new Intent();
							mIntent.setClass(IndexFragment.this.getActivity(), ConsultActivity.class);
							startActivity(mIntent);
							getActivity().overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);

							break;

						case "书本采购":
							mIntent = new Intent();
							mIntent.setClass(IndexFragment.this.getActivity(), PurchaseActivity.class);
							startActivity(mIntent);
							getActivity().overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
							break;
					}
				}
			});
		}

	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {

			case R.id.logout:
				readerLogout();
				break;

			case R.id.ll_activity_title_scan:
				if (ContextCompat.checkSelfPermission(IndexFragment.this.getActivity(), Manifest.permission.CAMERA)
						!= PackageManager.PERMISSION_GRANTED) {

					//申请WRITE_EXTERNAL_STORAGE权限
					ActivityCompat.requestPermissions(IndexFragment.this.getActivity(), new String[]{Manifest.permission.CAMERA}, 0);

				}
				if (ContextCompat.checkSelfPermission(IndexFragment.this.getActivity(), Manifest.permission.CAMERA)
						== PackageManager.PERMISSION_GRANTED) {
					Intent intent = new Intent(this.getActivity(), CaptureActivity.class);
					getActivity().startActivityForResult(intent, REQUEST_CODE_SCAN);
					getActivity().overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);

				}
				break;

		}

	}

	@Override
	public void onResume() {
		super.onResume();
		if (VersionSetting.IS_ADMIN) {
			isShowReaderMenu();
		}

	}

	/**
	 * 跳转页面
	 *
	 * @param activityClass 目标页面Class
	 */
	public void startActivity(Class activityClass) {
		Intent intent = new Intent();
		intent.setClass(getActivity(), activityClass);
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);

	}


	/**
	 * 判断是否显示读者信息菜单
	 */
	private void isShowReaderMenu() {
		if (Constant.readerId != null && Constant.readerName != null) {
			l1DisplayReader.setVisibility(View.VISIBLE);
			tvFunctionText.setVisibility(View.INVISIBLE);

			tvReaderId.setText(Constant.readerId);
			tvReaderName.setText(Constant.readerName);
            Log.d("test","读者信息"+Constant.readerId+Constant.readerName);
        }
	}

    /**
     * 展示轮播图
     */
    private void showConvenientBanner(){
        tvFunctionText.setVisibility(View.INVISIBLE);
        //添加图片在list中
        imageList = new ArrayList();
        imageList.add(R.drawable.lib);
        imageList.add(R.drawable.return_book);
        imageList.add(R.drawable.loadbook);
        imageList.add(R.drawable.renewbook);
        imageList.add(R.drawable.faqsearch);

        convenientBanner = (ConvenientBanner<Integer>) view.findViewById(R.id.convenientBanner);
        convenientBanner.setPages(new CBViewHolderCreator() {
            @Override
            public Object createHolder() {
                return new NetworkImageHolderView();
            }
        },imageList)//设置需要切换的view
                .setPointViewVisible(true)
                .setPageIndicator(new int[]{R.drawable.indicator_default,R.drawable.indicator_select})
                .startTurning(3000)//设置自动切换（同时设置了切换时间间隔）
                .setPageIndicatorAlign(ConvenientBanner.PageIndicatorAlign.ALIGN_PARENT_RIGHT); //设置指示器位置（左、中、右）
    }


    /**
	 * logout 按钮操作
	 */
	private void readerLogout() {
		l1DisplayReader.setVisibility(View.INVISIBLE);
		tvFunctionText.setVisibility(View.VISIBLE);
		Constant.readerName = null;
		Constant.readerId = null;
        //显示轮播图
        if(VersionSetting.SLIDE_PICTURE){
            showConvenientBanner();
        }
	}

	/**
	 * 判断是否存在读者证号
	 *
	 * @param readerAction
	 * @return
	 */
	private boolean isExistReader(int readerAction) {
		if (Constant.readerId == null && Constant.readerName == null) {
			Intent intent = new Intent();
			intent.putExtra("readerAction", readerAction);
			intent.setClass(getActivity(), CheckReaderActivity.class);
			startActivity(intent);
			getActivity().overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
			return false;
		}
		return true;
	}

    //轮播图的自定义viewholder
    private class NetworkImageHolderView implements Holder<Integer>{

        private ImageView imageView;

        @Override
        public View createView(Context context) {
            //你可以通过layout文件来创建，也可以像我一样用代码创建，不一定是Image，任何控件都可以进行翻页
            imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            return imageView;
        }

        @Override
        public void UpdateUI(Context context, int position, Integer data) {
            imageView.setBackgroundResource(data);
        }

    }

}
