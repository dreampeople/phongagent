package com.melon.phoneagent.autil;

import android.annotation.SuppressLint;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;

import com.melon.util.AndroidUtil;
import com.melon.util.FileUtil;
import com.melon.util.LogUtil;
import com.melon.util.StringUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@SuppressLint("NewApi")
public class AccessUtil {
	public static String getContentDescription(AccessibilityNodeInfo node) {
		if(node == null) return "";

		CharSequence cs =  node.getContentDescription();
		if(cs == null) return "";

		return cs.toString();
	}
	public static String getText(AccessibilityNodeInfo node) {
		if(node == null) return "";

		CharSequence cs = node.getText();
		if(cs == null) return "";

		return cs.toString().trim();
	}

	public static void dumpNode2File(String fileName) {
		dumpNode2File(AccessServiceUtil.getRootNode(), LogUtil.getNodeDumpFile(fileName));
	}
	public static void dumpNode2File(AccessibilityNodeInfo node, File file) {
		String xml = dumpNodeXML(node);
		FileUtil.writeFile(file, xml);
	}

	public static String dumpNodeXML(AccessibilityNodeInfo node) {
		StringBuilder sb = new StringBuilder();

		dumpNodeXML(node , sb, 0);

		return sb.toString();
	}

	public static String dumpNodeXML() {

		return dumpNodeXML(AccessServiceUtil.getRootNode());
	}

	static void dumpNodeXML(AccessibilityNodeInfo node, StringBuilder sb, int nn) {
		if(node == null) {
			sb.append("null");
			return;
		}

		CharSequence className = node.getClassName();
		String[] ss = className.toString().split("\\.");
		if(ss.length == 0) {
			sb.append(className);
			return;
		}
		String nodeName = ss[ss.length - 1];

		for(int ii=0;ii<nn;ii++) sb.append("\t");
		sb.append("<");
		sb.append(nodeName);


		sb.append(" class=\"");
		sb.append(className);
		sb.append("\"");

		CharSequence cs = null;
		cs = node.getText();
		if(cs != null) {
			sb.append(" text=\"");
			sb.append(node.getText());
			sb.append("\"");
		}

		cs = node.getPackageName();
		if(cs != null) {
			sb.append(" package=\"");
			sb.append(cs);
			sb.append("\"");
		}

		cs = node.getViewIdResourceName();
		if(cs != null) {
			sb.append(" resource-id=\"");
			sb.append(cs);
			sb.append("\"");
		}

		cs = node.getContentDescription();
		if(cs != null) {
			sb.append(" content-description=\"");
			sb.append(cs);
			sb.append("\"");
		}

		if(node.isVisibleToUser()) {
			sb.append(" visible=\"true\"");
		} else {
			sb.append(" visible=\"false\"");
		}

		if(node.isClickable()) {
			sb.append(" clickable=\"true\"");
		} else {
			sb.append(" clickable=\"false\"");
		}
		if(node.isCheckable()) {
			sb.append(" checkable=\"true\"");
		} else {
			sb.append(" checkable=\"false\"");
		}

		if(node.isChecked()) {
			sb.append(" checked=\"true\"");
		} else {
			sb.append(" checked=\"false\"");
		}

		if(node.isEnabled()) {
			sb.append(" enabled=\"true\"");
		} else {
			sb.append(" enabled=\"false\"");
		}

		if(node.isFocusable()) {
			sb.append(" focusable=\"true\"");
		} else {
			sb.append(" focusable=\"false\"");
		}

		if(node.isFocused()) {
			sb.append(" focused=\"true\"");
		} else {
			sb.append(" focused=\"false\"");
		}

		if(node.isScrollable()) {
			sb.append(" scrollable=\"true\"");
		} else {
			sb.append(" scrollable=\"false\"");
		}

		if(node.isLongClickable()) {
			sb.append(" longclickable=\"true\"");
		} else {
			sb.append(" longclickable=\"false\"");
		}

		if(node.isPassword()) {
			sb.append(" password=\"true\"");
		} else {
			sb.append(" password=\"false\"");
		}

		if(node.isSelected()) {
			sb.append(" selected=\"true\"");
		} else {
			sb.append(" selected=\"false\"");
		}

		Rect rect = new Rect();
		node.getBoundsInParent(rect);
		StringBuilder rsb = new StringBuilder();
		rsb.append(rect.left);
		rsb.append(","); rsb.append(rect.top);
		rsb.append(","); rsb.append(rect.right);
		rsb.append(","); rsb.append(rect.bottom);

		sb.append(" boundInP=\"" + rsb + "\"");

		node.getBoundsInScreen(rect);
		rsb = new StringBuilder();
		rsb.append(rect.left);
		rsb.append(","); rsb.append(rect.top);
		rsb.append(","); rsb.append(rect.right);
		rsb.append(","); rsb.append(rect.bottom);
		sb.append(" boundInS=\"" + rsb + "\"");

		int n = node.getChildCount();
		if(n == 0) {
			sb.append(" />\n");
			return;
		} else {
			sb.append(">\n");
		}

		for(int ii=0;ii<n;ii++) {
			AccessibilityNodeInfo ni = node.getChild(ii);
			dumpNodeXML(ni, sb, nn + 1);
		}

		for(int ii=0;ii<nn;ii++) sb.append("\t");
		sb.append("</" + nodeName + ">\n");
	}

