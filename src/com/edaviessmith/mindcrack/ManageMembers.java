package com.edaviessmith.mindcrack;


import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.edaviessmith.mindcrack.data.Member;
import com.edaviessmith.mindcrack.R;
import com.edaviessmith.mindcrack.util.MemberStateToggleButtons;
import com.edaviessmith.mindcrack.util.ToggleButton;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;



public class ManageMembers  extends ActionBarActivity {

	//private static String TAG = "ManageMembers";
	
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
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		save = (Button) findViewById(R.id.save);
		save.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				save.setBackgroundResource(R.drawable.button_section_shape_pressed);
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
			    		save.setBackgroundResource(R.drawable.button_section_shape);
			        break;
			    case MotionEvent.ACTION_DOWN:
			    		save.setBackgroundResource(R.drawable.button_section_shape_pressed);
			        break;
			    }
				
			    return false;
			}
		});
		
		manageMembers = new ArrayList<Member>();
		manageMembers.addAll(AppInstance.getAllMembers());
		
		mDslv = (DragSortListView) findViewById(android.R.id.list);
		adapter = new MemberAdapter(this, R.layout.member_draggable_item, manageMembers);	
		
        LayoutInflater infalter = getLayoutInflater();
        ViewGroup header = (ViewGroup) infalter.inflate(R.layout.draggable_legend, mDslv, false);        
        mDslv.addHeaderView(header, null, false);
        
        mDslv.setAdapter((ListAdapter) adapter);
        
         
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
                //DragSortListView list = (DragSortListView)getListView();
                Member item = adapter.getItem(from);                
                adapter.remove(item);
                adapter.insert(item, to);
                mDslv.moveCheckState(from, to);
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
		getMenuInflater().inflate(R.menu.settings, menu);
	    return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
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
