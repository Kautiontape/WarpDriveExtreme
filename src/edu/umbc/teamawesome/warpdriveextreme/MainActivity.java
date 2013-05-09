package edu.umbc.teamawesome.warpdriveextreme;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends Activity implements OnClickListener {

	private Handler frame = new Handler();
	private static final int FRAME_RATE = 20; // 50 fps
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Handler h = new Handler();
		h.postDelayed(new Runnable() {
			@Override
			public void run() {
				initGraphics();
			}
		}, 1000);
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
