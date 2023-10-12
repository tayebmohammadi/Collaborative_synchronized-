import java.util.ArrayList;
import java.util.List;
import java.awt.*;
import java.awt.event.*;
import java.util.Map;
import java.util.Objects;

import javax.swing.*;

/**
 * Client-server graphical editor
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; loosely based on CS 5 code by Tom Cormen
 * @author CBK, winter 2014, overall structure substantially revised
 * @author Travis Peters, Dartmouth CS 10, Winter 2015; remove EditorCommunicatorStandalone (use echo server for testing)
 * @author CBK, spring 2016 and Fall 2016, restructured Shape and some of the GUI
 */

public class Editor extends JFrame {	
	private static String serverIP = "localhost";			// IP address of sketch server
	// "localhost" for your own machine;
	// or ask a friend for their IP address

	private static final int width = 800, height = 800;		// canvas size

	// Current settings on GUI
	public enum Mode {
		DRAW, MOVE, RECOLOR, DELETE
	}
	private Mode mode = Mode.DRAW;				// drawing/moving/recoloring/deleting objects
	private String shapeType = "ellipse";		// type of object to add
	private Color color = Color.black;			// current drawing color
	ArrayList<Point> points = new ArrayList<>();

	// Drawing state
	// these are remnants of my implementation; take them as possible suggestions or ignore them
	private Shape curr = null;					// current shape (if any) being drawn
	private Sketch sketch = new Sketch();						// holds and handles all the completed objects
	private int movingId = -1;					// current shape id (if any; else -1) being moved
	private Point drawFrom = null;				// where the drawing started
	private Point moveFrom = null;				// where object is as it's being dragged
	private Point theFirstPoint = null;				// where object is as it's being dragged


	// Communication
	private EditorCommunicator comm;			// communication with the sketch server

	public Editor() {
		super("Graphical Editor");
		sketch = new Sketch();
		// Connect to server
		comm = new EditorCommunicator(serverIP, this);
		comm.start();

		// Helpers to create the canvas and GUI (buttons, etc.)
		JComponent canvas = setupCanvas();
		JComponent gui = setupGUI();

		// Put the buttons and canvas together into the window
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		cp.add(canvas, BorderLayout.CENTER);
		cp.add(gui, BorderLayout.NORTH);

		// Usual initialization
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible(true);
	}

	/**
	 * Creates a component to draw into
	 */
	private JComponent setupCanvas() {
		JComponent canvas = new JComponent() {
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				drawSketch(g);
			}
		};
		
		canvas.setPreferredSize(new Dimension(width, height));

