package com.lkpower.medical.activity;

import java.util.ArrayList;
import java.util.Stack;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.widget.Toast;

import com.lkpower.medical.client.ApplicationEnvironment;
import com.lkpower.medical.client.LKHttpRequestQueue;
import com.lkpower.medical.view.LKAlertDialog;
import com.lkpower.medical.view.LKProgressDialog;

import hei.permission.PermissionActivity;

public class BaseActivity extends PermissionActivity {
	
	private static Stack<BaseActivity> stack = new Stack<BaseActivity>();
	
	public static final int PROGRESS_DIALOG 	= 0; // 带滚动条的提示框 
	public static final int MODAL_DIALOG		= 1; // 带确定按纽的提示框，需要用户干预才能消失
	public static final int ALL_DIALOG			= 3; 
	
	// 要命的static
	private static LKProgressDialog progressDialog = null;
	private LKAlertDialog alertDialog = null;
	
	private String message = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		stack.push(this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		//stack.push(this);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK){
			this.setResult(Activity.RESULT_OK);
			this.finish();
		}
	}
	
	@Override
	public void startActivity(Intent intent) {
		super.startActivity(intent);
		// 然后会调用 startActivityForResult();
	}

	@Override
	public void startActivityForResult(Intent intent, int requestCode) {
		super.startActivityForResult(intent, requestCode);
	}

	@Override
	public void finish() {
		if (!stack.empty()){
			stack.pop();
		}
		
		super.finish();
	}
	
	public static BaseActivity getTopActivity(){
		try{
			return stack.peek();
			
		} catch(Exception e){
			e.printStackTrace();
			
			return null;
		}
		
	}
	
	public static ArrayList<BaseActivity> getAllActiveActivity() {
		if (null == stack || stack.isEmpty()){
			return null;
		}
		
		ArrayList<BaseActivity> list = new ArrayList<BaseActivity>();
		for (int i=0; i<stack.size(); i++){
			list.add(stack.get(i));
		}
		
		return list;
	}
	
	public static void popActivity(){
		try{
			stack.pop();
		} catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	public void showDialog(final int type, String message){
		this.message = message;
		
		this.runOnUiThread(new Runnable(){
			@Override
			public void run() {
				showDialog(type);
			}
		});
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id){
		case PROGRESS_DIALOG:
			this.showProgressDialog();
			break;
			
		case MODAL_DIALOG:
			this.showAlertDialog();
			break;
		}
		
		return super.onCreateDialog(id);
	}
	
	private void showProgressDialog(){
		try{
			// 这里应该关闭其它提示型的对话框
			this.hideDialog(ALL_DIALOG);
			
			this.createProgressDialog();
			
			progressDialog.setMessage(null==message?"":message);
			/***
			Activity activity = (Activity) ((ContextThemeWrapper)progressDialog.getContext()).getBaseContext();
			//android.view.WindowManager$BadTokenException: Unable to add window -- token android.os.BinderProxy@438e7108 is not valid; is your activity running?
			if (!activity.isFinishing()){
				progressDialog.create().show();
			} 
			***/
			progressDialog.create();
			progressDialog.show();
			
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void showAlertDialog(){
		try{
			// 这里应该关闭其它提示型的对话框
			this.hideDialog(ALL_DIALOG);
			
			this.createAlertDialog();
			
			alertDialog.setMessage(null == message?"":message);
			/*
			Activity act = (Activity) ((ContextThemeWrapper)alertDialog.getContext()).getBaseContext();
			// android.view.WindowManager$BadTokenException: Unable to add window -- token android.os.BinderProxy@438e7108 is not valid; is your activity running?
			if (!act.isFinishing()){
				alertDialog.create().show();
			} 
			**/
			alertDialog.create();
			alertDialog.show();
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void hideDialog(int type){
		switch(type){
		case PROGRESS_DIALOG:
			if (null != progressDialog && progressDialog.isShowing()){
				progressDialog.dismiss();
			}
			break;
			
		case MODAL_DIALOG:
			if (null != alertDialog && alertDialog.isShowing()){
				alertDialog.dismiss();
			}
			break;
			
		default:
			if (null != progressDialog && progressDialog.isShowing()){
				progressDialog.dismiss();
			}
			if (null != alertDialog && alertDialog.isShowing()){
				alertDialog.dismiss();
			}
			break;
		}
		
	}
	
	private void createProgressDialog(){
		progressDialog = new LKProgressDialog(this);
		
		progressDialog.setCancelable(false);
		progressDialog.setTitle("请稍候");
		
		progressDialog.setNegativeButton("取消",
		new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				
				if (!LKHttpRequestQueue.queueList.isEmpty()){
					LKHttpRequestQueue.queueList.get(LKHttpRequestQueue.queueList.size()-1).cancel();
				}
				
			}
		});
	}
	
	private void createAlertDialog(){
		alertDialog = new LKAlertDialog(this);
		
		alertDialog.setTitle("提示");
		alertDialog.setCancelable(false);
		alertDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
	}
	
	public void showToast(String message){
		Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
		//toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}
	
}
