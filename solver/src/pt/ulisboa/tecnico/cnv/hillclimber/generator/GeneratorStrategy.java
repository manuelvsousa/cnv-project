package pt.ulisboa.tecnico.cnv.hillclimber.generator;

public interface GeneratorStrategy {

    void generate(Generator gen);

    @Override
    String toString();
}
