package com.edaviessmith.mindcrack;


import java.io.IOException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.List;
import com.actionbarsherlock.app.SherlockFragment;

import com.edaviessmith.mindcrack.data.YoutubeItem;
import com.edaviessmith.mindcrack.util.ImageLoader;
import com.edaviessmith.mindcrack.util.ResizableImageView;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


public class YoutubeFragment extends SherlockFragment{

	private static String TAG = "YoutubeFragment";
	
	private Context context;
	private View view;
	private GridView gridView;
	private YoutubeAdapter adapter;
	
	private RelativeLayout loadingLayout;
	private TextView loadingTextView;
	private ProgressBar loadingProgressBar;
	private ImageView loadingImage;
	
	private static List<YoutubeItem> youtubeItemList;
	private ImageLoader imageLoader;

	private static boolean beginningOfList;
	private static boolean endOfList;
	private static boolean searchBusy;
	private static boolean waitingToSearch;
	private static YoutubeItem loading;
	
	
	private static boolean isDeviceTablet;
		
	
	public static YoutubeFragment newInstance() {
		YoutubeFragment fragment = new YoutubeFragment();
	    return fragment;
	}

	 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {    	
        view = inflater.inflate(R.layout.youtube_fragment, null);
        context = view.getContext();
       
        imageLoader = new ImageLoader(context);
        isDeviceTablet = Util.isDeviceTablet();
        loading = new YoutubeItem();
        
        if(!Util.isNetworkAvailable()) {
        	//Toast.makeText(context, "Cannot connect to the internet", Toast.LENGTH_LONG).show();
        	
        }
        
        return view;
    }
	
	
    
    public void onActivityCreated (Bundle savedInstanceState){
    	super.onActivityCreated(savedInstanceState);
    	Log.d(TAG, "fragment onActivityCreated");
    	
    	boolean changedMember = !AppInstance.checkSetYoutubeMemberIdIsCurrent();
    	
		if(changedMember) {
			Log.e(TAG, "member is changed");
			AppInstance.playlistPageToken = "";
    		beginningOfList = true;
			

			youtubeItemList = new ArrayList<YoutubeItem>(AppInstance.getYoutubeItems());
	    	// Add loading Youtube item
	    	youtubeItemList.add(youtubeItemList.size(), loading);
	    		    	
	    	if(adapter != null) {
	    		adapter.clearItems();
			 	for(YoutubeItem item : youtubeItemList) {
			 		adapter.add(item);
			 	}
			}
	    	
	    	AppInstance.isYoutubeItemsUpToDate = false;
	    	new YoutubePlaylist(YoutubeFragment.this, AppInstance.getCurrentYoutubeMemberId()).execute(AppInstance.getMember().getUploadsId());
	        
		} else {
			if(loadingLayout != null) {
				loadingLayout.setVisibility(View.GONE);
			}
		}
		
		
		if(adapter == null || gridView == null) {
			initView();
		}
		
    	
		if(youtubeItemList == null) {
			youtubeItemList = new ArrayList<YoutubeItem>(AppInstance.getYoutubeItems());
	    	// Add loading Youtube item
	    	youtubeItemList.add(youtubeItemList.size(), loading);
	    	
	    	if(adapter != null) {
	    		adapter.clearItems();
			 	for(YoutubeItem item : youtubeItemList) {
			 		adapter.add(item);
			 	}
			}
	    	
	    	AppInstance.isYoutubeItemsUpToDate = false;
	    	new YoutubePlaylist(YoutubeFragment.this, AppInstance.getCurrentYoutubeMemberId()).execute(AppInstance.getMember().getUploadsId());	    		   
		}
    	

        if(changedMember) {
        	Log.e(TAG, "MEMBER HAS CHANGED SCROLL NOW");
        	gridView.smoothScrollToPosition(0);
	    	if(loadingLayout != null) {
	    		loadingLayout.setVisibility(View.VISIBLE);
			}
        } 
    	    
    }

