package com.tc.nfc.app.activity;

import com.tc.api.VersionSetting;
import com.tc.nfc.R;
import com.tc.nfc.api.utils.NetworkHelp;
import com.tc.nfc.app.utils.StringToOther;
import com.tc.nfc.app.utils.ToastUtil;
import com.tc.nfc.util.Constant;
import com.tc.nfc.util.NFCApplication;
import com.tc.nfc.app.view.HintDialog;
import com.tc.nfc.core.listener.ActionCallbackListener;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 登录界面Activity
 */
public class LoginActivity extends BaseActivity implements OnClickListener {
	
	private static final String TAG = "LoginActivity";
	public NFCApplication mNFCApplication;
	private SharedPreferences sp;//用户记住密码的sp
	private SharedPreferences repeatSP;//周期性登陆记录用户的信息

	private RelativeLayout rlContent;
	private Toast toast;
	private Button btnLogin;
	private CheckBox cbRemeberPass;
	private EditText etUid, etPassword;
	//loading View
	private ImageView animationIV;
	private AnimationDrawable AniDraw ;
	//消息框
	private HintDialog mDialog = null;
	//图书馆logo
	private TextView tvLogoText;
	private ImageView ivLogoIcon;
	//标志是否正在登陆，如果正在登陆，再次点击登陆按钮无效
	private boolean IS_DO_ACTIO=false;
	private long mStartTime=0;
	//填写接口地址的
    private LinearLayout llInterface;
    //识别脸登陆
    private LinearLayout llScan;

    private static final int REQUEST_SCAN_FACE = 0x0011;

    //脸部识别提交参数
    private boolean IS_FACE_PICTURE = false;
    private String loginId = VersionSetting.SCAN_FACE_ID;
    private String password = VersionSetting.SCAN_FACE_PASSWORD;
    private String appid = VersionSetting.SCAN_FACE_APP_ID;

    //弹窗提醒
    HintDialog hintDialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.getWindowData();
		initView();
		setListener();
	}

	@Override
	protected void initContentView(Bundle savedInstanceState) {
		setContentView(R.layout.activity_login2);

	}

	protected void onResume() {
		super.onResume();
		mDialog=null;
		if(VersionSetting.OPEN_NFC_CHECK){
		    isHasNFC();//是否支持NFC功能，是否已开启NFC功能
		}
		getNetType();//获取网络状态，是否连接wifi
	}
	protected void onPause() {
		super.onPause();
	}

	/**
	 * 初始化
	 */
	private void initView(){

		mNFCApplication =(NFCApplication)this.getApplication();

		//隐藏toolbar_title
		findViewById(R.id.ly_main_tab_bottom).setVisibility(View.INVISIBLE);

		sp = getSharedPreferences("userInfo", Context.MODE_WORLD_READABLE);
        repeatSP = getSharedPreferences("USER_REPEAT_INFO",Context.MODE_PRIVATE);
		etUid = (EditText) findViewById(R.id.uid);
		etPassword = (EditText) findViewById(R.id.pass);
		cbRemeberPass = (CheckBox) findViewById(R.id.rempass);
		btnLogin = (Button) findViewById(R.id.loginbutton);
		animationIV = (ImageView)findViewById(R.id.animationIV);
		animationIV.setBackgroundResource(R.drawable.load_animation_1);
		animationIV.setVisibility(View.INVISIBLE);
		AniDraw = (AnimationDrawable) animationIV.getBackground();
		tvLogoText =(TextView)findViewById(R.id.logoText);
		ivLogoIcon =(ImageView)findViewById(R.id.logoIcon);
		tvLogoText.setText(VersionSetting.logoName);
		//设置app图书馆使用的logo
		ivLogoIcon.setImageResource(VersionSetting.logoIcon);
		rlContent =(RelativeLayout)findViewById(R.id.content);
		isRemPass();

        //设置接口的界面
        llInterface = (LinearLayout) findViewById(R.id.ll_interface_setting);
		if(VersionSetting.INTERFACE_SETTING && VersionSetting.IS_ADMIN){
            llInterface.setVisibility(View.VISIBLE);
        }
        llInterface.setOnClickListener(this);

        //录脸登陆
        llScan = (LinearLayout) findViewById(R.id.ll_login_title_scan);
        if(VersionSetting.IS_SCAN_FACE && !VersionSetting.IS_ADMIN){
            llScan.setVisibility(View.VISIBLE);
        }
        llScan.setOnClickListener(this);
    }
	
	/**
	 * 设置监听
	 */
	private void setListener(){

		btnLogin.setOnClickListener(this);

		//checkbox状态更改监听
		cbRemeberPass.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (cbRemeberPass.isChecked()) {
					sp.edit().putBoolean("isRem", true).commit();
				} else {
					sp.edit().putBoolean("isRem", false).commit();
				}
			}
		});

		rlContent.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				etUid.setCursorVisible(false);//失去光标
				etPassword.setCursorVisible(false);
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

				return false;
			}
		});
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.loginbutton:
//				if (!getNetType()){
//					return;
//				}
            getNetType();//连接wifi提醒
            actionLogin();//登陆
