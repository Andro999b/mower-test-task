package data;

import lombok.Value;

@Value
public class Lawn {
    int top;
    int right;

    public boolean isInBounds(Position position) {
        if(position.getY() < 0) return false;
        if(position.getX() < 0) return false;
        if(position.getY() > top) return false;
        return position.getX() <= right;
    }
}
