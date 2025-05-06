package com.melon.phoneagent.autil;

/**
 * 手机的基类
 */


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.provider.Settings;
import android.util.Size;
import android.view.accessibility.AccessibilityNodeInfo;

import com.melon.util.APKUtil;
import com.melon.util.AndroidUtil;
import com.melon.util.FileUtil;
import com.melon.util.LogUtil;
import com.melon.util.StaticVarUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressLint("NewApi")
public class BasePhone {
	public static final int SCROLL_OK = 1;
	public static final int SCROLL_BREAK = 0;
	public static final int SCROLL_PASS = -1;

	static protected final String COLECTEDFINISH = "collectfinish";

	//厂家 和 机型
	static public String brand = Build.BRAND.toLowerCase();
	static public String model = Build.MODEL.toLowerCase();

	@Override
	public String toString() {
		return "DefPhone [brand=" + brand +
				",model=" + model +
				",miuiVer=" + Build.VERSION.SDK_INT +
				",autoRunString=" + APhoneParams.autoRunString +
				"]";
	}

	public void init() {
	}

	public BasePhone getBasePhone() {
		return this;
	}

	/**
	 * 设置返回
	 */
	public boolean backActivity(int num) {
		return false;
	}

	public void loadListFile() {
		APhoneParams.loadAppList();
		APhoneParams.loadAutoRunList();
		APhoneParams.loadDevInfoList();
	}

	public boolean canContinue() {
		if (AccessUtil.getChild(new AccessSelector().text("验证码*")) != null) {
			AccessServiceUtil.goCurrentActivityBack();
			return false;
		}

		if (AccessUtil.getChild(new AccessSelector().text("前往登录*")) != null) {
			AccessServiceUtil.goCurrentActivityBack();
			return false;
		}

		return true;
	}

	protected boolean openDeviceAdmin() {
		boolean isOpened = APKUtil.startActivity("com.android.settings", ".DeviceAdminSettings");
		if (!isOpened) isOpened = APKUtil.startActivity("com.android.settings", ".SecuritySettings");
		if (!isOpened) isOpened = APKUtil.startActivity("com.android.settings", ".OntimSettingsActivity");
		return isOpened;
	}

	final public boolean isPermissionOpened() {
		//打开权限
		if(!AndroidUtil.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) return false;

		if(!AndroidUtil.checkPermission(Manifest.permission.READ_PHONE_STATE)) return false;

		if(!AndroidUtil.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) return false;


		LogUtil.writeLog("check OK");
		return true;
	}
	/**
	 * 打开设备管理器
	 */
	final public void openDAdmin() {
		if (!openDeviceAdmin()) return;

		AndroidUtil.sleep(1000);
		if (!canContinue()) return;

		//OPPO在输入登录码的时候需要登录
		String packageName = AccessServiceUtil.getCurrentPackage();
		if (packageName == null) {
			AccessServiceUtil.goCurrentActivityBack();
			return;
		}
		if (!packageName.equals("com.android.settings")) {
			AccessServiceUtil.goCurrentActivityBack();
			return;
		}

		ScanListView.startScan(new ScanListView.ScanListViewSimpleInterface() {

			ArrayList<String> scanList = new ArrayList<>();

			@Override
			public boolean scanItem(AccessibilityNodeInfo item, int pos) {
				List<String> texts = AccessUtil.getAllText(item);
				if (texts == null) return true;

				//是否发现了
				boolean found = AccessUtil.canFindChildByText(item, APhoneParams.devInfoString);
				if (!found) {
					ScanListView.itemTimes = 1;
					return true;
				}

				String ss = texts.toString();
				if (scanList.contains(ss)) {
					ScanListView.itemTimes = 1;
					return true;
				}
				scanList.add(ss);

				AccessibilityNodeInfo checkbox = AccessUtil.getChild(item, new AccessSelector().checkable(true));
				if (checkbox == null) {
					if (texts.contains("激活") || texts.contains("开启")) {
						ScanListView.itemTimes = 1;
						return true;
					}
				} else {
					if (checkbox.isChecked()) {
						ScanListView.itemTimes = 1;
						return true;
					}
				}

				ClickUtil.clickNodeForNext(item);
//				AndroidUtil.sleep(500);

				AccessibilityNodeInfo button = AccessUtil.getChild(new AccessSelector().text("激活|启动"));
				if (button == null) button = AccessUtil.getChild(new AccessSelector().text("激活此设备管理员|启用此设备管理应用"));
				if (button == null) AccessUtil.waitNode(new AccessSelector().text("激活*"), 1000);
				if (button != null && button.getText().equals("解除激活")) button = null;

				if (button == null) {
					ScanListView.itemTimes = 1;
					if (ClickUtil.waitAndClickButton(new AccessSelector().text("取消"))) {
						return true;
					}

					AccessServiceUtil.goActivityBack();
					return true;
				}

				ClickUtil.clickButtonForNext(button);
				AndroidUtil.sleep(500);
				if (!canContinue()) return false;
				AccessibilityNodeInfo dbtn = AccessUtil.getChild(new AccessSelector().text("我知道了|继续激活"));
				if (dbtn != null) {
					ClickUtil.clickButtonForNext(dbtn);
//					AndroidUtil.sleep(500);
				}

				ScanListView.itemTimes += 1;
				return true;
			}
		});
		ScanListView.itemTimes = 1;

		AccessServiceUtil.goCurrentActivityBack();
	}
	public void closeDAdmin() {
		
	}
	/**
	 * 关闭设备管理器
	 */
	final public void closeDAdmin1() {
		if (!openDeviceAdmin()) return;

		AndroidUtil.sleep(1000);
		if (!canContinue()) return;

		ScanListView.itemTimes = 1;
		ScanListView.startScan(new ScanListView.ScanListViewSimpleInterface() {

			@Override
			public boolean scanItem(AccessibilityNodeInfo item, int pos) {
				List<String> texts = AccessUtil.getAllText(item);
				if (texts == null) return true;

				if (!item.isClickable()) return true;

				AccessibilityNodeInfo checkbox = AccessUtil.getChild(item, new AccessSelector().enable(true).checkable(true));
				if (checkbox == null) {
					if (texts.contains("未激活")) {
						ScanListView.itemTimes = 1;
						return true;
					}
					if (!texts.contains("激活") && !texts.contains("开启")) {
						ScanListView.itemTimes = 1;
						return true;
					}
				} else {
					if (!checkbox.isChecked()) {
						ScanListView.itemTimes = 1;
						return true;
					}
				}

				ClickUtil.clickNodeForNext(item);
//				AndroidUtil.sleep(500);

				AccessibilityNodeInfo button = AccessUtil.waitNode(new AccessSelector().text("取消激活*|解除激活*"), 1000);
				if (button == null) button = AccessUtil.getChild(new AccessSelector().text("取消"));
				if (button == null) button = AccessUtil.getChild(new AccessSelector().text("停用*"));
				if (button != null) {
					ClickUtil.clickButtonForNext(button);
					AndroidUtil.sleep(500);
				}

				button = AccessUtil.waitNode(new AccessSelector().text("确定"), 1000);
				if (button != null) {
					ClickUtil.clickNodeForNext(button);
//					AndroidUtil.sleep(500);
				}
				ScanListView.itemTimes += 1;

				return true;
			}
		});
		ScanListView.itemTimes = 1;
	}

	protected void showInstallWindow() {
		LogUtil.writeLog("showInstallWindow");
		AndroidUtil.sleep(500);
	}

	protected void showMainWindow() {
//		InstallUtil.showMe();
//		AndroidUtil.sleep(500);
	}

	//打开辅助点击开关
	final public void openAccessibility(final boolean isvivvonew) {
		if (!APKUtil.startAction(Settings.ACTION_ACCESSIBILITY_SETTINGS)) return;

		ClickUtil.closeAllDialogForTime(500);

		if(AndroidUtil.isXiaomi()){
			AccessibilityNodeInfo ii = AccessUtil.waitNode(new AccessSelector().text("更多*"));
			if (ii!=null){
				LogUtil.writeLog(">>found更多1");
				ClickUtil.clickButton(ii);
			}else{
				ii = AccessUtil.waitNode(new AccessSelector().text("已下载的服务"));
				if (ii!=null){
					LogUtil.writeLog(">>found更多2");
				ClickUtil.clickButton(ii);
			}else{
				LogUtil.writeLog("not>>found更多");
			}
		}
		}

		ScanListView.startScan("Light - FlashEx", new ScanListView.ScanListViewEasySimpleInterface() {

			@Override
			public boolean scanItem(AccessibilityNodeInfo item, int pos, String title, ScanListView.ItemsUtil iu) {
				ClickUtil.clickNode(item);
				AndroidUtil.sleep(500);

				//使用listview模式
				ScanListView.startScan(new ScanListView.ScanListViewSimpleInterface() {

					@Override
					public boolean scanItem(AccessibilityNodeInfo item, int pos) {
						AccessibilityNodeInfo cb = AccessUtil.getChild(item, new AccessSelector().checkable(true));
						if (isvivvonew) {
							cb = AccessUtil.getChild(item, new AccessSelector().resourceId("android:id/checkbox"));
						}

						if (cb == null) return true;
						if (isvivvonew) {
//							AccessUtil.eClick(cb);
							//点确定按钮
							ClickUtil.closeAllDialogForTime(1000);
							return true;
						}

						if (cb.isChecked()) return true;

						//点switch
						ClickUtil.clickButton(cb);

						if(AndroidUtil.isXiaomi()){
							AccessibilityNodeInfo ii = AccessUtil.waitNode(new AccessSelector().text("允许"));
							if (ii!=null){
								LogUtil.writeLog(">>found允许");
								ClickUtil.clickNode(ii);
							}else{
								LogUtil.writeLog("not>>found允许");
							}
						}
						//点确定按钮
						ClickUtil.closeAllDialogForTime(1000);
						return true;
					}

				});

				//再检测一遍
				AccessibilityNodeInfo cb = AccessUtil.getChild(new AccessSelector().checkable(true));
				if (cb != null && !cb.isChecked()) {
					if (ClickUtil.clickButton(cb)) {
						ClickUtil.closeAllDialogForTime(1000);
					}
				}
				AccessServiceUtil.goActivityBack();

				return false;
			}

//			@Override
//			public boolean scanItem(AccessibilityNodeInfo item, int pos) {
//				List<String> texts = AccessUtil.getAllText(item);
//				if(texts == null) return true;
//				LogUtil.writeLog(">>"+texts);
//				if(!texts.contains("Light - FlashEx"))return true;
//				ClickUtil.clickNode(item);
//				AndroidUtil.sleep(500);
//
//				//使用listview模式
//				ScanListView.startScan(new ScanListView.ScanListViewSimpleInterface() {
//
//					@Override
//					public boolean scanItem(AccessibilityNodeInfo item, int pos) {
//						AccessibilityNodeInfo cb = AccessUtil.getChild(item, new AccessSelector().checkable(true));
//						if (isvivvonew) {
//							cb = AccessUtil.getChild(item, new AccessSelector().resourceId("android:id/checkbox"));
//						}
//
//						if(cb == null)return true;
//						if (isvivvonew) {
//							AccessUtil.eClick(cb);
//							//点确定按钮
//							ClickUtil.closeAllDialogForTime(1000);
//							return true;
//						}
//
//						if(cb.isChecked()) return true;
//
//						//点switch
//						ClickUtil.clickButton(cb);
//
//						if(AndroidUtil.isXiaomi()){
//							AccessibilityNodeInfo ii = AccessUtil.waitNode(new AccessSelector().text("允许"));
//							if (ii!=null){
//								LogUtil.writeLog(">>found允许");
//								ClickUtil.clickNode(ii);
//							}else{
//								LogUtil.writeLog("not>>found允许");
//							}
//						}
//						//点确定按钮
//						ClickUtil.closeAllDialogForTime(1000);
//						return true;
//					}
//
//				});
//
//				//再检测一遍
//				AccessibilityNodeInfo cb = AccessUtil.getChild(new AccessSelector().checkable(true));
//				if(cb != null && !cb.isChecked()) {
//					if(ClickUtil.clickButton(cb)) {
//						ClickUtil.closeAllDialogForTime(1000);
//					}
//				}
//				AccessServiceUtil.goActivityBack();
//
//				return false;
//			}
		});
		if(AndroidUtil.isXiaomi()){
			AccessServiceUtil.goActivityBack();
		}
		AccessServiceUtil.goActivityBack();
	}

