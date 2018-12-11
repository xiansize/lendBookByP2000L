package com.tc.nfc.app.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import com.tc.nfc.util.Constant;

/**
 * 本类为自定义消息框
 */
public class HintDialog
{
	Dialog mDialog = null;
	private Context mContext = null;
	private IHintDialog mIDialogInstance = null;
	private String title,message;
	/**
	 * 构造函数
	 * @param context
	 */
	public HintDialog(Context context,String title,String message)
	{
		mContext = context;
		this.title = title;
		this.message =message;
		mDialog = new AlertDialog(mContext)
		{

			@Override
            public boolean onKeyDown(int keyCode, KeyEvent event)
            {
				if (keyCode == KeyEvent.KEYCODE_BACK && mIDialogInstance != null)
                {
					//mIDialogInstance.onKeyDown(keyCode, event);
					return true;
                }
				
	            return super.onKeyDown(keyCode, event);
            }
			
		};
		mDialog.setCancelable(false);
		mDialog.setCanceledOnTouchOutside(false);

	}
	
	/**
	 * 
	 * @param iLayoutResId 此DIALOG采用的布局文件
	 * @param interfaceInstance 此DIALOG需要实现的一些接口事件
	 */
	public void showDialog(int iLayoutResId, IHintDialog interfaceInstance)
	{
		if (mDialog == null || iLayoutResId == 0)
        {
	        return;
        }
		
		mIDialogInstance = interfaceInstance;
		
	    
	    
		//dialog.show();一定要放在dialog.getWindow().setLayout(300, 200);的前面，否则不起作用。
		mDialog.show();
//		mDialog.getWindow().setLayout((int) (Constant.displayWidth * 0.9f + 0.5f),
//				(int) (Constant.displayHeight * 0.4f + 0.5f));   
		mDialog.setContentView(iLayoutResId);
		
		Window window = mDialog.getWindow();
		WindowManager.LayoutParams lp =window.getAttributes();
	    lp.y = (int) (-Constant.displayHeight/16);
	    lp.width =(int) (Constant.displayWidth * 0.9f + 0.2f); // 宽度
        
	    window.setAttributes(lp);
	    
		if (mIDialogInstance != null)
        {
			mIDialogInstance.showWindowDetail(window,title,message);
        }
		
	}
	/**
	 * 使dialog消失
	 */
	public void dismissDialog()
	{
		if (mDialog != null && mDialog.isShowing())
        {
	        mDialog.dismiss();
        }
	}
	
	/**
	 * 确定Dialog是否正在显示
	 * @return
	 */
	public boolean isShowing()
	{
		if (mDialog != null && mDialog.isShowing())
        {
	        return mDialog.isShowing();
        }
		return false;
	}
	
	public interface IHintDialog
	{
		//public void onKeyDown(int keyCode, KeyEvent event);
		public void showWindowDetail(Window window, String title, String message);
	}
	
}
