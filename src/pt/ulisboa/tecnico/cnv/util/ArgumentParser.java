package pt.ulisboa.tecnico.cnv.util;

public interface ArgumentParser {
    void parseValues(final String[] args);
    void setupCLIOptions();
}
