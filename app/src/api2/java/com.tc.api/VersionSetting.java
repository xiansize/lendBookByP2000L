package com.tc.api;

import com.tc.nfc.R;
import com.tc.nfc.app.fragment.IndexFragment;
import com.tc.nfc.app.fragment.MyFragment;
import com.tc.nfc.app.fragment.SearchFragment;
import com.tc.nfc.util.Constant;

import java.lang.Boolean;
import java.lang.String;

/**
 * Created by tangjiarao on 16/6/23.
 */
public class VersionSetting {

    /**
     * 是否是操作员模式
     */
    public static final boolean IS_ADMIN = true;

    /**
     * 判断interlib是现在版本(interlib3是新版，interlib是旧版)
     * 0:读者模式
     * 1:旧版
     * 2:新版
     */
    public static  int interlibVersion =2;


    /**
     * Tab button text
     */
    //public static final String mTextArray[] = { "读者操作", "查书易", "我的信息" };
    public static final String mTextArray[] = { "读者操作",  "我的信息" };//深圳图书馆 虹口图书馆


    /**
     * 每个tab对于的按钮背景
     */
    //public static final int mImageArray[] = { R.drawable.footer_index_button, R.drawable.footer_search_button, R.drawable.footer_persional_button};
    public static final int mImageArray[] = { R.drawable.footer_index_button,R.drawable.footer_persional_button};//深圳图书馆 虹口图书馆



    /**
     * 对应的fragment
     */
    //public static final Class mFragmentArray[] = {IndexFragment.class,SearchFragment.class,MyFragment.class};
    public static final Class mFragmentArray[] = {IndexFragment.class,MyFragment.class};//深圳图书馆 虹口图书馆

    /**
     * 登录页面logo名
     */
    public static final String logoName = "";

    /**
     * 登录页面logo
     */
    public static final int logoIcon =R.drawable.tcsoft2;//其它图书馆
    //public static final int logoIcon =R.drawable.logo;//广少图
    //public static final int logoIcon = R.drawable.jl_lib_logo;//九龙坡图书馆
    //public static final int logoIcon = R.drawable.deqing_logo;//德清图书馆

    /**
     * 基本功能
     */
    /**
     * 是否打开借书功能
     */
    public static final Boolean IS_OPEN_LOAN =true;
    /**
     * 是否打开续借功能
     */
    public static final Boolean IS_OPEN_RENEW =true;
    /**
     * 是否打开还书功能
     */
    public static final Boolean IS_OPEN_RETURN =true;
    /**
     * 是否打开查询已借功能
     */
    public static final Boolean IS_OPEN_LOAD =true;

    /**
     * 需求功能，只有interlibVersion=2的新版用户可用
     */
    /**
     * 是否打开上架功能
     */
    public static final Boolean IS_OPEN_SHELF =false;
    /**
     * 是否打开清点功能
     */
    public static final Boolean IS_OPEN_CHECK =false;
    /**
     * 是否打开顺架功能
     */
    public static final Boolean IS_OPEN_SHUNJIA =false;
    /**
     * 是否打开阅览功能
     */
    public static final Boolean IS_OPEN_YUELAN =false;
    /**
     * 是否打开采购功能
     */
    public static final Boolean IS_OPEN_CAIGOU =false;


    /**
     * 个人信息扫脸以及地址
     */
    public static final boolean IS_SCAN_FACE = false;
    public static final String SCAN_FACE_ID ="dhj";
    public static final String SCAN_FACE_PASSWORD = "96e79218965eb72c92a549dd5a330112";
    public static final String SCAN_FACE_APP_ID = "1007292";
    public static final String GLOBAL_LIB_ID = "P3CQ023005";//九龙坡    interlibtest全局馆代码
    public static final String USER_LIB = "P3CQ023005";//九龙坡   YNUL用户馆
    public static final String CHEKC_FACE_PICTURE = "http://open.interlib.cn/biometric/service/isfacephoto";
    public static final String SCAN_FACE_FIRST = "http://open.interlib.cn/biometric/service/insertface";
    public static final String SCAN_FACE_AGAIN = "http://open.interlib.cn/biometric/service/insertface";
    public static final String DELETE_SCAN_FACE = "http://open.interlib.cn/biometric/service/delface";
    public static final String FACE_IDENTIFY = "http://open.interlib.cn/biometric/service/faceidentify";
    public static final String GET_FACE = "http://open.interlib.cn/biometric/service/getface";
    public static final String DELETE_SCAN_FACE_USER = "http://open.interlib.cn/biometric/service/delperson";


    //是否打开上传日志
    public static final boolean IS_UPLOAD_LOG = false;


    //本地的日志上传地址
    public static  String PATH_LOG_MACHINE = "http://14.215.172.202:28098/ATMCenter/service/machine/log/save";
    public static  String PATH_LOG_UPDATE_BOOK ="http://14.215.172.202:28098/ATMCenter/service/uploadBooksLog";
    public static  String PATH_LOG_WORKSTATION = "http://14.215.172.202:28098/ATMCenter/service/workstation/save";
    public static  String PATH_LOG_LOGIN = "http://14.215.172.202:28098/ATMCenter/service/cardMachine/save";


