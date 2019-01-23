package com.lkpower.medical.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.lkpower.medical.R;
import com.lkpower.medical.client.ApplicationEnvironment;
import com.lkpower.medical.client.Constants;
import com.lkpower.medical.view.SwitchButton;

public class SettingActivity extends BaseActivity implements OnClickListener, OnCheckedChangeListener {
	
	private Button backButton 					= null;
	private Button saveButton					= null;
	
	private SwitchButton notifySwitchButton 	= null;
	private SwitchButton synchSwitchButton 		= null;
	private SwitchButton activeSwitchButton 	= null;
	
	private LinearLayout notifyLayout 			= null;
	private LinearLayout synchLayout 			= null;
	
	private EditText notifyTimeIntervalEdit		= null;
	private EditText synchTimeIntervalEdit		= null;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_activity);
		
		SharedPreferences preferences = ApplicationEnvironment.getInstance().getPreferences();
		
		backButton = (Button) this.findViewById(R.id.backButton);
		backButton.setOnClickListener(this);
		
		saveButton = (Button) this.findViewById(R.id.saveButton);
		saveButton.setOnClickListener(this);
		
		boolean notifyFlag = preferences.getBoolean(Constants.kOPEN_NOTIFY_CHECK, Constants.DEFAULT_NOTIFY_CHECK);
		notifySwitchButton = (SwitchButton) this.findViewById(R.id.notifySwitch);
		notifySwitchButton.setChecked(notifyFlag);
		notifySwitchButton.setOnCheckedChangeListener(this);
		
		boolean synchFlag = preferences.getBoolean(Constants.kOPEN_SYNC_CHECK, Constants.DEFAULT_SYNC_CHECK);
		synchSwitchButton = (SwitchButton) this.findViewById(R.id.synchSwitch);
		synchSwitchButton.setChecked(synchFlag);
		synchSwitchButton.setOnCheckedChangeListener(this);
		
		boolean activeFlag = preferences.getBoolean(Constants.kOPEN_ACTIVE_CHECK, Constants.DEFAULT_ACTIVE_CHECK);
		activeSwitchButton = (SwitchButton) this.findViewById(R.id.activeSwitch);
		activeSwitchButton.setChecked(activeFlag);
		activeSwitchButton.setOnCheckedChangeListener(this);
		
		notifyLayout = (LinearLayout) this.findViewById(R.id.notifyLayout);
		notifyLayout.setVisibility(notifyFlag?View.VISIBLE:View.GONE);
		
		synchLayout = (LinearLayout) this.findViewById(R.id.synchLayout);
		synchLayout.setVisibility(synchFlag?View.VISIBLE:View.GONE);
		
		notifyTimeIntervalEdit = (EditText) this.findViewById(R.id.notifyTimeEdit);
		notifyTimeIntervalEdit.setText(preferences.getInt(Constants.kNOTIFY_CHECK_INTERVAL, Constants.DEFAULT_NOTIFY_CHECK_INTERVAL)+"");
		
		synchTimeIntervalEdit = (EditText) this.findViewById(R.id.synchTimeEdit);
		synchTimeIntervalEdit.setText(preferences.getInt(Constants.kSYNC_CHECK_INTERVAL, Constants.DEFAULT_SYNC_CHECK_INTERVAL)+"");
	}

	@Override
	public void onClick(View view) {
		switch(view.getId()){
		case R.id.backButton:
			this.backAction();
			
			break;
			
		case R.id.saveButton:
			this.saveAction();
			
			break;
		}
	}
	
	// 检测输入值是否合法正确
	private boolean checkValue(){
		if (notifySwitchButton.isChecked()){
			String notifyText = notifyTimeIntervalEdit.getText().toString();
			if (notifyText.trim().equals("") || notifyText.trim().equals("0")){
				this.showToast("通知推送时间间隔设置不正确");
				return false;
			}
			
		} 
		
		if (synchSwitchButton.isChecked()){
			String synchText = synchTimeIntervalEdit.getText().toString();
			if (synchText.trim().equals("") || synchText.trim().equals("0")){
				this.showToast("主界面自动同步时间间隔设置不正确");
				return false;
			}
		}
		
		return true;
	}
	
	// 保存设置
	private void saveAction(){
		if(!checkValue()) return;
				
		Editor editor = ApplicationEnvironment.getInstance().getPreferences().edit();
		
		// 通知设置
		editor.putBoolean(Constants.kOPEN_NOTIFY_CHECK, notifySwitchButton.isChecked());
		if (notifySwitchButton.isChecked()){
			editor.putInt(Constants.kNOTIFY_CHECK_INTERVAL, Integer.parseInt(notifyTimeIntervalEdit.getText().toString()));
		}
		
		// 同步设置
		editor.putBoolean(Constants.kOPEN_SYNC_CHECK, synchSwitchButton.isChecked());
		if (synchSwitchButton.isChecked()){
			editor.putInt(Constants.kSYNC_CHECK_INTERVAL, Integer.parseInt(synchTimeIntervalEdit.getText().toString()));
		}
		
		// 激活设置
		editor.putBoolean(Constants.kOPEN_ACTIVE_CHECK, activeSwitchButton.isChecked());
		
		editor.commit();
		
		this.showToast("保存成功，设置已生效");
		
		this.backAction();
	}
	
	// 返回事件
	private void backAction(){
		Intent intent = new Intent(this, CatalogActivity.class);
		this.startActivity(intent);
	}
	
	// 点击手机的返回键的事件
	@Override
	public void onBackPressed(){
		super.onBackPressed();
		
		this.backAction();
	}

	@Override
	public void onCheckedChanged(CompoundButton button, boolean checked) {
		switch(button.getId()){
		case R.id.notifySwitch:
			notifyLayout.setVisibility(checked?View.VISIBLE:View.GONE);
			break;
			
		case R.id.synchSwitch:
			synchLayout.setVisibility(checked?View.VISIBLE:View.GONE);
			break;
			
		case R.id.activeSwitch:
			
			break;
		}
	}

}
