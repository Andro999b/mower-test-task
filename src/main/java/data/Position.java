package data;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
public class Position {
    int x;
    int y;
    @EqualsAndHashCode.Exclude
    Direction direction;

    public Position applyAction(Action action) {
        var direction = this.direction;
        var x = this.x;
        var y = this.y;
        var delta = 0;

        switch (action) {
            case F:
                delta = 1;
                break;
            case B:
                delta = -1;
                break;
            case L:
                direction = direction.rotateLeft();
                break;
            case R:
                direction = direction.rotateRight();
                break;
        }

        if(delta != 0) {
            switch (direction) {
                case N:
                    y += delta;
                    break;
                case S:
                    y -= delta;
                    break;
                case E:
                    x += delta;
                    break;
                case W:
                    x -= delta;
                    break;
            }
        }

        return new Position(x, y, direction);
    }
}
