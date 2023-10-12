import java.awt.*;
import java.awt.color.CMMException;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Handles communication to/from the server for the editor
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012
 * @author Chris Bailey-Kellogg; overall structure substantially revised Winter 2014
 * @author Travis Peters, Dartmouth CS 10, Winter 2015; remove EditorCommunicatorStandalone (use echo server for testing)
 */
public class EditorCommunicator extends Thread {
	private PrintWriter out;		// to server
	private BufferedReader in;		// from server
	protected Editor editor;		// handling communication for
	String msg = "";

	/**
	 * Establishes connection and in/out pair
	 */
	public EditorCommunicator(String serverIP, Editor editor) {
		this.editor = editor;
		System.out.println("connecting to " + serverIP + "...");
		try {
			Socket sock = new Socket(serverIP, 4242);
			out = new PrintWriter(sock.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			System.out.println("...connected");
		}
		catch (IOException e) {
			System.err.println("couldn't connect");
			System.exit(-1);
		}
	}

	/**
	 * Sends message to the server
	 */
	public void send(String msg) {
		out.println(msg);
	}
	/**
	 * Keeps listening for and handling (your code) messages from the server
	 */
	public void run() {

		try {
			System.out.println("here");

			String mess = in.readLine();
			while(mess!=null){
				System.out.println(mess);
				String Command = mess.split(" ")[0];
				System.out.println(Command);
				if(Command.equals("Move")){
					int id = Integer.parseInt(mess.split(" ")[1]);
					Shape shape = editor.shapeByID(id);
					System.out.println(mess);
					shape.moveBy(Integer.parseInt(mess.split(" ")[2]), Integer.parseInt(mess.split(" ")[3]));

				} else if(Command.equals("Draw")){
					String type = mess.split(" ")[1];
					int x1 = Integer.parseInt(mess.split(" ")[2]);
					int y1 = Integer.parseInt(mess.split(" ")[3]);
					int x2 = Integer.parseInt(mess.split(" ")[4]);
					int y2 = Integer.parseInt(mess.split(" ")[5]);
					Color color = new Color(Integer.parseInt(mess.split(" ")[6]));

					if(type.equals("ellipse")){
						editor.getSketch().add(new Ellipse(x1, y1, x2, y2, color));
					}
					else if(type.equals("rectangle")){
						editor.getSketch().add(new Rectangle(x1, y1, x2, y2, color));
					} else if(type.equals("segment")){
						editor.getSketch().add(new Segment(x1, y1, x2, y2, color));
					} else if(type.equals("freehand")){
						ArrayList<Point> data = new Message(mess).points;
						System.out.println("Data" + data);
						Color c = new Color(Integer.parseInt(mess.split(" ")[2]));
						Polyline toAdd = new Polyline(data, c);
						System.out.println("HERE" + toAdd.points);
						editor.getSketch().add(toAdd); // I am adding points to newly drawn feautre, but it is not being pronted
					}

				} else if(Command.equals("Recolor")){
					int id = Integer.parseInt(mess.split(" ")[1]);
					Color color = new Color(Integer.parseInt(mess.split(" ")[2]));
					Shape shape = editor.shapeByID(id);
					shape.setColor(color);

				} else if(Command.equals("Delete")){
					int id = Integer.parseInt(mess.split(" ")[1]);
					Shape shape = editor.shapeByID(id);
					editor.getSketch().getMap().replace(id, null);
				}
				editor.repaint();
				mess = in.readLine();
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			Person Destin  = new Person("Destion", "Musanze", 19);
			System.out.println(Destin.name);
		}
		finally {
			System.out.println("server hung up");
		}
	}

}
