package com.tc.nfc.util;

import com.tc.nfc.app.utils.Md5Parse;

import java.util.Date;
import org.json.JSONObject;

public class Constant {


	public static String IP="";
	public static String PORT="";
	public static String EMAIL="";

	public static int LOAN_ACTION=0;
	public static int RETURN_ACTION=1;
	public static int RELOAN_ACTION=2;
	public static int OWN_ACTION=3;


	public static int displayWidth;  //屏幕宽度
	public static int displayHeight; //屏幕高度

	public static final String baseKey = "NFC_lend!@#";
	public static String JSESSIONID;//存放cookie，之后与cardcenter的每次交互都要传递cookie
	public static JSONObject reader;//读者信息的json
	public static String[] userKeycode;
    public static String userName = null;//用户的账号
    public static String password = null;//用户的密码


	public static String machineId = "librarian";//设备号码
    public static String machineName = "手机盘点";//设备名字
    public static final String LOG_USER = "admin";//上传日志提交的账号
    public static final String LOG_PASSWORD = "admin";//上传日志提交的密码

	public static String uid = null;
	public static String readerId =null;//读者id
	public static String readerName =null;//读者姓名
	public static String readMoney = null;//逾期费
	public static String loanedBook = null;//本馆借书
	public static String maxLoanedBook = null;//本馆最多借书
	public static String gbLoanedBook = null;//馆际借书
	public static String maxGbLoanedBook = null;//馆际最多借书
	public static String score = null;//积分

	public static Boolean isNeedUpdate=true;

	public static String sessionId =null;

	//直接连接到interlib3的开放接口需要的token
	public static String TOKEN = "";
	public static String rdstartdate = "";//启用时间
	public static String rdenddate = "";//终止时间
	public static String rdlibname = "";//开户馆

	/**
	 * 获取密钥
	 * 
	 * @param rdid
	 * @return
	 */
	public static String[] getKeyCode(String rdid) {
		long time = new Date().getTime();
		String keycode = new Md5Parse().getMD5ofStr(rdid + baseKey + time);
		String[] result = new String[] { keycode, String.valueOf(time) };
		return result;
	}

}