	//打开安装未知来源应用的权限
	public void openMANAGE_UNKNOWN_APP_SOURCES(String pkgname){
		if(!APKUtil.startActionWithPackage(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, pkgname)) {
			LogUtil.writeLog("can't open MANAGE_UNKNOWN_APP_SOURCES..");
			AccessServiceUtil.goCurrentActivityBack();
			return;
		}
//		AccessUtil.dumpNode2File("bbbb.txt");
//		AndroidUtil.sleep(1000);
		if (AccessUtil.waitNode(new AccessSelector().checkable(true).checked(false)) == null){
			if (AccessUtil.waitNode(new AccessSelector().className("android.widget.Switch").checked(false)) == null) {
				LogUtil.writeLog("can't find MANAGE_UNKNOWN_APP_SOURCES..");
				if (AndroidUtil.isOPPO()){
					AccessibilityNodeInfo title = AccessUtil.getChild(new AccessSelector().resourceId("com.android.settings:id/entity_header_title"));
					if (title!=null){
						LogUtil.writeLog("find title..");
						Rect iconRect = new Rect();
						title.getBoundsInScreen(iconRect);
						GestureUtil.click(iconRect.centerX(),iconRect.bottom+200);
					}
				}
				AccessServiceUtil.goCurrentActivityBack();
				return;
			}
		}
		AccessibilityNodeInfo node = AccessUtil.getChild(new AccessSelector().checkable(true).checked(false));
		if(node != null) {
			if (node.isClickable()){
				LogUtil.writeLog("click open MANAGE_UNKNOWN_APP_SOURCES..");
				ClickUtil.clickButton(node);
			}else{
				LogUtil.writeLog("2click open MANAGE_UNKNOWN_APP_SOURCES..");
				GestureUtil.click(node);
			}
		}else{
			node = AccessUtil.getChild(new AccessSelector().className("android.widget.Switch").checked(false));
			if(node != null) {
				LogUtil.writeLog("click open MANAGE_UNKNOWN_APP_SOURCES..");
				ClickUtil.clickButton(node);
			}else {
				LogUtil.writeLog("can't find MANAGE_UNKNOWN_APP_SOURCES..");
			}
		}
		AccessServiceUtil.goCurrentActivityBack();
	}

    //打开获取悬浮窗权限
	final public void openTopPermission(final String apkName, final String name) {
		if(!APKUtil.startActionWithPackage(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, apkName)) return;

        AndroidUtil.sleep(1000);
        if (Build.BRAND.equalsIgnoreCase("OnePlus")&&Build.VERSION.SDK_INT >29&&!AndroidUtil.isOPPO()){
			ScanListView.startScan(new ScanListView.ScanListViewSimpleInterface() {
				@Override
				public boolean scanItem(AccessibilityNodeInfo item, int pos) {
					List<String> texts = AccessUtil.getAllText(item);
					if(texts == null) return true;
					if(!texts.contains(name)) return true;
					boolean found = AccessUtil.canFindChildByText(item, "不允许");
					if(!found) {
						LogUtil.writeLog("opened1");
						return true;
					}
					ClickUtil.clickButtonForNext(item);
					AccessibilityNodeInfo node = AccessUtil.getChild(new AccessSelector().checkable(true).checked(false));
					if(node == null) {
						LogUtil.writeLog("opened2");
						AccessServiceUtil.goActivityBack();
						return false;
					}
					ClickUtil.clickButtonForNext(node);
					AccessServiceUtil.goActivityBack();
					return false;
				}
			});
	    }else if (AndroidUtil.isOPPO()) {
        	int trynum=0;
			do{
				if (trynum>10)break;
				AndroidUtil.sleep(500);
				if (AccessUtil.canFindChildByText(AccessServiceUtil.getRootNode(), "悬浮窗*")) {
					LogUtil.writeLog("found ....>>>>悬浮窗*");
					break;
				}
				LogUtil.writeLog("not found ....>>>>悬浮窗*");
				trynum++;
			} while (!AccessUtil.canFindChildByText(AccessServiceUtil.getRootNode(), "悬浮窗*"));
			boolean useGesture=ScanListView.useGesture;
			if (useGesture)ScanListView.useGesture=false;
            ScanListView.startScan(new ScanListView.ScanListViewSimpleInterface() {
                @Override
                public boolean scanItem(AccessibilityNodeInfo item, int pos) {
                    List<String> texts = AccessUtil.getAllText(item);
                    if(texts == null) return true;
                    if(!texts.contains(name)) return true;
                    //点击打开开关
                    AccessibilityNodeInfo node = AccessUtil.getChild(item,new AccessSelector().checkable(true).checked(false));
                    if(node == null) {
						LogUtil.writeLog("node == null");
                    	return true;
					}
					ClickUtil.clickButtonForNext(node);

//					AndroidUtil.sleep(500);
                    return false;
                }
            });
			if (useGesture)ScanListView.useGesture=true;
        }else if (AndroidUtil.isXiaomi()) {
			String miuiVerNew= AndroidUtil.getProp("ro.miui.ui.version.code").trim();
			int v=0;
			if (miuiVerNew!=null){
				v=Integer.parseInt(miuiVerNew);
			}
			LogUtil.writeLog("miuiVerNew:"+v);
			if (v<=8) {
				ScanListView.startScan(new ScanListView.ScanListViewSimpleInterface() {

					@Override
					public boolean scanItem(AccessibilityNodeInfo item, int pos) {
						boolean found = AccessUtil.canFindChildByContentDesc(item, "拒绝|询问");
						boolean hasText = AccessUtil.canFindChildByText(item, "悬浮窗*");
						if (!hasText) return true;
						if (!found) return true;
						ClickUtil.clickButton(item);
						AndroidUtil.sleep(500);
						AccessibilityNodeInfo dbtn = AccessUtil.getChild(new AccessSelector().text("允许"));
						if (dbtn != null) {
							ClickUtil.clickButtonForNext(dbtn);
							AndroidUtil.sleep(500);
						}
						return true;
					}
				});
			}else{
				if(Build.VERSION.SDK_INT>=30){
					ScanListView.gotoItem(name);
				}
				do {
					if (AccessUtil.waitNode(new AccessSelector().checkable(true).checked(false)) == null){
						if (AccessUtil.waitNode(new AccessSelector().resourceId("android:id/checkbox")) == null){
							break;
						}
					}
					AccessibilityNodeInfo node = AccessUtil.getChild(new AccessSelector().checkable(true).checked(false));
					if(node != null) {
						ClickUtil.clickButton(node);
					}else{
						node = AccessUtil.getChild(new AccessSelector().resourceId("android:id/checkbox"));
						if (APhoneParams.isThisPhoneInUiConfigList("useeclick")) {
//							AccessUtil.eClick(node);
						}else{
							GestureUtil.click(node);
						}
					}

				}while (false);
			}
		}else if (brand.contains("hisense")) {
            if(AccessUtil.waitNode(new AccessSelector().checkable(true).checked(false)) != null) {
            }
            AccessibilityNodeInfo node = AccessUtil.getChild(new AccessSelector().checkable(true).checked(false));
            if(node != null) {
            	GestureUtil.click(node);
            }
        }else if (AndroidUtil.isVIVO()&&Build.VERSION.SDK_INT >29){
        	do {
				if (APhoneParams.isRedo == 1) break;
			ScanListView.startScan(new ScanListView.ScanListViewSimpleInterface() {
				@Override
				public boolean scanItem(AccessibilityNodeInfo item, int pos) {
					List<String> texts = AccessUtil.getAllText(item);
					if(texts == null) return true;
					if(!texts.contains(name)) return true;
						AccessibilityNodeInfo node = AccessUtil.getChild(item, new AccessSelector().checkable(true));
					LogUtil.writeLog("1");
					if(node == null) {
						LogUtil.writeLog("2");
						node = AccessUtil.getChild(item, new AccessSelector().className("android.widget.Switch"));
					}
					if(node == null) {
						LogUtil.writeLog("3");
							Rect rect = new Rect();
							item.getBoundsInScreen(rect);
							GestureUtil.click(rect.right-150,rect.centerY());
							AndroidUtil.sleep(200);
							return false;
						}
					if (node.isChecked()) {
							LogUtil.writeLog("opened");
							AccessServiceUtil.goActivityBack();
							return false;
					}
					ClickUtil.clickButtonForNext(node);
					AccessServiceUtil.goActivityBack();
					return false;
				}
			});
			}while(false);
        }else{
			if(Build.VERSION.SDK_INT>=30){
				ScanListView.gotoItem(name);
			}
            do {
                if (AccessUtil.waitNode(new AccessSelector().checkable(true).checked(false)) == null){
                    if (AccessUtil.waitNode(new AccessSelector().resourceId("android:id/checkbox")) == null){
                        break;
                    }
                }
                AccessibilityNodeInfo node = AccessUtil.getChild(new AccessSelector().checkable(true).checked(false));
                if(node != null) {
					ClickUtil.clickButton(node);
                }else{
                    node = AccessUtil.getChild(new AccessSelector().resourceId("android:id/checkbox"));
					if (APhoneParams.isThisPhoneInUiConfigList("useeclick")) {
//						AccessUtil.eClick(node);
					}else{
						GestureUtil.click(node);
					}
                }

            }while (false);
        }

        AndroidUtil.sleep(500);
		AccessServiceUtil.goCurrentActivityBack();
    }

