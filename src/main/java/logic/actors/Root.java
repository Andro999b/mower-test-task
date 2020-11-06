package logic.actors;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Routers;
import com.sun.jdi.Bootstrap;
import data.Mower;
import data.Plan;
import lombok.Value;

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;

public final class Root {

    interface Command {
    }

    @Value
    public static class Init implements Root.Command {
        Plan plan;
    }

    @Value
    public static class Done implements Command {
        Mower mower;
    }

    private final ActorContext<Command> context;

    public Root(ActorContext<Root.Command> context) {
        this.context = context;
    }

    public static Behavior<Command> create(Plan plan) {
        return Behaviors.setup(ctx -> new Root(ctx).init(plan));
    }

    private Behavior<Command> init(Plan plan) {
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

        return awaitDone(new TreeSet<>(Comparator.comparingInt(Mower::getId)), plan.getScripts().size());
    }

    private Behavior<Command> awaitDone(Collection<Mower> doneMowers, int mowersLeft) {

        if(mowersLeft == 0) {
            context.getLog().info("{}", doneMowers);
            return Behaviors.stopped();
        }

        return Behaviors
                .receive(Command.class)
                .onMessage(Done.class, done -> {
                    doneMowers.add(done.mower);
                    return awaitDone(doneMowers, mowersLeft - 1);
                })
                .build();

    }
}
