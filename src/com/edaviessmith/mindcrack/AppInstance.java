package com.edaviessmith.mindcrack;

import java.util.List;
import java.util.Random;


import com.edaviessmith.mindcrack.data.Member;
import com.edaviessmith.mindcrack.data.YoutubeItem;
import com.edaviessmith.mindcrack.db.MemberORM;
import com.edaviessmith.mindcrack.db.YoutubeItemORM;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class AppInstance extends Application{

	private static Context context;
	private static String TAG = "AppInstance";
	
	//Members Activity
    public static Member currentMember;
    public static List<Member> mindcrackers; //Sidemenu
    public static List<Member> allMembers; //Manage members (any status)
	

	//Youtube Fragment
	public static int currentFragmentMemberId = -1;	
	public static List<YoutubeItem> youtubeItems;
	public static String playlistPageToken = "";

	//Update booleans
	public static boolean isMindcrackersUpToDate;
    public static boolean isAllMembersUpToDate;
    public static boolean isYoutubeItemsUpToDate;

    
	// CONTEXT
    public void onCreate(){
        super.onCreate();
        AppInstance.context = getApplicationContext();
    }

    public static Context getContext() {
        return AppInstance.context;
    }
	

	// MEMBERS
	public static List<Member> getMindcrackers() {
		if(mindcrackers == null || mindcrackers.size() == 0 || !isMindcrackersUpToDate) {
			mindcrackers = MemberORM.getVisibleMembers(getContext());
			isMindcrackersUpToDate = true;
			
			Log.d("APP","member list changed to up to date");
		}
		return mindcrackers;
	}
	
	public static List<Member> getAllMembers() {
		if(allMembers == null || allMembers.size() == 0 || !isAllMembersUpToDate) {
			allMembers = MemberORM.getMembers(getContext());
			isAllMembersUpToDate = true;
			
			Log.d("APP","all list changed to up to date");
		}
		return allMembers;
	}
	
	
	public static Member getMember() {
		if(currentMember == null) {
			SharedPreferences settings = getContext().getSharedPreferences(Constants.PREFS, 0);
		    int memberId = settings.getInt(Constants.PREF_MEMBER, -1);
		    //none set so make it random :D
		    if(memberId == -1) {
		    	Random rnd = new Random();
		    	memberId = rnd.nextInt(getMindcrackers().size());
		    }
		    for(Member member : getMindcrackers()) {
	        	if(member.getId() == memberId)
	        		currentMember = member;
	        }
		    //currentMember = getMindcrackers().get(memberId);
		}
		return currentMember;
	}
	
	@SuppressLint("NewApi")
	public static void setMember(int memberId) {
		SharedPreferences.Editor settings = getContext().getSharedPreferences(Constants.PREFS, 0).edit();
        settings.putInt(Constants.PREF_MEMBER,  memberId);
        if (android.os.Build.VERSION.SDK_INT >= 9) {
        	settings.apply();
        } else {
        	settings.commit();
        }
        //Member currently set
        if(memberId != getMember().getId()) {
	        for(Member member : getMindcrackers()) {
	        	if(member.getId() == memberId)
	        		currentMember = member;
	        }
	        isYoutubeItemsUpToDate = false;
        }
	}
		
    public static int getCurrentFragmentMemberId() {
    	if(currentFragmentMemberId == -1) {
    		setCurrentFragmentMemberId(AppInstance.getMember().getId());
    	}
		return currentFragmentMemberId;
	}

	public static void setCurrentFragmentMemberId(int currentFragmentMemberId) {
		AppInstance.currentFragmentMemberId = currentFragmentMemberId;
	}
	
    public static boolean checkSetFragmentMemberIdIsCurrent() {
    	boolean same = AppInstance.currentFragmentMemberId == getMember().getId();
    	setCurrentFragmentMemberId(AppInstance.getMember().getId());
    	
    	return same;    	
	}
	
	public static List<YoutubeItem> getYoutubeItems() {
		if(youtubeItems == null || youtubeItems.size() == 0 || !isYoutubeItemsUpToDate) {
			Log.d(TAG, "getting youtube items from db");
			youtubeItems = YoutubeItemORM.getMemberYoutubeItems(getContext(), getMember().getId());
			isYoutubeItemsUpToDate = true;
		}
		return youtubeItems;
	}
	
	//Update YoutubeItems
    public static void updateYoutubeItems(List<YoutubeItem> newYoutubeItems){

    	//ORM get latest videoid
    	YoutubeItem latestYoutubeItem = YoutubeItemORM.getMemberLatestYoutubeItem(getContext(), getMember().getId());
    	
    	
    	if(latestYoutubeItem == null) {
    		YoutubeItemORM.insertMemberYoutubeItems(getContext(), newYoutubeItems);
    		Log.d(TAG, "adding youtube items");
    		isYoutubeItemsUpToDate = false;
    	}else {
    		//Compare the first NewYoutubeItem video with the latest
    		if(!newYoutubeItems.get(0).getVideoId().equals(latestYoutubeItem.getVideoId())) {
    			YoutubeItemORM.updateMemberYoutubeItems(getContext(), newYoutubeItems);
    			isYoutubeItemsUpToDate = false;
    			Log.d(TAG, "updating youtube items");
    		} else {
    			Log.d(TAG, "youtube items already up to date");
    			isYoutubeItemsUpToDate = true;
    		}
    	}

    	//MemberORM.updateMembers(getContext(), members);
    	//isMindcrackersUpToDate = false;
    	//isAllMembersUpToDate = false;
    }
    
	
    
	//Update single Member from Members
    public static void updateCurrentMember(){
    	MemberORM.updateMember(getContext(), getMember());
    	isAllMembersUpToDate = false;
    }
    
	//Update all Members from MamnageMember
    public static void updateMembers(List<Member> members){

    	for(int i = 0; i < members.size(); i++) {
    		members.get(i).setSort(i);
    		Log.d("Data",members.get(i).toDatabaseString());
    	}
    	
    	MemberORM.updateMembers(getContext(), members);
    	isMindcrackersUpToDate = false;
    	isAllMembersUpToDate = false;
    }
	

}
