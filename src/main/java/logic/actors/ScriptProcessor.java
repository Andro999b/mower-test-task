package logic.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import data.*;
import lombok.Value;

import java.util.Iterator;

public class ScriptProcessor {
    interface Command {
    }

    @Value
    public static class RunScript implements Command {
        Script script;
    }

    @Value
    public static class CheckResult implements Command {
        boolean occupied;
    }

    private final ActorContext<Command> context;
    private final IntersectionGrid grid;
    private final ActorRef<Root.Command> root;
    private final Lawn lawn;

    public ScriptProcessor(ActorContext<Command> context, IntersectionGrid grid, ActorRef<Root.Command> root, Lawn lawn) {
        this.context = context;
        this.grid = grid;
        this.root = root;
        this.lawn = lawn;
    }

    public static Behavior<Command> create(
            Lawn lawn,
            IntersectionGrid grid,
            ActorRef<Root.Command> root
    ) {
        return Behaviors.setup((ctx) -> new ScriptProcessor(ctx, grid, root, lawn).awaitRunScript());
    }

    private Behavior<Command> awaitRunScript() {
        return Behaviors
                .receive(Command.class)
                .onMessage(RunScript.class, runScript -> {
                    var script = runScript.getScript();
                    context.getLog().info("Start script: {}", script);
                    return calcNextPosition(
                            new Mower(script.getMowerId(), script.getInitialPosition()),
                            script.getActions().iterator()
                    );
                })
                .build();
    }

    private Behavior<Command> calcNextPosition(Mower mower, Iterator<Action> actionIterator) {
        try {
            context.getLog().info("Calc next position for mower {}", mower);

            var calculatedPosition = mower.getPosition();

            while (actionIterator.hasNext()) {
                var action = actionIterator.next();

                var nextPosition = calculatedPosition.applyAction(action);
                if (action.isMoveAction()) {
                    if (lawn.isInBounds(nextPosition)) { // check if new position in lawn
                        calculatedPosition = nextPosition;
                        break;
                    }// else just continue cycle
                } else {
                    calculatedPosition = nextPosition;// just rotation
                }
            }

            if (calculatedPosition.getX() == mower.getPosition().getX() &&
                    calculatedPosition.getY() == mower.getPosition().getY()) { // not moved
                var finalMower = mower.move(calculatedPosition);
                context.getLog().info("Mower done {}", finalMower);
                root.tell(new Root.Done(finalMower));
                return Behaviors.empty();
            }

            context.getLog().info("Check new mower position {}, {}", mower, calculatedPosition);

            //check intersection
            grid
                    .getChecker(calculatedPosition)
                    .tell(
                            new IntersectionChecker.CheckRequest(mower.getPosition(), calculatedPosition, context.getSelf())
                    );

            return awaitCheckResponse(mower, actionIterator, calculatedPosition);
        } catch (Throwable t) {
            context.getLog().error("Script Processor fail for mower {}", mower, t);
            root.tell(new Root.Done(mower));
            return Behaviors.stopped();
        }
    }

    private Behavior<Command> awaitCheckResponse(Mower mower, Iterator<Action> actionIterator, Position position) {
        return Behaviors
                .receive(Command.class)
                .onMessage(CheckResult.class, (result) -> {
                    if (result.occupied) // do not apply new position
                        return calcNextPosition(mower, actionIterator);
                    else
                        return calcNextPosition(mower.move(position), actionIterator);
                })
                .build();
    }
}
