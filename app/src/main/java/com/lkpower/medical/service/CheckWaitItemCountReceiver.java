package com.lkpower.medical.service;

import com.lkpower.medical.client.ApplicationEnvironment;
import com.lkpower.medical.client.Constants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class CheckWaitItemCountReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent i) {
		Log.e("Refresh", "开机检测是否要打开服务...");
		
		boolean open = ApplicationEnvironment.getPreferences(context).getBoolean(Constants.kOPEN_NOTIFY_CHECK, Constants.DEFAULT_NOTIFY_CHECK);
		
		Intent intent = new Intent(context, CheckWaitItemCountService.class);
		if (open){
			Log.e("Refresh", "开机启动服务...");
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startService(intent);
		} else {
			Log.e("Refresh", "开机关闭服务...");
			context.stopService(intent);
		}

	}

}
