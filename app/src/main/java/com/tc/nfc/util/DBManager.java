package com.tc.nfc.util;

import java.util.ArrayList;
import java.util.List;

import com.tc.nfc.model.Purchase;
import com.tc.nfc.util.DatabaseHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

//http://blog.csdn.net/liuhe688/article/details/6715983  
public class DBManager {
	// private static final String TAG = "DBManager";
	private final DatabaseHelper helper;
	private final SQLiteDatabase db;
	private String TAG ="DBManager";

	public DBManager(Context context) {
		Log.d(TAG, "DBManager --> Constructor");
		helper = new DatabaseHelper(context);
		db = helper.getWritableDatabase();
		System.out.println(db);
	}

	public Cursor queryTheCursor() {
		Log.d(TAG, "DBManager --> queryTheCursor");
		Cursor c = db.query(DatabaseHelper.TABLE_NUMBER, null, null, null,
				null, null, null, null);
		;
		return c;
	}

	public Cursor queryTheCursor2(String limitStr) {
		Log.d(TAG, "DBManager --> queryTheCursor");
		Cursor c = db.query(DatabaseHelper.TABLE_NUMBER, null, null, null,
				null, null, null, limitStr);
		;
		return c;
	}

	/**
	 * close database
	 */
	public void closeDB() {
		Log.d(TAG, "DBManager --> closeDB");
		db.close();
	}

