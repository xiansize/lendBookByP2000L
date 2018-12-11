package com.tc.nfc.app.adapter;

/**
 * Created by tangjiarao on 16/7/19.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.tc.api.VersionSetting;
import com.tc.nfc.R;
import com.tc.nfc.app.view.AnimatedExpandableListView;
import com.tc.nfc.model.GroupItem;
import com.tc.nfc.model.ReturnBookResult;

import java.util.List;
import java.util.Map;

/**
 * Adapter for our list of {@link GroupItem}s.
 */
public class ExampleAdapter extends AnimatedExpandableListView.AnimatedExpandableListAdapter {
    private LayoutInflater inflater;

    private List<GroupItem> items;
    private Map<String,String> imageUrl=null;
    private ImageLoader mImageLoader;
    private ImageLoader.ImageListener listener2;
    public interface CheckOnClickListener {

        void onClick(View v,int position,ReturnBookResult data);
        void reFresh();
    }
    private CheckOnClickListener listener;
    public ExampleAdapter(Context context) {

        inflater = LayoutInflater.from(context);
        RequestQueue mQueue = Volley.newRequestQueue(context);
        mImageLoader = new ImageLoader(mQueue, new BitmapCache());
    }

    public void setData(List<GroupItem> items) {
        this.items = items;
    }

    public ReturnBookResult getChild(int groupPosition, int childPosition) {
        return items.get(groupPosition).getItems().get(childPosition);
    }
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    public View getRealChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildHolder holder;
        final ReturnBookResult item = getChild(groupPosition, childPosition);
        if (convertView == null) {
            holder = new ChildHolder();
            convertView = inflater.inflate(R.layout.loadbook_list2, parent, false);
            holder.mark2 =(ImageView)convertView.findViewById(R.id.mark2);
            holder.title = (TextView) convertView.findViewById(R.id.tv_title_activity);
            holder.barcode = (TextView) convertView.findViewById(R.id.barcode);
            holder.barcode.setVisibility(View.GONE);
            holder.returnDate = (TextView) convertView.findViewById(R.id.returnDate);
            holder.loanResult_btn = (ImageButton)convertView.findViewById(R.id.loanResult_btn);
            holder.iv =(ImageView)convertView.findViewById(R.id.image);
            holder.loanDate =(TextView)convertView.findViewById(R.id.loanDate);
            holder.loanDate.setVisibility(View.VISIBLE);
            convertView.setTag(holder);
        } else {
            holder = (ChildHolder) convertView.getTag();
        }

        if (imageUrl!=null){
            listener2 = ImageLoader.getImageListener(holder.iv, R.drawable.bookbg, R.drawable.bookbg);
            try{
                mImageLoader.get(imageUrl.get(item.getBook().getIsbn()), listener2);
            }catch (NullPointerException e){
                holder.iv.setImageResource(R.drawable.bookbg);
                Log.d("ERROR","BookSearchAdapter NullPointerException: "+"由于map中的某些键值对为空值引发的错误");
            }

        }
        holder.mark2.setVisibility(View.INVISIBLE);
        holder.title.setText(item.getBook().getBookTitle());
        holder.barcode.setText(item.getBook().getBookBarcode());
        String loanDate =item.getBook().getLoanDate()==null?"":item.getBook().getLoanDate();
        String returnDate =item.getBook().getReturnDate()==null?"":item.getBook().getReturnDate();
        if(!VersionSetting.IS_SHENZHEN_LIB){
            holder.loanDate.setText("借书日期: " + loanDate);
            holder.returnDate.setText("应还日期: "+returnDate);
        }

        if (item.isReturnResult()){
            holder.loanResult_btn.setVisibility(View.INVISIBLE);
            holder.loanResult_btn.setOnClickListener(null);
        }
        else{
            holder.loanResult_btn.setVisibility(View.VISIBLE);
            holder.loanResult_btn.setImageResource(R.drawable.mark);
            holder.loanResult_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    listener.onClick(v, childPosition, item);
                }
            });
        }

        return convertView;
    }

    @Override
    public int getRealChildrenCount(int groupPosition) {
        return items.get(groupPosition).getItems().size();
    }


    public GroupItem getGroup(int groupPosition) {
        return items.get(groupPosition);
    }


    public int getGroupCount() {
        return items.size();
    }

    public long getGroupId(int groupPosition) {
        return groupPosition;
    }


    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupHolder holder;
        GroupItem item = getGroup(groupPosition);
        if (convertView == null) {
            holder = new GroupHolder();
            convertView = inflater.inflate(R.layout.group_item, parent, false);
            holder.title = (TextView) convertView.findViewById(R.id.textTitle);
            holder.Image = (ImageView)convertView.findViewById(R.id.ImageTitle);
            convertView.setTag(holder);
        } else {

            holder = (GroupHolder) convertView.getTag();
        }

        holder.title.setText(item.getBigTitle());
        if (isExpanded)
            holder.Image.setImageResource(R.drawable.open_icon);
        else
            holder.Image.setImageResource(R.drawable.close_icon);

        return convertView;
    }


    public boolean hasStableIds() {
        return true;
    }

    public boolean isChildSelectable(int arg0, int arg1) {
        return true;
    }

    private static class ChildHolder {
        TextView title;
        TextView barcode;
        TextView returnDate,loanDate;
        ImageView iv,mark2;
        ImageButton loanResult_btn;

    }

    private static class GroupHolder {
        TextView title;
        ImageView Image;
    }

    public void setAdapterListener(CheckOnClickListener listener) {
        this.listener = listener;
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