package edu.umbc.teamawesome.warpdriveextreme;

import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends Activity implements OnClickListener 
{

	private Handler frame = new Handler();
	private static final int FRAME_RATE = 20; // 50 fps
	private MediaPlayer mMusicPlayer;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mMusicPlayer = MediaPlayer.create(this, R.raw.pixel_wars);
		if(mMusicPlayer != null && UserPreferences.getMusicEnabled(this)) 
		{
			mMusicPlayer.start();
			mMusicPlayer.setLooping(true);
		}

		
		Handler h = new Handler();
		h.postDelayed(new Runnable() {
			@Override
			public void run() {
				initGraphics();
			}
		}, 1000);
	}
	
	@Override
	protected void onDestroy() 
	{
		mMusicPlayer.stop();
		super.onDestroy();
	}
	
	synchronized public void initGraphics() {
		frame.removeCallbacks(frameUpdate);
		frame.postDelayed(frameUpdate, FRAME_RATE);
	}
	
	@Override
	synchronized public void onClick(View v) {
		initGraphics();
	}
	
	private Runnable frameUpdate = new Runnable() {		
		@Override
		synchronized public void run() {
			frame.removeCallbacks(frameUpdate);
			((GameCanvas)findViewById(R.id.canvas)).invalidate();
			frame.postDelayed(frameUpdate, FRAME_RATE);
		}
	};

}
