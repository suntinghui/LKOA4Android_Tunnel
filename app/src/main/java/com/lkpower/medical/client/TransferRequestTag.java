package com.lkpower.medical.client;

import android.annotation.SuppressLint;
import java.util.HashMap;

public class TransferRequestTag {
	
	public static final int LOGINSERVICE					= 1;
	public static final int ADDRESSLIST						= 1986;
	
	public static final int GET_WAITITEMSCOUNT              = 2;
	public static final int GET_NEWSTOTALLISTCOUNT          = 3; 
	public static final int GET_WAITFILESCOUNT              = 4;
	public static final int GET_NEWSLISTCOUNT               = 5;
	public static final int GET_NEWSNOTICECOUNT             = 6;
	public static final int GET_MAILCOUNT					= 7;
	public static final int GET_NOTICECOUNT					= 8;
	
	public static final int GetPortalNoticeNewCount			= 9;
	public static final int GetMailNewCount					= 10;
	public static final int GetCirculatedCount				= 11;
	
	public static final int GET_NEWWAITITEMSCOUNT			= 100; // 新增代办事务
	
	private static HashMap<Integer, String> requestTagMap 	= null;
	
	@SuppressLint("UseSparseArrays")
	public static HashMap<Integer, String> getRequestTagMap(){
		if (null == requestTagMap) {
			requestTagMap = new HashMap<Integer, String>();
			
			// value值与服务器地址匹配 ！！！
			
			requestTagMap.put(LOGINSERVICE, "Get_WapLogin"); // 登录
			requestTagMap.put(ADDRESSLIST, "GetServiceAddressList"); // 获取地址
			
			
			requestTagMap.put(GET_WAITITEMSCOUNT, "Get_WaitItemsCount"); // 待办事宜个数
			requestTagMap.put(GET_WAITFILESCOUNT, "Get_WaitFilesCount"); // 事务审批－待办事务个数
			requestTagMap.put(GET_NEWSTOTALLISTCOUNT, "Get_NewsTotalListCount"); // 最新信息个数
			requestTagMap.put(GET_NEWSLISTCOUNT, "Get_NewsListCount");  // 信息中心－信息动态个数
			requestTagMap.put(GET_NEWSNOTICECOUNT, "Get_NewsNoticeCount"); // 通知公告－最新通知个数
			requestTagMap.put(GET_NOTICECOUNT, "GetNoticeCount"); // 紧急通知个数
			requestTagMap.put(GET_NEWWAITITEMSCOUNT, "GetNewWaitItemsCount"); // 新增代办事务
			requestTagMap.put(GET_MAILCOUNT, "GetMailCount"); // 电子邮件个数
			requestTagMap.put(GetPortalNoticeNewCount, "GetPortalNoticeNewCount"); // 门户通知个数
			requestTagMap.put(GetMailNewCount, "GetMailNewCount"); // 电子邮件个数
			requestTagMap.put(GetCirculatedCount, "GetCirculatedCount"); // 传阅个数
			
		}
		
		return requestTagMap;
	}
}
