package com.lkpower.medical.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import com.lkpower.medical.R;
import com.lkpower.medical.client.ApplicationEnvironment;
import com.lkpower.medical.client.Constants;
import com.lkpower.medical.client.DownloadFileRequest;
import com.lkpower.medical.view.NoZoomControllWebView;

@SuppressLint("SetJavaScriptEnabled")
public class ShowWebViewActivity extends BaseActivity implements OnClickListener, OnKeyListener {

	private NoZoomControllWebView mWebView;
	private Button backButton;
	private TextView titleView;
	private TextView nameView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_webcontent_activity);

		Intent intent = this.getIntent();
		String title = intent.getStringExtra("TITLE");
		String backTitle = intent.getStringExtra("BACKTITLE");
		String URL = intent.getStringExtra("URL");

		backButton = (Button) this.findViewById(R.id.backButton);
		backButton.setOnClickListener(this);
		backButton.setText(backTitle);

		titleView = (TextView) this.findViewById(R.id.titleText);
		titleView.setText(title);

		nameView = (TextView) this.findViewById(R.id.nameText);
		nameView.setText(ApplicationEnvironment.getInstance().getPreferences().getString(Constants.kUSERNAME, "") + ", 您好！");

		mWebView = (NoZoomControllWebView) this.findViewById(R.id.webview);
		mWebView.setOnKeyListener(this); // 容易造成混乱

		WebSettings setting = mWebView.getSettings();

		setting.setJavaScriptEnabled(true);
		setting.setJavaScriptCanOpenWindowsAutomatically(true);

		setting.setSupportZoom(true);
		setting.setLoadsImagesAutomatically(true);

		setting.setBuiltInZoomControls(true);

		setting.setUseWideViewPort(true);
		setting.setLoadWithOverviewMode(true);
		setting.setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);

		// this.setFitZoom();
		// http://blog.csdn.net/to_cm/article/details/7801918

		if (null != URL) {

			mWebView.setWebChromeClient(new WebChromeClient() {

				@Override
				public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
					return super.onJsAlert(view, url, message, result);
				}

				@Override
				public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
					return super.onJsConfirm(view, url, message, result);
				}

				@Override
				public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
					return super.onJsPrompt(view, url, message, defaultValue, result);
				}

			});

			mWebView.setWebViewClient(new WebViewClient() {
				@Override
				public void onPageStarted(WebView view, String url, Bitmap favicon) {
					super.onPageStarted(view, url, favicon);
					ShowWebViewActivity.this.showDialog(BaseActivity.PROGRESS_DIALOG, "正在加载，请稍候...");
				}

				@Override
				public void onPageFinished(WebView view, String url) {
					super.onPageFinished(view, url);
					ShowWebViewActivity.this.hideDialog(BaseActivity.PROGRESS_DIALOG);
				}

				// 重写此方法返回true表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边。
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					Log.e("url", url);

					if (url.endsWith("returnPageIndex")) {
						ShowWebViewActivity.this.finish();
						return true;

					} else if (url.contains("DownLoadAttFile.aspx?")) {
						// http://124.205.53.178:8008/Wap/attFileManager/DownLoadAttFile.aspx?attId=791de718-a7f4-4354-8226-409a15941714

						DownloadFileRequest.sharedInstance().downloadAndOpen(ShowWebViewActivity.this, url);
						return true;
					}

					mWebView.loadUrl(url);
					return true;
				}

			});

			mWebView.loadUrl(URL);
		}

	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.backButton:
			this.finish();
			break;
		}
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) { // 表示按返回键时的操作
				mWebView.goBack(); // 后退
				return true; // 已处理
			}
		}
		return false;
	}

	private void setFitZoom() {
		// Enum for specifying the WebView's desired density. FAR makes 100%
		// looking like
		// in 240dpi MEDIUM makes 100% looking like in 160dpi CLOSE makes 100%
		// looking like in 120dpi
		int screenDensity = getResources().getDisplayMetrics().densityDpi;
		WebSettings.ZoomDensity zoomDensity = WebSettings.ZoomDensity.MEDIUM;

		switch (screenDensity) {
		case DisplayMetrics.DENSITY_LOW:
			zoomDensity = WebSettings.ZoomDensity.CLOSE;
			break;
		case DisplayMetrics.DENSITY_MEDIUM:
			zoomDensity = WebSettings.ZoomDensity.MEDIUM;
			break;
		case DisplayMetrics.DENSITY_HIGH:
			zoomDensity = WebSettings.ZoomDensity.FAR;
			break;
		}

		mWebView.getSettings().setDefaultZoom(zoomDensity);
	}

}
