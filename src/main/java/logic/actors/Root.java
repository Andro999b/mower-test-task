package logic.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Routers;
import data.Mower;
import data.Plan;
import lombok.Value;

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;

public final class Root {

    public interface Command { }
    public interface Reply { }

    @Value
    public static class Mowers implements Reply {
        Collection<Mower> mowers;
    }

    @Value
    public static class Run implements Command {
        Plan plan;
        ActorRef<Reply> replyTo;
    }

    @Value
    public static class Done implements Command {
        Mower mower;
    }

    private final ActorContext<Command> context;

    public Root(ActorContext<Root.Command> context) {
        this.context = context;
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(ctx -> new Root(ctx).run());
    }

    private Behavior<Command> run() {
        return Behaviors
                .receive(Command.class)
                .onMessage(Run.class, (init) -> {
                    var plan = init.plan;
                    var intersectionChecker = context.spawn(IntersectionChecker.create(), "intersectionChecker");
                    var router =
                            Routers.pool(
                                    plan.getScripts().size(),
                                    ScriptProcessor.create(
                                            plan.getLawn(),
                                            intersectionChecker,
                                            context.getSelf()
                                    )
                            ).withRoundRobinRouting();

                    var scriptProcessor = context.spawn(router, "scriptProcessor");

                    plan.getScripts()
                            .stream()
                            .map(ScriptProcessor.RunScript::new)
                            .forEach(scriptProcessor::tell);

                    return awaitDone(new TreeSet<>(Comparator.comparingInt(Mower::getId)), plan.getScripts().size(), init.replyTo);
                })
                .build();
    }

    private Behavior<Command> awaitDone(Collection<Mower> doneMowers, int mowersLeft, ActorRef<Reply> replyTo) {

        if(mowersLeft == 0) {
            replyTo.tell(new Mowers(doneMowers));
            return Behaviors.stopped();
        }

        return Behaviors
                .receive(Command.class)
                .onMessage(Done.class, done -> {
                    doneMowers.add(done.mower);
                    return awaitDone(doneMowers, mowersLeft - 1, replyTo);
                })
                .build();

    }
}
