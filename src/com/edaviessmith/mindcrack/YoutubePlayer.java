package com.edaviessmith.mindcrack;

import java.io.IOException;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.OnInitializedListener;
import com.google.android.youtube.player.YouTubePlayer.PlaybackEventListener;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
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


public class YoutubePlayer extends SherlockFragmentActivity {
	
	private View view;
    private String video;    
    static TextView title, text;
    private boolean playing;
    
    private ScrollView content_layout;
    private LinearLayout youtube_video;
    
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.youtube_player);
	    // Show the Up button in the action bar.
	 	getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);
	 	
	    FragmentManager fragmentManager = getSupportFragmentManager();
	    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
	    
	    view = this.getWindow().getDecorView();
	    
	    title = (TextView)findViewById(R.id.title);
	    text = (TextView)findViewById(R.id.text);
	    content_layout = (ScrollView)findViewById(R.id.youtube_scroll_view);
	    view = (View)findViewById(R.id.youtube_player_layout);
	    youtube_video = (LinearLayout)findViewById(R.id.youtube_video);
	    
	    YouTubePlayerSupportFragment fragment = new YouTubePlayerSupportFragment();
	    
	    fragmentTransaction.replace(R.id.youtube_video, fragment);
	    fragmentTransaction.commit();
	    
        refreshActionbar();
	    
	    Intent i = getIntent();
        video = i.getStringExtra("video_id");
	    
	    fragment.initialize(Constants.DEVELOPER_KEY, new OnInitializedListener() {

            @Override
            public void onInitializationSuccess(Provider provider, YouTubePlayer player, boolean wasRestored) {
            	if(!wasRestored) {
            		player.cueVideo(video);
            	}
            	player.setShowFullscreenButton(false);
            	player.setPlaybackEventListener(new PlaybackEventListener(){

					@Override
					public void onBuffering(boolean arg0) { }

					@Override
					public void onPaused() {
						playing = false;
						displayActionbarNav();
					}

					@Override
					public void onPlaying() {
						playing = true;		
						displayActionbarNav();
					}

					@Override
					public void onSeekTo(int arg0) { }

					@Override
					public void onStopped() {
						playing = false;
						displayActionbarNav();
					}
            		
            	});
            }

            @Override
            public void onInitializationFailure(Provider provider, YouTubeInitializationResult error) {
            	Toast.makeText(getApplicationContext(), "Sorry, Your device is not working because ("+error.toString()+")", Toast.LENGTH_LONG).show();
            }
            

        });

	    
	    new YoutubePlayerInfo().execute(video);
	}

	@Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        displayActionbarNav();
    }
	
	
	private void refreshActionbar() {
		if(AppInstance.getMember() != null) {
			getSherlock().getActionBar().setTitle(AppInstance.getMember().name);
			getSherlock().getActionBar().setIcon(AppInstance.getMember().icon);
		}
	}
	
	
	@SuppressLint("NewApi")
	private void displayActionbarNav() {
		
		if(!playing || getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {			
			getSherlock().getActionBar().show();			
		    
		    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		    content_layout.setVisibility(LinearLayout.VISIBLE);
		    
		    ViewGroup.LayoutParams params = youtube_video.getLayoutParams();
		    params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
		    youtube_video.setLayoutParams(params);
		    
		    if (android.os.Build.VERSION.SDK_INT >= 14) {
		    	view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
		    }
		}
		
		if (playing && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			getSherlock().getActionBar().hide();
			
		    getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		    content_layout.setVisibility(LinearLayout.GONE);
		    
		    ViewGroup.LayoutParams params = youtube_video.getLayoutParams();
		    params.height = LinearLayout.LayoutParams.MATCH_PARENT;
		    youtube_video.setLayoutParams(params);
		    
		    if (android.os.Build.VERSION.SDK_INT >= 14) {
		    	view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		    }
		    
		}
	}
	@Override
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            NavUtils.navigateUpFromSameTask(this);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		//displayActionbarNav();	
	}
	
	static class YoutubePlayerInfo extends AsyncTask<String, String, Void> {

		private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	    private static final JsonFactory JSON_FACTORY = new JacksonFactory();
	    private static YouTube youtube;
		//private ProgressDialog progressDialog = new ProgressDialog(YoutubeFragment.this);

	    private List<Video> videoItems;
	    
	    

	    protected void onPostExecute(Void v) {
	    	
	    	if(videoItems != null) {
	    		Video videoItem = videoItems.get(0);

	    		title.setText(videoItem.getSnippet().getTitle());
	    		text.setText(videoItem.getSnippet().getDescription());
	    	}
	    	
	    } 
	    
	    
	    protected void onPreExecute() {
	    	/* progressDialog.setMessage("Downloading your data...");
	        progressDialog.show();
	        progressDialog.setOnCancelListener(new OnCancelListener() {
	            public void onCancel(DialogInterface arg0) {
	            	YoutubePlaylist.this.cancel(true);
	            }
	        });*/
	    }

	    @Override
	    protected Void doInBackground(String... params) {

	        try {
		        youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpRequestInitializer() {
		            public void initialize(HttpRequest request) throws IOException {}
		          }).setApplicationName("mindcrack-andriod-api").build();

		        YouTube.Videos.List search = youtube.videos().list("snippet, contentDetails, statistics");
		       
		        search.setKey(Constants.DEVELOPER_KEY);
		        search.setId(params[0]);
		        //search.setPageToken("");
		       
		        //search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
		
		        VideoListResponse playlistItemResponse = search.execute();
		        Log.i("Youtube","search executed");
		        List<Video> playlistItemList = playlistItemResponse.getItems();
		        
		        if (playlistItemList != null) {
		        	videoItems = playlistItemList;          
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

