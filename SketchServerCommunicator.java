import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.Map;

/**
 * Handles communication between the server and one client, for SketchServer
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; revised Winter 2014 to separate SketchServerCommunicator
 */
public class SketchServerCommunicator extends Thread {
	private Socket sock;					// to talk with client
	private BufferedReader in;				// from client
	private PrintWriter out;				// to client
	private SketchServer server;			// handling communication for

	public SketchServerCommunicator(Socket sock, SketchServer server) {
		this.sock = sock;
		this.server = server;
	}

	/**
	 * Sends a message to the client
	 * @param msg
	 */
	public void send(String msg) {
		out.println(msg);
	}

	public void handleMess(String line){
		System.out.println("received:  ");
		System.out.println(line);
		String type = "";
		String Command;
		Command = line.split(" ")[0];
		type = line.split(" ")[1];
		System.out.println(type);
		System.out.println(Command);
		if (Command.equals("Draw")) { // why is the Mode private?
			Message received = new Message(line);
			System.out.println(received.points);
			if (type.equals("ellipse")) {
				Ellipse added = new Ellipse(received.points.get(0).x, received.points.get(0).y, received.color);
				server.getSketch().add(added);
				System.out.println(server.getSketch().getMap());
			} else if (type.equals("rectangle")) {
				server.getSketch().add(new Rectangle(received.points.get(0).x,received.points.get(0).y, received.color));
			} else if (type.equals("segment")) {
				System.out.println("Segment");
				server.getSketch().add(new Segment(received.points.get(0).x, received.points.get(0).y, received.color));
			} else if (type.equals("freehand")) {
				System.out.println("freehand");
				System.out.println(received.points);
				server.getSketch().add(new Polyline(received.points, received.color));
			}
//					server.broadcast(in.readLine()); // broadcast the message from one server
		} else if (Command.equals("Move")) {
			int id = Integer.parseInt(line.split(" ")[1]);
			System.out.println(id + "the ID here");
			Shape shape = server.getSketch().getMap().get(id);
			shape.moveBy(Integer.parseInt(line.split(" ")[2]), Integer.parseInt(line.split(" ")[3]));
		} else if (Command.equals("Delete")) {
			int id = Integer.parseInt(line.split(" ")[1]);
			server.getSketch().getMap().remove(id); // just change the shape to null, but keep the id to not change the ids for all other sketches
		} else if (Command.equals("Recolor")) {
			int id = Integer.parseInt(line.split(" ")[1]);
			Color color = new Color(Integer.parseInt(line.split(" ")[2]));
			Shape shape = server.getSketch().getMap().get(id);
			shape.setColor(color);
			server.getSketch().getMap().put(id, shape);
		}
		server.broadcast(line);
	}
	
	/**
	 * Keeps listening for and handling (your code) messages from the client
	 */
	public void run() {
		try {
			System.out.println("someone connected");
			// Communication channel
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintWriter(sock.getOutputStream(), true);

//			 Tell the client the current state of the world
//			 TODO: YOUR CODE HERE

			// Helper method
			if (!Sketch.sketches.isEmpty()){
				String currentState  = "";
				currentState += "Draw ";
				for(Map.Entry<Integer, Shape> record: Sketch.sketches.entrySet()){
					currentState+= (record.getValue().toString()+ " "); // build a string of ids of the shapes
					System.out.println(currentState);
					handleMess(currentState);
				}
			}

///
//			 Keep getting and handling messages from the client
//			 TODO: YOUR CODE HERE
			String line;
			while((line = in.readLine())!=null) {
				handleMess(line);
			}
			// Clean up -- note that also remove self from server's list so it doesn't broadcast here
			server.removeCommunicator(this);
			out.close();
			in.close();
			sock.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
