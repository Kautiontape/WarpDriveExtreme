package edu.umbc.teamawesome.warpdriveextreme;

import java.util.ArrayList;
import java.util.Set;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

public class HighScoresListAdapter implements ListAdapter 
{
	private ArrayList<String> highScores;
	private Context ctx;
	
	public HighScoresListAdapter(Context ctx, ArrayList<String> highScores)
	{
		this.ctx = ctx;
		this.highScores = highScores;
	}
	
	@Override
	public int getCount() 
	{
		return 10;
	}

	@Override
	public String getItem(int position) 
	{
		
		if(highScores == null || position >= highScores.size())
		{
			return (position + 1) + ": ";
		}
			
		String rawString = highScores.get(position);
		
		if(rawString != null && rawString.contains(":"))
		{
			String[] scoreUserArray = rawString.split(":");
		
			return (position + 1) + ": " + scoreUserArray[0] + "\t" + scoreUserArray[1];
		}
		else
		{
			return (position + 1) + ": ";
		}
	}
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getItemViewType(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) 
	{
		if(view == null)
		{
			view = new TextView(ctx);
		}
		
		((TextView)view).setText(getItem(position));
		((TextView)view).setTextSize(25);

		return view;
	}

	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return (highScores != null);
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean areAllItemsEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		// TODO Auto-generated method stub
		return false;
	}

}
