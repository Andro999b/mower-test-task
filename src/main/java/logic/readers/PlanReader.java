package logic.readers;

import data.Plan;
import data.Script;

import java.io.InputStream;
import java.util.Collection;

public interface PlanReader {
    Plan read(InputStream inputStream);
}
