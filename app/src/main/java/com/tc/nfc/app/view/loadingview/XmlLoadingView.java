package com.tc.nfc.app.view.loadingview;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.tc.nfc.R;

/**
 * Created by tangjiarao on 16/9/1.
 * 使用xml布局文件设置LoadingView样式
 */
public class XmlLoadingView extends BaseLoadingView {

    //帧动画
    private AnimationDrawable AniDraw;
    private Context context;

    //loadImageview
    private ImageView loadImageView;

    LayoutInflater flater ;

    private Button b1 ;
    private TextView textView,loadtext;

    public XmlLoadingView(Context context) {
        super(context);
        this.context=context;
        flater= LayoutInflater.from(context);
        setInitView();
        setReFreshView();
        setListener();
    }

    /**
     * 设置开始加载时的界面
     */
    private void setInitView(){

        View view = flater.inflate(R.layout.init_view, null);
        loadImageView=(ImageView)view.findViewById(R.id.loadimage);
        loadImageView.setBackgroundResource(R.drawable.load_animation_1);
        AniDraw = (AnimationDrawable) loadImageView.getBackground();
        loadtext=(TextView)view.findViewById(R.id.loadtext);
        super.setInitView(view, null);
    }

    /**
     * 设置更新的界面
     */
    private void setReFreshView(){

        View view = flater.inflate(R.layout.refresh_view, null);
        super.setRefreshView(view, null);
        textView =(TextView)view.findViewById(R.id.bigTitle);
        b1 =(Button)view.findViewById(R.id.bt1);
    }

    /**
     * 获取回调
     */
    public void setListener(){
        super.getDisappearAnimation().setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                AniDraw.stop();
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        super.getAppearAnimation().setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                AniDraw.start();
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public Button getB1() {
        return b1;
    }

    public TextView getTextView() {
        return textView;
    }

    public void setText(String str){
        loadtext.setText(str);
    }

}