//			if(VersionSetting.IS_HONGKOU){
//                //跳转
//                Intent intent = new Intent();
//                intent.setClass(LoginActivity.this, MainActivity.class);
//                startActivity(intent);
//
//
//			}else{
//				getNetType();//连接wifi提醒
//				actionLogin();//登陆
//			}

			break;

            case R.id.ll_interface_setting:
                Intent intent = new Intent(this,InterfaceActivity.class);
				//Intent intent = new Intent(this,DemoActivity.class);
                startActivity(intent);
                break;
            case R.id.ll_login_title_scan:
                if(("".equals(etPassword.getText().toString())) || (etPassword.getText().toString() == null)){
                    showHintDialog("提示","识脸登陆也需输入密码的哟",0);
                }else{
                    showHintDialog("提示","存在密码，是否前往识脸登陆",1);
                }
                break;
		}
	}


    /**
     * 登陆操作
     */
    public void actionLogin(){
        //对账号密码输入进行规范
        if (etUid.getText().toString().equals("")|| etPassword.getText().toString().equals("")){
            ToastUtil.showToastShortTime(this,"账户或密码不能为空!");
            btnLogin.setText(R.string.user_login);
            return;
        }
        animationIV.setVisibility(View.VISIBLE);
        AniDraw.start();
        //标志是否正在登陆，如果正在登陆，再次点击登陆按钮无效
        if (IS_DO_ACTIO==false){
            IS_DO_ACTIO=true;
            btnLogin.setText(R.string.user_logining);// 点击登陆的时候按钮显示:登陆中...
            login();//登陆
        }
    }

    /**
     * 获取图片操作
     */
    private void getPicture() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {

            takePhoto();
            overridePendingTransition(R.anim.slide_right_in,R.anim.slide_left_out);
        }
}

	/**
	 * 跳转到手机的前置摄像头进行拍摄
	 */
    private void takePhoto() {
        Intent scanIntent = new Intent();
        //设置动作
        scanIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        //跳转到前置摄像头
        scanIntent.putExtra("android.intent.extras.CAMERA_FACING",1);
        //开始跳转
        startActivityForResult(scanIntent, REQUEST_SCAN_FACE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(requestCode == REQUEST_SCAN_FACE && resultCode == RESULT_OK) {
            if (intent != null) {
                Bitmap bitmap = intent.getParcelableExtra("data");
                if (bitmap != null) {
                    checkPicture(bitmap);//先上传到服务器检测是否是图片
                    if (IS_FACE_PICTURE == true) {//如果检查正确
                        actionScanning();
                        faceIndentify(bitmap);//识别照片
                    } else {
                        ToastUtil.showToastShortTime(LoginActivity.this,"照片检测失败");
                        takePhoto();//继续获取拍到的照片
                    }
                } else {
                    Log.d("test", "bitmap为空");
                }
            }
        }
    }

    /**
     * 验证图片是否是人脸
     * @param bitmap
     */
    private void checkPicture(Bitmap bitmap) {
        mNFCApplication.getmPictureAction().isFacePicture(loginId, password, appid, base64PictureData(bitmap), new ActionCallbackListener<Void>() {
            @Override
            public void onSuccess(Void data) {
                IS_FACE_PICTURE = true;
            }

            @Override
            public void onFailure(String errorEvent, String message) {
                IS_FACE_PICTURE = false;
            }
        });
    }

    /**
     * 验证人脸
     * @param bitmap
     */
    private void faceIndentify(Bitmap bitmap) {
        mNFCApplication.getmPictureAction().faceIdentify(loginId, password, appid, base64PictureData(bitmap), VersionSetting.GLOBAL_LIB_ID, new ActionCallbackListener<JSONObject>() {
            @Override
            public void onSuccess(JSONObject data) {
				try {
					String userName = data.getString("USERCODE");
					//显示读者的账号
					actionScanFaceLogin(userName);
				} catch (JSONException e) {
					Log.d(TAG,"json异常:"+e.getMessage());
				}
			}

            @Override
            public void onFailure(String errorEvent, String message) {
                IS_DO_ACTIO = false;
                ToastUtil.showToastShortTime(LoginActivity.this,message);
                btnLogin.setText(R.string.user_login);
            }
        });
    }


    /**
     * 刷脸操作的显示
     */
    public void actionScanning(){
        btnLogin.setText(R.string.user_scan_face_login);
        IS_DO_ACTIO = true;
    }

    /**
     * 刷脸之后的动作
     */
    public void actionScanFaceLogin(String data){
        etUid.setText(data);
        IS_DO_ACTIO = false;//可再次点击登陆
        actionLogin();
    }

    private void saveUserAndPassword(){
        //记录读者账号密码，后期周期登陆(南山图书馆)
        repeatSP.edit().putString("etUid",etUid.getText().toString()).commit();
        repeatSP.edit().putString("etPassword",etPassword.getText().toString()).commit();
        //将账号密码记录下来
        Constant.userName = etUid.getText().toString();
        Constant.password = etPassword.getText().toString();
    }

    /**
	 * 登录
	 */
	public void login(){

		if (!VersionSetting.IS_ADMIN){
			mNFCApplication.getLoginAction().login(etUid.getText().toString(), etPassword.getText().toString(),new ActionCallbackListener<Void>() {

				@Override
				public void onSuccess(Void data) {

                    //用户记住密码
					if (cbRemeberPass.isChecked()) {
						// 记住用户名、密码、
						Editor editor = sp.edit();
						editor.putString("etUid", etUid.getText().toString());
						editor.putString("etPassword", etPassword.getText().toString());
						editor.commit();
					}
                    //自动保留账号密码
                    saveUserAndPassword();
					//提交登陆日志
                    //upLoadMachineLog(VersionSetting.LOG_USER,VersionSetting.LOG_PASSWORD,VersionSetting.MACHINE_ID_LOG_BOOK,"1",getDEVICE_MODAL()+"登陆成功");
                    if(VersionSetting.IS_UPLOAD_LOG) {
                        //提交日志注册
                        upLoadLogLogin(Constant.userName, Constant.readerName,  Constant.LOG_PASSWORD, "202", "", "1", "0", "1");
                    }

					btnLogin.setText(R.string.user_login);//显示登陆字样
					ToastUtil.showToastShortTime(LoginActivity.this,"登陆成功");
					//跳转
					Intent intent = new Intent();
					intent.setClass(LoginActivity.this, MainActivity.class);
					startActivity(intent);
					overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
					IS_DO_ACTIO = false;
					finish();
				}

				@Override
				public void onFailure(String errorEvent, String message) {
					AniDraw.stop();
					animationIV.setVisibility(View.INVISIBLE);
					IS_DO_ACTIO = false;//可再次点击登陆
					btnLogin.setText(R.string.user_login);//显示登陆字样
					ToastUtil.showToastShortTime(LoginActivity.this,message);
				}
			});
		}else{

			mNFCApplication.getLoginAction().loginAdmin(etUid.getText().toString(), etPassword.getText().toString(), new ActionCallbackListener<Void>() {
				@Override
				public void onSuccess(Void data) {

                    //用户选择记住密码
					if (cbRemeberPass.isChecked()) {
						// 记住用户名、密码、
						Editor editor = sp.edit();
						editor.putString("etUid", etUid.getText().toString());
						editor.putString("etPassword", etPassword.getText().toString());
						editor.commit();
					}

                    //自动保存账号密码
                    saveUserAndPassword();
                    if(VersionSetting.IS_UPLOAD_LOG) {
                        //提交日志注册
                        upLoadLogLogin(Constant.machineId,Constant.machineName , Constant.LOG_PASSWORD, "204", "", "", "", "");
                    }

                    btnLogin.setText(R.string.user_login);//显示登陆字样
					ToastUtil.showToastShortTime(LoginActivity.this,"登陆成功");
					//跳转
					Intent intent = new Intent();
					intent.setClass(LoginActivity.this, MainActivity.class);
					startActivity(intent);
					overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
					IS_DO_ACTIO = false;
					finish();
				}

				@Override
				public void onFailure(String errorEvent, String message) {
					AniDraw.stop();
					animationIV.setVisibility(View.INVISIBLE);
					IS_DO_ACTIO = false;
					btnLogin.setText(R.string.user_login);//显示登陆字样
					ToastUtil.showToastShortTime(LoginActivity.this,message);
				}
			});
		}



	}

	/**
	 * 记住密码操作
	 */
	public void isRemPass(){
		if (sp.getBoolean("isRem", false)) {
			etUid.setText(sp.getString("etUid", ""));
			etPassword.setText(sp.getString("etPassword", ""));
		}
		cbRemeberPass.setChecked(sp.getBoolean("isRem", false));
	}

	/**
	 * 是否支持NFC功能
	 */
	public void isHasNFC() {
		int flag =mNFCApplication.getCommonAction().isHasNFC();

		//手机不支持NFC
		if (flag==1){
			createDialog("",getString(R.string.nfc_none));
		}
		//NFC未开启
		else if (flag==2){
			createDialog("",getString(R.string.nfc_unable));
		}
	}

	/**
	 * 获取网络状态
	 * @return
	 */
	public Boolean getNetType(){
		if (NetworkHelp.getNetType(this)!=1){
			//createDialog("", getString(R.string.nfc_wifi));//创建图窗连接wifi
			ToastUtil.showToastShortTime(LoginActivity.this,"请连接WiFi");
		}
		else{
			return true;
		}
		return false;
	}



	/**
	 * 创建弹框
	 * @param title
	 * @param message
	 */
	private void createDialog(String title,String message){
		if(mDialog==null){
			mDialog = new HintDialog(this,title,message);
			mDialog.showDialog(R.layout.dialog_hint_layout, new HintDialog.IHintDialog() {

				@Override
				public void showWindowDetail(Window window, String title,
											 final String message) {
					TextView sureTextView = (TextView) window.findViewById(R.id.positiveButton);
					TextView messageTextView = (TextView) window.findViewById(R.id.message);
					TextView titleTextView = (TextView) window.findViewById(R.id.title_textview);
					sureTextView.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {

							if (message.contains("开启")) {
								startActivity(new Intent("android.settings.NFC_SETTINGS"));
							} else if (message.contains("wifi")){
								startActivity(new Intent("android.settings.WIFI_SETTINGS"));//跳转到手机设置界面打开wifi
							}
							else {
								finish();
							}
							mDialog.dismissDialog();
						}

					});
					messageTextView.setText(message);
				}
			});
		}
	}

	/**
	 * 点击两次退出
	 * @param keyCode
	 * @param event
	 * @return
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

			//当点击的时候计算2次点击的时间差，如果在2秒之内点击2次，就退出
			long currentTimeMillis = System.currentTimeMillis();
			if ((currentTimeMillis - mStartTime) < 2000) {
				super.mNFCApplication.clearAllActivity();
				android.os.Process.killProcess(android.os.Process.myPid());
			} else {
				Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
				mStartTime = System.currentTimeMillis();    //记录点击后的时间
			}
			return true;
		} else
			return super.onKeyDown(keyCode, event);
	}


    /**
     *
     * @param title
     * @param tipMessage
     * @param type
     */
    private void showHintDialog(String title, final String tipMessage, final int type){
        if(hintDialog == null){
            hintDialog = new HintDialog(this,title,tipMessage);
        }
        hintDialog.showDialog(R.layout.dialog_normals_layout, new HintDialog.IHintDialog() {
            @Override
            public void showWindowDetail(Window window, String title, String message) {
                TextView tvMessage = (TextView) window.findViewById(R.id.message);
                tvMessage.setText(tipMessage);

                TextView tvSure = (TextView) window.findViewById(R.id.positiveButton);
                TextView tvCannel = (TextView) window.findViewById(R.id.negativeButton);

                switch (type){
                    case 0:
                        //还没有输入密码
                        tvSure.setText("识别脸");
                        tvSure.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                getPicture();
                                hintDialog.dismissDialog();
                            }
                        });


                        tvCannel.setText("先输密码");
                        tvCannel.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                hintDialog.dismissDialog();
                            }
                        });

                        break;
                    case 1:
                        //输入密码
                        tvSure.setText("识别脸");
                        tvSure.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                getPicture();
                                hintDialog.dismissDialog();
                            }
                        });

                        tvCannel.setText("取消");
                        tvCannel.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                hintDialog.dismissDialog();
                            }
                        });
                        break;
                }

            }
        });
    }




    /**
     * 得到适合上传的base64编码的二进制图片数据
     * @param bitmap
     * @return
     */
    private String base64PictureData(Bitmap bitmap){
        return StringToOther.bitmap2StrByBase64(bitmap).replace("+","%2B");
    }



}
