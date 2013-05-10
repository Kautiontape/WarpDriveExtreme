package edu.umbc.teamawesome.warpdriveextreme;

import android.graphics.Point;

public class Asteroid {
	public static final int MAX_RADIUS = 65;
	public static final int MAX_DAMAGE = 20;
	public static final int MAX_HEALTH = 20; 

	private Point pos = new Point(0, 0), initPos = new Point(0, 0);
	private double heading = 0.0, speed = 0.0, radius = 0.0;
	private int health = 0;
	
	public Asteroid() { this(new Point(0, 0)); }
	public Asteroid(int posX, int posY) { this(new Point(posX, posY)); }
	public Asteroid(Point p) { pos = p; }
    
    public int getDamage() {
    	return (int)(MAX_DAMAGE*(getRadius()/MAX_RADIUS)); 
    }
	
	public boolean collidingWithRect(float left, float top, float right, float bottom) {
		return (pos.x > left && pos.x < right && pos.y > top && pos.y < bottom);
	}
	
	public boolean collidingWithLine(Point a, Point b) {
    	Point c = getPos(); 
    	double L = Math.sqrt(Math.pow(b.x - a.x, 2) + Math.pow(b.y - a.y, 2));
    	double r = ((a.y - c.y)*(a.y - b.y) - (a.x - c.x)*(b.x - a.x))/(L*L);
    	double s = ((a.y - c.y)*(b.x - a.x) - (a.x - c.x)*(b.y - a.y))/(L*L);
//    	PointF i = new PointF((float)(a.x + r*(b.x - a.x)), (float)(a.y + r*(b.y - a.y)));
    	double dist = s*L;
    	
    	if(dist < getRadius() && 0 <= r && r <= 1) return true;
    	
		return false;
	}
	
	public boolean collidingWithAsteroid(Asteroid o) {
		return (Math.sqrt(Math.pow(o.pos.x - this.pos.x, 2) + Math.pow(o.pos.y - this.pos.y, 2)) <= this.radius + o.radius);
	}
	
	// accessors / mutators	
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

}
