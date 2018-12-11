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
import com.tc.api.VersionSetting;
import com.tc.nfc.R;
import com.tc.nfc.app.view.SmoothCheckBox;
import com.tc.nfc.model.Book;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;


public class ReloadAdapter extends HolderAdapter<Book, ReloadAdapter.ViewHolder>{

    private List<Book> listData;
    private boolean isShowBookCheck;
    private String date;

    private Map<String,String> imageUrl=null;
    private ImageLoader mImageLoader;
    private ImageLoader.ImageListener listener;

    public interface ChangeCheckedNumListener {
        void change(boolean isAdd);
    }
    private ChangeCheckedNumListener checknumlistener;

    public ReloadAdapter(Context context, List<Book> listData,boolean isShowBookCheck) {
        super(context, listData);
        this.listData =listData;
        this.isShowBookCheck=isShowBookCheck;
        RequestQueue mQueue = Volley.newRequestQueue(context);
        mImageLoader = new ImageLoader(mQueue, new BitmapCache());
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        date = sDateFormat.format(new java.util.Date());
    }

    @Override
    public View buildConvertView(LayoutInflater layoutInflater,Book b, int position) {
      return inflate(R.layout.loadbook_list3);
    }

    @Override
    public ViewHolder buildHolder(View convertView,Book b, int position) {

        ViewHolder holder = new ViewHolder();
        holder.image = (ImageView)convertView.findViewById(R.id.image);
        holder.title = (TextView)convertView.findViewById(R.id.tv_title_activity);
        holder.barcode = (TextView)convertView.findViewById(R.id.barcode);
        holder.author =(TextView)convertView.findViewById(R.id.author);
        holder.publisher =(TextView)convertView.findViewById(R.id.publisher);
        holder.loanDate =(TextView)convertView.findViewById(R.id.loanDate);
        holder.returnDate = (TextView)convertView.findViewById(R.id.returnDate);
        holder.loanCount = (TextView)convertView.findViewById(R.id.loanCount);
        holder.checkBox =(SmoothCheckBox)convertView.findViewById(R.id.checkbox);
        holder.overDate =(LinearLayout)convertView.findViewById(R.id.overDate);
        holder.number =(TextView)convertView.findViewById(R.id.number);
        return holder;
    }

    @Override
    public void bindViewDatas(ViewHolder holder,final Book b, final int position) {

        Log.d("XX",position+"");
        if (imageUrl!=null){
            listener = ImageLoader.getImageListener(holder.image, R.drawable.bookbg, R.drawable.bookbg);
            try{
                mImageLoader.get(imageUrl.get(b.getIsbn()), listener);
            }catch (NullPointerException e){
                holder.image.setImageResource(R.drawable.bookbg);
                Log.d("ERROR","BookSearchAdapter NullPointerException: "+"由于map中的某些键值对为空值引发的错误");
            }

        }
	    holder.title.setText(b.getBookTitle());
	    //holder.barcode.setText("条形码: " + b.getBookBarcode());
        String author = b.getAuthor() == null ? "" : b.getAuthor();
        String publisher = b.getPublisher() == null ? "" : b.getPublisher();
        String loanDate = b.getLoanDate() == null ? "" : b.getLoanDate();
        String returnDate = b.getReturnDate() == null ? "" : b.getReturnDate();

        if(!VersionSetting.IS_SHENZHEN_LIB) {
            holder.author.setText("作者: " + author);
            if(VersionSetting.IS_CONNECT_INTERLIB3) {
                holder.publisher.setText("馆藏地点: " + publisher);
            }else{
                holder.publisher.setText("出版社: " + publisher);
            }
            holder.loanDate.setText("借书日期: " + loanDate);
            holder.returnDate.setText("应还日期: " + returnDate);
            holder.loanCount.setText("续借次数: " + b.getLoanCount());
        }

        holder.number.setText(String.valueOf(position+1));
        if (isShowBookCheck){
            holder.checkBox.setOnCheckedChangeListener(new SmoothCheckBox.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(SmoothCheckBox checkBox, boolean isChecked) {
                    boolean oldcheck = (b.getIsCheck() == null ? false : b.getIsCheck());
                    b.setIsCheck(isChecked);
                    if (oldcheck != isChecked)
                        checknumlistener.change(isChecked);
                }
            });
          //这句话一定要在setOnCheckedChangeListener下面，不知为什么
            holder.checkBox.setChecked(b.getIsCheck() == null ? false : b.getIsCheck());
            holder.number.setVisibility(View.GONE);
            holder.overDate.setVisibility(View.GONE);
        }
        else{
            holder.number.setVisibility(View.VISIBLE);
            holder.checkBox.setVisibility(View.INVISIBLE);
            if (compare_date(returnDate,date)<0){
                holder.overDate.setVisibility(View.VISIBLE);
            }else{
                holder.overDate.setVisibility(View.INVISIBLE);
            }
        }



    }
    public List<Book> getListData(){
        return listData;
    }

    @Override
    public void setListData(List<Book> listData) {
        this.listData = listData;
    }

    public void setChecknumlistener(ChangeCheckedNumListener checknumlistener){
        this.checknumlistener = checknumlistener;
    }

    public static int compare_date(String DATE1, String DATE2) {

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date dt1 = df.parse(DATE1);
            Date dt2 = df.parse(DATE2);
            if (dt1.getTime() < dt2.getTime()) {
                return -1;
            } else {
                return 0;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return 0;
    }
    public static class ViewHolder {
        
	    ImageView image;
	    TextView title,barcode,returnDate,author,publisher,loanDate,loanCount,number;
        SmoothCheckBox checkBox;
        LinearLayout overDate;
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
}  