	/**
	 * 打开设备管理
	 */
	public boolean openSetting() {
		LogUtil.writeLog("open setting!!!!!!!!!!!!!!!!");
		if(!AccessServiceUtil.isPackage("com.android.settings")) {
			LogUtil.writeLog("not setting 1..");

			if(!APKUtil.startAction(Settings.ACTION_SETTINGS)) {
				LogUtil.writeLog("can't open setting 1..");
				return false;
			}
			if (AndroidUtil.isOPPO()) {
				AndroidUtil.sleep(100);
			} else {
				AndroidUtil.sleep(500);
			}
			if(AccessServiceUtil.getRootNode() == null) {
				AccessServiceUtil.stopPackage("com.android.settings");
				if(!APKUtil.startAction(Settings.ACTION_SETTINGS)) {
					LogUtil.writeLog("can't open setting 2..");
					return false;
				}
				if (AndroidUtil.isOPPO())
					AndroidUtil.sleep(100);
				else
				AndroidUtil.sleep(500);
			}
		}
		if(AccessUtil.getChild(new AccessSelector().inListView(false).text("设置")) == null) {
			LogUtil.writeLog("not setting 2..");
			AccessServiceUtil.goCurrentActivityBack("com.android.settings");
			if(!APKUtil.startAction(Settings.ACTION_SETTINGS)) {
				LogUtil.writeLog("can't open setting 3..");
				return false;
			}
		}

		AccessServiceUtil.waitForPackage("com.android.settings", 3000);
		LogUtil.writeLog("open setting OK");
		return true;
	}

	/**
	 * 处理设置函数
	 */
	Set<String> allCloseSettingItem = new HashSet<String>();
	final protected boolean getCloseSettingItemValue(String value) {
		if (allCloseSettingItem.contains(value)) return true;
		allCloseSettingItem.add(value);

		return false;
	}

	/**
	 * 处理设置函数
	 */
	protected int doCloseSettingItem(AccessibilityNodeInfo item) {
		return SCROLL_PASS;
	}

	final int doCloseDefSettingItem(AccessibilityNodeInfo item) {
		List<String> texts = AccessUtil.getAllText(item);
		if (texts == null) return SCROLL_PASS;

		if (texts.contains("更多设置")) {
			if (getCloseSettingItemValue("更多设置")) return SCROLL_OK;
			loopCloseSetting(item);
			return SCROLL_OK;
		}
		if (texts.contains("高级设置")) {
			if (getCloseSettingItemValue("高级设置")) return SCROLL_OK;
			loopCloseSetting(item);
			return SCROLL_OK;
		}

		if(texts.contains("安全") || texts.contains("系统安全") || texts.contains("指纹和安全")) {
			if(getCloseSettingItemValue("安全")) return SCROLL_OK;
			loopCloseSetting(item);
			return SCROLL_OK;
		}

		if(texts.contains("设备管理与凭证")) { //打开设备管理器
			if(getCloseSettingItemValue("设备管理与凭证")) return SCROLL_OK;
			loopCloseSetting(item);
			return SCROLL_OK;
		}

		if(!brand.equalsIgnoreCase("Xiaomi")&&!brand.equalsIgnoreCase("Redmi")&&!brand.equalsIgnoreCase("blackshark")) {
			if(texts.contains("开发者选项")) {
				if(getCloseSettingItemValue("开发者选项")) return SCROLL_OK;
				loopCloseSetting(item);
				return SCROLL_OK;
			}
			if(texts.contains("开启开发者选项")) {
				if(APhoneParams.isThisPhoneInUiConfigList("dontcloseusb"))  return SCROLL_OK;
				if(getCloseSettingItemValue("开启开发者选项")) return SCROLL_OK;

				ClickUtil.clickNodeForNext(item);
				AndroidUtil.sleep(500);
				return SCROLL_OK;
			}
		}

		return SCROLL_PASS;
	}

	/**
	 * 遍历设置项
	 */
	final void loopCloseSetting(AccessibilityNodeInfo item) {
		if (item != null) {
			if(!ClickUtil.clickNode(item, false)) return;
			AndroidUtil.sleep(500);
		}

		ScanListView.startScan(new ScanListView.ScanListViewSimpleInterface() {

			@Override
			public boolean scanItem(AccessibilityNodeInfo item, int pos) {
				int retVal = doCloseSettingItem(item);
				if (retVal == SCROLL_BREAK) return false;
				if (retVal >= SCROLL_OK) return true;

				retVal = doCloseDefSettingItem(item);
				if (retVal == SCROLL_BREAK) return false;
				if (retVal >= SCROLL_OK) return true;

				return true;
			}
		});

		if(item != null) AccessServiceUtil.goActivityBack();
	}

	/**
	 * 关闭设置
	 */
	final public void closeSetting() {
		if (!openSetting()) return;
		AndroidUtil.sleep(500);

		ScanListView.startSettingScan(new ScanListView.ScanListViewSimpleInterface() {

			@Override
			public boolean scanItem(AccessibilityNodeInfo item, int pos) {
				LogUtil.writeLog("pos =>>" + pos);
				int retVal = doCloseSettingItem(item);
				if (retVal == SCROLL_BREAK) return false;
				if (retVal >= SCROLL_OK) return true;

				retVal = doCloseDefSettingItem(item);
				if (retVal == SCROLL_BREAK) return false;
				if (retVal >= SCROLL_OK) return true;

				return true;
			}
		});

		AccessServiceUtil.goActivityBack();
	}

	/**
	 * 打开自启动程序
	 */
	public void openAutoRun() {
		LogUtil.writeLog("openAutoRun");
		ScanListView.startScan(new ScanListView.ScanListViewEasySimpleInterface() {
			@Override
			public boolean scanItem(AccessibilityNodeInfo item, int pos, String title, ScanListView.ItemsUtil iu) {
				AccessibilityNodeInfo checkbox = AccessUtil.getChild(item, new AccessSelector().checkable(true));
				if (checkbox == null) {
					LogUtil.writeLog("checkbox is null");
					return true;
				}
				//是否发现了
				LogUtil.writeLog(APhoneParams.autoRunString);
				boolean found = AccessUtil.canFindChildByText(item, APhoneParams.autoRunString);

				if (checkbox.isChecked() == found) return true;
				ClickUtil.clickButton(checkbox);
				return true;
			}
		});
//		ScanListView.startScan(new ScanListView.ScanListViewSimpleInterface() {
//
//			@Override
//			public boolean scanItem(AccessibilityNodeInfo item, int pos) {
//
//				AccessibilityNodeInfo checkbox = AccessUtil.getChild(item, new AccessSelector().checkable(true));
//				if (checkbox == null) {
//					LogUtil.writeLog("checkbox is null");
//					return true;
//				}
//				//是否发现了
//				LogUtil.writeLog(APhoneParams.autoRunString);
//				boolean found = AccessUtil.canFindChildByText(item, APhoneParams.autoRunString);
//
//				if (checkbox.isChecked() == found) return true;
//				ClickUtil.clickButton(checkbox);
//				return true;
//			}
//		});
	}

