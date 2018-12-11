package com.tc.nfc.app.adapter;

/**
 * Created by tangjiarao on 16/7/12.
 */
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tc.nfc.R;
import com.tc.nfc.util.Constant;


public class BookLoanRecyclerAdapter extends RecyclerView.Adapter<BookLoanRecyclerAdapter.MyViewHolder> {

    private List<Map<String, Object>> mDatas;
    private Context mContext;
    private LayoutInflater inflater;
    private int action;
    public interface CheckOnClickListener {

        void onClick(View v,int position,Map<String, Object> data);
        void reFresh();
    }
    private CheckOnClickListener listener;

    public BookLoanRecyclerAdapter(Context context, List<Map<String, Object>> datas,int action){
        this.mContext=context;
        this.mDatas=datas;
        this.action=action;
        inflater=LayoutInflater. from(mContext);
    }

    @Override
    public int getItemCount() {

        return mDatas.size();
    }

    //填充onCreateViewHolder方法返回的holder中的控件
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        listener.reFresh();
        holder.res_layout.setVisibility(View.VISIBLE);
        if ((boolean)mDatas.get(position).get("LoanResult")){
            holder.loanResult_btn.setVisibility(View.GONE);
            holder.loanResult_btn.setOnClickListener(null);
            holder.littleText.setText("借书成功");
            if (action== Constant.RETURN_ACTION){
                holder.littleText.setText("还书成功");
            }
            holder.littleText.setTextColor(Color.parseColor("#4fbcb2"));
            holder.littleImage.setImageResource(R.drawable.res_yes);
        }
        else{
            holder.loanResult_btn.setVisibility(View.VISIBLE);
            holder.loanResult_btn.setImageResource(R.drawable.mark);
            holder.littleText.setText("借书失败");
            if (action== Constant.RETURN_ACTION){
                holder.littleText.setText("还书失败");
            }
            holder.littleText.setTextColor(Color.parseColor("#eb6d50"));
            holder.littleImage.setImageResource(R.drawable.res_no);
            holder.loanResult_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    listener.onClick(v, position, mDatas.get(position));
                }
            });
        }

        if ((int)mDatas.get(position).get("isChangeSafe")==-1){
            holder.mark2.setVisibility(View.VISIBLE);

        }else{
            holder.mark2.setVisibility(View.GONE);
        }

        try {
            holder.title.setText(mDatas.get(position).get("title").toString());
        }catch (NullPointerException e){
            holder.title.setText("");
        }
        try {
            holder.barcode.setText("条形码:"+mDatas.get(position).get("barcode").toString());
        }catch (NullPointerException e){
            holder.barcode.setText("条形码:");
        }
        try {
            holder.returnDate.setText("应还日期:"+mDatas.get(position).get("returnDate").toString());
        }catch (NullPointerException e){
            holder.returnDate.setText("应还日期:");
        }

    }

    //重写onCreateViewHolder方法，返回一个自定义的ViewHolder
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout. loadbook_list2,parent, false);
        MyViewHolder holder= new MyViewHolder(view);
        return holder;
    }

    class MyViewHolder extends ViewHolder{

        private TextView title,barcode,returnDate,littleText;
        private ImageButton loanResult_btn;
        private ImageView littleImage,mark2;
        private LinearLayout res_layout;

        public MyViewHolder(View view) {
            super(view);

            loanResult_btn = (ImageButton)view.findViewById(R.id.loanResult_btn);
            title=(TextView) view.findViewById(R.id.tv_title_activity);
            barcode=(TextView) view.findViewById(R.id.barcode);
            returnDate=(TextView) view.findViewById(R.id.returnDate);
            littleText=(TextView)view.findViewById(R.id.littleText);
            littleImage =(ImageView)view.findViewById(R.id.littleImage);
            res_layout=(LinearLayout)view.findViewById(R.id.res_layout);
            mark2 =(ImageView)view.findViewById(R.id.mark2);
        }

    }
    public void setAdapterListener(CheckOnClickListener listener) {
        this.listener = listener;
    }
}