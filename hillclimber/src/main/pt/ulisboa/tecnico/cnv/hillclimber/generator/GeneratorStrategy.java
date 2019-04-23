package pt.ulisboa.tecnico.cnv.generator;

public interface GeneratorStrategy {

    void generate(Generator gen);

    @Override
    String toString();
}
