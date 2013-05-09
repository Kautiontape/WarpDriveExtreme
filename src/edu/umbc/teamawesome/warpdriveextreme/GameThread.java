package edu.umbc.teamawesome.warpdriveextreme;

import java.util.ArrayList;
import java.util.Vector;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;

public class GameThread extends Thread {
	
	// constants
	private float SHIP_SCALE = 0.5f;
	private int ENERGY_PER_SECOND = 6;
	private int FRAMES_PER_SECOND = 50;
	private boolean ENERGY_BAR = true;
	private int SHIELD_COLOR = Color.CYAN;
	
	// game status
	private int energy = 100, health = 100;
	private boolean isDrawingShield = false;
	private Shield currentShield = null;
    
    private ArrayList<Asteroid> asteroids = new ArrayList<Asteroid>();
    private ArrayList<Shield> shields = new ArrayList<Shield>();
	
	// game metrics
	private int canvasWidth = 1, canvasHeight = 1;
	private int frame = 0, time = 0;
	
	// ship info
	private int shipLeft, shipTop, shipRight, shipBottom;

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
    int timeFontSize = 12; int energyFontSize = 26;
    
    // game bars
    GameBar healthBar = new GameBar(0, Color.GREEN);
    GameBar energyBar = new GameBar(1, SHIELD_COLOR);

    public GameThread(SurfaceHolder holder, Context context, Handler handler) {
    	this.holder = holder;
    	this.context = context;
    	this.handler = handler;

        Resources res = context.getResources();            
        spaceBitmap = BitmapFactory.decodeResource(res, R.drawable.space);
        ship = res.getDrawable(R.drawable.spaceship);
        shipWidth = ship.getIntrinsicWidth();
        shipHeight = ship.getIntrinsicHeight();
        
        asteroidDraw = res.getDrawable(R.drawable.asteroid);
        timeFontSize = res.getDimensionPixelSize(R.dimen.timeFontSize);
        energyFontSize = res.getDimensionPixelSize(R.dimen.energyFontSize);
    }
    
    private void updateGame() {
    	if(isCreateAsteroidTime()) {
    		createAsteroid();
    	}
    	
    	moveAsteroids();
    	regenerateEnergy();
    }

    private void updateDisplay(Canvas canvas) {
    	if(canvas == null) return;
        Paint p = new Paint();
        
        canvas.drawBitmap(spaceBitmap, 0, 0, null);
        
        canvas.save();
        ship.setBounds(shipLeft, shipTop, shipRight, shipBottom);
        ship.draw(canvas);
        canvas.restore();
        
        int asteroidWidth = asteroidDraw.getIntrinsicWidth();
        int asteroidHeight = asteroidDraw.getIntrinsicHeight();
        for(Asteroid a : asteroids) {
        	canvas.save();
        	float scale = (float)a.getRadius() / Asteroid.MAX_RADIUS;
        	canvas.scale(scale, scale, a.getPos().x, a.getPos().y);
        	asteroidDraw.setBounds(a.getPos().x - (asteroidWidth / 2), a.getPos().y - (asteroidHeight / 2),
        			a.getPos().x + (asteroidWidth / 2), a.getPos().y + (asteroidHeight / 2));
        	asteroidDraw.draw(canvas);
        	canvas.restore();

//        	p.setColor(Color.RED);
//        	p.setAlpha(50);
//        	canvas.drawCircle((float)a.getPos().x, (float)a.getPos().y, (float)a.getRadius(), p);
//        	p.setAlpha(255);
        }
        
        // display the shields
        p.setColor(SHIELD_COLOR);
        p.setStrokeWidth(2.0f);
        for(Shield s : shields) {
        	canvas.drawLine(s.getStart().x, s.getStart().y, s.getEnd().x, s.getEnd().y, p);
        }
        if(isDrawingShield && currentShield != null) {
        	p.setColor(Color.WHITE);
        	p.setAlpha(100);
        	canvas.drawLine(currentShield.getStart().x, currentShield.getStart().y,
        			currentShield.getEnd().x, currentShield.getEnd().y, p);
            p.setAlpha(255);
        }
        
        // display the time
        p.setColor(Color.WHITE);
        p.setTextSize(timeFontSize);
        String timeDisplay = String.format("Time: %d", time);
        canvas.drawText(timeDisplay, canvasWidth - p.measureText(timeDisplay) - 20.0f,
        		timeFontSize, p);

        // display your energy
        int currentEnergy = energy - (isDrawingShield && currentShield != null ? 
        		currentShield.getCost(canvasWidth) : 0);
        if(ENERGY_BAR) {
        	energyBar.setCurrent(currentEnergy);
        	energyBar.draw(canvas);
        } else {
	        p.setColor(SHIELD_COLOR);
	        p.setTextSize(energyFontSize);
	        String shieldDisplay = String.format("Energy: %03d", currentEnergy);
	        canvas.drawText(shieldDisplay, 40.0f, canvasHeight - 40.0f, p);
        }
        
        // display the ship health
        int c = Color.GREEN;
        if(health < 25) p.setColor(Color.RED);
        else if (health < 75) p.setColor(Color.YELLOW);
        healthBar.setCurrent(health);
        healthBar.setColor(c);
        healthBar.draw(canvas);
    }
    
