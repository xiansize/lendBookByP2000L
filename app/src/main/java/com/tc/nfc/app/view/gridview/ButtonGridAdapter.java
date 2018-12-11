package com.tc.nfc.app.view.gridview;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tc.nfc.R;
import com.tc.nfc.util.Constant;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description:gridview的Adapter
 *
 */
public class ButtonGridAdapter extends BaseAdapter {
	private Context mContext;
	public List<Integer> functionImgList =new ArrayList<Integer>();
	public List<String> functionTextList =new ArrayList<String>();


	public ButtonGridAdapter(Context mContext, List<String> functionTextList,List<Integer> functionImgList) {
		super();
		this.mContext = mContext;
		this.functionImgList =functionImgList;
		this.functionTextList =functionTextList;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return functionTextList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.grid_item, parent, false);

			convertView.setMinimumHeight(Constant.displayWidth / 3);
		}
		TextView tv = BaseViewHolder.get(convertView, R.id.tv_item);
		ImageView iv = BaseViewHolder.get(convertView, R.id.iv_item);
		if (functionTextList.get(position).equals("")){
			iv.setBackgroundResource(0);
		}else{
			iv.setBackgroundResource(functionImgList.get(position));
		}


		tv.setText(functionTextList.get(position));
		return convertView;
	}



}
