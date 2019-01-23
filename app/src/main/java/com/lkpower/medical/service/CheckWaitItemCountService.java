package com.lkpower.medical.service;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import com.lkpower.medical.R;
import com.lkpower.medical.activity.LoginActivity;
import com.lkpower.medical.client.ApplicationEnvironment;
import com.lkpower.medical.client.Constants;
import com.lkpower.medical.client.LKAsyncHttpResponseHandler;
import com.lkpower.medical.client.LKHttpRequest;
import com.lkpower.medical.client.LKHttpRequestQueue;
import com.lkpower.medical.client.LKHttpRequestQueueDone;
import com.lkpower.medical.client.TransferRequestTag;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

public class CheckWaitItemCountService extends Service {

	// 默认初始时间
	private static final String DEFAULT_TRACK_TIME = "2000-01-01 00:00:01";

	private Timer timer = null;

	private int waitItemCount = 0;
	private int newWaitItemCount = 0;

	@Override
	public void onCreate() {
		Log.e("WaitItem Count", "服务创建...");
		super.onCreate();

	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.e("WaitItem Count", "服务启动...");

		try {
			if (null != timer) {
				timer.cancel();
				timer = null;
			}

			int timeInterval = ApplicationEnvironment.getPreferences(this).getInt(Constants.kNOTIFY_CHECK_INTERVAL, Constants.DEFAULT_NOTIFY_CHECK_INTERVAL);
			Log.e("Service Interval", "" + timeInterval);

			timer = new Timer();
			timer.schedule(new CheckWaitItemCountTask(), timeInterval * 60 * 1000, timeInterval * 60 * 1000);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return START_REDELIVER_INTENT;
		// return super.onStartCommand(intent, flags, startId);
	}

	// 销毁服务时要关闭定时器
	@Override
	public void onDestroy() {
		Log.e("WaitItem Count", "关闭服务，关闭定时器....");

		if (null != timer) {
			timer.cancel();
			timer = null;
		}

		super.onDestroy();
	}

	// 定时服务，取得待办数量及新增加的数量
	class CheckWaitItemCountTask extends TimerTask {

		@Override
		public void run() {
			SharedPreferences prefernce = CheckWaitItemCountService.this.getSharedPreferences(ApplicationEnvironment.LKOA4ANDROID, Context.MODE_PRIVATE);
			String userId = prefernce.getString(Constants.kUSERID, null);

			if (null != userId && !userId.trim().equals("")) {
				Log.e("WaitItem Count", "检测是否有新的待办事宜。。。");

				LKHttpRequestQueue queue = new LKHttpRequestQueue(CheckWaitItemCountService.this);
				queue.addHttpRequest(this.getWaitItemCountRequest(userId));
				queue.addHttpRequest(this.getNewWaitItemCountRequest(userId));
				queue.executeQueue(null, new LKHttpRequestQueueDone() {
					@Override
					public void onComplete() {
						super.onComplete();
						if (newWaitItemCount > 0) {
							showNotification();
						}

					}
				});
			}
		}

		private LKHttpRequest getWaitItemCountRequest(String userId) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put(Constants.kWEBSERVICENAME, "WapService.asmx");
			map.put(Constants.kMETHODNAME, TransferRequestTag.GET_WAITITEMSCOUNT);

			HashMap<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("sUserID", userId);
			map.put(Constants.kPARAMNAME, paramMap);

			LKHttpRequest request = new LKHttpRequest(CheckWaitItemCountService.this, map, new LKAsyncHttpResponseHandler() {
				@Override
				public void successAction(Object obj) {
					String[] temp = ((String) obj).split("\\|");
					try {
						waitItemCount = Integer.parseInt(temp[1]);
					} catch (Exception e) {
						waitItemCount = 0;
					}
				}
			});

			return request;
		}

		private LKHttpRequest getNewWaitItemCountRequest(String userId) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put(Constants.kWEBSERVICENAME, "WapService.asmx");
			map.put(Constants.kMETHODNAME, TransferRequestTag.GET_NEWWAITITEMSCOUNT);

			HashMap<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("sUserID", userId);
			paramMap.put("maxTrackTime", ApplicationEnvironment.getPreferences(CheckWaitItemCountService.this).getString(Constants.kMAXTRACKTIME, DEFAULT_TRACK_TIME).trim());
			map.put(Constants.kPARAMNAME, paramMap);

			LKHttpRequest request = new LKHttpRequest(CheckWaitItemCountService.this, map, new LKAsyncHttpResponseHandler() {
				@Override
				public void successAction(Object obj) {
					try {
						@SuppressWarnings("unchecked")
						HashMap<String, String> map = (HashMap<String, String>) obj;

						newWaitItemCount = Integer.parseInt(map.get("count"));

						if (newWaitItemCount != 0) {
							Editor editor = ApplicationEnvironment.getPreferences(CheckWaitItemCountService.this).edit();
							editor.putString(Constants.kMAXTRACKTIME, map.get("time"));
							editor.commit();
						}

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			return request;
		}

		// 发送通知
		private void showNotification() {
			NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

			Notification notification = new Notification(R.drawable.ic_launcher, "LK移动办公", System.currentTimeMillis());
			notification.defaults = Notification.DEFAULT_ALL;
			notification.flags = Notification.FLAG_AUTO_CANCEL;

			String message = "您有" + newWaitItemCount + "条新增待办事务，共有" + waitItemCount + "条待办事务";
			RemoteViews contentView = new RemoteViews(CheckWaitItemCountService.this.getPackageName(), R.layout.custom_notification_layout);
			contentView.setTextViewText(R.id.textView, message);
			notification.contentView = contentView;

			// 当点击通知时，启动该contentIntent关联的activity
			Intent intent = new Intent(CheckWaitItemCountService.this, LoginActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			PendingIntent contentIntent = PendingIntent.getActivity(CheckWaitItemCountService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			notification.contentIntent = contentIntent;

			manager.notify(0, notification);
		}

	}

}
