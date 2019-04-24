package pt.ulisboa.tecnico.cnv.hillclimber.util;

public interface ArgumentParser {
    void parseValues(final String[] args);
    void setupCLIOptions();
}
