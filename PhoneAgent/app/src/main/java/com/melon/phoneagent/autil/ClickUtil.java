package com.melon.phoneagent.autil;

/********************************************************************
 * 该模块为点击的工具函数库
 * 其本身需要 AccessUtil 库来支撑
 ********************************************************************/

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.os.Build;
import android.view.accessibility.AccessibilityNodeInfo;

import com.melon.util.AndroidUtil;
import com.melon.util.LogUtil;

import java.util.List;

@SuppressLint("NewApi")
public class ClickUtil {
	public static int mWaitTimeout = 3000;
	public static String toolTitle = "";

	/**
	 * 设置等待节点的等待时间
	 * @param timeout (毫秒)
	 */
	static public void setTimeout(int timeout) {
		mWaitTimeout = timeout;
	}

	/**
	 * 设置缺省的等待时间
	 */
	static public void setTimeout() {
		mWaitTimeout = 3000;
	}

	/**
	 * 点击一个node
	 * @param node
	 * @param force 为true的话，则使用Gesture进行点击
	 * @return
	 */
	static public boolean clickNode(AccessibilityNodeInfo node, boolean force) {
		if(node == null) return false;

		if(node.isClickable() && node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
			return true;
		}

		if(!force) return false;

		return GestureUtil.click(node);
	}

	/**
	 * 点击一个 node ，如果该node不支持点击，则用Gesture进行点击
	 * @param node
	 * @return
	 */
	static public boolean clickNode(AccessibilityNodeInfo node) {
		return clickNode(node, true);
	}

	static public boolean clickNodeForNext(AccessibilityNodeInfo node) {
		String serial = AccessUtil.getXMLSerial();
		if(serial == null) return false;

		if(!clickNode(node)) {
			return false;
		}

		return AccessUtil.waitNextStill(serial);
	}

	/**
	 * 点击一个node,该node用 selector 来查询（查询到的第一个即为node)
	 * @param selector
	 * @return
	 */
	static public boolean clickNode(AccessSelector selector) {
		AccessibilityNodeInfo child = AccessUtil.getChild(selector);
		if(child == null) return false;
		return clickNode(child);
	}

	static public boolean clickNodeForNext(AccessSelector selector) {
		AccessibilityNodeInfo node = AccessUtil.getChild(selector);
		if(node == null) return false;

		return clickNodeForNext(node);
	}

	/**
	 * 点击 node 的子节点(用selector)来进行查询
	 * @param node
	 * @param selector
	 * @return
	 */
	static public boolean clickNode(AccessibilityNodeInfo node, AccessSelector selector) {
		AccessibilityNodeInfo child = AccessUtil.getChild(node, selector);
		if(child == null) return false;
		return clickNode(child);
	}

	static public boolean clickNodeForNext(AccessibilityNodeInfo node, AccessSelector selector) {
		AccessibilityNodeInfo child = AccessUtil.getChild(node, selector);
		if(child == null) return false;

		return clickNodeForNext(child);
	}

	public static boolean waitAndClickNode(AccessibilityNodeInfo node, AccessSelector selector) {
		AccessibilityNodeInfo child = AccessUtil.waitNode(node, selector, mWaitTimeout);
		if(child == null) return false;
		return clickNode(child);
	}

	public static boolean waitAndClickNodeForNext(AccessibilityNodeInfo node, AccessSelector selector) {
		AccessibilityNodeInfo child = AccessUtil.waitNode(node, selector, mWaitTimeout);
		if(child == null) return false;
		return clickNodeForNext(child);
	}

	public static boolean waitAndClickNode(AccessSelector selector) {
		AccessibilityNodeInfo child = AccessUtil.waitNode(selector, mWaitTimeout);
		if(child == null) return false;
		return clickNode(child);
	}

	public static boolean waitAndClickNodeForNext(AccessSelector selector) {
		AccessibilityNodeInfo child = AccessUtil.waitNode(selector, mWaitTimeout);
		if(child == null) return false;
		return clickNodeForNext(child);
	}

	static public AccessibilityNodeInfo getButton(AccessibilityNodeInfo node) {
		if(node == null) return null;

		AccessibilityNodeInfo pnode = node;
		while(!pnode.isClickable()) {
			pnode = pnode.getParent();
			if(pnode == null) break;
		}

		return pnode;
	}