	public static boolean isListView(AccessibilityNodeInfo node) {
		if(node.isScrollable()) return true;
		
		String className = node.getClassName().toString();
		if(className == null) return false;
		if(className.equals("android.widget.ListView")) return true;
		if(className.equals("android.widget.GridView")) return true;
		if(className.equals("android.widget.ScrollView")) return true;
		return false;
	}
	
	public static boolean inListView(AccessibilityNodeInfo node) {
		if(node == null) return false;
		
		while(node != null) {
			node = node.getParent();
			if(node == null) return false;
			if(node.isScrollable()) return true;
		}
		
		return false;
	}
	
	static boolean getChildren(AccessibilityNodeInfo node, AccessSelector selector, List<AccessibilityNodeInfo> rs, int index, boolean inListView, boolean onlyOne) {
		return getChildren(node, selector, rs, index, inListView, onlyOne, false);
	}

	public static List<AccessibilityNodeInfo> getChildren(AccessibilityNodeInfo node,AccessSelector selector,int forlevel) {
		if(node == null) return null;
		List<AccessibilityNodeInfo> rs = new ArrayList<AccessibilityNodeInfo>();
		
		getChildren(node, selector, rs, -1, false, false, false, forlevel, 0);
		
		if(rs.size() == 0) return null;
		return rs;
	}

	public static List<AccessibilityNodeInfo> getChildren(AccessibilityNodeInfo node,AccessSelector selector) {
		if(node == null) return null;
		List<AccessibilityNodeInfo> rs = new ArrayList<AccessibilityNodeInfo>();

		getChildren(node, selector, rs, -1, false, false);

		if(rs.size() == 0) return null;
		return rs;
	}

	public static List<AccessibilityNodeInfo> getChildren(AccessSelector selector) {
		AccessibilityNodeInfo node = AccessServiceUtil.getRootNode();
		return getChildren(node, selector);
	}

	public static List<AccessibilityNodeInfo> getChildren(List<AccessibilityNodeInfo> nodes, AccessSelector selector) {

		List<AccessibilityNodeInfo> rs = new ArrayList<AccessibilityNodeInfo>();
		for(AccessibilityNodeInfo node: nodes) {
			getChildren(node, selector, rs, -1, false, false);
		}
		if(rs.size() == 0) return null;
		return rs;
	}

