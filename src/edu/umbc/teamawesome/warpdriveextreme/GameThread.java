package edu.umbc.teamawesome.warpdriveextreme;

import java.util.ArrayList;
import java.util.Vector;

import math.geom2d.Vector2D;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;

public class GameThread extends Thread {
	
	// constants
	private static final float SHIP_SCALE = 0.5f;
	private static final int ENERGY_PER_SECOND = 6;
	private static final int FRAMES_PER_SECOND = 50;
	private static final boolean ENERGY_BAR = true;
	private static final int SHIELD_COLOR = Color.CYAN;
	
	private static final int STATE_START = 0;
	private static final int STATE_PLAYING = 1;
	private static final int STATE_GAMEOVER = 2;
	
	// game status
	private int gameState = STATE_START;
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
    int gameoverFontSize = 32; int scoreFontSize = 28;
    int titleFontSize = 38;
    
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
        gameoverFontSize = res.getDimensionPixelSize(R.dimen.gameoverFontSize);
        scoreFontSize = res.getDimensionPixelSize(R.dimen.scoreFontSize);
        titleFontSize = res.getDimensionPixelSize(R.dimen.titleFontSize);
    }
    
    private void updateGame() {
    	if(health <= 0) {
    		gameState = STATE_GAMEOVER;
    		return;
    	}
    	
    	if(isCreateAsteroidTime()) {
    		createAsteroid();
    	}
    	
    	moveAsteroids();
    	regenerateEnergy();
    }

    private void updateDisplay(Canvas canvas) {
    	if(canvas == null) return;
        
        canvas.drawBitmap(spaceBitmap, 0, 0, null);
    	if(gameState == STATE_GAMEOVER) {
    		drawGameOver(canvas);
    	} else if(gameState == STATE_START){
    		drawTitle(canvas);
    	} else {
    		drawGame(canvas);
    	}
    }
    
    private void startGame() {
    	frame = 0;
    	time = 0;
    	health = 100;
    	energy = 100;
    	isDrawingShield = false; currentShield = null;
    	asteroids = new ArrayList<Asteroid>();
    	shields = new ArrayList<Shield>();
    	gameState = STATE_PLAYING;
    }
    
    private void drawTitle(Canvas canvas) {
    	Paint p = new Paint();
        
        canvas.save();
        ship.setBounds(shipLeft, shipTop, shipRight, shipBottom);
        ship.draw(canvas);
        canvas.restore();

        String title1 = "WARP";
        String title2 = "DRIVE";
        String title3 = "EXTREME";
        p.setTextSize(titleFontSize);
        p.setColor(Color.RED);
        canvas.drawText(title3, (canvasWidth / 2) - p.measureText(title3) / 2, canvasHeight / 2, p);
        p.setColor(Color.WHITE);
        canvas.save();
        canvas.rotate(-15, canvasWidth / 2, canvasHeight / 2);
        canvas.drawText(title1, (canvasWidth / 4.0f), canvasHeight / 2.0f - titleFontSize*1.5f, p);
        canvas.restore();
        canvas.save();
        canvas.rotate(15, canvasWidth / 2, canvasHeight / 2);
        canvas.drawText(title2, canvasWidth - (canvasHeight / 4) - p.measureText(title1) / 2, canvasHeight / 2 - titleFontSize, p);
        canvas.restore();
        
    	
    	p.setColor(Color.WHITE);        
        String startText = context.getResources().getString(R.string.start_text);
        p.setTextSize(timeFontSize);
        canvas.drawText(startText, (canvasWidth / 2) - p.measureText(startText) / 2, shipTop - timeFontSize - 10.0f, p);
    }
    
    private void drawGame(Canvas canvas) {
        Paint p = new Paint();
        
        // space
        canvas.drawBitmap(spaceBitmap, 0, 0, null);
        
        // ship
        canvas.save();
        ship.setBounds(shipLeft, shipTop, shipRight, shipBottom);
        ship.draw(canvas);
        canvas.restore();
        
        // asteroids
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

        	/*// hit box on meteors
        	p.setColor(Color.RED);
        	p.setAlpha(50);
        	canvas.drawCircle((float)a.getPos().x, (float)a.getPos().y, (float)a.getRadius(), p);
        	p.setAlpha(255);
        	*/
        }
        
        // shields
        p.setColor(SHIELD_COLOR);
        p.setStrokeWidth(2.0f);
        for(Shield s : shields) {
        	p.setAlpha((int)(((float)s.getHealth() / Shield.MAX_HEALTH) * 255));
        	canvas.drawLine(s.getStart().x, s.getStart().y, s.getEnd().x, s.getEnd().y, p);
        }
        if(isDrawingShield && currentShield != null) {
        	p.setColor(Color.WHITE);
        	p.setAlpha(100);
        	canvas.drawLine(currentShield.getStart().x, currentShield.getStart().y,
        			currentShield.getEnd().x, currentShield.getEnd().y, p);
            p.setAlpha(255);
        }
        
        // time
        p.setColor(Color.WHITE);
        p.setTextSize(timeFontSize);
        String timeDisplay = context.getResources().getString(R.string.time_label) + ": " + time;
        canvas.drawText(timeDisplay, canvasWidth - p.measureText(timeDisplay) - 20.0f,
        		timeFontSize, p);

        // energy
        int currentEnergy = energy - (isDrawingShield && currentShield != null ? 
        		currentShield.getCost(canvasWidth) : 0);
        if(ENERGY_BAR) {
        	energyBar.setCurrent(currentEnergy);
        	energyBar.draw(canvas);
        } else {
	        p.setColor(SHIELD_COLOR);
	        p.setTextSize(energyFontSize);
	        String energyText = String.format("%s: %03d", 
	        		context.getResources().getString(R.string.energy_label), currentEnergy);
	        canvas.drawText(energyText, 40.0f, canvasHeight - 40.0f, p);
        }
        
        // ship health
        int c = Color.GREEN;
        if(health < 25) c = Color.RED;
        else if (health < 75) c = Color.YELLOW;
        healthBar.setCurrent(health);
        healthBar.setColor(c);
        healthBar.draw(canvas);
    }
    
    private void drawGameOver(Canvas canvas) {
        Paint p = new Paint();
        
        p.setColor(Color.WHITE);
        p.setTextSize(gameoverFontSize);
        String text = context.getResources().getString(R.string.gameover);
        canvas.drawText(text, (canvasWidth / 2) - p.measureText(text) / 2, canvasHeight / 2 - gameoverFontSize, p);
        
        String score = String.format("%s: %d %s!", context.getResources().getString(R.string.youWent), 
        		time, context.getResources().getString(R.string.seconds));
        p.setTextSize(scoreFontSize);
        canvas.drawText(score, (canvasWidth / 2) - p.measureText(score) / 2, canvasHeight / 2, p);
        
        String retry = context.getResources().getString(R.string.retry);
        p.setTextSize(timeFontSize);
        canvas.drawText(retry, (canvasWidth / 2) - p.measureText(retry) / 2, canvasHeight - timeFontSize - 10.0f, p);
    }
    
    public boolean isCreateAsteroidTime() {
    	if(frame % Math.max(FRAMES_PER_SECOND - time*2, 1) != 0 || time < 1) return false;
    	return true;
    }
    
    public void createAsteroid() {
    	double radius = Math.min(0.3 + Math.random()*Asteroid.MAX_RADIUS, Asteroid.MAX_RADIUS);
    	int startX = (int)(Math.random() * (canvasWidth + 1));
    	int startY = (int)-radius;
    	int endX = (int)(Math.random() * (canvasWidth + 1));
    	double deltaX = startX - endX;
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
    		if(deleteAsteroid.contains(a)) continue;
    		
    		// new location
    		double speed = a.getSpeed();
    		double heading = a.getHeading();
    		Point oldPos = new Point(a.getPos().x, a.getPos().y);
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
    		
    		// check if hit ship
    		if(a.collidingWithRect(shipLeft, shipTop, shipRight, shipBottom)) {
    			health -= a.getDamage();
    			deleteAsteroid.add(a);
    			continue;
    		}
    		
    		// check if hit asteroid
    		for(Asteroid a2 : asteroids) {
    			if(a != a2 && a.collidingWithAsteroid(a2)) {
    				a.setHealth(a.getHealth() - a2.getDamage());
    				a2.setHealth(a2.getHealth() - a.getDamage());
    				
    				if(a2.getHealth() <= 0) deleteAsteroid.add(a2);
    				if(a.getHealth() <= 0) deleteAsteroid.add(a);
    				else {
    					// TODO: Calculate the new asteroid position
    				}
    			}
    		}
    		
    		// check if hit shield
    		ArrayList<Shield> deleteShield = new ArrayList<Shield>();
    		for(Shield s : shields) {
    			if(a.collidingWithLine(s.getStart(), s.getEnd())) {
    				s.setHealth(s.getHealth() - a.getDamage());
    				if(s.getHealth() < 0) deleteShield.add(s);
    				
    				a.setHealth(a.getHealth() - s.getHealth());

    				// new asteroid direction
					int dx = s.getEnd().x - s.getStart().x;
					int dy = s.getEnd().y - s.getStart().y;
					
					Vector2D ri = (new Vector2D(x - oldPos.x, y - oldPos.y)).opposite();
					Vector2D normal = (new Vector2D(-dy, dx)).normalize();
					
					Vector2D rr = ri.minus((normal.times(2).times(ri.dot(normal))));
					a.setHeading(rr.angle() / Math.PI * 180);
    			}
    		}
    		shields.removeAll(deleteShield);
    	}

    	asteroids.removeAll(deleteAsteroid);
    }
    
    public void startShield(Point start) {
    	if(gameState != STATE_PLAYING) startGame();
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
            if(frame % FRAMES_PER_SECOND == 0 && gameState == STATE_PLAYING) {
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
