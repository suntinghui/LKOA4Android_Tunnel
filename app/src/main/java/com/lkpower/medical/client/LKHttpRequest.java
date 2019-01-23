package com.lkpower.medical.client;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.lkpower.medical.activity.BaseActivity;
import com.loopj.android.http.AsyncHttpClient;

public class LKHttpRequest {
	
	private int tag;
	
	private HashMap<String, Object> requestDataMap;
	private LKAsyncHttpResponseHandler responseHandler;
	private AsyncHttpClient client;
	private LKHttpRequestQueue queue;
	
	private Context context;
	
	public LKHttpRequest(Context context, HashMap<String, Object> requestMap, LKAsyncHttpResponseHandler handler){
		this.context = context;
		
		this.requestDataMap = requestMap;
		this.responseHandler = handler;
		client = new AsyncHttpClient();
		
		if (handler != null){
			this.responseHandler.setRequest(this);
		}
	}
	
	public String getRequestURL(){
		String host = ApplicationEnvironment.getPreferences(this.context).getString(Constants.kHOSTNAME, "");
		if (!host.endsWith("/")){
			host += "/";
			
			Editor editor = ApplicationEnvironment.getPreferences(this.context).edit();
			editor.putString(Constants.kHOSTNAME, host);
			editor.commit();
		}
		
		return host + requestDataMap.get(Constants.kWEBSERVICENAME);
	}
	
	public int getTag(){
		return tag;
	}
	
	public void setTag(int tag){
		this.tag = tag;
	}
	
	public LKHttpRequestQueue getRequestQueue(){
		return this.queue;
	}
	
	public void setRequestQueue(LKHttpRequestQueue queue){
		this.queue = queue;
	}
	
	public HashMap<String, Object> getRequestDataMap() {
		return requestDataMap;
	}
	
	public LKAsyncHttpResponseHandler getResponseHandler() {
		return responseHandler;
	}

	public AsyncHttpClient getClient() {
		return client;
	}
	
	
	/****************************************/
	
	public void post(){ 
		try{
			this.client.addHeader("SOAPAction", "http://www.lkpower.com/wap/"+TransferRequestTag.getRequestTagMap().get(this.getRequestDataMap().get(Constants.kMETHODNAME)));
			this.client.post(this.context, this.getRequestURL(), this.getHttpEntity(this), "text/xml; charset=utf-8", this.responseHandler);
			
		} catch(IllegalArgumentException e){
			 BaseActivity.getTopActivity().showDialog(BaseActivity.MODAL_DIALOG, "服务器地址异常，请重新输入");
		} catch(Exception e){
			BaseActivity.getTopActivity().showDialog(BaseActivity.MODAL_DIALOG, "系统异常，请重试");
		}
	}
	
	@SuppressWarnings("unchecked")
	private HttpEntity getHttpEntity(LKHttpRequest request){
		HashMap<String, Object> reqMap = request.getRequestDataMap();
		
		StringBuffer bodySB = new StringBuffer();
		bodySB.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>"
				+ "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
				+ "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" "
				+ "xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><");
		bodySB.append(TransferRequestTag.getRequestTagMap().get(reqMap.get(Constants.kMETHODNAME)));
		bodySB.append(" xmlns=\"http://www.lkpower.com/wap\">");
		bodySB.append(this.param2String((HashMap<String, Object>)reqMap.get(Constants.kPARAMNAME)));
		bodySB.append("</");
		bodySB.append(TransferRequestTag.getRequestTagMap().get(reqMap.get(Constants.kMETHODNAME)));
		bodySB.append("></soap:Body></soap:Envelope>");
		
		request.getClient().addHeader("Content-Length", bodySB.length()+"");
		
		Log.e("request body:", bodySB.toString());
		
		HttpEntity entity = null;
		try {
			//entity = new StringEntity(bodySB.toString(), HTTP.UTF_8);
			entity = new StringEntity(bodySB.toString());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return entity;
	}
	
	@SuppressWarnings("unchecked")
	private String param2String(HashMap<String, Object> paramMap){
		StringBuffer sb = new StringBuffer();
		for	(String key : paramMap.keySet()){
			Object obj = paramMap.get(key);
			if (obj instanceof String){
				sb.append("<").append(key).append("><![CDATA[").append((String)obj).append("]]></").append(key).append(">");
			} else {
				sb.append("<").append(key).append("><![CDATA[").append(this.hashMap2XML((HashMap<String, Object>)obj)).append("]]></").append(key).append(">");
			}
		}
		return sb.toString();
	}
	
	@SuppressWarnings("unchecked")
	private String hashMap2XML(HashMap<String, Object> paramMap){
		StringBuffer sb = new StringBuffer();
		for	(String key : paramMap.keySet()){
			Object obj = paramMap.get(key);
			if (obj instanceof String){
				//sb.append("<").append(key).append(">").append(obj).append("</").append(key).append(">");
				sb.append("<").append(key).append("><![CDATA[").append((String)obj).append("]]></").append(key).append(">");
			} else {
				sb.append("<").append(key).append(">").append(this.hashMap2XML((HashMap<String, Object>)obj)).append("</").append(key).append(">");
			}
		}
		return sb.toString();
	}
	
}
