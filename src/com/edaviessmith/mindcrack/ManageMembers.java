package com.edaviessmith.mindcrack;


import java.util.ArrayList;
import java.util.List;


import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.edaviessmith.mindcrack.YoutubeFragment.YoutubeAdapter.YoutubeHolder;
import com.edaviessmith.mindcrack.data.Member;
import com.edaviessmith.mindcrack.data.YoutubeItem;
import com.edaviessmith.mindcrack.db.MemberORM;
import com.edaviessmith.mindcrack.util.MemberStateToggleButtons;
import com.edaviessmith.mindcrack.util.ResizableImageView;
import com.edaviessmith.mindcrack.util.ToggleButton;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.RemoveListener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;



public class ManageMembers  extends SherlockListActivity {

	
	private Context context;
	private Button save;
	private MemberAdapter adapter;
	
	private List<Member> manageMembers;
	
	
    private DragSortListView mDslv;
    private DragSortController mController;

    public int dragStartMode = DragSortController.ON_DOWN;
    public boolean removeEnabled = false;
    public int removeMode = DragSortController.FLING_REMOVE;
    public boolean sortEnabled = true;
    public boolean dragEnabled = true;
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manage_members);
		// Show the Up button in the action bar.
		getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);
		context = getApplicationContext();
		
		save = (Button) findViewById(R.id.save);
		save.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				save.setBackground(getResources().getDrawable(R.drawable.button_section_shape_pressed));
				AppInstance.updateMembers(adapter.data);
				NavUtils.navigateUpFromSameTask(ManageMembers.this);
			}
		});
		save.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				
				switch(event.getAction()){
			    case MotionEvent.ACTION_CANCEL:
			    case MotionEvent.ACTION_UP:
			    		save.setBackground(getResources().getDrawable(R.drawable.button_section_shape));
			        break;
			    case MotionEvent.ACTION_DOWN:
			    		save.setBackground(getResources().getDrawable(R.drawable.button_section_shape_pressed));
			        break;
			    }
				
			    return false;
			}
		});
		
		manageMembers = new ArrayList<Member>();
		manageMembers.addAll(AppInstance.getAllMembers());
		
		adapter = new MemberAdapter(this, R.layout.member_draggable_item, manageMembers);	
		
        LayoutInflater infalter = getLayoutInflater();
        ViewGroup header = (ViewGroup) infalter.inflate(R.layout.draggable_legend, getListView(), false);        
        getListView().addHeaderView(header, null, false);
        
        setListAdapter((ListAdapter) adapter);
        
        mDslv = (DragSortListView) getListView(); 
        mController = buildController(mDslv);
        
        mDslv.setDropListener(onDrop);
        //mDslv.setRemoveListener(onRemove);
        mDslv.setFloatViewManager(mController);
        mDslv.setOnTouchListener(mController);
        mDslv.setDragEnabled(dragEnabled);

	}
	
	
	private DragSortListView.DropListener onDrop = new DragSortListView.DropListener() {
        @Override
        public void drop(int from, int to) {
            if (from != to) {
                DragSortListView list = (DragSortListView)getListView();
                Member item = adapter.getItem(from);                
                adapter.remove(item);
                adapter.insert(item, to);
                list.moveCheckState(from, to);
            }
        }
    };

    
	        
    
    public DragSortController buildController(DragSortListView dslv) {
        // defaults are
        //   dragStartMode = onDown
        //   removeMode = flingRight
        DragSortController controller = new DragSortController(dslv);
        controller.setDragHandleId(R.id.drag_handle);
        controller.setClickRemoveId(R.id.click_remove);
        controller.setRemoveEnabled(removeEnabled);
        controller.setSortEnabled(sortEnabled);
        controller.setDragInitMode(dragStartMode);
        controller.setRemoveMode(removeMode);
        return controller;
    }

    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
	    case R.id.about_button:
	    	Intent intent = new Intent(this, About.class);
	    	startActivity(intent);
            return true;
		
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	protected class MemberAdapter extends ArrayAdapter<Member> {

	    Context context; 
	    int layoutResourceId;    
	    List<Member> data = new ArrayList<Member>();
	    
	    public MemberAdapter(Context context, int layoutResourceId, List<Member> data) {
	        super(context, layoutResourceId, data);
	        this.layoutResourceId = layoutResourceId;
	        this.context = context;
	        this.data = data;
	        
	    }
	    
	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {	    	
	        View row = convertView;
	        MemberHolder holder = null;
	        
	        if(row == null)
	        {
	            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
	            row = inflater.inflate(layoutResourceId, parent, false);
	            
	            holder = new MemberHolder();
	            holder.itemView = (RelativeLayout) row.findViewById(R.id.member_list_item);
	            holder.iconImage = (ImageView) row.findViewById(R.id.member_icon);
	            holder.name = (TextView) row.findViewById(R.id.member_name);
	            holder.state = (MemberStateToggleButtons) row.findViewById(R.id.member_state);
	            row.setTag(holder);
	        }
	        else
	        {
	            holder = (MemberHolder)row.getTag();
	        }
	        
	        
	        final Member item = data.get(position);
	        holder.iconImage.setImageResource(item.getImage());
	        holder.name.setText(item.getName());
	        
	        holder.state.setOnValueChangedListener(null);
	        holder.state.setValue(item.getStatus());
	        
	        holder.state.setOnValueChangedListener(new ToggleButton.OnValueChangedListener() {

				@Override
				public void onValueChanged(int value) {
					item.setStatus(value);
				}
			});
	        
	        return row;
	    }
	    
	    class MemberHolder
	    {
	    	RelativeLayout itemView;
	        ImageView iconImage;
	        TextView name;	
	        MemberStateToggleButtons state;
	    }
	}
	
	
	
}