	static boolean getChildren(AccessibilityNodeInfo node, AccessSelector selector, List<AccessibilityNodeInfo> rs, int index, boolean inListView, boolean onlyOne, boolean isstar, int forlevel, int level) {
		if(node == null) return false;
		if(forlevel >= 0 && level >= forlevel) {
			return false;
		}

		if(selector.isVisible != 0) {
			if(!node.isVisibleToUser()) return false;
		}
		if(selector.inListView >= 0 && !inListView) {
			inListView = isListView(node);
			
			//如果不能再listview里,而当前也是listview则退出
			if(inListView && selector.inListView == 0) return false;
		}
		
		boolean found = true;
		while(selector != null) {
			if(selector.inListView >= 0) {
				if(!inListView && selector.inListView != 0) {
					found = false;
					break;
				}
			}

			if(selector.index >= 0 && index >= 0) {
				if(selector.index != index) {
					found = false;
					break;
				}
			}
			
			if(selector.text != null) {
				CharSequence text = node.getText();
				if(text == null) {
					found = false;
					break;
				}
				if(!StringUtil.isTextMatch(text.toString().trim(), selector.text.trim(),isstar)) {
					found = false;
					break;
				}
			}
			if(selector.className != null) {
				CharSequence className = node.getClassName();
				if(className == null) {
					found = false;
					break;
				}
				if(!StringUtil.isTextMatch(className.toString().trim(), selector.className.trim())) {
					found = false;
					break;
				}
			}
			if(selector.resourceId != null) {
				String resourceId = node.getViewIdResourceName();
				if(resourceId == null) {
					found = false;
					break;
				}
				if(!StringUtil.isTextMatch(resourceId, selector.resourceId)) {
					found = false;
					break;
				}
			}
			if(selector.packageName != null) {
				CharSequence packageName = node.getPackageName();
				if(packageName == null) {
					found = false;
					break;
				}
				if(!StringUtil.isTextMatch(packageName.toString().trim(), selector.packageName.trim())) {
					found = false;
					break;
				}
			}
			if(selector.contentDesc != null) {
				CharSequence contentDesc = node.getContentDescription();
				if(contentDesc == null) {
					found = false;
					break;
				}
				if(!StringUtil.isTextMatch(contentDesc.toString().trim(), selector.contentDesc.trim(),isstar)) {
					found = false;
					break;
				}
			}
			if(selector.isClickable >= 0) {
				if(node.isClickable() != (selector.isClickable != 0)) {
					found = false;
					break;
				}
			}
			if(selector.isCheckable >= 0) {
				if(node.isCheckable() != (selector.isCheckable != 0)) {
					found = false;
					break;
				}
			}
			if(selector.isChecked >= 0) {
				if(node.isChecked() != (selector.isChecked != 0)) {
					found = false;
					break;
				}
			}
			if(selector.isEnabled >= 0) {
				if(node.isEnabled() != (selector.isEnabled != 0)) {
					found = false;
					break;
				}
			}
			if(selector.isFocusable >= 0) {
				if(node.isFocusable() != (selector.isFocusable != 0)) {
					found = false;
					break;
				}
			}
			if(selector.isFocused >= 0) {
				if(node.isFocused() != (selector.isFocused != 0)) {
					found = false;
					break;
				}
			}
			if(selector.isScrollable >= 0) {
				if(node.isScrollable() != (selector.isScrollable != 0)) {
					found = false;
					break;
				}
			}
			if(selector.isLongClickable >= 0) {
				if(node.isLongClickable() != (selector.isLongClickable != 0)) {
					found = false;
					break;
				}
			}
			if(selector.isPassword >= 0) {
				if(node.isPassword() != (selector.isPassword != 0)) {
					found = false;
					break;
				}
			}
			if(selector.isSelected >= 0) {
				if(node.isSelected() != (selector.isSelected != 0)) {
					found = false;
					break;
				}
			}
			break;
		}
		
		if(found) {
			rs.add(node);
			if(onlyOne) return true;
		}
		
		level ++;
		int cnt = node.getChildCount();
		for(int ii=0;ii<cnt;ii++) {
			getChildren(node.getChild(ii), selector, rs, ii, inListView, onlyOne,isstar,forlevel,level);
		}
		return true;
	}

	static boolean getChildren(AccessibilityNodeInfo node,AccessSelector selector, List<AccessibilityNodeInfo> rs, int index, boolean inListView, boolean onlyOne,boolean isstar) {
		return getChildren(node, selector, rs, index, inListView, onlyOne, isstar, -1, 0);
	}

	public static List<AccessibilityNodeInfo> getChildren(AccessibilityNodeInfo node, boolean visible) {
		List<AccessibilityNodeInfo> nodes = new ArrayList<>();
		int nn = node.getChildCount();
		for(int ii=0;ii<nn;ii++) {
			AccessibilityNodeInfo cnode = node.getChild(ii);
			if(visible && !cnode.isVisibleToUser()) continue;
			nodes.add(node.getChild(ii));
		}

		return nodes;
	}

	public static List<AccessibilityNodeInfo> getChildren(AccessibilityNodeInfo node) {
		return getChildren(node, false);
	}

