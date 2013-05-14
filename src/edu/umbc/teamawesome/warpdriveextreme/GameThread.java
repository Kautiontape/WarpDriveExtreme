package edu.umbc.teamawesome.warpdriveextreme;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;

import edu.umbc.teamawesome.warpdriveextreme.GamePhysics.Rect;

import math.geom2d.Vector2D;

import android.app.Activity;
import android.app.AlertDialog;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.EditText;

@SuppressLint("DefaultLocale")
public class GameThread extends Thread {
	
	// constants
	private static final float SHIP_SCALE = 0.5f;
	private static final int ENERGY_PER_SECOND = 6;
	private static final int ENERGY_PER_BOUNCE = 5;
	private static final int ENERGY_PER_BLOCK = 5;
	private static final int FRAMES_PER_SECOND = 50;
	private static final boolean ENERGY_BAR = true;
	private static final int SHIELD_COLOR = Color.CYAN;
	private static final double TIME_MAX_SPEED = 30.0;
//	private static final double GAIN_EVENT_SHOW_TIME = 0.5;
	private static final double ASTEROID_ROTATE = 1.0 / (double)(Asteroid.SEC_PER_ROTATE * FRAMES_PER_SECOND / (2.0*Math.PI));
	
	// scoring
	private static final int POINTS_PER_BOUNCE = 500;
	private static final int POINTS_PER_BLOCK = 500;
	private static final int POINTS_PER_SECOND = 100;
	
	private static final int STATE_START = 0;
	private static final int STATE_PLAYING = 1;
	private static final int STATE_GAMEOVER = 2;
	
	private boolean hasPrompt = false;
	private boolean hasEnteredHighScore = false;

	// game status
	private int gameState = STATE_START;
	private int energy = 100, health = 10;
	private boolean isDrawingShield = false;
	private Shield currentShield = null;
	private GamePhysics.Rect shipBox;
	private int points = 0;
    
    private ArrayList<Asteroid> asteroids = new ArrayList<Asteroid>();
    private ArrayList<Shield> shields = new ArrayList<Shield>();
	
	// game metrics
	private int canvasWidth = 1, canvasHeight = 1;
	private int frame = 0, time = 0;

	// overhead
    private SurfaceHolder holder;
	private Context context;
//	private Handler handler;
	private boolean running = false;
	private Activity parentActivity;
	
	// sound 
	private int explosionId;
	private int shieldId;
	private int thudId;
	private int crunchId;
	private SoundPool mSoundPool;
	private AudioManager mAudioManager;

	private GamePhysics gp;
	private ArrayList<EnergyGainEvent> gainEvents = new ArrayList<EnergyGainEvent>();
	private ArrayList<PointGainEvent> pointEvents = new ArrayList<PointGainEvent>();
	
	// images
    private Bitmap spaceBitmap;
    private Bitmap explosionBitmap;
    private Drawable ship;
    private Drawable asteroidDraw;
    int shipWidth = 1, shipHeight = 1;
    int timeFontSize = 12; int energyFontSize = 26;
    int gameoverFontSize = 32; int scoreFontSize = 28;
    int titleFontSize = 38;
    ExplosionAnimated shipExplosion;
    
    // game bars
    GameBar healthBar = new GameBar(0, Color.GREEN);
    GameBar energyBar = new GameBar(1, SHIELD_COLOR);

