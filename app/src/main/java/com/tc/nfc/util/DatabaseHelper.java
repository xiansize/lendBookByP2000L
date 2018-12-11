package com.tc.nfc.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {


	// 订购空表 purchase
	public static final String TABLE_PURCHASE = "purchase";
	public static final String KEY_ID_PURCHASE = "_id";
	public static final String KEY_ISBN_PURCHASE = "ISBN";
	public static final String KEY_PURCHASE_NUMS = "已订购数量";
	public static final String KEY_PURCHASE_DATE = "订购时间";
	public static final String KEY_PURCHASE_IMEI = "设备编号";
	public static final String KEY_PURCHASE_IS_UPLOADED = "is_uploaded"; //是否已经上传,默认:否  否= 0, 1=是 
	public static final String KEY_PURCHASE_ORDERLIBLOCAL = "orderLibLocal"; //订购分配字段

	// report字段
	public static final String KEY_ID_REPORT = "_id";
	public static final String KEY_ISBN = "ISBN";
	public static final String KEY_TITLE = "题名";
	public static final String KEY_DOL = "分类号";
	public static final String KEY_AUTHOR = "作者";
	public static final String KEY_PUBLISHER = "出版社";
	public static final String KEY_PLACE_OF_PUBLICATION = "出版地";
	public static final String KEY_DATE_OF_PUBLICATION = "出版日期";
	public static final String KEY_PAGES = "页数";
	public static final String KEY_PRICE = "价格";
	public static final String KEY_SIZE = "尺寸";
	public static final String KEY_CENTER_COLLECTION_NUMS = "本中心馆藏数";
	public static final String KEY_LIBRARY_COLLECTION_NUMS = "本馆馆藏数";
	public static final String KEY_CENTER_RESERVE_NUMS = "本中心预订数";
	public static final String KEY_LIBRARY_RESERVE_NUMS = "本馆预订数";
	public static final String KEY_ORDER_LOANNUM = "预借量";
	public static final String KEY_LOANNUM = "借阅量";
	// 表名report
	public static final String TABLE_REPORT = "report";

	public static final String KEY_ROWID = "_id";
	public static final String KEY_NUMBER = "number";
	public static final String KEY_MYRIABIT = "myriabit";
	public static final String KEY_KILOBIT = "kilobit";
	public static final String KEY_HUNDREDS_PLACE = "hundreds_place";
	public static final String KEY_DECADE = "decade";
	public static final String KEY_THE_UNIT = "the_unit";
	public static final String KEY_MAX_VALUE = "max_value";
	public static final String KEY_MIN_VALUE = "min_value";

	public static final String TABLE_NUMBER = "numbers";
	private static final String DATABASE_NAME = "test"; //purchase.db
	private static final int DATABASE_VERSION = 1;
	public static final String TAG = "DatabaseHelper";
	//同步订购数据的日志表,记录着上次同步后的id,下次获取这个id继续同步
	public static final String TABLE_LOG_SYNC = "log_sync";
	public static final String KEY_ID = "id";
	public static final String KEY_LAST_ID = "last_id";
	public static final String KEY_REGTIME = "regtime";
	
	//临时记录扫描isbn表,用于与前台交互
	public static final String TABLE_SCAN_ISBN = "scan_isbn";
	public static final String KEY_SCAN_ISBN = "isbn";
	public static final String KEY_SCAN_DATE = "scan_date";

	// 构造函数，调用父类SQLiteOpenHelper的构造函数
	@SuppressLint("NewApi")
	public DatabaseHelper(Context context, String name, CursorFactory factory,
			int version, DatabaseErrorHandler errorHandler) {
		super(context, name, factory, version, errorHandler);

	}

	public DatabaseHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		// SQLiteOpenHelper的构造函数参数：
		// context：上下文环境
		// name：数据库名字
		// factory：游标工厂（可选）
		// version：数据库模型版本号
	}

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);

		// 数据库实际被创建是在getWritableDatabase()或getReadableDatabase()方法调用时
		Log.d(TAG, "DatabaseHelper Constructor" + DATABASE_NAME);
		// CursorFactory设置为null,使用系统默认的工厂类
	}

	// SQLiteOpenHelper onCreate(),onUpgrade(),onOpen()
	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(TAG, "DatabaseHelper onCreate");
		String createTableReportSql = "CREATE TABLE" + " " + TABLE_REPORT + " "
				+ "(" + "_id INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_ISBN+" TEXT,"
				+ KEY_TITLE+" TEXT," + KEY_DOL+" TEXT," + KEY_AUTHOR+" TEXT," + KEY_PUBLISHER+" TEXT,"
				+ KEY_PLACE_OF_PUBLICATION+" TEXT," + KEY_DATE_OF_PUBLICATION+" TEXT," + KEY_PAGES+" TEXT," + KEY_PRICE+" TEXT,"
				+ KEY_SIZE+" TEXT," + KEY_CENTER_COLLECTION_NUMS+" TEXT DEFAULT 0," + KEY_LIBRARY_COLLECTION_NUMS+" TEXT DEFAULT 0," + KEY_CENTER_RESERVE_NUMS+" TEXT DEFAULT 0,"
				+ KEY_LIBRARY_RESERVE_NUMS+" TEXT DEFAULT 0," + KEY_ORDER_LOANNUM+" TEXT DEFAULT 0,"+KEY_LOANNUM+" TEXT DEFAULT 0"+");";
		String createTablePurchaseSql = "CREATE TABLE" + " " + TABLE_PURCHASE
				+ " " + "(" + "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ KEY_ISBN_PURCHASE+" TEXT," + KEY_PURCHASE_NUMS+" TEXT,"
				+KEY_PURCHASE_IMEI + " TEXT,"
				+ KEY_PURCHASE_DATE+" DATETIME DEFAULT CURRENT_TIMESTAMP ,"
				+KEY_PURCHASE_IS_UPLOADED +" INTEGER DEFAULT 0 ,"
				+KEY_PURCHASE_ORDERLIBLOCAL +" TEXT "
				+ ");";
		String createTableLogSyncSql = "CREATE TABLE "+TABLE_LOG_SYNC
				+"( "+KEY_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"
				+KEY_LAST_ID+" INTEGER, " 
				+ KEY_REGTIME+" DATETIME DEFAULT CURRENT_TIMESTAMP )";
		String createTableScanIsbnSql = "CREATE TABLE "+TABLE_SCAN_ISBN
				+"( "+KEY_SCAN_ISBN+" TEXT,"
				+KEY_SCAN_DATE+" DATETIME DEFAULT CURRENT_TIMESTAMP );";
		
		db.execSQL(createTablePurchaseSql);
		db.execSQL(createTableReportSql);
		db.execSQL(createTableLogSyncSql);
		db.execSQL(createTableScanIsbnSql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// 调用时间：如果DATABASE_VERSION值被改为别的数,系统发现现有数据库版本不同,即会调用onUpgrade

		// onUpgrade方法的三个参数，一个 SQLiteDatabase对象，一个旧的版本号和一个新的版本号
		// 这样就可以把一个数据库从旧的模型转变到新的模型
		// 这个方法中主要完成更改数据库版本的操作
		Log.d(TAG, "DatabaseHelper onUpgrade");
		// db.execSQL("DROP TABLE IF EXISTS " + TABLE_REPORT);
		// db.execSQL("DROP TABLE IF EXISTS " + TABLE_PURCHASE);
		onCreate(db);
		// 上述做法简单来说就是，通过检查常量值来决定如何，升级时删除旧表，然后调用onCreate来创建新表
		// 一般在实际项目中是不能这么做的，正确的做法是在更新数据表结构时，还要考虑用户存放于数据库中的数据不丢失
		// createTableOrder(db);
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		//db.execSQL("PRAGMA synchronous = OFF;");
		Log.d(TAG, "DatabaseHelper onOpen");
	}

}