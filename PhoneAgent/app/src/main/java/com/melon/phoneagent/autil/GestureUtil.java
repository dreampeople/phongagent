package com.melon.phoneagent.autil;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.util.Size;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.RequiresApi;

import com.melon.util.AndroidUtil;
import com.melon.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.O)
public class GestureUtil {
	public static int LONG_PRESS_DURATION = ViewConfiguration.getLongPressTimeout() + 100;
	public static int PRESS_DURATION = ViewConfiguration.getPressedStateDuration();

	//手势回调
	public static abstract class GestureCallback {
		protected GestureUtil mUtil = null;
		public void setGestureUtil(GestureUtil util) {
			mUtil = util;
		};
		//手势开始
		public boolean onStart() {
			return true;
		}
		//手势执行中
		public boolean onStep(int step) {
			return true;
		}
		//手势成功执行完毕
		public void onOK() {
		}
		//手势被取消
		public void onCancel() {
		}
	};

	static GestureUtil mInstance = null;
	static public void setInstance(GestureUtil instance) {
		mInstance = instance;
	}
	static public GestureUtil newInstance() {
		return new GestureUtil();
	}

	static public boolean isValid() {
		return true;
	}

	static public boolean click(int x,int y, int duration) {
		LogUtil.writeLog("useGestureClick:"+x+","+y);

		if(duration == 0) duration = 50;

		Path path = new Path();
		path.moveTo(x,y);
		GestureDescription gestureDescription = new GestureDescription.Builder()
				.addStroke(new GestureDescription.StrokeDescription(path, 0, duration))
				.build();

		class ClickGestureResCB extends AccessibilityService.GestureResultCallback {
			public int result = -1;
			public ClickGestureResCB() {
				super();
			}
		};

		ClickGestureResCB cb = new ClickGestureResCB() {
			@Override
			public void onCompleted(GestureDescription gestureDescription) {
				super.onCompleted(gestureDescription);
				result = 1;
				LogUtil.writeLog("useGestureClick onCompleted called");
			}

			@Override
			public void onCancelled(GestureDescription gestureDescription) {
				super.onCancelled(gestureDescription);
				result = 0;
				LogUtil.writeLog("useGestureClick onCancelled called");
			}
		};
		((AccessibilityService)AccessServiceUtil.mService).dispatchGesture(gestureDescription, cb,null);

		AndroidUtil.sleep(duration + 100);
		return cb.result == 1;
	}

	static public boolean click(int x, int y) {
		return mInstance.click(x, y, PRESS_DURATION  * 2);
	}
	static public boolean longClick(int x, int y) {
		return mInstance.click(x, y, LONG_PRESS_DURATION * 2);
	}

	static public boolean click(AccessibilityNodeInfo node, int duration) {
		if(node == null) return false;
		Rect rect = new Rect();
		node.getBoundsInScreen(rect);
		return click(rect.centerX(),rect.centerY(), duration);
	}

	static public boolean click(AccessibilityNodeInfo node) {
		if(node == null) {
			LogUtil.writeLog("Gesture click node is null!");
			return false;
		}
		Rect rect = new Rect();
		node.getBoundsInScreen(rect);
		return click(rect.centerX(),rect.centerY());
	}

	static public boolean longClick(AccessibilityNodeInfo node) {
		if(node == null) return false;

		Rect rect = new Rect();
		node.getBoundsInScreen(rect);
		return longClick(rect.centerX(),rect.centerY());
	}

	static public boolean drag(int fromX, int fromY, int toX, int toY, boolean isSwipe) {
		LogUtil.writeLog("from=(" + fromX + "," + fromY + ")");
		LogUtil.writeLog("to=(" + toX + "," + toY + ")");
		if(!isValid()) return false;

		GestureUtil g = GestureUtil.newInstance();
		if(isSwipe) {
			g.addNodeStroke(fromX, fromY, 0, 10);
		} else {
			g.addNodeStroke(fromX, fromY, 0, 500);
		}
		int stepX = Math.abs((toY - fromX) / 5);
		int stepY = Math.abs((toY - fromY) / 5);
		for(int ii=0;;ii++) {
			if(fromX == toX && fromY == toY) break;

			if(fromX < toX) {
				fromX += stepX;
				if(fromX > toX) fromX = toX;
			} else if(fromX > toX) {
				fromX -= stepX;
				if(fromX < toX) fromX = toX;
			}
			if(Math.abs(fromX - toX) < stepX) fromX = toX;

			if(fromY < toY) {
				fromY += stepY;
				if(fromY > toY) fromY = toY;
			} else if(fromY > toY) {
				fromY -= stepY;
				if(fromY < toY) fromY = toY;
			}
			if(Math.abs(fromY - toY) < stepY) fromY = toY;

			g.addNodeStroke(fromX, fromY, 0, 10);
		}
		//如果是滑动，则最后不停顿
		if(!isSwipe) {
			g.addNodeStroke(toX, toY, 0, 200);
		}
		if(g.goAndWait() <= 0) return false;

		return true;
	}

