package com.lkpower.medical.client;

import android.os.Environment;

public class Constants {
	
	public static final String kHOSTNAME 					= "hostName";
	public static final String kWEBSERVICENAME				= "WebServiceName";
	
	public static final String kCOMPANYNAME					= "kCOMPANYNAME";
	
	public static final String kMETHODNAME 					= "methodName";
	public static final String kPARAMNAME 					= "paramName";
	
	public static final String kLOGINNAME					= "loginName";
	public static final String kPASSWORD 					= "password"; // 保存用户输入的密码
	
	public static final String kUSERID 						= "userId";
	public static final String kUSERNAME 					= "userName";
	
	public static final String kREMEBERPWD  				= "remeberPWD";
	public static final String kAUTOLOGIN  					= "autoLogin";
	
	
	/**通知设置参数**/
	public static final String kOPEN_NOTIFY_CHECK			= "OPEN_NOTIFY_CHECK";
	// 通知推送（是否进行通知的推送功能，默认打开）
	public static final boolean DEFAULT_NOTIFY_CHECK		= true;
	
	public static final String kNOTIFY_CHECK_INTERVAL		= "NOTIFY_CHECK_INTERVAL";
	// 可设置通知推送的时间间隔，默认为10分钟
	public static final int DEFAULT_NOTIFY_CHECK_INTERVAL   = 10;
	
	public static final String FILEPATH 					= Environment.getExternalStorageDirectory()+"/LKOA/MedicalAtt/";
	
	
	/**同步设置参数**/
	public static final String kOPEN_SYNC_CHECK				= "OPEN_SYNC_CHECK";
	// 自动同步设置（默认关闭）
	public static final boolean DEFAULT_SYNC_CHECK			= false;
	
	// 主界面每隔一段时间同步一次信息条数，默认为10分钟
	public static final String kSYNC_CHECK_INTERVAL			= "SYNC_CHECK_INTERVAL";
	public static final int DEFAULT_SYNC_CHECK_INTERVAL		= 10;
	
	/**激活同步参数**/
	public static final String kOPEN_ACTIVE_CHECK			= "OPEN_ACTIVE_CHECK";
	// 主界面激活同步（默认打开）：只要主界面一激活就同步
	public static final boolean DEFAULT_ACTIVE_CHECK		= true;
	
	public static final String kMAXTRACKTIME				= "MAXTRACKTIME";
	
	
	public static final String kWAITITEMSCOUNT              = "Get_WaitItemsCount"; // 待办事宜个数
	public static final String kWAITFILESCOUNT              = "Get_WaitFilesCount"; //  事务审批－待办事务个数
	public static final String kNEWSTOTALLISTCOUNT          = "Get_NewsTotalListCount"; // 最新信息个数
	public static final String kNEWSLISTCOUNT	            = "Get_NewsListCount"; // 信息中心－信息动态个数
	public static final String kNEWSNOTICECOUNT             = "Get_NewsNoticeCount"; // 通知公告－最新通知个数
	public static final String kNOTICECOUNT					= "GetNoticeCount"; // 紧急通知个数
	public static final String kNEWWAITITEMSCOUNT			= "GetNewWaitItemsCount"; // 紧急通知个数
	
	@Deprecated
	public static final String kMAILCOUNT					= "GetMailCount"; // 我的邮件个数
	
	public static final String kGetPortalNoticeNewCount		= "GetPortalNoticeNewCount"; // 门户通知个数
	public static final String kGetMailNewCount				= "GetMailNewCount"; // 邮件
	public static final String kGetCirculatedCount			= "GetCirculatedCount"; // 传阅
	
}
