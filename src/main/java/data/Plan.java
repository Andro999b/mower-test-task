package data;

import lombok.Value;

import java.util.Collection;

@Value
public class Plan {
    Lawn lawn;
    Collection<Script> scripts;
}
