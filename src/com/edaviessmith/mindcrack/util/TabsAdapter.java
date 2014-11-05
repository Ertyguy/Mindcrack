package com.edaviessmith.mindcrack.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabWidget;

import com.edaviessmith.mindcrack.Members;

public class TabsAdapter extends FragmentPagerAdapter implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {
	private final Context mContext;
	private final TabHost mTabHost;
	private final ViewPager mViewPager;
	
	private ArrayList<String> mTabTags = new ArrayList<String>();
	private HashMap<String, Stack<TabInfo>> mTabStackMap = new HashMap<String, Stack<TabInfo>>();
	
	static final class TabInfo {
	    public final String tag;
	    public final Class<?> clss;
	    public Bundle args;
	
	    TabInfo(String _tag, Class<?> _class, Bundle _args) {
	        tag = _tag;
	        clss = _class;
	        args = _args;
	    }
	}
	
	static class DummyTabFactory implements TabHost.TabContentFactory {
	    private final Context mContext;
	
	    public DummyTabFactory(Context context) {
	        mContext = context;
	    }
	
	    @Override
	    public View createTabContent(String tag) {
	        View v = new View(mContext);
	        v.setMinimumWidth(0);
	        v.setMinimumHeight(0);
	        return v;
	    }
	}
	
	public interface SaveStateBundle{
	    public Bundle onRemoveFragment(Bundle outState);
	}
	
	public TabsAdapter(Members activity, TabHost tabHost, ViewPager pager) {
	    super(activity.getSupportFragmentManager());
	    mContext = activity;
	    mTabHost = tabHost;
	    mViewPager = pager;
	    mTabHost.setOnTabChangedListener(this);
	    mViewPager.setAdapter(this);
	    mViewPager.setOnPageChangeListener(this);
	}
	
	/**
	 * Add a Tab which will have Fragment Stack. Add Fragments on this Stack by using
	 * addFragment(FragmentManager fm, String _tag, Class<?> _class, Bundle _args)
	 * The Stack will hold always the default Fragment u add here.
	 * 
	 * DON'T ADD Tabs with same tag, it's not beeing checked and results in unexpected
	 * beahvior.
	 * 
	 * @param tabSpec
	 * @param clss
	 * @param args
	 */
	public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args){
	    Stack<TabInfo> tabStack = new Stack<TabInfo>();
	
	    tabSpec.setContent(new DummyTabFactory(mContext));
	    mTabHost.addTab(tabSpec);
	    String tag = tabSpec.getTag();
	    TabInfo info = new TabInfo(tag, clss, args);
	
	    mTabTags.add(tag);                  // to know the position of the tab tag 
	    tabStack.add(info);
	    mTabStackMap.put(tag, tabStack);
	    notifyDataSetChanged();
	}
	
	/**
	 * Will add the Fragment to Tab with the Tag _tag. Provide the Class of the Fragment
	 * it will be instantiated by this object. Proivde _args for your Fragment.
	 * 
	 * @param fm
	 * @param _tag
	 * @param _class
	 * @param _args
	 */
	public void addFragment(FragmentManager fm, String _tag, Class<?> _class, Bundle _args){
	    TabInfo info = new TabInfo(_tag, _class, _args);
	    Stack<TabInfo> tabStack = mTabStackMap.get(_tag);   
	    Fragment frag = fm.findFragmentByTag("android:switcher:" + mViewPager.getId() + ":" + mTabTags.indexOf(_tag));
	    if(frag instanceof SaveStateBundle){
	        Bundle b = new Bundle();
	        ((SaveStateBundle) frag).onRemoveFragment(b);
	        tabStack.peek().args = b;
	    }
	    tabStack.add(info);
	    FragmentTransaction ft = fm.beginTransaction();
	    ft.remove(frag).commit();
	    notifyDataSetChanged();
	}
	
	/**
	 * Will pop the Fragment added to the Tab with the Tag _tag
	 * 
	 * @param fm
	 * @param _tag
	 * @return
	 */
	public boolean popFragment(FragmentManager fm, String _tag){
	    Stack<TabInfo> tabStack = mTabStackMap.get(_tag);   
	    if(tabStack.size()>1){
	        tabStack.pop();
	        Fragment frag = fm.findFragmentByTag("android:switcher:" + mViewPager.getId() + ":" + mTabTags.indexOf(_tag));
	        FragmentTransaction ft = fm.beginTransaction();
	        ft.remove(frag).commit();
	        notifyDataSetChanged();
	        return true;
	    }
	    return false;
	}
	
	public boolean back(FragmentManager fm) {
	    int position = mViewPager.getCurrentItem();
	    return popFragment(fm, mTabTags.get(position));
	}
	
	@Override
	public int getCount() {
	    return mTabStackMap.size();
	}
	
	@Override
	public int getItemPosition(Object object) {
	    ArrayList<Class<?>> positionNoneHack = new ArrayList<Class<?>>();
	    for(Stack<TabInfo> tabStack: mTabStackMap.values()){
	        positionNoneHack.add(tabStack.peek().clss);
	    }   // if the object class lies on top of our stacks, we return default
	    if(positionNoneHack.contains(object.getClass())){
	        return POSITION_UNCHANGED;
	    }
	    return POSITION_NONE;
	}
	
	@Override
	public Fragment getItem(int position) {
	    Stack<TabInfo> tabStack = mTabStackMap.get(mTabTags.get(position));
	    TabInfo info = tabStack.peek();
	    return Fragment.instantiate(mContext, info.clss.getName(), info.args);
	}
	
	@Override
	public void onTabChanged(String tabId) {
	    int position = mTabHost.getCurrentTab();
	    mViewPager.setCurrentItem(position);
	}
	
	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
	}
	
	@Override
	public void onPageSelected(int position) {
	    // Unfortunately when TabHost changes the current tab, it kindly
	    // also takes care of putting focus on it when not in touch mode.
	    // The jerk.
	    // This hack tries to prevent this from pulling focus out of our
	    // ViewPager.
	    TabWidget widget = mTabHost.getTabWidget();
	    int oldFocusability = widget.getDescendantFocusability();
	    widget.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
	    mTabHost.setCurrentTab(position);
	    widget.setDescendantFocusability(oldFocusability);
	}
	
	@Override
	public void onPageScrollStateChanged(int state) {
	}
	
}