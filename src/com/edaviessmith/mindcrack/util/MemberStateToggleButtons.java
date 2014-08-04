package com.edaviessmith.mindcrack.util;


import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.edaviessmith.mindcrack.R;


public class MemberStateToggleButtons extends ToggleButton {

		
	private static final String TAG = "ThreeStateToggleButton";
	Button leftButton;
	Button middleButton;
	Button rightButton;

	public MemberStateToggleButtons(Context context) {
		super(context, null);
	}

	public MemberStateToggleButtons(Context context, AttributeSet attrs) {
		super(context, attrs);
    
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ThreeStateToggleButton, 0, 0);
        String leftText = a.getString(R.styleable.ThreeStateToggleButton_leftText);
        String middleText = a.getString(R.styleable.ThreeStateToggleButton_middleText);
        String rightText = a.getString(R.styleable.ThreeStateToggleButton_rightText);
        a.recycle();

        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.member_state_buttons, this, true);

        this.leftButton = (Button) v.findViewById(R.id.button_yes);
        this.middleButton = (Button) v.findViewById(R.id.button_no);
        this.rightButton = (Button) v.findViewById(R.id.button_na);
        
        leftButton.setText(leftText);
        middleButton.setText(middleText);
        rightButton.setText(rightText);
        
		leftButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				setValue(-1);
			}

		});
		middleButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				setValue(0);
			}

		});	
		rightButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				setValue(1);
			}

		});	
    }
    
	public void setButtonState(Button button, boolean selected) {
		if(button == null) {
			return;
		}
		button.setSelected(selected);
		if(selected) {
			//button.setBackgroundResource(R.drawable.button_pressed);
			
			if(button.equals(leftButton)) {
				button.setBackgroundResource(R.drawable.remove);
			}
			if(button.equals(middleButton)) {
				button.setBackgroundResource(R.drawable.show);
			}
			if(button.equals(rightButton)) {
				button.setBackgroundResource(R.drawable.star);
			}
		} else {
			//button.setBackgroundResource(R.drawable.button_not_pressed);
			
			if(button.equals(leftButton)) {
				button.setBackgroundResource(R.drawable.remove_grey);
			}
			if(button.equals(middleButton)) {
				button.setBackgroundResource(R.drawable.show_grey);
			}
			if(button.equals(rightButton)) {
				button.setBackgroundResource(R.drawable.star_grey);
			}
		}
	}

	public int getValue() {
		boolean state_a = leftButton.isSelected();
		boolean state_b = middleButton.isSelected();
		boolean state_c = rightButton.isSelected();
		int sum = (state_a?1:0)+(state_b?1:0)+(state_c?1:0);
		if(sum > 1) {
			Log.d(TAG, "Bad state");
			return -2;
		}
		if(sum == 0) {
			Log.d(TAG, "Nothing selected");
			return -2;
		}
		int state = state_a ? -1 : (state_b ? 0 : 1);
		Log.d(TAG, "State: " + state);
		return state;
	}

	public void setValue(int state) {
		switch(state) {
		case -1: 
			setButtonState(leftButton, true);
			setButtonState(middleButton, false);
			setButtonState(rightButton, false);
			break;
		case 0: 
			setButtonState(leftButton, false);
			setButtonState(middleButton, true);
			setButtonState(rightButton, false);
			break;
		case 1: 
			setButtonState(leftButton, false);
			setButtonState(middleButton, false);
			setButtonState(rightButton, true);
			break;
		default:
			setButtonState(leftButton, false);
			setButtonState(middleButton, false);
			setButtonState(rightButton, false);
			break;
		}
		super.setValue(state);
	}
}