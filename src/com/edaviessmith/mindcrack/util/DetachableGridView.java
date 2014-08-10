package com.edaviessmith.mindcrack.util;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.widget.GridView;

public class DetachableGridView extends GridView {

	public DetachableGridView(Context context) {
		super(context);
	}

	 public DetachableGridView(Context context, AttributeSet attrs) { 
         super(context, attrs); 
     } 

     public DetachableGridView(Context context, AttributeSet attrs, int defStyle) { 
         super(context, attrs, defStyle); 

     } 

     
	@Override
	protected void onDetachedFromWindow() {
		try {
			super.onDetachedFromWindow();
		} catch (IllegalArgumentException iae) {
			iae.printStackTrace();
			// Workaround for
			// http://code.google.com/p/android/issues/detail?id=22751
		}
	}

}
