package com.melon.phoneagent.autil;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;

import com.melon.util.AndroidUtil;
import com.melon.util.LogUtil;
import com.melon.util.StringUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressLint({ "NewApi", "DefaultLocale" })
public class ScanListView {

	public static boolean useGesture = false;
	public static int itemTimes = 1;
	public static boolean contentForChild = false;

	public static class ItemsUtil {
		Set<String> itemNames = new HashSet<>();

		public ItemsUtil() {
		}

		public String getValid(List<String> texts, String matchString, boolean onlyTime) {
			if(texts == null) return null;

			boolean exclude = false;
			if(!StringUtil.isEmpty(matchString) && matchString.startsWith("^")) {
				if(matchString.length() == 1) {
					//空串不允许翻转
					matchString = null;
				} else {
					matchString = matchString.substring(1);
					exclude = true;
				}
			}
			String title;
			if(matchString == null) {
				title = StringUtil.join(",", texts);
			} else {
				title = StringUtil.getListMatchString(texts, matchString);
				if(exclude) {   //翻转
					if(title == null) {
						title = StringUtil.join(",", texts);
					} else {
						title = null;
					}
				}
			}
			if(title == null) return null;

			if(!onlyTime) return title;

			if(itemNames.contains(title)) return null;
			itemNames.add(title);

			return title;
		}

		public String getValid(List<String> texts, String matchString) {
			return getValid(texts, matchString, true);
		}
	};

	public interface ScanListViewInterface {

		public AccessibilityNodeInfo getListView();
		public AccessibilityNodeInfo getItem(AccessibilityNodeInfo listView, int pos);
		public boolean scanItem(AccessibilityNodeInfo item, int pos);
	}

	public interface ScanListViewEasyInterface {

		public AccessibilityNodeInfo getListView();
		public AccessibilityNodeInfo getItem(AccessibilityNodeInfo listView, int pos);
		public boolean scanItem(AccessibilityNodeInfo item, int pos, String title, ItemsUtil iu);
	}

	public interface ScanListViewSimpleInterface {
		public boolean scanItem(AccessibilityNodeInfo item, int pos);
	}
	
	public interface ScanListViewEasySimpleInterface {
		public boolean scanItem(AccessibilityNodeInfo item, int pos, String title, ItemsUtil iu);
	}

	public interface ScanListViewSimpleInterface2 {

		public AccessibilityNodeInfo getItem(AccessibilityNodeInfo listView, int pos);
		public boolean scanItem(AccessibilityNodeInfo item, int pos);
	}

	public interface ScanListViewEasySimpleInterface2 {

		public AccessibilityNodeInfo getItem(AccessibilityNodeInfo listView, int pos);
		public boolean scanItem(AccessibilityNodeInfo item, int pos, String title, ItemsUtil iu);
	}

	/**
	 * 获取一个listview
	 * @param waitReady 等待里面的有条目
	 * @return
	 */
	public static AccessibilityNodeInfo getListView(boolean waitReady) {
		AccessibilityNodeInfo listView = null;
		for(int times = 0;times<6;times++) {
//			if(closeDialog) {
//				ClickUtil.closeAllDialog();
//			}
			listView = AccessUtil.getChild(new AccessSelector().className("androidx.recyclerview.widget.RecyclerView|android.widget.ListView|android.widget.ExpandableListView|android.widget.GridView|android.widget.ScrollView|flyme.support.v7.widget.RecyclerView|android.support.v7.widget.RecyclerView|com.zte.mifavor.support.v7.widget.RecyclerView"));
			if(listView == null) listView = AccessUtil.getChild(new AccessSelector().scrollable(true));
			if(listView != null) break;
			
			AndroidUtil.sleep(500);
		}
		if(listView == null) return null;
		if(!waitReady) return listView;

		for(int times = 0;times<6;times++) {
			
			if(listView.getChildCount() > 0) return listView;
			
			AndroidUtil.sleep(500);
		}

		if(listView != null && contentForChild && listView.getChildCount() == 1) {
			listView = listView.getChild(0);
		}

		return listView;
	}

	public static AccessibilityNodeInfo getListView() {
		return getListView(true);
	}
	
	static AccessibilityNodeInfo getListView(ScanListViewInterface inter) {
		AccessibilityNodeInfo listView = inter.getListView();
		if(listView == null) listView = getListView();
		return listView;
	}

