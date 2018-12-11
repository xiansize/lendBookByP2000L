package com.tc.nfc.app.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.tc.nfc.R;
import com.tc.nfc.model.LibPurchase;

import java.util.List;
import java.util.Map;

/**
 * Created by tangjiarao on 16/12/19.
 */
public class LibPurchaseAdapter extends HolderAdapter<LibPurchase, LibPurchaseAdapter.ViewHolder> {
    private List<LibPurchase> listData;

    public LibPurchaseAdapter(Context context, List<LibPurchase> listData) {
        super(context, listData);
        this.listData =listData;
    }

    @Override
    public View buildConvertView(LayoutInflater layoutInflater,LibPurchase b, int position) {
        return inflate(R.layout.libpurchaseitem_list);
    }

    @Override
    public ViewHolder buildHolder(View convertView,LibPurchase b, int position) {

        ViewHolder holder = new ViewHolder();

        holder.libName = (TextView)convertView.findViewById(R.id.libName);
        holder.quantity = (TextView)convertView.findViewById(R.id.quantity);


        return holder;
    }

    @Override
    public void bindViewDatas(ViewHolder holder,final LibPurchase b, final int position) {

        if (position ==0){
            holder.libName.setTextSize(18);
            holder.quantity.setTextSize(18);
            holder.libName.setTextColor(Color.BLACK);
            holder.quantity.setTextColor(Color.BLACK);
        }else{
            holder.libName.setTextSize(17);
            holder.quantity.setTextSize(17);
            holder.libName.setTextColor(Color.parseColor("#717070"));
            holder.quantity.setTextColor(Color.parseColor("#717070"));
        }

        holder.libName.setText(b.getLibName());
        holder.quantity.setText(b.getQuantity());
    }
    public List<LibPurchase> getListData(){
        return listData;
    }

    @Override
    public void setListData(List<LibPurchase> listData) {
        this.listData = listData;
    }

    public static class ViewHolder {

        TextView libName,quantity;
    }
}
