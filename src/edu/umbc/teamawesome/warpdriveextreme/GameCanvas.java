package edu.umbc.teamawesome.warpdriveextreme;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameCanvas extends SurfaceView implements SurfaceHolder.Callback {
	private SurfaceHolder holder;
	private GameThread thread;
	
	public GameCanvas(Context context, AttributeSet attrs) {
		super(context, attrs);

		holder = getHolder();
		holder.addCallback(this);

		thread = new GameThread(holder, context, new Handler() {
			@Override
			public void handleMessage(Message m) {}
		});
		
		setFocusable(true); // make sure we get key events
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
       thread.setSurfaceSize(width, height);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
        thread.setRunning(true);
        thread.start();
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        thread.setRunning(false);
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }		
	}
}
