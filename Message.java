import java.awt.*;
import java.util.ArrayList;

public class Message {
    ArrayList<Point> points = new ArrayList<>();
    String type;
    Color color;
    public Message(String message){
        int x =0;
        String[] params = message.split(" ");
        for(int i = 1; i<params.length - 1; i++){
            if(i == 1){
                type = params[i];
            } else if(i%2==0){
                x = Integer.parseInt(params[i]);
            } else{
                points.add(new Point(x, Integer.parseInt(params[i])));
            }
        }
        color = new Color(Integer.parseInt(params[params.length-1]));
    }
}