    public GameThread(SurfaceHolder holder, Context context, Handler handler) {
    	this.holder = holder;
    	this.context = context;
//    	this.handler = handler;
    	
    	gp = new GamePhysics();

        Resources res = context.getResources();            
        spaceBitmap = BitmapFactory.decodeResource(res, R.drawable.space);
        explosionBitmap = BitmapFactory.decodeResource(res, R.drawable.explosion);
        ship = res.getDrawable(R.drawable.spaceship);
        shipWidth = ship.getIntrinsicWidth();
        shipHeight = ship.getIntrinsicHeight();
        
		mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

		explosionId = mSoundPool.load(context, R.raw.explosion, 1);
		shieldId = mSoundPool.load(context, R.raw.zap, 1);
		thudId = mSoundPool.load(context, R.raw.thud, 1);
		crunchId = mSoundPool.load(context, R.raw.crunch, 1);

        asteroidDraw = res.getDrawable(R.drawable.asteroid);
        timeFontSize = res.getDimensionPixelSize(R.dimen.timeFontSize);
        energyFontSize = res.getDimensionPixelSize(R.dimen.energyFontSize);
        gameoverFontSize = res.getDimensionPixelSize(R.dimen.gameoverFontSize);
        scoreFontSize = res.getDimensionPixelSize(R.dimen.scoreFontSize);
        titleFontSize = res.getDimensionPixelSize(R.dimen.titleFontSize);
    }
    
    public void setParentActivity(Activity parent)
    {
    	this.parentActivity = parent;
    }
    
    public Activity getParentActivity()
    {
    	return parentActivity;
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
    	generatePoints();
    }

    private void updateDisplay(Canvas canvas) {
    	if(canvas == null) return;
            	
        canvas.drawBitmap(spaceBitmap, 0, 0, null);
    	if(gameState == STATE_GAMEOVER)
    	{
    		if(shipExplosion == null)
    		{
    			shipExplosion = new ExplosionAnimated(explosionBitmap, shipBox);
    		}
    		else if(!shipExplosion.isFinished())
    		{
    			shipExplosion.draw(canvas);
    		}
    		else
    		{
    			drawGameOver(canvas);
    			if(UserPreferences.checkHighScore(context, String.valueOf(time)) && hasPrompt == false && hasEnteredHighScore == false)
    			{
    				hasPrompt = true;
    				hasEnteredHighScore = true;

    				promptHighScore();
    			}
    		}
    	} else if(gameState == STATE_START){
    		drawTitle(canvas);
    	} else {
    		try {
    			drawGame(canvas);
    		} catch(ConcurrentModificationException cme) {
    			// ignore
    		}
    	}
    }
    
    private void startGame() {
    	hasEnteredHighScore = false;
    	frame = 0;
    	time = 0;
    	points = 0;
    	health = 100;
    	energy = 100;
    	isDrawingShield = false; currentShield = null;
    	asteroids = new ArrayList<Asteroid>();
    	shields = new ArrayList<Shield>();
    	gameState = STATE_PLAYING;
    	shipExplosion = null;
    }
    
    private void drawTitle(Canvas canvas) {
    	startGame();
    	
    	if(true) return;
    	
    	Paint p = new Paint();
        
        canvas.save();
        ship.setBounds((int)shipBox.left(), (int)shipBox.top(), (int)shipBox.right(), (int)shipBox.bottom());
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
        canvas.drawText(startText, (canvasWidth / 2) - p.measureText(startText) / 2, shipBox.top() - timeFontSize - 10.0f, p);
    }
    
