package edu.umbc.teamawesome.warpdriveextreme;

import math.geom2d.Vector2D;
import android.graphics.Point;

public class GamePhysics {
	public class Rect {
		private float left, top, right, bottom;
		public Rect(float left, float top, float right, float bottom) {
			this.left = left;
			this.top = top;
			this.right = right;
			this.bottom = bottom;
		}
		
		public float left() { return this.left; }		
		public float top() { return this.top; }	
		public float right() { return this.right; }	
		public float bottom() { return this.bottom; }
	}
	
	public class Line {
		private Point a, b;
		
		public Line(Point a, Point b) {
			this.a = new Point(a.x, a.y);
			this.b = new Point(b.x, b.y);
		}
		
		public Point getA() {
			return new Point(a.x, a.y);
		}
		
		public Point getB() {
			return new Point(b.x, b.y);
		}
	}
	
	public class Circle {
		private Point c;
		private double radius;
		
		public Circle(Point c, double radius) {
			this.c = new Point(c.x, c.y);
			this.radius = radius;
		}
		
		public Point getC() {
			return new Point(c.x, c.y);
		}
		
		public double getRadius() {
			return this.radius;
		}
	}
	
	public static boolean colliding(Circle circ, Rect rect) {
		Point c = circ.getC();
		return (c.x > rect.left() && c.x < rect.right() && c.y > rect.top() && c.y < rect.bottom());
	}
	
	public static boolean colliding(Circle circ, Line line) {
		Point a = line.getA(), b = line.getB(); Point c = circ.getC();
    	double L = Math.sqrt(Math.pow(b.x - a.x, 2) + Math.pow(b.y - a.y, 2));
    	double r = ((a.y - c.y)*(a.y - b.y) - (a.x - c.x)*(b.x - a.x))/(L*L);
    	double s = ((a.y - c.y)*(b.x - a.x) - (a.x - c.x)*(b.y - a.y))/(L*L);
//    	PointF i = new PointF((float)(a.x + r*(b.x - a.x)), (float)(a.y + r*(b.y - a.y)));
    	double dist = s*L;
    	
    	if(dist < circ.getRadius() && 0 <= r && r <= 1) return true;
    	
		return false;
	}
	
	public static boolean colliding(Circle a, Circle b) {
		Point ac = a.getC(), bc = b.getC();
		return Math.pow(ac.x - bc.x, 2) + Math.pow(ac.y - bc.y, 2) <= Math.pow(a.getRadius() + b.getRadius(), 2);
	}
	
	public static double reflect(Circle circle, Line l) {
		int dx = l.getB().x - l.getA().x;
		int dy = l.getB().y - l.getA().y;
		
		Vector2D ri = (new Vector2D(circle.getC().x, circle.getC().y)).opposite();
		Vector2D normal = (new Vector2D(-dy, dx)).normalize();
		
		Vector2D rr = ri.minus((normal.times(2).times(ri.dot(normal))));
		return rr.angle();
	}
}
