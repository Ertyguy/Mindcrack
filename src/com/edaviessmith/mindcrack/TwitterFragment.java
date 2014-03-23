package com.edaviessmith.mindcrack;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.OAuth2Token;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.edaviessmith.mindcrack.data.Member;
import com.edaviessmith.mindcrack.data.Tweet;
import com.edaviessmith.mindcrack.util.ResizableImageView;


public class TwitterFragment extends SherlockFragment {
	
	private static String TAG = "TwitterFragment";
	
	private Context context;
	private View view;
	private ListView listView;
	private TwitterAdapter adapter;
	
	private RelativeLayout loadingLayout;
	private TextView loadingTextView;
	private ProgressBar loadingProgressBar;
	private ImageView loadingImage;
	
	private static List<Tweet> tweetList;
	//private ImageLoader imageLoader;

	private static boolean beginningOfList;
	private static boolean endOfList;
	private static boolean searchBusy;
	private static boolean waitingToSearch;
	private static Tweet loading;
    
	private TwitterFactory twitterFactory = null;
    private Twitter twitter;
    private ConfigurationBuilder configurationBuilder;
	    
    
    
	public static TwitterFragment newInstance(Member member) {
		TwitterFragment fragment = new TwitterFragment();
        return fragment;
    }

		
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	view = inflater.inflate(R.layout.twitter_fragment, null);
        context = view.getContext();
       
        //imageLoader = new ImageLoader(context);
        
        loading = new Tweet();
        
        if(!Util.isNetworkAvailable()) {
        	//Toast.makeText(context, "Cannot connect to the internet", Toast.LENGTH_LONG).show();
        }
            	
        configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setOAuthConsumerKey("dTvI2BDGYncp0H1ypSYygg")
							.setOAuthConsumerSecret("84iO1oRvzFlMBJIRZm47pMV1FCWdrnFW7koDv0dyY8")
					        .setUseSSL(true)
					        .setApplicationOnlyAuthEnabled(true).setHttpConnectionTimeout(100000);
					         
        twitterFactory = new TwitterFactory(configurationBuilder.build());
        twitter = twitterFactory.getInstance();
         
         
        
        
    	 
