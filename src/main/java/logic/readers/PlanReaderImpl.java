package logic.readers;

import data.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PlanReaderImpl implements PlanReader {
    public final static Pattern LAWN_PATTERN = Pattern.compile("([0-9]+) ([0-9]+)");
    public final static Pattern POSITION_PATTERN = Pattern.compile("([0-9]+) ([0-9]+) ([NSEW])");

    @Override
    public Plan read(InputStream inputStream) {
        var buffer = new BufferedReader(new InputStreamReader(inputStream));

        List<String> lines = buffer.lines().collect(Collectors.toList());

        if(lines.isEmpty()) throw new IllegalArgumentException("Script file is empty");

        int lineNum = 0;

        var lawn = readLawn(lines.get(lineNum++));

        var mower = 0;
        var scripts = new ArrayList<Script>();

        while (lineNum < lines.size()) {
            scripts.add(new Script(
                    mower++,
                    readPosition(lines.get(lineNum++)),
                    readActions(lines.get(lineNum++))
            ));
        }

        return new Plan(lawn, scripts);
    }

    private List<Action> readActions(String line) {
        if(line.isBlank())
            return Collections.emptyList();

        return line
                .trim()
                .chars()
                .mapToObj(ch -> Action.fromChar((char) ch))
                .collect(Collectors.toList());
    }

    private Position readPosition(String line) {
        if(line.isBlank())
            throw new IllegalArgumentException("Mower position did not set");

        var matcher = POSITION_PATTERN.matcher(line.trim());

        if(!matcher.matches())
            throw new IllegalArgumentException("Incorrect position size format");

        return new Position(
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)),
                Direction.valueOf(matcher.group(3))
        );
    }

    private Lawn readLawn(String line) {
        if(line.isBlank())
            throw new IllegalArgumentException("Lawn size did not set");

        var matcher = LAWN_PATTERN.matcher(line.trim());

        if(!matcher.matches())
            throw new IllegalArgumentException("Incorrect laws size format");

        return new Lawn(
                0,
                Integer.parseInt(matcher.group(1)),
                0,
                Integer.parseInt(matcher.group(2))
        );
    }
}