		canvas.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent event) {
				handlePress(event.getPoint());
			}
			public void mouseReleased(MouseEvent event) {
				handleRelease();
			}
		});		

		canvas.addMouseMotionListener(new MouseAdapter() {
			public void mouseDragged(MouseEvent event) {
				handleDrag(event.getPoint());
			}
		});
		
		return canvas;
	}

	/**
	 * Creates a panel with all the buttons
	 *
	 */
	private JComponent setupGUI() {
		// Select type of shape
		String[] shapes = {"ellipse", "freehand", "rectangle", "segment"};
		JComboBox<String> shapeB = new JComboBox<String>(shapes);
		shapeB.addActionListener(e -> shapeType = (String)((JComboBox<String>)e.getSource()).getSelectedItem());

		// Select drawing/recoloring color
		// Following Oracle example
		JButton chooseColorB = new JButton("choose color");
		JColorChooser colorChooser = new JColorChooser();
		JLabel colorL = new JLabel();
		colorL.setBackground(Color.black);
		colorL.setOpaque(true);
		colorL.setBorder(BorderFactory.createLineBorder(Color.black));
		colorL.setPreferredSize(new Dimension(25, 25));
		JDialog colorDialog = JColorChooser.createDialog(chooseColorB,
				"Pick a Color",
				true,  //modal
				colorChooser,
				e -> { color = colorChooser.getColor(); colorL.setBackground(color); },  // OK button
				null); // no CANCEL button handler
		chooseColorB.addActionListener(e -> colorDialog.setVisible(true));

		// Mode: draw, move, recolor, or delete
		JRadioButton drawB = new JRadioButton("draw");
		drawB.addActionListener(e -> mode = Mode.DRAW);
		drawB.setSelected(true);
		JRadioButton moveB = new JRadioButton("move");
		moveB.addActionListener(e -> mode = Mode.MOVE);
		JRadioButton recolorB = new JRadioButton("recolor");
		recolorB.addActionListener(e -> mode = Mode.RECOLOR);
		JRadioButton deleteB = new JRadioButton("delete");
		deleteB.addActionListener(e -> mode = Mode.DELETE);
		ButtonGroup modes = new ButtonGroup(); // make them act as radios -- only one selected
		modes.add(drawB);
		modes.add(moveB);
		modes.add(recolorB);
		modes.add(deleteB);
		JPanel modesP = new JPanel(new GridLayout(1, 0)); // group them on the GUI
		modesP.add(drawB);
		modesP.add(moveB);
		modesP.add(recolorB);
		modesP.add(deleteB);

		JComponent gui = new JPanel();
		gui.setLayout(new FlowLayout());
		gui.add(shapeB);
		gui.add(chooseColorB);
		gui.add(colorL);
		gui.add(modesP);
		return gui;
	}

	/**
	 * Getter for the sketch instance variable
	 */
	public Sketch getSketch() {
		return sketch;
	}

	/**
	 * Draws all the shapes in the sketch,
	 * along with the object currently being drawn in this editor (not yet part of the sketch)
	 */
	public void drawSketch(Graphics g) {
		// TODO: YOUR CODE HERE
		for(Map.Entry<Integer, Shape> shapes: sketch.getMap().entrySet()) {
			if(shapes.getValue() != null){
			shapes.getValue().draw(g);
			System.out.println("drawing");}
		}
		if(curr != null){
			curr.draw(g);
			System.out.println("drawing 2");
		}

	}
	/**
	 *get the shape ID
	 * @param p the point clicked
	 */
	public int currentShapeID(Point p){
		ArrayList<Integer> keys = new ArrayList<>(Sketch.sketches.descendingKeySet());
		for(int i = 0; i<keys.size(); i++){
			if(Sketch.sketches.get(i) != null && Sketch.sketches.get(i).contains(p.x, p.y)){
			return i;
			}
		}
		return -1;
	}

	/**
	 *get the shape referenced by ID
	 * @param id the id of hte name
	 */
	public Shape shapeByID(int id){
		for(Map.Entry<Integer, Shape> record: sketch.getMap().entrySet()){
			if(record.getKey() == id && record.getValue() != null){
				return record.getValue();
			}
		}
		return null;
	}
	// Helpers for event handlers
	
	/**
	 * Helper method for press at point
	 * In drawing mode, start a new object;
	 * in moving mode, (request to) start dragging if clicked in a shape;
	 * in recoloring mode, (request to) change clicked shape's color
	 * in deleting mode, (request to) delete clicked shape
	 */
	private void handlePress(Point p) {
		System.out.println(shapeType);
		// TODO: YOUR CODE HERE
		if(mode == Mode.DRAW){
			drawFrom = p;
			if(shapeType.equals("ellipse")){
				System.out.println("creating ellipse");
				curr = new Ellipse(p.x, p.y, color);
			}
			else if(shapeType.equals("rectangle")){
				curr = new Rectangle(p.x, p.y, p.x, p.y, color);
			}
			else if(shapeType.equals("freehand")){
				points.add(p);
				curr = new Polyline(points, color);
				System.out.println(curr.toString());
			}
			else if(shapeType.equals("segment")){
				curr = new Segment(p.x, p.y, p.x,p.y, color); // two points								//and thisssss
			}
		}
		else if(mode == Mode.MOVE){
			if(curr != null){
			moveFrom = p;
			theFirstPoint = p;}
		}
		else if(mode == Mode.RECOLOR){

			curr = sketch.getMap().get(currentShapeID(p));
			curr.setColor(color);
			System.out.println(color.getRGB());
			comm.send("Recolor "+currentShapeID(p)+" "+color.getRGB()); 					//send request to recolor a shape
		}
		else if(mode == Mode.DELETE){
			comm.send("Delete "+currentShapeID(p)+""); 						// send request to delete a shape=
		}
		repaint();
	}

	/**
	 * Helper method for drag to new point
	 * In drawing mode, update the other corner of the object;
	 * in moving mode, (request to) drag the object
	 */
	private void handleDrag(Point p) {
		// TODO: YOUR CODE HERE
		if (mode == Mode.DRAW){
			if(Objects.equals(shapeType, "ellipse")){
				Ellipse theEllipse = (Ellipse) curr;
				theEllipse.setCorners(drawFrom.x, drawFrom.y, p.x, p.y);
			}
			else if(Objects.equals(shapeType, "rectangle")){
				Rectangle theRectangle = (Rectangle) curr;
				theRectangle.setCorners(drawFrom.x, drawFrom.y, p.x, p.y);
			}
			else if(Objects.equals(shapeType, "freehand")){
				Polyline thePolyline = (Polyline) curr;
				thePolyline.points.add(p);
			}
			else if(Objects.equals(shapeType, "segment")){
				Segment theSegment = (Segment) curr;
				theSegment.setEnd(p.x, p.y);
			}
		}
		else if (mode == Mode.MOVE){
			int id = currentShapeID(theFirstPoint);
			comm.send("Move "+id+" "+(p.x-moveFrom.x)+" "+(p.y-moveFrom.y));
			moveFrom = p; // set the point to move from
		}

		repaint();
	}

	/**
	 * Helper method for release
	 * In drawing mode, pass the add new object request on to the server;
	 * in moving mode, release it		
	 */
	private void handleRelease() {
		// TODO: YOUR CODE HERE
		if (mode == Mode.DRAW){
			comm.send("Draw "+ curr.toString()); // send the object to the server
			// request to add the newly added feature, if we are in the mode of drawing
			points.clear();
		}
		else if (mode == Mode.MOVE){
			//Stop dragging.
			moveFrom = null;
		}
		repaint();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new Editor();
			}
		});	
	}

}
