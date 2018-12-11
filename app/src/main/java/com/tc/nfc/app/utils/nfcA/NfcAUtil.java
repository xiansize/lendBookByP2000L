package com.tc.nfc.app.utils.nfcA;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;

import com.tc.nfc.app.utils.StringToOther;
import com.tc.nfc.core.listener.ActionCallbackListener;

import java.io.File;
/**
 * @author tangjiarao
 * 该类是读取nfcA类型的工具类
 */
public class NfcAUtil {

	private static boolean mIsCreatingKeyMap;
	private static int count=-1;
	/*要读取的扇区
	 Read all.
	 mFirstSector = 0;
	 mLastSector = reader.getSectorCount()-1;
	 */
	private static final int mFirstSector =1;
	private static final int mLastSector =1;
	private static final Handler mHandler = new Handler();
	private static SparseArray<String[]> mRawDump;
	private static Context content;
	private static ActionCallbackListener callBackListener;

	/**
	 * Create a key map and save it to
	 * {@link NfcCommon#setKeyMap(android.util.SparseArray)}.
	 * For doing so it uses other methods (
	 * {@link #createKeyMap(MCReader, Context)},
	 * {@link #keyMapCreated(MCReader)}).
	 * If {@link Preference#SaveLastUsedKeyFiles} is active, this will also
	 * save the selected key files.
	 * @param view The View object that triggered the method
	 * (in this case the map keys to sectors button).
	 * @see #createKeyMap(MCReader, Context)
	 * @see #keyMapCreated(MCReader)
	 */
	public static void onCreateKeyMap(Context context,ActionCallbackListener<String> listener) {

		if (content==null)
			content =context;
		//刷新每次的listener
		callBackListener =listener;

		File mKeyDirPath=null;
		if (mKeyDirPath == null) {
			mKeyDirPath = new File(NfcCommon.PATH);
		}
		Log.d("JSON",""+ mKeyDirPath.listFiles());
		//只取一个读卡规则，要不然很慢
		File[] files = new File[]{mKeyDirPath.listFiles()[0]};

		if (files.length > 0) {
			// Create reader.
			MCReader reader = NfcCommon.checkForTagAndCreateReader(content);
			if (reader == null) {
				return;
			}
			// Set key files.
			if (!reader.setKeyFile(files, content)) {
				// Error.
				reader.close();
				return;
			}
			// Set map creation range.
			if (!reader.setMappingRange(
					mFirstSector, mLastSector)) {
				// Error.
//				Toast.makeText(content,
//						R.string.info_mapping_sector_out_of_range,
//						Toast.LENGTH_LONG).show();
				reader.close();
				return;
			}
			NfcCommon.setKeyMapRange(mFirstSector, mLastSector);

			mIsCreatingKeyMap = true;
			//创建中，请稍后
//			Toast.makeText(content, R.string.info_wait_key_map,
//					Toast.LENGTH_SHORT).show();
			// Read as much as possible with given key file.
			createKeyMap(reader);
		}

	}
	/**
	 * Triggered by {@link #onCreateKeyMap(View)} this
	 * method starts a worker thread that first creates a key map and then
	 * calls {@link #keyMapCreated(MCReader)}.
	 * It also updates the progress bar in the UI thread.
	 * @param reader A connected {@link MCReader}.
	 * @see #onCreateKeyMap(View)
	 * @see #keyMapCreated(MCReader)
	 */
	private static void createKeyMap(final MCReader reader) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// Build key map parts and update the progress bar.
				while (count < mLastSector) {
					count = reader.buildNextKeyMapPart();
					if (count == -1 || !mIsCreatingKeyMap) {
						// Error while building next key map part.
						break;
					}
				}

				mHandler.post(new Runnable() {
					@Override
					public void run() {

						reader.close();
						if (mIsCreatingKeyMap && count != -1) {
							keyMapCreated(reader);
						} else {
							// Error during key map creation.
							NfcCommon.setKeyMap(null);
							NfcCommon.setKeyMapRange(-1, -1);
							//标签丢失
//							Toast.makeText(content, R.string.info_key_map_error,
//									Toast.LENGTH_LONG).show();
							callBackListener.onFailure("标签丢失","标签丢失");
						}
						count=-1;
						mIsCreatingKeyMap = false;
					}
				});
			}
		}).start();
	}

	/**
	 * Triggered by {@link #createKeyMap(MCReader, Context)}, this method
	 * sets the result code to {@link Activity#RESULT_OK},
	 * saves the created key map to
	 * {@link NfcCommon#setKeyMap(android.util.SparseArray)}
	 * and finishes this Activity.
	 * @param reader A {@link MCReader}.
	 * @see #createKeyMap(MCReader, Context)
	 * @see #onCreateKeyMap(View)
	 */
	private static void keyMapCreated(MCReader reader) {
		// LOW: Return key map in intent.
		if (reader.getKeyMap().size() == 0) {
			NfcCommon.setKeyMap(null);
			// Error. No valid key found.
//			Toast.makeText(content, R.string.info_no_key_found,
//					Toast.LENGTH_LONG).show();
			callBackListener.onFailure("没有有效的标签", "没有有效的标签");
		} else {
			NfcCommon.setKeyMap(reader.getKeyMap());
			readTag();
		}
	}

	/**
	 * Triggered by {@link #onActivityResult(int, int, Intent)}
	 * this method starts a worker thread that first reads the tag and then
	 * calls {@link #createTagDump(SparseArray)}.
	 */
	private static void readTag() {

		final MCReader reader = NfcCommon.checkForTagAndCreateReader(content);
		if (reader == null) {
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				// Get key map from glob. variable.
				mRawDump = reader.readAsMuchAsPossible(
						NfcCommon.getKeyMap());

				reader.close();

				mHandler.post(new Runnable() {
					@Override
					public void run() {
						createTagDump(mRawDump);
					}
				});
			}
		}).start();
	}

	/**
	 * 这方法被我修改成只能读一个扇区，如果读多个扇区要修改
	 * Create a tag dump in a format the {@link DumpEditor}
	 * can read (format: headers (sectors) marked with "+", errors
	 * marked with "*"), and then start the dump editor with this dump.
	 * @param rawDump A tag dump like {@link MCReader#readAsMuchAsPossible()}
	 * returns.
	 * @see DumpEditor#EXTRA_DUMP
	 * @see DumpEditor
	 */
	private static void createTagDump(SparseArray<String[]> rawDump) {
		if (rawDump != null) {
			if (rawDump.size() != 0) {
				//这里只能读一个扇区
				for (int i = NfcCommon.getKeyMapRangeFrom();
					 i <= NfcCommon.getKeyMapRangeTo(); i++) {

					String[] val = rawDump.get(i);
					getBarcode(val[1]);
				}
			} else {
				// Error, keys from key map are not valid for reading.
//				Toast.makeText(content, R.string.info_none_key_valid_for_reading,
//						Toast.LENGTH_LONG).show();
			}
		} else {
//			Toast.makeText(content, R.string.info_tag_removed_while_reading,
//					Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * 获取NFCA类型条形码
	 * @param s
	 */
	private static void getBarcode(final String s){
		if (s.length()<2){
			callBackListener.onFailure("该扇区为空","该扇区为空");
			Log.d("ERROR","NfcAUtil ERROR:该扇区为空");
		}
		else{
			callBackListener.onSuccess(StringToOther.convertStringToDec(s));
		}
	}

}