package com.tc.nfc.app.adapter;

/**
 * Created by tangjiarao on 16/7/12.
 */

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tc.api.VersionSetting;
import com.tc.nfc.R;
import com.tc.nfc.app.fragment.IndexFragment;
import com.tc.nfc.model.ShelvesResult;

import java.util.List;


public class BookShelvesRecyclerAdapter extends RecyclerView.Adapter<BookShelvesRecyclerAdapter.MyViewHolder> {

    private Context mContext;
    private LayoutInflater inflater;
    private List<ShelvesResult> mShelvesResultList;
    private boolean isSingleBtn;
    private String shelveNo;
    public interface ShelveListener{
        void onClick(ShelvesResult shelvesResult);
    }
    private ShelveListener shelveListener;


    public BookShelvesRecyclerAdapter(Context context,List<ShelvesResult> shelvesResultList,boolean isSingleBtn,String shelveNo){
        this.mContext=context;
        this.mShelvesResultList =shelvesResultList;
        this.isSingleBtn =isSingleBtn;
        this.shelveNo = shelveNo;
        inflater=LayoutInflater. from(mContext);

    }

    @Override
    public int getItemCount() {

        return mShelvesResultList.size();
    }

    //填充onCreateViewHolder方法返回的holder中的控件
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.title.setText(mShelvesResultList.get(position).getCheckBook().getBookTitle());
        holder.barcode.setText("条形码:"+ mShelvesResultList.get(position).getCheckBook().getBookBarcode());
        holder.publisher.setText("出版社:"+ mShelvesResultList.get(position).getCheckBook().getPublisher());
        holder.publisher.setVisibility(View.GONE);
        holder.pubdate.setText("出版年份:" + mShelvesResultList.get(position).getCheckBook().getPubdate());
        holder.pubdate.setVisibility(View.GONE);
        //直接连接到interlib3开放接口的上架，没有索引号
        if(mShelvesResultList.get(position).getCheckBook().getReferenceNum().equals("暂无")){
            holder.referenceNum.setVisibility(View.GONE);
        }else{
            holder.referenceNum.setText("索引号:" + mShelvesResultList.get(position).getCheckBook().getReferenceNum());
        }

        holder.isbn.setText("ISBN:" + mShelvesResultList.get(position).getCheckBook().getIsbn());
        holder.isbn.setVisibility(View.GONE);
        holder.shelfno.setText("书架号:" + mShelvesResultList.get(position).getCheckBook().getShelfno());

        if (mShelvesResultList.get(position).getCheckResult()){

//            holder.littleText.setText("清点成功");
//            holder.littleText.setTextColor(Color.parseColor("#4fbcb2"));
//            holder.littleImage.setImageResource(R.drawable.res_yes);

            holder.shelfno.setVisibility(View.VISIBLE);
            holder.littleText.setVisibility(View.GONE);
            holder.littleImage.setVisibility(View.GONE);

            Log.d("XX", mShelvesResultList.get(position).getCheckBook().getShelfno()+" "+shelveNo);
            //是否显示上架按钮
            if (mShelvesResultList.get(position).getCheckBook().getShelfno().equals(shelveNo)){
                holder.singleShelveBtn.setVisibility(View.INVISIBLE);
                holder.singleShelveBtn.setOnClickListener(null);
                holder.shelfno_now.setVisibility(View.INVISIBLE);
            }
            else{
                if (!isSingleBtn){
                    holder.singleShelveBtn.setVisibility(View.INVISIBLE);
                    holder.singleShelveBtn.setOnClickListener(null);

                }else{
                    holder.shelfno_now.setText("[" + shelveNo + "]");
                    holder.shelfno_now.setVisibility(View.VISIBLE);
                    holder.singleShelveBtn.setVisibility(View.VISIBLE);
                    holder.singleShelveBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            shelveListener.onClick(mShelvesResultList.get(position));
                        }
                    });
                }

            }
        }
        else{
            holder.littleText.setText(mShelvesResultList.get(position).getMessage());
            holder.littleText.setTextColor(Color.parseColor("#eb6d50"));
            holder.littleImage.setImageResource(R.drawable.res_no);
            holder.shelfno_now.setVisibility(View.INVISIBLE);
            holder.singleShelveBtn.setVisibility(View.GONE);
            holder.singleShelveBtn.setOnClickListener(null);
            holder.littleText.setVisibility(View.VISIBLE);
            holder.littleImage.setVisibility(View.VISIBLE);
        }

        if (null!= mShelvesResultList.get(position).getNotice()&& mShelvesResultList.get(position).getNotice()){
            holder.notice.setVisibility(View.VISIBLE);
        }
        else{
            holder.notice.setVisibility(View.GONE);
        }
    }

    //重写onCreateViewHolder方法，返回一个自定义的ViewHolder
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout. shelvesbook_list,parent, false);
        MyViewHolder holder= new MyViewHolder(view);
        return holder;
    }

    class MyViewHolder extends ViewHolder{

        TextView title,barcode, publisher,pubdate, referenceNum, isbn,singleShelveBtn,shelfno,littleText, shelfno_now;
        ImageView littleImage,notice;

        public MyViewHolder(View view) {
            super(view);

            notice =(ImageView)view.findViewById(R.id.notice);
            title=(TextView) view.findViewById(R.id.tv_title_activity);
            barcode=(TextView) view.findViewById(R.id.barcode);
            publisher =(TextView) view.findViewById(R.id.Publisher);
            pubdate=(TextView) view.findViewById(R.id.pubdate);
            referenceNum =(TextView) view.findViewById(R.id.referenceNum);
            isbn =(TextView) view.findViewById(R.id.isbn);
            singleShelveBtn =(TextView)view.findViewById(R.id.singleShelveBtn);
            shelfno =(TextView)view.findViewById(R.id.shelfno);
            shelfno_now =(TextView)view.findViewById(R.id.shelfno_now);
            littleText=(TextView)view.findViewById(R.id.littleText);
            littleImage=(ImageView)view.findViewById(R.id.littleImage);

        }

    }
    public void setShelveListener(ShelveListener shelveListener){
        this.shelveListener =shelveListener;
    }

    public void setShelveNo (String shelveNo){
        this.shelveNo =shelveNo;
    }

}