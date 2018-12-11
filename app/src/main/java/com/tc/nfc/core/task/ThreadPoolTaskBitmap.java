package com.tc.nfc.core.task;

import android.util.Log;

import com.tc.nfc.api.utils.NetworkHelp;
import com.tc.nfc.util.Constant;

import java.util.Map;

/**
 * 图片加载的任务单元
 * @author carrey
 *
 */
public class ThreadPoolTaskBitmap extends ThreadPoolTask {

	private static final String TAG = "ThreadPoolTaskBitmap";

	private CallBack callBack;

	private Map<String, String> maps;

	public ThreadPoolTaskBitmap(String url, CallBack callBack,Map<String, String> maps) {
		super(url);
		this.callBack = callBack;
		this.maps =maps;
	}

	@Override
	public void run() {

		String result = NetworkHelp.sendDataByPost2(maps, "utf-8", getURL());
		Log.i(TAG, "loaded: " + url);

		if (callBack != null&&result!=null) {
			callBack.onReady(result);

		}

	}

	public interface CallBack {
		public void onReady(String result);
	}
}
