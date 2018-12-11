package com.tc.nfc.app.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.tc.nfc.R;

import java.util.List;
import java.util.Map;

/**
 * Created by tangjiarao on 16/12/19.
 */
public class PurchaseAdapter extends HolderAdapter<Map<String,Object>, PurchaseAdapter.ViewHolder> {
    public interface Listener{
        void sureClick(int num,String codeLib);
    }
    private Listener listener;
    private List<Map<String,Object>> listData;
    private int count=0;
    public PurchaseAdapter(Context context, List<Map<String, Object>> listData) {
        super(context, listData);
        this.listData =listData;
    }

    @Override
    public View buildConvertView(LayoutInflater layoutInflater,Map<String,Object> b, int position) {
        return inflate(R.layout.purchaseitem_list1);
    }

    @Override
    public ViewHolder buildHolder(View convertView,Map<String,Object> b, int position) {

        ViewHolder holder = new ViewHolder();

        holder.text1 = (TextView)convertView.findViewById(R.id.text1);
        holder.text2 = (TextView)convertView.findViewById(R.id.text2);
        holder.reduceBtn = (Button)convertView.findViewById(R.id.reduceBtn);
        holder.addBtn = (Button)convertView.findViewById(R.id.addBtn);
        holder.sureBtn =(Button)convertView.findViewById(R.id.sureBtn);

        return holder;
    }

    @Override
    public void bindViewDatas(final ViewHolder holder,final Map<String,Object> b, final int position) {

        if (position==0){
            holder.sureBtn.setVisibility(View.VISIBLE);
        }else{
            holder.sureBtn.setVisibility(View.INVISIBLE);
        }
        holder.text1.setText(b.get("libCode").toString());
        holder.text2.setText(b.get("num").toString());
        holder.reduceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int num = (int) b.get("num") ;
                if (num-1<0){
                    return;
                }
                num -=1;
                holder.text2.setText(String.valueOf(num));
                b.put("num", num);
            }
        });
        holder.addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int num = (int) b.get("num") + 1;
                holder.text2.setText(String.valueOf(num));
                b.put("num", num);
            }
        });

        holder.sureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int totalNum =0;
                String totalCodeLib="";
                for (int i=0;i<listData.size();i++){
                    Map item =listData.get(i);

                    int itemNum =(int)item.get("num");

                    if (!((String)item.get("libCode")).equals("")){
                        String itemCodeLib =itemNum +":"+(String)item.get("libCode");
                        if (i!=listData.size()-1)
                            itemCodeLib+=";";


                        totalCodeLib +=itemCodeLib;
                    }
                    totalNum +=itemNum;

                    Log.d("XX",totalNum+" "+totalCodeLib);
                }

                listener.sureClick(totalNum,totalCodeLib);
            }
        });

    }
    public List<Map<String,Object>> getListData(){
        return listData;
    }

    @Override
    public void setListData(List<Map<String,Object>> listData) {
        this.listData = listData;
    }

    public static class ViewHolder {

        Button reduceBtn,addBtn,sureBtn;
        TextView text1,text2;
    }
    public void setListener(Listener listener){
        this.listener =listener;
    }
}
