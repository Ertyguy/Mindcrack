package com.edaviessmith.mindcrack;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.edaviessmith.mindcrack.data.Member;
import com.edaviessmith.mindcrack.R;

public  class MembersAdapter extends BaseAdapter {

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
            convertView = mInflater.inflate(R.layout.member_item, parent, false);
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
            /*if(type == Constants.FAVORITE) {
            	if(AppInstance.getMember() != null && item.getId() == AppInstance.getMember().getId()) {
            		holder.itemView.setBackgroundResource(R.drawable.member_item_fav_selected);
            	}else {
            		holder.itemView.setBackgroundResource(R.color.favorite);
            	}
            } else if(AppInstance.getMember() != null && item.getId() == AppInstance.getMember().getId()) {
            	holder.itemView.setBackgroundResource(R.drawable.member_item_selected);
            } else {
            	holder.itemView.setBackgroundResource(R.color.dark_grey);
            }*/
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