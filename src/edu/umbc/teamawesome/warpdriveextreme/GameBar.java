package edu.umbc.teamawesome.warpdriveextreme;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class GameBar {
	public static float BAR_WIDTH = 0.8f;
	public static float BAR_HEIGHT = 5.0f;
	
	private int max = 100;
	private int current = 100;
	private int color = Color.WHITE;
	private int index = 0;
	
	public GameBar() {}
	
	public GameBar(int index) {
		this(index, Color.WHITE);
	}
	
	public GameBar(int index, int color) {
		this.index = index;
		this.color = color;
	}
	
	public void draw(Canvas c) {
		Paint p = new Paint();
        float barMargin = ((1.0f - BAR_WIDTH) / 2.0f) * c.getWidth();
        
        float left = barMargin;
        float top = c.getHeight() - ((20.0f + 10.0f * index) + (index + 1)*BAR_HEIGHT);
        float right = c.getWidth() - barMargin;
        float bottom = top - BAR_HEIGHT;
        
        p.setColor(Color.BLACK);
        c.drawRect(left, top, right, bottom, p);   
        
        p.setColor(color);
        c.drawRect(left, top, left + (right - left)*(current / (float)max), bottom, p);
	}
	
	public void setIndex(int index) {
		this.index = index;
	}
	
	public int getIndex() {
		return index;
	}

	public void setMax(int max) {
		this.max = max;
	}
	
	public void setCurrent(int current) {
		this.current = current;
	}
	
	public void setColor(int color) {
		this.color = color;
	}
}
