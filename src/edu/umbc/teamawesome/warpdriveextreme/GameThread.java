package edu.umbc.teamawesome.warpdriveextreme;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.SurfaceHolder;

public class GameThread extends Thread {
	
	// constants
	private float HEALTH_BAR_WIDTH = 0.8f;
	private float HEALTH_BAR_HEIGHT = 5.0f;
	private float SHIP_SCALE = 0.5f;
	
	private int shields = 100, health = 100;
	
	private int canvasWidth = 1, canvasHeight = 1;
	int frame = 0, time = 0;

	// overhead
    private SurfaceHolder holder;
    private Context context;
    private Handler handler;
	private boolean running = false;
	
	// images
    private Bitmap spaceBitmap;
    private Drawable ship;
    private Drawable asteroidDraw;    
    int shipWidth = 1, shipHeight = 1;
    
    private ArrayList<Asteroid> asteroids = new ArrayList<Asteroid>();

    public GameThread(SurfaceHolder holder, Context context, Handler handler) {
    	this.holder = holder;
    	this.context = context;
    	this.handler = handler;

        Resources res = context.getResources();            
        spaceBitmap = BitmapFactory.decodeResource(res, R.drawable.space);
        ship = context.getResources().getDrawable(R.drawable.spaceship);
        shipWidth = ship.getIntrinsicWidth();
        shipHeight = ship.getIntrinsicHeight();
        asteroidDraw = context.getResources().getDrawable(R.drawable.asteroid);
    }
    
    private void updateGame() {
    	if(isCreateAsteroidTime()) {
    		createAsteroid();
    	}
    }

    private void updateDisplay(Canvas canvas) {
        canvas.drawBitmap(spaceBitmap, 0, 0, null);
        
        canvas.save();
        canvas.scale(SHIP_SCALE, SHIP_SCALE, canvasWidth / 2, canvasHeight);
        ship.setBounds((canvasWidth / 2) - (shipWidth / 2), canvasHeight - (shipHeight + 20), 
        		(canvasWidth / 2) + (shipWidth / 2), canvasHeight - 20);
        ship.draw(canvas);
        canvas.restore();
        
        int asteroidWidth = asteroidDraw.getIntrinsicWidth();
        int asteroidHeight = asteroidDraw.getIntrinsicHeight();
        for(Asteroid a : asteroids) {
        	asteroidDraw.setBounds(a.getPosX() - (asteroidWidth / 2), a.getPosY() - (asteroidHeight / 2),
        			a.getPosX() + (asteroidWidth / 2), a.getPosY() + (asteroidHeight / 2));
        	asteroidDraw.draw(canvas);
        
        Paint p = new Paint();
        
        // display the time
        p.setColor(Color.WHITE);
        String timeDisplay = String.format("Time: %d", time);
        float textWidth = p.measureText(timeDisplay);
        canvas.drawText(timeDisplay, canvasWidth - textWidth - 20.0f, 20.0f, p);
        
        // display your shields
        p.setColor(Color.CYAN);
        p.setTextSize(16.0f);
        String shieldDisplay = String.format("Shields: %03d", shields);
        canvas.drawText(shieldDisplay, 40.0f, canvasHeight - 40.0f, p);
        
        // display the ship health
        p.setColor(Color.BLACK);
        float healthBarMargin = ((1.0f - HEALTH_BAR_WIDTH) / 2.0f) * canvasWidth;
        canvas.drawRect(healthBarMargin, canvasHeight - (20.0f + HEALTH_BAR_HEIGHT),
        		canvasWidth - healthBarMargin, canvasHeight - 20.0f, p);        
        if(health > 75) p.setColor(Color.GREEN);
        else if (health > 25) p.setColor(Color.YELLOW);
        else p.setColor(Color.RED);
        canvas.drawRect(healthBarMargin, canvasHeight - (20.0f + HEALTH_BAR_HEIGHT),
        		(canvasWidth - healthBarMargin) * (health / 100.0f), canvasHeight - 20.0f, p);
        }
    }
    
    public boolean isCreateAsteroidTime() {
    	if(frame != 0) return false;
    	
    	// TODO: Some more complicated function can go here
    	if(time % 10 == 0) return true;
    	
    	return false;
    }
    
    public void createAsteroid() {
    	int startX = (int)(Math.random() * (canvasWidth + 1));
    	int startY = -5;
    	double heading = (0 - startY) / ((canvasWidth / 2) - startX);
    	double speed = 10;
    	
    	Asteroid a = new Asteroid(startX, startY, heading, speed);
    	asteroids.add(a);
    }

	public void setRunning(boolean b) {
		this.running = b;
	}

    @Override
    public void run() {
        while (this.running) {
        	updateGame();
        	
            Canvas c = null;
            try {
                c = holder.lockCanvas(null);
                synchronized (holder) {
                    updateDisplay(c);
                }
            } finally {
                if (c != null) {
                    holder.unlockCanvasAndPost(c);
                }
            }
            
            frame++;
            if(frame % 50 == 0) {
            	time++;
            	frame = 0;
            }
        }
    }
    
    public void setSurfaceSize(int width, int height) {
        // synchronized to make sure these all change atomically
        synchronized (holder) {
            canvasWidth = width;
            canvasHeight = height;

            // don't forget to resize the background image
            spaceBitmap = Bitmap.createScaledBitmap(spaceBitmap, width, height, true);
        }
    }

}