	/**
	 * 收尾操作
	 */
	final public void doClean() {
		LogUtil.writeLog("delete start");

		//清除文件
		try {
			if(AndroidUtil.isHuawei()) {
				File file= FileUtil.getSDFile("/Android/data/com.baidu.input_huawei/cache/");
				FileUtil.deleteFile(file);
				file=FileUtil.getSDFile("/Android/data/com.baidu.input_huawei/files/");
				FileUtil.deleteFile(file);
			}
			File file=FileUtil.getSDFile("/Pictures/com.baidu.searchbox.lite/");
			FileUtil.deleteFile(file);


           if(AndroidUtil.isVIVO()) {
				file= FileUtil.getSDFile("atmp");
				FileUtil.deleteFile(file);

                file= FileUtil.getSDFile("aatmp");
				FileUtil.deleteFile(file);

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		LogUtil.writeLog("delete end");

		if(APhoneParams.hasApp("com.android.app.notificationbar")) {
			FileUtil.writeFile(FileUtil.getSDFile(".onegot_profile.txt"), "yz-mkt1-ver37");
		}

		if(brand.contains("meizu")) {
			APhoneParams.closeKeyString = APhoneParams.closeKeyString.replaceAll("完成\\|", "");
		}

		AccessServiceUtil.pressHome();

		AccessServiceUtil.enableSleepActive(false);

		//关闭窗口5秒钟
		ClickUtil.closeAllDialogForTime(2000);

		//卸载装机工具
//		AccessServiceUtil.stopPackage(APhoneParams.toolAPKPKG);
//		AccessServiceUtil.runShell("pm uninstall " + APhoneParams.toolAPKPKG, 5000);

		//关闭窗口1秒钟
		ClickUtil.closeAllDialogForTime(1000);

		//关闭后台进程
		if (APhoneParams.clearBack == 1) {
			doCloseRecents();
		}

//		//删除 InstallRun
//		if (NMAPKUtil.isAPKInstalled(APhoneParams.toolAPKPKG)) {
//			LogUtil.writeLog("uninstall fail");
//			int r = PhoneUtil.triggerUninstall(APhoneParams.toolAPKPKG);
//			LogUtil.writeLog("uninstall again"+r);
//			ClickUtil.closeAllDialogForTime(1000);
//		}

//		String content = FileUtil.readFile(new File("/data/local/tmp/channel"));
//		String ainfoContent = FileUtil.readFile(new File("/data/local/tmp/ainfo"));

		//删除所有文件
//		FileUtil.clearDir(new File("/data/local/tmp/"));
//
//		if(content != null) {
//			FileUtil.writeFile(new File("/data/local/tmp/channel"), content);
//			ShellUtil.runShell("chmod 777 /data/local/tmp/channel",500);
//		}
//		if(ainfoContent != null) {
//			FileUtil.writeFile(new File("/data/local/tmp/ainfo"), ainfoContent);
//			ShellUtil.runShell("chmod 777 /data/local/tmp/ainfo",500);
//		}
//
//		FileUtil.writeFile(new File("/data/local/tmp/.finish"), "");
//		ShellUtil.runShell("chmod 777 /data/local/tmp/.finish",500);

		//设置屏幕休眠时间
		AccessServiceUtil.runShell("settings put system screen_off_timeout 30000",3000);
		if(brand.contains("zte")) {
			AccessServiceUtil.runShell("settings put system screen_off_timeout 30000",5000);
		}

		//打开开机引导
		FileUtil.writeFile(FileUtil.getSDFile("HWHome/showguide"), "1");
		if (APhoneParams.openGuide == 1) {
			if (APhoneParams.clearBack == 1) AndroidUtil.sleep(1000);
			if (AndroidUtil.isVIVO()){
				StaticVarUtil.mUseExternalFilesDir=true;
			}

			FileUtil.writeFile(FileUtil.getSDFile(".sysZero"), "");
			FileUtil.writeFile(FileUtil.getSDFile(".sysOne"), "");
		}

		//关闭窗口5秒钟
		ClickUtil.closeAllDialogForTime(5000);

		if(AndroidUtil.isHuawei()) {
			AccessServiceUtil.runShell("settings put global wifi_sleep_policy 2",5000);
		}
		AccessServiceUtil.runShell("dumpsys batterystats --reset",5000);
		//关闭飞行模式
//		if(!APhoneParams.isThisPhoneInUiConfigList("dontcloseairplane")) {
//			AccessServiceUtil.runShell("settings put global airplane_mode_on 0",5000);
//		}
		//OPPO 手机，恢复界面   配置了机型+打开了关机开关（默认打开，不需要开机引导的关闭）+不使用dpm
		if(APhoneParams.isThisPhoneInUiConfigList("openboot") && APhoneParams.doShutdown) {
			AccessServiceUtil.runShell("am start -n com.oppo.engineermode/.PowerOff",2000);
			AccessServiceUtil.runShell("am start -n com.android.engineeringmode/.PowerOff",2000);
			AndroidUtil.sleep(5000);
		}
		if(brand.contains("meizu")) {
			AndroidUtil.sleep(5000);
		}

		//关闭USB
		do {
			if(Build.VERSION.SDK_INT >= 29) break;
			if(APhoneParams.isThisPhoneInUiConfigList("dontcloseusb")) break;

			AccessServiceUtil.runShell("settings put global adb_enabled 0",5000);

			if(AndroidUtil.isVIVO()) {
				Size size = AccessServiceUtil.getRootSize();
				int x = size.getWidth() / 3;
				int y=5;
				LogUtil.writeLog("click"+x+":"+y);
				GestureUtil.click(x, y);
			}
		} while(false);

		//关机
		do {
			if(!APhoneParams.doShutdown) break;
			if(APhoneParams.isThisPhoneInUiConfigList("dontshutdown")) break;

			AccessServiceUtil.runShell("reboot -p",2000);
		} while (false);

	}

	boolean tryagain=true;
	public  void clickOpenwifi(){
		String wifiname = FileUtil.readFile(FileUtil.getSDFile(".wifi"));
		String key="已停用*|已保存*|不可上网*|无法自动连接*";
		LogUtil.writeLog("key："+key);
		if (wifiname!=null){
			LogUtil.writeLog("wifi："+wifiname);
			key=key+"|"+wifiname+"*";
			LogUtil.writeLog("key："+key);
		}

		String finalKey = key;
		class _WifiIntf implements ScanListView.ScanListViewEasySimpleInterface {
			public boolean foundWifi = false;
			@Override
			public boolean scanItem(AccessibilityNodeInfo item, int pos, String title, ScanListView.ItemsUtil iu) {
				List<String> texts = AccessUtil.getAllText(item, true);
				LogUtil.writeLog("found connected wifi:" + texts);
				if(AccessUtil.getChild(item,new AccessSelector().text("已连接*")) != null) {
					foundWifi = true;
					LogUtil.writeLog("alreay connect!!");
					return false;
				}

				if (AndroidUtil.isOPPO()){
					LogUtil.writeLog("isOPPO wifi0");
					if (texts.contains("已保存网络"))return true;
				}

				if (AndroidUtil.isXiaomi()){
					LogUtil.writeLog("isXiaomi wifi0");
					if (texts.contains("已保存的WLAN"))return true;
				}
				if (Build.BRAND.equalsIgnoreCase("samsung")){
					boolean r = ClickUtil.clickNodeForNext(item,new AccessSelector().text(finalKey));
					LogUtil.writeLog("click found wifi" + r);
					foundWifi = true;
				}else {
					boolean r = ClickUtil.clickNodeForNext(item);
					LogUtil.writeLog("click found wifi" + r);
					foundWifi = true;
				}

				if (AndroidUtil.isVIVO()){
					AccessibilityNodeInfo node=AccessUtil.getChild(new AccessSelector().text("断开连接"));
					if (node!=null) {
						GestureUtil.click(node);
						ClickUtil.waitAndClickButton(new AccessSelector().text("确定|断开"));
						LogUtil.writeLog("click 断开连接");
						clickOpenwifi();
						return false;
					}else{
						LogUtil.writeLog("not found 断开连接");
						node=AccessUtil.getChild(new AccessSelector().text("加入网络"));
						if (node!=null) {
							GestureUtil.click(node);
							LogUtil.writeLog("click 加入网络");
						}
					}
				}

				AndroidUtil.sleep(500);
				AccessibilityNodeInfo node=AccessUtil.getChild(new AccessSelector().text("连接"));
				if (node!=null) {
					GestureUtil.click(node);
					LogUtil.writeLog("click dialog");
				}else{
					LogUtil.writeLog("not found");
				}
				return false;
			}
		};
		_WifiIntf intf = new _WifiIntf();
		ScanListView.startScan(key, intf);

		if(!intf.foundWifi) {
			LogUtil.writeLog("wifi not found wifi!");
			AccessServiceUtil.goCurrentActivityBack();
			return;
		}

		if(AccessUtil.waitNode(new AccessSelector().text("已连接*|不可上网*|无法访问互联网*|无互联网连接|已在没有互联网的情况下连接*|互联网可能不可用"), 5000) != null) {
			if (Build.BRAND.equalsIgnoreCase("samsung")){
				AccessibilityNodeInfo flag=AccessUtil.getChild(new AccessSelector().text("互联网可能不可用"));
				if (flag!=null){
					LogUtil.writeLog("alert：互联网可能不可用");
					ClickUtil.waitAndClickButtonForNext(new AccessSelector().contentDesc("始终连接"));
				}
			}

			AccessibilityNodeInfo fff=AccessUtil.getChild(new AccessSelector().text("已保存 (不可上网)"));
			if (fff!=null){
				LogUtil.writeLog("found 已保存");
				if(AccessUtil.waitNode(new AccessSelector().text("已连接*"), 5000) != null) {
					LogUtil.writeLog("wifi connected!");
				}
			}else{
				LogUtil.writeLog("wifi connected!");
				if (Build.BRAND.equalsIgnoreCase("Coolpad")){
					if(AccessUtil.waitNode(new AccessSelector().text("已连接*"), 5000) != null) {
						LogUtil.writeLog("wifi connected!");
					}else{
						LogUtil.writeLog("wifi connected fail!"+tryagain);
						if (tryagain){
							tryagain=false;
							clickOpenwifi();
							return;
						}
					}
				}
			}
		}else {
			LogUtil.writeLog("wifi connected fail!"+tryagain);
			if (tryagain){
				tryagain=false;
				clickOpenwifi();
				return;
			}
		}
		AccessServiceUtil.goCurrentActivityBack();
	}

	public void doOpenIt() {
		LogUtil.writeLog("doOpenIt>>>>>>>>>>>");
		AccessServiceUtil.pressHome();
		LogUtil.writeLog("open>>>>>>>>>>>");
		ClickUtil.closeAllDialogForTime(1000);
		boolean found = false;
		for(int ii=0;ii<6;ii++) {
			AccessibilityNodeInfo node = AccessUtil.getChild(new AccessSelector().text("立即授权|允许|始终允许|同意|同意并进入|同意并继续|同意并开启服务|立即开启|确定|我知道了|知道了|下一步|开始|好的|继续|马上体验|立即体验"));
			if(node == null) node = AccessUtil.getChild(new AccessSelector().text("同意"));
			if(node == null) node = AccessUtil.getChild(new AccessSelector().resourceId("android:id/button1"));
			if(node == null) node = AccessUtil.getChild(new AccessSelector().text("同意*"));
			if(node == null) node = AccessUtil.getChild(new AccessSelector().text("进入*"));
			if(node == null) node = AccessUtil.getChild(new AccessSelector().text("使用时允许*"));
			if(node == null) node = AccessUtil.getChild(new AccessSelector().text("运行允许*"));
			if(node == null) node = AccessUtil.getChild(new AccessSelector().text("允许*"));
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
		AccessServiceUtil.goActivityBack();
		AndroidUtil.sleep(500);
		LogUtil.writeLog("end>>>>>>>>>>>");
		AccessServiceUtil.pressHome();
	}

	public void doOpenWifi() {
		String pkg="com.android.settings";
		if (AndroidUtil.isOPPO())pkg="com.coloros.wirelesssettings";
		if(!AccessServiceUtil.startActionAndWait(Settings.ACTION_WIFI_SETTINGS, pkg, 5000)) {
			if (AndroidUtil.isOPPO()) {
				pkg="com.oplus.wirelesssettings";
				if (!AccessServiceUtil.startActionAndWait(Settings.ACTION_WIFI_SETTINGS,pkg , 5000)) {
					LogUtil.writeLog("open wifi settings fail!");
					return;
				}
			}else{
				LogUtil.writeLog("open wifi settings fail!");
				return;
			}
		}
		LogUtil.writeLog("Open doOpenWifi ACTION_WIFI_SETTINGS ok");

		boolean oflag=true;
		AccessibilityNodeInfo sw = null;
		if(AndroidUtil.isOPPO()) {
			sw = AccessUtil.getChild(new AccessSelector().resourceId(pkg+":id/switchWidget"));
			if(sw == null) sw =AccessUtil.getChild(new AccessSelector().resourceId("android:id/switch_widget"));
		}
		if(sw == null) sw = AccessUtil.getChild(new AccessSelector().checkable(true));
		if(sw == null) {
			sw = AccessUtil.getChild(new AccessSelector().className("android.widget.Switch"));
			if (sw == null) {
				LogUtil.writeLog("can't found check");
				if (AndroidUtil.isHuawei()) {
					sw = AccessUtil.getChild(new AccessSelector().resourceId("com.android.settings:id/swich_bar_layout"));
					AccessibilityNodeInfo fff=AccessUtil.getChild(new AccessSelector().text("为提高位置信息的精确度*"));
					if (sw == null||fff==null){
						LogUtil.writeLog("Huawei swich_bar_layout not found!");
						AccessServiceUtil.goCurrentActivityBack();
						return;
					}
					oflag=false;
					ClickUtil.clickButtonForNext(sw);
					AndroidUtil.sleep(1000);
					fff=AccessUtil.getChild(new AccessSelector().text("为提高位置信息的精确度*"));
					if (fff!=null){
						LogUtil.writeLog("swich_bar_layout click fail use rect click");
						Rect rect = new Rect();
						sw.getBoundsInScreen(rect);
						GestureUtil.click(rect.right-100, rect.centerY());
					}
				}else{
					AccessServiceUtil.goCurrentActivityBack();
					return;
				}
			}
		}
		if(oflag&&sw.isChecked()) {
			LogUtil.writeLog("switch is checked!");
			if (AndroidUtil.isVIVO()){
				if(AccessUtil.waitNode(new AccessSelector().text("已连接*|不可上网*|无法访问互联网*|无互联网连接"), 1000) != null) {
					LogUtil.writeLog("wifi connected1!");
					AccessServiceUtil.goCurrentActivityBack();
					return;
				}
				clickOpenwifi();
				return;
			}
			AccessServiceUtil.goCurrentActivityBack();
			return;
		}

		LogUtil.writeLog("switch is ready!");

		if(oflag&&!ClickUtil.clickButtonForNext(sw)) {
			LogUtil.writeLog("open switch fail!");
			AccessServiceUtil.goCurrentActivityBack();
			return;
		}
		ClickUtil.waitAndClickButton(new AccessSelector().text("同意并继续"));
		LogUtil.writeLog("after click doOpenWifi ACTION_WIFI_SETTINGS");
		AccessUtil.waitSleepAndStill();

		if(AndroidUtil.isVIVO()){
			clickOpenwifi();
			return;
		}
		if (Build.BRAND.equalsIgnoreCase("honor")){
			clickOpenwifi();
			return;
		}

		//vivo y35等待引发线程挂起 数据上传出现问题
		if(AccessUtil.waitNode(new AccessSelector().text("已连接*|不可上网*|无法访问互联网*|无互联网连接|已在没有互联网的情况下连接*"), 10000) != null) {
			AccessibilityNodeInfo fff=AccessUtil.getChild(new AccessSelector().text("已保存 (不可上网)|已保存，加密 (不可上网)"));
			if (fff!=null){
				LogUtil.writeLog("found 已保存:"+fff.getText());
			}else {
				fff=AccessUtil.getChild(new AccessSelector().text("已保存"));
				if (fff!=null){
					LogUtil.writeLog("found 已保存:"+fff.getText());
					String text = fff.getText().toString();
					if (text.contains("不可上网")&&text.contains("加密")){
						LogUtil.writeLog("new condition！");
					}else{
						LogUtil.writeLog("wifi connected11!");
						AccessServiceUtil.goCurrentActivityBack();
						return;
					}
				}else{
					LogUtil.writeLog("wifi connected11!");
					AccessServiceUtil.goCurrentActivityBack();
					return;
				}
			}
		}

		clickOpenwifi();

	}

	boolean flag = false;
	/**
	 * 关闭wifi通用代码
	 */
	public void doCloseWifi() {
//		if (AndroidUtil.isVIVO()){
//			String pkg="com.android.settings";
//			AccessServiceUtil.startActionAndWait(Settings.ACTION_WIFI_SETTINGS, pkg, 5000);
//			LogUtil.writeLog("Open doRemoveWifi ACTION_WIFI_SETTINGS ok");
//		}else {
		if (!doOpenWifiSetting(false)) {
			LogUtil.writeLog("doOpenWifiSettting fail!");
			return;
		}
//		}
		//vivo挂起
		if (AndroidUtil.isVIVO()) {
			AndroidUtil.sleep(500);
		}else{
			AccessUtil.waitSleepAndStill();
		}

		if (AndroidUtil.isVIVO()) {
			class _WifiIntf implements ScanListView.ScanListViewEasySimpleInterface {
				@Override
				public boolean scanItem(AccessibilityNodeInfo item, int pos, String title, ScanListView.ItemsUtil iu) {
					List<String> texts = AccessUtil.getAllText(item, true);
					LogUtil.writeLog("found connected wifi:" + texts);
					if (texts != null && texts.size() > 0) {
						String wifiname = texts.get(0);
						if (texts.size() > 1) wifiname = wifiname + "|" + texts.get(1);
						LogUtil.writeLog("===>" + wifiname + "<===");
						FileUtil.writeFile(FileUtil.getSDFile(".wifi"), wifiname);
					}
					return false;
				}
			}
			;
			_WifiIntf intf = new _WifiIntf();
			ScanListView.startScan("已连接*", intf);

			AccessibilityNodeInfo listView = AccessUtil.getChild(new AccessSelector().scrollable(true));
			int times = 0;
			LogUtil.writeLog("===>" + (listView != null) + "<===");
			while (listView != null && listView.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)) {
				LogUtil.writeLog("===>" + times + "<===");
				AndroidUtil.sleep(200);
				times++;
				if (times > 10) break;
			}
		}

		if (Build.BRAND.equalsIgnoreCase("samsung")){
			class _WifiIntf implements ScanListView.ScanListViewEasySimpleInterface {
				@Override
				public boolean scanItem(AccessibilityNodeInfo item, int pos, String title, ScanListView.ItemsUtil iu) {
					List<String> texts = AccessUtil.getAllText(item, true);
					LogUtil.writeLog("found connected wifi:" + texts);
					if (texts != null && texts.size() > 0) {
						String wifiname = texts.get(0);
						LogUtil.writeLog("===>" + wifiname + "<===");
						FileUtil.writeFile(FileUtil.getSDFile(".wifi"), wifiname);
					}
					return false;
				}
			}
			;
			_WifiIntf intf = new _WifiIntf();
			ScanListView.startScan("已在没有互联网的情况下连接*", intf);
		}

		if (AndroidUtil.isXiaomi()){
			class _WifiIntf implements ScanListView.ScanListViewEasySimpleInterface {
				@Override
				public boolean scanItem(AccessibilityNodeInfo item, int pos, String title, ScanListView.ItemsUtil iu) {
					List<String> texts = AccessUtil.getAllText(item, true);
					LogUtil.writeLog("found connected wifi:" + texts);
					if (texts != null && texts.size() > 0) {
						String wifiname = texts.get(0).split(",")[0];
						LogUtil.writeLog("===>" + wifiname + "<===");
						FileUtil.writeFile(FileUtil.getSDFile(".wifi"), wifiname);
					}
					return false;
				}
			}
			;
			_WifiIntf intf = new _WifiIntf();
			ScanListView.startScan(",已连接*", intf);
			AccessibilityNodeInfo listView = AccessUtil.getChild(new AccessSelector().scrollable(true));
			int times = 0;
			LogUtil.writeLog("===>" + (listView != null) + "<===");
			while (listView != null && listView.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)) {
				LogUtil.writeLog("===>" + times + "<===");
				AndroidUtil.sleep(200);
				times++;
				if (times > 10) break;
			}
		}

		if (AndroidUtil.isOPPO()) {
			class _WifiIntf implements ScanListView.ScanListViewEasySimpleInterface {
				@Override
				public boolean scanItem(AccessibilityNodeInfo item, int pos, String title, ScanListView.ItemsUtil iu) {
					List<String> texts = AccessUtil.getAllText(item, true);
					LogUtil.writeLog("found connected wifi:" + texts);
					if (texts != null && texts.size() > 0) {
						String wifiname = texts.get(0);
						LogUtil.writeLog("===>" + wifiname + "<===");
						FileUtil.writeFile(FileUtil.getSDFile(".wifi"), wifiname);
					}
					return false;
				}
			}
			;
			_WifiIntf intf = new _WifiIntf();
			ScanListView.startScan("不安全|无法访问互联网|已连接|不可上网*", intf);

			AccessibilityNodeInfo listView = AccessUtil.getChild(new AccessSelector().scrollable(true));
			int times = 0;
			LogUtil.writeLog("===>" + (listView != null) + "<===");
			while (listView != null && listView.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)) {
				LogUtil.writeLog("===>" + times + "<===");
				AndroidUtil.sleep(200);
				times++;
				if (times > 10) break;
			}
		}

		if (AndroidUtil.isHuawei()) {
			flag = false;
			ScanListView.startScan(new ScanListView.ScanListViewSimpleInterface() {

				@Override
				public boolean scanItem(AccessibilityNodeInfo item, int pos) {
					List<String> texts = AccessUtil.getAllText(item, true);
					if (texts == null) {
						LogUtil.writeLog("scan=> null found!");
						return true;
					}
					LogUtil.writeLog("scan=>" + texts + "<");
					if ((texts.contains("已连接")||texts.contains("已连接 (不可上网)")) && !texts.contains("已连接 WLAN")) {
						flag=true;
						LogUtil.writeLog("found connected wifi:" + texts);
						if (texts != null && texts.size() > 0) {
							String wn = texts.get(0);
							if (texts.size() == 3) {
								wn = texts.get(1);
								LogUtil.writeLog("3===>" + wn + "<===");
							} else {
								String wifiname = texts.get(0);
								wn = wifiname.split(",")[0];
								LogUtil.writeLog("0===>" + wifiname + "<===");
							}

							LogUtil.writeLog("===>" + wn + "<===");
							FileUtil.writeFile(FileUtil.getSDFile(".wifi"), wn);
						}
						return false;
					}

					return true;
				}
			});

//			class _WifiIntf implements ScanListView.ScanListViewEasySimpleInterface {
//				@Override
//				public boolean scanItem(AccessibilityNodeInfo item, int pos, String title, ScanListView.ItemsUtil iu) {
//					List<String> texts = AccessUtil.getAllText(item, true);
//					LogUtil.writeLog("found connected wifi:" + texts);
//					if (texts!=null&&texts.size()>0){
//						String wifiname=texts.get(0);
//						LogUtil.writeLog("===>"+wifiname+"<===");
//						FileUtil.writeFile(FileUtil.getSDFile(".wifi"), wifiname);
//					}
//					return false;
//				}
//			}
			;
//			_WifiIntf intf = new _WifiIntf();
//			ScanListView.startScan("不可上网*", intf);

			AccessibilityNodeInfo listView = AccessUtil.getChild(new AccessSelector().scrollable(true));
			int times = 0;
			LogUtil.writeLog("===>" + (listView != null) + "<===");
			while (listView != null && listView.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)) {
				LogUtil.writeLog("===>" + times + "<===");
				AndroidUtil.sleep(200);
				times++;
				if (times > 10) break;
			}
		}
		AccessibilityNodeInfo sw = null;
		if (AndroidUtil.isOPPO()) {
			sw = AccessUtil.getChild(new AccessSelector().resourceId("com.coloros.wirelesssettings:id/switchWidget"));
			if(sw == null) sw = AccessUtil.getChild(new AccessSelector().resourceId("com.oplus.wirelesssettings:id/switchWidget|android:id/switch_widget"));
		}
		if (sw == null) sw = AccessUtil.getChild(new AccessSelector().checkable(true));
		if (sw == null) {
			sw = AccessUtil.getChild(new AccessSelector().className("android.widget.Switch"));
			if (sw == null) {
				LogUtil.writeLog("wifi switch not found!");
				if (AndroidUtil.isHuawei()) {
					LogUtil.writeLog("wifi switch not found!"+flag);
					sw = AccessUtil.getChild(new AccessSelector().resourceId("com.android.settings:id/swich_bar_layout"));
					if (sw != null&&flag){
						ClickUtil.clickButtonForNext(sw);
						AndroidUtil.sleep(1000);
						AccessibilityNodeInfo fff=AccessUtil.getChild(new AccessSelector().text("为提高位置信息的精确度*"));
						if (fff==null){
							LogUtil.writeLog("swich_bar_layout click fail use rect click");
							Rect rect = new Rect();
							sw.getBoundsInScreen(rect);
							GestureUtil.click(rect.right-100, rect.centerY());
						}
					}else{
						LogUtil.writeLog("Huawei swich_bar_layout not found!");
					}
				}
				AccessServiceUtil.goCurrentActivityBack();
				return;
			}
		}
		if (!sw.isChecked()) {
			LogUtil.writeLog("wifi switch not checked!");
			AccessServiceUtil.goCurrentActivityBack();
			return;
		}
		ClickUtil.clickButtonForNext(sw);

