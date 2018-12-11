package com.tc.nfc.app.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.tc.nfc.R;
import com.tc.nfc.app.utils.BitmapCache;
import com.tc.nfc.model.Book;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by tangjiarao on 17/1/10.
 */
public class BookItemAdapter extends PagerAdapter {

    private Context mContext;
    private List<Book> bookList =null;
    private Map<String, String> imageUrl;
    private LinkedList<View> mCaches = new LinkedList<View>();
    private LayoutInflater mLayoutInflater = null;
    private Boolean isShow=false;
    private ViewHolder mHolder = null;
    private ImageLoader mImageLoader;
    private ImageLoader.ImageListener imageListener;
    public BookItemAdapter(Context mContext,List<Book> bookList){
        this.bookList = bookList;
        this.mContext = mContext;
        this.bookList = bookList;
        this.mLayoutInflater = LayoutInflater.from(mContext);
        RequestQueue mQueue = Volley.newRequestQueue(mContext);
        mImageLoader = new ImageLoader(mQueue, new BitmapCache());
    }

    @Override
    public int getCount() {
        return this.bookList.size();
    }


    @Override
    public int getItemPosition(Object object) {
        return super.getItemPosition(object);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        View contentView = (View) object;
        container.removeView(contentView);
        this.mCaches.add(contentView);

    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return view == o;
    }

    @Override
    public Object instantiateItem(ViewGroup container,final int position) {
        View convertView =  null;
        mHolder = null;
        if(mCaches.size() == 0){

            convertView = this.mLayoutInflater.inflate(R.layout.viewpage_layout, container,false);
            mHolder = new ViewHolder();
            mHolder.image = (ImageView)convertView.findViewById(R.id.image);
            mHolder.booktitle = (TextView)convertView.findViewById(R.id.booktitle);
            mHolder.Isbn = (TextView)convertView.findViewById(R.id.isbn);
            mHolder.author = (TextView)convertView.findViewById(R.id.author);
            mHolder.price = (TextView)convertView.findViewById(R.id.price);
            mHolder.publisher = (TextView)convertView.findViewById(R.id.publisher);
            mHolder.publishDate = (TextView)convertView.findViewById(R.id.publishDate);
            mHolder.classNo = (TextView)convertView.findViewById(R.id.classNo);
            mHolder.number =(TextView)convertView.findViewById(R.id.number);
            convertView.setTag(mHolder);
        }else{
            convertView = (View)mCaches.removeFirst();
            mHolder = (ViewHolder)convertView.getTag();
        }


        mHolder.booktitle.setText(bookList.get(position).getBookTitle());
        mHolder.Isbn.setText(bookList.get(position).getIsbn());
        mHolder.author.setText(bookList.get(position).getAuthor());
        mHolder.price.setText(bookList.get(position).getPrice());
        mHolder.publisher.setText(bookList.get(position).getPublisher());
        mHolder.publishDate.setText(bookList.get(position).getBookDate());
        mHolder.classNo.setText(bookList.get(position).getClassNo());
        if (bookList.get(position).getIsbn().equals("")){
            mHolder.number.setText("");
        }else{
            mHolder.number.setText("["+(position+1)+"/"+bookList.size()+"]");
        }
        if (isShow){
            mHolder.image.setVisibility(View.VISIBLE);
        }else{
            mHolder.image.setVisibility(View.GONE);
        }

        if (imageUrl!=null){
            imageListener = ImageLoader.getImageListener(mHolder.image, R.drawable.bookbg, R.drawable.bookbg);
            try{
                mImageLoader.get(imageUrl.get(bookList.get(position).getIsbn().replace("-","")), imageListener);
            }catch (NullPointerException e){
                mHolder.image.setImageResource(R.drawable.bookbg);
                Log.d("ERROR", "BookSearchAdapter NullPointerException: " + "由于map中的某些键值对为空值引发的错误");
            }

        }

        container.addView(convertView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT );
        return convertView;

    }


    private class ViewHolder{
        ImageView image;
        TextView booktitle,Isbn,author,price,publisher,publishDate,classNo,number;
    }

    public void setShow(Boolean isShow){
        this.isShow =isShow;
        if (mHolder!=null){
            if (isShow){
                mHolder.image.setVisibility(View.VISIBLE);
            }else{
                mHolder.image.setVisibility(View.GONE);
            }
        }
    }

    public void setImageUrl(Map<String, String> imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setSearch(String Isbn){
        mHolder.booktitle.setText("查询中...");
        mHolder.Isbn.setText(Isbn);
        mHolder.author.setText("获取中...");
        mHolder.price.setText("获取中...");
        mHolder.publisher.setText("获取中...");
        mHolder.publishDate.setText("获取中...");
        mHolder.classNo.setText("获取中...");
    }
}