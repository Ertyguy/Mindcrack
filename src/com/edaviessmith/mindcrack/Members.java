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
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
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
import com.edaviessmith.mindcrack.data.Member;
import com.edaviessmith.mindcrack.util.SlidingTabLayout;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
//import com.viewpagerindicator.PageIndicator;
//import com.viewpagerindicator.TabPageIndicator;




public class Members extends SlidingFragmentActivity {
	public static String TAG = "Members";
	
	Context context;
	FragmentPagerAdapter fragmentPagerAdapter;
    ViewPager viewPager;
    //PageIndicator pageIndicator;
    SlidingTabLayout pagerTitleStrip;
    
    //boolean isReddit;
    
    RelativeLayout progressBarRelativeLayout;
    ListView member_list;
    MembersAdapter adapter;
    LinearLayout reddit_list_item;
    

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.members);
        setBehindContentView(R.layout.members);
        context = getApplicationContext();
        
        setSlidingActionBarEnabled(true);
        //isReddit = false;
        fragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager());
        refreshFragment();
        
        
        //TODO need to check if intent has member_id (only if other extras are used in intent)
        setMemberFromIntent(getIntent());
        
        
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
        reddit_list_item = (LinearLayout) menuView.findViewById(R.id.reddit_list_item);
        reddit_list_item.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AppInstance.setMember(-1);
				
				refreshActionbar();
	            refreshFragment();
	            getSherlock().dispatchInvalidateOptionsMenu();
	            getSlidingMenu().toggle(true);
	            
	            adapter.notifyDataSetChanged();
			}
		});
        
        
        //Create memberList as AsyncTask
        new MemberList(this, menuView).execute();

        refreshActionbar();
        Util.startAlarm();	
	}
	
	
	static class MemberList extends AsyncTask<Void, Void, Void> {
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
	

	@Override
	public void onStop() {
		super.onStop();
		
		//BugSenseHandler.closeSession(Members.this);
	}
	

	protected void toggle_favorite() {
		
		Member member = AppInstance.getMember();
		
		if(member.getStatus() == Constants.VISIBLE) {
			member.setStatus(Constants.FAVORITE);
		} else if(member.getStatus() == Constants.FAVORITE) {
			member.setStatus(Constants.VISIBLE);
		}
		
		AppInstance.updateCurrentMember();		
		getSherlock().dispatchInvalidateOptionsMenu();
		adapter.notifyDataSetChanged();
	}


	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if(AppInstance.getMember() != null) {
			boolean fav = AppInstance.getMember().status == Constants.FAVORITE;
			MenuItem star = (MenuItem)menu.findItem(R.id.favorite_button);
			if(fav)
				star.setIcon(R.drawable.ic_action_important);
			else
				star.setIcon(R.drawable.ic_action_not_important);
		}
		return true;
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
		getSherlock().getActionBar().setHomeButtonEnabled(true);
    	getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);
    	if(AppInstance.getMember() != null) {
	    	getSherlock().getActionBar().setTitle(AppInstance.getMember().getName());
	    	getSherlock().getActionBar().setIcon(AppInstance.getMember().getIcon());
    	} else {
    		getSherlock().getActionBar().setTitle("Reddit");
	    	getSherlock().getActionBar().setIcon(R.drawable.redditmindcrack);
    	}
	}
	
	
	private void refreshFragment() {  		
        viewPager = (ViewPager)findViewById(R.id.pager);
        viewPager.setAdapter(fragmentPagerAdapter);
		viewPager.getAdapter().notifyDataSetChanged();
        
        pagerTitleStrip = (SlidingTabLayout)findViewById(R.id.pager_tab_strip);
        pagerTitleStrip.setViewPager(viewPager);
        
        //pageIndicator = (TabPageIndicator)findViewById(R.id.indicator);
        //pageIndicator.setViewPager(viewPager); 
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
	    
	    /*public void add(Member item)
	    {
	    	//Add progress bar, then items before it
	    	//int index = getCount() > 1? getCount() - 1 : (getCount() > 0? 0: 1);
	        //youtubeItemList.add(index, item);
	        notifyDataSetChanged();	        
	    }*/
	    
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
	            	if(AppInstance.getMember() != null && item.getId() == AppInstance.getMember().getId()) {
	            		holder.itemView.setBackgroundResource(R.drawable.member_item_fav_selected);
	            	}else {
	            		holder.itemView.setBackgroundResource(R.color.favorite);
	            	}
	            } else if(AppInstance.getMember() != null && item.getId() == AppInstance.getMember().getId()) {
	            	holder.itemView.setBackgroundResource(R.drawable.member_item_selected);
	            } else {
	            	holder.itemView.setBackgroundResource(R.color.dark_grey);
	            }
	        }
            
	        return convertView;
	    }
	    
	    class MemberHolder {
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
	
	class FragmentPagerAdapter extends FragmentStatePagerAdapter {
		private final String[] REDDIT_TABS = new String[] { "Reddit", "Post" };
		private final String[] MEMBER_TABS = new String[] { "Youtube", "Twitter" };
		
	    private int mCount = MEMBER_TABS.length;
	    
	    public FragmentPagerAdapter(FragmentManager fm) {
	        super(fm);	
	        
	    }
	 
	    @Override
	    public Fragment getItem(int position) {
	    	if(AppInstance.getMember() != null) {
		    	switch (position) {
		    		case 0:
		    	 		return  YoutubeFragment.newInstance();
		    	 	case 1:
		    	 		return TwitterFragment.newInstance();
		    	}
	    	} else {
	    		switch (position) {
	    		case 0:
	    	 		return  RedditFragment.newInstance();
	    		}
	    	}
	    	
	    	 
	        return TestFragment.newInstance(String.valueOf(position)); //This should never happen
	    }	     

		@Override
	    public int getCount() {
	        return mCount;
	    }
	    
	    @Override
	    public CharSequence getPageTitle(int position) {
	    	if(AppInstance.getMember() != null) {
	    		return MEMBER_TABS[position];
	    	} else {
	    		return REDDIT_TABS[position];
	    	}
	    }
	}
		
    

}
