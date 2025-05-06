package com.melon.phoneagent.autil;

import android.os.Build;
import android.view.accessibility.AccessibilityNodeInfo;

import com.melon.util.APKUtil;
import com.melon.util.AndroidUtil;
import com.melon.util.FileUtil;
import com.melon.util.LogUtil;
import com.melon.util.StringUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public abstract class APhoneParams {
	static public boolean doShutdown = true;

	static public int isRedo = 0;
	static public int openGuide = 0;
	static public int clearBack = 0;

	static public int installType = 0;
	static public String tarPath = "/sdcard/MIUI/backup.tar";

	static public String accountId = "";

	static final public String yizhiNames = "一知|通知管理|通知管家|通知管理大师|一知.通知管理|一知.通知大师|一知.通知管家|通知大师";

	static public class PhoneApp {
		public String apkName;
		public String name;
		public String md5;
		public String checkV3;
		public int ii;
		public boolean devOpen;
		public boolean autoRun;
		public boolean display;
		public boolean installed;

		public boolean startApp() {
			if(!APKUtil.startActivity(apkName)) return false;
			if(AndroidUtil.isXiaomi()) {
				String pkg = AccessServiceUtil.getCurrentPackage();
				if (pkg.equals("android")) {
					LogUtil.writeLog("alert>>>>>>>>>>>");
					ClickUtil.clickButton(new AccessSelector().text("知道了"));
					AndroidUtil.sleep(500);
				}
			}
			return AccessServiceUtil.waitForPackage(apkName, 2000);
		}

		public boolean doPermission() {
			if(apkName.startsWith("com.naijia.")) return true;
			if(apkName.startsWith("com.huawei.")) return true;
			if(apkName.startsWith("com.zengame.")) return true;
			if(apkName.startsWith("backup")) return true;
			if(apkName.equals("cn.kuwo.player")) return true;
			if(apkName.equals("com.ss.android.ugc.live")) return true;
			if(apkName.equals("com.ss.android.ugc.aweme")) return true;
			if(apkName.equals("com.ss.android.ugc.aweme.lite")) return true;
			if(apkName.equals("com.autonavi.minimap")) return true;
			if(apkName.equals("com.funqingli.clear")) return true;
			if(apkName.equals("com.iflytek.inputmethod")) return true;
			if(apkName.equals("com.sohu.newsclient")) return true;

			if(!APKUtil.isAPKInstalled(apkName)) return true;

			if(!startApp()) {
				if(!AndroidUtil.isXiaomi()) return false;
				String pkg = AccessServiceUtil.getCurrentPackage();
				if (!pkg.equals("android")) return false;
				ClickUtil.clickButton(new AccessSelector().text("知道了"));
				if(!AccessServiceUtil.waitForPackage(apkName, 2000)) return false;
			}

			if(ClickUtil.clickButton(new AccessSelector().text("跳过*"))) {
				AndroidUtil.sleep(500);
			}

			boolean found = false;
			for(int ii=0;ii<6;ii++) {
				AccessibilityNodeInfo node = AccessUtil.getChild(new AccessSelector().text("立即授权|允许|始终允许|同意|同意并进入|同意并继续|同意并开启服务|立即开启|确定|我知道了|知道了|下一步|开始|好的|继续|马上体验"));
				if(node == null) node = AccessUtil.getChild(new AccessSelector().text("同意*"));
				if(node == null) node = AccessUtil.getChild(new AccessSelector().text("进入*"));
				if(node == null) {
					if(found) break;
					AndroidUtil.sleep(500);
					continue;
				}

				LogUtil.writeLog("click>>>>>>>>>>>" + node.getText());
				if(ClickUtil.clickButton(node)) {
					found = true;
					AndroidUtil.sleep(500);
					continue;
				}

				if(found) break;
				AndroidUtil.sleep(500);
			}

			AccessServiceUtil.pressHome();
			return true;
		}
	};

	static public List<PhoneApp> phoneApps = new ArrayList<>();

	//UI配置对象
	static JSONObject uiConfigObject = null;

	static public boolean delApp = false;


	//	static public String toolAPKPKG = "com.melon.installtool";
	//装机工具包名
	static public String toolAPKPKG = "com.naijia.itool";
	//装机工具应用名称
	static public String toolTitle = "itool";

	//装机工具包名  留存
	static public String toolAPKPKG2 = "com.xtool.criterion";
	//装机工具应用名称  留存
	static public String toolTitle2 = "cen";

//	//计数器包名数组
//	static public final String[] Counters = {
//			"com.enneahedron.browser","com.enneahedron.clean","com.enneahedron.calendar","com.enneahedron.weather"
//	};
//	//计数器是否都安装上去了
//	public static boolean isCountersInstalled() {
//		for (String counter:APhoneParams.Counters){
//			if (counter.contains("clean"))continue;
//			if(!APKUtil.isAPKInstalled(counter))return false;
//		}
//		return true;
//	}
	//应用市场包名
//	static public final String apkstorePkg ="com.enneahedron.apkstore";

	//搜狗浏览器 QQ浏览器 UC 浏览器
 	static public boolean hasBrowser=false;
    static public final String BrowserName ="UC 浏览器";
//    static public final String BrowserName ="手机浏览器";
	//sogou.mobile.explorer com.tencent.mtt com.UCMobile  sogou.mobile.explorer.xiangyixing
	static public final String BrowserPkg ="com.UCMobile";
//	static public final String BrowserPkg ="com.muniu.potatobrowser";
	
 	static public boolean hasSms=false;
	static protected final String SmsName ="信息";
	static public final String SmsPkg ="com.huhu.sms";
	
 	static public boolean hasInput=false;
	static protected final String InputName ="讯飞输入法";
	static public final String InputPkg ="com.iflytek.inputmethod";

	private static String[] closeKeyText = {
			"确认退出"		,
			"安装"		,
			"激活"		,
			"确定"		,
			"确认"		,
			"完成"		,
			"允许"		,
			"始终"		,
			"总是允许"	,
			"始终允许"	,
			"始终打开"	,
			"继续使用"	,
			"仍然允许"	,
			"忽略本次",
			"开启",
			"启动"		,
			"立即清理"		,
			"不切换",
			"关闭"		,
			"清理"		,//小米SD卡安装适配需要注释掉这行
			"好"			,
			"允许本次安装"	,
			"允许安装",
			"仅允许一次"	,
			"允许一次"	,
			"仅在使用中允许"	,
			"解除"		,
			"解散"		,
			"等待"		,
			"继续卸载"		,
			"立即清除"		,
			"卸载"		,
			"移除",
			"删除"		,
			"立即删除"		,
			"狠心删除"	,
			"继续安装"	,
			"继续"		,
			"好的"		,
			"使用",
			"稍后",

//		"打开"		,
			"下一步"		,
			"解除禁止"	,
			"同意"		,
			"同意并使用"		,
			"同意并开始使用"		,
			"同意并继续"		,
			"更换"		,
			"继续安装旧版本"	,
			"我已充分了解该风险，继续安装",
			"我已知问题严重性。无视风险安装",
			"我已知问题严重性，无视风险安装",
			"我已经了解该安全风险，继续安装",
			"重新安装",
			"创建",
			"知道了",
			"开始",
			"确认开启",
			"允许全部安装",
			"忽略",
	};

	private static String[] checklist = {
			"记住我的选择",
			"下次不再提示",
			"允许",
			"不再提示", "记住我的选择。",
			"不再提示",
			"记住我的选择。",
			"我已充分了解该风险，继续安装",
			"始终允许灵犀桌面创建小部件并访问其数据",
	};

	private static String[] checkreslist = {
			"com.qiku:id/permission_remember_choice_checkbox",
			"com.android.packageinstaller:id/decide_to_continue",
	};

	//需要检查的点击
	private static String[] closeKeyCheckText = {
			"激活",
			"继续安装旧版本",
			"我已知问题严重性。无视风险安装",
			"我已充分了解该风险，继续安装",
	};

	private static String[] closeKeyInAllText = {
			"继续安装",
			"激活此设备管理员",
	};

	//安装过程中自动点击的按钮
	static public String closeKeyString = null;
	static public String closeKeyCheckString = null;
	static public String closeKeyInAllString = null;
	static public String checkKeyString = null;
	static public String checkResString = null;

	static public String closeAddString = null;

	//自动运行数组和查找字符串
	static public String appString = null;
	static public String autoRunString = null;

	//打开设备管理器数组和查找字符串
	static public String devInfoString = null;

	static APhoneParams mInstance = null;
	static public void setInstance(APhoneParams instance) {
		mInstance = instance;
	}

	public static void init() {
		//将关闭按钮
		StringBuilder keyName = new StringBuilder();
		for(int ii = 0; ii< APhoneParams.closeKeyText.length; ii++) {
			String s = APhoneParams.closeKeyText[ii];
			if(AndroidUtil.isVIVO()) {
				if(s.equals("清理")) continue;
			}

			if (ii == 0) {
				keyName.append(s);
				continue;
			}

			keyName.append("|");
			keyName.append(s);
		}

		if(Build.BRAND.equalsIgnoreCase("hisense")) {
			keyName.append("|");
			keyName.append("打开");
		}

		APhoneParams.closeKeyString = keyName.toString();

		APhoneParams.closeKeyInAllString = StringUtil.join("|", APhoneParams.closeKeyInAllText);
		APhoneParams.closeKeyCheckString = StringUtil.join("|", APhoneParams.closeKeyCheckText);
		APhoneParams.checkResString = StringUtil.join("|", APhoneParams.checkreslist);
		APhoneParams.checkKeyString = StringUtil.join("|", APhoneParams.checklist);
		LogUtil.writeLog(APhoneParams.closeKeyInAllString);

//		APhoneParams.toolTitle = StaticVarUtil.mAppTitle;

		loadAppList();
		loadAutoRunList();
		loadDevInfoList();

		isThisPhoneInUiConfigList(null);
	}

	/**
	 * 加载安装软件列表
	 */
	static public void loadAppList() {
		mInstance.loadAppList_();
	}

	/**
	 * 加载自动运行列表
	 */
	static public void loadAutoRunList() {
		mInstance.loadAutoRunList_();
	}

	/**
	 * 加载设备管理器列表
	 */
	static public void loadDevInfoList() {
		mInstance.loadDevInfoList_();
	}

	//type 类型 @不关闭开发者   #不设置设备管理器  %打开开机引导  <用eclick点击 >停用应用市场
	//newversion:type 类型dontcloseusb不关闭开发者   dontsetdadmin不设置设备管理器  openboot打开开机引导  useeclick用eclick点击 ableappmarket停用应用市场  dontsetinput不设置默认输入法
	public static boolean isThisPhoneInUiConfigList(String type, boolean defVal){
		if (uiConfigObject == null) return defVal;
		if (type == null) return defVal;

		try {
			if(!uiConfigObject.has(type)) return defVal;

			JSONArray typeArray=uiConfigObject.getJSONArray(type);
			for(int ii=0;ii<typeArray.length();ii++) {
				JSONObject one=typeArray.getJSONObject(ii);
				String _brand=one.getString("brand");
				String _model=one.getString("model");
				if (Build.BRAND.toLowerCase().contains(_brand)&&Build.MODEL.toLowerCase().contains(_model)) {
					LogUtil.writeLog("brand:"+Build.BRAND+",model:"+Build.MODEL+",type="+type);
					return true;
				}
//				return false;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return defVal;
	}

	public static boolean isThisPhoneInUiConfigList(String type) {
		return isThisPhoneInUiConfigList(type, false);
	}

	public static PhoneApp getApp(String apkName) {
		for(PhoneApp app: phoneApps) {
			if(app.apkName.equals(apkName)) return app;
		}
		return null;
	}

	public static boolean hasApp(String apkName) {
		return getApp(apkName) != null;
	}

	abstract protected void loadAppList_();
	abstract protected void loadAutoRunList_();
	abstract protected void loadDevInfoList_();
}