	public static AccessibilityNodeInfo getChild(AccessibilityNodeInfo node,AccessSelector selector,int forlevel) {
		return getChild(node, selector, false, forlevel);
	}
	
	public static AccessibilityNodeInfo getChild(AccessibilityNodeInfo node,AccessSelector selector) {
		return getChild(node, selector, false, -1);
	}

	public static AccessibilityNodeInfo getChild(AccessibilityNodeInfo node,AccessSelector selector,boolean isstar, int forlevel) {
		if(node == null) return null;
		List<AccessibilityNodeInfo> rs = new ArrayList<AccessibilityNodeInfo>();
		getChildren(node, selector, rs, -1, false, true,isstar,forlevel,0);
		if(rs.size() == 0) return null;

		return rs.get(0);
	}
	
	public static AccessibilityNodeInfo getChild(AccessibilityNodeInfo node,AccessSelector selector,boolean isstar) {
		return getChild(node, selector, isstar, -1);
	}
	
	public static AccessibilityNodeInfo getChild(AccessSelector selector,boolean isstar ) {
		AccessibilityNodeInfo node = AccessServiceUtil.getRootNode();
		return getChild(node, selector,isstar);
	}

	public static AccessibilityNodeInfo getChild(AccessSelector selector) {
		return getChild(selector, false);
	}

	public static AccessibilityNodeInfo waitNode(AccessibilityNodeInfo node, AccessSelector selector, int timeout) {
		int tm = 0;
		AccessibilityNodeInfo child = null;
		while(true) {
			if(tm >= timeout) break;
			if(node == null) {
				child = getChild(selector);
			} else {
				child = getChild(node, selector);
			}
			if(child != null) return child;
			AndroidUtil.sleep(100);
			tm += 100;
		}
		return null;
	}

	public static AccessibilityNodeInfo waitNode(AccessSelector selector, int timeout) {
		return waitNode(null, selector, timeout);
	}

	public static AccessibilityNodeInfo waitNode(AccessibilityNodeInfo node, AccessSelector selector) {
		return waitNode(node, selector, 3000);
	}

	public static AccessibilityNodeInfo waitNode(AccessSelector selector) {
		return waitNode(selector, 3000);
	}

	public static boolean waitNodeAway(AccessibilityNodeInfo node, AccessSelector selector, int timeout) {
		int tm = 0;
		AccessibilityNodeInfo child = null;
		while(true) {
			if(tm >= timeout) break;
			if(node == null) {
				child = getChild(selector);
			} else {
				child = getChild(node, selector);
			}
			if(child == null) return true;
			AndroidUtil.sleep(500);
			tm += 500;
		}
		return false;
	}

	public static boolean waitNodeAway(AccessSelector selector, int timeout) {
		return waitNodeAway(null, selector, timeout);
	}
	
	public static AccessibilityNodeInfo getNodeAtPoint(AccessibilityNodeInfo node, Point point) {
		if(node == null) node = AccessServiceUtil.getRootNode();
		if(node == null) return null;

		Rect rect = new Rect();
		int num = node.getChildCount();
		for(int idx=0;idx<num;idx++) {
			AccessibilityNodeInfo child = node.getChild(idx);
			child = getNodeInBound(child, rect);
			if(child != null) return child;
		}

		return null;
	}

	public static AccessibilityNodeInfo getNodeInBound(AccessibilityNodeInfo node, Rect rect) {
		if(node == null) node = AccessServiceUtil.getRootNode();
		if(node == null) return null;

		Rect r = new Rect();
		node.getBoundsInScreen(r);
		if(rect.contains(r)) return node;

		int num = node.getChildCount();
		for(int idx=0;idx<num;idx++) {
			AccessibilityNodeInfo child = node.getChild(idx);
			child = getNodeInBound(child, rect);
			if(child != null) return child;
		}

		return null;
	}

	public static AccessibilityNodeInfo getNodeInBound(Rect rect) {
		return getNodeInBound(null, rect);
	}

	public static boolean iscenter(AccessibilityNodeInfo root,AccessibilityNodeInfo info){
		Rect rect1 = new Rect();
		root.getBoundsInScreen(rect1);
		int x1=rect1.centerX();
		int y1=rect1.centerY();
		LogUtil.writeLog("root:"+x1+","+y1);
		return  iscenter(x1,y1,info);
	}

