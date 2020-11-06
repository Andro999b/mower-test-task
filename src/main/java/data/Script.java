package data;

import lombok.Value;

import java.util.List;

@Value
public class Script {
    int mowerId;
    Position initialPosition;
    List<Action> actions;
}
