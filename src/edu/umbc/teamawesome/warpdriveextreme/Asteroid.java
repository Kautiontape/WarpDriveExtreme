package edu.umbc.teamawesome.warpdriveextreme;

public class Asteroid {

	private int posX = 0, posY = 0;
	private double heading = 0.0, speed = 0.0;
	
	private double initPosX = 0.0, initPosY = 0.0;
	
	public Asteroid() {}
	
	public Asteroid(int posX, int posY) {
		this(posX, posY, 0.0);
	}
	
	public Asteroid(int posX, int posY, double heading) {
		this(posX, posY, heading, 0.0);
	}
	
	public Asteroid(int posX, int posY, double heading, double speed) {
		this.initPosX = this.posX = posX; this.initPosY = this.posY = posY;
		this.heading = heading; this.speed = speed;
	}
	
	// accessors / mutators
	public int getPosX() {
		return posX;
	}

	public void setPosX(int posX) {
		this.posX = posX;
	}

	public int getPosY() {
		return posY;
	}

	public void setPosY(int posY) {
		this.posY = posY;
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

	public double getInitPosX() {
		return initPosX;
	}

	public double getInitPosY() {
		return initPosY;
	}

}
