package edu.umbc.teamawesome.warpdriveextreme;

import android.graphics.Point;

public class Shield {
	public static final int MAX_HEALTH = 20;
	
	private Point start = new Point(0, 0), end = new Point(0, 0);
	private int health;
	
	public Shield() {}
	public Shield(int startX, int startY) {
		end = start = new Point(startX, startY);
	}
	public Shield(Point p) {
		end = start = new Point(p.x, p.y);
	}
	
	public int getLength() {
		return (int)(Math.sqrt(Math.pow(getEnd().x - getStart().x, 2) + 
				Math.pow(getEnd().y - getStart().y, 2)));
	}
	
	public int getCost(int normal) {
    	return (int)((getLength() / (float)normal) * 100);
	}
	
	public void setStart(Point p) {
		this.start = p;
	}
	public void setStart(int startX, int startY) {
		this.start = new Point(startX, startY);
	}
	
	public void setEnd(Point p) {
		this.end = p;
	}
	public void setEnd(int endX, int endY) {
		this.end = new Point(endX, endY);
	}
	
	public Point getStart() {
		return start;
	}
	public Point getEnd() {
		return end;
	}
	public int getHealth() {
		return health;
	}
	public void setHealth(int health) {
		this.health = health;
	}
}