	static AccessibilityNodeInfo loopCurrentItems(ScanListViewInterface inter) {
		int pos = 0;
		AccessibilityNodeInfo lastItem = null;
		int times =0;
		while(true) {
			AccessibilityNodeInfo listView = getListView(inter);
			if(listView == null) {
				LogUtil.writeLog("listview is null!!=in loop");
				return null;
			}

			AccessibilityNodeInfo item = inter.getItem(listView, pos);
			if(item == null && pos < listView.getChildCount()) {
				item = listView.getChild(pos);
				if(item == null) {
					AndroidUtil.sleep(500);
					continue;
				}
				if(!item.isVisibleToUser()) {
					pos ++;
					continue;
				}
			}
			if(item == null) {
				LogUtil.writeLog("count = " + listView.getChildCount());
				break;
			}
			LogUtil.writeLog("scan listview pos = " + pos);
			if(!inter.scanItem(item, pos)) return null;
			lastItem = item;
			times ++;
			if(times < itemTimes) continue;
			pos ++;
			times = 0;
		}
		return lastItem;
	}

	static boolean scrollForward(ScanListViewInterface inter, AccessibilityNodeInfo lastItem) {

		LogUtil.writeLog("getListView....");
		AccessUtil.waitForStill();
		AccessibilityNodeInfo listView = getListView(inter);
		if(listView == null) {
			LogUtil.writeLog("listview is null");
			return false;
		}
		
		if(!useGesture || !GestureUtil.isValid()) {
			if(listView.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)) {
				LogUtil.writeLog("forward.....");
				return true;
			}

			AndroidUtil.sleep(500);
//			AccessUtil.waitSleepAndStill();
//			AccessUtil.waitForStill();
			if(listView.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)) {
				LogUtil.writeLog("forward2.....");
				return true;
			}

			LogUtil.writeLog("Scroll End!!!!");
			return false;
		}

		//使用 input 来移位的话，判断是否移动完了
		Rect itemrect1 = new Rect();
		lastItem.refresh();
		lastItem.getBoundsInScreen(itemrect1);
		
		Rect rect = new Rect();
		listView.getBoundsInScreen(rect);

		int X = rect.centerX();
		int toY = rect.top + 20;
		int fromY = rect.bottom - 100;
		LogUtil.writeLog("==>>toY:" + toY + ",fromY:" + fromY);

		GestureUtil.drag(X, fromY, X, toY);
//		AndroidUtil.sleep(600);
		AccessUtil.waitForStill();


		lastItem.refresh();
		Rect itemrect2 = new Rect();
		lastItem.getBoundsInScreen(itemrect2);
		
		if(itemrect2.bottom >= itemrect1.bottom) return false;
		
		return true;
	}

	/**
	 * 扫描一个 item
	 * @param inter
	 */
	public static void startScan(ScanListViewInterface inter) {
		
		AccessibilityNodeInfo listView = getListView(inter);
		if(listView == null) {
			LogUtil.writeLog("listview is null!!!!!!!!!!");
			return;
		}
		if(!listView.isEnabled()) {
			LogUtil.writeLog("listview is disable!!!!!!!!!!");
			return;
		}
		int times = 0;
		while(listView.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)) {
			AndroidUtil.sleep(200);
			times ++;
			if(times > 10) break;
		}
