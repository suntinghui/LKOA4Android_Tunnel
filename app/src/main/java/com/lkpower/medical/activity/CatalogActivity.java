package com.lkpower.medical.activity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Xml;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.webkit.WebSettings;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.lkpower.medical.R;
import com.lkpower.medical.client.ApplicationEnvironment;
import com.lkpower.medical.client.Constants;
import com.lkpower.medical.client.LKAsyncHttpResponseHandler;
import com.lkpower.medical.client.LKHttpRequest;
import com.lkpower.medical.client.LKHttpRequestQueue;
import com.lkpower.medical.client.LKHttpRequestQueueDone;
import com.lkpower.medical.client.TransferRequestTag;
import com.lkpower.medical.model.CatalogModel;
import com.lkpower.medical.service.CheckWaitItemCountService;
import com.lkpower.medical.view.LKAlertDialog;

@SuppressLint({ "CommitPrefEdits", "NewApi" })
public class CatalogActivity extends BaseActivity implements OnClickListener {
	
	private ArrayList<CatalogModel> allCatalogList = new ArrayList<CatalogModel>();
	private ArrayList<CatalogModel> currentCatalogList = new ArrayList<CatalogModel>();
	
	private GridView gridView = null;
	private CatalogAdapter adapter = null;
	
	private Button backButton = null;
	private Button menuButton = null;
	private TextView nameView = null;
	private TextView titleView = null;

	private long exitTimeMillis = 0;
	private int currentParentId = 0;
	
	private boolean firstStart = true;
	
	private boolean syncFlag = false; // 同步刷新标志
	