    	return view;
     }
	
    public void onActivityCreated (Bundle savedInstanceState){
    	super.onActivityCreated(savedInstanceState);
    	Log.d(TAG, "fragment onActivityCreated");
    	
    	boolean changedMember = !AppInstance.checkSetTwitterMemberIdIsCurrent();
    	
		if(changedMember) {
			Log.e(TAG, "member is changed");
			AppInstance.twitterPageToken = 1;
    		beginningOfList = true;
			

			tweetList = new ArrayList<Tweet>(AppInstance.getTwitterFeed());
	    	// Add loading Tweet
	    	tweetList.add(tweetList.size(), loading);
	    		    	
	    	if(adapter != null) {
	    		adapter.clearItems();
			 	for(Tweet item : tweetList) {
			 		adapter.add(item);
			 	}
			}
	    	
	    	AppInstance.isTwitterFeedUpToDate = false;
	    	
	    	if (isNeededTwitterAuth()) {
	             new TwitterAuthenticateTask(this).execute();
	        }
	        else
	        {
	        	new TwitterFeed(TwitterFragment.this, AppInstance.getCurrentTwitterMemberId()).execute(AppInstance.getMember().getTwitterId());
	        }
	    	
	        
		} else {
			if(loadingLayout != null) {
				loadingLayout.setVisibility(View.GONE);
			}
		}
		
		
		if(adapter == null || listView == null) {
			initView();
		}
		
    	
		if(tweetList == null) {
			tweetList = new ArrayList<Tweet>(AppInstance.getTwitterFeed());
	    	// Add loading Tweet
	    	tweetList.add(tweetList.size(), loading);
	    	
	    	if(adapter != null) {
	    		adapter.clearItems();
			 	for(Tweet item : tweetList) {
			 		adapter.add(item);
			 	}
			}
	    	
	    	AppInstance.isTwitterFeedUpToDate = false;
	    	if (isNeededTwitterAuth()) {
	             new TwitterAuthenticateTask(this).execute();
	        }
	        else
	        {
	        	new TwitterFeed(TwitterFragment.this, AppInstance.getCurrentTwitterMemberId()).execute(AppInstance.getMember().getTwitterId());
	        }	    		   
		}
    	

        if(changedMember) {
        	Log.e(TAG, "MEMBER HAS CHANGED SCROLL NOW");
        	listView.smoothScrollToPosition(0);
	    	if(loadingLayout != null) {
	    		loadingLayout.setVisibility(View.VISIBLE);
			}
        } 
    	    
    }
    
    private void initView() {
    	Log.i(TAG, "initView");
    	if(isAdded()) {
	        listView = (ListView) getActivity().findViewById(R.id.twitter_fragment_list);
	        
	        adapter = new TwitterAdapter(getActivity(), R.layout.tweet, tweetList);
	        //adapter.clearItems();
	        listView.setAdapter(adapter);
	        listView.setOnScrollListener(new OnScrollListener() {      
	            @Override
	            public void onScrollStateChanged(AbsListView view, int scrollState) { }
	
	            @Override
	            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
	
	                if(view.getLastVisiblePosition() >= totalItemCount - 5){
	                	
	                	if(!searchBusy && !waitingToSearch  && AppInstance.twitterPageToken != 0) {
	            			
	            			if(beginningOfList && AppInstance.twitterPageToken == 1) {
	            	    		listView.setSelection(0);
	            	    		Log.d(TAG,"Scroll to the top of the list");
	            	    	}
	            			Log.d(TAG,"Trigger to search for new items");
	            			new TwitterFeed(TwitterFragment.this, AppInstance.getCurrentTwitterMemberId()).execute(AppInstance.getMember().getTwitterId());
	            			endOfList = false;
	            			
	            		} else if(searchBusy && AppInstance.twitterPageToken == 1) {
	            			waitingToSearch = true;
	            			
	            		} else if (AppInstance.twitterPageToken == 0 && !endOfList){
	            			Toast.makeText(context, "No more uploads found for "+AppInstance.getMember().getName(), Toast.LENGTH_SHORT).show();
	            			adapter.removeProgressBar();
	            			endOfList = true;		
	            		}
	                }
	            }
	        });  
	        
	        loadingLayout = (RelativeLayout) getActivity().findViewById(R.id.loading_layout);
	        loadingTextView = (TextView) getActivity().findViewById(R.id.loading_text);
        	loadingProgressBar = (ProgressBar) getActivity().findViewById(R.id.loading_progress_bar);
        	loadingImage = (ImageView) getActivity().findViewById(R.id.loading_image);
        	
	        if(!Util.isNetworkAvailable()) {
	        	loadingTextView.setText(getResources().getString(R.string.loading_twitter_failed));
	        	loadingProgressBar.setVisibility(View.GONE);
	        	loadingImage.setVisibility(View.VISIBLE);
	        } else {
	        	loadingTextView.setText(getResources().getString(R.string.loading_twitter));
	        	loadingProgressBar.setVisibility(View.VISIBLE);
	        	loadingImage.setVisibility(View.GONE);
	        }
	        
    	}
    }
    
    
    
    protected class TwitterAdapter extends ArrayAdapter<Tweet> {

	    Context context; 
	    int layoutResourceId;    
	    List<Tweet> data = new ArrayList<Tweet>();
	    
	    public TwitterAdapter(Context context, int layoutResourceId, List<Tweet> data) {
	        super(context, layoutResourceId, tweetList);
	        this.layoutResourceId = layoutResourceId;
	        this.context = context;
	        this.data = data;	       
	    }
	    
	    @Override
	    public int getCount() {	    	
	    	return data.size();
	    }
	    
	    public void add(Tweet item)
	    {
	    	if(getIndex() < data.size()) {
	    		data.add(getIndex(), item);
	    	} else {
	    		Log.e(TAG, "index bigger then data");
	    		clearItems();	    		
	    		data.add(0, item);	    		
	    	}
	    	
	        notifyDataSetChanged();	        
	    }
	    
	    private int getIndex() {
	    	return getCount() > 1? getCount() - 1 : (getCount() > 0? 0: 0);
	    }
	    
	    public void removeProgressBar() {
	    	data.remove(getCount() - 1);

	        notifyDataSetChanged();
	    }
	    
	    public void clearItems() {
	    	data.clear();
	    	data.add(loading);
	    	
	        notifyDataSetChanged();
	    }
	    
	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {	    	
	        View row = convertView;
	        TweetHolder holder = null;
	        
	        if(row == null)
	        {
	            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
	            row = inflater.inflate(layoutResourceId, parent, false);
	            
	            holder = new TweetHolder();
	            holder.itemView = (LinearLayout) row.findViewById(R.id.item_view);
	            holder.tweetImage = (ResizableImageView) row.findViewById(R.id.tweet_image);
	            holder.progressBar = (ProgressBar) row.findViewById(R.id.progress_bar);
	            holder.text = (TextView)row.findViewById(R.id.text);
	            holder.date = (TextView)row.findViewById(R.id.date);
	            
	            row.setTag(holder);
	        }
	        else
	        {
	            holder = (TweetHolder)row.getTag();
	        }
	        
	        
	        final Tweet item = data.get(position);
	        	        
	        //Regex and highlight @			
			if(!TextUtils.isEmpty(item.getText())) {
				
				String text = item.getText();
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
				holder.text.setText(wordtoSpan);
			} else {
				holder.text.setText(R.string.loading_text);
			}
		    
		    
            holder.date.setText(Util.getTimeSince(item.getDate()));
            
	        return row;
	    }
	    
	    class TweetHolder
	    {
	    	LinearLayout itemView;
	        ResizableImageView tweetImage;
	        ProgressBar progressBar;
	        TextView text;
	        TextView date;
	    }
	}
    
    

	public void onTaskCompleted(ResponseList<twitter4j.Status> twitterFeed) {
		
		if(loadingLayout.getVisibility() == View.VISIBLE) {
			loadingLayout.setVisibility(View.GONE);
		}
		
		if(twitterFeed != null) {
			List<Tweet> tweets = new ArrayList<Tweet>();
			int index = 0;
			int memberId = AppInstance.getMember().getId();
			for(twitter4j.Status tweet : twitterFeed) {
				if(tweet != null) {
					try {
						Tweet item = new Tweet();   
						item.setId(index++);
						item.setMemberId(memberId);
					    item.setTweetId(tweet.getId());
					    item.setText(tweet.getText());
					    	    
						item.setDate(tweet.getCreatedAt().getTime()/1000);	
						
						
					    tweets.add(item);
					    
				    } catch (Exception e) {
						//TODO add logging to excpetion (unsure occurance)
						e.printStackTrace();
					}
				}
			}
			
			//Only save the first page of YoutubeItems
	        if(beginningOfList) {
	        	AppInstance.updateTwitterFeed(tweets);
	        	beginningOfList = false;
	        	Log.d(TAG, "adding items to updateYoutubeItems");
	        } else {
	        	for(Tweet item : tweets) {
	        		adapter.add(item);
				}
	        }
	        	
	        	
	        // If the records in the database are not up to date
	        if(!AppInstance.isTwitterFeedUpToDate) {
	        	adapter.clearItems(); //Clear all items currently in the adapter
	        	
	        	for(Tweet item : tweets) {
	        		adapter.add(item);
				}
	        	AppInstance.isTwitterFeedUpToDate = true;
	        }
	        
	        
			searchBusy = false;
			
			//Used if you scroll to the end of the list before the first result is returned
			if(waitingToSearch) {
				
				if(AppInstance.twitterPageToken != 0) {
        			new TwitterFeed(TwitterFragment.this, AppInstance.getCurrentTwitterMemberId()).execute(AppInstance.getMember().getTwitterId());
        			endOfList = false;
        		} 
				waitingToSearch = false;
			}
		}
			
	}
	
	
    
	class TwitterFeed extends AsyncTask<String, String, Void> {

	    ResponseList<twitter4j.Status> twitterFeed;
	    private TwitterFragment twitterFragment;
	    private int currentMemberId;
	    
	    
	    public TwitterFeed(TwitterFragment twitterFragment, int currentMemberId) {
	    	this.twitterFragment = twitterFragment;
	    	this.currentMemberId = currentMemberId;
	    	waitingToSearch = false;
	    }
	    
	    protected void onPreExecute() {
	    	searchBusy = true;
	    }

	    @Override
	    protected Void doInBackground(String... params) {

	    	twitterFeed = getRecentTwitterFeed(params[0], AppInstance.twitterPageToken);
	    	
			return null;
	    }  


	    protected void onPostExecute(Void v) {
	    	//Still the current member
	    	if(twitterFeed != null) {
	    		twitterFragment.onTaskCompleted(twitterFeed);
	    	}
	    } 
 
	    
	    public ResponseList<twitter4j.Status> getRecentTwitterFeed(String playlistId, int pageToken) {
	    	try {
		        
	    		SharedPreferences settings = twitterFragment.context.getSharedPreferences(Constants.PREFS, 0);
		        String twitterAccesToken = settings.getString(Constants.PREF_TW_ACCESS_TOKEN, "");
		        String twitterTokenType = settings.getString(Constants.PREF_TW_TOKEN_TYPE, "");

		    	try {
		    		twitter.setOAuth2Token(new OAuth2Token(twitterTokenType, twitterAccesToken));
		    		
		    		if(AppInstance.getMember().getId() == this.currentMemberId) {
		    			
		    			ResponseList<twitter4j.Status> response = twitter.getUserTimeline(playlistId, new Paging(pageToken, 20));
		    			if(response == null || response.size() == 0) 
		    				AppInstance.twitterPageToken = 0;
		    			else
		    				AppInstance.twitterPageToken ++;
		    			Log.e(TAG, "token: "+AppInstance.twitterPageToken);
		    			return response;
		    		}
		        } catch (Exception e) {
		            e.printStackTrace();
		            //TODO://
		            //if API is not available this doesn't tell the user
		        }
	        } catch (Throwable t) {
	            t.printStackTrace();
	        }
	    return null;
	    }
	    
	}

	

     
     @Override
     public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);  

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
		    	
		    	new TwitterFeed(TwitterFragment.this, AppInstance.getCurrentTwitterMemberId()).execute(AppInstance.getMember().getTwitterId());
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
 
}
