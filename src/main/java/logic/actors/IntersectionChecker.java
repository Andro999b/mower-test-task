package logic.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import data.Lawn;
import data.Position;
import lombok.Value;

import java.util.HashSet;
import java.util.Set;

public final class IntersectionChecker extends AbstractBehavior<IntersectionChecker.Command> {

    interface Command {}

    @Value
    public static class CheckRequest implements Command {
        Position currentPosition;
        Position requestPosition;
        ActorRef<ScriptProcessor.Command> scriptProcessor;
    }

    @Value
    private static class FreePosition implements Command {
        Position position;
    }

    private final IntersectionGrid grid;
    private final Lawn lawnPart;
    private final Set<Position> occupiedPositions = new HashSet<>();

    public IntersectionChecker(ActorContext<Command> context, IntersectionGrid grid, Lawn lawnPart) {
        super(context);
        this.grid = grid;
        this.lawnPart = lawnPart;
    }

    public static Behavior<Command> create(IntersectionGrid grid, Lawn lawnPart) {
        return Behaviors
                .<Command>supervise(Behaviors.setup(ctx -> new IntersectionChecker(ctx, grid, lawnPart)))
                .onFailure(SupervisorStrategy.restart());
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(CheckRequest.class, this::onCheckRequest)
                .onMessage(FreePosition.class, this::onFreePosition)
                .build();
    }

    private Behavior<Command> onFreePosition(FreePosition freePosition) {
        occupiedPositions.remove(freePosition.position);
        return this;
    }

    private Behavior<Command> onCheckRequest(CheckRequest checkRequest) {
        var requestPosition = checkRequest.requestPosition;
        var currentPosition = checkRequest.currentPosition;

        if(occupiedPositions.contains(requestPosition)) {
            getContext().getLog().info("Requested position {} already occupied", requestPosition);
            checkRequest.scriptProcessor.tell(new ScriptProcessor.CheckResult(true));
        } else {
            if(lawnPart.isInBounds(currentPosition)) {
                occupiedPositions.remove(currentPosition);
            } else {
                grid.getChecker(currentPosition).tell(new FreePosition(currentPosition));
            }
            occupiedPositions.add(requestPosition);
            checkRequest.scriptProcessor.tell(new ScriptProcessor.CheckResult(false));
        }

        return this;
    }
}