	public static boolean isleft(int x,int y,AccessibilityNodeInfo info){
		LogUtil.writeLog("root:"+x+","+y);

		Rect rect = new Rect();
		info.getBoundsInScreen(rect);
		int x1=rect.left;
		int centery=rect.centerY();
		LogUtil.writeLog("info:"+x1+",centery:"+centery);

		return  x1<=100&&centery>=y;
	}

	public static boolean iscenter(int x,int y,AccessibilityNodeInfo info){
		LogUtil.writeLog("root:"+x+","+y);
		Rect rect2 = new Rect();
		info.getBoundsInScreen(rect2);
		int x2=rect2.centerX();
		int y2=rect2.centerY();
		LogUtil.writeLog("info:"+x2+","+y2);

		double result=Math.sqrt(Math.abs((x - x2)* (x - x2)+(y - y2)* (y - y2)));
		return  result<=110;
	}

	public static boolean isleft(AccessibilityNodeInfo root,AccessibilityNodeInfo info){
		Rect rect1 = new Rect();
		root.getBoundsInScreen(rect1);
		int x1=rect1.centerX();
		int y1=rect1.centerY();
		LogUtil.writeLog("root:"+x1+","+y1);

		return  isleft(x1,y1,info);
	}

	public static boolean canFindChildByText(AccessibilityNodeInfo node,String text) {
		if(StringUtil.isEmpty(text)) return false;

		if(node == null) node = AccessServiceUtil.getRootNode();
		List<AccessibilityNodeInfo> rs = getChildren(node, new AccessSelector().text(text));
		if(rs != null && rs.size() > 0) return true;
		
		return false;
	}

	public static boolean canFindChildByText(String text) {
		return canFindChildByText(null, text);
	}

	public static boolean canFindChildByContentDesc(AccessibilityNodeInfo node,String contentdesc) {
		if(StringUtil.isEmpty(contentdesc)) return false;

		if(node == null) node = AccessServiceUtil.getRootNode();
		List<AccessibilityNodeInfo> rs = getChildren(node, new AccessSelector().contentDesc(contentdesc));
		if(rs != null && rs.size() > 0) return true;

		return false;
	}
	
	public static boolean canFindChildByContentDesc(String contentdesc) {
		return canFindChildByContentDesc(null, contentdesc);
	}

	/**
	 * 获取node里的所有text中间函数，用来递归
	 * @param node
	 * @param rs
	 * @param andDesc 是否含desc
	 * @return
	 */
	static boolean getAllText(AccessibilityNodeInfo node, List<String> rs, boolean andDesc) {
		if(node == null) return false;
		if(!node.isVisibleToUser()) return false;
		
		CharSequence text = node.getText();
		if(text != null && text.length() > 0) {
			rs.add(text.toString().trim());
		}
		text = node.getContentDescription();
		if(text != null && text.length() > 0) {
			rs.add(text.toString());
		}

		int cnt = node.getChildCount();
		for(int ii=0;ii<cnt;ii++) {
			getAllText(node.getChild(ii), rs, andDesc);
		}
		return true;
	}

	/**
	 * 获取node里的所有text
	 * @param node
	 * @param andDesc 是否含desc
	 * @return
	 */
	public static List<String> getAllText(AccessibilityNodeInfo node, boolean andDesc) {
		if(node == null) return null;
		ArrayList<String> rs = new ArrayList<String>();
		
		getAllText(node, rs, andDesc);
		if(rs.size() == 0) return null;
		return rs;
	}

	/**
	 * 获取node里的所有text(不含desc)
	 * @param node
	 * @return
	 */
	public static List<String> getAllText(AccessibilityNodeInfo node) {
		return getAllText(node, false);
	}

	/**
	 * 获取node节点串的中间函数，用来递归
	 * @param node
	 * @param serial
	 */
	private static void getXMLSerial(AccessibilityNodeInfo node, StringBuilder serial) {
		if(node == null) return;
		if(!node.isVisibleToUser()) return;

		Rect rect = new Rect();
		node.getBoundsInScreen(rect);
		if(serial.length() > 0) serial.append("|");
		serial.append(rect.toShortString());
		CharSequence cs = node.getText();
		if(cs != null) {
			serial.append(",");
			serial.append(cs.toString().trim());
		}
		cs = node.getContentDescription();
		if(cs != null) {
			serial.append(",");
			serial.append(cs);
		}
		if(node.isChecked()) {
			serial.append(",1");
		}

		for(int ii=0;ii<node.getChildCount();ii++) {
			AccessibilityNodeInfo nnode = node.getChild(ii);
			if(nnode == null) continue;
			getXMLSerial(nnode, serial);
		}
	}

