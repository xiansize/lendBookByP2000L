package com.tc.nfc.app.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.tc.nfc.R;
import com.tc.nfc.model.BookHolding;
import com.tc.nfc.model.LibPurchase;

import java.util.List;

/**
 * Created by tangjiarao on 16/12/19.
 */
public class HoldingAdapter extends HolderAdapter<BookHolding, HoldingAdapter.ViewHolder> {
    private List<BookHolding> listData;
    private Context context;
    public HoldingAdapter(Context context, List<BookHolding> listData) {
        super(context, listData);
        this.listData =listData;
        this.context =context;
    }

    @Override
    public View buildConvertView(LayoutInflater layoutInflater,BookHolding b, int position) {
        return inflate(R.layout.holdinglistitem);
    }

    @Override
    public ViewHolder buildHolder(View convertView,BookHolding b, int position) {

        ViewHolder holder = new ViewHolder();

        holder.tv1 = (TextView)convertView.findViewById(R.id.tv1);
        holder.tv2 = (TextView)convertView.findViewById(R.id.tv2);
        holder.tv3 = (TextView)convertView.findViewById(R.id.tv3);
        holder.tv4 = (TextView)convertView.findViewById(R.id.tv4);

        return holder;
    }

    @Override
    public void bindViewDatas(ViewHolder holder,final BookHolding b, final int position) {

        Log.d("XX",b.getRecno()+" "+b.getState()+" "+b.getLib()+" "+b.getShelfno());
        holder.tv1.setText(b.getRecno());
        holder.tv2.setText(b.getState());
        holder.tv3.setText(b.getLib());
        holder.tv4.setText(b.getShelfno());

    }
    public List<BookHolding> getListData(){
        return listData;
    }

    @Override
    public void setListData(List<BookHolding> listData) {
        this.listData = listData;
    }

    public static class ViewHolder {

        TextView tv1,tv2,tv3,tv4;
    }

}
