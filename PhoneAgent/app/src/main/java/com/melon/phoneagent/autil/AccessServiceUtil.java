package com.melon.phoneagent.autil;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.media.AudioManager;
import android.util.Size;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import com.melon.util.APKUtil;
import com.melon.util.AndroidUtil;
import com.melon.util.LogUtil;
import com.melon.util.StaticVarUtil;
import com.melon.util.StringUtil;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

@SuppressLint("NewApi")
public class AccessServiceUtil {

//	static protected APhoneInterface mCurrentPhone = null;

	static public boolean redrawWhenNull = true;

	static AccessibilityService mService = null;

	public static void enableSleepActive(boolean enable) {
	}

	public static boolean startActivity(String pkgName, String activityName, JSONObject json) {

		if (!APKUtil.hasActivity(pkgName, activityName)) return false;

		if (mService == null) return false;
		return APKUtil.startActivity(pkgName, activityName, json);
	}

	public static boolean startActivity(String pkgName, JSONObject json) {
		if (!APKUtil.hasPackage(pkgName)) return false;

		if (mService == null) return false;
		return APKUtil.startActivity(pkgName, json);
	}

	public static boolean startAction(String actionName, JSONObject json) {
		if (mService == null) return false;
		return APKUtil.startAction(actionName, json);
	}

	/**
	 * 停用模拟点击
	 */
	public static void disableSelf() {
		if (mService == null) {
			return;
		}

		mService.disableSelf();
		mService = null;
	}

	/**
	 * 当前的Service是否可用
	 *
	 * @return
	 */
	public static boolean isValid() {
		if (mService == null) return false;

		return true;
	}

	/**
	 * 初始化
	 *
	 * @param service
	 */
	public static void init(AccessibilityService service) {
		if (mService == null) return;

		if (service == null) {
			mService = null;
			return;
		}

		//开始
		mService = service;
	}

	static long extwindow_lastshowtime = 0;
	static public boolean showExtWindow() {
		if (AndroidUtil.isXiaomi()) return false;
		if (AndroidUtil.isHuawei()) return false;
		long now = System.currentTimeMillis();
		if (now - extwindow_lastshowtime < 2000) return false;
		extwindow_lastshowtime = now;
		try {
			AudioManager am = (AudioManager) mService.getSystemService(Context.AUDIO_SERVICE);
			if (am == null) return false;
			int currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
			am.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, AudioManager.FLAG_SHOW_UI);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * 获取根节点
	 *
	 * @return
	 */
	static public AccessibilityNodeInfo getLocalRootNode() {
		if (mService == null) return null;

		AccessibilityNodeInfo root = null;
		try {
			root = mService.getRootInActiveWindow();
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.writeLog("getLocalRootNode exception: " + e.toString());
		}
		if (root != null) return root;

		List<AccessibilityWindowInfo> nodes = mService.getWindows();
		for (AccessibilityWindowInfo node : nodes) {
			if (node == null) {
				LogUtil.writeLog("mService.getWindows() node==null!");
				continue;
			}
			root = node.getRoot();
			if (root != null) return root;
		}

		return null;
	}

	public static boolean powerOff() {
		if (mService == null) return false;
		return mService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_POWER_DIALOG);
	}

