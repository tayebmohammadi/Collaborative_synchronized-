import javax.swing.plaf.SliderUI;
import java.awt.*;
import java.util.*;
import java.util.List;

public class Sketch {


    public Sketch(){

    }
    int size = 0;
    static TreeMap<Integer, Shape> sketches = new TreeMap<>();

    public void add(Shape newOne){
        sketches.put(size, newOne);
        size++;
    }

    public Map<Integer, Shape> getMap(){
        return sketches;
    }

    public Shape getShape(int x, int y){
        for(Integer shapeKey: sketches.descendingKeySet()){
            if(sketches.get(shapeKey).contains(x, y)){
                return sketches.get(shapeKey);
            }
        }
        return null;
    }
    public void delete(int x, int y){
        for(Integer shapeKey: sketches.descendingKeySet()){
            if(sketches.get(shapeKey).contains(x, y)){
                sketches.remove(shapeKey);
            }
        }
    }
    public void recolor(int x, int y, Color color){
        getShape(x, y).setColor(color);
    }

    public void move(int x, int y){
        Point moveFrom = null;
        if(getShape(x, y) != null){
            moveFrom = new Point(x, y);
        }
    }
}
