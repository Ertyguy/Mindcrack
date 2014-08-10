package com.edaviessmith.mindcrack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
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

import com.edaviessmith.mindcrack.data.RedditPost;
import com.edaviessmith.mindcrack.R;
import com.edaviessmith.mindcrack.util.ImageLoader;
import com.edaviessmith.mindcrack.util.RemoteData;
import com.edaviessmith.mindcrack.util.ResizableImageView;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;


public class RedditPostFragment extends Fragment {
	
	private static String TAG = "RedditPostFragment";
	
	private Context context;
	private View view;
	private ListView listView;
	private RedditAdapter adapter;
	
	private RelativeLayout loadingLayout;
	private TextView loadingTextView;
	private ProgressBar loadingProgressBar;
	private ImageView loadingImage;
	
	private static List<RedditPost> tweetList;
	private ImageLoader imageLoader;

	private static boolean beginningOfList;
	private static boolean endOfList;
	private static boolean searchBusy;
	private static boolean waitingToSearch;
	private static RedditPost loading;

	private static boolean isDeviceTablet;
	    
    
    
	public static RedditPostFragment newInstance() {
		RedditPostFragment fragment = new RedditPostFragment();
        return fragment;
    }

		
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	view = inflater.inflate(R.layout.reddit_fragment, null);
        context = view.getContext();
        imageLoader = new ImageLoader(context);
        
        loading = new RedditPost();
        
        if(!Util.isNetworkAvailable()) {
        	//Toast.makeText(context, "Cannot connect to the internet", Toast.LENGTH_LONG).show();
        }       
        
        AppInstance.redditPageToken = "";
    	 
