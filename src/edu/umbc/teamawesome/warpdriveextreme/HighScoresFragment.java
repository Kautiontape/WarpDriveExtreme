package edu.umbc.teamawesome.warpdriveextreme;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class HighScoresFragment extends Fragment 
{
	private LayoutInflater inflater;
	private ListView highScoresList;
	private HighScoresListAdapter adapter;
	
    public void onCreate(Bundle savedInstanceState) 
    {
    	super.onCreate(savedInstanceState);
    	    	    	
		adapter = new HighScoresListAdapter(getActivity(), UserPreferences.getHighScores(getActivity()));

    }
    
    public void onResume()
    {
    	super.onResume();
    	
    	if(highScoresList.getAdapter() == null)
    	{
    		adapter = new HighScoresListAdapter(getActivity(), UserPreferences.getHighScores(getActivity()));
    		highScoresList.setAdapter(adapter);
    	}
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
    	
    	View view = inflater.inflate(R.layout.fragment_high_scores, container, false);
        
        highScoresList = (ListView) view.findViewById(R.id.high_scores_list);
        highScoresList.setAdapter(adapter);
        
        view.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        
        return view;
    }

}