    private void initView() {
    	Log.i(TAG, "initView");
    	if(isAdded()) {
	        gridView = (GridView) getActivity().findViewById(R.id.youtube_fragment_list);
	        setGridColumns();
	        
	        adapter = new YoutubeAdapter(getActivity(), R.layout.youtube_item, youtubeItemList);
	        //adapter.clearItems();
	        gridView.setAdapter(adapter);
	        gridView.setOnScrollListener(new OnScrollListener() {      
	            @Override
	            public void onScrollStateChanged(AbsListView view, int scrollState) { }
	
	            @Override
	            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
	
	                if(view.getLastVisiblePosition() >= totalItemCount - 5){
	                	
	                	if(!searchBusy && !waitingToSearch  && AppInstance.playlistPageToken != null) {
	            			
	            			if(beginningOfList && AppInstance.playlistPageToken == "") {
	            	    		gridView.setSelection(0);
	            	    		Log.d(TAG,"Scroll to the top of the list");
	            	    	}
	            			Log.d(TAG,"Trigger to search for new items");
	            			new YoutubePlaylist(YoutubeFragment.this, AppInstance.getCurrentYoutubeMemberId()).execute(AppInstance.getMember().getUploadsId());
	            			endOfList = false;
	            			
	            		} else if(searchBusy && AppInstance.playlistPageToken == "") {
	            			waitingToSearch = true;
	            			
	            		} else if (AppInstance.playlistPageToken == null && !endOfList){
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
	        	loadingTextView.setText(getResources().getString(R.string.loading_youtube_failed));
	        	loadingProgressBar.setVisibility(View.GONE);
	        	loadingImage.setVisibility(View.VISIBLE);
	        } else {
	        	loadingTextView.setText(getResources().getString(R.string.loading_youtube));
	        	loadingProgressBar.setVisibility(View.VISIBLE);
	        	loadingImage.setVisibility(View.GONE);
	        }
	        
    	}
    }
    
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        setGridColumns();
    }
    

    
    protected void setGridColumns() {
    	if(isDeviceTablet || Util.isDeviceLandscape()) {
        	gridView.setNumColumns(2);
        } else {
        	gridView.setNumColumns(1);
        }
    }
	
	protected class YoutubeAdapter extends ArrayAdapter<YoutubeItem> {

	    Context context; 
	    int layoutResourceId;    
	    List<YoutubeItem> data = new ArrayList<YoutubeItem>();
	    
	    public YoutubeAdapter(Context context, int layoutResourceId, List<YoutubeItem> data) {
	        super(context, layoutResourceId, youtubeItemList);
	        this.layoutResourceId = layoutResourceId;
	        this.context = context;
	        this.data = data;	       
	    }
	    
	    @Override
	    public int getCount() {
	    	return data.size();
	    }
	    
	    public void add(YoutubeItem item)
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
	        YoutubeHolder holder = null;
	        
	        if(row == null)
	        {
	            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
	            row = inflater.inflate(layoutResourceId, parent, false);
	            
	            holder = new YoutubeHolder();
	            holder.itemView = (LinearLayout) row.findViewById(R.id.item_view);
	            holder.videoImage = (ResizableImageView) row.findViewById(R.id.video_image);
	            holder.progressBar = (ProgressBar) row.findViewById(R.id.progress_bar);
	            holder.title = (TextView)row.findViewById(R.id.title);
	            holder.date = (TextView)row.findViewById(R.id.date);
	            
	            row.setTag(holder);
	        }
	        else
	        {
	            holder = (YoutubeHolder)row.getTag();
	        }
	        
	        
	        final YoutubeItem item = data.get(position);
	        
	        if(!TextUtils.isEmpty(item.getVideoId())) {
		        OnClickListener openYoutubePlayer = new OnClickListener() {
			        @Override
			        public void onClick(View v) {
			            Log.d("Youtube Playlist item", item.getVideoId());
			            Intent i = new Intent(context, YoutubePlayer.class);
						i.putExtra("video_id", item.getVideoId());
						startActivity(i);
			        }
			    };
		        
			    holder.title.setOnClickListener(openYoutubePlayer);
			    holder.videoImage.setOnClickListener(openYoutubePlayer);
			    
	        }
	        
		    holder.videoImage.setImageBitmap(null);
		    if(Util.getHiResThumbnail()){
		    	imageLoader.DisplayImage(item.getImageHigh(), holder.videoImage);
		    } else {
		    	imageLoader.DisplayImage(item.getImageMed(), holder.videoImage);
		    }
		    
            holder.title.setText(item.getTitle());
            holder.date.setText(Util.getTimeSince(item.getDate()));

            /*if(holder.videoImage.getDrawable() != null){
            	//holder.videoImage.setImageBitmap();
            	holder.progressBar.setVisibility(View.GONE);
            } else {
            	
            	holder.progressBar.setVisibility(View.VISIBLE);
            }*/
            
	        return row;
	    }
	    
	    class YoutubeHolder
	    {
	    	LinearLayout itemView;
	        ResizableImageView videoImage;
	        ProgressBar progressBar;
	        TextView title;
	        TextView date;
	    }
	}
	
	
	public void onTaskCompleted(List<PlaylistItem> playlistItems) {
		
		if(loadingLayout.getVisibility() == View.VISIBLE) {
			loadingLayout.setVisibility(View.GONE);
		}
		
		if(playlistItems != null) {
			List<YoutubeItem> youtubeItems = new ArrayList<YoutubeItem>();
			int index = 0;
			int memberId = AppInstance.getMember().getId();
			for(PlaylistItem playlistItem : playlistItems) {
				if(playlistItem != null) {
					try {
						YoutubeItem item = new YoutubeItem();   
						item.setId(index++);
						item.setMemberId(memberId);
					    item.setVideoId(playlistItem.getSnippet().getResourceId().getVideoId());
					    item.setImageMed(playlistItem.getSnippet().getThumbnails().getMedium().getUrl());
					    item.setImageHigh(playlistItem.getSnippet().getThumbnails().getHigh().getUrl());
					    item.setTitle(playlistItem.getSnippet().getTitle());
					    
					    
					    String published = playlistItem.getSnippet().getPublishedAt().toString();			    
					    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	
					    long dateInMilli = formatter.parse(published).getTime();		    
						item.setDate(dateInMilli/1000);					
	
					    youtubeItems.add(item);
					    
				    } catch (ParseException e) { 
						e.printStackTrace();
					} catch (Exception e) {
						//TODO add logging to excpetion (unsure occurance)
						e.printStackTrace();
					}
				}
			}
			
			//Only save the first page of YoutubeItems
	        if(beginningOfList) {
	        	AppInstance.updateYoutubeItems(youtubeItems);
	        	beginningOfList = false;
	        	Log.d(TAG, "adding items to updateYoutubeItems");
	        } else {
	        	for(YoutubeItem item : youtubeItems) {
	        		adapter.add(item);
				}
	        }
	        	
	        	
	        // If the records in the database are not up to date
	        if(!AppInstance.isYoutubeItemsUpToDate) {
	        	adapter.clearItems(); //Clear all items currently in the adapter
	        	
	        	for(YoutubeItem item : youtubeItems) {
	        		adapter.add(item);
				}
	        	AppInstance.isYoutubeItemsUpToDate = true;
	        }
	        
	        
			searchBusy = false;
			
			//Used if you scroll to the end of the list before the first result is returned
			if(waitingToSearch) {
				
				if(AppInstance.playlistPageToken != null) {
        			new YoutubePlaylist(YoutubeFragment.this, AppInstance.getCurrentYoutubeMemberId()).execute(AppInstance.getMember().getUploadsId());
        			endOfList = false;
        		} 
				waitingToSearch = false;
			}
		}
			
	}

  

	static class YoutubePlaylist extends AsyncTask<String, String, Void> {

		private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	    private static final JsonFactory JSON_FACTORY = new JacksonFactory();
	    
	    private static YouTube youtube;
	    private List<PlaylistItem> playlistItems;
	    private YoutubeFragment youtubeFragment;
	    private int currentMemberId;
	    
	    public YoutubePlaylist(YoutubeFragment youtubeFragment, int currentMemberId) {
	    	this.youtubeFragment = youtubeFragment;
	    	this.currentMemberId = currentMemberId;
	    	waitingToSearch = false;
	    }
	    
	    protected void onPreExecute() {
	    	searchBusy = true;
	    }

	    @Override
	    protected Void doInBackground(String... params) {

	    	playlistItems = getRecentPlaylistItems(params[0], AppInstance.playlistPageToken);
	    	
			return null;
	    }  


	    protected void onPostExecute(Void v) {
	    	//Still the current member
	    	if(playlistItems != null) {
	    		youtubeFragment.onTaskCompleted(playlistItems);
	    	}
	    } 
 
	    
	    public List<PlaylistItem> getRecentPlaylistItems(String playlistId, String pageToken) {
	    	try {
		        youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpRequestInitializer() {
		            public void initialize(HttpRequest request) throws IOException {}
		          }).setApplicationName("mindcrack-andriod-api").build();

		        YouTube.PlaylistItems.List search = youtube.playlistItems().list("snippet");
		        
		        search.setKey(Constants.DEVELOPER_KEY);
		        search.setPlaylistId(playlistId);
		        search.setPageToken(pageToken);
		        search.setMaxResults(20L);
		        search.setFields("nextPageToken,items(snippet/resourceId/videoId,snippet/title,snippet/publishedAt,snippet/thumbnails/medium/url,snippet/thumbnails/high/url)"); 
		
		        PlaylistItemListResponse playlistItemResponse = search.execute();
		        
		        List<PlaylistItem> playlistItemList = playlistItemResponse.getItems();
		        
		        
		        if(AppInstance.getMember().getId() == this.currentMemberId) {
		        	AppInstance.playlistPageToken = playlistItemResponse.getNextPageToken();
			        Log.i("Youtube","search executed page token: "+ AppInstance.playlistPageToken);
			        
			        if (playlistItemList != null) {
			        	return playlistItemList;          
			        }
		        } else {
		        	return null;
		        }
		        
	        } catch (GoogleJsonResponseException e) {
	            System.err.println("There was a service error: " + e.getDetails().getCode() + " : "
	                + e.getDetails().getMessage()+" - "+e.getContent());
	        } catch (IOException e) {
	            System.err.println("There was an IO error: " + e.getCause() + " : " + e.getMessage());
	        } catch (Throwable t) {
	            t.printStackTrace();
	        }
	    return null;
	    }
	    
	}




}
