package edu.umbc.teamawesome.warpdriveextreme;

import android.graphics.Point;

public class EnergyGainEvent {
	private Point eventLoc;
	private int gain;
	private double time;
	
	public EnergyGainEvent(Point p, int i, double t) {
		eventLoc = new Point(p.x, p.y);
		gain = i;
		time = t;
	}
	
	public Point getEventLocation() {
		return new Point(eventLoc.x, eventLoc.y);
	}
	
	public int getGain() {
		return gain;
	}
	
	public double getTime() {
		return time;
	}
}
