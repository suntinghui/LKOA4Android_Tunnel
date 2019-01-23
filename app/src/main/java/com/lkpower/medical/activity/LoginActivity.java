package com.lkpower.medical.activity;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import com.lkpower.medical.R;
import com.lkpower.medical.client.ApplicationEnvironment;
import com.lkpower.medical.client.Constants;
import com.lkpower.medical.client.LKAsyncHttpResponseHandler;
import com.lkpower.medical.client.LKHttpRequest;
import com.lkpower.medical.client.LKHttpRequestQueue;
import com.lkpower.medical.client.LKHttpRequestQueueDone;
import com.lkpower.medical.client.TransferRequestTag;
import com.lkpower.medical.util.PhoneUtil;
import com.lkpower.medical.view.LKAlertDialog;

@SuppressLint("NewApi")
public class LoginActivity extends BaseActivity implements TextWatcher {

	private LinearLayout rootLayout;
	private LinearLayout setIpLayout;

	private ImageView imeiImageView;

	private Button setIpButton;
	private Button loginButton;

	private EditText userNameET;
	private EditText pwdET;
	private TextView companyNameTextView;
	private EditText ipET;
	private ImageView helpImageView;

	private TextView remberTV;

	private ImageView remeberIV;
	private ImageView autoLoginIV;

	private Boolean isRemeberPwd;
	private Boolean isAutoLogin;

