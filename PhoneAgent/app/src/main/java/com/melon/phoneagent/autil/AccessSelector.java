package com.melon.phoneagent.autil;

public class AccessSelector {
	public int index = -1;
	public String text = null;
	public String className = null;
	public String resourceId = null;
	public String packageName = null;
	public String contentDesc = null;
	public int isClickable = -1;
	public int isCheckable = -1;
	public int isChecked = -1;
	public int isEnabled = -1;
	public int isFocusable = -1;
	public int isFocused = -1;
	public int isScrollable = -1;
	public int isLongClickable = -1;
	public int isPassword = -1;
	public int isSelected = -1;
	public int inListView = -1;
	public int isVisible = -1;
	
	public AccessSelector() {}
	
	public AccessSelector index(int indexNum) { index = indexNum; return this; } 
	public AccessSelector text(String text_) { text = text_; return this; } 
	public AccessSelector className(String className_) { className = className_; return this; } 
	public AccessSelector resourceId(String resourceId_) { resourceId = resourceId_; return this; } 
	public AccessSelector packageName(String packageName_) { packageName = packageName_; return this; }
	public AccessSelector contentDesc(String contentDesc_) { contentDesc = contentDesc_; return this; } 
	public AccessSelector clickable(boolean isClickable_) { isClickable = isClickable_?1:0; return this; } 
	public AccessSelector checkable(boolean isCheckable_) { isCheckable = isCheckable_?1:0; return this; } 
	public AccessSelector checked(boolean isChecked_) { isChecked = isChecked_?1:0; return this; } 
	public AccessSelector enable(boolean isEnabled_) { isEnabled = isEnabled_?1:0; return this; }
	public AccessSelector focusable(boolean isFocusable_) { isFocusable = isFocusable_?1:0; return this; }
	public AccessSelector focused(boolean isFocused_) { isFocused = isFocused_?1:0; return this; }
	public AccessSelector scrollable(boolean isScrollable_) { isScrollable = isScrollable_?1:0; return this; }
	public AccessSelector longClickable(boolean isLongClickable_) { isLongClickable = isLongClickable_?1:0; return this; }
	public AccessSelector password(boolean isPassword_) { isPassword = isPassword_?1:0; return this; }
	public AccessSelector selected(boolean isSelected_) { isSelected = isSelected_?1:0; return this; }
	public AccessSelector inListView(boolean inListView_) { inListView = inListView_?1:0; return this; }
	public AccessSelector isVisible(boolean isVisible_) { isVisible = isVisible_?1:0; return this; }
}
