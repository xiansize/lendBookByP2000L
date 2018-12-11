package com.tc.nfc.app.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.tc.nfc.R;
import com.tc.nfc.model.SearchBookResult;

import java.util.List;
import java.util.Map;

/**
 * Created by tangjiarao on 16/8/1.
 */
public class BookSearchAdapter extends HolderAdapter<SearchBookResult, BookSearchAdapter.ViewHolder>{

    private List<SearchBookResult> listData;
    private Map<String,String> imageUrl=null;
    private ImageLoader mImageLoader;
    private ImageLoader.ImageListener listener;

    public interface  BookSearchAdapterListener{
        void onClick(SearchBookResult t);
    }
    public BookSearchAdapterListener bookSearchAdapterListener;
    public BookSearchAdapter(Context context, List<SearchBookResult> listData) {
        super(context, listData);
        this.listData =listData;
        RequestQueue mQueue = Volley.newRequestQueue(context);
        mImageLoader = new ImageLoader(mQueue, new BitmapCache());
    }

    @Override
    public View buildConvertView(LayoutInflater layoutInflater,SearchBookResult t, int position) {
        return inflate(R.layout.vw_list_item);
    }

    @Override
    public ViewHolder buildHolder(View convertView,final SearchBookResult t, int position) {
        ViewHolder holder = new ViewHolder();
        holder.image = (ImageView)convertView.findViewById(R.id.image);
        holder.title = (TextView)convertView.findViewById(R.id.tv_title_activity);
        holder.barcode = (TextView)convertView.findViewById(R.id.barcode);
        holder.author = (TextView)convertView.findViewById(R.id.author);
        holder.classno = (TextView)convertView.findViewById(R.id.classno);
        holder.booktype=(TextView)convertView.findViewById(R.id.booktype);
        holder.layout =(LinearLayout)convertView.findViewById(R.id.layout);


        return holder;
    }

    @Override
    public void bindViewDatas(ViewHolder holder,final SearchBookResult b, int position) {

        holder.title.setText(b.getTitle());
        holder.barcode.setText("作者: " + b.getAuthor());
        holder.author.setText("出版社: "+b.getPublisher());
        holder.classno.setText("出版日期: " + b.getPubdate());
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (bookSearchAdapterListener != null) {
                    bookSearchAdapterListener.onClick(b);

                }

            }
        });

        String type="";
        switch(b.getBooktype()){

            case "1":
                type ="图书";
                break;
            case "2":
                type ="期刊";
                break;
            case "3":
                type ="非书资料";
                break;
            case "4":
                type ="古籍";
                break;
            case "5":
                type ="音像资料";
                break;
            case "6":
                type ="电子图书";
                break;
            case "7":
                type ="光盘资料";
                break;
            case "8":
                type ="外文期刊";
                break;
            case "9":
                type ="分馆图书";
                break;
        }
        holder.booktype.setText("文献类型: "+type);
        if (imageUrl!=null){
            listener = ImageLoader.getImageListener(holder.image, R.drawable.bookbg,R.drawable.bookbg);
            try{
                b.setImageUrl(imageUrl.get(b.getIsbn()));
                mImageLoader.get(imageUrl.get(b.getIsbn()), listener);
            }catch (NullPointerException e){
                holder.image.setImageResource(R.drawable.bookbg);
                Log.d("ERROR","BookSearchAdapter NullPointerException: "+"由于map中的某些键值对为空值引发的错误");
            }

        }
    }


    public static class ViewHolder {

        ImageView image;
        TextView title,barcode,author,classno,booktype;
        LinearLayout layout;

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

    public void setImageUrl(Map<String, String> imageUrl) {
        this.imageUrl = imageUrl;
    }
    public void setBookSearchAdapterListener(BookSearchAdapterListener listener){
        this.bookSearchAdapterListener = listener;
    }
}