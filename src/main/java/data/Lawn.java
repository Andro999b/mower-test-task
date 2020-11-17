package data;

import lombok.Value;

@Value
public class Lawn {
    int bottom;
    int top;
    int left;
    int right;

    public boolean isInBounds(Position position) {
        if(position.getY() < bottom) return false;
        if(position.getX() < left) return false;
        if(position.getY() > top) return false;
        return position.getX() <= right;
    }
}