		if(AndroidUtil.isVIVO()){
			String wifiname = FileUtil.readFile(FileUtil.getSDFile(".wifi"));
			String key = "已保存*|不可上网*";
			LogUtil.writeLog("key：" + key);
			if (wifiname != null) {
				LogUtil.writeLog("wifi：" + wifiname);
				key = key + "|" + wifiname + "*";
				LogUtil.writeLog("key：" + key);
			}

			class _WifiIntf implements ScanListView.ScanListViewEasySimpleInterface {
				@Override
				public boolean scanItem(AccessibilityNodeInfo item, int pos, String title, ScanListView.ItemsUtil iu) {
					List<String> texts = AccessUtil.getAllText(item, true);
					LogUtil.writeLog("found connected wifi:" + texts);
					boolean r = ClickUtil.clickNodeForNext(item);
					LogUtil.writeLog("click found wifi" + r);

					ClickUtil.waitAndClickButtonForNext(new AccessSelector().text("断开连接"));
					AndroidUtil.sleep(500);
					ClickUtil.waitAndClickButtonForNext(new AccessSelector().text("确定"));
					return false;
				}
			}
			_WifiIntf intf = new _WifiIntf();
			ScanListView.startScan(key, intf);
	}

		LogUtil.writeLog("after click doCloseWifi ACTION_WIFI_SETTINGS");

