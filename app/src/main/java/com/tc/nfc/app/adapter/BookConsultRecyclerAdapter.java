package com.tc.nfc.app.adapter;

/**
 * Created by tangjiarao on 16/7/12.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.tc.nfc.R;
import com.tc.nfc.model.ShelvesResult;

import java.util.List;


public class BookConsultRecyclerAdapter extends RecyclerView.Adapter<BookConsultRecyclerAdapter.MyViewHolder> {

    private List<ShelvesResult> list;
    private Context mContext;
    private LayoutInflater inflater;
    private ImageLoader mImageLoader;
    private ImageLoader.ImageListener imageListener;
    public interface CheckOnClickListener {

        void onClick(View v, int position,ShelvesResult shelvesResult);
        void reFresh();
    }
    private CheckOnClickListener listener;

    public BookConsultRecyclerAdapter(Context context, List<ShelvesResult> list){
        this.mContext=context;
        this.list=list;
        inflater=LayoutInflater. from(mContext);
        RequestQueue mQueue = Volley.newRequestQueue(context);
        mImageLoader = new ImageLoader(mQueue, new BitmapCache());
    }

    @Override
    public int getItemCount() {

        return list.size();
    }

    //填充onCreateViewHolder方法返回的holder中的控件
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        listener.reFresh();
        holder.res_layout.setVisibility(View.VISIBLE);
        if (list.get(position).getCheckResult()){
            holder.loanResult_btn.setVisibility(View.INVISIBLE);
            holder.loanResult_btn.setOnClickListener(null);
            holder.littleText.setText("插入日志成功");
            holder.littleText.setTextColor(Color.parseColor("#4fbcb2"));
            holder.littleImage.setImageResource(R.drawable.res_yes);
        }
        else{
            holder.loanResult_btn.setVisibility(View.VISIBLE);
            holder.loanResult_btn.setImageResource(R.drawable.mark);
            holder.littleText.setText("插入日志失败");
            holder.littleText.setTextColor(Color.parseColor("#eb6d50"));
            holder.littleImage.setImageResource(R.drawable.res_no);
            holder.loanResult_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    listener.onClick(v, position, list.get(position));
                }
            });
        }

        if (list.get(position).getCheckBook().getImageUrl()!=null){
            imageListener = ImageLoader.getImageListener(holder.image, R.drawable.bookbg, R.drawable.bookbg);
            try{
                mImageLoader.get(list.get(position).getCheckBook().getImageUrl(), imageListener);
            }catch (NullPointerException e){
                holder.image.setImageResource(R.drawable.bookbg);
                Log.d("ERROR", "BookSearchAdapter NullPointerException: " + "由于map中的某些键值对为空值引发的错误");
            }

        }
        try {
            holder.title.setText(list.get(position).getCheckBook().getBookTitle());
        }catch (NullPointerException e){
            holder.title.setText("");
        }
        try {
            holder.barcode.setText("条形码:"+list.get(position).getCheckBook().getBookBarcode());
        }catch (NullPointerException e){
            holder.barcode.setText("条形码:");
        }
        try {
            holder.publisher.setText("出版社:"+list.get(position).getCheckBook().getPublisher());
        }catch (NullPointerException e){
            holder.publisher.setText("出版社:");
        }
        try {
            holder.pubdate.setText("出版年:" + list.get(position).getCheckBook().getPubdate());
        }catch (NullPointerException e){
            holder.pubdate.setText("出版年:");
        }
        try {
            holder.isbn.setText("ISBN:" + list.get(position).getCheckBook().getIsbn());
        }catch (NullPointerException e){
            holder.isbn.setText("ISBN:");
        }
        try {
            holder.callno.setText("索引号:" + list.get(position).getCheckBook().getReferenceNum());
        }catch (NullPointerException e){
            holder.callno.setText("索引号:");
        }

    }

    //重写onCreateViewHolder方法，返回一个自定义的ViewHolder
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout. booklog_list,parent, false);
        MyViewHolder holder= new MyViewHolder(view);
        return holder;
    }

    class MyViewHolder extends ViewHolder{

        TextView title,barcode,publisher,pubdate,isbn,callno,littleText;
        ImageButton loanResult_btn;
        ImageView littleImage,image;
        LinearLayout res_layout;

        public MyViewHolder(View view) {
            super(view);

            loanResult_btn = (ImageButton)view.findViewById(R.id.loanResult_btn);
            title=(TextView) view.findViewById(R.id.tv_title_activity);
            barcode=(TextView) view.findViewById(R.id.barcode);
            publisher=(TextView) view.findViewById(R.id.publisher);
            pubdate=(TextView) view.findViewById(R.id.pubdate);
            isbn=(TextView) view.findViewById(R.id.isbn);
            callno=(TextView) view.findViewById(R.id.callno);

            littleText=(TextView)view.findViewById(R.id.littleText);
            image =(ImageView)view.findViewById(R.id.image);
            littleImage =(ImageView)view.findViewById(R.id.littleImage);
            res_layout=(LinearLayout)view.findViewById(R.id.res_layout);
        }

    }
    public class BitmapCache implements ImageLoader.ImageCache {
        private LruCache<String, Bitmap> mCache;

        public BitmapCache() {
            int maxSize = 10 * 1024 * 1024;
            mCache = new LruCache<String, Bitmap>(maxSize) {
                @Override
                protected int sizeOf(String key, Bitmap value) {
                    return value.getRowBytes() * value.getHeight();
                }

            };
        }

        @Override
        public Bitmap getBitmap(String url) {
            return mCache.get(url);
        }

        @Override
        public void putBitmap(String url, Bitmap bitmap) {
            mCache.put(url, bitmap);
        }

    }
    public void setAdapterListener(CheckOnClickListener listener) {
        this.listener = listener;
    }
}