	static public boolean swipe(int fromX, int fromY, int toX, int toY) {
		return drag(fromX, fromY, toX, toY, true);
	}

	static public boolean drag(int fromX, int fromY, int toX, int toY) {
		return drag(fromX, fromY, toX, toY, false);
	}

	static public boolean swipeLeft(int x, int y) {
		return swipe(x, y, 0, y);
	}
	static public boolean dragLeft(int x, int y) {
		return drag(x, y, 0, y);
	}

	static public boolean swipeLeft(AccessibilityNodeInfo node) {
		if(node == null) return false;

		Rect rect = new Rect();
		node.getBoundsInScreen(rect);
		int x = rect.centerX();
		int y = rect.centerY();
		return swipeLeft(x, y);
	}
	static public boolean dragLeft(AccessibilityNodeInfo node) {
		if(node == null) return false;

		Rect rect = new Rect();
		node.getBoundsInScreen(rect);
		int x = rect.centerX();
		int y = rect.centerY();
		return dragLeft(x, y);
	}

	static public boolean swipeRight(int x, int y) {
		int len = 1000;
		Size size = AccessServiceUtil.getRootSize();
		if(size != null) {
			len = size.getWidth() - x;
		}
		return swipe(x, y, x + len, y);
	}
	static public boolean dragRight(int x, int y) {
		int len = 1000;
		Size size = AccessServiceUtil.getRootSize();
		if(size != null) {
			len = size.getWidth() - x;
		}
		return drag(x, y, x + len, y);
	}

	static public boolean swipeRight(AccessibilityNodeInfo node) {
		if(node == null) return false;
		Rect rect = new Rect();
		node.getBoundsInScreen(rect);
		int x = rect.centerX();
		int y = rect.centerY();
		return swipeRight(x, y);
	}
	static public boolean dragRight(AccessibilityNodeInfo node) {
		if(node == null) return false;
		Rect rect = new Rect();
		node.getBoundsInScreen(rect);
		int x = rect.centerX();
		int y = rect.centerY();
		return dragRight(x, y);
	}

	static public boolean swipeUp(int x, int y) {
		return swipe(x, y, x, 0);
	}
	static public boolean dragUp(int x, int y) {
		return drag(x, y, x, 0);
	}

	static public boolean swipeUp(AccessibilityNodeInfo node) {
		if(node == null) return false;
		Rect rect = new Rect();
		node.getBoundsInScreen(rect);
		int x = rect.centerX();
		int y = rect.centerY();
		return swipeUp(x, y);
	}
	static public boolean dragUp(AccessibilityNodeInfo node) {
		if(node == null) return false;
		Rect rect = new Rect();
		node.getBoundsInScreen(rect);
		int x = rect.centerX();
		int y = rect.centerY();
		return dragUp(x, y);
	}

	static public boolean swipeDown(int x, int y) {
		int len = 1000;
		Size size = AccessServiceUtil.getRootSize();
		if(size != null) {
			len = size.getHeight() - y;
		}
		return swipe(x, y, x, y + len);
	}
	static public boolean dragDown(int x, int y) {
		int len = 1000;
		Size size = AccessServiceUtil.getRootSize();
		if(size != null) {
			len = size.getHeight() - y;
		}
		return drag(x, y, x, y + len);
	}

	static public boolean swipeDown(AccessibilityNodeInfo node) {
		if(node == null) return false;
		Rect rect = new Rect();
		node.getBoundsInScreen(rect);
		int x = rect.centerX();
		int y = rect.centerY();
		return swipeDown(x, y);
	}
	static public boolean dragDown(AccessibilityNodeInfo node) {
		if(node == null) return false;
		Rect rect = new Rect();
		node.getBoundsInScreen(rect);
		int x = rect.centerX();
		int y = rect.centerY();
		return dragDown(x, y);
	}

