package com.tc.nfc.app.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tc.nfc.R;

/**
 * Created by tangjiarao on 16/7/15.
 *
 * 加载LoadingView
 */
public class LoadingView extends RelativeLayout {

    private AnimationDrawable AniDraw;
    private  RelativeLayout.LayoutParams mParams;
    private Context context;
    private AlphaAnimation disappearAnimation,appearAnimation;
    private ImageView loadImage;
    private String loadTexts;

    public LoadingView(Context context,String loadText) {
        super(context);
        this.context =context;
        this.loadTexts =loadText;
        initView();

    }
    public void initView(){

        this.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        this.setBackgroundColor(Color.parseColor("#AA000000"));

        RelativeLayout relativeLayout =new RelativeLayout(context);
        TextView loadText=new TextView(context);
        loadImage = new ImageView(context);
        MarginLayoutParams mp = new MarginLayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);  //item的宽高
        mp.setMargins(100, 0, 0, 0);//分别是margin_top那四个属性

        RelativeLayout.LayoutParams textParams= new RelativeLayout.LayoutParams(mp);
        textParams.addRule(RelativeLayout.RIGHT_OF, loadImage.getId());

        loadImage.setBackgroundResource(R.drawable.load_animation_1);
        AniDraw = (AnimationDrawable) loadImage.getBackground();
        loadText.setText(loadTexts);
        loadText.setTextColor(Color.parseColor("#FFFFFFFF"));

        relativeLayout.addView(loadImage);
        relativeLayout.addView(loadText, textParams);

        mParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);

        mParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        this.addView(relativeLayout, mParams);
        newAppearAnimation();
        newDisappearAnimation();

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    /**
     * 出现动画
     */
    private void newAppearAnimation(){
        appearAnimation = new AlphaAnimation(0, 1);
        appearAnimation.setDuration(300);

    }

    /**
     * 消失动画
     */
    private void newDisappearAnimation(){
        disappearAnimation = new AlphaAnimation(1, 0);
        disappearAnimation.setDuration(300);
    }

    /**
     * 开始出现动画
     */
    public void startAppearAnimation(){
        super.startAnimation(appearAnimation);
        AniDraw.start();
    }

    /**
     * 开始消失动画
     */
    public void startDisappearAnimation(){
        super.startAnimation(disappearAnimation);
        AniDraw.stop();
    }

    /**
     * 更新View
     * @param view
     */
    public void refreshView(View view){

        this.addView(view, mParams);
    }

    /**
     * 返回消失动画
     * @return
     */
    public AlphaAnimation getDisappearAnimation(){

        return disappearAnimation;
    }

    public void removeLoadingView(){
        removeView(loadImage);
    }
}
