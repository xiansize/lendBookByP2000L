package com.tc.nfc.app.view.loadingview;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;

import com.tc.nfc.R;

/**
 * Created by tangjiarao on 16/8/31.
 */
public class BaseLoadingView extends RelativeLayout {

    private Context context;

    /**
     * 布局属性
     */
    private LayoutParams mParams;
    /**
     * loadingView默认高度
     */
    private int viewHeight = ViewGroup.LayoutParams.MATCH_PARENT;

    /**
     * loadingView默认宽度
     */
    private int viewWidth = ViewGroup.LayoutParams.MATCH_PARENT;

    /**
     * loadingView默认颜色
     */
    private int baseView_bg = R.color.baseLoadingView_bg;

    /**
     * loadingView默认圆角
     */
    private int baseView_radius =15;
    /**
     * loadingView默认padding
     */
    private int baseView_paddingLeft =20;
    private int baseView_paddingTop =20;
    private int baseView_paddingRight =20;
    private int baseView_paddingBottom =20;
    /**
     * LoadingView 出现动画和消失动画
     */
    private AlphaAnimation disappearAnimation,appearAnimation;

    /**
     * 设置出现动画、消失动画时间
     */
    private int appearDurationMillis =300;
    private int disappearDurationMillis =300;

    /**
     * 储存初始化view及其params
     */
    private View initView;
    private LayoutParams initViewParams=null;
    /**
     * 储存初始化refreshView及其params
     */
    private View refreshView;
    private LayoutParams refreshViewParams=null;

    /**
     * 后增加view容器
     */
    private  RelativeLayout relativeLayout;

    /**
     * 每次结束loadingview后，记录relativeLayout，删除其内部对initView和refreshview的关联，解决bug
     */
    private RelativeLayout lastLayout=null;

    /**
     * 构造函数一
     * @param context
     */
    public BaseLoadingView(Context context){
        super(context);
        this.context=context;
        initViews();
    }

    /**
     * 构造函数二
     * @param context
     * @param initView  loading首次展现页面
     * @param refreshView  后展示页面
     */
    public BaseLoadingView(Context context, View initView, View refreshView){
        super(context);
        this.initView=initView;
        this.refreshView=refreshView;
        this.context=context;
        initViews();
    }

    /**
     * 初始化渐变动画
     */
    private void initViews() {

        appearAnimation = new AlphaAnimation(0, 1);
        disappearAnimation = new AlphaAnimation(1, 0);

    }

    /**
     * 设置布局参数
     */
    protected void setConfigure(){
        //为loadingView设置布局参数，这里覆盖整个屏幕
        mParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        mParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        setLayoutParams(mParams);

        //设置padding
        setPadding(baseView_paddingLeft,baseView_paddingTop,baseView_paddingRight,baseView_paddingBottom);
        //为loadingView设置背景颜色、圆角
        setBackgroundResource(R.drawable.base_loadview_bg);

        GradientDrawable myGrad = (GradientDrawable)getBackground();
        myGrad.setColor(getResources().getColor(baseView_bg));
        myGrad.setCornerRadius(baseView_radius);

        //如果布满整个屏幕，则不设置圆角
        if (viewHeight==ViewGroup.LayoutParams.MATCH_PARENT
                &&viewWidth==ViewGroup.LayoutParams.MATCH_PARENT){
            myGrad.setCornerRadius(0);
        }
        //设置动画时间
        appearAnimation.setDuration(appearDurationMillis);
        disappearAnimation.setDuration(disappearDurationMillis);

        //新建一个RelativeLayout布局用来装载布局文件
        relativeLayout =new RelativeLayout(context);
        //设置relativeLayout布局
        LayoutParams mParams = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        mParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        addView(relativeLayout,mParams);
    }


    /**
     * 增加view
     * @param view
     * @param layoutParams
     */
    private void addInnerViews(View view, LayoutParams layoutParams){

        if (layoutParams==null)
            relativeLayout.addView(view);
        else
            relativeLayout.addView(view, layoutParams);
    }

    /**
     * 设置初始化布局
     * @param view  加载的view
     * @param layoutParams  加载规则，没有制null
     */
    public void setInitView(View view, LayoutParams layoutParams){
        this.initView =view;
        this.initViewParams=layoutParams;
    }

    /**
     * 设置刷新布局
     * @param view
     * @param layoutParams
     */
    public void setRefreshView(View view, LayoutParams layoutParams){
        this.refreshView =view;
        this.refreshViewParams=layoutParams;
    }
    /**
     * 开始出现动画
     */
    public void startAppearAnimation(){

        //清空上一次relativelayout布局
        if (lastLayout!=null)
            lastLayout.removeAllViews();
        //配置LoadingView布局
        setConfigure();
        //加载初始化view
        addInnerViews(initView, initViewParams);
        super.startAnimation(appearAnimation);

    }
    /**
     * 刷新布局
     */
    public void refreshView(){
        relativeLayout.removeAllViews();
        addInnerViews(refreshView, refreshViewParams);

    }
    /**
     * 开始消失动画
     */
    public void startDisappearAnimation(){
        super.startAnimation(disappearAnimation);
        lastLayout =relativeLayout;
    }

    /**
     * 设置布局大小
     * @param width
     * @param Height
     */
    public void setViewSize(int width,int Height){
        if (width==0&&Height==0){
            this.viewWidth =ViewGroup.LayoutParams.WRAP_CONTENT;
            this.viewHeight =ViewGroup.LayoutParams.WRAP_CONTENT;
            return;
        }
        this.viewWidth =width;
        this.viewHeight =Height;
    }

    /**
     * 设置背景颜色
     * @param colorString
     */
    public void setViewColor(int colorString){
        this.baseView_bg =colorString;
    }

    /**
     * 设置padding
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    public void setViewPadding(int left,int top,int right,int bottom){
        this.baseView_paddingLeft =left;
        this.baseView_paddingTop =top;
        this.baseView_paddingRight = right;
        this.baseView_paddingBottom =bottom;
    }

    /**
     * 设置动画出现持续时间
     * @param appearDurationMillis
     */
    public void setAppearDurationMillis(int appearDurationMillis) {
        this.appearDurationMillis = appearDurationMillis;
    }

    /**
     * 设置动画消失持续时间
     * @param disappearDurationMillis
     */
    public void setDisappearDurationMillis(int disappearDurationMillis) {
        this.disappearDurationMillis = disappearDurationMillis;
    }

    /**
     * 返回动画实例，给调用者获取动画的监听
     * @return
     */
    public AlphaAnimation getAppearAnimation() {
        return appearAnimation;
    }

    /**
     * 返回动画实例，给调用者获取动画的监听
     * @return
     */
    public AlphaAnimation getDisappearAnimation() {
        return disappearAnimation;
    }
}
