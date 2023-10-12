import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;

/**
 * A multi-segment Shape, with straight lines connecting "joint" points -- (x1,y1) to (x2,y2) to (x3,y3) ...
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Spring 2016
 * @author CBK, updated Fall 2016
 */
public class Polyline implements Shape {
	// TODO: YOUR CODE HERE
	ArrayList<Point> points = new ArrayList<>();
	Color color;

	String type = "polyline";

	public String getType(){
		return type;
	}

	public Polyline(ArrayList<Point> points, Color color){
		this.color = color;
		this.points = points;
		this.points.addAll(points);
	}
	@Override
	public void moveBy(int dx, int dy) {
		// change the position of the points of the polyline
		for(Point point: points){
			point.x+=dx;
			point.y+=dy;
		}
	}

	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public void setColor(Color color) {
		this.color = color;
	}
	
	@Override
	public boolean contains(int x, int y) {
		// use the distance
		Segment segment;
		for (int i = 1; i < points.size(); i++){
			segment = new Segment(points.get(i-1).x,points.get(i-1).y,points.get(i).x,points.get(i).y, color);
			if(segment.contains(x, y)){
				return true;
			}
		}

		return false;
	}

	@Override
	public void draw(Graphics g){
		System.out.println(points);
		g.setColor(color);
		if(points.size() == 0 || points.size() == 1){
			System.out.println("Please drag over a distance");
		} else{
			// draw as you go
			for(int i = 1; i<points.size(); i++){
				g.drawLine(points.get(i-1).x, points.get(i-1).y, points.get(i).x, points.get(i).y);
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder pointsString = new StringBuilder();
		for (Point point : points) {
			pointsString.append(point.x).append(" ");
			pointsString.append(point.y);
		}
		return "freehand "+pointsString+" "+color.getRGB();
	}
}