	// get designation report record(book info) according to isbn
	public Cursor getReport(String isbn) throws SQLException {
		Cursor mCursor = db.query(true, DatabaseHelper.TABLE_REPORT,
				new String[] { DatabaseHelper.KEY_ID_REPORT,
						DatabaseHelper.KEY_ISBN, DatabaseHelper.KEY_TITLE,
						DatabaseHelper.KEY_DOL, DatabaseHelper.KEY_AUTHOR,
						DatabaseHelper.KEY_PUBLISHER,
						DatabaseHelper.KEY_PLACE_OF_PUBLICATION,
						DatabaseHelper.KEY_DATE_OF_PUBLICATION,
						DatabaseHelper.KEY_PAGES, DatabaseHelper.KEY_PRICE,
						DatabaseHelper.KEY_SIZE,
						DatabaseHelper.KEY_CENTER_COLLECTION_NUMS,
						DatabaseHelper.KEY_LIBRARY_COLLECTION_NUMS,
						DatabaseHelper.KEY_CENTER_RESERVE_NUMS,
						DatabaseHelper.KEY_LIBRARY_RESERVE_NUMS,
						DatabaseHelper.KEY_LOANNUM},
				// DatabaseHelper.KEY_ID_REPORT+"="+id,
				DatabaseHelper.KEY_ISBN + "=" + "'" + isbn + "'", null, null,
				null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;

	}

	// insert test data into report
	public void insertReport(String isbn, String title, String dol,
			String author, String publisher, String place_of_publication,
			String date_of_publication, String pages, String price,
			String size, String center_collection_nums,
			String library_collection_nums, String center_reserve_nums,
			String library_reserve_nums) {

		// 采用事务处理，确保数据完整性
		// db.beginTransaction(); // 开始事务
		ContentValues values=new ContentValues();
		values.put(DatabaseHelper.KEY_ISBN, isbn);//isbn
		values.put(DatabaseHelper.KEY_TITLE, title);//题名
		values.put(DatabaseHelper.KEY_AUTHOR, author);//作者
		values.put(DatabaseHelper.KEY_DOL, dol);//分类号
		values.put(DatabaseHelper.KEY_PUBLISHER, publisher);//出版社
		values.put(DatabaseHelper.KEY_DATE_OF_PUBLICATION, date_of_publication);//出版时间
		values.put(DatabaseHelper.KEY_PAGES, pages);//页数
		values.put(DatabaseHelper.KEY_PRICE, price);//价格
		values.put(DatabaseHelper.KEY_SIZE, size);// 尺寸
		values.put(DatabaseHelper.KEY_CENTER_COLLECTION_NUMS, center_collection_nums);//本中心馆藏数
		values.put(DatabaseHelper.KEY_LIBRARY_COLLECTION_NUMS, library_collection_nums);//本馆馆藏数
		values.put(DatabaseHelper.KEY_CENTER_RESERVE_NUMS, center_reserve_nums);//本中心预订数
		values.put(DatabaseHelper.KEY_LIBRARY_RESERVE_NUMS, library_reserve_nums);//本馆预订数
		db.insert(DatabaseHelper.TABLE_REPORT,null,values);
		/*db.execSQL("INSERT INTO " + DatabaseHelper.TABLE_REPORT 
				+" ("+DatabaseHelper.KEY_ISBN+",'题名','分类号','作者','出版社','出版地','出版日期','页数','价格','尺寸','本中心馆藏数','本馆馆藏数','本中心预订数','本馆预订数')"
				+ " VALUES("
				+ " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", new Object[] {
				isbn, title, dol, author, publisher, place_of_publication,
				date_of_publication, pages, price, size,
				center_collection_nums, library_collection_nums,
				center_reserve_nums, library_reserve_nums });*/
		// db.setTransactionSuccessful(); // 设置事务成功完成
		// db.endTransaction(); // 结束事务
		return;

	}

	//数据库插入11个参数
	public void insertReport(String ISBN,String title,String callno,String author,String publisher,
			String publishdate,String price,String library_collection_nums,String orderLoannum ,
			String loannum,String library_reserve_nums){
		
		ContentValues values=new ContentValues();
		values.put(DatabaseHelper.KEY_ISBN, ISBN);//isbn
		values.put(DatabaseHelper.KEY_TITLE, title);//题名
		values.put(DatabaseHelper.KEY_AUTHOR, author);//作者
		values.put(DatabaseHelper.KEY_DOL, callno);//分类号
		values.put(DatabaseHelper.KEY_PUBLISHER, publisher);//出版社
		values.put(DatabaseHelper.KEY_DATE_OF_PUBLICATION, publishdate);//出版时间
		values.put(DatabaseHelper.KEY_PRICE, price);//价格
		values.put(DatabaseHelper.KEY_LIBRARY_COLLECTION_NUMS, library_collection_nums);//本馆馆藏数
		values.put(DatabaseHelper.KEY_LIBRARY_RESERVE_NUMS, library_reserve_nums);//本馆预订数
		values.put(DatabaseHelper.KEY_ORDER_LOANNUM, orderLoannum);//预借量
		values.put(DatabaseHelper.KEY_LOANNUM, loannum);//借阅量
		db.insert(DatabaseHelper.TABLE_REPORT,null,values);
		/*db.execSQL("INSERT INTO " + DatabaseHelper.TABLE_REPORT
				+" (ISBN,'题名','分类号','作者','出版社','出版日期','价格','本馆馆藏数','预借量','借阅量','本馆预订数')"
				+ "VALUES "
				+ "(?,?,?,?,?,?,?,?,?,?,?)",
					new Object[]{ISBN,title,callno,author,publisher,publishdate,price,library_collection_nums,
								orderLoannum,loannum,library_reserve_nums});*/
		return;
		
	}
	// insert test data into report
	public void insertReportList(List<String[]> reportList) {
		// 采用事务处理，确保数据完整性
		db.beginTransaction(); // 开始事务
		int size = reportList.size();
		for (int i = 0; i < size; i++) {
			String[] record = reportList.get(i);
			try {
				if(record.length == 11){
					if("ISBN".equals(record[0]) && "题名".equals(record[1]) && "分类号".equals(record[2]) 
							&& "作者".equals(record[3]) && "出版社".equals(record[4]) && "出版日期".equals(record[5])
							&& "价格".equals(record[6]) && "库存".equals(record[7]) && "预借量".equals(record[8])
							&& "借阅量".equals(record[9]) && "本馆预订数".equals(record[10])){
						//离线数据插入数据库
						continue;
						
					}else {
						insertReport(record[0],record[1],record[2],record[3],
								record[4],record[5],record[6],record[7],
								record[8],record[9],record[10]);
					}
				}else{
					insertReport(record[0], record[1], record[2], record[3],
							record[4], record[5], record[6], record[7], record[8],
							record[9], record[10], record[11], record[12],
							record[13]);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		db.setTransactionSuccessful(); // 设置事务成功完成
		db.endTransaction(); // 结束事务
		return;

	}

	// insert purchase_nums into purchase
	public long insertPurchase(String isbn, String purchase_nums,String imei,String orderLibLocal) {

		ContentValues initialValues = new ContentValues();

		initialValues.put(DatabaseHelper.KEY_ISBN_PURCHASE, isbn);
		initialValues.put(DatabaseHelper.KEY_PURCHASE_NUMS, purchase_nums);
		initialValues.put(DatabaseHelper.KEY_PURCHASE_IMEI, imei);
		initialValues.put(DatabaseHelper.KEY_PURCHASE_ORDERLIBLOCAL, orderLibLocal);
		return db.insert(DatabaseHelper.TABLE_PURCHASE, null, initialValues);

	}

	// 未修改完成
	// update purchase
	public boolean updatePurchase(String isbn, String purchase_nums,String imei,String orderLibLocal) {

		ContentValues args = new ContentValues();

		args.put(DatabaseHelper.KEY_ISBN_PURCHASE, isbn);
		args.put(DatabaseHelper.KEY_PURCHASE_NUMS, purchase_nums);
		args.put(DatabaseHelper.KEY_PURCHASE_IMEI, imei);
		args.put(DatabaseHelper.KEY_PURCHASE_IS_UPLOADED, 0);
		args.put(DatabaseHelper.KEY_PURCHASE_ORDERLIBLOCAL, orderLibLocal);



		return db
				.update(DatabaseHelper.TABLE_PURCHASE, args,
						DatabaseHelper.KEY_ISBN_PURCHASE + "=" + "'" + isbn
								+ "'", null) > 0;

	}

	// get designation purchase record(purchase_nums) according to isbn
	public Cursor getPurchase(String isbn) throws SQLException {

		Cursor mCursor = db.query(true, DatabaseHelper.TABLE_PURCHASE,
				new String[] { DatabaseHelper.KEY_ID_PURCHASE,
						DatabaseHelper.KEY_ISBN_PURCHASE,
						DatabaseHelper.KEY_PURCHASE_NUMS,
						DatabaseHelper.KEY_PURCHASE_ORDERLIBLOCAL},
				// DatabaseHelper.KEY_ID_REPORT+"="+id,
				(isbn==""?null:DatabaseHelper.KEY_ISBN_PURCHASE + "=" + "'" + isbn + "'"),
				null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		
		return mCursor;

	}


	/**
	 * 获取总记录数
	 * 
	 * @return
	 */
	public long getCountOfReports() {
		Cursor cursor = db.rawQuery("select count(*) from "
				+ DatabaseHelper.TABLE_REPORT, null);
		// 游标移到第一条记录准备获取数据
		cursor.moveToFirst();
		// 获取数据中的LONG类型数据
		long count = cursor.getLong(0);
		return count;
	}

	public void createIsbnIdx() {
		Log.d(TAG, "DatabaseHelper createIsbnIdx");
		db.execSQL("CREATE INDEX idx_" + DatabaseHelper.TABLE_REPORT
				+ "_isbn ON " + DatabaseHelper.TABLE_REPORT + "("
				+ DatabaseHelper.KEY_ISBN + ")");
	}

	public void dropIsbnIdx() {
		Log.d(TAG, "DatabaseHelper createIsbnIdx");
		db.execSQL("DROP INDEX idx_" + DatabaseHelper.TABLE_REPORT + "_isbn");
	}



	/**
	 * 判断SDCard是否存在 [当没有外挂SD卡时，内置ROM也被识别为存在sd卡]
	 * 
	 * @return
	 */
	public static boolean isSdCardExist() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}
	
	public long getCountsOfTable(){
		String sql = "select sum(" +"\"" + DatabaseHelper.KEY_PURCHASE_NUMS + "\""+") from "+DatabaseHelper.TABLE_PURCHASE;
		Cursor cursor = db.rawQuery(sql, null);
		// 游标移到第一条记录准备获取数据
		cursor.moveToFirst();
		// 获取数据中的LONG类型数据
		long count = cursor.getLong(0);
		return count;
	}
	
	/**
	 * 查询同步表,是否有上次的同步记录,如果没有返回0,如果有返回last_id(上次同步的id)
	 */
	public int getLastSyncId(){
		String sql = "select last_id from log_sync order by regtime desc,id limit 1 offset 0";//这是是分页取第一条记录
		Cursor cursor = db.rawQuery(sql,null);
		int count = cursor.getCount();
		if(count == 0){//没有记录
			return 0;
		}
		if(cursor.moveToFirst()){
			String lastIdStr = cursor.getString(0);
			if(lastIdStr != null && lastIdStr != ""){
				return Integer.valueOf(lastIdStr);
			}else{
				return 0;
			}
		}
		return  0;
	}

	public List<Purchase> getPurchaseThanId(String uploadType) {
		String sql = "";
		if("2".equals(uploadType)){//重新上传
			sql = "select ISBN,"+DatabaseHelper.KEY_PURCHASE_NUMS+","+DatabaseHelper.KEY_PURCHASE_DATE+","+DatabaseHelper.KEY_PURCHASE_IMEI+","+DatabaseHelper.KEY_PURCHASE_ORDERLIBLOCAL+" from "+DatabaseHelper.TABLE_PURCHASE;
		}else{//追加上传
			sql = "select ISBN,"+DatabaseHelper.KEY_PURCHASE_NUMS+","+DatabaseHelper.KEY_PURCHASE_DATE+","+DatabaseHelper.KEY_PURCHASE_IMEI+","+DatabaseHelper.KEY_PURCHASE_ORDERLIBLOCAL+" from "+DatabaseHelper.TABLE_PURCHASE+" where "+DatabaseHelper.KEY_PURCHASE_IS_UPLOADED +" = 0";
		}
		Cursor cursor = db.rawQuery(sql,null);
		int count = cursor.getCount();
		List<Purchase> list = new ArrayList<Purchase>();
		if(count == 0){//没有记录
			return list;
		}else{
			while(cursor.moveToNext()){
				int isbnIndex = cursor.getColumnIndex("ISBN");
				int ordernumIndex = cursor.getColumnIndex(DatabaseHelper.KEY_PURCHASE_NUMS);
				int dateIndex = cursor.getColumnIndex(DatabaseHelper.KEY_PURCHASE_DATE);
				int imeiIndex = cursor.getColumnIndex(DatabaseHelper.KEY_PURCHASE_IMEI);
				int orderLibLocalIndex = cursor.getColumnIndex(DatabaseHelper.KEY_PURCHASE_ORDERLIBLOCAL);
				String isbn = cursor.getString(isbnIndex);
				String ordernum = cursor.getString(ordernumIndex);
				String date = cursor.getString(dateIndex);
				String imei = cursor.getString(imeiIndex);
				String orderLibLocal = cursor.getString(orderLibLocalIndex);
				list.add(new Purchase(isbn,Integer.valueOf(ordernum),date,imei,orderLibLocal));
				
			}
			return list;
		}
	}

	public void savePurchaseLogId() {
		String sql = "insert into "+DatabaseHelper.TABLE_LOG_SYNC+" ("+DatabaseHelper.KEY_LAST_ID+") values ((select max("+DatabaseHelper.KEY_ID_PURCHASE+") from "+DatabaseHelper.TABLE_PURCHASE+"))";
		db.rawQuery(sql, null);
	}
	
	public void saveOrderText(){
		int a = 9999;
		int b = 999;
		int c = 999;
		for(int i = 0 ;i < 100000 ; i++){
			if(i % 999 == 0){
				b = b - 1;
			}else if(i % 9999 == 0){
				a = a - 1;
			}else{
				c = c - 1;
			}
			ContentValues initialValues = new ContentValues();
			initialValues.put(DatabaseHelper.KEY_ISBN_PURCHASE, String.valueOf(a)+String.valueOf(b)+String.valueOf(c));
			initialValues.put(DatabaseHelper.KEY_PURCHASE_NUMS, 1);
			initialValues.put(DatabaseHelper.KEY_PURCHASE_IMEI, "123456789");
			db.insert(DatabaseHelper.TABLE_PURCHASE, null, initialValues);
			initialValues.clear();
		}
	}

	public void updatePurchaseUploaded() {
		ContentValues initialValues = new ContentValues();
		initialValues.put(DatabaseHelper.KEY_PURCHASE_IS_UPLOADED, 1);
		db.update(DatabaseHelper.TABLE_PURCHASE, initialValues, null, null);
		
	}

	public void insertReport(String isbn) {
		//先判断本地是否存在
		String sql = "select isbn from "+DatabaseHelper.TABLE_REPORT+" where isbn = \"" + isbn + "\" ";
		Cursor cursor = db.rawQuery(sql,null);
		System.out.println(cursor.moveToFirst());
		if(!cursor.moveToFirst()){ //就是没有记录的,新增记录
			String insertSQL = "insert into "+DatabaseHelper.TABLE_REPORT+" ("+DatabaseHelper.KEY_ISBN_PURCHASE+") values ("+isbn+")";
			//db.rawQuery(insertSQL, null);
			ContentValues initialValues = new ContentValues();
			initialValues.put(DatabaseHelper.KEY_ISBN, isbn);
			db.insert(DatabaseHelper.TABLE_REPORT, null, initialValues);
		}
		
	}

	public void clearReportData() {
		int delete = db.delete(DatabaseHelper.TABLE_REPORT, null, null);
		
		System.out.println(delete);
	}

	public void clearOrderNumByIsbnForReport(String isbn) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(DatabaseHelper.KEY_LIBRARY_RESERVE_NUMS, 0);
		db.update(DatabaseHelper.TABLE_REPORT, initialValues, "本馆预订数=?", new String[]{isbn});
	}

	public int clearDataByTable(String table) {
		int delete = db.delete(table, null, null);
		Log.d("XX","清空table");
		return delete;
	}

	public void insertScanIsbn(String isbn) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(DatabaseHelper.KEY_ISBN, isbn);
		db.insert(DatabaseHelper.TABLE_SCAN_ISBN, null, initialValues);
	}

	public String getScanIsbnByMaxDate() {
		String sql = "select "+DatabaseHelper.KEY_SCAN_ISBN+" from "+DatabaseHelper.TABLE_SCAN_ISBN+" ORDER BY "+DatabaseHelper.KEY_SCAN_DATE+" desc limit 0,1";
		Cursor cursor = db.rawQuery(sql,null);
		String isbn = "";
		if(cursor != null && cursor.moveToFirst()){
			 isbn = cursor.getString(0);
		}
		return isbn;
	}

}