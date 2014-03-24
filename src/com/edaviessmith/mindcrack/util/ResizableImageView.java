package com.edaviessmith.mindcrack.util;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ResizableImageView extends ImageView {

    public ResizableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override 
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
    	int size = (int) Math.floor(MeasureSpec.getSize(widthMeasureSpec) * 0.5625f) - 5;   
    	getLayoutParams().height = size;
        
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

}