    //九龙坡的上传日志地址
    //public static  String PATH_LOG_MACHINE = "http://http://183.230.38.24:8280/ATMCenter/service/machine/log/save";
    //public static  String PATH_LOG_UPDATE_BOOK ="http://183.230.38.24:8280/ATMCenter/service/uploadBooksLog";
    //public static  String PATH_LOG_WORKSTATION = "http://183.230.38.24:8280/ATMCenter/service/workstation/save";
    //public static  String PATH_LOG_LOGIN = "http://183.230.38.24:8280/ATMCenter/service/cardMachine/save";

    /**
     * 获取网络版本号、版本更新接口名
     */
    public static String interfaceName = "gzchild";
    //获取服务器上app版本地址
    public static String GET_SERVER_IP = "http://192.168.0.168:18098/cardcenter/service/getServerVersion";
    //下载新版本app地址
    public static String DOWN_NEW_APP = "http://nfc-10054164.cos.myqcloud.com/nfc_new.apk";


    /**
     * 服务器中间件
     */
    //本电脑
    //public static String baseUrl = "http://192.168.0.69:8080/cardcenter/appInterface";
    //本地
    public static String baseUrl = "http://14.215.172.202:28098/cardcenter/appInterface";
    //连接到ATMcenter
    //public static String baseUrl = "http://14.215.172.202:28098/ATMCenter/service/asc/list";
    //广少图
    //public static String baseUrl = "http://183.63.126.165:8081/cardcenter/appInterface";
    //广图
    //public static String baseUrl = "http://10.0.10.114:8080/cardcenter/appInterface";
    //德清县图书馆
    //public static String baseUrl = "http://220.189.239.254:9801/cardcenter/appInterface";
    //洛少图
    //public static String baseUrl = "";
    //深圳图书馆(本地)
    //public static String baseUrl = "http://14.215.172.202:28098/ATMCenter/service/socket";
    //深圳图书馆
    //public static String baseUrl = "";
    //九龙坡图书馆
    //public static String baseUrl = "http://183.230.38.24:8180/cardcenter/appInterface";



    /**
     * interlib接口地址
     */
    //本地
    public static String baseUrl2 ="http://opac.interlib.com.cn:28098/interlib3";
    //广少图
    //public static String baseUrl2 ="http://192.168.130.105:8080/interlib3";//http://14.18.147.163:8089/interface
    //广图
    //public static String baseUrl2 ="http://10.0.10.15:80/interlib/common/Login";
    //德清县图书馆
    //public static String baseUrl2 = "http://10.38.48.153:8080/interlib";
    //洛少图
    //public static String baseUrl2 = "";
    //深圳图书馆
    //public static String baseUrl2 = "";
    //九龙坡图书馆
    //public static String baseUrl2 = "http://183.230.38.7:81/interlib3";



    /**
     * opac接口地址
     */
    //本电脑
    //public static String  baseUrl3 ="";
    //本地
    public static String baseUrl3="http://14.215.172.202:28098/opac";
    //广少图
    //public static String baseUrl3 ="http://183.63.126.165:8000/opac";
    //广图
    //public static String baseUrl3="http://opac.gzlib.gov.cn/opac";
    //德清县图书馆
    //public static String baseUrl3="http://www.dqlib.com.cn:8080/opac";
    //洛少图
    //public static String baseUrl3 = "";
    //深圳南山图书馆
    //public static String baseUrl3 = "";
    //九龙坡图书馆
    //public static String baseUrl3 = "http://183.230.38.7/opac";


    /**
     * 直接连接到interlib3开放平台接口
     */
    //设置参数控制
    public static final boolean IS_CONNECT_INTERLIB3 = false;
    //interlib3开放接口地址
    public static final String URL_INTERLIB3_OPEN = "http://opac.interlib.com.cn:28098/interlib3";
    //interlib3管理员账号
    public static  String OPERATOR_USER = "yzj";
    //馆代码
    public static final String LIBCODE = "YNUL";//馆代码，用来搜索图书
    //appID
    public static final String INTERLIB3_APP_ID  = "atmopeninterface";
    //appSECRET
    public static final String INTERLIB3_APP_SECRET = "b4e8d12b5bca263f9b1ec36835d29c108";


    /**
     * NFC功能是否打开
     */
    public static final boolean OPEN_NFC_CHECK = false;

    /**
     * app登陆页面配置接口地址
     */
    public static final boolean INTERFACE_SETTING = true;


    /**
     * 图书馆判断是否是书架，及截取的条码数据
     */
    public static final int CUT_DIGITS = 4;//决定截取条码的前几位
    public static final String[] SHLF_NUMBER = new String[]{"MAHF","BGCG","JLPG","SRFG"};//九龙坡书架MAHF

    /**
     * 实现图片轮播的功能
     */
    public static final boolean SLIDE_PICTURE = false;

    //个别图书馆需求
    /**
     *深圳图书馆的配置以及周期连接和参数
     */
    public static final boolean IS_SHENZHEN_LIB = false;
    public static final boolean IS_REPEAT_LOGIN = false;
    public static final int REPEAT_TIME = 30;//登陆时间间隔（秒单位）


    /**
     * P2000L设备的读卡方式
     */
    public static final boolean IS_P2000L_DEVICE = true;
    public static final boolean IS_PRINTER = true;


    //上海虹口图书馆
    public static final boolean IS_HONGKOU = true;



}
