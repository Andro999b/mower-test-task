package logic.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.ActorContext;
import data.Lawn;
import data.Position;

import java.util.ArrayList;
import java.util.List;

public class IntersectionGrid {

    private static final int SEGMENT_SIZE = 5;

    private List<List<ActorRef<IntersectionChecker.Command>>> checkers;
    private final int w;
    private final int h;

    private IntersectionGrid(List<List<ActorRef<IntersectionChecker.Command>>> checkers, int w, int h) {
        this.checkers = checkers;
        this.w = w;
        this.h = h;
    }

    public static IntersectionGrid create(ActorContext<Root.Command> context, Lawn lawn) {
        var w = (int) Math.ceil((lawn.getRight() + 1) / (double) SEGMENT_SIZE); // lawn top inclusive
        var h = (int) Math.ceil((lawn.getTop() + 1) / (double) SEGMENT_SIZE);

        var columns = new ArrayList<List<ActorRef<IntersectionChecker.Command>>>();
        var grid = new IntersectionGrid(columns, w, h);

        for (int i = 0; i < w; i++) {
            var rows = new ArrayList<ActorRef<IntersectionChecker.Command>>();
            for (int j = 0; j < h; j++) {
                rows.add(context.spawn(
                        IntersectionChecker.create(grid, new Lawn(j * SEGMENT_SIZE, i * SEGMENT_SIZE)),
                        "intersection-checker-" + i + "-" + j)
                );
            }
            columns.add(rows);
        }

        return grid;
    }

    public ActorRef<IntersectionChecker.Command> getChecker(Position position) {
        var segw = (int) Math.floor(position.getX() / (double) SEGMENT_SIZE);
        var segh = (int) Math.floor(position.getY() / (double) SEGMENT_SIZE);

        if (segh >= h || segw >= w || segh < 0 || segw < 0)
            throw new IllegalArgumentException("Position out of bounds");

        return checkers.get(segw).get(segh);
    }
}