	static public boolean clickButton(AccessibilityNodeInfo node) {
		if(node == null) return false;

		AccessibilityNodeInfo pnode = getButton(node);
		if(pnode == null) {
			LogUtil.writeLog("clickButton node is null!!!");
			return GestureUtil.click(node);
		}
		if(pnode.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
			return true;
		}
		LogUtil.writeLog("clickButton action_click fail");

		return GestureUtil.click(node);
	}

	static public boolean clickButtonForNext(AccessibilityNodeInfo node) {

		String serial = AccessUtil.getXMLSerial();
		if(serial == null) {
			return false;
		}

		if(!clickButton(node)) {
			return false;
		}

		return AccessUtil.waitNextStill(serial);
	}

	public static boolean waitAndClickButton(AccessibilityNodeInfo node, AccessSelector selector) {
		AccessibilityNodeInfo child = AccessUtil.waitNode(node, selector, mWaitTimeout);
		if(child == null) return false;
		return clickButton(child);
	}

	public static boolean waitAndClickButtonForNext(AccessibilityNodeInfo node, AccessSelector selector) {
		AccessibilityNodeInfo child = AccessUtil.waitNode(node, selector, mWaitTimeout);
		if(child == null) return false;
		return clickButtonForNext(child);
	}

	public static boolean waitAndClickButton(AccessSelector selector) {
		AccessibilityNodeInfo child = AccessUtil.waitNode(selector, mWaitTimeout);
		if(child == null) {
			return false;
		}
		return clickButton(child);
	}

	public static boolean waitAndClickButtonForNext(AccessSelector selector) {
		AccessibilityNodeInfo child = AccessUtil.waitNode(selector, mWaitTimeout);
		if(child == null) {
			return false;
		}
		return clickButtonForNext(child);
	}

	static public boolean clickButton(AccessSelector selector) {
		AccessibilityNodeInfo node = AccessUtil.getChild(selector);
		return clickButton(node);
	}

	static public boolean clickButtonForNext(AccessSelector selector) {
		AccessibilityNodeInfo node = AccessUtil.getChild(selector);
		return clickButtonForNext(node);
	}

