package com.edaviessmith.mindcrack.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;

import android.widget.ImageView;

public class ResizableImageView extends ImageView {

    public ResizableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override 
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
    	//Log.e("ResizableImageView","wxh "+widthMeasureSpec+" - "+heightMeasureSpec);
    	//int height = (int) (widthMeasureSpec * 0.5625f);
    	//Log.e("ResizableImageView","height "+heightMeasureSpec+" - "+height);
    	//super.onMeasure(widthMeasureSpec, height);
    	
    	
    	int size = (int) Math.floor(MeasureSpec.getSize(widthMeasureSpec) * 0.5625f) - 5;   
    	
        //int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        // Restrict the aspect ratio to 1:1, fitting within original specified dimensions
        //int chosenDimension = Math.min(widthSize, heightSize);
        //widthMeasureSpec = MeasureSpec.makeMeasureSpec(chosenDimension, MeasureSpec.AT_MOST);
        //heightMeasureSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);

        getLayoutParams().height = size;
        //getLayoutParams().width = chosenDimension;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec); 
    	
    	
        /* Drawable d = getDrawable();

         if(d!=null){
                 // ceil not round - avoid thin vertical gaps along the left/right edges
                 int width = MeasureSpec.getSize(widthMeasureSpec);
                 int height = (int) Math.ceil((float) width * (float) d.getIntrinsicHeight() / (float) d.getIntrinsicWidth());
                 setMeasuredDimension(width, height);
         }else{
                 super.onMeasure(widthMeasureSpec, heightMeasureSpec);
         }*/
    	
    }

}