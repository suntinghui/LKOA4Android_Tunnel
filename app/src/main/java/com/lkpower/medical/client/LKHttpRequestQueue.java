package com.lkpower.medical.client;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;

import com.lkpower.medical.activity.BaseActivity;

public class LKHttpRequestQueue {
	
	private ArrayList<LKHttpRequest> requestList = null;
	
	private int completedTag = 0;
	private int finishedTag = 0;
	private LKHttpRequestQueueDone queueDone;
	
	public static ArrayList<LKHttpRequestQueue> queueList = new ArrayList<LKHttpRequestQueue>();
	
	private Context context;
	
	public LKHttpRequestQueue(Context context){
		this.context = context;
		
		requestList = new ArrayList<LKHttpRequest>();
		
		queueList.add(this);
	}
	
	public LKHttpRequestQueue addHttpRequest(LKHttpRequest... httpRequests){
		for (int i=0; i<httpRequests.length; i++){
			LKHttpRequest request = httpRequests[i];
			request.setTag((int)Math.pow(2, requestList.size()));
			request.setRequestQueue(this);
			requestList.add(request);
		}
		
		return this;
	}
	
	public void executeQueue(String prompt, LKHttpRequestQueueDone queueDone){
		if (!ApplicationEnvironment.getInstance().checkNetworkAvailable(this.context)){
			
			Log.e("LKOA", "网络连接不可用，请稍候重试");
			
			try{
				BaseActivity.getTopActivity().showToast("网络连接不可用，请稍候重试");
			}catch(Exception e){
				e.printStackTrace();
			}
			
			return;
		}
		
		try{
			if (null != prompt) {
				BaseActivity.getTopActivity().showDialog(BaseActivity.PROGRESS_DIALOG, prompt);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		this.queueDone = queueDone;
		this.queueDone.setRequestQueue(this);
		
		this.completedTag = 0;
		this.finishedTag = 0;
		
		for (LKHttpRequest request : requestList) {
			request.post();
		}
	}
	
	public void updateCompletedTag(int tag){
		synchronized (this) {
			this.completedTag += tag;
			
			if (completedTag == (int)Math.pow(2, this.requestList.size()) - 1) {
				// 在updataFinishedTag清空
				//requestList.clear();
				this.queueDone.onComplete();
			}
		}
	}
	
	
	public void updataFinishedTag(int tag){
		synchronized (this) {
			this.finishedTag += tag;
			
			if (this.finishedTag == (int)Math.pow(2, this.requestList.size()) - 1) {
				requestList.clear();
				this.queueDone.onFinish();
			}
		}
	}
	
	public void cancel(){
		for (LKHttpRequest request : requestList){
			request.getClient().cancelRequests(this.context, true);
		}
		
		requestList.clear();
	}
	
}
