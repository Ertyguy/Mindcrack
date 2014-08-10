package com.edaviessmith.mindcrack;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.edaviessmith.mindcrack.data.Member;
import com.edaviessmith.mindcrack.data.Reddit;
import com.edaviessmith.mindcrack.db.MemberORM;
import com.edaviessmith.mindcrack.db.RedditORM;


public class Hub extends ActionBarActivity{
	
	public static String TAG = "Hub";
	
	int cols = 6;
	LinearLayout memberLayout, redditLayout;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.hub);
        
        memberLayout = (LinearLayout) findViewById(R.id.member_layout);
        redditLayout = (LinearLayout) findViewById(R.id.reddit_layout);
        
        List<Member> mindcrackers = MemberORM.getMembers(this);
        
        int padding = Util.getPixels(TypedValue.COMPLEX_UNIT_DIP, 10);
        int width = Util.getPhoneWidth() - (padding * cols * 2);
        int iconSize = width / cols;
        
        
        LayoutParams LLParams = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        LayoutParams IVParams = new LayoutParams(iconSize, iconSize);
        IVParams.setMargins(padding, padding, padding, padding);
        LayoutParams LLRParams = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        LLRParams.bottomMargin = Util.getPixels(TypedValue.COMPLEX_UNIT_DIP, 2);
        
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
        
        List<Reddit> reddits = RedditORM.getReddits(this);
        
        for(int i = 0; i < reddits.size(); i++) {
        	final Reddit r = reddits.get(i);
        	
        	TextView redditText = new TextView(this);
        	redditText.setLayoutParams(LLRParams);
        	redditText.setTextSize(20);
        	redditText.setTextColor(getResources().getColor(R.color.white));
        	redditText.setCompoundDrawablesWithIntrinsicBounds(r.getImage(), 0, 0, 0);
        	redditText.setText(r.getTitle());
        	redditText.setPadding(padding, padding, padding, padding);
        	redditText.setBackgroundColor(getResources().getColor(R.color.transparent_dark_grey));
        	
        	redditText.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					Intent i = new Intent(Hub.this, Reddits.class);
					i.putExtra("reddit_id", r.getId());
					startActivity(i);
				}
			});
        	
        	redditLayout.addView(redditText);

        }
        
	}
	
	
	
}