	private Spinner addressSpinner;
	private ArrayList<HashMap<String, String>> addressList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.login_activity);

		imeiImageView = (ImageView) this.findViewById(R.id.imeiImageView);
		imeiImageView.setOnClickListener(listener);

		userNameET = (EditText) this.findViewById(R.id.usernameET);
		userNameET.setText(ApplicationEnvironment.getInstance().getPreferences().getString(Constants.kLOGINNAME, ""));

		pwdET = (EditText) this.findViewById(R.id.pwdET);
		pwdET.setText(ApplicationEnvironment.getInstance().getPreferences().getString(Constants.kPASSWORD, ""));

		// 设置默认地址
		String tempHost = ApplicationEnvironment.getInstance().getPreferences().getString(Constants.kHOSTNAME, "");
		if (tempHost.equals("")) {
			Editor editor = ApplicationEnvironment.getInstance().getPreferences().edit();
			// editor.putString(Constants.kHOSTNAME, "http://oa.ccteb.com:8080/");
			editor.putString(Constants.kHOSTNAME, "http://oa.cctebec.com:8080/");
			editor.commit();
		}

		companyNameTextView = (TextView) this.findViewById(R.id.companyNameTextView);
		companyNameTextView.setText(ApplicationEnvironment.getInstance().getPreferences().getString(Constants.kCOMPANYNAME, "服务器地址"));
		
		ipET = (EditText) this.findViewById(R.id.ipET);
		ipET.setText(ApplicationEnvironment.getInstance().getPreferences().getString(Constants.kHOSTNAME, ""));
		ipET.addTextChangedListener(this);

		helpImageView = (ImageView) this.findViewById(R.id.helpImageView);
		helpImageView.setOnClickListener(listener);
		
		addressSpinner = (Spinner) this.findViewById(R.id.addressSpinner);

		rootLayout = (LinearLayout) this.findViewById(R.id.rootLayout);
		rootLayout.setOnClickListener(listener);

		setIpLayout = (LinearLayout) this.findViewById(R.id.settingIPLayout);

		setIpButton = (Button) this.findViewById(R.id.settingIPButton);
		setIpButton.setOnClickListener(listener);

		// 登录按钮
		loginButton = (Button) this.findViewById(R.id.loginButton);
		loginButton.setOnClickListener(listener);

		// 记住密码按钮
		remeberIV = (ImageView) this.findViewById(R.id.selectIV_left);
		remeberIV.setOnClickListener(listener);
		isRemeberPwd = ApplicationEnvironment.getInstance().getPreferences().getBoolean(Constants.kREMEBERPWD, false);
		if (isRemeberPwd) {
			remeberIV.setBackgroundResource(R.drawable.check_button_selected);
		} else {
			remeberIV.setBackgroundResource(R.drawable.check_button_normal);
		}

		// remberTV
		remberTV = (TextView) this.findViewById(R.id.remeber_pwdTV);

		// 自动登录按钮
		autoLoginIV = (ImageView) this.findViewById(R.id.selectIV_right);
		autoLoginIV.setOnClickListener(listener);
		isAutoLogin = ApplicationEnvironment.getInstance().getPreferences().getBoolean(Constants.kAUTOLOGIN, false);
		if (isAutoLogin) {
			autoLoginIV.setBackgroundResource(R.drawable.check_button_selected);
			this.disableRemberPwdButton();
		} else {
			autoLoginIV.setBackgroundResource(R.drawable.check_button_normal);
		}

		// 自动登录
		if (ApplicationEnvironment.getInstance().getPreferences().getBoolean(Constants.kAUTOLOGIN, false)) {
			doLogin();
		}

	}

	@Override
	protected void onNewIntent(Intent intent) {
		// 如果要统计Push引起的用户使用应用情况，请实现本方法，且加上这一个语句
		setIntent(intent);
	}

	private void disableRemberPwdButton() {
		remberTV.setTextColor(getResources().getColor(R.color.gray));
		remeberIV.setBackgroundResource(R.drawable.check_button_selected);
		remeberIV.setEnabled(false);
		isRemeberPwd = true;
	}

	private Boolean checkValue() {
		if (userNameET.length() == 0) {
			this.showToast("用户名不能为空！");
			return false;
		} else if (pwdET.length() == 0) {
			this.showToast("密码不能为空！");
			return false;
		} else if (ApplicationEnvironment.getInstance().getPreferences().getString(Constants.kHOSTNAME, "").length() == 0) {
			this.showToast("服务器地址不能为空！");
			return false;
		}

		return true;
	}

	@SuppressWarnings("deprecation")
	private void doLogin() {
		/*
		 * Intent intent = new Intent(LoginActivity.this, CatalogActivity.class); startActivity(intent);
		 */
		if (this.checkValue()) {
			Editor editor = ApplicationEnvironment.getInstance().getPreferences().edit();
			editor.putString(Constants.kLOGINNAME, userNameET.getText().toString());
			editor.putBoolean(Constants.kAUTOLOGIN, isAutoLogin);
			editor.putBoolean(Constants.kREMEBERPWD, isRemeberPwd);
			if (isRemeberPwd) {
				editor.putString(Constants.kPASSWORD, pwdET.getText().toString());
			} else {
				editor.putString(Constants.kPASSWORD, "");
			}
			editor.commit();

			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put(Constants.kWEBSERVICENAME, "WapService.asmx");
			map.put(Constants.kMETHODNAME, TransferRequestTag.LOGINSERVICE);

			HashMap<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("sUserName", URLEncoder.encode(userNameET.getText().toString()));
			paramMap.put("sUserPwd", pwdET.getText().toString());
			paramMap.put("sDeviceID", PhoneUtil.getIMEI());
			map.put(Constants.kPARAMNAME, paramMap);

			LKHttpRequest req1 = new LKHttpRequest(this, map, getLoginHandler());

			new LKHttpRequestQueue(this).addHttpRequest(req1).executeQueue("正在登录请稍候...", new LKHttpRequestQueueDone() {

				@Override
				public void onComplete() {
					super.onComplete();

				}

			});
		}

	}

	// 服务器返回数据后的处理
	private LKAsyncHttpResponseHandler getLoginHandler() {
		return new LKAsyncHttpResponseHandler() {

			@Override
			public void successAction(Object obj) {
				try {
					String respStr[] = ((String) obj).split(";");
					if (respStr[0].equals("0")) {
						// 登录成功
						Log.e("success:", obj.toString());

						Editor editor = ApplicationEnvironment.getInstance().getPreferences().edit();
						editor.putString(Constants.kUSERID, respStr[1]);
						editor.putString(Constants.kUSERNAME, respStr[2]);
						editor.commit();

						Intent intent = new Intent(LoginActivity.this, CatalogActivity.class);
						startActivity(intent);
					} else {
						LoginActivity.this.showDialog(BaseActivity.MODAL_DIALOG, getErrorMsg(respStr[0]));
					}
				} catch (Exception e) {
					e.printStackTrace();
					LoginActivity.this.showDialog(BaseActivity.MODAL_DIALOG, "服务器返回数据异常。");
				}

			}

		};
	}

	// 处理错误消息
	private String getErrorMsg(String errorCode) {
		if (null == errorCode) {
			return "服务器返回数据异常，请重新登录！";
		}

		if (errorCode.equals("1")) {
			return "用户名称不存在";
		} else if (errorCode.equals("2")) {
			return "密码错误，请重新登录";
		} else if (errorCode.equals("3")) {
			return "您还没有登录权限，请与管理员联系";
		} else if (errorCode.equals("4")) {
			return "设备号未绑定";
		} else if (errorCode.equals("5")) {
			return "设备号与所属人员身份不符合";
		} else if (errorCode.equals("6")) {
			return "设备处理禁用状态";
		}

		return errorCode;
	}

	private void requestAddressList() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(Constants.kWEBSERVICENAME, "WapService.asmx");
		map.put(Constants.kMETHODNAME, TransferRequestTag.ADDRESSLIST);

		HashMap<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("sFlag", "ANDROID");
		map.put(Constants.kPARAMNAME, paramMap);

		LKHttpRequest req1 = new LKHttpRequest(this, map, responseAddressListHander());

		new LKHttpRequestQueue(this).addHttpRequest(req1).executeQueue("正在查询请稍候...", new LKHttpRequestQueueDone() {

			@Override
			public void onComplete() {
				super.onComplete();

			}

		});
	}

	private LKAsyncHttpResponseHandler responseAddressListHander() {
		return new LKAsyncHttpResponseHandler() {

			@Override
			public void successAction(Object obj) {
				try {
					addressList = (ArrayList<HashMap<String, String>>) obj;
					
					chooseAddress();
					
				} catch (Exception e) {
					e.printStackTrace();
					LoginActivity.this.showDialog(BaseActivity.MODAL_DIALOG, "服务器返回数据异常。");
				}

			}

		};
	}

	// 各按纽的事件
	private OnClickListener listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {

			case R.id.imeiImageView:
				checkPermission(new CheckPermListener() {
					@Override
					public void superPermission() {
						showIMEI();
					}
				}, R.string.readPhoneState, Manifest.permission.READ_PHONE_STATE);

				break;

			case R.id.loginButton:
				checkPermission(new CheckPermListener() {
					@Override
					public void superPermission() {
						doLogin();
					}
				}, R.string.readPhoneState, Manifest.permission.READ_PHONE_STATE);

				break;

			case R.id.selectIV_left:
				isRemeberPwd = !isRemeberPwd;
				if (isRemeberPwd) {
					remeberIV.setBackgroundResource(R.drawable.check_button_selected);
				} else {
					remeberIV.setBackgroundResource(R.drawable.check_button_normal);
				}
				break;

			case R.id.selectIV_right:
				isAutoLogin = !isAutoLogin;
				if (isAutoLogin) {
					autoLoginIV.setBackgroundResource(R.drawable.check_button_selected);
					LoginActivity.this.disableRemberPwdButton();
				} else {
					autoLoginIV.setBackgroundResource(R.drawable.check_button_normal);
					remeberIV.setEnabled(true);
					remberTV.setTextColor(getResources().getColor(R.color.black));
				}
				break;

			case R.id.settingIPButton:
				if (setIpLayout.getVisibility() == View.GONE) {
					setIpLayout.setVisibility(View.VISIBLE);
				} else {
					setIpLayout.setVisibility(View.GONE);
				}
				break;

			case R.id.rootLayout:
				setIpLayout.setVisibility(View.GONE);
				break;

			case R.id.helpImageView:
				// LoginActivity.this.showDialog(BaseActivity.MODAL_DIALOG, "请勿随意修改服务器地址，否则会导致无法登录系统。\n服务器地址格式为 http://oa.ccteb.com:8080/");
				requestAddressList();
				break;
			}
		}
	};

	// 设置IP时，每次输入删除内容后都保存一次。
	@Override
	public void afterTextChanged(Editable editable) {
		Editor editor = ApplicationEnvironment.getInstance().getPreferences().edit();
		editor.putString(Constants.kHOSTNAME, editable.toString());
		editor.commit();
	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
	}

	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
	}

	private void showIMEI() {
		LKAlertDialog dialog = new LKAlertDialog(this);
		dialog.setTitle("提示");
		dialog.setMessage("您手机的串号是:" + PhoneUtil.getIMEI() + ",点击复制可以将该串号拷贝到您手机的剪贴板中，您可以通过微信或短信告知管理员，谢谢。");
		dialog.setCancelable(false);
		dialog.setPositiveButton("复制", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int arg1) {
				dialog.dismiss();

				// 复制串号
				ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
				clipboardManager.setPrimaryClip(ClipData.newPlainText("IMEI", PhoneUtil.getIMEI()));

				Toast.makeText(LoginActivity.this, "复制成功！", Toast.LENGTH_SHORT).show();

			}
		});
		dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		dialog.create();
		dialog.show();
	}

	// //////////////////

	// 选择地址
	private void chooseAddress() {
		final SpinnerAdapter adapter = new SpinnerAdapter(this);
		addressSpinner.setAdapter(adapter);
		addressSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				String ip = addressList.get(position).get("SERVICEURL");
				String company = addressList.get(position).get("NAME");
				
				ipET.setText(ip);
				companyNameTextView.setText(company);
				
				Editor editor = ApplicationEnvironment.getInstance().getPreferences().edit();
				editor.putString(Constants.kHOSTNAME, ip);
				editor.putString(Constants.kCOMPANYNAME, company);
				editor.commit();

				adapter.notifyDataSetChanged();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		addressSpinner.performClick();
	}

	private class ViewHolder {
		private TextView nameTextView;
		private TextView urlTextView;
	}

	public class SpinnerAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		private Context mContext;

		public SpinnerAdapter(Context pContext) {
			this.mContext = pContext;

			this.mInflater = LayoutInflater.from(mContext);
		}

		@Override
		public int getCount() {
			return addressList.size();
		}

		@Override
		public Object getItem(int position) {
			return addressList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;

			if (null == convertView) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.spinner_address_item, null);

				holder.nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
				holder.urlTextView = (TextView) convertView.findViewById(R.id.urlTextView);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.nameTextView.setText(addressList.get(position).get("NAME"));
			holder.urlTextView.setText(addressList.get(position).get("SERVICEURL"));

			return convertView;
		}


	}

}
