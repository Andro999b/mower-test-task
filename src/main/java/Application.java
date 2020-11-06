import akka.actor.typed.ActorSystem;
import data.Plan;
import logic.actors.Root;
import logic.readers.PlanReader;
import logic.readers.PlanReaderImpl;

import java.io.InputStream;

public class Application {

    public static void main(String[] args) {
        PlanReader reader = new PlanReaderImpl();
        InputStream inputStream = Application.class.getClassLoader().getResourceAsStream("plan.txt");
        Plan plan = reader.read(inputStream);

        ActorSystem.create(Root.create(plan), "root");
    }
}