    public boolean isCreateAsteroidTime() {
    	if(frame != 0) return false;
    	
    	// TODO: Some more complicated function can go here
    	if(time % 1 == 0) return true;
    	
    	return false;
    }
    
    public void createAsteroid() {
    	double radius = Math.min(0.3 + Math.random()*Asteroid.MAX_RADIUS, Asteroid.MAX_RADIUS);
    	int startX = (int)(Math.random() * (canvasWidth + 1));
    	int startY = (int)-radius;
    	double deltaX = startX - (canvasWidth / 2);
    	double deltaY = startY - canvasHeight;
    	
    	Asteroid a = new Asteroid(startX, startY);
    	a.setHeading(Math.atan2(deltaY, deltaX) / Math.PI * 180);
    	a.setSpeed(1000);
    	a.setRadius(radius);
    	asteroids.add(a);
    }
    
    public void moveAsteroids() {
    	ArrayList<Asteroid> deleteAsteroid = new ArrayList<Asteroid>();
    	for(Asteroid a : asteroids) {
    		double speed = a.getSpeed();
    		double heading = a.getHeading();
    		int x = ((int)(a.getPos().x - Math.cos(heading/180*Math.PI)*(speed / FRAMES_PER_SECOND)));
    		int y = ((int)(a.getPos().y - Math.sin(heading/180*Math.PI)*(speed / FRAMES_PER_SECOND)));
    		a.setPos(new Point(x, y));
    		
    		// cleanup missing asteroids
    		if(a.getPos().y - a.getRadius() > canvasHeight) { // off screen
    			deleteAsteroid.add(a);
    			continue;
    		} else if (a.getPos().x + a.getRadius() < 0 || a.getPos().x - a.getRadius() > canvasWidth) {
    			deleteAsteroid.add(a);
    			continue;
    		}
    		
    		if(a.collidingWithRect(shipLeft, shipTop, shipRight, shipBottom)) {
    			health -= a.getDamage();
    			deleteAsteroid.add(a);
    			continue;
    		}
    		
    		ArrayList<Shield> deleteShield = new ArrayList<Shield>();
    		for(Shield s : shields) {
    			if(a.collidingWithLine(s.getStart(), s.getEnd())) {
    				deleteAsteroid.add(a);
    				s.setHealth(s.getHealth() - a.getDamage());
    				if(s.getHealth() < 0)
    					deleteShield.add(s);
    				continue;
    			}
    		}
    		shields.removeAll(deleteShield);
    	}

    	asteroids.removeAll(deleteAsteroid);
    }
    
    public void startShield(Point start) {
    	if(energy <= 0) return;
    	
    	Shield s = new Shield(start);
    	s.setStart(start);
    	isDrawingShield = true;
    	this.currentShield = s;
    }
    
    public void updateShield(Point update) {
    	if(isDrawingShield && currentShield != null) {
    		this.currentShield.setEnd(update);
    		
    		if(energy - currentShield.getCost(canvasWidth) <= 0) {
    			energy = 0;
    			finishShield(update);
    		}
    	}
    }
    
    public void finishShield(Point finish) {
    	if(isDrawingShield && currentShield != null) {
    		currentShield.setEnd(finish);
    		currentShield.setHealth(Shield.MAX_HEALTH);
    		energy -= currentShield.getCost(canvasWidth);
    		if(energy <= 0) energy = 0;
        	shields.add(currentShield);
    		currentShield = null;
    		isDrawingShield = false;
    	}
    }
    
    private void regenerateEnergy() {
    	if(frame % Math.floor((double)FRAMES_PER_SECOND / (double)ENERGY_PER_SECOND) != 0) return;
    	energy += 1;
    	if(energy > 100) energy = 100;
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
            if(frame % FRAMES_PER_SECOND == 0) {
            	time++;
            	frame = 0;
            }
        }
    }
    
    public void setSurfaceSize(int width, int height) {
        synchronized (holder) {
            canvasWidth = width;
            canvasHeight = height;
            spaceBitmap = Bitmap.createScaledBitmap(spaceBitmap, width, height, true);
            updateShipSize();
        }
    }
    
    public void updateShipSize() {
    	synchronized (holder) {
	        shipLeft = (canvasWidth / 2) - (int)(shipWidth*SHIP_SCALE / 2);
	        shipRight = (canvasWidth / 2) + (int)(shipWidth*SHIP_SCALE / 2);
	        shipTop = canvasHeight - (int)(shipHeight*SHIP_SCALE + 20); 
	        shipBottom = canvasHeight - 20;
    	}
    }

}