	/**
	 * 点掉当前的确认按钮
	 */
	static public int closeDialogWindow() {

		AccessServiceUtil.wakeUp();
		String brand = Build.BRAND;

		//如果获取不到根节点，则按返回键
		AccessibilityNodeInfo root = AccessServiceUtil.getRootNode();
		if (root == null) {
//			File file=new File("/data/local/tmp/click");
//			if (file.exists()) {
//				file.delete();
//				LogUtil.writeLog("del click file!!!");
//			}

			LogUtil.writeLog("closeDialogWindow=>AccessServiceUtil.getRootNode() is null!!!");
			return 0;
		}

		String packageName = root.getPackageName().toString();

		//WLAN连接不可上网(华为)
		if (AccessUtil.canFindChildByText(root, "WLAN连接不可上网")) {
			if (ClickUtil.waitAndClickButton(root, new AccessSelector().text("否"))) {
				return 1;
			}
		}

		if (brand.toLowerCase().contains("zte")) {
			AccessibilityNodeInfo qd= AccessUtil.getChild(root, new AccessSelector().text("确定"));
			AccessibilityNodeInfo flag=AccessUtil.getChild(root, new AccessSelector().text("要卸载此应用吗？"));
			if (packageName.equals("com.android.packageinstaller")&&qd!=null&&flag!=null) {
				LogUtil.writeLog("found:zte uninstall");
				ClickUtil.clickButton(qd);
				AndroidUtil.sleep(200);
			}
		}

		if (brand.toLowerCase().contains("samsung")) {
			AccessibilityNodeInfo qd= AccessUtil.getChild(root, new AccessSelector().text("确定"));
			AccessibilityNodeInfo flag=AccessUtil.getChild(root, new AccessSelector().text("要卸载此应用吗？"));
			if (packageName.equals("com.samsung.android.packageinstaller")&&qd!=null&&flag!=null) {
				LogUtil.writeLog("found:samsung uninstall");
				ClickUtil.clickButton(qd);
				AndroidUtil.sleep(200);
			}
		}

		if(AndroidUtil.isHuawei() && packageName.equals("com.android.packageinstaller")) {
			//荣耀100 对应界面“继续安装”为空  需要根据 取消 或者 查找类似应用按钮位置点
			if (Build.BRAND.equalsIgnoreCase("honor")){
				AccessibilityNodeInfo jxazbtn = AccessUtil.getChild(root, new AccessSelector().text("继续安装"));
				AccessibilityNodeInfo ingbtn = AccessUtil.getChild(root, new AccessSelector().text("正在安装*"));
				AccessibilityNodeInfo btn = AccessUtil.getChild(root, new AccessSelector().text("取消"));
				AccessibilityNodeInfo btn2 = AccessUtil.getChild(root, new AccessSelector().text("查找类似应用"));
				AccessibilityNodeInfo flag=AccessUtil.getChild(root, new AccessSelector().text("安装来源："+toolTitle));
				AccessibilityNodeInfo flag2=AccessUtil.getChild(root, new AccessSelector().text("来自“"+toolTitle+"”"));
				AccessibilityNodeInfo flag3=AccessUtil.getChild(root, new AccessSelector().text(toolTitle+"*"));
				boolean hasflag = false;
				if (flag!=null||flag2 != null){
					hasflag = true;
				}
				LogUtil.writeLog("hasflag:"+hasflag);
				if (!hasflag){
					LogUtil.writeLog(AccessUtil.dumpNodeXML());
					if (flag3!=null){
						hasflag = true;
					}
				}
				if ((btn != null||btn2 != null)&&hasflag&&jxazbtn==null&&ingbtn==null) {
					LogUtil.writeLog("use cancel:huawei install");
					LogUtil.writeLog(AccessUtil.dumpNodeXML());
					boolean usebtn2=false;
					if (btn2 != null){
						Rect btn2rect = new Rect();
						btn2.getBoundsInScreen(btn2rect);
						LogUtil.writeLog("btn2rect.width()"+btn2rect.width());
						Rect rootrect = new Rect();
						root.getBoundsInScreen(rootrect);
						LogUtil.writeLog("root.width()"+rootrect.width());
						if (btn2rect.width()<=rootrect.width()/2)
							usebtn2=true;
					}

					Rect rect = new Rect();
					if (usebtn2)
						btn2.getBoundsInScreen(rect);
					else
						btn.getBoundsInScreen(rect);
					LogUtil.writeLog("btnXrect.width()"+rect.width());
					GestureUtil.click(rect.right + (rect.right - rect.left) / 2,rect.centerY());
				}
			}

			{
				AccessibilityNodeInfo flag = AccessUtil.getChild(root, new AccessSelector().text("已了解此应用未经*"));
				if (flag != null&&flag.getText().toString().contains("可能存在风险")) {
					LogUtil.writeLog("found:honor tips:"+flag.getText().toString());
					ClickUtil.clickButton(flag);
					AndroidUtil.sleep(200);
				}
			}

			do {
				if (AccessUtil.getChild(root, new AccessSelector().text("是否允许“移动软件助手”安装应用？|是否允许“"+toolTitle+"”安装应用？")) == null)
					break;

				AccessibilityNodeInfo node = AccessUtil.getChild(root, new AccessSelector().text("以后都允许"));
				AccessibilityNodeInfo btn = AccessUtil.getChild(root, new AccessSelector().text("允许"));
				AccessibilityNodeInfo check = AccessUtil.getChild(root, new AccessSelector().text("不再提示"));
				if ((node == null && check == null) || (btn == null && check == null)) break;

				LogUtil.writeLog("found:huawei new");
				ClickUtil.clickButton(check);
				AndroidUtil.sleep(200);
				if (node != null) {
					ClickUtil.clickButton(node);
					AndroidUtil.sleep(200);
				}
				if (btn != null) {
					ClickUtil.clickButton(btn);
					AndroidUtil.sleep(200);
				}
			} while (false);

			do {
				if (AccessUtil.getChild(root, new AccessSelector().text("是否卸载此应用？|是否将此应用替换为出厂版本？这样会移除所有数据。")) == null)
					break;
				LogUtil.writeLog("found:huawei uninstall");
				AccessibilityNodeInfo btn = AccessUtil.getChild(root, new AccessSelector().text("确定"));
				if (btn != null) {
					LogUtil.writeLog("click:huawei uninstall");
					LogUtil.writeLog("click:" + ClickUtil.clickButton(btn));
					AndroidUtil.sleep(200);
				}
			} while(false);

			do {
				if (AccessUtil.getChild(root, new AccessSelector().text("移动软件助手")) == null) break;
				if (AccessUtil.getChild(root, new AccessSelector().text("安装来源：华为浏览器")) == null) break;
				LogUtil.writeLog("found:huawei install");
				AccessibilityNodeInfo btn = AccessUtil.getChild(root, new AccessSelector().text("取消"));
				if (btn != null) {
					LogUtil.writeLog("cancel:huawei install");
					ClickUtil.clickButton(btn);
					AndroidUtil.sleep(200);
				}
			} while (false);
		}

		//OPPO允许安装应用
		if(AndroidUtil.isOPPO() && packageName.equals("com.android.packageinstaller")) {
			do {
				if (AccessUtil.getChild(root, new AccessSelector().text("允许“文件管理”安装应用吗？")) == null)
					break;

				AccessibilityNodeInfo node = AccessUtil.getChild(root, new AccessSelector().text("以后都允许"));
				if (node == null) break;

				LogUtil.writeLog("found:oppo new");
				ClickUtil.clickButton(node);
				AndroidUtil.sleep(200);
			} while (false);
		}

		if(AndroidUtil.isOPPO()) {
			AccessibilityNodeInfo vvbtn = AccessUtil.getChild(new AccessSelector().text("重新安装"));
			if(vvbtn!=null) {
				LogUtil.writeLog("click 重新安装!!!");
				ClickUtil.clickNode(vvbtn);
				AndroidUtil.sleep(500);
			}
		}

		if(AndroidUtil.isOPPO()) {
			do {
				if (AccessUtil.getChild(new AccessSelector().text("安装包与系统不兼容*")) == null)
					break;

				AccessibilityNodeInfo node = AccessUtil.getChild(new AccessSelector().text("取消"));
				if (node == null) break;

				LogUtil.writeLog("found:oppo 安装包与系统不兼容");
				ClickUtil.clickButton(node);
				AndroidUtil.sleep(200);
			} while (false);
		}

		//xiaomi允许安装应用
		if(AndroidUtil.isXiaomi() && packageName.contains("installer")) {
			do {
				if (AccessUtil.getChild(root, new AccessSelector().text("未知应用*")) == null) break;

				AccessibilityNodeInfo node = AccessUtil.getChild(root, new AccessSelector().text("设置*").clickable(true));
				if (node == null) break;

				ClickUtil.clickButton(node);
				AndroidUtil.sleep(500);

				for (int ii = 0; ; ii++) {
					node = AccessUtil.getChild(new AccessSelector().checkable(true));
					if (node == null) {
						AccessServiceUtil.goActivityBack();
						return 0;
					}
					if (node.isChecked()) break;

					ClickUtil.clickNode(node);
					AndroidUtil.sleep(500);
				}
				AccessServiceUtil.goActivityBack();
				return 0;
			} while (false);
		}

		if(AndroidUtil.isHuawei()) {
			{
			AccessibilityNodeInfo flag=AccessUtil.getChild(root, new AccessSelector().text("风险提示"));
			AccessibilityNodeInfo jx=AccessUtil.getChild(root, new AccessSelector().text("继续安装"));
			if (jx!=null&&flag!=null) {
				LogUtil.writeLog("found:huawei fxts");
				ClickUtil.clickButton(jx);
				AndroidUtil.sleep(200);
			}
		}
			{
				AccessibilityNodeInfo flag = AccessUtil.getChild(root, new AccessSelector().text("移入风险管控中心"));
				AccessibilityNodeInfo jx = AccessUtil.getChild(root, new AccessSelector().text("取消"));
				if (jx != null && flag != null) {
					LogUtil.writeLog("found:huawei fxgk");
					ClickUtil.clickButton(jx);
					AndroidUtil.sleep(200);
				}
			}
		}

		if(AndroidUtil.isOPPO()) {
			AccessibilityNodeInfo sz=AccessUtil.getChild(root, new AccessSelector().text("确定"));
			AccessibilityNodeInfo flag=AccessUtil.getChild(root, new AccessSelector().text("未找到应用"));
			AccessibilityNodeInfo flag2=AccessUtil.getChild(root, new AccessSelector().text("未在已安装应用的列表中找到该应用。"));
			if (root.getPackageName().equals("com.android.packageinstaller")&&sz!=null&&flag!=null&&flag2!=null) {
				LogUtil.writeLog("found:oppo not found app");
				ClickUtil.clickButton(sz);
				AndroidUtil.sleep(200);
			}
		}

		if(AndroidUtil.isOPPO()) {
			AccessibilityNodeInfo sz=AccessUtil.getChild(root, new AccessSelector().text("以后都允许"));
			AccessibilityNodeInfo flag=AccessUtil.getChild(root, new AccessSelector().text("允许“文件管理”安装应用吗？|允许“"+toolTitle+"”安装应用吗？"));
			if (root.getPackageName().equals("com.android.packageinstaller")&&sz!=null&&flag!=null) {
				LogUtil.writeLog("found:oppo new");
				ClickUtil.clickButton(sz);
				AndroidUtil.sleep(200);
			}
		}

		if (AndroidUtil.isVIVO()) {
			{
				AccessibilityNodeInfo sz=AccessUtil.getChild(root, new AccessSelector().text("设置"));
				if (sz==null) {
					sz=AccessUtil.getChild(root, new AccessSelector().text("设置*"));
				}
				AccessibilityNodeInfo flag=AccessUtil.getChild(root, new AccessSelector().text("InstallRun"));
				if (flag==null) {
					flag=AccessUtil.getChild(root, new AccessSelector().text("InstallRun*"));
				}

				if (packageName.equals("com.android.packageinstaller")&&sz!=null&&flag!=null) {
					LogUtil.writeLog("found:vivo new");
					ClickUtil.clickButton(sz);
					AndroidUtil.sleep(200);
					ScanListView.startScan(new ScanListView.ScanListViewSimpleInterface() {

						@Override
						public boolean scanItem(AccessibilityNodeInfo item, int pos) {
							AccessibilityNodeInfo titleNode = AccessUtil.getChild(item, new AccessSelector().text("允许安装未知应用|InstallRun"));
							if(titleNode == null) return true;
							CharSequence title = titleNode.getText();
							if(title == null)return true;

							AccessibilityNodeInfo sw = AccessUtil.getChild(item, new AccessSelector().className("android.view.View"));
							if (sw == null)return true;
							GestureUtil.click(sw);
							closeAllDialogForTime(1000);
							return true;
						}
					});
					AccessServiceUtil.goActivityBack();
				}
			}

//			{
//				AccessibilityNodeInfo fl = AccessUtil.getChild(root, new AccessSelector().text("安装失败*"));
//				AccessibilityNodeInfo qx = AccessUtil.getChild(root, new AccessSelector().text("取消"));
//
//				if (root.getPackageName().equals("com.android.packageinstaller") && fl != null && qx != null) {
//					LogUtil.writeLog("found:vivo cancel1");
//					ClickUtil.clickButton(qx);
//				}
//			}

			{
				AccessibilityNodeInfo bt=AccessUtil.getChild(root, new AccessSelector().text("确定"));
				AccessibilityNodeInfo ts=AccessUtil.getChild(root, new AccessSelector().text("要卸载此应用吗*"));

				if (bt!=null&&ts!=null) {
					LogUtil.writeLog("found:vivo uninstall");
                    boolean r = ClickUtil.clickNode(bt);
//					boolean r = GestureUtil.click(bt);
					LogUtil.writeLog("found:vivo click"+r);
					AndroidUtil.sleep(200);
				}
			}

			{
				AccessibilityNodeInfo fl = AccessUtil.getChild(root, new AccessSelector().text("安全验证"));
				AccessibilityNodeInfo qx = AccessUtil.getChild(root, new AccessSelector().text("取消"));

				if (root.getPackageName().equals("com.android.packageinstaller") && fl != null && qx != null) {
					LogUtil.writeLog("found:vivo cancel2");
					ClickUtil.clickButton(qx);
				}
			}

			{
				AccessibilityNodeInfo fl=AccessUtil.getChild(root, new AccessSelector().text("安全警示"));
				AccessibilityNodeInfo qx=AccessUtil.getChild(root, new AccessSelector().text("允许安装"));

				if (root.getPackageName().equals("com.android.packageinstaller")&&fl!=null&&qx!=null) {
					LogUtil.writeLog("found:vivo alert");
					ClickUtil.clickButton(qx);
				}
			}

			{
				AccessibilityNodeInfo fl=AccessUtil.getChild(root, new AccessSelector().text("已了解应用的风险检测结果"));

				if (root.getPackageName().equals("com.android.packageinstaller")&&fl!=null) {
					LogUtil.writeLog("found:vivo alert");
					if (!fl.isChecked())
						ClickUtil.clickButton(fl);
				}
			}
		}

		//xiaomi允许安装应用
		if (Build.BRAND.equalsIgnoreCase("Coolpad") && packageName.contains("installer")) {
			AccessibilityNodeInfo ts = AccessUtil.getChild(root, new AccessSelector().text("未安装应用*"));
			AccessibilityNodeInfo bt = AccessUtil.getChild(root, new AccessSelector().text("安装失败"));

			if (bt != null && ts != null) {
				LogUtil.writeLog("found:Coolpad");
				ClickUtil.clickButton(bt);
				AndroidUtil.sleep(200);
			}
		}


		if (brand.toLowerCase().contains("xiaolajiao")||brand.toLowerCase().contains("4g")||brand.toLowerCase().contains("5g")) {
			{
				AccessibilityNodeInfo bt = AccessUtil.getChild(root, new AccessSelector().className("android.widget.CheckBox"));
				AccessibilityNodeInfo ts = AccessUtil.getChild(root, new AccessSelector().text("我已充分了解该风险，继续安装*"));

				if (bt != null && ts != null) {
					LogUtil.writeLog("found:xiaolajiao");
					if (!bt.isChecked()) {
						ClickUtil.clickButton(bt);
						AndroidUtil.sleep(200);
					}
				}
			}

			{
				AccessibilityNodeInfo bt=AccessUtil.getChild(root, new AccessSelector().text("确定"));
				AccessibilityNodeInfo ts=AccessUtil.getChild(root, new AccessSelector().text("要卸载此应用吗*"));

				if (bt!=null&&ts!=null) {
					LogUtil.writeLog("found:xiaolajiao uninstall");
					ClickUtil.clickButton(bt);
					AndroidUtil.sleep(200);
				}
			}

			{
				AccessibilityNodeInfo item=AccessUtil.getChild(root, new AccessSelector().text("应用包安装程序*"));
				if (item!=null) {
					LogUtil.writeLog("found:xiaolajiao install");
					ClickUtil.clickButton(item);
					AndroidUtil.sleep(500);
					AccessibilityNodeInfo jx=AccessUtil.getChild(root, new AccessSelector().text("始终"));
					if (jx!=null){
						ClickUtil.clickButton(jx);
						AndroidUtil.sleep(200);
					}
				}
			}
		}

		if (brand.toLowerCase().contains("doov")||brand.toLowerCase().contains("l19")) {
			AccessibilityNodeInfo bt=AccessUtil.getChild(root, new AccessSelector().className("android.widget.CheckBox"));
			AccessibilityNodeInfo ts=AccessUtil.getChild(root, new AccessSelector().text("我已充分了解该风险，继续安装*"));

			if (bt!=null&&ts!=null) {
				LogUtil.writeLog("found:doov");
				if (!bt.isChecked()) {
					ClickUtil.clickButton(bt);
					AndroidUtil.sleep(200);
				}
			}
		}

		if (brand.toLowerCase().contains("motorola")) {
			AccessibilityNodeInfo bt=AccessUtil.getChild(root, new AccessSelector().text("确定"));
			AccessibilityNodeInfo ts=AccessUtil.getChild(root, new AccessSelector().text("要卸载此应用吗*"));

			if (bt!=null&&ts!=null) {
				LogUtil.writeLog("found:motorola uninstall");
				boolean r = ClickUtil.clickNode(bt);
				LogUtil.writeLog("found:motorola click"+r);
				AndroidUtil.sleep(200);
			}
		}

		{
//			AccessibilityNodeInfo title = AccessUtil.getChild(root, new AccessSelector().text("打开方式"));
			AccessibilityNodeInfo bt = AccessUtil.getChild(root, new AccessSelector().text("软件包安装程序"));
			AccessibilityNodeInfo ts = AccessUtil.getChild(root, new AccessSelector().text("设为默认选项*"));
			AccessibilityNodeInfo ts0 = AccessUtil.getChild(root, new AccessSelector().text("通过主屏幕上*"));
			AccessibilityNodeInfo ts2 = AccessUtil.getChild(root, new AccessSelector().text("始终*"));
			if (bt != null) {
				LogUtil.writeLog("found:uc alert");
				if (ts != null&&ts0==null) {
					boolean r = ClickUtil.clickNode(ts);
					LogUtil.writeLog("found:uc alert click ts " + r);
					AndroidUtil.sleep(200);
				}
				boolean r = ClickUtil.clickNode(bt);
				LogUtil.writeLog("found:uc alert click bt " + r);
				AndroidUtil.sleep(200);
				if (ts2 != null) {
					r = ClickUtil.clickNode(ts2);
					LogUtil.writeLog("found:uc alert click ts2 " + r);
				}
			}
		}

		if (brand.toLowerCase().contains("gionee")) {
			AccessibilityNodeInfo bt=AccessUtil.getChild(root, new AccessSelector().className("android.widget.CheckBox"));
			AccessibilityNodeInfo ts=AccessUtil.getChild(root, new AccessSelector().text("我已充分了解该风险，继续安装*"));

			if (bt!=null&&ts!=null) {
				LogUtil.writeLog("found:gionee");
				if (!bt.isChecked()) {
					ClickUtil.clickButton(bt);
					AndroidUtil.sleep(200);
				}
			}
		}

		if (brand.toLowerCase().contains("coolpad")) {
			AccessibilityNodeInfo bt=AccessUtil.getChild(root, new AccessSelector().className("android.widget.CheckBox"));
			AccessibilityNodeInfo ts=AccessUtil.getChild(root, new AccessSelector().text("我已充分了解该风险，继续安装*"));

			if (root.getPackageName().equals("com.zhuoyi.security.service")&&bt!=null&&ts!=null) {
				LogUtil.writeLog("found:coolpad new");
				if (!bt.isChecked()) {
					ClickUtil.clickButton(bt);
					AndroidUtil.sleep(200);
				}
			}
		}

		if (APhoneParams.checkKeyString != null) {
			List<AccessibilityNodeInfo> btns = AccessUtil.getChildren(root, new AccessSelector().text(APhoneParams.checkKeyString).checkable(true).inListView(false));
			if (btns != null) {
				for (AccessibilityNodeInfo button : btns) {

					if (!button.isChecked()) {
						ClickUtil.clickButton(button);

						LogUtil.writeLog("found:" + button.getText());
						AndroidUtil.sleep(200);
					}
				}
			}
		}

		if (APhoneParams.checkResString != null) {
			List<AccessibilityNodeInfo> btns = AccessUtil.getChildren(root, new AccessSelector().resourceId(APhoneParams.checkResString).checkable(true).inListView(false));
			if (btns != null) {
				for (AccessibilityNodeInfo button : btns) {

					if (!button.isChecked()) {
						ClickUtil.clickButton(button);

						LogUtil.writeLog("found:" + button.getText());
						AndroidUtil.sleep(200);
					}
				}
			}
		}

		do {
			if (APhoneParams.closeKeyInAllString == null) break;

			List<AccessibilityNodeInfo> btns = AccessUtil.getChildren(root, new AccessSelector().text(APhoneParams.closeKeyInAllString).checkable(false));
			if (btns == null) break;
			for (AccessibilityNodeInfo button : btns) {
				while (!button.isClickable()) {
					button = button.getParent();
					if (button == null) break;
				}
				if (button == null) continue;
				if (AccessUtil.getChild(button, new AccessSelector().checkable(true)) != null)
					continue;

				ClickUtil.clickNode(button);
				AndroidUtil.sleep(200);
				LogUtil.writeLog("found:" + button.getText());
				break;
			}
		} while (false);

		int cnt = 0;
		do {
//			LogUtil.writeLog("==>" + APhoneParams.closeKeyString);
			if (APhoneParams.closeKeyString == null) break;

			List<AccessibilityNodeInfo> btns = AccessUtil.getChildren(root, new AccessSelector().text(APhoneParams.closeKeyString).checkable(false).inListView(false));
			if (btns == null) break;
			boolean writeClick = false;
			for (AccessibilityNodeInfo button : btns) {
				if (button.getClassName().toString().contains("Switch")) continue;

				while (!button.isClickable()) {
					button = button.getParent();
					if (button == null) break;
				}
				if (button == null) continue;
				if (!button.isEnabled()) continue;
				if (AccessUtil.getChild(button, new AccessSelector().checkable(true)) != null)
					continue;

				boolean clicked = false;

				//特殊处理,OPPO r11 开始检测广告，并且只能通过 input 进行点击
				CharSequence textSeq = button.getText();
				if(textSeq != null) {
					String text = button.getText().toString();
					if(text.equals("我已知问题严重性。无视风险安装")) {
						//必须要点 四分之三处
						Rect rect = new Rect();
						button.getBoundsInScreen(rect);
						GestureUtil.click(rect.centerX() + (rect.right - rect.left) / 4,rect.centerY());
						AndroidUtil.sleep(500);
						clicked = true;
						writeClick = true;
					}
				}

				//如果之前没有点过，则继续点
				if (!clicked) {
					ClickUtil.clickNode(button);
					AndroidUtil.sleep(200);
					writeClick = true;
				}
				LogUtil.writeLog("found:" + textSeq);
				cnt += 1;
				break;
			}
//			if(writeClick) {
//				ShellUtil.writeFileToSystem("/data/local/tmp/click", "");
//			}
		} while (false);

		//临时增加字符串
		do {
			if (APhoneParams.closeAddString == null) break;

			List<AccessibilityNodeInfo> btns = AccessUtil.getChildren(root, new AccessSelector().text(APhoneParams.closeAddString).checkable(false));
			if (btns == null) break;
			for (AccessibilityNodeInfo button : btns) {
				while (!button.isClickable()) {
					button = button.getParent();
					if (button == null) break;
				}
				if (button == null) continue;
				if (AccessUtil.getChild(button, new AccessSelector().checkable(true)) != null)
					continue;

				ClickUtil.clickNode(button);
				AndroidUtil.sleep(200);
				LogUtil.writeLog("found:" + button.getText());
				break;
			}
		} while (false);

		int checkTimes = 0;
		for (; checkTimes < 10; checkTimes++) {
			if (APhoneParams.closeKeyCheckString == null) break;

			List<AccessibilityNodeInfo> btns = AccessUtil.getChildren(root, new AccessSelector().text(APhoneParams.closeKeyCheckString).checkable(false));
			if (btns == null) break;
			for (AccessibilityNodeInfo button : btns) {
				while (!button.isClickable()) {
					button = button.getParent();
					if (button == null) break;
				}
				if (button == null) continue;
				if (AccessUtil.getChild(button, new AccessSelector().checkable(true)) != null)
					continue;
				boolean clicked = false;
				CharSequence textSeq = button.getText();
				if(textSeq != null) {
					String text = button.getText().toString();
					if(text.equals("我已知问题严重性。无视风险安装")) {
						//必须要点 四分之三处
						Rect rect = new Rect();
						button.getBoundsInScreen(rect);
						GestureUtil.click(rect.centerX() + (rect.right - rect.left) / 4,rect.centerY());
						AndroidUtil.sleep(500);
						clicked = true;
					}
				}

				//如果之前没有点过，则继续点
				if (!clicked) {
					ClickUtil.clickNode(button);
				}
				LogUtil.writeLog("found:" + button.getText());
				break;
			}
			AndroidUtil.sleep(500);
		}
		if (checkTimes >= 10) {
			AccessServiceUtil.goActivityBack();
		}
		return cnt;
	}