         //vivo挂起
		if (AndroidUtil.isVIVO()) {
			AndroidUtil.sleep(500);
		}else{
			ClickUtil.closeAllDialogForTime(1000);
		}
		if(AndroidUtil.isOPPO()){
			AccessServiceUtil.goCurrentActivityBack("com.coloros.wirelesssettings");
			AccessServiceUtil.goCurrentActivityBack("com.oplus.wirelesssettings");
		}
		else AccessServiceUtil.goCurrentActivityBack();
	}

	public void doRemoveWifi() {
		String pkg="com.android.settings";
		if (AndroidUtil.isOPPO())pkg="com.coloros.wirelesssettings";
		AccessServiceUtil.startActionAndWait(Settings.ACTION_WIFI_SETTINGS, pkg, 5000);
		LogUtil.writeLog("Open doRemoveWifi ACTION_WIFI_SETTINGS ok");
		ScanListView.startScan("已连接*|不可上网*|无法访问互联网*|无互联网连接", new ScanListView.ScanListViewEasySimpleInterface() {

			@Override
			public boolean scanItem(AccessibilityNodeInfo item, int pos, String title, ScanListView.ItemsUtil iu) {
				LogUtil.writeLog(title);
				if (title.equals("已连接 WLAN"))return true;
				LogUtil.writeLog("found wifi");
				ClickUtil.clickNodeForNext(item);
				LogUtil.writeLog("click found wifi");
				ClickUtil.waitAndClickButtonForNext(new AccessSelector().text("取消保存|不保存|移除此网络|删除|忘记网络|删除网络|清除"));
				LogUtil.writeLog("click dialog");
				AndroidUtil.sleep(500);
				ClickUtil.waitAndClickButtonForNext(new AccessSelector().text("删除|移除|确定|断开连接"));
				LogUtil.writeLog("click twice");
				return false;
			}
		});
		AccessServiceUtil.goCurrentActivityBack();
	}

	public boolean doOpenWifiSetting(boolean isOpen) {
		int backtime=1;
		String pkg="com.android.settings";
		if (AndroidUtil.isOPPO())pkg="com.coloros.wirelesssettings";
		if(!AccessServiceUtil.startActionAndWait(Settings.ACTION_WIFI_SETTINGS, pkg, 5000)) {
			if (AndroidUtil.isOPPO()) {
				pkg="com.oplus.wirelesssettings";
				if (!AccessServiceUtil.startActionAndWait(Settings.ACTION_WIFI_SETTINGS,pkg , 5000)) {
					LogUtil.writeLog("open wifi settings fail!");
					return false;
				}
			}else{
				LogUtil.writeLog("open wifi settings fail!");
				return false;
			}
		}
		LogUtil.writeLog("Open doOpenWifiSetting ACTION_WIFI_SETTINGS ok");

		do {
			String text= "WLAN+|WLAN助理|WLAN 助理|网络加速|设置*|网络助理";
			if (Build.BRAND.equalsIgnoreCase("honor")){
				text= "WLAN+|WLAN助理|WLAN 助理|设置*|网络助理";
			}
			AccessibilityNodeInfo item = AccessUtil.waitNode(new AccessSelector().text(text));
			if (AndroidUtil.isOPPO()&&item!=null&&item.getText().toString().contains("WLAN 设置")){
				LogUtil.writeLog("OPPO new text");
				item=null;
			}
			if (AndroidUtil.isXiaomi()&&item!=null&&item.getText().toString().contains("更多设置")){
				LogUtil.writeLog("Xiaomi new text");
				item=null;
			}
			if(item == null) {
				item = AccessUtil.getChild(new AccessSelector().contentDesc("更多选项"));
				if(item == null) {
					if(!ScanListView.gotoItem("高级设置|WLAN 高级设置")) {
					LogUtil.writeLog("not found wifi+ item!");
					break;
					}else{
						backtime=2;
				}
				}else {
				ClickUtil.clickButtonForNext(item);
				AccessUtil.waitSleepAndStill();
				}

				item = AccessUtil.getChild(new AccessSelector().text("WLAN+|WLAN助理|WLAN 助理|设置*"));
				if(item == null) {
					AccessServiceUtil.goActivityBack();
					break;
				}else{
					LogUtil.writeLog("second!"+item.getText());
				}
			}
			LogUtil.writeLog("?"+item.getText().toString());
			ClickUtil.clickButtonForNext(item);
			AccessUtil.waitSleepAndStill();

			if(AndroidUtil.isHuawei()) {
				AccessibilityNodeInfo flag1 = AccessUtil.getChild(new AccessSelector().text("提升网络智能连接体验*"));
				LogUtil.writeLog("1!"+(flag1!=null));
				String ft;
				if (isOpen)ft="已关闭";
				else ft="已开启";
				AccessibilityNodeInfo flag2 = AccessUtil.getChild(new AccessSelector().text(ft));
				LogUtil.writeLog("2!"+(flag2!=null));
				if (flag1!=null&&flag2!=null){
					item = AccessUtil.getChild(new AccessSelector().text("WLAN+"));
					if(item == null) {
						AccessServiceUtil.goActivityBack();
						break;
					}
					ClickUtil.clickButtonForNext(item);
					AccessUtil.waitSleepAndStill();
					backtime=2;
				}
			}

			if(AndroidUtil.isXiaomi()) {
				if(!ScanListView.gotoItem("智能选网")) {
					LogUtil.writeLog("not found 智能选网 item!");

					AccessUtil.waitSleepAndStill();
					AccessibilityNodeInfo listView = AccessUtil.getChild(new AccessSelector().resourceId(pkg+":id/recycler_view"));
					if (listView==null) listView = ScanListView.getListView();
					int times = 0;
					while(listView.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)) {
						AndroidUtil.sleep(200);
						times ++;
						if(times > 10) break;
					}
				}else{
					backtime=2;
				}
			}

			List<AccessibilityNodeInfo> sws = null;
			if(AndroidUtil.isOPPO()) {
				LogUtil.writeLog("oppo new condition");
				sws = AccessUtil.getChildren(new AccessSelector().resourceId(pkg+":id/switchWidget|android:id/switch_widget"));
			}
			if(sws == null) {
				sws = AccessUtil.getChildren(new AccessSelector().checkable(true));
			}

			if(sws == null) {
				sws = AccessUtil.getChildren(new AccessSelector().className("android.widget.Switch"));
			}

			if (sws == null) {
				LogUtil.writeLog("not found check");
				AccessServiceUtil.goActivityBack();
				break;
			}

			for (AccessibilityNodeInfo sw : sws) {
				if(sw.isChecked() == isOpen) continue;
				LogUtil.writeLog("clickButton check!!!!");
				ClickUtil.clickButton(sw);
				if (AndroidUtil.isVIVO()){
					ClickUtil.closeAllDialogForTime(500);
				}
			}

			if (Build.BRAND.equalsIgnoreCase("Coolpad"))
				AccessServiceUtil.goActivityBack();
			else
				AccessServiceUtil.goActivityBack(backtime);
			if(AndroidUtil.isOPPO()) {
				AccessUtil.waitSleepAndStill();
				AccessibilityNodeInfo i = AccessUtil.getChild(new AccessSelector().text("WLAN 助理"));
				AccessibilityNodeInfo t = AccessUtil.getChild(new AccessSelector().text("高级设置"));
				AccessibilityNodeInfo t1 = AccessUtil.getChild(new AccessSelector().text("双通道网络加速"));
				AccessibilityNodeInfo t2 = AccessUtil.getChild(new AccessSelector().text("智能连接最佳 WLAN"));
				if ((i!=null&&t!=null)||(i!=null&&t1!=null&&t2!=null)){
					LogUtil.writeLog("oppo new condition!!!!");
					AccessServiceUtil.goActivityBack();
				}
			}
			if (backtime==2){
				AccessUtil.waitSleepAndStill();
				AccessibilityNodeInfo listView = AccessUtil.getChild(new AccessSelector().resourceId(pkg+":id/recycler_view"));
				if (listView==null) listView = ScanListView.getListView();
				int times = 0;
				while(listView.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)) {
					AndroidUtil.sleep(200);
					times ++;
					if(times > 10) break;
				}
			}
		} while(false);

		return true;
	}

	public void doOpenWifiSetting() {
		doOpenWifiSetting(true);
	}

	public void setupIME(String apkName) {
		setupIME(apkName,1);
	}

	public void setupIME(String apkName,int trytime) {
		if (AndroidUtil.isXiaomi())doRemoveWifi();

		LogUtil.writeLog("trytime!!!" + trytime);
		LogUtil.writeLog("setupIME=" + apkName);
		final String imeName = APKUtil.getPackageAppName(apkName);
		if (imeName == null) return;
//		String imeName = "讯飞输入法";
//		String imeName = "搜狗输入法";
		LogUtil.writeLog("imeName =>>" + imeName);
		//第一步使能输入法
		if(AndroidUtil.isOPPO()) {
			if (!APKUtil.startAction("android.settings.INPUT_METHOD_SETTINGS")) return;
	if(!AccessServiceUtil.waitForPackage("com.android.settings", 5000)) {
				return;
			}
			AndroidUtil.sleep(100);
		}else{
		if (!AccessServiceUtil.startActionAndWait(Settings.ACTION_INPUT_METHOD_SETTINGS, "com.android.settings", 5000)) return;
		}
		LogUtil.writeLog("still");
		//等待界面
		if(!AndroidUtil.isVIVO()) {
			AccessUtil.waitNode(new AccessSelector().enable(true).checkable(true));
			LogUtil.writeLog("wait node!!!!");
		}
		if (AndroidUtil.isXiaomi()){
			if (ClickUtil.waitAndClickButton(new AccessSelector().text("输入法管理"))) {
				LogUtil.writeLog("new Version!!!!");
				AndroidUtil.sleep(500);
			}
		}
		do {
			List<AccessibilityNodeInfo> nodes = AccessUtil.getChildren(new AccessSelector().enable(true).checkable(true));
			if (nodes == null) {
				LogUtil.writeLog("nodes =>> null1");
				nodes = AccessUtil.getChildren(new AccessSelector().text("未启用"));
				if (nodes == null) {
					nodes = AccessUtil.getChildren(new AccessSelector().className("android.view.View"));
					if (nodes == null) {
						LogUtil.writeLog("nodes =>> null2");
						break;
					}
				}
			}
			for (AccessibilityNodeInfo node : nodes) {
				if (node.isChecked()) continue;
				LogUtil.writeLog("node isnotChecked");
				if(AndroidUtil.isOPPO()||(AndroidUtil.isVIVO()&&Build.VERSION.SDK_INT >= 30)) {
					if(!ClickUtil.clickNodeForNext(node)){
					LogUtil.writeLog("clickNode false");
					}
                    AndroidUtil.sleep(100);
					AccessUtil.waitNode(new AccessSelector().text("使用|确定"));
					LogUtil.writeLog("wait node!!!!");
                    AndroidUtil.sleep(100);
					ClickUtil.clickButton(new AccessSelector().text("使用|确定"));
				}else{
					if(!ClickUtil.clickNodeForNext(node)){
						LogUtil.writeLog("clickNode false");
					}
//					if (Build.BRAND.equalsIgnoreCase("OnePlus")){
						ClickUtil.waitAndClickButtonForNext(new AccessSelector().text("确定"));
						ClickUtil.waitAndClickButtonForNext(new AccessSelector().text("确定"));
//					}else
						ClickUtil.closeAllDialogForTime(1000);
			}
			}
		}while (false);
		if (AndroidUtil.isXiaomi()) AccessServiceUtil.goActivityBack();
//		showMainWindow();
//		InstallUtil.showMe();
//		AndroidUtil.sleep(500);

		//第二步
		showInstallWindow();
		APKUtil.startIMESetting();

		if (AndroidUtil.isXiaomi()) {
			AndroidUtil.sleep(500);
			AccessibilityNodeInfo newnode = AccessUtil.getChild(new AccessSelector().text("其他输入法"));
			if (newnode != null) {
				LogUtil.writeLog("click!!!newnode" + ClickUtil.clickButton(newnode));
			}else{
				LogUtil.writeLog("no!!!newnode");
			}
		}else{
			ClickUtil.closeAllDialogForTime(500);
		}
		AccessibilityNodeInfo node = AccessUtil.getChild(new AccessSelector().text(imeName));
		if (node==null) node = AccessUtil.getChild(new AccessSelector().text(imeName + "*"));
		if (node != null){
			String text=node.getText().toString();
			LogUtil.writeLog("item!!!" + text);
			if (text.contains("定制版"))node=null;
		}

		if (node != null) {
			LogUtil.writeLog("click!!!" + imeName + ":" + ClickUtil.clickButton(node));
		} else {
			LogUtil.writeLog("notfound!!!" + imeName);
			if (trytime>1){
				AccessServiceUtil.goActivityBack();
				setupIME(apkName,1);
				return;
			}
		}

		if(AndroidUtil.isVIVO()&&Build.VERSION.SDK_INT >= 30) {
			showInstallWindow();
		}else{
			ClickUtil.closeAllDialogForTime(1500);
		}
	}

	public void getImeiByClick() {
		AccessUtil.prepareNextStill();
		LogUtil.writeLog("open dail.................................");
		if(!APKUtil.startAction(Intent.ACTION_DIAL)) return;
		AccessUtil.waitNextStill();
		LogUtil.writeLog("open dail>>>>>>>>>>>>>>>>>>>>");

		if (AndroidUtil.isXiaomi()){
			AccessServiceUtil.goCurrentActivityBack();
			AndroidUtil.sleep(200);
			APKUtil.startAction(Intent.ACTION_DIAL);
		}

		if (AndroidUtil.isVIVO()){
			AccessServiceUtil.pressHome();
			AndroidUtil.sleep(500);
			APKUtil.startAction(Intent.ACTION_DIAL);
		}

		if(ClickUtil.clickButton(new AccessSelector().text("取消"))){
			AccessServiceUtil.goCurrentActivityBack();
			AndroidUtil.sleep(200);
			APKUtil.startAction(Intent.ACTION_DIAL);
		}

		if (Build.BRAND.equalsIgnoreCase("5G")&&Build.MODEL.equalsIgnoreCase("i14Pro")){
			AndroidUtil.sleep(500);
			AccessibilityNodeInfo btn=AccessUtil.getChild(new AccessSelector().resourceId("com.qiku.android.contacts:id/people_float_main_image_button"));
			if (btn!=null) {
				ClickUtil.clickButtonForNext(btn);
			}
		}

		ClickUtil.closeAllDialogForTime(500);

		if (AndroidUtil.isOnePlus()){
			ClickUtil.waitAndClickButton(new AccessSelector().text("同意并使用"));

			AccessibilityNodeInfo btn=AccessUtil.getChild(new AccessSelector().resourceId("com.oneplus.dialer:id/floating_action_button"));
			if (btn!=null) {
				ClickUtil.clickButtonForNext(btn);
			}
		}

		if (Build.BRAND.equalsIgnoreCase("ZTE")){
			ClickUtil.waitAndClickButton(new AccessSelector().text("接受"));
		}

		if (Build.BRAND.equalsIgnoreCase("samsung")){
			ClickUtil.waitAndClickButton(new AccessSelector().text("允许"));
		}

		AccessibilityNodeInfo numzero=AccessUtil.getChild(new AccessSelector().resourceId("com.android.contacts:id/zero|com.android.dialer:id/zero|com.android.contacts:id/contacts_dialpad_zero|com.huawei.contacts:id/contacts_dialpad_zero|com.qiku.android.contacts:id/dial_num_0|com.android.dialer:id/dial_num_0|com.samsung.android.dialer:id/zero"));
		if (numzero==null) numzero=AccessUtil.getChild(new AccessSelector().contentDesc("0|零"));
		if (numzero==null) numzero=AccessUtil.getChild(new AccessSelector().text("0"));
//		LogUtil.writeLog("1numzero==null?"+(numzero==null));
//		if (numzero==null) numzero=AccessUtil.getChild(new AccessSelector().text("+"));
//		LogUtil.writeLog("2numzero==null?"+(numzero==null));

		AccessibilityNodeInfo numsix = AccessUtil.getChild(new AccessSelector().resourceId("com.android.contacts:id/six|com.android.dialer:id/six|com.android.contacts:id/contacts_dialpad_six|com.huawei.contacts:id/contacts_dialpad_six|com.qiku.android.contacts:id/dial_num_6|com.android.dialer:id/dial_num_6|com.samsung.android.dialer:id/six"));
		if (numsix == null) numsix = AccessUtil.getChild(new AccessSelector().contentDesc("6|六"));
		if (numsix == null) numsix = AccessUtil.getChild(new AccessSelector().text("6"));
		if (AndroidUtil.isVIVO()){
			if (numsix != null&&numsix.getViewIdResourceName().equals("com.android.dialer:id/smart_six")){
				numsix = AccessUtil.getChild(new AccessSelector().text("6").resourceId("com.android.dialer:id/text_1"));
			}
		}

		AccessibilityNodeInfo asterisk = AccessUtil.getChild(new AccessSelector().text("*"),true);
		if(asterisk == null) asterisk = AccessUtil.getChild(new AccessSelector().contentDesc("*|星号"),true);
		if(asterisk == null) asterisk = AccessUtil.getChild(new AccessSelector().resourceId("com.android.contacts:id/star|com.android.dialer:id/star|com.android.contacts:id/contacts_dialpad_star|com.huawei.contacts:id/contacts_dialpad_star|com.hihonor.contacts:id/contacts_dialpad_star|com.qiku.android.contacts:id/dial_num_star|com.android.dialer:id/dial_num_star"));

		AccessibilityNodeInfo well=AccessUtil.getChild(new AccessSelector().text("#"));
		if(well == null) well = AccessUtil.getChild(new AccessSelector().contentDesc("#|井号"));
		if(well == null) well=AccessUtil.getChild(new AccessSelector().resourceId("com.android.contacts:id/pound|com.android.dialer:id/pound|com.android.contacts:id/contacts_dialpad_pound|com.huawei.contacts:id/contacts_dialpad_pound|com.hihonor.contacts:id/contacts_dialpad_pound|com.qiku.android.contacts:id/dial_num_pound|com.android.dialer:id/dial_num_pound"));

		GestureUtil.click(asterisk); AndroidUtil.sleep(200);
		GestureUtil.click(well); AndroidUtil.sleep(200);
		GestureUtil.click(numzero); AndroidUtil.sleep(200);
		GestureUtil.click(numsix); AndroidUtil.sleep(200);
		GestureUtil.click(well);

		if(AccessUtil.waitNode(new AccessSelector().text("IMEI*"), 5000) == null) {
			if (AndroidUtil.isVIVO()){
				AccessServiceUtil.pressHome();
				AndroidUtil.sleep(500);
				APKUtil.startAction(Intent.ACTION_DIAL);

				if(AccessUtil.waitNode(new AccessSelector().text("IMEI*"), 5000) == null) {
					LogUtil.writeLog("not found IMEI String");
					AccessServiceUtil.goActivityBack();
					return;
				}

			}else{
				LogUtil.writeLog("not found IMEI String");
				AccessServiceUtil.goActivityBack();
				return;
			}
		}

		List<AccessibilityNodeInfo> imeis=AccessUtil.getChildren( new AccessSelector().text("IMEI*|MEID*"));
		int index=1;
		if (Build.BRAND.equalsIgnoreCase("samsung")){
			imeis=AccessUtil.getChildren( new AccessSelector().text("/ 19*"));
		}

		if (imeis==null) {
			LogUtil.writeLog("not found IMEI String");
			AccessServiceUtil.goActivityBack();
			return;
		}

		JSONObject json=new JSONObject();
		try {
		for (AccessibilityNodeInfo imei:imeis) {
			if(imei==null|imei.getText()==null)continue;
			String ss=imei.getText().toString();
			LogUtil.writeLog("line>>"+ss);
			if (Build.BRAND.equalsIgnoreCase("samsung")){
				json.put("IMEI"+index, ss.trim().replace(" / 19", ""));
				index++;
			}else {
				if (!ss.contains(":")) continue;

				String[] ids = ss.split("\n");
				if (ids.length == 1) {
					String[] keyvalue = ids[0].split(":");
					json.put(keyvalue[0].trim().replace(" ", ""), keyvalue[1].trim().replace(" ", ""));
				} else {
					for (String id : ids) {
						if (!id.contains(":")) continue;
						String[] keyvalue = id.split(":");
						json.put(keyvalue[0].trim().replace(" ", ""), keyvalue[1].trim().replace(" ", ""));
					}
				}
			}
			}
			LogUtil.writeLog(json.toString());
			FileUtil.writeFile(FileUtil.getSDFile(".devInfo"), json.toString());
		} catch (JSONException e) {
			LogUtil.writeLog("JSONException"+e.getMessage());
			e.printStackTrace();
		}
		ClickUtil.waitAndClickButton(new AccessSelector().text("确定|知道了|取消"));
		AndroidUtil.sleep(500);
		AccessServiceUtil.goActivityBack();
	}

	public int doCloseRecentsOnce() {
		AccessServiceUtil.pressHome();
		AccessServiceUtil.getRecents();

		AndroidUtil.sleep(1000);
		AccessUtil.waitForStill();

		AccessibilityNodeInfo root = AccessServiceUtil.getRootNode();
		List<String> texts = AccessUtil.getAllText(root, true);
		if(texts == null) {
			LogUtil.writeLog("texts is null");
			AccessServiceUtil.pressHome();
			return 0;
		}
		LogUtil.writeLog(texts.toString());

		int cnt = 0;
		int sameTimes = 0;
		while(true) {
			Rect rect = new Rect();
			root.getBoundsInScreen(rect);
			GestureUtil.swipe(rect.centerX(), rect.centerY(), rect.centerX(), rect.height() / 4);
			AndroidUtil.sleep(200);

			root = AccessServiceUtil.getRootNode();
			if(root == null) {
				LogUtil.writeLog("root is null!");
				break;
			}
//			if(root.getPackageName().toString().contains("launcher")) break;

			List<String> newTexts = AccessUtil.getAllText(root, true);
			if(newTexts == null) {
				LogUtil.writeLog("newTexts is null");
				break;
			}

			LogUtil.writeLog(newTexts.toString());
			if(newTexts.equals(texts)) {
				sameTimes ++;
			} else {
				sameTimes = 0;
				texts = newTexts;
				cnt ++;
			}

			if(sameTimes > 1) break;
		}
		AccessServiceUtil.pressHome();

		return cnt;
	}

	public void doCloseRecents() {
		doCloseRecentsOnce();
		for(int ii=0;ii<5;ii++) {
			int cnt = doCloseRecentsOnce();
			if(cnt == 0) break;
		}
	}

    public void doCloseRecentsAll() {
        AccessServiceUtil.pressHome();
        AccessServiceUtil.getRecents();

        AndroidUtil.sleep(1000);
        AccessUtil.waitForStill();

        //关闭按钮
        AccessSelector selector = new AccessSelector().contentDesc("清理*|清除|关闭所有最近打开的应用");
        if(AccessUtil.waitNode(selector,5000) == null) {
            LogUtil.writeLog("not found recents button!");
            AccessUtil.dumpNode2File("recents.xml");
            AndroidUtil.sleep(500);
        }
        AndroidUtil.sleep(500);
        AccessibilityNodeInfo btn = AccessUtil.getChild(selector);
        if(!ClickUtil.clickButton(btn)){
            LogUtil.writeLog("click recents button fail!");
        }
    }

	//循环发送显示可以拔除usb线的广播
	final void sendBroadcastShowTips() {
//		AccessServiceUtil.sendBroadcast("com.qjkj.nnb.showsnackbar");
	}

	final public void doBatchPermit() {
		if (Build.VERSION.SDK_INT<=26) return;

		for(APhoneParams.PhoneApp app: APhoneParams.phoneApps) {
			try {
				app.doPermission();
			} catch (Exception e) {
				LogUtil.writeLog("doPermission=>" + app.apkName + ":" + e.toString());
			}
		}
	}

	protected void clearLauncher() {
		AccessServiceUtil.pressHome();
		do {
			AccessibilityNodeInfo root = AccessServiceUtil.getRootNode();
			if(root == null) break;
			CharSequence packageName_ = root.getPackageName();
			if(packageName_ == null) break;
			String packageName = packageName_.toString();

			if(!packageName.contains("launcher")) break;
//			if (AndroidUtil.isHuawei()) break;

			AccessServiceUtil.clearPackage(packageName);

			AccessServiceUtil.pressHome();
			ClickUtil.closeAllDialogForTime(3000);
			break;
		}while(false);
	}

	public void doTest() {
	}

