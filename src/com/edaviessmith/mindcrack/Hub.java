package com.edaviessmith.mindcrack;

import java.util.List;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.edaviessmith.mindcrack.data.Member;
import com.edaviessmith.mindcrack.db.DatabaseHelper;
import com.edaviessmith.mindcrack.db.MemberORM;
import com.edaviessmith.mindcrack.R;


public class Hub extends ActionBarActivity{
	
	public static String TAG = "Hub";
	
	int cols = 6;
	LinearLayout memberLayout;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.hub);
        
        memberLayout = (LinearLayout) findViewById(R.id.member_layout);
        
        List<Member> mindcrackers = MemberORM.getMembers(this);
        int padding = Util.getPixels(TypedValue.COMPLEX_UNIT_DIP, 10);
        int width = Util.getPhoneWidth() - (padding * cols * 2);
        int iconSize = width / cols;
        
        
        LayoutParams LLParams = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        LayoutParams IVParams = new LayoutParams(iconSize, iconSize);
        IVParams.setMargins(padding, padding, padding, padding);
        
        
        for(int r = 0; r * cols < mindcrackers.size(); r++) {
        	LinearLayout rowLayout = new LinearLayout(this);
        	rowLayout.setLayoutParams(LLParams);
        	rowLayout.setOrientation(LinearLayout.HORIZONTAL);

        	for(int i = 0; i < cols && ((r*cols + i) < mindcrackers.size()); i++) {
        		Member member = mindcrackers.get(((r * cols) + i));
        		Log.d(TAG, "member: "+i+" - "+((r * cols) + i));
        		
        		ImageView memberImage = new ImageView(this);
        		memberImage.setLayoutParams(IVParams);
            	memberImage.setImageResource(member.getImage());
            	memberImage.setContentDescription(member.name);
            	
            	rowLayout.addView(memberImage);

        	}
            
        	memberLayout.addView(rowLayout);

        }
        
	}
	
	
	
}
