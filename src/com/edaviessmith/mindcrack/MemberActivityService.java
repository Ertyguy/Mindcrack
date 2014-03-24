package com.edaviessmith.mindcrack;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.edaviessmith.mindcrack.data.Member;
import com.edaviessmith.mindcrack.data.YoutubeItem;
import com.edaviessmith.mindcrack.db.MemberORM;
import com.edaviessmith.mindcrack.db.YoutubeItemORM;
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


public class MemberActivityService extends IntentService   {

	private static String TAG = "MemberActivityService";

	private static List<Member> memberList;
	private static List<YoutubeItem> youtubeItemList;
	private static int threadCounter;

	public MemberActivityService(){
		super("MemberActivityService");
	}


	@Override
	protected void onHandleIntent(Intent arg0) {
	
		memberList = MemberORM.getMembersbyStatus(this, Constants.FAVORITE);
		youtubeItemList = new ArrayList<YoutubeItem>();
		threadCounter = 0;
		
		for(Member member : memberList) {
			YoutubeItem youtubeItem = YoutubeItemORM.getMemberLatestYoutubeItem(this, member.getId());
			if(youtubeItem != null) {
				youtubeItemList.add(youtubeItem);
			} else {
				//No videos for the user
				threadCounter ++;
				new YoutubePlaylist(this, member.getId(), null).execute(member.getUploadsId());
			}
			
		}
	
		checkMemberActivity();
	}
	
	
	//Update latestVideo if checked member exists in memberActivities or add then start notification
	private void checkMemberActivity() {			

		//Only create notifications after all thread have completed
		if(threadCounter > 0) {
			return;
		} else {
			threadCounter = 0;
			createNotification();
		}

	}
	
	
	
	
	private void createNotification() {
		List<Member> updatedMembers = new ArrayList<Member>();
		
		for(int i=0; i< memberList.size(); i++) {
			Member member = memberList.get(i);
			if(youtubeItemList.get(i).getMemberId() != member.getId()) {
				youtubeItemList.add(i, YoutubeItemORM.getMemberLatestYoutubeItem(this, member.getId()));
			}
			YoutubeItem youtubeItem = youtubeItemList.get(i);
			
			try {				
				boolean newYoutubeItem = new YoutubePlaylist(this, member.getId(),youtubeItem.getVideoId()).execute(member.getUploadsId()).get();
			
				if(newYoutubeItem || true) {	//REMOVE true
					updatedMembers.add(member);
				}
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
			
		}

		if(updatedMembers.size() > 0) {
			String title = "Mindcrack News";
			String text = "New upload from "+updatedMembers.get(0).getName();
			
			
			Intent intent = new Intent(this, Members.class);
			
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
	        intent.putExtra(Constants.PREF_MEMBER, updatedMembers.get(0).getId());
	
	
	        NotificationCompat.Builder builder = new NotificationCompat.Builder(this).setContentTitle(title)
	    		.setContentText(text)	        		
	    		.setContentIntent(PendingIntent.getActivity(this, updatedMembers.get(0).getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT));
	        
	        if(Util.notificationsIconEnabled()) {
	        	builder.setSmallIcon(updatedMembers.get(0).getIcon());
	        } else {
	        	builder.setSmallIcon(R.drawable.ic_launcher);
	        }
	        //Next 3 members as sub-icons
	        for(int i = 1; i < updatedMembers.size() && i<= 3; i++) {
	        	Member mem = updatedMembers.get(i);
	        	
	        	Intent in = new Intent(this, Members.class);
	        	in.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		        in.putExtra(Constants.PREF_MEMBER, mem.id);
		        
	        	builder.addAction(mem.getIcon(), mem.getName(), PendingIntent.getActivity(this, mem.getId(), in, PendingIntent.FLAG_UPDATE_CURRENT));
	        }        
	        
	        Notification notification = builder.build();
	
	        // Hide the notification after it's selected
	        notification.flags |= Notification.FLAG_AUTO_CANCEL;
	        
	        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	        notificationManager.notify(0, notification);
	        
		}
	}


	static class  YoutubePlaylist extends AsyncTask<String, Void, Boolean> {

		private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	    private static final JsonFactory JSON_FACTORY = new JacksonFactory();
	    
	    private static YouTube youtube;
	    private List<PlaylistItem> playlistItems;
	    private MemberActivityService memberActivityService;
	    private int currentMemberId;
	    private String currentVideoId;
	    
	    public YoutubePlaylist(MemberActivityService memberActivityService, int currentMemberId, String currentVideoId) {
	    	this.memberActivityService = memberActivityService;
	    	this.currentMemberId = currentMemberId;
	    	this.currentVideoId = currentVideoId;
	    }
	    
	    protected void onPreExecute() {
	    }

	    @Override
	    protected Boolean doInBackground(String... params) {

	    	playlistItems = getRecentPlaylistItems(params[0]);
	    	Log.e(TAG, "playlistItems are good "+(playlistItems != null));
	    	
	    	if(playlistItems != null) {
	    		return memberActivityService.onTaskCompleted(playlistItems, currentMemberId, currentVideoId);
	    	} else {
	    		return false;
	    	}
	    }  
	    
	    public List<PlaylistItem> getRecentPlaylistItems(String playlistId) {
	    	try {
		        youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpRequestInitializer() {
		            public void initialize(HttpRequest request) throws IOException {}
		          }).setApplicationName("mindcrack-andriod-api").build();

		        YouTube.PlaylistItems.List search = youtube.playlistItems().list("snippet");
		        
		        search.setKey(Constants.DEVELOPER_KEY);
		        search.setPlaylistId(playlistId);
		        search.setMaxResults(20L);
		        search.setFields("nextPageToken,items(snippet/resourceId/videoId,snippet/title,snippet/publishedAt,snippet/thumbnails/medium/url,snippet/thumbnails/high/url)"); 
		
		        PlaylistItemListResponse playlistItemResponse = search.execute();
		        
		        List<PlaylistItem> playlistItemList = playlistItemResponse.getItems();
		        
		        if (playlistItemList != null) {
		        	return playlistItemList;          
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
	
	
	//Return true if videoId in Database is not the first playlistItem returned from API
	public boolean onTaskCompleted(List<PlaylistItem> playlistItems, int memberId, String currentVideoId) {
		
		List<YoutubeItem> youtubeItems = new ArrayList<YoutubeItem>();
		
		if(playlistItems != null) {
			
			int index = 0;

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
		}
		
		
		//Insert youtubeItems for member
		if(currentVideoId == null) {
			YoutubeItemORM.insertMemberYoutubeItems(this, youtubeItems);
			threadCounter --;
			checkMemberActivity();
			return false;
		} else {
			//Update member youtubeItems
			if(!youtubeItems.get(0).getVideoId().equals(currentVideoId)) {
				YoutubeItemORM.updateMemberYoutubeItems(this, youtubeItems);
				return true;
			}

			return false;
		}
		
		
	}


}
