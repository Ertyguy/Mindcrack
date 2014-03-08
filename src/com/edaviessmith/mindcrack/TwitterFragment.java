package com.edaviessmith.mindcrack;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.OAuth2Token;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.actionbarsherlock.app.SherlockFragment;
import com.edaviessmith.mindcrack.data.Member;



public class TwitterFragment extends SherlockFragment {
	
	private Context context;
	private View view;
	private Member member;
    private RequestToken requestToken = null;
    private TwitterFactory twitterFactory = null;
    private Twitter twitter;
    private ConfigurationBuilder configurationBuilder;
    
	public static TwitterFragment newInstance(Member member) {
		 TwitterFragment fragment = new TwitterFragment();
		 fragment.member = member;

        return fragment;
    }

		
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	view = inflater.inflate(R.layout.twitter_fragment, null);
    	context = view.getContext();

    	
        configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setOAuthConsumerKey("dTvI2BDGYncp0H1ypSYygg")
							.setOAuthConsumerSecret("84iO1oRvzFlMBJIRZm47pMV1FCWdrnFW7koDv0dyY8")
					        .setUseSSL(true)
					        .setApplicationOnlyAuthEnabled(true).setHttpConnectionTimeout(100000);
					         
        twitterFactory = new TwitterFactory(configurationBuilder.build());
        twitter = twitterFactory.getInstance();
         
         
        if(Util.isNetworkAvailable()) {
	     	if (isNeededTwitterAuth()) {
	             new TwitterAuthenticateTask(this).execute();
	        }
	        else
	        {
	        	 Log.i("Twitter","Already authenticated");
	        	 OnTwitterAuthenticated();
	        }
        }
    	 
    	return view;
     }
	

     public void OnTwitterAuthenticated() {
         if(member == null) {
 			SharedPreferences settings = context.getSharedPreferences(Constants.PREFS, 0);
 		    int memberId = settings.getInt(Constants.PREF_MEMBER, 0);
 		    member = AppInstance.getMindcrackers().get(memberId);
         }
         
    	 new TwitterFeedTask(this).execute(member.getTwitterId());
     }
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);  

     }
     
     
     public void OnTwitterFeed (ResponseList<twitter4j.Status> responseList) {
    	 
    	 if(responseList == null){ 
    		 Toast.makeText(context, "Unable to read response from Twitter", Toast.LENGTH_SHORT).show();
    		 return;
    	 }
    	 
    	for(twitter4j.Status status : responseList) {
    		//Log.i("Tweet",status.getText());
    		
    		ViewGroup parent = (ViewGroup) view.findViewById(R.id.twitter_fragment_scroll);    			
			View itemView = LayoutInflater.from(context).inflate(R.layout.twitter_item, parent, false);

			
			String text = status.getText();
			
			ArrayList<Integer> tags = new ArrayList<Integer>();
			char tag = '@';
			for (int index = text.indexOf(tag); index >= 0; index = text.indexOf(tag, index + 1))
			{
			    tags.add(index);
			}
			
			Spannable wordtoSpan = new SpannableString(text);  
			for(int tagIndex : tags) {
				//int tagEnd = text.indexOf(" ", tagIndex);
				
				Pattern p = Pattern.compile("@[a-zA-Z0-9]+");
				Matcher m = p.matcher(text.substring(tagIndex));
				if (m.find()) {					
					int tagEnd = m.end();	
					if(tagEnd > 0) {
						wordtoSpan.setSpan(new ForegroundColorSpan(R.color.twitter_blue), tagIndex, tagIndex+tagEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					}
				}
				
				
			}
			
		    TextView title = (TextView)  itemView.findViewById(R.id.text);
		    title.setText(wordtoSpan);
		   	 
		    
			TextView dateText = (TextView)  itemView.findViewById(R.id.date);
			dateText.setText(Util.getTimeSince(status.getCreatedAt().getTime()));
								
		    parent.addView(itemView);
    	 }

     }
     
     
     private boolean isNeededTwitterAuth() {
         SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
         String twitterAccesToken = settings.getString(Constants.PREF_TW_ACCESS_TOKEN, "");
         String twitterTokenType = settings.getString(Constants.PREF_TW_TOKEN_TYPE, "");
         return ((twitterAccesToken.length() == 0) && (twitterTokenType.length() == 0));
     }
     
     
 	class TwitterAuthenticateTask extends AsyncTask<Void, Void, OAuth2Token> {
		 
 		TwitterFragment twitterFragment;
 		
 		public TwitterAuthenticateTask(TwitterFragment twitterFragment) {
 			this.twitterFragment = twitterFragment;
		}
 		
	    @Override
	    protected void onPostExecute(OAuth2Token bearerToken) {
	    	
	    	if(bearerToken != null) {

		    	SharedPreferences.Editor settings = context.getSharedPreferences(Constants.PREFS, 0).edit();
		    	settings.putString(Constants.PREF_TW_ACCESS_TOKEN, bearerToken.getAccessToken());
		    	settings.putString(Constants.PREF_TW_TOKEN_TYPE, bearerToken.getTokenType());
		    	settings.commit();
		    	
		    	twitterFragment.OnTwitterAuthenticated();
	    	}
	    }
	 
	    @Override
	    protected OAuth2Token doInBackground(Void... params) {
	        OAuth2Token bearerToken = null;

	        try {
	            bearerToken = twitter.getOAuth2Token();
	            Log.i("Twitter","Authenticated");
	        } catch (Exception e) {
	            e.printStackTrace();
	            Log.e("Twitter","Unable to Authenticate Twitter");
	        }
	        return bearerToken;
	    }
	}
 	
 	class TwitterFeedTask extends AsyncTask<String, Void, ResponseList<twitter4j.Status>> {
		 
 		TwitterFragment twitterFragment;
 		
 		ResponseList<twitter4j.Status> response;
 		
 		public TwitterFeedTask(TwitterFragment twitterFragment) {
 			this.twitterFragment = twitterFragment;
		}
 		
		@Override
		protected ResponseList<twitter4j.Status> doInBackground(String... params) {
			
			SharedPreferences settings = context.getSharedPreferences(Constants.PREFS, 0);
	        String twitterAccesToken = settings.getString(Constants.PREF_TW_ACCESS_TOKEN, "");
	        String twitterTokenType = settings.getString(Constants.PREF_TW_TOKEN_TYPE, "");

	    	try {
	    		twitter.setOAuth2Token(new OAuth2Token(twitterTokenType, twitterAccesToken));
	    		Log.i("Twitter","Requesting user("+params[0]+") timeline");
	    		return twitter.getUserTimeline(params[0]);
	        } catch (Exception e) {
	            e.printStackTrace();
	            //TODO://
	            //if API is not available this doesn't tell the user
	        }
	        return null;
		}
		
		protected void onPostExecute(ResponseList<twitter4j.Status> response) {
			twitterFragment.OnTwitterFeed(response);
		}
	}
 	
}
