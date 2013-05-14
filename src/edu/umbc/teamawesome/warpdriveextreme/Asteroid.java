package edu.umbc.teamawesome.warpdriveextreme;

import math.geom2d.Vector2D;
import android.graphics.Point;

public class Asteroid {
	public static final int MAX_DAMAGE = 20;
	public static final int MAX_HEALTH = 20;
	public static final int MAX_SPEED = 600;
	public static final int MIN_SPEED = 200;
	public static final int SEC_PER_ROTATE = 2;
	
	private static int max_radius = 65;

	private Point pos = new Point(0, 0), initPos = new Point(0, 0);
	private double heading = 0.0, speed = 0.0, radius = 0.0, rotate = 0.0;
	private int health = MAX_HEALTH; private boolean isBounced = false;
	
	public Asteroid() { this(new Point(0, 0)); }
	public Asteroid(int posX, int posY) { this(new Point(posX, posY)); }
	public Asteroid(Point p) { pos = p; }
    
    public int getDamage() {
    	return (int)(MAX_DAMAGE*(getRadius()/Asteroid.getMaxRadius())); 
    }
    
    public Point getNextPos() {
		double speed = getSpeed();
		double heading = getHeading();
		int x = ((int)(getPos().x - Math.cos(heading)*speed));
		int y = ((int)(getPos().y - Math.sin(heading)*speed));
		return new Point(x, y);
    }
    
    public void rotate(double radians) {
    	rotate = (rotate + radians) % (2*Math.PI);      	
    }
    
    public double getMass() {
    	return (4.0/3)*Math.PI * radius*radius;
    }
    
    public Vector2D getVelocityVector() {
    	return new Vector2D(speed*Math.cos(heading), speed*Math.sin(heading));	
    }
	
	// accessors / mutators	
    public static int getMaxRadius() {
    	return max_radius;
    }
    
    public static void setMaxRadius(int max) {
    	max_radius = max;
    }
    
    public boolean isBounced() {
    	return isBounced;
    }
    
    public void setBounced(boolean b) {
    	this.isBounced = b;
    }
    
	public int getHealth() {
		return health;
	}
	
	public void setHealth(int h) {
		health = h;
	}
	
	public Point getPos() {
		return pos;
	}

	public void setPos(Point p) {
		this.pos = p;
	}

	public double getHeading() {
		return heading;
	}

	public void setHeading(double heading) {
		this.heading = heading;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public Point getInitPos() {
		return initPos;
	}
	
	public double getRadius() {
		return this.radius;
	}
	
	public void setRadius(double radius) {
		this.radius = radius;
	}
	
	public double getRotate() {
		return rotate;
	}
	
	public void setRotate(double rotate) {
		this.rotate = rotate;
	}

}
