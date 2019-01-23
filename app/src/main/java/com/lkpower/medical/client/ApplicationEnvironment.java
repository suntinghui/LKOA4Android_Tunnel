package com.lkpower.medical.client;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.lkpower.medical.activity.BaseActivity;

public class ApplicationEnvironment {

	public static final String LKOA4ANDROID = "LKOA4ANDROIDMEDICAL";

	private static ApplicationEnvironment appEnv = null;
	private Application application = null;
	private SharedPreferences preferences = null;

	public static ApplicationEnvironment getInstance() {
		if (null == appEnv) {
			appEnv = new ApplicationEnvironment();

		}

		return appEnv;
	}

	public Application getApplication() {
		if (null == this.application) {
			this.application = BaseActivity.getTopActivity().getApplication();
		}

		return this.application;
	}

	// 取得屏幕大小
	public DisplayMetrics getPixels() {
		DisplayMetrics dm = new DisplayMetrics();
		((WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);
		return dm;
	}

	public SharedPreferences getPreferences() {
		if (null == preferences)
			preferences = this.getApplication().getSharedPreferences(ApplicationEnvironment.LKOA4ANDROID, Context.MODE_PRIVATE);

		return preferences;
	}

	public static SharedPreferences getPreferences(Context context) {
		return context.getSharedPreferences(ApplicationEnvironment.LKOA4ANDROID, Context.MODE_PRIVATE);
	}

	// 检测网络是否链接正常
	public boolean checkNetworkAvailable(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (manager == null)
			return false;

		NetworkInfo netinfo = manager.getActiveNetworkInfo();
		if (netinfo == null) {
			return false;
		}
		if (netinfo.isConnected()) {
			return true;
		}
		return false;
	}

}
