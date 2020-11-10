import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import data.Plan;
import logic.actors.Root;
import logic.readers.PlanReader;
import logic.readers.PlanReaderImpl;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.time.Duration;
import java.util.concurrent.CompletionStage;

@Slf4j
public class Application {

    public static void main(String[] args) {
        PlanReader reader = new PlanReaderImpl();
        InputStream inputStream = Application.class.getClassLoader().getResourceAsStream("plan.txt");
        Plan plan = reader.read(inputStream);

        var system = ActorSystem.create(Root.create(), "root");

        CompletionStage<Root.Reply> result = AskPattern.ask(
                system,
                reply -> new Root.Run(plan, reply),
                Duration.ofSeconds(3),
                system.scheduler()
        );

        result.whenComplete((reply, failure) -> {
            if (reply instanceof Root.Mowers)
                log.info("Result: {}", ((Root.Mowers) reply).getMowers());
            else
                log.error("Computation filed. ", failure);
        });
    }
}