	/**
	 * 获取node的序列
	 * @return
	 */
	public static String getXMLSerial(AccessibilityNodeInfo node) {
		if(node == null) node = AccessServiceUtil.getRootNode();
		if(node == null) return null;

//		LogUtil.writeLog(">>>\n" + dumpNodeXML(node));
		StringBuilder sb = new StringBuilder();
		getXMLSerial(node, sb);
		return sb.toString();
	}

	/**
	 * 获取界面序列
	 * @return
	 */
	public static String getXMLSerial() {
		String serial = getXMLSerial(null);
		for(int ii=0;serial == null && ii<10;ii++) {
			AndroidUtil.sleep(50);
			serial = AccessUtil.getXMLSerial(null);
		}

		return serial;
	}

	/**
	 * 等待节点处于静止状态
	 * @param oldSerial 原来的界面序列
	 * @param timeout
	 * @return
	 */
	public static boolean waitNextStill(String oldSerial, int waitTime, int timeout, int afterTime) {
		int sleepTime = 100;
		int times = timeout /sleepTime;

		//先休眠一段时间,等待页面动起来
		if(waitTime > 0) {
			AndroidUtil.sleep(waitTime);
		}

		//获取根节点
		if(AccessServiceUtil.getRootNode(timeout) == null) {
			return true;
		}

		//等待界面动
		if(oldSerial != null) {
			for(int waitnum=0;waitnum<times;waitnum++) {
				String serial = AccessUtil.getXMLSerial();
				if(serial == null) {
					AndroidUtil.sleep(sleepTime);
					continue;
				}
				//界面动了，用当前的serial替换原来的serial
				if(!oldSerial.equals(serial)) {
					oldSerial = serial;
					break;
				}

				AndroidUtil.sleep(sleepTime);
			}
		} else {
			oldSerial = AccessUtil.getXMLSerial();
		}
		//保证当前的界面序列不为空
		if(oldSerial == null) {
			return true;
		}

		//等待两个间隔间的serial相同，认为是界面静止了
		for(int waitnum=0;waitnum<times;waitnum++) {
			AndroidUtil.sleep(sleepTime);
			String serial = AccessUtil.getXMLSerial();
			if(serial == null) {
				continue;
			}
			if(oldSerial.equals(serial)) {
				break;
			}

			oldSerial = serial;
		}

		//静止后再等待多长时间
		if(afterTime > 0) AndroidUtil.sleep(afterTime);

		return true;
	}

	/**
	 * 等待下一个静止的界面(等待3秒钟)
	 * @param serial 当前界面的serial
	 * @return
	 */
	public static boolean waitNextStill(String serial) {
		//先等待100毫秒
		//再等待3秒钟界面静止
		//静止后再等待0秒
		return waitNextStill(serial, 100, 3000, 100);
	}

	static String serialString_ = null;
	public static void prepareNextStill() {
		serialString_ = getXMLSerial();
	}
	public static boolean waitNextStill() {
		//先等待100毫秒
		//再等待3秒钟界面静止
		//静止后再等待0秒
		String s = serialString_;
		serialString_ = null;
		return waitNextStill(s, 100, 5000, 100);
	}

	/**
	 * 等待界面处于静止状态(等待3秒钟)
	 * @return
	 */
	public static boolean waitForStill() {
		//先等待100毫秒
		//再等待3秒钟界面静止
		//静止后再等待100秒
		return waitNextStill(null, 100, 3000, 100);
	}

	public static boolean waitSleepAndStill(int sleepBefore, int sleepAfter) {
		return waitNextStill(null, sleepBefore + 100, 3000, sleepAfter);
	}

	public static boolean waitSleepAndStill() {
		return waitSleepAndStill(200, 200);
	}

	public static AccessibilityNodeInfo update(AccessibilityNodeInfo node) {
		return node;
	}
}