//	public void setCounterPermission(){
//		if (Build.VERSION.SDK_INT < 30) {
//			LogUtil.writeLog("not need setCounterPermission");
//			return;
//		}
//		LogUtil.writeLog("setCounterPermission");
//		for (String counter:APhoneParams.Counters){
//			LogUtil.writeLog("setCounterPermission:"+counter);
////			if(!APhoneParams.hasApp(counter)) continue;
//			LogUtil.writeLog("do setCounterPermission:"+counter);
//			InstallUtil.showMe();
//			setSpecialPermission("android.settings.MANAGE_APP_ALL_FILES_ACCESS_PERMISSION",counter);
//		}
//		AccessServiceUtil.goCurrentActivityBack();
//		AccessServiceUtil.pressHome();
//	}

	public void setSpecialPermission(String actionName,String pkgName){
		if (!APKUtil.startActionWithPackage(actionName, pkgName)) {
			LogUtil.writeLog("can't open "+actionName+" ..");
			return;
		}
		AndroidUtil.sleep(1000);
		AccessUtil.dumpNode2File("aaaa.txt");
		AccessSelector selector = new AccessSelector().checkable(true).checked(false);
		if (AccessUtil.waitNode(selector) == null) {
			LogUtil.writeLog("can't find "+actionName+" ..");
			selector = new AccessSelector().className("android.widget.Switch").checked(false);
		}
		AccessibilityNodeInfo node = AccessUtil.getChild(selector);
		if (node != null) {
			LogUtil.writeLog("click open "+actionName+" ..");
			ClickUtil.clickButton(node);
		} else {
			LogUtil.writeLog("can't find checkable checked false "+actionName+" ..");
			node = AccessUtil.getChild(new AccessSelector().text("允许此应用*"));
			if (node!=null){
				Rect rect=new Rect();
				node.getBoundsInScreen(rect);
				if (Build.BRAND.equalsIgnoreCase("honor")){
					GestureUtil.click(rect.right,rect.top-100);
				}else{
					GestureUtil.click(rect.centerX(),rect.top-100);
				}
				AccessServiceUtil.goActivityBack();
				AccessServiceUtil.pressHome();
				return;
			}

		}
		AccessServiceUtil.goActivityBack();
		AccessServiceUtil.pressHome();
	}

	public void doBeforeCommit() {
		doOpenWifi();
	}

	public void doSetupIME(String apkName) {
		setupIME(apkName);
	}


	public boolean doEnd(){
		return true;
	}
}