	public static boolean lockScreen() {
		if (mService == null) return false;
		return mService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN);
	}

	public static boolean getRecents() {
		if (mService == null) return false;
		LogUtil.writeLog("getRecents");
		return mService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
	}

	public static boolean getNotifications() {
		if (mService == null) return false;
		return mService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS);
	}

	private static boolean goHome() {
		if (mService == null) return false;
		return mService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
	}

	public static boolean wakeUp() {
		return true;
	}

	public static void stopPackage(String apkName) {
		if (mService == null) return;
		//不能把自己停掉
		if (apkName.equals(StaticVarUtil.mPackageName)) {
			LogUtil.writeLogWithTrace("stopPackage light");
			return;
		}

		goCurrentActivityBack(apkName);
	}

	public static void clearPackage(String apkName) {
		if (mService == null) return;

		//不能把自己停掉
		if (apkName.equals(StaticVarUtil.mPackageName)) {
			LogUtil.writeLogWithTrace("clearPackage light");
			return;
		}

		if (!APKUtil.openAppDetail(apkName)) return;

		AndroidUtil.sleep(1000);
		if (!ClickUtil.waitAndClickButton(new AccessSelector().text("存储|存储占用"))) {
			return;
		}

		ClickUtil.waitAndClickButton(new AccessSelector().text("删除数据|清除数据"));

		ClickUtil.waitAndClickButton(new AccessSelector().text("删除|确定|清除"));
		AndroidUtil.sleep(500);

		AccessServiceUtil.goCurrentActivityBack();
	}

	public static String runShell(String shell, boolean showLog) {
		if (mService == null) return "";
		if (showLog) LogUtil.writeLog(shell);

		StringBuilder sb = new StringBuilder();
		BufferedReader input = null;
		try {
			Process p = Runtime.getRuntime().exec(shell);
			input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
			String line;
			while ((line = input.readLine()) != null) {
				sb.append("\n");
				sb.append(line);
			}
		} catch (Exception e) {
			LogUtil.writeLog(e);
		}
		if (input != null) {
			try {
				input.close();
			} catch (Exception e) {
				LogUtil.writeLog(e);
			}
		}
		return sb.toString();
	}

	public static String runShell(String shell) {
		return runShell(shell, true);
	}

	public static String runShell(String shell, int timeout) {
		return runShell(shell, true);
	}