    private void drawGame(Canvas canvas) {
        Paint p = new Paint();
        
        // space
        canvas.drawBitmap(spaceBitmap, 0, 0, null);
        
        
        // ship
        canvas.save();
        ship.setBounds((int)shipBox.left(), (int)shipBox.top(), (int)shipBox.right(), (int)shipBox.bottom());
        ship.draw(canvas);
        canvas.restore();
        
        // asteroids
        int asteroidWidth = asteroidDraw.getIntrinsicWidth();
        int asteroidHeight = asteroidDraw.getIntrinsicHeight();
        for(Asteroid a : asteroids) {
        	canvas.save();
        	float scale = (float)a.getRadius() / Asteroid.getMaxRadius();
        	canvas.rotate((float)Math.toDegrees(a.getRotate()), (float)a.getPos().x, (float)a.getPos().y);
        	asteroidDraw.setBounds((int)(a.getPos().x - (scale*asteroidHeight / 2)), 
        			(int)(a.getPos().y - (scale*asteroidWidth / 2)),
        			(int)(a.getPos().x + (scale*asteroidHeight / 2)),
        			(int)(a.getPos().y + (scale*asteroidWidth / 2)));
        	asteroidDraw.draw(canvas);
        	canvas.restore();

        	// hit box on meteors
        	/* p.setColor(Color.RED);
        	p.setAlpha(50);
        	canvas.drawCircle((float)a.getPos().x, (float)a.getPos().y, (float)a.getRadius(), p);
        	p.setAlpha(255); */
        }
        
        // shields
        p.setColor(SHIELD_COLOR);
        p.setStrokeWidth(2.0f);
        for(Shield s : shields) {
        	p.setAlpha(Math.max((int)(((float)s.getHealth() / Shield.MAX_HEALTH) * 255), 50));
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
        
        // points
        p.setColor(Color.WHITE);
        p.setTextSize(timeFontSize);
        String pointsDisplay = context.getResources().getString(R.string.points_label) + ": " + points;
        canvas.drawText(pointsDisplay, 20.0f, timeFontSize, p);

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
        
        // energy gain
        /*
        for(EnergyGainEvent ege : gainEvents) {
        	if(ege.getTime() + GAIN_EVENT_SHOW_TIME > exactTime()) {
        		p.setColor(SHIELD_COLOR);
        		p.setTextSize(energyFontSize);
        		Point loc = ege.getEventLocation();
        		canvas.drawText("+" + ege.getGain(), loc.x, loc.y, p);
        	} else {
        		gainEvents.remove(ege);
        	}
        }
        */
        
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
        
        String score = String.format("%s: %d", context.getResources().getString(R.string.final_score), points);
        p.setTextSize(scoreFontSize);
        canvas.drawText(score, (canvasWidth / 2) - p.measureText(score) / 2, canvasHeight / 2, p);
        
        String retry = context.getResources().getString(R.string.retry);
        p.setTextSize(timeFontSize);
        canvas.drawText(retry, (canvasWidth / 2) - p.measureText(retry) / 2, canvasHeight - timeFontSize - 10.0f, p);
    }
    
    private boolean isCreateAsteroidTime() {
    	if(time < 1) return false;
    	return ((frame % (int)(FRAMES_PER_SECOND * (15.0 / time))) == 0);
    }
    
    private void createAsteroid() {
    	double radius = Math.min(0.5 + Math.random()*Asteroid.getMaxRadius(), Asteroid.getMaxRadius());
    	int startX = (int)(Math.random() * (canvasWidth + 1));
    	int startY = (int)-radius;
    	int endX = (int)(Math.random() * (canvasWidth + 1));
    	Vector2D delta = new Vector2D(startX - endX, startY - canvasHeight);
    	
    	Asteroid a = new Asteroid(startX, startY);
    	a.setHeading(delta.angle());
    	a.setSpeed(Math.max(Asteroid.MIN_SPEED / FRAMES_PER_SECOND, 
    			(time / TIME_MAX_SPEED)*(Asteroid.MAX_SPEED / FRAMES_PER_SECOND)));
    	a.setRadius(radius);
    	asteroids.add(a);
    }
    
    private void moveAsteroids() {
    	ArrayList<Asteroid> deleteAsteroid = new ArrayList<Asteroid>();
    	for(Asteroid a : asteroids) {
    		if(deleteAsteroid.contains(a)) continue;
    		
    		// new location
    		Point oldPos = a.getPos();
    		a.setPos(a.getNextPos());    		
    		GamePhysics.Circle c = gp.new Circle(a.getPos(), a.getRadius());
    		
    		a.rotate(ASTEROID_ROTATE);
    		
    		// movement vector for circle
    		GamePhysics.Circle c_vector = gp.new Circle(
    				new Point(a.getPos().x - oldPos.x, a.getPos().y - oldPos.y), a.getRadius());
    		
    		// cleanup missing asteroids
    		if(a.getPos().y - a.getRadius() > canvasHeight) { // off screen
    			deleteAsteroid.add(a);
    			continue;
    		} else if (a.getPos().x + a.getRadius() < 0 || a.getPos().x - a.getRadius() > canvasWidth) {
    			deleteAsteroid.add(a);
    			continue;
    		}
    		
    		// check if hit ship
    		if(GamePhysics.colliding(c, shipBox)) {
    			health -= a.getDamage();
    			deleteAsteroid.add(a);
    			
    			playThud();
    			
    			if(health <= 0)
    			{
    				playExplosion();
    			}
    			
    			continue;
    		}
    		
    		// check if hit asteroid
    		for(Asteroid a2 : asteroids) {
    			if(a != a2 && a.isBounced() && GamePhysics.colliding(c, gp.new Circle(a2.getPos(), a2.getRadius()))) {
    				Point mid = collideAsteroids(a, a2);
					addEnergy(ENERGY_PER_BOUNCE, mid);
					addPoints(POINTS_PER_BOUNCE, mid);
					
					playCrunch();
    			}
    		}
    		
    		// check if hit shield
    		ArrayList<Shield> deleteShield = new ArrayList<Shield>();
    		for(Shield s : shields) {
    			if(GamePhysics.colliding(c, gp.new Line(s.getStart(), s.getEnd())) &&
    					c.getC().y < Math.max(s.getStart().y, s.getEnd().y)) {
    				s.setHealth(s.getHealth() - a.getDamage());
    				if(s.getHealth() < 0) deleteShield.add(s);
    				
    				a.setHealth(a.getHealth() - s.getHealth());

    				// new asteroid direction
					a.setHeading(GamePhysics.reflect(c_vector, gp.new Line(s.getStart(), s.getEnd())));
					a.setBounced(true);
					
					addEnergy(ENERGY_PER_BLOCK, c.getC());
					addPoints(POINTS_PER_BOUNCE, c.getC());
					
					playShield();
    			}
    		}
	    	synchronized (shields) {
        		shields.removeAll(deleteShield);
			}
    	}

	    synchronized (asteroids) {
        	asteroids.removeAll(deleteAsteroid);			
		}
    }
    
    private Point collideAsteroids(Asteroid a, Asteroid b) {
		a.setHealth(a.getHealth() - b.getDamage());
		b.setHealth(b.getHealth() - a.getDamage());
		
		// https://sites.google.com/site/t3hprogrammer/research/circle-circle-collision-tutorial
		Point ca = a.getPos();
		Point cb = b.getPos();
		double d = Math.sqrt(Math.pow(ca.x - cb.x, 2) + Math.pow(ca.y - cb.y, 2));
		
		// fix collision
		Point mid = new Point((ca.x + cb.x) / 2, (ca.y + cb.y) / 2);
		a.setPos(new Point((int)(mid.x + a.getRadius() * (ca.x - cb.x) / d),
				(int)(mid.y + a.getRadius() * (ca.y - cb.y) / d)));
		b.setPos(new Point((int)(mid.x + b.getRadius() * (cb.x - ca.x) / d),
				(int)(mid.y + b.getRadius() * (cb.y - ca.y) / d)));
		
		// update d for new position
		ca = a.getPos();
		cb = b.getPos();
		d = Math.sqrt(Math.pow(ca.x - cb.x, 2) + Math.pow(ca.y - cb.y, 2));
		
		// calculate new velocity vectors
		Vector2D n = new Vector2D((cb.x - ca.x) / d, (cb.y - ca.y) / d);
		Vector2D va = a.getVelocityVector();
		Vector2D va2 = b.getVelocityVector();
		
		double p = 2 * (va.dot(n) - va2.dot(n));
		Vector2D wa = va.minus(n.times(p)); 
		Vector2D wa2 = va2.plus(n.times(p));
		// NOTE: Removed mass, since it caused problems

		// set new headings
		a.setHeading(wa.angle());
		b.setHeading(wa2.angle());
		
		return mid;
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
    			finishShield(update);
    			energy = 0;
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
    
    private void generatePoints() {
    	if(frame == 0) {
    		addPoints(POINTS_PER_SECOND);
    	}
    }
    
    private void regenerateEnergy() {
    	if(frame % Math.floor((double)FRAMES_PER_SECOND / (double)ENERGY_PER_SECOND) != 0) return;
    	addEnergy(1);
    }

	public void setRunning(boolean b) {
		this.running = b;
	}
	
	public boolean isRunning() {
		return this.running;
	}
	
	private void addEnergy(int gain) {
		energy = Math.min(energy + gain, 100);
	}
	
	private void addEnergy(int gain, Point eventLoc) {
		addEnergy(gain);
		gainEvents.add(new EnergyGainEvent(eventLoc, gain, exactTime()));		
	}
	
	private void addPoints(int gain) {
		points += gain;
	}
	
	private void addPoints(int gain, Point eventLoc) {
		addPoints(gain);
		pointEvents.add(new PointGainEvent(eventLoc, gain, exactTime()));		
	}
	
	private double exactTime() {
		return time + ((double)frame / FRAMES_PER_SECOND);
	}

    @Override
    public void run() {
        while (this.running) {
        	try {
        		updateGame();
        	} catch (ConcurrentModificationException cme) {
        		// ignore
        	}
        	
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
            	frame = 0;
	            if(gameState == STATE_PLAYING) time++;
            }
        }
    }
    
    public void setSurfaceSize(int width, int height) {
        synchronized (holder) {
            canvasWidth = width;
            canvasHeight = height;
            spaceBitmap = Bitmap.createScaledBitmap(spaceBitmap, width, height, true);
            Asteroid.setMaxRadius((int)Math.min(asteroidDraw.getIntrinsicHeight() / 2.0, 
            		asteroidDraw.getIntrinsicWidth() / 2.0));
            updateShipSize();
        }
    }
    
    public void updateShipSize() {
    	synchronized (holder) {
	        int shipLeft = (canvasWidth / 2) - (int)(shipWidth*SHIP_SCALE / 2);
	        int shipRight = (canvasWidth / 2) + (int)(shipWidth*SHIP_SCALE / 2);
	        int shipTop = canvasHeight - (int)(shipHeight*SHIP_SCALE + 20); 
	        int shipBottom = canvasHeight - 20;
	        shipBox = gp.new Rect(shipLeft, shipTop, shipRight, shipBottom);
    	}
    }
    
    public void playExplosion()
    {
		float streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		streamVolume = streamVolume / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

		if(UserPreferences.getSoundEnabled(context))
		{
			mSoundPool.play(explosionId, streamVolume / 3, streamVolume / 3, 1, 0, 1f);
		}

    }
    
    public void playShield()
    {
		float streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		streamVolume = streamVolume / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

		if(UserPreferences.getSoundEnabled(context))
		{
			mSoundPool.play(shieldId, streamVolume / 3, streamVolume / 3, 1, 0, 1f);
		}

    }
    
    public void playThud()
    {
		float streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		streamVolume = streamVolume / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

		if(UserPreferences.getSoundEnabled(context))
		{
			mSoundPool.play(thudId, streamVolume / 3, streamVolume / 3, 1, 0, 1f);
		}

    }
    
    public void playCrunch()
    {
		float streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		streamVolume = streamVolume / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

		if(UserPreferences.getSoundEnabled(context))
		{
			mSoundPool.play(crunchId, streamVolume / 3, streamVolume / 3, 1, 0, 1f);
		}

    }
    
    public void promptHighScore()
    {
    	parentActivity.runOnUiThread(new Runnable() {

    		@Override
    		public void run() {
    			AlertDialog.Builder alert = new AlertDialog.Builder(context);

    			alert.setTitle("New High Score!");
    			alert.setMessage("Please enter your username.");

    			// Set an EditText view to get user input 
    			final EditText input = new EditText(context);
    			alert.setView(input);
    			alert.setCancelable(false);
    			alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int whichButton) {
    					UserPreferences.addHighScore(context, String.valueOf(points), input.getText().toString());
    					hasPrompt = false;
    				}
    			});

    			alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int whichButton) {
    					hasPrompt = false;
    				}
    			});

    			alert.show();
    		}
    	});

    }
 
    
}