	private Timer timer = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.catalog_activity);
		
		Log.e("Refresh", "创建CatalogActivity...");
		
		// 首先从XML中取得功能菜单
		this.getCatalogFromXML();
		
		// 下面是初始化控件
		gridView = (GridView) this.findViewById(R.id.gridveiw);
		gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		gridView.setOnItemClickListener(catalogClickListener);
		adapter = new CatalogAdapter(this);
		gridView.setAdapter(adapter);
		
		backButton = (Button) this.findViewById(R.id.backButton);
		backButton.setOnClickListener(this);
		backButton.setVisibility(View.GONE);

		menuButton = (Button) this.findViewById(R.id.menuButton);
		menuButton.setOnClickListener(this);
		menuButton.setVisibility(View.VISIBLE);
		
		nameView = (TextView) this.findViewById(R.id.nameText);
		nameView.setText(ApplicationEnvironment.getInstance().getPreferences().getString(Constants.kUSERNAME, "")+", 您好！");
		
		titleView = (TextView) this.findViewById(R.id.titleText);
		titleView.setText("OA办公系统");
		
		// 重置数量
		this.resetBadgeNum();
		// 进入先刷新数量
		this.loadBadgeNum();
		
		// 开启检查新增事务通知的服务
		this.startCheckWaititemCountService();
		// 开启刷新数量的定时器
		this.startSyncTask();
	}
	
	@Override
	public void onResume(){
		super.onResume();
		
		if (!firstStart){
			// 激活设置，如果已激活，则每次进入界面都会刷新数量
			boolean open = ApplicationEnvironment.getInstance().getPreferences().getBoolean(Constants.kOPEN_ACTIVE_CHECK, Constants.DEFAULT_ACTIVE_CHECK);
			if(open){
				Log.e("Refresh", "激活设置打开，刷新数量...");
				this.loadBadgeNum();
			}
		}
		
		firstStart = false;
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.e("Refresh", "销毁CatalogActivity...");
		
		if (null != timer){
			timer.cancel();
		}
	}

	// 检查是否开启了在后台轮询数量的服务，如果开启则打开，如果没有开启则关闭。注：可以重复打开和关闭，Service不受影响。
	private void startCheckWaititemCountService(){
		// 判断系统设置是否打开了通知推送功能
		boolean open = ApplicationEnvironment.getInstance().getPreferences().getBoolean(Constants.kOPEN_NOTIFY_CHECK, Constants.DEFAULT_NOTIFY_CHECK);
		
		Intent intent = new Intent(this, CheckWaitItemCountService.class);
		if (open){
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			this.startService(intent);
		} else {
			Log.e("Refresh", "关闭检测服务...");
			this.stopService(intent);
		}
	}
	
	// 检查是否开启了同步服务，如果开启了则打开。
	private void startSyncTask(){
		boolean open = ApplicationEnvironment.getInstance().getPreferences().getBoolean(Constants.kOPEN_SYNC_CHECK, Constants.DEFAULT_SYNC_CHECK);
		
		if (open){
			int timerInterval = ApplicationEnvironment.getInstance().getPreferences().getInt(Constants.kSYNC_CHECK_INTERVAL, Constants.DEFAULT_SYNC_CHECK_INTERVAL);
			
			timer = new Timer();
			timer.schedule(new TimerTask(){
				@Override
				public void run() {
					loadBadgeNum();
				}
				
			}, timerInterval*60*1000, timerInterval*60*1000);
			
		} else {
			if (null != timer){
				timer.cancel();
			}
		}
	}
	
	// 重置所有的数量
	private void resetBadgeNum(){
		Editor editor = ApplicationEnvironment.getInstance().getPreferences().edit();
		editor.putString(Constants.kWAITITEMSCOUNT, "0");
		editor.putString(Constants.kWAITFILESCOUNT, "0");
		editor.putString(Constants.kNEWSTOTALLISTCOUNT, "0");
		editor.putString(Constants.kNEWSLISTCOUNT, "0");
		editor.putString(Constants.kNEWSNOTICECOUNT, "0");
		editor.putString(Constants.kNOTICECOUNT, "0");
		editor.putString(Constants.kGetCirculatedCount, "0");
		editor.putString(Constants.kGetMailNewCount, "0");
		editor.putString(Constants.kGetPortalNoticeNewCount, "0");
		editor.commit();
		
	}
	
	// 异步联网取得各个数量
	private void loadBadgeNum(){
		LKHttpRequest req1 = this.getLKHttpRequest(TransferRequestTag.GET_WAITITEMSCOUNT);
		LKHttpRequest req2 = this.getLKHttpRequest(TransferRequestTag.GET_NEWSTOTALLISTCOUNT);
		LKHttpRequest req3 = this.getLKHttpRequest(TransferRequestTag.GET_WAITFILESCOUNT);
		LKHttpRequest req4 = this.getLKHttpRequest(TransferRequestTag.GET_NEWSLISTCOUNT);
		LKHttpRequest req5 = this.getLKHttpRequest(TransferRequestTag.GET_NEWSNOTICECOUNT);
		LKHttpRequest req7 = this.getLKHttpRequest(TransferRequestTag.GET_NOTICECOUNT);
		LKHttpRequest req8 = this.getLKHttpRequest(TransferRequestTag.GetCirculatedCount);
		LKHttpRequest req9 = this.getLKHttpRequest(TransferRequestTag.GetMailNewCount);
		LKHttpRequest req10 = this.getLKHttpRequest(TransferRequestTag.GetPortalNoticeNewCount);
				
		new LKHttpRequestQueue(this).addHttpRequest(req1, req3, req4, req8, req9, req10)
		.executeQueue(null, new LKHttpRequestQueueDone() {
			@Override
			public void onComplete() {
				super.onComplete();
				
				if (syncFlag){
					Toast.makeText(CatalogActivity.this, "同步成功", Toast.LENGTH_SHORT).show();
				}
				syncFlag = false;
				
				adapter.notifyDataSetChanged();
				
			}
		});
	 }
	 
	private LKHttpRequest getLKHttpRequest(int requestTag){
		SharedPreferences prefernce = (SharedPreferences) ApplicationEnvironment.getInstance().getPreferences();
		 
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(Constants.kWEBSERVICENAME, "WapService.asmx");
		map.put(Constants.kMETHODNAME, requestTag);

		HashMap<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("sUserID", prefernce.getString(Constants.kUSERID, ""));
		map.put(Constants.kPARAMNAME, paramMap);

		return new LKHttpRequest(this, map, getBadgeHandler());
	}
	
	// 从服务器取得数量成功后将其保存在Preferences中
	private LKAsyncHttpResponseHandler getBadgeHandler(){
		 return new LKAsyncHttpResponseHandler(){
			 
			@Override
			public void successAction(Object obj) {
				String[] temp = ((String)obj).split("\\|");
				Editor editor = ApplicationEnvironment.getInstance().getPreferences().edit();
				if (temp.length == 1){
					editor.putString(temp[0], "0");
				} else {
					editor.putString(temp[0], temp[1]);
				}
				editor.commit();
				
				adapter.notifyDataSetChanged();
			}
			
			@Override
			public void failureAction(Throwable error, String content){
				if (syncFlag){
					Toast.makeText(CatalogActivity.this, "同步失败", Toast.LENGTH_SHORT).show();
				}
				syncFlag = false;
			}
		 };
	}

	// 点击catalog事件
	private OnItemClickListener catalogClickListener = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> arg0, View view, int arg2,long arg3) {
			//view.startAnimation(AnimationUtils.loadAnimation(CatalogActivity.this, R.anim.catalogbutton));
			
			CatalogModel catalog = currentCatalogList.get(arg2);
			
			if (catalog.isTransferCatalog()){
				SharedPreferences prefernce = (SharedPreferences) ApplicationEnvironment.getInstance().getPreferences();
				
				Intent intent = new Intent(CatalogActivity.this, ShowWebViewActivity.class);
				intent.putExtra("TITLE", catalog.getTitle());
				intent.putExtra("BACKTITLE", titleView.getText());
				intent.putExtra("URL", prefernce.getString(Constants.kHOSTNAME, "") + catalog.getAction().replace("${USERID}", prefernce.getString(Constants.kUSERID, "")));
				startActivity(intent);
				
			} else {
				currentCatalogList.clear();
				
				for (CatalogModel model : allCatalogList){
					if (model.getParentId() == catalog.getCatalogId()){
						currentCatalogList.add(model);
					}
				}
				
				titleView.setText(catalog.getTitle());
				backButton.setVisibility(View.VISIBLE);
				menuButton.setVisibility(View.GONE);
				
				currentParentId = catalog.getCatalogId();
				
				adapter.notifyDataSetChanged();
			}
			
		}

	};
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.backButton:
			currentCatalogList.clear();
			
			currentParentId = 0;
			
			for (CatalogModel model : allCatalogList){
				if (model.getParentId() == currentParentId){
					currentCatalogList.add(model);
				}
			}
			
			titleView.setText("OA办公系统");
			
			menuButton.setVisibility(View.VISIBLE);
			backButton.setVisibility(View.GONE);
			
			adapter.notifyDataSetChanged();
			break;
			
		case R.id.menuButton:
			showPopupWindow();
			break;
		}
		
	}
	
	// 功能菜单界面左上解的菜单处理
	public void showPopupWindow() {
		LinearLayout layout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.menu_popup, null);
		ListView listView = (ListView) layout.findViewById(R.id.lv_dialog);
		
		SimpleAdapter adapter = new SimpleAdapter(this, getPopupMenuData(),R.layout.popup_listview_item,
				new String[]{"img","title"},
				new int[]{R.id.iv_img,R.id.tv_title});
		listView.setAdapter(adapter);

		final PopupWindow popupWindow = new PopupWindow(this);
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		popupWindow.setWidth(getWindowManager().getDefaultDisplay().getWidth() / 2);
		popupWindow.setHeight(getPopupHeight());
		popupWindow.setOutsideTouchable(true);
		popupWindow.setFocusable(true);
		popupWindow.setContentView(layout);
		popupWindow.showAsDropDown(menuButton);
		
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				popupWindow.dismiss();
				
				if (arg2 == 0){
					logout(); // 注销登录
				} else if (arg2 == 1){
					syncBadge(); // 同步刷新一次数量
				} else if (arg2 == 2){
					gotoSettingActivity(); // 进入设置界面
				}
			}
		});
	}
	
	// 根据不同手机分辨率的大小设置popup的高度
	private int getPopupHeight(){
		int screenDensity = getResources().getDisplayMetrics().densityDpi;
		switch(screenDensity){
		case DisplayMetrics.DENSITY_HIGH:
			return 300;
			
		case 320: // DisplayMetrics.DENSITY_XHIGH   Added in API level 9
			return 400;
			
		case 480:
			return 500; // DENSITY_XXHIGH  Added in API level 16
			
		case DisplayMetrics.DENSITY_DEFAULT:
			return 300;
				
		}
		
		return 300;
		
	}
	
	// 设置popup的菜单
	private ArrayList<HashMap<String, Object>> getPopupMenuData(){
		ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
		
		HashMap<String, Object> map1 = new HashMap<String, Object>();
		map1.put("img", android.R.drawable.ic_lock_power_off);
		map1.put("title", "注销");
		list.add(map1);
		
		HashMap<String, Object> map2 = new HashMap<String, Object>();
		map2.put("img", android.R.drawable.ic_menu_rotate);
		map2.put("title", "同步");
		list.add(map2);
		
		HashMap<String, Object> map3 = new HashMap<String, Object>();
		map3.put("img", android.R.drawable.ic_menu_manage);
		map3.put("title", "设置");
		list.add(map3);
		
		return list;
	}
	
	// 注销
	private void logout(){
		LKAlertDialog dialog = new LKAlertDialog(this);
		dialog.setTitle("提示");
		dialog.setMessage("您确定要注销登录吗？");
		dialog.setCancelable(false);
		dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				arg0.dismiss();
				
				for (Activity mActivity : CatalogActivity.getAllActiveActivity()){
					if (!(mActivity instanceof LoginActivity)){
						mActivity.finish();
					}
					
				}
				
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
	
	// 同步
	private void syncBadge(){
		syncFlag = true;
		
		Toast.makeText(this, "开始同步待办事务...", Toast.LENGTH_SHORT).show();
		
		loadBadgeNum();
	}
	
	// 进入设置界面
	private void gotoSettingActivity(){
		Intent intent = new Intent(CatalogActivity.this, SettingActivity.class);
		CatalogActivity.this.startActivity(intent);
		
		// 进入设置界面后销毁主菜单界面，返回后再重新创建，方便更改设置后的处理。
		this.finish();
	}
	
	// 程序退出
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
			if (currentParentId != 0){
				currentCatalogList.clear();
				currentParentId = 0;
				
				for (CatalogModel model : allCatalogList){
					if (model.getParentId() == currentParentId){
						currentCatalogList.add(model);
					}
				}
				
				titleView.setText("OA办公系统");
				
				menuButton.setVisibility(View.VISIBLE);
				backButton.setVisibility(View.GONE);
				
				adapter.notifyDataSetChanged();
				
			} else {
				if ((System.currentTimeMillis() - exitTimeMillis) > 2000) {
					Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
					exitTimeMillis = System.currentTimeMillis();
				} else {
					ArrayList<BaseActivity> list = BaseActivity.getAllActiveActivity();
					for (BaseActivity activity : list) {
						activity.finish();
					}

					System.exit(0);
				}
			}
			
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	// 解析XML得到菜单
	private void getCatalogFromXML(){
		CatalogModel catalog = null;
		
		try{
	        InputStream inputStream = this.getResources().openRawResource(R.raw.catalog); 
	        XmlPullParser parser = Xml.newPullParser();  
	        parser.setInput(inputStream, "UTF-8");  
	          
	        int event = parser.getEventType();  
	        while(event!=XmlPullParser.END_DOCUMENT){  
	            switch(event){  
	            case XmlPullParser.START_TAG:  
	                if("catalog".equals(parser.getName())){
	                    catalog = new CatalogModel();  
	                }  
	                if(catalog!=null){
	                    if("catalogId".equals(parser.getName())){  
	                        catalog.setCatalogId(Integer.parseInt(parser.nextText()));  
	                    }else if("title".equals(parser.getName())){  
	                        catalog.setTitle(parser.nextText());  
	                    }else if("parentId".equals(parser.getName())){  
	                        catalog.setParentId(Integer.parseInt(parser.nextText()));  
	                    }else if("transferCatalog".equals(parser.getName())){
	                    	catalog.setTransferCatalog(Boolean.parseBoolean(parser.nextText()));
	                    }else if("action".equals(parser.getName())){  
	                        catalog.setAction(parser.nextText());
	                    }else if("showBadge".equals(parser.getName())){
	                    	catalog.setShowBadge(parser.nextText());
	                    }else if("iconId".equals(parser.getName())){  
	                        catalog.setIconId((Integer.parseInt(parser.nextText())));  
	                    }  
	                }  
	                break;
	                
	            case XmlPullParser.END_TAG:  
	                if("catalog".equals(parser.getName())){
	                	allCatalogList.add(catalog);
	                	
	                	if (catalog.getParentId() == currentParentId){
	                		currentCatalogList.add(catalog);
	                	}
	                }  
	                break;  
	            }  
	            event = parser.next();  
	        }
	        
		}catch(IOException e){
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}
		
	}
	
	// 以下为设置主菜单九宫格样式。
	public final class CatalogHolder{
		public ImageView CatalogCellImage;
		public ImageView badgeImage;
		public TextView badgeNumText;
		public TextView catalogTitleText;
	}

	public class CatalogAdapter extends BaseAdapter {
		
		private LayoutInflater mInflater;
		
		public CatalogAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
		}
		
		public int getCount(){
			return currentCatalogList.size();
		}
		
		public Object getItem(int arg0){
			return currentCatalogList.get(arg0);
		}
		
		public long getItemId(int arg0){
			return arg0;
		}
		
		public View getView(int position, View convertView, ViewGroup parent){
			CatalogHolder holder = null;
			
			if (null == convertView){
				convertView = this.mInflater.inflate(R.layout.catalog_item, null);
				holder = new CatalogHolder();
				
				holder.CatalogCellImage = (ImageView) convertView.findViewById(R.id.catalogCellImage);
				holder.badgeImage = (ImageView) convertView.findViewById(R.id.badgeImageView);
				holder.badgeNumText = (TextView) convertView.findViewById(R.id.badgeNumText);
				holder.catalogTitleText = (TextView) convertView.findViewById(R.id.catalogTitleText);
				
				convertView.setTag(holder);
			} else {
				holder = (CatalogHolder) convertView.getTag(); 
			}
			
			CatalogModel catalog = currentCatalogList.get(position);
			holder.CatalogCellImage.setImageResource(getIconId(catalog.getIconId()));
			holder.catalogTitleText.setText(catalog.getTitle());
			
			if (catalog.getShowBadge().trim().equals("") || ApplicationEnvironment.getInstance().getPreferences().getString(catalog.getShowBadge(), "0").equals("0")){
				holder.badgeImage.setVisibility(View.GONE);
				holder.badgeNumText.setVisibility(View.GONE);
				
			} else {
				holder.badgeImage.setVisibility(View.VISIBLE);
				holder.badgeNumText.setVisibility(View.VISIBLE);
				String tmp = ApplicationEnvironment.getInstance().getPreferences().getString(catalog.getShowBadge(), "0");
				int numInt = Integer.parseInt(tmp);
				if(numInt>99){
					tmp = "99";
				}
				holder.badgeNumText.setText(tmp);
			}
			
			convertView.setOnTouchListener(new OnTouchListener(){
				@Override
				public boolean onTouch(View view, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN){
						view.startAnimation(AnimationUtils.loadAnimation(CatalogActivity.this, R.anim.catalogbutton));
					}
					return false;
				}
				
			});
			
			return convertView;
		}
		
		private int getIconId(int iconId) {
			String resourceName = "catalog_icon_" + iconId;
			int resourceId = getResources().getIdentifier(resourceName, "drawable", getPackageName());
			if (resourceId == 0)
				resourceId = R.drawable.catalog_icon_0;
			
			return resourceId;
		}
	}

}