	static public void closeAllDialogForTime(int duration) {

		int forTimes = (duration + 499) / 500;

		int times = 0;
		while (true) {
			int n = closeDialogWindow();
			if (n > 0) continue;

			times += 1;
			if (times > forTimes) break;
			AndroidUtil.sleep(500);
		}
	}

	static public boolean closeAllDialog() {
		if(!AccessServiceUtil.isValid()) return false;

		LogUtil.writeLog(">>>closeAllDialog");
		try {
			while(true) {
				int n = closeDialogWindow();
				if(n == 0) break;
				AndroidUtil.sleep(200);
			}
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.writeLog("closeAllDialog exception: " + e.toString());
		}
		return true;
	}

	static class CloseAllDialog {
		static boolean mKeepRunInstall = false;
		static Thread mRunInstall = null;

		static void start() {
			if(mRunInstall != null) return;

			mRunInstall = new Thread(new Runnable() {
				@Override
				public void run() {
					mKeepRunInstall = true;
					while (mKeepRunInstall) {
						if(!ClickUtil.closeAllDialog()) break;
						AndroidUtil.sleep(500);
					}
				}
			});
			mRunInstall.start();
			while(!mKeepRunInstall) {
				AndroidUtil.sleep(500);
			}
		}

		static void stop() {
			if(mRunInstall == null) return;
			if(!mRunInstall.isAlive()) {
				mRunInstall = null;
				mKeepRunInstall = false;
				return;
			}
			mKeepRunInstall = false;
			try {
				mRunInstall.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			mRunInstall = null;
			LogUtil.writeLog(">>>closeAllDialog stop");
		}
	}

	public static synchronized void startCloseDialog() {
		CloseAllDialog.start();
		AndroidUtil.sleep(500);
	}

	public static synchronized void stopCloseDialog() {
		CloseAllDialog.stop();
		AndroidUtil.sleep(500);
	}

	/**
	 * 在同一个界面中连续点击
	 */
	public static boolean clickButtons(String... buttons) {

		boolean retVal = true;
		for(int ii=0;ii<buttons.length;ii++) {
			String text = buttons[ii];
			if(text == null || text.length() == 0) continue;
			LogUtil.writeLog(String.format("click[2] %s", text));
			if(!waitAndClickButton(new AccessSelector().text(text))) {
				retVal = false;
				break;
			}
		}
		return retVal;
	}

}