	/**
	 * 合并两个icon为一个文件夹
	 * @param node1
	 * @return
	 */
	static public GestureUtil getContactGesture(AccessibilityNodeInfo node1, int x, int y) {
		return GestureUtil.newInstance()
			.addNodeStroke(node1, 0, LONG_PRESS_DURATION) //长按
			.addNodeStroke(x, y, 0, 100) //拖拽
			.addNodeStroke(x, y, 350, 50) //停一下
			;
	}

	static public GestureUtil getContactGesture(AccessibilityNodeInfo node1, AccessibilityNodeInfo node2) {
		Rect rect = new Rect();
		node2.getBoundsInScreen(rect);
		int x = rect.centerX();
		int y = rect.centerY();

		return getContactGesture(node1, x, y);
	}

	static public GestureUtil getContactGesture11(AccessibilityNodeInfo node1, AccessibilityNodeInfo node2) {
		Rect rect = new Rect();
		node2.getBoundsInScreen(rect);
		int x = rect.centerX();
		int y = rect.centerY();

		node1.getBoundsInScreen(rect);

		int dx = 20, dy = 20;
		if(rect.width() / 6 < 20) dx = rect.width() / 6;
		if(rect.height() / 6 < 20) dy = rect.height() / 6;
		return GestureUtil.newInstance()
				.addNodeStroke(node1, 0, LONG_PRESS_DURATION) //长按
				.addNodeStroke(x + rect.width()/4, y, 0, 100) //拖拽
				.addNodeStroke(x, y, 150, 50) //拖拽
				.addNodeStroke(x + dx, y + dy, 50, 50) //停一下
				;
	}

	protected class Stroke {
		public Path path;
		public int startTime;
		public int duration;

		public int sleepTime;

		public int fromX;
		public int fromY;

		public int targetX;
		public int targetY;

		public GestureDescription.StrokeDescription strokeDescription;

		public Stroke(Path path, int startTime, int duration, int targetX, int targetY) {
			this.path = path;
			this.startTime = startTime;
			this.duration = duration;
			this.targetX = targetX;
			this.targetY = targetY;
		}

		public Stroke(int fromX, int fromY, int startTime, int duration, int targetX, int targetY) {
			this.startTime = startTime;
			this.duration = duration;
			this.fromX = fromX;
			this.fromY = fromY;
			this.targetX = targetX;
			this.targetY = targetY;
		}
	}

	protected List<Stroke> mStrokes = new ArrayList<Stroke>();
	protected GestureUtil() {
	}

	/**
	 * 重置
	 * @return
	 */
	public GestureUtil reset() {
		mStrokes.clear();
		return this;
	}

	public GestureUtil addStroke(Path path, int startTime, int duration, int targetX, int targetY) {
		mStrokes.add(new Stroke(path, startTime, duration, targetX, targetY));

		return this;
	}

	public GestureUtil addNodeStroke(AccessibilityNodeInfo node, int startTime, int duration) {
		Rect rect = new Rect();
		node.getBoundsInScreen(rect);
		int x = rect.centerX();
		int y = rect.centerY();
//		LogUtil.writeLog("addNodeStroke: x=" + x + ",y=" + y);
		addNodeStroke(x, y, startTime, duration);

		return this;
	}

	public void removeNodeStroke(int pos, int len) {
		if(pos >= mStrokes.size()) return;

		for(int ii=0;ii<len;ii++) {
			mStrokes.remove(pos);
			if(pos >= mStrokes.size()) return;
		}
	}

	public void removeNodeStroke(int pos) {
		removeNodeStroke(pos, mStrokes.size() - pos);
	}

	public GestureUtil go() {
		return go(null);
	}

	/**
	 * 等待并执行手势
	 * @param callback
	 * @return
	 */
	public int goAndWait(GestureCallback callback) {

//		int waitDuration = 0;
//		for(GestureUtil.Stroke stroke: mStrokes) {
//			waitDuration += stroke.duration + stroke.startTime;
//		}

		go(callback);
		if(mFinished != 0) {
			//失败了,返回
			return mFinished;
		}

		while(true) {
			AndroidUtil.sleep(100);
			if(mFinished != 0) {
				AndroidUtil.sleep(50);
				break;
			}
		}
//		int times = (waitDuration + 99) / 100;
//		for(int ii=0;ii<times + 10;ii++) {
//			AndroidUtil.sleep(100);
//			if(mFinished != 0) break;
//		}

		return mFinished;
	}

