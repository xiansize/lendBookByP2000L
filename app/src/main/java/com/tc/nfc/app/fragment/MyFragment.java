package com.tc.nfc.app.fragment;

import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tc.api.VersionSetting;
import com.tc.nfc.R;
import com.tc.nfc.app.activity.LoginActivity;
import com.tc.nfc.app.activity.SettingActivity;
import com.tc.nfc.app.utils.StringToOther;
import com.tc.nfc.app.utils.ToastUtil;
import com.tc.nfc.app.view.HintDialog;
import com.tc.nfc.util.Constant;
import com.tc.nfc.util.NFCApplication;
import com.tc.nfc.core.listener.ActionCallbackListener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MyFragment extends Fragment implements OnClickListener {

    private final String TAG ="MyFragment";
    public NFCApplication mNFCApplication;

    private View view;
    private TextView title, tvUid, tvName,loanedNum, tvMoney, gbLoanedNum,tvScore,tvGbLoanedNumBefore,tvLoaneNumBefore,tvScoreBefore,tvMoneyBefore;
    private LinearLayout persional,book,setting,scanFace;
    private static final int REQUEST_SCAN_FACE = 0x0001;
    private String mCurrentImagePath;
    private File photoFile = null;

    private ImageView ivPersonal;

    private HintDialog hintDialog;//自定义的弹窗

    //刷脸显示弹窗的action
    private int ACTION_TAKE_SCANFACE = 0;
    private int ACTION_SUCCESS_INSERT_FACE_FIRST=1;
    private int ACTION_SUCCESS_INSERT_FACE_AGAIN=2;


    //脸部识别提交参数
    private boolean IS_FACE_PICTURE = false;
    private String loginId = VersionSetting.SCAN_FACE_ID;
    private String password = VersionSetting.SCAN_FACE_PASSWORD;
    private String appid = VersionSetting.SCAN_FACE_APP_ID;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_my, container, false);
        initView();

        return view;
    }

    public void initView(){
        mNFCApplication =(NFCApplication)getActivity().getApplication();
        title = (TextView) view.findViewById(R.id.tv_title_activity);
        title.setText("个人中心");
        setting=(LinearLayout)view.findViewById(R.id.ll_fragment_title_setting);
        tvUid = (TextView) view.findViewById(R.id.uid);
        tvName = (TextView) view.findViewById(R.id.name);
        ivPersonal = (ImageView) view.findViewById(R.id.roundImageView);
        persional = (LinearLayout) view.findViewById(R.id.persional);
        persional.setVisibility(View.VISIBLE);


        //扫脸识别
        scanFace = (LinearLayout) view.findViewById(R.id.ll_fragment_title_scan);
        if( !VersionSetting.IS_ADMIN && VersionSetting.IS_SCAN_FACE){
            scanFace.setVisibility(View.VISIBLE);
        }
        scanFace.setOnClickListener(this);

        if (!VersionSetting.IS_ADMIN){
            tvMoney = (TextView) view.findViewById(R.id.money);
            loanedNum = (TextView) view.findViewById(R.id.loanedNum);
            gbLoanedNum = (TextView) view.findViewById(R.id.gb_loanedNum);
            tvScore = (TextView) view.findViewById(R.id.tvScore);

            //其它接口修改个人显示的信息
            tvMoneyBefore  = (TextView) view.findViewById(R.id.tvMoneyBefore);
            tvGbLoanedNumBefore = (TextView) view.findViewById(R.id.tvGbLoanedNum);
            tvLoaneNumBefore = (TextView) view.findViewById(R.id.tvLoanedNum);
            tvScoreBefore = (TextView) view.findViewById(R.id.tvScoreBefore);

            book = (LinearLayout) view.findViewById(R.id.book);
            book.setVisibility(View.VISIBLE);
        }
        setting.setVisibility(View.VISIBLE);
        setting.setOnClickListener(this);


        if (VersionSetting.IS_ADMIN){
            tvUid.setText(Constant.uid);
            tvName.setText(Constant.uid);

        }else {

            if(VersionSetting.IS_SHENZHEN_LIB) {

                tvGbLoanedNumBefore.setText("办证押金");
                tvScoreBefore.setText("预付款");
                //显示个人借书信息
                tvUid.setText(Constant.readerId);
                tvName.setText(Constant.readerName);
                tvMoney.setText(Constant.readMoney);
                gbLoanedNum.setText(Constant.gbLoanedBook);//显示办证押金
                loanedNum.setText(Constant.loanedBook);//显示本馆已借数目
                tvScore.setText(Constant.score);//显示预付款

            } else if(VersionSetting.IS_CONNECT_INTERLIB3){

                tvMoneyBefore.setText("性别");
                tvGbLoanedNumBefore.setText("启用日期");
                tvLoaneNumBefore.setText("终止日期");
                tvScoreBefore.setText("开户馆");
                //显示个人信息
                tvUid.setText(Constant.readerId);
                tvName.setText(Constant.readerName);
                tvMoney.setText(Constant.readMoney);
                gbLoanedNum.setText(Constant.rdstartdate);//启用时间
                loanedNum.setText(Constant.rdenddate);//终止时间
                tvScore.setText(Constant.rdlibname);//开户馆


            }else{

                //显示个人借书信息
                tvUid.setText(Constant.readerId);
                tvName.setText(Constant.readerName);
                tvMoney.setText(Constant.readMoney);
                loanedNum.setText(Constant.loanedBook);//显示本馆已借数目
                gbLoanedNum.setText(Constant.gbLoanedBook);//显示馆际已借数目
                tvScore.setText(Constant.score);//显示读者积分
            }
            getUserInfo();//获取网络上的读者信息
        }
    }


    /**从网络后台和sp中获取读者数据*/
    private void getUserInfo() {
        mNFCApplication.getUserAction().getUserInfo(new ActionCallbackListener<JSONObject>(){

            @Override
            public void onSuccess(JSONObject data) {
                displayReaderInfo(data);
            }

            @Override
            public void onFailure(String errorEvent, String message) {
                displayReaderInfoAfterFailed(errorEvent,message);
                //账号验证过期，重新登陆
                if(message.contains("账号验证过期")){
                    getActivity().finish();
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    /**
     * 获取后台数据（读者信息）成功，再解析显示在读者个人界面*/
    private void displayReaderInfo(JSONObject data){
        try {

            if(VersionSetting.IS_SHENZHEN_LIB) {

                //tvUid.setText(data.getString("rdid").toString());//读者id显示（已经从Constant中获取到显示）
                //tvName.setText(data.getString("rdname").toString());//读者姓名显示（已经从Constant中获取到显示）
                tvMoney.setText(data.getString("Fine").toString());//显示逾期费用
                gbLoanedNum.setText(data.getString("CardReplacementCharge"));//显示办证押金
                loanedNum.setText(data.getString("LoanedItemCountValue"));//显示本馆已借数目
                tvScore.setText(data.getString("ServiceCharge"));//显示预付款


            }else if(VersionSetting.IS_CONNECT_INTERLIB3){

                tvName.setText(Constant.readerName);
                tvMoney.setText(Constant.readMoney);
                gbLoanedNum.setText(Constant.rdstartdate);//启用时间
                loanedNum.setText(Constant.rdenddate);//终止时间
                tvScore.setText(Constant.rdlibname);//开户馆


            }else{

                //tvUid.setText(data.getString("rdid").toString());//读者id显示（已经从Constant中获取到显示）
                //tvName.setText(data.getString("rdname").toString());//读者姓名显示（已经从Constant中获取到显示）
                tvMoney.setText(data.getString("money").toString());//显示逾期费用
                gbLoanedNum.setText(data.getString("gb_loanedNum"));//显示馆际已借数目
                loanedNum.setText(data.getString("loanedNum"));//显示本馆已借数目
                tvScore.setText(data.getString("score"));//显示读者积分
            }
        } catch (JSONException e) {

            Log.d(TAG,"JSON解析错误");
        }
    }


    /**获取后台数据失败，之后的信息显示为“ ”*/
    private void displayReaderInfoAfterFailed(String errorEvent, String message){
        tvUid.setText("");
        tvName.setText("");
        tvMoney.setText("");
        gbLoanedNum.setText("" );
        loanedNum.setText("");

        try {
            ToastUtil.showToastShortTime(getActivity(),message);
        }catch (NullPointerException e){
            Log.d("EXCEPTION", "未从服务器获取完用户信息，该页面就被finish出现的错误:" + e.toString());
        }
        //账号验证过期，跳到登陆的界面
        if(message.contains("账号验证过期")){
            getActivity().finish();
        }

    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.ll_fragment_title_setting:
                Intent intent = new Intent();
                intent.setClass(this.getActivity(), SettingActivity.class);
                startActivity(intent);
                this.getActivity().overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
                break;
            case R.id.ll_fragment_title_scan:
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    //申请WRITE_EXTERNAL_STORAGE权限
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, 0);
                }
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {

                    showHintDialog("提示","是否前往录入您的脸",ACTION_TAKE_SCANFACE);
                    getActivity().overridePendingTransition(R.anim.slide_right_in,R.anim.slide_left_out);
                }
                break;
        }
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        //回调接收照片
        if(requestCode == REQUEST_SCAN_FACE && resultCode == getActivity().RESULT_OK){
            if(intent != null){
                Bitmap bitmap = intent.getParcelableExtra("data");
                if(bitmap != null){
                    ivPersonal.setImageBitmap(bitmap);//显示扫到的照片
                    tvName.setText("录脸操作中...");
                    checkPicture(bitmap);//先上传到服务器检测是否是图片
                    if(IS_FACE_PICTURE == true){//如果检查正确
                        faceIndentify(bitmap);//识别照片
                    }else{
                        tvName.setText(Constant.readerName);
                        ToastUtil.showToastShortTime(getActivity(),"检测图片失败，请重录");
                        takeFacePicture();
                    }
                }else{
                    Log.d("test","bitmap为空");
                }
            }else{
                // 通过目标uri，找到图片
                Uri uri = Uri.fromFile(photoFile);
                // 显示头像中
                ivPersonal.setImageURI(uri);
            }
        }
    }


    /**
     * 检测图片是否是人脸图片
     * @param bitmap
     */
    private void checkPicture(final Bitmap bitmap){
        mNFCApplication.getmPictureAction().isFacePicture(loginId, password, appid,base64PictureData(bitmap), new ActionCallbackListener<Void>() {
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
     * 识别人脸
     * @param bitmap
     */
    private void faceIndentify(final Bitmap bitmap){
        mNFCApplication.getmPictureAction().faceIdentify(loginId, password, appid, base64PictureData(bitmap), VersionSetting.GLOBAL_LIB_ID, new ActionCallbackListener<JSONObject>() {
            @Override
            public void onSuccess(JSONObject data) {//返回userRecNo
                //识别成功，做多次录入的操作,提交用户记录好
                String userRecNo = null;//识别号
                String userCode = null;//读者证号
                try {
                    userRecNo = data.getString("RECNO");
                    userCode = data.getString("USERCODE");
                    if(userCode.equals(Constant.readerId)){//如果是识别到的是当前的用户，进行多次录脸操作
                        scanFaceAgain(bitmap,userRecNo);
                    }else{
                        ToastUtil.showToastShortTime(getActivity(),"识别到的脸与账号不匹配");
                        tvName.setText(Constant.readerName);
                    }
                } catch (JSONException e) {
                    Log.d(TAG,"json异常:"+e.getMessage());
                }
                //ToastUtil.showToastShortTime(getActivity(),"识别成功");
            }

            @Override
            public void onFailure(String errorEvent, String message) {
                //识别失败，做首次录入的操作
                scanFaceFirst(bitmap);
                //ToastUtil.showToastShortTime(getActivity(),"识别失败："+message);
            }
        });
    }

    /**
     *首次录入脸图片
     * @param bitmap
     */
    private void scanFaceFirst(Bitmap bitmap){
        mNFCApplication.getmPictureAction().RecordFacePictureFirst(loginId, password, appid, base64PictureData(bitmap), VersionSetting.GLOBAL_LIB_ID, VersionSetting.USER_LIB, Constant.readerId, new ActionCallbackListener<Void>() {
            @Override
            public void onSuccess(Void data) {
                tvName.setText(Constant.readerName);
                showHintDialog("提示","首次录入成功！",ACTION_SUCCESS_INSERT_FACE_FIRST);
            }

            @Override
            public void onFailure(String errorEvent, String message) {
                tvName.setText(Constant.readerName);
                ToastUtil.showToastShortTime(getActivity(),"首次录入失败");
            }
        });
    }

    /**
     * 多次录入脸图片
     * @param bitmap
     */
    private void scanFaceAgain(Bitmap bitmap,String userRecNo){
        mNFCApplication.getmPictureAction().RecordFacePictureAgain(loginId, password, appid, base64PictureData(bitmap), VersionSetting.GLOBAL_LIB_ID, userRecNo, new ActionCallbackListener<Void>() {
            @Override
            public void onSuccess(Void data) {
                tvName.setText(Constant.readerName);
                showHintDialog("提示","添加录脸成功",ACTION_SUCCESS_INSERT_FACE_AGAIN);
            }

            @Override
            public void onFailure(String errorEvent, String message) {
                tvName.setText(Constant.readerName);
                ToastUtil.showToastShortTime(getActivity(),"添加录脸失败");
            }
        });
    }

    /**
     * 获取脸照片的操作
     */
    private void takeFacePicture() {
        Intent scanIntent = new Intent();
        //设置动作
        scanIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        //跳转到前置摄像头
        scanIntent.putExtra("android.intent.extras.CAMERA_FACING",1);
        //保存图片到文件中
        if (scanIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            try {
                photoFile = takePictureAsFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //指定了uri则在onActivityResult中，Intent为空为null
            //scanIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
            //开始跳转
            startActivityForResult(scanIntent, REQUEST_SCAN_FACE);
        }
    }



    /**
     * 将图片保存到文件中
     * @return
     * @throws IOException
     */
    private File takePictureAsFile() throws IOException{
        //获取时间作为文件名的一部份
        String dataStr = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
        //文件名为时间组合
        String imageFileName = "image_" + dataStr +"_";
        //手机存放图片的文件的目录
        File storeDir =Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(imageFileName, ".jpg", storeDir);
        //mCurrentImagePath主要为生成该图片的Uri
        mCurrentImagePath = "file:" + imageFile.getAbsolutePath();
        return imageFile;
    }

    /**
     *
     * @param title    提示title
     * @param tipMessage  提示message
     */
    private void showHintDialog(String title, final String tipMessage, final int operation){
        if(hintDialog == null){
            hintDialog = new HintDialog(getContext(),title,tipMessage);
        }
        hintDialog.showDialog(R.layout.dialog_normals_layout, new HintDialog.IHintDialog() {
            @Override
            public void showWindowDetail(Window window, String title, String message) {
                TextView tvMessage = (TextView) window.findViewById(R.id.message);
                tvMessage.setText(tipMessage);

                TextView tvSure = (TextView) window.findViewById(R.id.positiveButton);
                tvSure.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch (operation){
                            case 0:takeFacePicture();//获取拍摄到的人脸照片的操作
                                break;
                            case 1: hintDialog.dismissDialog();
                                break;
                            case 2: hintDialog.dismissDialog();
                                break;
                        }

                        hintDialog.dismissDialog();
                    }
                });

                TextView tvCannel = (TextView) window.findViewById(R.id.negativeButton);
                tvCannel.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        hintDialog.dismissDialog();
                    }
                });
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

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}