//		AndroidUtil.sleep(500);
		AccessUtil.waitForStill();
		AccessibilityNodeInfo lastItem = null;
		times = 0;
		while(true) {
			lastItem = loopCurrentItems(inter);
			
			if(lastItem == null) break;
			
			if(!scrollForward(inter, lastItem)) break;
//			AndroidUtil.sleep(500);
			AccessUtil.waitForStill();
//			AccessUtil.waitSleepAndStill(100,100);
			times ++;
			LogUtil.writeLog("scroll times=" + times);
			if(times >= 50) break;
		}
	}
	
	public static void startScan(final ScanListViewSimpleInterface inter) {
		startScan(new ScanListViewEasySimpleInterface() {
			
			@Override
			public boolean scanItem(AccessibilityNodeInfo item, int pos, String title, ItemsUtil iu) {
				boolean retval = inter.scanItem(item, pos);
				
				return retval;
			}
		});
	}
	
	public static void startScan(final ScanListViewSimpleInterface2 inter) {
		startScan(new ScanListViewInterface() {
			
			@Override
			public boolean scanItem(AccessibilityNodeInfo item, int pos) {
				boolean retval = inter.scanItem(item, pos);
				
				return retval;
			}
			
			@Override
			public AccessibilityNodeInfo getListView() {
				return null;
			}
			
			@Override
			public AccessibilityNodeInfo getItem(AccessibilityNodeInfo listView, int pos) {
				return inter.getItem(listView, pos);
			}
		});
	}

	public static void startScan(final String matchString, final ScanListViewEasyInterface inter) {
		startScan(matchString,true,inter);
	}

	public static void startScan(final String matchString, final boolean onlyTime,final ScanListViewEasyInterface inter) {
		startScan(new ScanListViewInterface() {

			ItemsUtil iu = new ItemsUtil();

			@Override
			public boolean scanItem(AccessibilityNodeInfo item, int pos) {
				List<String> texts = AccessUtil.getAllText(item, true);
				if(texts == null) {
					LogUtil.writeLog("scan=> null found!");
					ScanListView.itemTimes = 1;
					return true;
				}

//				LogUtil.writeLog("scan=>" + StringUtil.join(",", texts));
//				if(matchString == null) {
//					LogUtil.writeLog("scan=>match is null");
//				} else {
//					LogUtil.writeLog("scan=>match = " + matchString);
//				}
				LogUtil.writeLog("scan=>" + texts + "<");
				String title = iu.getValid(texts, matchString,onlyTime);
				if(title == null) {
//					LogUtil.writeLog("scan=>title null");
					ScanListView.itemTimes = 1;
					return true;
				}
				LogUtil.writeLog("scan=>" + title + " OK!");
				return inter.scanItem(item, pos, title, iu);
			}

			@Override
			public AccessibilityNodeInfo getListView() {
				return inter.getListView();
			}

			@Override
			public AccessibilityNodeInfo getItem(AccessibilityNodeInfo listView, int pos) {
				return inter.getItem(listView, pos);
			}
		});
	}
	public static void startScan(final ScanListViewEasyInterface inter) {
		startScan(null, inter);
	}

	public static void startScan(final String matchString, final ScanListViewEasySimpleInterface inter) {
		startScan(matchString,true,inter);
	}

	public static void startScan(final String matchString,final boolean onlyTime, final ScanListViewEasySimpleInterface inter) {
		startScan(matchString,onlyTime, new ScanListViewEasyInterface() {

			@Override
			public boolean scanItem(AccessibilityNodeInfo item, int pos, String title, ItemsUtil iu) {
				return inter.scanItem(item, pos, title, iu);
			}

			@Override
			public AccessibilityNodeInfo getListView() {
				return null;
			}

			@Override
			public AccessibilityNodeInfo getItem(AccessibilityNodeInfo listView, int pos) {
				return null;
			}
		});
	}

	public static void startScan(final ScanListViewEasySimpleInterface inter) {
		startScan(null, inter);
	}

	public static void startScan(final String matchString, final ScanListViewEasySimpleInterface2 inter) {
		startScan(matchString, new ScanListViewEasyInterface() {
			
			@Override
			public boolean scanItem(AccessibilityNodeInfo item, int pos, String title, ItemsUtil iu) {
				return inter.scanItem(item, pos, title, iu);
			}
			
			@Override
			public AccessibilityNodeInfo getListView() {
				return null;
			}
			
			@Override
			public AccessibilityNodeInfo getItem(AccessibilityNodeInfo listView, int pos) {
				return inter.getItem(listView, pos);
			}
		});
	}
	public static void startScan(final ScanListViewEasySimpleInterface2 inter) {
		startScan(null, inter);
	}

	public static void startSettingScan(final ScanListViewSimpleInterface inter) {
		startScan(new ScanListViewInterface() {
			
			@Override
			public boolean scanItem(AccessibilityNodeInfo item, int pos) {
				boolean retval = inter.scanItem(item, pos);
				
				return retval;
			}
			
			@Override
			public AccessibilityNodeInfo getListView() {
				return null;
			}
			
			@Override
			public AccessibilityNodeInfo getItem(AccessibilityNodeInfo listView, int pos) {
//				LogUtil.writeLog("getitem pos = " + pos);
				AccessSelector selector = new AccessSelector().className("android.widget.FrameLayout");
				
				//步步高在主窗口不移动也可以点击
				if(AndroidUtil.isVIVO()) {
					selector.isVisible(false);
				} else {
					selector.isVisible(true);
				}
				
				List<AccessibilityNodeInfo> items = AccessUtil.getChildren(listView, selector);
				if(items == null) return null;
				if(pos >= items.size()) return null;
				
				return items.get(pos);
			}
		});
	}

	public static boolean gotoSettingItem(final String itemName) {
		
		class _ScanListViewSimpleInterface implements ScanListViewSimpleInterface {
			public boolean isSuccess = false;

			@Override
			public boolean scanItem(AccessibilityNodeInfo item, int pos) {

				List<String> texts = AccessUtil.getAllText(item);
				if(texts == null) return true;
				
				if(itemName != null && !texts.contains(itemName)) return true;

				AccessibilityNodeInfo clickItem = null;
				if(item.isClickable()) {
					clickItem = item;
				} else {
					clickItem = AccessUtil.getChild(item, new AccessSelector().clickable(true));
				}
				if(clickItem == null) {
					if (!ClickUtil.clickNode(item)) return false;
				} else {
					if (!ClickUtil.clickNode(clickItem)) return false;
				}
			
				isSuccess = true;
				
				return false;
			}
		};
		_ScanListViewSimpleInterface inter = new _ScanListViewSimpleInterface();
		startSettingScan(inter);
		return inter.isSuccess;
	}
	
	/**
	 * 连点
	 */
	public static AccessibilityNodeInfo getItem(final String itemName) {
		if(itemName.isEmpty()) return null;
		
		class _ScanListViewSimpleInterface implements ScanListViewSimpleInterface {
			public AccessibilityNodeInfo findItem = null;
			public boolean scanItem(AccessibilityNodeInfo item, int pos) {
				List<String> texts = AccessUtil.getAllText(item);
//				if(texts == null) {
//					LogUtil.writeLog("item is null");
//				} else {
//					LogUtil.writeLog(texts.toString());
//				}
//				LogUtil.writeLog("go===>" + pos + ">>>>>" + itemName);
				if(!AccessUtil.canFindChildByText(item, itemName)) return true;

				findItem = item;
				return false;
			}
		};
		
		_ScanListViewSimpleInterface inter = new _ScanListViewSimpleInterface();

		startScan(inter);
//		AndroidUtil.sleep(500);
		AccessUtil.waitForStill();
		return inter.findItem;
	}

	public static boolean gotoItem(final String itemName) {
		
		class _ScanListViewSimpleInterface implements ScanListViewSimpleInterface {
			public boolean isSuccess = false;
			
			@Override
			public boolean scanItem(AccessibilityNodeInfo item, int pos) {

				List<String> texts = AccessUtil.getAllText(item);
				if(texts == null) return true;
				if(itemName != null && !StringUtil.isListMatch(texts, itemName)) return true;
				AccessibilityNodeInfo clickItem = null;
				if(item.isClickable()) {
					clickItem = item;
				} else {
					clickItem = AccessUtil.getChild(item, new AccessSelector().clickable(true));
				}
				if(clickItem == null) {
					clickItem = item;
				}
				if (!ClickUtil.clickNode(clickItem)) return false;

				AccessUtil.waitForStill();
				isSuccess = true;
				
				return false;
			}
		};
		_ScanListViewSimpleInterface inter = new _ScanListViewSimpleInterface();
		startScan(inter);
		return inter.isSuccess;
	}

	/**
	 * 连点
	 * @param items
	 */
	public static boolean gotoItems(final String ...items) {
		if(items.length == 0) return true;
		
		for(int ii=0;ii<items.length;ii++) {
			if(!gotoItem(items[ii])) return false;
			AccessUtil.waitSleepAndStill();
//			AndroidUtil.sleep(500);
		}
		return true;
	}
	
	public static boolean gotoSettingItems(final String ...items) {
		if(items.length == 0) return true;
		if(!gotoSettingItem(items[0])) return false;
		
		String[] nextItems = new String[items.length - 1];
		for(int ii=0;ii<items.length - 1;ii++) {
			nextItems[ii] = items[ii + 1];
		}
//		AndroidUtil.sleep(500);
		AccessUtil.waitForStill();
		return gotoItems(nextItems);
	}

	public static boolean gotoScrollItem(final AccessSelector selector) {
		class _ScanListViewSimpleInterface implements ScanListViewSimpleInterface {
			public boolean isSuccess = false;

			@Override
			public boolean scanItem(AccessibilityNodeInfo item, int pos) {

				AccessibilityNodeInfo node = AccessUtil.getChild(selector);
				if(node == null) return true;

				if(!ClickUtil.clickButton(node)) {
					return false;
				}

				isSuccess = true;

				return false;
			}
		};
		_ScanListViewSimpleInterface inter = new _ScanListViewSimpleInterface();
		startScan(inter);
		return inter.isSuccess;
	}
}