	public int goAndWait() {
		return goAndWait(null);
	}

	protected int mFinished = 0;

	public GestureUtil addNodeStroke(int x, int y, int startTime, int duration) {
		Path path = new Path();

		int sleepTime = 0;
		GestureUtil.Stroke stroke;
		if(mStrokes.size() > 0) {
			GestureUtil.Stroke oldStroke = mStrokes.get(mStrokes.size() - 1);
			path.moveTo(oldStroke.targetX, oldStroke.targetY);
			path.lineTo(x, y);

			sleepTime = oldStroke.startTime + oldStroke.duration;
		} else {
			path.moveTo(x, y);
		}

		stroke = new GestureUtil.Stroke(path, startTime, duration, x, y);
		stroke.sleepTime = sleepTime;
		mStrokes.add(stroke);

		return this;
	}

	private class GECallback extends AccessibilityService.GestureResultCallback {
		private int mContinuePos;   //需要执行的手势位置
		@Override
		public void onCompleted(GestureDescription gestureDescription) {
			if(gestureDescription != null) {
				super.onCompleted(gestureDescription);
			} else {
				mContinuePos = 0;
			}
			LogUtil.writeLog("GestureUtil onCompleted called");

			if(mContinuePos >= mStrokes.size()) {
				Stroke stroke = mStrokes.get(mStrokes.size() - 1);
				if(stroke.startTime + stroke.duration > 0) {
					AndroidUtil.sleep(stroke.startTime + stroke.duration);
				}

				if(mCallback != null) mCallback.onOK();
				mFinished = 1;
				return;
			}

			Stroke stroke = mStrokes.get(mContinuePos);
			if(stroke.sleepTime > 0) {
				AndroidUtil.sleep(stroke.sleepTime);
			}

			if(mCallback != null) {
				//onStep 要求结束
				if(!mCallback.onStep(mContinuePos)) {
					if(mContinuePos == 0) return;

					//结束最后一次
					stroke = mStrokes.get(mContinuePos);
					stroke.strokeDescription = new GestureDescription.StrokeDescription(stroke.path, stroke.startTime, stroke.duration, true);
					GestureDescription gd = new GestureDescription.Builder().addStroke(
							stroke.strokeDescription.continueStroke(stroke.path, 0, 10, false)).build();
					((AccessibilityService)AccessServiceUtil.mService).dispatchGesture(gd, null, null);
//					((AccessibilityService)AccessServiceUtil.mService).dispatchGesture(gd, this, null);
					mFinished = -1;
					return;
				}
			}

			stroke = mStrokes.get(mContinuePos);

			//构建 strokeDescription
			boolean isContinue = mContinuePos < mStrokes.size() - 1;
			if(mContinuePos == 0) {
				stroke.strokeDescription = new GestureDescription.StrokeDescription(stroke.path, stroke.startTime, stroke.duration, isContinue);
			} else {
				Stroke oldStroke = mStrokes.get(mContinuePos - 1);
				stroke.strokeDescription = oldStroke.strokeDescription.continueStroke(stroke.path, stroke.startTime, stroke.duration, isContinue);
			}

			mContinuePos ++;
			GestureDescription gd = new GestureDescription.Builder().addStroke(stroke.strokeDescription).build();
			((AccessibilityService)AccessServiceUtil.mService).dispatchGesture(gd, this,null);
		}

		@Override
		public void onCancelled(GestureDescription gestureDescription) {
			super.onCancelled(gestureDescription);
			if(mCallback != null) mCallback.onCancel();

			LogUtil.writeLog("GestureUtil onCancelled called");
			mFinished = -1;
		}
	};

	private GestureCallback mCallback = null;
	private GECallback geCallback = new GECallback();
	public GestureUtil go(GestureCallback callback) {
		mCallback = callback;
		if(mCallback != null) {
			if(!mCallback.onStart()) {
				LogUtil.writeLog("GestureUtil onStart called failed!");
				//失败则直接返回
				mFinished = -1;
				return this;
			}
		}

		//没有需要运行的动作，则返回
		if(mStrokes.isEmpty()) {
			if(mCallback != null) mCallback.onOK();
			mFinished = 1;
			return this;
		}

		mFinished = 0;
		geCallback.onCompleted(null);

		return this;
	}
}