    	return view;
     }
	
    public void onActivityCreated (Bundle savedInstanceState){
    	super.onActivityCreated(savedInstanceState);
    	Log.d(TAG, "fragment onActivityCreated");
    	
		if(AppInstance.redditPost.equals("")) {
			Log.e(TAG, "member is changed");
			//AppInstance.redditPageToken = "";
    		beginningOfList = true;
			

			tweetList = new ArrayList<RedditPost>(AppInstance.getRedditFeed());
	    	// Add loading Post
	    	tweetList.add(tweetList.size(), loading);
	    		    	
	    	if(adapter != null) {
	    		adapter.clearItems();
			 	for(RedditPost item : tweetList) {
			 		adapter.add(item);
			 	}
			}
	    	
	    	AppInstance.isRedditFeedUpToDate = false;
	    	
	    	new RedditFeed(RedditPostFragment.this).execute();
	        
	    	
	        
		} else {
			if(loadingLayout != null) {
				loadingLayout.setVisibility(View.GONE);
			}
		}
		
		
		if(adapter == null || listView == null) {
			initView();
		}
		
    	
		if(tweetList == null) {
			tweetList = new ArrayList<RedditPost>(AppInstance.getRedditFeed());
	    	// Add loading Post
	    	tweetList.add(tweetList.size(), loading);
	    	
	    	if(adapter != null) {
	    		adapter.clearItems();
			 	for(RedditPost item : tweetList) {
			 		adapter.add(item);
			 	}
			}
	    	
	    	AppInstance.isRedditFeedUpToDate = false;
	    	
	    	new RedditFeed(RedditPostFragment.this).execute();
	            		   
		}
    	

        if(true) {
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
	        listView = (ListView) getActivity().findViewById(R.id.reddit_fragment_list);
	        
	        adapter = new RedditAdapter(getActivity(), R.layout.reddit_post, tweetList);
	        //adapter.clearItems();
	        listView.setAdapter(adapter);
	        listView.setOnScrollListener(new OnScrollListener() {      
	            @Override
	            public void onScrollStateChanged(AbsListView view, int scrollState) { }
	
	            @Override
	            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
	
	                if(view.getLastVisiblePosition() >= totalItemCount - 5){
	                	
	                	if(!searchBusy && !waitingToSearch  && AppInstance.redditPageToken != null) {
	            			
	            			if(beginningOfList && AppInstance.redditPageToken == "") {
	            	    		listView.setSelection(0);
	            	    		Log.d(TAG,"Scroll to the top of the list");
	            	    	}
	            			Log.d(TAG,"Trigger to search for new items");
	            			new RedditFeed(RedditPostFragment.this).execute();
	            			endOfList = false;
	            			
	            		} else if(searchBusy && AppInstance.redditPageToken == "") {
	            			waitingToSearch = true;
	            			
	            		} else if (AppInstance.redditPageToken == null && !endOfList){
	            			Toast.makeText(context, "No more uploads found for "+AppInstance.getMember().getName(), Toast.LENGTH_SHORT).show();
	            			adapter.removeProgressBar();
	            			endOfList = true;		
	            		}
	                }
	            }
	        });  
	        
	        loadingLayout = (RelativeLayout) getView().findViewById(R.id.loading_layout);
	        loadingTextView = (TextView) getView().findViewById(R.id.loading_text);
        	loadingProgressBar = (ProgressBar) getView().findViewById(R.id.loading_progress_bar);
        	loadingImage = (ImageView) getView().findViewById(R.id.loading_image);
        	
	        if(!Util.isNetworkAvailable()) {
	        	loadingTextView.setText(getResources().getString(R.string.loading_reddit_failed));
	        	loadingProgressBar.setVisibility(View.GONE);
	        	loadingImage.setVisibility(View.VISIBLE);
	        } else {
	        	loadingTextView.setText(getResources().getString(R.string.loading_reddit));
	        	loadingProgressBar.setVisibility(View.VISIBLE);
	        	loadingImage.setVisibility(View.GONE);
	        }
	        
    	}
    }
    
    
    
    protected class RedditAdapter extends ArrayAdapter<RedditPost> {

	    Context context; 
	    int layoutResourceId;    
	    List<RedditPost> data = new ArrayList<RedditPost>();
	    
	    public RedditAdapter(Context context, int layoutResourceId, List<RedditPost> data) {
	        super(context, layoutResourceId, tweetList);
	        this.layoutResourceId = layoutResourceId;
	        this.context = context;
	        this.data = data;	       
	    }
	    
	    @Override
	    public int getCount() {	    	
	    	return data.size();
	    }
	    
	    public void add(RedditPost item)
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
	        PostHolder holder = null;
	        
	        if(row == null)
	        {
	            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
	            row = inflater.inflate(layoutResourceId, parent, false);
	            
	            holder = new PostHolder();
	            holder.itemView = (LinearLayout) row.findViewById(R.id.item_view);
	            holder.image = (ResizableImageView) row.findViewById(R.id.image);
	            holder.progressBar = (ProgressBar) row.findViewById(R.id.progress_bar);
	            holder.youtubePlay = (ImageView) row.findViewById(R.id.youtube_play);
	            holder.title = (TextView)row.findViewById(R.id.title);
	            holder.domain = (TextView)row.findViewById(R.id.domain);
	            holder.comments = (TextView)row.findViewById(R.id.comments);
	            
	            row.setTag(holder);
	        }
	        else
	        {
	            holder = (PostHolder)row.getTag();
	        }
	        
	        
	        final RedditPost item = data.get(position);
	        	        
	        //Regex and highlight @			
			if(!TextUtils.isEmpty(item.getTitle())) {
				
				String text = item.getTitle();
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
				holder.title.setText(wordtoSpan);
			} else {
				holder.title.setText(R.string.loading_text);
			}
		    
			if(!TextUtils.isEmpty(item.getImageMed())) {
				holder.image.setVisibility(View.VISIBLE);				
				holder.image.setImageBitmap(null);
				holder.progressBar.setVisibility(View.VISIBLE);
				
			    if(Util.getHiResThumbnail() && !TextUtils.isEmpty(item.getImageHigh()) ){
			    	imageLoader.DisplayImage(item.getImageHigh(), holder.image);
			    	//Log.e(TAG,"high thumb: "+item.getImageHigh());
			    } else {
			    	imageLoader.DisplayImage(item.getImageMed(), holder.image);
			    }
			} else {
				holder.image.setVisibility(View.GONE);
				holder.progressBar.setVisibility(View.GONE);
			}
		    
			if(item.getVideoId() != null) {
				holder.youtubePlay.setVisibility(View.VISIBLE);
				holder.youtubePlay.setOnClickListener(new OnClickListener() {
			        @Override
			        public void onClick(View v) {
			            Intent i = new Intent(context, YoutubePlayer.class);
						i.putExtra("video_id", item.getVideoId());
						startActivity(i);
			        }
			    });
			} else {
				holder.youtubePlay.setVisibility(View.GONE);
			}
			
            holder.domain.setText("("+item.getDomain()+")");
            holder.comments.setText(item.getNumComments()+" comments");
            
	        return row;
	    }
	    
	    class PostHolder
	    {
	    	LinearLayout itemView;
	        ResizableImageView image;
	        ProgressBar progressBar;
	        TextView title;
	        TextView domain;
	        TextView comments;
	        ImageView youtubePlay;
	    }
	}
    
    

	public void onTaskCompleted(List<RedditPost> redditFeed) {
		
		if(loadingLayout.getVisibility() == View.VISIBLE) {
			loadingLayout.setVisibility(View.GONE);
		}
		
		if(redditFeed != null) {			
			//Only save the first page of RedditItems
	        if(beginningOfList) {
	        	AppInstance.updateRedditFeed(redditFeed);
	        	beginningOfList = false;
	        	Log.d(TAG, "adding items to updateYoutubeItems");
	        } else {
	        	for(RedditPost item : redditFeed) {
	        		adapter.add(item);
				}
	        }
	        	
	        	
	        // If the records in the database are not up to date
	        if(!AppInstance.isRedditFeedUpToDate) {
	        	adapter.clearItems(); //Clear all items currently in the adapter
	        	
	        	for(RedditPost item : redditFeed) {
	        		adapter.add(item);
				}
	        	AppInstance.isRedditFeedUpToDate = true;
	        }
	        
	        
			searchBusy = false;
			
			//Used if you scroll to the end of the list before the first result is returned
			if(waitingToSearch) {
				
				if(AppInstance.redditPageToken != null) {
        			new RedditFeed(RedditPostFragment.this).execute();
        			endOfList = false;
        		} 
				waitingToSearch = false;
			}
		}
			
	}
	
	
    
	class RedditFeed extends AsyncTask<Void, Void, Void> {

		private final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	    private final JsonFactory JSON_FACTORY = new JacksonFactory();
	    private YouTube youtube;
	    
	    List<RedditPost> redditFeed;
	    private RedditPostFragment redditFragment;
	    
	    private final String URL= "http://www.reddit.com/r/mindcrack/.json";
	    
	    public RedditFeed(RedditPostFragment redditFragment) {
	    	this.redditFragment = redditFragment;
	    	waitingToSearch = false;
	    }
	    
	    protected void onPreExecute() {
	    	searchBusy = true;
	    }

	    @Override
	    protected Void doInBackground(Void... params) {

	    	redditFeed = getRecentRedditFeed(AppInstance.redditPageToken);
	    	
			return null;
	    }  


	    protected void onPostExecute(Void v) {
	    	//Still the current member
	    	if(redditFeed != null) {
	    		redditFragment.onTaskCompleted(redditFeed);
	    	}
	    } 
 
	    
	    public List<RedditPost> getRecentRedditFeed(String pageToken) {
	    	try {
		        String rawUrl = URL + (TextUtils.isEmpty(pageToken)? "": ("?after="+pageToken));
	    		
	    		String raw=RemoteData.readContents(rawUrl);
	            List<RedditPost> list = new ArrayList<RedditPost>();
	            try{
	                JSONObject data=new JSONObject(raw).getJSONObject("data");
	                JSONArray children=data.getJSONArray("children");
	                 
	                //Using this property we can fetch the next set of posts
	                AppInstance.redditPageToken = data.getString("after");
	                Log.e(TAG, "pageToken: "+AppInstance.redditPageToken);
	                for(int i=0;i<children.length();i++){
	                    JSONObject cur=children.getJSONObject(i).getJSONObject("data");
	                    
	                    RedditPost post = new RedditPost();
	                    post.setId(cur.optString("id"));
	                    post.setTitle(cur.optString("title"));
	                    post.setTitleFlair(cur.optString("link_flair_text"));
	                    
	                    post.setNumComments(cur.optInt("num_comments"));
	                    post.setPoints(cur.optInt("score"));
	                    post.setAuthor(cur.optString("author"));
	                    post.setPermalink(cur.optString("permalink"));
	                    post.setDomain(cur.optString("domain"));
	                    post.setUrl(cur.optString("url"));
	                    post.setTextHtml(cur.optString("selftext_html"));
	                    //post.setCreated(Long.valueOf(cur.optString("created")));
	                    
	                    String thumb = cur.optString("thumbnail");
	                    if(!thumb.equals("self") && !thumb.equals("nsfw") && !thumb.equals("default"))
	                    	post.setImageMed(thumb);
	                    
	                    Pattern compiledImage = Pattern.compile(".(?:jpg|gif|png)$", Pattern.CASE_INSENSITIVE);
                		Matcher matcherImage = compiledImage.matcher(post.getUrl());
                		if(matcherImage.find()) {
                			post.setImageHigh(post.getUrl());
                		}
	                    
	                    
	                    
	                    if(post.getDomain().equals("youtube.com") || post.getDomain().equals("m.youtube.com")) {
	                    	try {
	                    		
	                    		String pattern = "https?:\\/\\/(?:[0-9A-Z-]+\\.)?(?:youtu\\.be\\/|youtube\\.com\\S*[^\\w\\-\\s])([\\w\\-]{11})(?=[^\\w\\-]|$)(?![?=&+%\\w]*(?:['\"][^<>]*>|<\\/a>))[?=&+%\\w]*";

	                    		Pattern compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
	                    		Matcher matcher = compiledPattern.matcher(post.getUrl());
	                    		if(matcher.find()) { //Url Validated as youtube
	                    			Pattern patternParam = Pattern.compile("(?<=v=).*?(?=&|$)");
		                    		Matcher match = patternParam.matcher(post.getUrl());
		                    		if(match.find()){ //Url has video id parameter
		                    			post.setVideoId(match.group()); 
		                    		} else {
		                    			Pattern encodedParam = Pattern.compile("(?<=v%3D).*?(?=%|$)");
			                    		Matcher encodedMatch = encodedParam.matcher(post.getUrl());
			                    		if(encodedMatch.find()){ //Url has encodedvideo id parameter
			                    			post.setVideoId(encodedMatch.group()); 
			                    		} else {
		                    			
			                    			String shortUrl = "([a-z_A-Z0-9-]{11})";
			                    			Pattern patternSlash = Pattern.compile(shortUrl);
				                    		Matcher m = patternSlash.matcher(post.getUrl());
				                    		if(m.find()){ //Url has video id after slash
				                    			post.setVideoId(m.group()); 
				                    		}
			                    		}
		                    		}
	                    		}
	                    		
	                    		
	                    			
	                    		
	                    		if(!TextUtils.isEmpty(post.getVideoId())) {
		                    		Log.e(TAG, "vid: "+post.getVideoId());
		            		        youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpRequestInitializer() {
		            		            public void initialize(HttpRequest request) throws IOException {}
		            		          }).setApplicationName("mindcrack-andriod-api").build();
	
		            		        YouTube.Videos.List search = youtube.videos().list("snippet");
		            		       
		            		        search.setKey(Constants.DEVELOPER_KEY);
		            		        search.setId(post.getVideoId());
		            		       
		            		        search.setFields("items(snippet/thumbnails/medium/url,snippet/thumbnails/high/url)");
		            		        
		            		        VideoListResponse playlistItemResponse = search.execute();
		            		       
		            		        List<Video> playlistItemList = playlistItemResponse.getItems();
		            		        
		            		        if (playlistItemList != null) {
		            		        	post.setImageMed(playlistItemList.get(0).getSnippet().getThumbnails().getMedium().getUrl());
		            		        	post.setImageHigh(playlistItemList.get(0).getSnippet().getThumbnails().getHigh().getUrl());       
		            		        }
	                    		}
	            	        } catch (GoogleJsonResponseException e) {
	            	            System.err.println("There was a service error: " + e.getDetails().getCode() + " : "
	            	                + e.getDetails().getMessage()+" - "+e.getContent());
	            	        } catch (IOException e) {
	            	            System.err.println("There was an IO error: " + e.getCause() + " : " + e.getMessage());
	            	        } catch (Exception e) {
	            	            Log.e(TAG, e.getMessage());
	            	            e.printStackTrace();
	            	        }
	                    }
	                    
	                    if(post.getTitle() != null){
	                        list.add(post);
	                    }
	                }
	            }catch(Exception e){
	                Log.e("fetchPosts()",e.toString());
	            }
	            return list;
	    		
	    		
	    		//SharedPreferences settings = redditFragment.context.getSharedPreferences(Constants.PREFS, 0);
		        //String redditAccesToken = settings.getString(Constants.PREF_TW_ACCESS_TOKEN, "");
		        //String redditTokenType = settings.getString(Constants.PREF_TW_TOKEN_TYPE, "");

		    	/*try {
		    		//reddit.setOAuth2Token(new OAuth2Token(redditTokenType, redditAccesToken));
		    		
	    			List<Post> response = null;//reddit.getUserTimeline(playlistId, new Paging(pageToken, 20));
	    			if(response == null || response.size() == 0) 
	    				;//AppInstance.redditPageToken = 0;
	    			else
	    				;//AppInstance.redditPageToken ++;
	    			Log.e(TAG, "token: "+AppInstance.redditPageToken);
	    			return response;
	    		
		        } catch (Exception e) {
		            e.printStackTrace();
		            //TODO://
		            //if API is not available this doesn't tell the user
		        }*/
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

}
