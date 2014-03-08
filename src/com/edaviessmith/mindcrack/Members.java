package com.edaviessmith.mindcrack;


import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.bugsense.trace.BugSenseHandler;
import com.edaviessmith.mindcrack.data.Member;
import com.edaviessmith.mindcrack.db.MemberORM;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItem;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.TabPageIndicator;




public class Members extends SlidingFragmentActivity {
	 public static String TAG = "Members";
	private static final String[] TAB_TITLES = new String[] { "Youtube", "Twitter" };
	
	TestFragmentAdapter mAdapter;
    ViewPager mPager;
    PageIndicator mIndicator;
    Context context;
    
    RelativeLayout progressBarRelativeLayout;

    ListView member_list;
    MembersAdapter adapter;
    

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.members);
        setBehindContentView(R.layout.members);
        context = getApplicationContext();
        
        //BugSenseHandler.initAndStartSession(context, Constants.BUGSENSE_KEY);

        setSlidingActionBarEnabled(true);
        
        
        
        mAdapter = new TestFragmentAdapter(getSupportFragmentManager());
        refreshFragment();
        

        //TODO need to check if intent has member_id (only if other extras are used in intent)
        setMemberFromIntent(getIntent());
        
        
        Log.d("Members","prefs set to "+AppInstance.getMember().name);

        //not in library (TODO:  investigate) [set drawer icon]
        //getSlidingMenu().setActionBarSlideIcon(new ActionBarSlideIcon(
        //		this, R.drawable.ic_drawer, R.string.app_name, R.string.app_name));
        getSlidingMenu().setMode(SlidingMenu.LEFT);
        getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        getSlidingMenu().setShadowWidth(25);
        getSlidingMenu().setFadeDegree(0.0f);        
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, getResources().getDisplayMetrics());  
        getSlidingMenu().setBehindWidth((int)px);//400       
        
        
        View menuView = View.inflate(context, R.layout.member_list, null);
        getSlidingMenu().setMenu(menuView);         

        adapter = new MembersAdapter(this);
        progressBarRelativeLayout = (RelativeLayout) menuView.findViewById(R.id.progress_bar_relative_layout);
        
        //Create memberList as AsyncTask
        new MemberList(this, menuView).execute();


        refreshActionbar();

        Util.startAlarm();

        //BugSenseHandler.addCrashExtraData("Members", "Initialized and Alarm Set");

	
	}
	
	
	static class MemberList extends AsyncTask<Void, Void, Void> {

	    private List<Members> memberItems;
	    private View menuView;
	    private Members members;
	    
	    public MemberList(Members members, View menuView) {
	    	this.members = members;
	    	this.menuView = menuView;
	    }
	    
	    protected void onPreExecute() { 
	    	
	    }

	    @Override
	    protected Void doInBackground(Void ...params) {	
	    		members.adapter.setData(AppInstance.getMindcrackers());    
			return null;
	    }  


	    protected void onPostExecute(Void v) {	
	    	members.updateList(menuView);
	    }
 
	}
	
	private void updateList(View menuView) {
		member_list = (ListView) menuView.findViewById(R.id.member_listview);
    	member_list.setAdapter(adapter);
    	member_list.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	view.setSelected(true);
	            Member member = (Member) member_list.getItemAtPosition(position);
	            AppInstance.setMember(member.getId());
	           
	            refreshActionbar();
	            refreshFragment();
	            getSherlock().dispatchInvalidateOptionsMenu();
	            getSlidingMenu().toggle(true);
	            
	            adapter.notifyDataSetChanged();	            
            }
        });
    	
    	
    	if(getSlidingMenu().isMenuShowing()) {
	    	Animation slideHiddenAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_up);
	    	progressBarRelativeLayout.setVisibility(View.VISIBLE);
	    	progressBarRelativeLayout.startAnimation(slideHiddenAnimation);
    	} else {
    		progressBarRelativeLayout.setVisibility(View.GONE);
    	}
	}
	
	protected void onFragmentResume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStop() {
		super.onStop();
		
		//BugSenseHandler.closeSession(Members.this);
	    //EasyTracker.getInstance(this).activityStop(this);  // Google analytics
	}
	
	@Override
	public void onResumeFragments() {
		//super.onResume();

		
	}

	protected void toggle_favorite() {
		
		Member member = AppInstance.getMember();
		Integer id = Integer.valueOf(member.getId());
		
		if(member.getStatus() == Constants.VISIBLE ) {
			member.setStatus(Constants.FAVORITE);
		} else if(member.getStatus() == Constants.FAVORITE ) {
			member.setStatus(Constants.VISIBLE);
		}
		
		AppInstance.updateCurrentMember();
		
		//member_list.invalidate();
		getSherlock().dispatchInvalidateOptionsMenu();
	}


	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		
		boolean fav = AppInstance.getMember().status == Constants.FAVORITE;
		MenuItem star = (MenuItem)menu.findItem(R.id.favorite_button);
		if(fav)
			star.setIcon(R.drawable.ic_action_important);
		else
			star.setIcon(R.drawable.ic_action_not_important);
		Log.d("Menu", "Star is on: "+fav);
		
		return true;
		
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		//TODO fix (now db)
		//AppInstance.setMemberStates();
	}
	
	protected void onNewIntent (Intent intent){
		setMemberFromIntent(intent);
        refreshActionbar();
        refreshFragment();
        getSherlock().dispatchInvalidateOptionsMenu();
	}

	
	private void setMemberFromIntent(Intent intent) {	
		if(intent != null && intent.hasExtra(Constants.PREF_MEMBER)){
      	  	int member_id = intent.getIntExtra(Constants.PREF_MEMBER, 0);
      	  	AppInstance.setMember(member_id);
      }
        
	}
	

	private void refreshActionbar() {
		//ImageView view = (ImageView)findViewById(android.R.id.home);
		//view.setPadding(10, 10, 10, 10);
		
		
		getSherlock().getActionBar().setHomeButtonEnabled(true);
    	getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);
    	getSherlock().getActionBar().setTitle(AppInstance.getMember().getName());
    	getSherlock().getActionBar().setIcon(AppInstance.getMember().getIcon());        
	}
	
	
	private void refreshFragment() {       
        mPager = (ViewPager)findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
 
        mIndicator = (TabPageIndicator)findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);  
        BugSenseHandler.addCrashExtraData("FragmentAdapter", "Set");
	}
	
	
	protected class MembersAdapter extends BaseAdapter {

	    Context context; 
	    int layoutResourceId;    
	    List<Member> data = new ArrayList<Member>();
	    
	    private LayoutInflater mInflater;
	    
	    public MembersAdapter(Context context) {
	        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
	    }
	    
	    public void setData(List<Member> data) {
	    	this.data.addAll(data);
	    	notifyDataSetChanged();
	    }
	    
	    public void add(Member item)
	    {
	    	//Add progress bar, then items before it
	    	//int index = getCount() > 1? getCount() - 1 : (getCount() > 0? 0: 1);
	        //youtubeItemList.add(index, item);
	        notifyDataSetChanged();
	        
	    }
	    
	    @Override
        public int getItemViewType(int position) {
            return data.get(position).getStatus();
        }
 
        @Override
        public int getViewTypeCount() {
            return 2;
        }
	    
	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {	    	
	        MemberHolder holder = null;
	        int type = getItemViewType(position);

            if (convertView == null) {
                holder = new MemberHolder();
               /* switch (type) {
                    case Constants.VISIBLE:
                        convertView = mInflater.inflate(R.layout.member_item, null);
                        break;
                        
                    case Constants.FAVORITE:
                        convertView = mInflater.inflate(R.layout.member_item_favorite, null);
                        break;
                }*/
                convertView = mInflater.inflate(R.layout.member_item, null);
                if(type != Constants.HIDDEN) {
                	
	                holder.itemView = (LinearLayout) convertView.findViewById(R.id.member_list_item);
		            holder.memberIcon = (ImageView) convertView.findViewById(R.id.member_icon);
		            holder.mamberName = (TextView) convertView.findViewById(R.id.member_name);
		            
		            convertView.setTag(holder);
                }
                
            } else {
                holder = (MemberHolder)convertView.getTag();
            }
	        
	        final Member item = data.get(position);
	        
	        if(type != Constants.HIDDEN) {
	            holder.memberIcon.setImageResource(item.getImage());
	            holder.mamberName.setText(item.getName());
	            
	            //Set the background color for member status
	            if(type == Constants.FAVORITE) {
	            	if(item.getId() == AppInstance.getMember().getId()) {
	            		holder.itemView.setBackgroundResource(R.drawable.member_item_fav_selected);
	            	}else {
	            		holder.itemView.setBackgroundResource(R.color.favorite);
	            	}
	            } else if(item.getId() == AppInstance.getMember().getId()) {
	            	holder.itemView.setBackgroundResource(R.drawable.member_item_selected);
	            } else {
	            	holder.itemView.setBackgroundResource(R.color.dark_grey);
	            }
	        }
            
	        return convertView;
	    }
	    
	    class MemberHolder
	    {
	    	LinearLayout itemView;
	        ImageView memberIcon;
	        TextView mamberName;
	    }

	    @Override
        public int getCount() {
            return data.size();
        }
 
        @Override
        public Member getItem(int position) {
            return data.get(position);
        }
 
        @Override
        public long getItemId(int position) {
        	//return position;
            return data.get(position).getId();
        }
 
	}
	
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       getSupportMenuInflater().inflate(R.menu.members, menu);

       return super.onCreateOptionsMenu(menu);
    }
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case android.R.id.home:
        	getSlidingMenu().toggle();
            return true;
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
	
	 class TestFragmentAdapter extends FragmentStatePagerAdapter {     
	     private int mCount = TAB_TITLES.length;
	     //private YoutubeFragment youtubeFragment = null;
	     
	     public TestFragmentAdapter(FragmentManager fm) {
	         super(fm);
	         if (context == null) context = getApplicationContext();
	         //BugSenseHandler.addCrashExtraData("FragmentAdapter", "Initialized");
	     }
	 
	     @Override
	     public Fragment getItem(int position) {
	    	 switch (position) {
	    	 	case 0:
	    	 		//Log.e(TAG, "youtube fragment new instance" + (youtubeFragment == null));
	    	 		//youtubeFragment = YoutubeFragment.newInstance();
	    	 		return  YoutubeFragment.newInstance();//youtubeFragment;
	    	 	case 1:
	    	 		return TwitterFragment.newInstance(AppInstance.getMember());
	    	 }
	    	 
	         return TestFragment.newInstance(String.valueOf(position)); //nothing
	     }	     

		@Override
	     public int getCount() {
	         return mCount;
	     }
	      
	     @Override
	     public CharSequence getPageTitle(int position) {
	      return TAB_TITLES[position];
	     }
	 }
		
    

}
