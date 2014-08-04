package com.edaviessmith.mindcrack;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.edaviessmith.mindcrack.data.Member;
import com.edaviessmith.mindcrack.data.Post;
import com.edaviessmith.mindcrack.data.Tweet;
import com.edaviessmith.mindcrack.data.YoutubeItem;
import com.edaviessmith.mindcrack.db.MemberORM;
import com.edaviessmith.mindcrack.db.RedditORM;
import com.edaviessmith.mindcrack.db.TwitterORM;
import com.edaviessmith.mindcrack.db.YoutubeItemORM;
import com.edaviessmith.mindcrack.R;
import com.edaviessmith.mindcrack.util.SlidingTabLayout;


public class Members extends ActionBarActivity {
	public static String TAG = "Members";

	FragPagerAdapter fragmentPagerAdapter;
    ViewPager viewPager;
    //PageIndicator pageIndicator;
    SlidingTabLayout pagerTitleStrip;
    
    //boolean isReddit;
    
    RelativeLayout progressBarRelativeLayout;
    RelativeLayout left_drawer;
    ListView member_list;
    MembersAdapter adapter;
    LinearLayout reddit_list_item;
    
    MenuItem star;
    
    private DrawerLayout navDrawerLayout;
    private ActionBarDrawerToggle navDrawerToggle;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.social_media);

        //TODO need to check if intent has member_id (only if other extras are used in intent)
        setMemberFromIntent(getIntent());
        
        
        // D R A W E R //
        
        navDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navDrawerToggle = new ActionBarDrawerToggle(this, navDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                refreshActionbar();
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("Mindcrack");
                getSupportActionBar().setIcon(R.drawable.ic_launcher);
            }
        };

        // Set the drawer toggle as the DrawerListener
        navDrawerLayout.setDrawerListener(navDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        navDrawerToggle.syncState();
        
        left_drawer = (RelativeLayout) findViewById(R.id.left_drawer);
        adapter = new MembersAdapter(this);
        adapter.setData(getMindcrackers());
        
        member_list = (ListView) findViewById(R.id.member_listview);
        member_list.setAdapter(adapter);
    	member_list.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	//view.setSelected(true);
	            Member member = (Member) member_list.getItemAtPosition(position);
	            setMember(member.getId());
	            
	            fragmentPagerAdapter.update();
				fragmentPagerAdapter.notifyDataSetChanged();
				navDrawerLayout.closeDrawer(left_drawer);
            }
        });
    	
    	
        reddit_list_item = (LinearLayout) findViewById(R.id.reddit_list_item);
        reddit_list_item.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent redditIntent = new Intent(Members.this, Reddit.class);
		    	startActivity(redditIntent);
		    	
				/*setMember(-1);
				fragmentPagerAdapter.update();
				fragmentPagerAdapter.notifyDataSetChanged();
				navDrawerLayout.closeDrawer(left_drawer);*/
			}
		});
        
    	
        
        // F R A G M E N T   P A G E R //
        
        //isReddit = false;
        fragmentPagerAdapter = new FragPagerAdapter(getSupportFragmentManager(), this);
        
        viewPager = (ViewPager)findViewById(R.id.pager);
        viewPager.setAdapter(fragmentPagerAdapter);
		viewPager.getAdapter().notifyDataSetChanged();
        
        pagerTitleStrip = (SlidingTabLayout)findViewById(R.id.pager_tab_strip);
        pagerTitleStrip.setViewPager(viewPager);
        
        
        
        refreshActionbar();
        Util.startAlarm();	
	}
	
	
	@Override
	public void onStop() {
		super.onStop();
		
		//BugSenseHandler.closeSession(Members.this);
	}
	
		@Override
	public void onResume() {
		super.onResume();
		fragmentPagerAdapter.update();
	}
	

	protected void toggle_favorite() {
		Member member = getMember();
		if(member.getStatus() == Constants.VISIBLE) {
			member.setStatus(Constants.FAVORITE);
			star.setIcon(R.drawable.ic_action_important);
			
		} else if(member.getStatus() == Constants.FAVORITE) {
			member.setStatus(Constants.VISIBLE);
			star.setIcon(R.drawable.ic_action_not_important);
		}
		
		updateCurrentMember();
	}


	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.members, menu);
		
		if(getMember() != null) {
			boolean fav = getMember().status == Constants.FAVORITE;
			star = (MenuItem)menu.findItem(R.id.favorite_button);
			if(fav)
				star.setIcon(R.drawable.ic_action_important);
			else
				star.setIcon(R.drawable.ic_action_not_important);
		}
		
	    return super.onCreateOptionsMenu(menu);
	}

	protected void onNewIntent (Intent intent){
		setMemberFromIntent(intent);
	}

	
	private void setMemberFromIntent(Intent intent) {	
		if(intent != null && intent.hasExtra(Constants.PREF_MEMBER)){
			int member_id = intent.getIntExtra(Constants.PREF_MEMBER, 0);
			setMember(member_id);
		}        
	}
	

	private void refreshActionbar() {
    	if(getMember() != null) {
	    	getSupportActionBar().setTitle(getMember().getName());
	    	getSupportActionBar().setIcon(getMember().getIcon());
	    	if(star != null) {
				if(getMember().status == Constants.FAVORITE)
					star.setIcon(R.drawable.ic_action_important);
				else
					star.setIcon(R.drawable.ic_action_not_important);
	    	}
    	} else {
    		getSupportActionBar().setTitle("Reddit");
    		getSupportActionBar().setIcon(R.drawable.redditmindcrack);
    	}
	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		if (navDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
		
	    switch (item.getItemId()) {
		    case R.id.favorite_button:
	        	toggle_favorite();        	
	            return true;
		    case R.id.settings_button:
		    	Intent settingsIntent = new Intent(Members.this, Settings.class);
		    	startActivity(settingsIntent);
	            return true; 
		    case R.id.manage_button:
		    	Intent manageIntent = new Intent(Members.this, ManageMembers.class);
		    	startActivity(manageIntent);
	            return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
	
	
	
	
	////////  H E L P E R S /////////
	
	
	//Members Activity
    public Member currentMember;
    public List<Member> mindcrackers; //Sidemenu
    public List<Member> allMembers; //Manage members (any status)
	

	//Fragment Variables
	public int currentYoutubeMemberId = -1;		
	public List<YoutubeItem> youtubeItems;
	public String playlistPageToken = "";
	public boolean isYoutubeItemsUpToDate;
	
	public int currentTwitterMemberId = -1;
	public List<Tweet> twitterFeed;
	public int twitterPageToken;
	public boolean isTwitterFeedUpToDate;
	
	public List<Post> redditFeed;
	public String redditPageToken;
	public boolean isRedditFeedUpToDate;
	
	public String redditPost;
	
	//Update booleans
	public boolean isMindcrackersUpToDate;
    public boolean isAllMembersUpToDate;
    


    
	// MEMBERS
	public List<Member> getMindcrackers() {
		if(mindcrackers == null || mindcrackers.size() == 0 || !isMindcrackersUpToDate) {
			mindcrackers = MemberORM.getVisibleMembers(this);
			isMindcrackersUpToDate = true;
			
			Log.d("APP","member list changed to up to date");
		}
		return mindcrackers;
	}
	
	public List<Member> getAllMembers() {
		if(allMembers == null || allMembers.size() == 0 || !isAllMembersUpToDate) {
			allMembers = MemberORM.getMembers(this);
			isAllMembersUpToDate = true;
			
			Log.d("APP","all list changed to up to date");
		}
		return allMembers;
	}
	
	
	public Member getMember() {
		if(currentMember == null) {
			SharedPreferences settings = this.getSharedPreferences(Constants.PREFS, 0);
		    int memberId = settings.getInt(Constants.PREF_MEMBER, -1);
		    //none set so make it random :D
		    if(memberId == -1) {
		    	currentMember = null;
		    	//Random rnd = new Random();
		    	//memberId = rnd.nextInt(getMindcrackers().size());
		    } else {
			    for(Member member : getMindcrackers()) {
		        	if(member.getId() == memberId)
		        		currentMember = member;
		        }
		    }
		    //currentMember = getMindcrackers().get(memberId);
		}
		return currentMember;
	}
	
	@SuppressLint("NewApi")
	public void setMember(int memberId) {
		SharedPreferences.Editor settings = this.getSharedPreferences(Constants.PREFS, 0).edit();
        settings.putInt(Constants.PREF_MEMBER,  memberId);
        if (android.os.Build.VERSION.SDK_INT >= 9) {
        	settings.apply();
        } else {
        	settings.commit();
        }
        //Member currently set
        if(memberId == -1) {
        	currentMember = null;
        } else if(getMember() == null || memberId != getMember().getId()) {
	        for(Member member : getMindcrackers()) {
	        	if(member.getId() == memberId)
	        		currentMember = member;
	        }
	        isYoutubeItemsUpToDate = false;
	        isTwitterFeedUpToDate = false;
        }
	}
	
	//// YOUTUBE ////
    
	public int getCurrentYoutubeMemberId() {
    	if(currentYoutubeMemberId == -1) {
    		setCurrentYoutubeMemberId(getMember().getId());
    	}
		return currentYoutubeMemberId;
	}

	public void setCurrentYoutubeMemberId(int currentFragmentMemberId) {
		currentYoutubeMemberId = currentFragmentMemberId;
	}
	
    public boolean checkSetYoutubeMemberIdIsCurrent() {
    	if(getMember() == null) return false; 
    	boolean same = currentYoutubeMemberId == getMember().getId();
    	setCurrentYoutubeMemberId(getMember().getId());    	
    	return same;
	}
	
	public List<YoutubeItem> getYoutubeItems() {
		if(youtubeItems == null || youtubeItems.size() == 0 || !isYoutubeItemsUpToDate) {
			Log.d(TAG, "getting youtube items from db");
			youtubeItems = YoutubeItemORM.getMemberYoutubeItems(this, getMember().getId());
			isYoutubeItemsUpToDate = true;
		}
		return youtubeItems;
	}
	
	//Update YoutubeItems
    public void updateYoutubeItems(List<YoutubeItem> newYoutubeItems){

    	//ORM get latest videoid
    	YoutubeItem latestYoutubeItem = YoutubeItemORM.getMemberLatestYoutubeItem(this, getMember().getId());
    	
    	
    	if(latestYoutubeItem == null) {
    		YoutubeItemORM.insertMemberYoutubeItems(this, newYoutubeItems);
    		Log.d(TAG, "adding youtube items");
    		isYoutubeItemsUpToDate = false;
    	}else {
    		//Compare the first NewYoutubeItem video with the latest
    		if(!newYoutubeItems.get(0).getVideoId().equals(latestYoutubeItem.getVideoId())) {
    			YoutubeItemORM.updateMemberYoutubeItems(this, newYoutubeItems);
    			isYoutubeItemsUpToDate = false;
    			Log.d(TAG, "updating youtube items");
    		} else {
    			Log.d(TAG, "youtube items already up to date");
    			isYoutubeItemsUpToDate = true;
    		}
    	}
    }
    
    
    //// TWITTER ////
    
    
    public int getCurrentTwitterMemberId() {
    	if(currentTwitterMemberId == -1) {
    		setCurrentTwitterMemberId(getMember().getId());
    	}
		return currentTwitterMemberId;
	}

	public void setCurrentTwitterMemberId(int currentFragmentMemberId) {
		currentTwitterMemberId = currentFragmentMemberId;
	}
	
    public boolean checkSetTwitterMemberIdIsCurrent() {
    	boolean same = currentTwitterMemberId == getMember().getId();
    	setCurrentTwitterMemberId(getMember().getId());    	
    	return same;    	
	}
    
	public List<Tweet> getTwitterFeed() {
		if(twitterFeed == null || twitterFeed.size() == 0 || !isTwitterFeedUpToDate) {
			Log.d(TAG, "getting twitter feed from db");
			twitterFeed = TwitterORM.getMemberTwitterFeed(this, getMember().getId());
			isTwitterFeedUpToDate = true;
		}
		return twitterFeed;
	}
    
	public void updateTwitterFeed(List<Tweet> tweets) {
		//ORM get latest tweet
    	Tweet latestTweet = TwitterORM.getMemberLatestTwitterFeed(this, getMember().getId());
    	
    	
    	if(latestTweet == null) {
    		TwitterORM.insertMemberTwitterFeed(this, tweets);
    		Log.d(TAG, "adding youtube items");
    		isTwitterFeedUpToDate = false;
    	}else {
    		//Compare the first NewYoutubeItem video with the latest
    		if(tweets.get(0).getTweetId() != latestTweet.getTweetId()) {
    			TwitterORM.updateMemberTwitterFeed(this, tweets);
    			isTwitterFeedUpToDate = false;
    			Log.d(TAG, "updating twitter feed");
    		} else {
    			Log.d(TAG, "twitter feed already up to date");
    			isTwitterFeedUpToDate = true;
    		}
    	}
	}
	
	
	//// REDDIT ////
	
	//TODO go to database for reddit
	public List<Post> getRedditFeed() {
		if(redditFeed == null || redditFeed.size() == 0 || !isRedditFeedUpToDate) {
			Log.d(TAG, "getting reddit feed from db");
			redditFeed = RedditORM.getRedditFeed(this);
			isRedditFeedUpToDate = true;
		}
		return redditFeed;
	}
    
	public void updateRedditFeed(List<Post> posts) {
		//ORM get latest tweet
    	Post latestPost = RedditORM.getLatestRedditFeed(this);
    	
    	
    	if(latestPost == null) {
    		RedditORM.insertRedditFeed(this, posts);
    		Log.d(TAG, "adding youtube items");
    		isRedditFeedUpToDate = false;
    	}else {
    		//Compare the first NewYoutubeItem video with the latest
    		if(posts.get(0).getId() != latestPost.getId()) {
    			RedditORM.insertRedditFeed(this, posts);
    			isRedditFeedUpToDate = false;
    			Log.d(TAG, "updating reddit feed");
    		} else {
    			Log.d(TAG, "reddit feed already up to date");
    			isRedditFeedUpToDate = true;
    		}
    	}
	}
	
	
	//// MEMBER ////
	
	//Update single Member from Members
    public void updateCurrentMember(){
    	MemberORM.updateMember(this, getMember());
    	isAllMembersUpToDate = false;
    }
    
	//Update all Members from MamnageMember
    public void updateMembers(List<Member> members){

    	for(int i = 0; i < members.size(); i++) {
    		members.get(i).setSort(i);
    		Log.d("Data", members.get(i).toDatabaseString());
    	}
    	
    	MemberORM.updateMembers(this, members);
    	isMindcrackersUpToDate = false;
    	isAllMembersUpToDate = false;
    }

	
	
	
	
	class FragPagerAdapter extends FragmentStatePagerAdapter {
		private final String[] REDDIT_TABS = new String[] { "Reddit", "Post" };
		private final String[] MEMBER_TABS = new String[] { "Youtube", "Twitter" };
		
	    private int mCount = MEMBER_TABS.length;
	    private int pos;
	    YoutubeFragment youtubeFragment;
	    TwitterFragment twitterFragment;
	    RedditFragment redditFragment;
	    Members act;
	    
	    public FragPagerAdapter(FragmentManager fm, Members activity) {
	        super(fm);	
	        act = activity;
	    }
	    
	    @Override
	    public Fragment getItem(int position) {
	    	pos = position;
	    	Log.d("FragPagerAdapter","get item called: "+position);
	    	if(getMember() != null) {
		    	switch (position) {
		    		case 0:
		    	 		youtubeFragment = YoutubeFragment.newInstance(act);
		    	 		return youtubeFragment;
		    	 	case 1:
		    	 		twitterFragment = TwitterFragment.newInstance();
		    	 		return twitterFragment;
		    	}
	    	} else {
	    		switch (position) {
	    		case 0:
	    			redditFragment =  RedditFragment.newInstance();
	    			return redditFragment;
	    		}
	    	}
	    	
	    	 
	        return TestFragment.newInstance(String.valueOf(position)); //This should never happen
	    }

	    public void update() {
	    	Log.d("FragPagerAdapter","update called: "+pos);
	    	if(getMember() != null) {
	    		if(youtubeFragment != null) youtubeFragment.update(); else youtubeFragment = YoutubeFragment.newInstance(act);
	    		if(twitterFragment != null) twitterFragment.update(); else twitterFragment = TwitterFragment.newInstance();
		    	/*switch (pos) {
		    		case 0:
		    	 		youtubeFragment.update();
		    	 	case 1:
		    	 		twitterFragment.update();
		    	}*/
	    	} else {
	    		//switch (pos) {
	    		//case 0:
	    			if(redditFragment != null) redditFragment.update(); else redditFragment = RedditFragment.newInstance();
	    		//}
	    	}
	    }
	    
		@Override
	    public int getCount() {
	        return mCount;
	    }
	    
	    @Override
	    public CharSequence getPageTitle(int position) {
	    	if(getMember() != null) {
	    		return MEMBER_TABS[position];
	    	} else {
	    		return REDDIT_TABS[position];
	    	}
	    }
	    
	   // @Override public int getItemPosition (Object object) { return POSITION_NONE; }
	    
	    @Override
	    public void unregisterDataSetObserver(DataSetObserver observer) {
	        if (observer != null) {
	            super.unregisterDataSetObserver(observer);
	        }
	    }
	}
		
    

}
