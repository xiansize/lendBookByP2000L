
package com.tc.api;

import com.tc.nfc.R;
import com.tc.nfc.app.fragment.IndexFragment;
import com.tc.nfc.app.fragment.MyFragment;
import com.tc.nfc.app.fragment.SearchFragment;
import com.tc.nfc.util.Constant;

/**
 * Created by tangjiarao on 16/6/23.
 */
public class VersionSetting {

    /**
     * 是否是操作员模式
     */
    public static final boolean IS_ADMIN= false;
    /**
     * 判断interlib是现在版本
     * 0:读者模式
     * 1:旧版
     * 2:新版
     */
    public static final int interlibVersion =0;
    /**
     * Tab button text
     */
    public static final String mTextArray[] = { "读者操作", "查书易", "我的信息" };
    //public static final String mTextArray[] = { "读者操作", "我的信息" };//深圳图书馆


    /**
     * 每个tab对于的按钮背景
     */
    public static final int mImageArray[] = { R.drawable.footer_index_button, R.drawable.footer_search_button, R.drawable.footer_persional_button};
    //public static final int mImageArray[] = { R.drawable.footer_index_button, R.drawable.footer_persional_button};//深圳图书馆


    /**
     * 对应的fragment
     */
    public static final Class mFragmentArray[] = {IndexFragment.class,SearchFragment.class,MyFragment.class};
    //public static final Class mFragmentArray[] = {IndexFragment.class,MyFragment.class};//深圳图书馆


    /**
     * 登录页面logo名
     */
    public static final String logoName = "";

    /**
     * 登录页面logo
     */
    //public static final int logoIcon =R.drawable.logo;//广少图的图标
    //public static final int logoIcon =R.drawable.tcsoft2;//通用图标
    public static final int logoIcon = R.drawable.jl_lib_logo;//九龙坡图书馆
    //public static final int logoIcon = R.drawable.deqing_logo;//德清图书馆

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
    public static final Boolean IS_OPEN_RETURN =false;
    /**
     * 是否打开查询已借功能
     */
    public static final Boolean IS_OPEN_LOAD =true;
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
    public static final String LOGIN_ID ="dhj";
    public static final String PASSWORD = "96e79218965eb72c92a549dd5a330112";
    public static final String APP_ID = "1007292";

    //九龙坡图书馆（全局馆代码：P3CQ023005，用户馆：P3CQ023005）
    //全局馆代码
    public static final String GLOBAL_LIB_ID = "P3CQ023005";//interlibtest
    //用户馆
    public static final String USER_LIB = "P3CQ023005";//YNUL

    public static final String CHEKC_FACE_PICTURE = "http://open.interlib.cn/biometric/service/isfacephoto";
    public static final String SCAN_FACE_FIRST = "http://open.interlib.cn/biometric/service/insertface";
    public static final String SCAN_FACE_AGAIN = "http://open.interlib.cn/biometric/service/insertface";
    public static final String DELETE_SCAN_FACE = "http://open.interlib.cn/biometric/service/delface";
    public static final String FACE_IDENTIFY = "http://open.interlib.cn/biometric/service/faceidentify";
    public static final String GET_FACE = "http://open.interlib.cn/biometric/service/getface";
    public static final String DELETE_SCAN_FACE_USER = "http://open.interlib.cn/biometric/service/delperson";




    //是否打开上传日志
    public static final boolean IS_UPLOAD_LOG = false;

    //本地的上传日志地址
    public static  String PATH_LOG_MACHINE = "http://14.215.172.202:28098/ATMCenter/service/machine/log/save";//192.168.0.236:8080
    public static  String PATH_LOG_UPDATE_BOOK ="http://14.215.172.202:28098/ATMCenter/service/uploadBooksLog";//192.168.0.236:8080
    public static  String PATH_LOG_WORKSTATION = "http://14.215.172.202:28098/ATMCenter/service/workstation/save";//192.168.0.236:8080
    public static  String PATH_LOG_LOGIN = "http://14.215.172.202:28098/ATMCenter/service/cardMachine/save";//192.168.0.236:8080


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
     * 获取数据地址
     */
    /**
     * 服务器中间件cardcenter
     */
    //本电脑
    public static String baseUrl = "http://192.168.0.69:8080/cardcenter/appInterface";
    //本地(ATMCenter)
    //public static String baseUrl = "http://14.215.172.202:28098/ATMCenter/service/asc/list";
    //本地
    //public static String baseUrl = "http://14.215.165.10:28098/cardcenter/appInterface";
    //广少图
    //public static String baseUrl = "http://183.63.126.165:8081/cardcenter/appInterface";
    //广图
    //public static String baseUrl = "http://10.0.10.114:8080/cardcenter/appInterface";
    //德清县图书馆
    //public static String baseUrl = "http://220.189.239.254:9801/cardcenter/appInterface";
    //九龙坡图书馆
    //public static String baseUrl = "http://183.230.38.24:8180/cardcenter/appInterface";
    //深圳图书馆(hanxin)
    //public static String baseUrl = "http://192.168.0.242:8080/ATMCenter/service/socket";
    //深圳图书馆(本地)
    //public static String baseUrl = "http://14.215.172.202:28098/ATMCenter/service/socket";


    /**
     * interlib接口地址
     */
    //本地
    public static String baseUrl2 ="http://14.215.172.202:28098/interlib3";
    //广少图
    //public static String baseUrl2 ="http://14.18.147.163:8089/interface";
    //广图
   //public static String baseUrl2 ="http://10.0.10.15:80/interlib/common/Login";
    //德清县图书馆
    //public static String baseUrl2 = "http://10.38.48.153:8080/interlib";
    //九龙坡图书馆
    //public static String baseUrl2 = "http://183.230.38.7:81/interlib3";
    //深圳图书馆
    //public static String baseUrl2 = "";


    /**
     * opca接口地址
     */
    //本地的opac
    public static String baseUrl3 = "http://14.215.172.202:28098/opac";
    //广少图
    //public static String baseUrl3="http://183.63.126.165:8000/opac";
    //广图
    //public static String baseUrl3="http://opac.gzlib.gov.cn/opac";
    //德清县图书馆
    //public static String baseUrl3="http://www.dqlib.com.cn:8080/opac";
    //九龙坡图书馆
    //public static String baseUrl3 = "http://183.230.38.7/opac";
    //深圳南山图书馆
    //public static String baseUrl3 = "";

    /**
     * NFC功能是否打开
     */
    public static final boolean OPEN_NFC_CHECK = false;


    /**
     * app登陆页面是否配置接口地址的入口
     */
    public static final boolean INTERFACE_SETTING = false;


    /**
     * 实现首页图片轮播的功能
     */
    public static final boolean SLIDE_PICTURE = true;


    /**
     * 图书馆判断是否是书架，及截取的条码数据
     */
    public static final int CUT_DIGITS = 2;//决定截取条码的前几位
    //书架的条码的前几位，还有其它的书架（包含其它的书架）
    public static final String SHLF_NUM = "02";
    public static final String SHLF_NUM1 = "";
    public static final String SHLF_NUM2 = "";
    public static final String SHLF_NUM3 = "";

    public static final String[] SHLF_NUMBER = new String[]{"MAHF","BGCG","JLPG","SRFG"};

    //个别图书馆需求
    /**
     *深圳南山图书馆的配置以及周期连接和参数
     */
    public static final boolean IS_SHENZHEN_LIB = false;//是否是深圳图书馆
    public static final boolean IS_REPEAT_LOGIN = false;//是否周期性连接服务器
    public static final int REPEAT_TIME = 30;//登陆时间间隔（秒单位）
}