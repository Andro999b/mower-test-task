package logic.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import data.Position;
import lombok.Value;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class IntersectionChecker extends AbstractBehavior<IntersectionChecker.Command> {

    interface Command {}

    @Value
    public static class CheckRequest implements Command {
        int mowerId;
        Position position;
        ActorRef<ScriptProcessor.Command> scriptProcessor;
    }

    private final Set<Position> occupiedPositions = new HashSet<>();
    private final Map<Integer, Position> mowerIdToPosition = new HashMap<>();

    public IntersectionChecker(ActorContext<Command> context) {
        super(context);
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(IntersectionChecker::new);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(CheckRequest.class, this::onCheckRequest)
                .build();
    }

    private Behavior<Command> onCheckRequest(CheckRequest checkRequest) {
        var mowerId = checkRequest.mowerId;
        var requestPosition = checkRequest.position;

        if(occupiedPositions.contains(requestPosition)) {
            getContext().getLog().info("Requested position {} for mower {} already occupied", requestPosition, mowerId);
            checkRequest.scriptProcessor.tell(new ScriptProcessor.CheckResult(true));
        } else {
            Position currentPosition = mowerIdToPosition.get(mowerId);
            if(currentPosition != null) occupiedPositions.remove(currentPosition);
            occupiedPositions.add(requestPosition);
            mowerIdToPosition.put(mowerId, requestPosition);
            checkRequest.scriptProcessor.tell(new ScriptProcessor.CheckResult(false));
        }

        return this;
    }
}
