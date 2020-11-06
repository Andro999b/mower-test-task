package data;

import lombok.Value;

@Value
public class Mower {
    int id;
    Position position;

    public Mower move(Position position) {
        return new Mower(id, position);
    }
}
