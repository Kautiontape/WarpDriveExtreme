package edu.umbc.teamawesome.warpdriveextreme;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;

public class SettingsFragment extends Fragment implements OnCheckedChangeListener
{
	private ToggleButton toggleMusic;
	private ToggleButton toggleSound;
	private ToggleButton toggleTutorial;

	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	
    	View view = inflater.inflate(R.layout.fragment_settings, container, false);
                
        view.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
                
        toggleMusic = (ToggleButton) view.findViewById(R.id.musicToggle);
        toggleMusic.setChecked(UserPreferences.getMusicEnabled(getActivity()));
        toggleMusic.setOnCheckedChangeListener(this);
        
        toggleSound = (ToggleButton) view.findViewById(R.id.soundToggle);
        toggleSound.setChecked(UserPreferences.getSoundEnabled(getActivity()));
        toggleSound.setOnCheckedChangeListener(this);

        toggleTutorial = (ToggleButton) view.findViewById(R.id.showTutorialToggle);
        toggleTutorial.setChecked(UserPreferences.getShowTutorial(getActivity()));
        toggleTutorial.setOnCheckedChangeListener(this);

        return view;
    }


	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) 
	{
		if(buttonView == toggleMusic)
		{
			UserPreferences.setMusicEnabled(getActivity(), isChecked);
		}
		else if(buttonView == toggleSound)
		{
			UserPreferences.setSoundEnabled(getActivity(), isChecked);
		}
		else if(buttonView == toggleTutorial)
		{
			UserPreferences.setShowTutorial(getActivity(), isChecked);
		}
		
	}

	
}
