package edu.umbc.teamawesome.warpdriveextreme;

import java.util.HashMap;
import java.util.Hashtable;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;

public class MainMenuActivity extends Activity 
{
	
	protected Button newGameButton;
	protected Button settingsButton;
	protected Button highScoresButton;

	private SoundPool mSoundPool;
	private AudioManager  mAudioManager;

	private int MENU_SOUND_ID;
	private int BACK_SOUND_ID;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_menu);

		mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

		MENU_SOUND_ID = mSoundPool.load(this, R.raw.confirm, 1);
		BACK_SOUND_ID = mSoundPool.load(this, R.raw.back, 1);
		
		newGameButton = (Button) findViewById(R.id.newGameButton);
		settingsButton = (Button) findViewById(R.id.settingsButton);
		highScoresButton = (Button) findViewById(R.id.highScoresButton);

		newGameButton.setOnClickListener(mainMenuClickListener);
		settingsButton.setOnClickListener(mainMenuClickListener);
		highScoresButton.setOnClickListener(mainMenuClickListener);
		
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK && UserPreferences.getSoundEnabled(this))
		{
			float streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			streamVolume = streamVolume / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

			mSoundPool.play(BACK_SOUND_ID, streamVolume / 3, streamVolume / 3, 1, 0, 1f);
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	private OnClickListener mainMenuClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View button) {
			float streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			streamVolume = streamVolume / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

			if(UserPreferences.getSoundEnabled(MainMenuActivity.this))
			{
				mSoundPool.play(MENU_SOUND_ID, streamVolume / 3, streamVolume / 3, 1, 0, 1f);
			}
			
			if(button == newGameButton)
			{
				if(UserPreferences.getShowTutorial(MainMenuActivity.this))
				{
					promptTutorial();
				}
				else
				{
					Intent intent = new Intent(MainMenuActivity.this, MainActivity.class);
					startActivity(intent);
				}
			}
			else if(button == settingsButton)
			{
				SettingsFragment settingsFragment = new SettingsFragment();
				
				FragmentTransaction transaction = getFragmentManager().beginTransaction();
				transaction.add(R.id.main_menu_activity_layout, settingsFragment);
				transaction.addToBackStack(null);
				transaction.commit();

			}
			else if(button == highScoresButton)
			{
				HighScoresFragment highScoresFragment = new HighScoresFragment();
								
				FragmentTransaction transaction = getFragmentManager().beginTransaction();
				transaction.add(R.id.main_menu_activity_layout, highScoresFragment);
				transaction.addToBackStack(null);
				transaction.commit();

			}

		}
	};
	
    public void promptTutorial()
    {
		UserPreferences.setShowTutorial(this, false);
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Instructions");
		alert.setMessage("Defend yourself as you fly through an asteroid field by drawing shields on the screen to deflect them away from your ship, and towards incoming asteroid. " +
				"But be careful! The energy for your shields only regenerates so quickly. If you manage to successfully deflect one asteroid into another, you can harvest the kinetic" +
				"energy to give your shield energy a quick boost. Good luck!");

		alert.setCancelable(false);
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
	            Intent intent = new Intent(MainMenuActivity.this, MainActivity.class);
	            startActivity(intent);
			}
		});

		alert.show();
    }
	
	@Override
	protected void onStart() 
	{
		super.onStart();
		
	}
	
}