//	/**
//	 * 设置当前的手机
//	 * @param phone
//	 */
//	public static void setCurrentPhone(APhoneInterface phone) {
//		mCurrentPhone = phone;
//	}
//
//	/**
//	 * 获取当前的手机
//	 * @return
//	 */
//	public static APhoneInterface getCurrentPhone() {
//		return mCurrentPhone;
//	}

	/**
	 * 当前的顶部包体是否是该包名
	 *
	 * @param packagename
	 * @return
	 */
	static public boolean isPackage(String packagename) {
		String pn = getCurrentPackage();
		if (pn == null) return false;

		if (StringUtil.isTextMatch(pn, packagename)) return true;

		return false;
	}

	/**
	 * 获取当前的顶部包名
	 *
	 * @return
	 */
	static public String getCurrentPackage() {
		AccessibilityNodeInfo info = null;
		try {
			info = getRootNode();
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.writeLog("getCurrentPackage exception: " + e.toString());
		}
		if (info == null) return null;

		return info.getPackageName().toString();
	}

	/**
	 * 等待顶部的包名,直到等到
	 *
	 * @param packageName
	 * @param timeout
	 * @return
	 */
	static public boolean waitPackage(String packageName, int timeout) {
		int tm = 0;
		while (true) {
			if (tm >= timeout) break;
			if (isPackage(packageName)) return true;
			AndroidUtil.sleep(500);
			tm += 500;
		}

		return false;
	}

	static public AccessibilityNodeInfo getRootNode(int timeout) {
		int sleepTime = 50;

		AccessibilityNodeInfo root = getLocalRootNode();
		if (root != null) return root;

		LogUtil.writeLog("root is null in getRootNode<<1");
		showExtWindow();
		for (int ii = 1; ; ii++) {
			AndroidUtil.sleep(sleepTime);
//			LogUtil.writeLog("seep num " + ii);
			root = getLocalRootNode();
			if (root != null) return root;

			if (sleepTime * ii >= timeout) break;
		}

		if (!redrawWhenNull || !AndroidUtil.isXiaomi()) {
			LogUtil.writeLog("root is null in getRootNode");
			return null;
		}

		//小米可以用这个来解决
		getRecents();
		AndroidUtil.sleep(500);
		goActivityBack();
		AndroidUtil.sleep(500);

		root = getLocalRootNode();
		if (root != null) return root;

		LogUtil.writeLog("root is null in getRootNode");
		return null;
	}

	static public AccessibilityNodeInfo getRootNode() {
		return getRootNode(100);
	}

	/**
	 * 获取根节点
	 *
	 * @return
	 */
	static public Size getRootSize() {
		AccessibilityNodeInfo root = getRootNode();
		if (root == null) return null;

		Rect rect = new Rect();
		root.getBoundsInScreen(rect);
		return new Size(rect.width(), rect.height());
	}

	static public boolean goActivityBack(int num) {
		if (mService == null) return false;

		try {
			for (int ii = 0; ii < num; ii++) {
				if (!mService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK))
					return false;
//					if(AndroidUtil.isOPPO()||AndroidUtil.isXiaomi()) {
				if (AndroidUtil.isOPPO()) {
					AndroidUtil.sleep(100);
				} else {
					String serial = AccessUtil.getXMLSerial();
					AccessUtil.waitNextStill(serial);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.writeLog("goActivityBack exception: " + e.toString());
		}
		return true;
	}

	static public boolean goActivityBack() {
		boolean retVal = false;
		try {
			retVal = goActivityBack(1);
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.writeLog("goActivityBack exception: " + e.toString());
		}
		return retVal;
	}

	static public boolean goActivityBackWithPackage(String packageName, boolean isToPackage) {
		if (!isValid()) return false;

		AccessibilityNodeInfo root = null;
		for (int ii = 0; ii < 5; ii++) {
			root = getRootNode();
			if (root != null) break;
			goActivityBack();
			LogUtil.writeLog("back1");
			AndroidUtil.sleep(500);
		}
		if (root == null) return false;

		if (packageName == null) {
			packageName = root.getPackageName().toString();
		}

		int times = 0;
		while (true) {
			root = getRootNode();
			if (root == null) {
				LogUtil.writeLog("back3:null");
				goActivityBack();
				AndroidUtil.sleep(500);
				continue;
			}

			String pName = root.getPackageName().toString();
			if (pName.contains("launcher")) break;

			boolean isSame = pName.equals(packageName);
			if (isToPackage == isSame) break;
			goActivityBack();
			LogUtil.writeLog("back2:" + root.getPackageName());
			times++;
			if (times > 6) break;
		}

		return true;
	}

	static public boolean goActivityBackFromCurrent() {
		return goActivityBackWithPackage(null, false);
	}

	static public boolean goActivityBackToSelf() {
		return goActivityBackWithPackage(StaticVarUtil.mPackageName, true);
	}

	static public boolean installAPK(String apkPath, String apkName, int pos) {
		if (!isValid()) return false;

		LogUtil.writeLog(">>>installAPK");
		try {
			ClickUtil.stopCloseDialog();
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.writeLog("installAPK exception: " + e.toString());
		}
		return true;
	}

	/**
	 * 等待界面，知道界面非空
	 *
	 * @param times
	 * @param message
	 * @return
	 */
	public static boolean waitForNotNull(int times, String message) {
		if (!isValid()) return false;
		int ii = 0;

		while (true) {
			if (getRootNode() != null) break;

			if (times > 0) {
				times--;
				if (times > 0) break;

				return false;
			}

			if (ii % 3 == 0) {
//				InstallUtil.showMessage(message);
			}
			ii++;
			AndroidUtil.sleep(1000);
		}

		return true;
	}

	/**
	 * 等待软件到前台
	 *
	 * @param packageName
	 * @param timeout
	 * @param sleep
	 * @return
	 */
	public static boolean waitForPackage(String packageName, int timeout, int sleep) {
		if (!isValid()) return false;

		if (packageName == null) return true;

		boolean found = false;
		for (int ii = 0; ii < timeout / 100; ii++) {
			AccessibilityNodeInfo info = getRootNode();
			if (info == null) {
				LogUtil.writeLog("root is null[waitForPackage]");
				AndroidUtil.sleep(100);
				continue;
			}

			String pkg = info.getPackageName().toString();
			if (packageName.equalsIgnoreCase(pkg)) {
				found = true;
				break;
			} else {
				LogUtil.writeLog(packageName + " != " + pkg);
			}

			AndroidUtil.sleep(100);
		}

		if (!found) return false;

		if (sleep <= 0) return true;

		AndroidUtil.sleep(sleep);
		return true;
	}

	public static boolean waitForPackage(String packageName, int timeout) {
		return waitForPackage(packageName, timeout, 0);
	}

	public static boolean startActivityAndWait(String packageName, String activity, int timeout) {
		if (!APKUtil.startActivity(packageName, activity)) {
			return false;
		}

		if (!waitForPackage(packageName, timeout)) {
			return false;
		}

		AccessUtil.waitSleepAndStill();

		return true;
	}

	public static boolean startActionAndWait(String actionName, String packageName, int timeout) {
		if (!APKUtil.startAction(actionName)) {
			return false;
		}

		if (!waitForPackage(packageName, timeout)) {
			return false;
		}

		AccessUtil.waitSleepAndStill();

		return true;
	}

	public static boolean startActivity2Main(String packageName, String activity, int timeout) {
		if (startActivityAndWait(packageName, activity, timeout)) {
			goCurrentActivityBack();
		}

		if (!startActivityAndWait(packageName, activity, timeout)) {
			return false;
		}

		return true;
	}

	public static void pressHome() {
		if (!goHome()) return;
		LogUtil.writeLog("pressHome");
		if (AndroidUtil.isOPPO()) {
			AndroidUtil.sleep(100);
		} else {
			AndroidUtil.sleep(500);
		}
	}

	public static boolean hasStopFile() {
		return false;
	}

	public static void goCurrentActivityBack(String packageName) {
		AccessibilityNodeInfo root = null;
		for (int ii = 0; ii < 5; ii++) {
			root = AccessServiceUtil.getRootNode();
			if (root != null) break;
			goActivityBack();
			if (AndroidUtil.isOPPO()) {
				AndroidUtil.sleep(100);
			} else {
				AndroidUtil.sleep(500);
			}
		}
		if (root == null) return;

		String currentPackage = root.getPackageName().toString();
		if (packageName == null) {
			packageName = currentPackage;
		}
		if (!packageName.equals(currentPackage)) return;
		int trynum = 0;
		while (true) {
			LogUtil.writeLog("trynum" + trynum);
//			trynum++;
			if (trynum > 6) {
				LogUtil.writeLog("back " + trynum);
				break;
			}

			trynum++;
			root = AccessServiceUtil.getRootNode();
			if (root == null) {
				LogUtil.writeLog("back null");
				goActivityBack();
				continue;
			}

			if (!root.getPackageName().equals(currentPackage)) {
				LogUtil.writeLog("back break " + root.getPackageName() + "!=" + currentPackage);
				break;
			}
			goActivityBack();
		}

		root = AccessServiceUtil.getRootNode();
		if (root == null || root.getPackageName().equals(currentPackage)) {
			if (root == null) {
				LogUtil.writeLog("goback is null");
			} else {
				LogUtil.writeLog("goback " + currentPackage + " == " + root.getPackageName());
			}
			pressHome();
		}
	}

	public static void goCurrentActivityBack() {
		goCurrentActivityBack(null);
	}

	public static void stopCurrentPackage(String apkName) {
		String name = getCurrentPackage();
		if (name == null) return;

		if (!name.equals(apkName)) return;

		stopPackage(apkName);
	}

	public static boolean isinInstalling() {
		String packageName = AccessServiceUtil.getCurrentPackage();
		if (packageName == null) return true;
		packageName = packageName.toLowerCase();
		if (packageName.contains("packageinstaller")) return true;
		if (packageName.contains("com.oplus.appdetail")) return true;
		return false;
	}

	public static boolean waitInstalling(int timeout) {

		int duration = 0;
		do {

			if (isinInstalling()) return true;

			if (timeout == 0) return false;

			AndroidUtil.sleep(1000);

			duration++;
		} while (duration < timeout);

		return false;
	}

	public static boolean waitNoInstalling(int timeout) {
		int duration = 0;
		do {

			if (!isinInstalling()) return true;

			if (timeout == 0) return false;

			AndroidUtil.sleep(1000);

			duration++;
		} while (duration < timeout);

		return false;
	}

	public static boolean checkInstall(String apkName) {

		boolean inInstalling = waitInstalling(5);

		if (!inInstalling) {
			LogUtil.writeLog("no in install : " + AccessServiceUtil.getCurrentPackage());
			return false;
		}

		for (int ii = 0; ii < 100; ii++) {
			AccessibilityNodeInfo vvbtn = AccessUtil.getChild(new AccessSelector().text("重新安装"));
			if (vvbtn != null) {
				LogUtil.writeLog("click 重新安装!!!");
				ClickUtil.clickNode(vvbtn);
				AndroidUtil.sleep(500);
			}

			if (APKUtil.isAPKInstalled(apkName)) {
				waitNoInstalling(5);
				return true;
			}

			if (!isinInstalling()) return false;
			ClickUtil.closeAllDialogForTime(1000);
		}
		return false;
	}

	public static void sendBroadcast(String intent) {
	}
}
