package edu.umbc.teamawesome.warpdriveextreme;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameCanvas extends SurfaceView implements SurfaceHolder.Callback {
	private static SurfaceHolder holder;
	private static Handler handler;
	private GameThread thread;
	private Activity parentActivity;
	
	public GameCanvas(Context context, AttributeSet attrs) {
		super(context, attrs);

		holder = getHolder();
		holder.addCallback(this);

		thread = new GameThread(holder, context, handler);
		setFocusable(true); // make sure we get key events
	}
	
    public void setParentActivity(Activity parent)
    {
    	this.parentActivity = parent;
		thread.setParentActivity(parentActivity);
    }
    
    public Activity getParentActivity()
    {
    	return parentActivity;
    }
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch(event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			thread.startShield(new Point((int)event.getX(), (int)event.getY()));	
			break;
		case MotionEvent.ACTION_MOVE:
			thread.updateShield(new Point((int)event.getX(), (int)event.getY()));
			break;
		case MotionEvent.ACTION_UP:
			thread.finishShield(new Point((int)event.getX(), (int)event.getY()));
			break;			
		}
		return true;
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
