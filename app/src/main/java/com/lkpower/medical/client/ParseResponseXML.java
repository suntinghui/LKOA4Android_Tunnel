package com.lkpower.medical.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;
import android.util.Xml;

public class ParseResponseXML {

	private static InputStream inStream = null;

	public static Object parseXML(int reqType, String responseStr) {
		responseStr = responseStr.replace("&lt;", "<").replace("&gt;", ">");

		Log.e("response:", responseStr);

		try {
			inStream = new ByteArrayInputStream(responseStr.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		try {
			switch (reqType) {

			case TransferRequestTag.LOGINSERVICE:
				return login();
				
			case TransferRequestTag.ADDRESSLIST:
				return addressList();

			case TransferRequestTag.GET_WAITITEMSCOUNT: // 待办事宜个数
				return get_WaitItemsCount();

			case TransferRequestTag.GET_WAITFILESCOUNT: // 事务审批－待办事务个数
				return get_WaitFilesCount();

			case TransferRequestTag.GET_NEWSTOTALLISTCOUNT: // 最新信息个数
				return get_NewsTotalListCount();

			case TransferRequestTag.GET_NEWSLISTCOUNT: // 信息中心－信息动态个数
				return get_NewsListCount();

			case TransferRequestTag.GET_NEWSNOTICECOUNT: // 通知公告－最新通知个数
				return get_NewsNoticeCount();

			case TransferRequestTag.GET_NOTICECOUNT: // 紧急通知个数
				return get_NoticeCount();

			case TransferRequestTag.GET_NEWWAITITEMSCOUNT: // 新增待办事务
				return get_NewWaitItemsCount();

			case TransferRequestTag.GET_MAILCOUNT: // 电子邮件个数
				return get_MailCount();

			case TransferRequestTag.GetCirculatedCount:
				return GetCirculatedCount();

			case TransferRequestTag.GetMailNewCount:
				return GetMailNewCount();

			case TransferRequestTag.GetPortalNoticeNewCount:
				return GetPortalNoticeNewCount();

			}

		} catch (XmlPullParserException e) {
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();

		} finally {
			try {
				if (null != inStream)
					inStream.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	private static Object login() throws XmlPullParserException, IOException {
		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(inStream, "UTF-8");
		int event = parser.getEventType();
		while (event != XmlPullParser.END_DOCUMENT) {
			switch (event) {
			case XmlPullParser.START_TAG:
				if (parser.getName().contains("Get_WapLoginResult")) {
					return parser.nextText();
				}

				break;
			}

			event = parser.next();
		}
		return null;
	}
	
	private static Object addressList() throws XmlPullParserException, IOException {
		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> map = null;
		
		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(inStream, "UTF-8");
		int event = parser.getEventType();
		while (event != XmlPullParser.END_DOCUMENT) {
			switch (event) {
			case XmlPullParser.START_TAG:
				
				if ("LIST".equalsIgnoreCase(parser.getName())) {
					map = new HashMap<String, String>();
				} else if ("NAME".equalsIgnoreCase(parser.getName())) {
					map.put("NAME", parser.nextText());
				} else if ("SERVICEURL".equalsIgnoreCase(parser.getName())) {
					map.put("SERVICEURL", parser.nextText());
				} 
				
				break;
				
			case XmlPullParser.END_TAG:
				if ("LIST".equalsIgnoreCase(parser.getName())) {
					list.add(map);
				}
				break;
			}

			event = parser.next();
		}
		return list;
	}

	// 待办事宜个数
	private static Object get_WaitItemsCount() throws XmlPullParserException, IOException {
		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(inStream, "UTF-8");
		int event = parser.getEventType();
		while (event != XmlPullParser.END_DOCUMENT) {
			switch (event) {
			case XmlPullParser.START_TAG:
				if (parser.getName().contains("Get_WaitItemsCountResult")) {
					return Constants.kWAITITEMSCOUNT + "|" + parser.nextText();
				}

				break;
			}

			event = parser.next();
		}
		return Constants.kWAITITEMSCOUNT + "|" + "0";
	}

	// 最新信息个数
	private static Object get_NewsTotalListCount() throws XmlPullParserException, IOException {
		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(inStream, "UTF-8");
		int event = parser.getEventType();
		while (event != XmlPullParser.END_DOCUMENT) {
			switch (event) {
			case XmlPullParser.START_TAG:
				if (parser.getName().contains("Get_NewsTotalListCountResult")) {
					return Constants.kNEWSTOTALLISTCOUNT + "|" + parser.nextText();
				}

				break;
			}

			event = parser.next();
		}
		return Constants.kNEWSTOTALLISTCOUNT + "|" + "0";
	}

	// 事务审批－待办事务个数
	private static Object get_WaitFilesCount() throws XmlPullParserException, IOException {
		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(inStream, "UTF-8");
		int event = parser.getEventType();
		while (event != XmlPullParser.END_DOCUMENT) {
			switch (event) {
			case XmlPullParser.START_TAG:
				if (parser.getName().contains("Get_WaitFilesCountResult")) {
					return Constants.kWAITFILESCOUNT + "|" + parser.nextText();
				}

				break;
			}

			event = parser.next();
		}
		return Constants.kWAITFILESCOUNT + "|" + "0";
	}

	// 信息中心－信息动态个数
	private static Object get_NewsListCount() throws XmlPullParserException, IOException {
		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(inStream, "UTF-8");
		int event = parser.getEventType();
		while (event != XmlPullParser.END_DOCUMENT) {
			switch (event) {
			case XmlPullParser.START_TAG:
				if (parser.getName().contains("Get_NewsListCountResult")) {
					return Constants.kNEWSLISTCOUNT + "|" + parser.nextText();
				}

				break;
			}

			event = parser.next();
		}
		return Constants.kNEWSLISTCOUNT + "|" + "0";
	}

	// 通知公告－最新通知个数
	private static Object get_NewsNoticeCount() throws XmlPullParserException, IOException {
		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(inStream, "UTF-8");
		int event = parser.getEventType();
		while (event != XmlPullParser.END_DOCUMENT) {
			switch (event) {
			case XmlPullParser.START_TAG:
				if (parser.getName().contains("Get_NewsNoticeCountResult")) {
					return Constants.kNEWSNOTICECOUNT + "|" + parser.nextText();
				}

				break;
			}

			event = parser.next();
		}
		return Constants.kNEWSNOTICECOUNT + "|" + "0";
	}

	// 紧急通知个数
	private static Object get_NoticeCount() throws XmlPullParserException, IOException {
		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(inStream, "UTF-8");
		int event = parser.getEventType();
		while (event != XmlPullParser.END_DOCUMENT) {
			switch (event) {
			case XmlPullParser.START_TAG:
				if (parser.getName().contains("GetNoticeCountResult")) {
					return Constants.kNOTICECOUNT + "|" + parser.nextText();
				}

				break;
			}

			event = parser.next();
		}

		return Constants.kNOTICECOUNT + "|" + "0";
	}

	// 新增待办事务
	private static Object get_NewWaitItemsCount() throws XmlPullParserException, IOException {
		HashMap<String, String> map = new HashMap<String, String>();

		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(inStream, "UTF-8");
		int event = parser.getEventType();
		while (event != XmlPullParser.END_DOCUMENT) {
			switch (event) {
			case XmlPullParser.START_TAG:
				if ("NewsFlowCount".equalsIgnoreCase(parser.getName())) {
					map.put("count", parser.nextText());
				} else if ("MaxTrackTime".equalsIgnoreCase(parser.getName())) {
					map.put("time", parser.nextText());
				}

				break;
			}

			event = parser.next();
		}
		return map;
	}

	// 电子邮件个数
	private static Object get_MailCount() throws XmlPullParserException, IOException {
		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(inStream, "UTF-8");
		int event = parser.getEventType();
		while (event != XmlPullParser.END_DOCUMENT) {
			switch (event) {
			case XmlPullParser.START_TAG:
				if (parser.getName().contains("GetMailCountResult")) {
					return Constants.kMAILCOUNT + "|" + parser.nextText();
				}

				break;
			}

			event = parser.next();
		}

		return Constants.kMAILCOUNT + "|" + "0";
	}

	// 门户通知个数
	private static Object GetPortalNoticeNewCount() throws XmlPullParserException, IOException {
		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(inStream, "UTF-8");
		int event = parser.getEventType();
		while (event != XmlPullParser.END_DOCUMENT) {
			switch (event) {
			case XmlPullParser.START_TAG:
				if (parser.getName().contains("GetPortalNoticeNewCountResult")) {
					return Constants.kGetPortalNoticeNewCount + "|" + parser.nextText();
				}

				break;
			}

			event = parser.next();
		}

		return Constants.kGetPortalNoticeNewCount + "|" + "0";
	}

	// 电子邮件个数
	private static Object GetMailNewCount() throws XmlPullParserException, IOException {
		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(inStream, "UTF-8");
		int event = parser.getEventType();
		while (event != XmlPullParser.END_DOCUMENT) {
			switch (event) {
			case XmlPullParser.START_TAG:
				if (parser.getName().contains("GetMailNewCountResult")) {
					return Constants.kGetMailNewCount + "|" + parser.nextText();
				}

				break;
			}

			event = parser.next();
		}

		return Constants.kGetMailNewCount + "|" + "0";
	}

	// 传阅个数
	private static Object GetCirculatedCount() throws XmlPullParserException, IOException {
		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(inStream, "UTF-8");
		int event = parser.getEventType();
		while (event != XmlPullParser.END_DOCUMENT) {
			switch (event) {
			case XmlPullParser.START_TAG:
				if (parser.getName().contains("GetCirculatedCountResult")) {
					return Constants.kGetCirculatedCount + "|" + parser.nextText();
				}

				break;
			}

			event = parser.next();
		}

		return Constants.kGetCirculatedCount + "|" + "0";
	}

}
