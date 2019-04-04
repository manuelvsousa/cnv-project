package pt.ulisboa.tecnico.cnv.generator;

public abstract class AbstractGeneratorStrategy implements GeneratorStrategy {
    public abstract void generate(Generator gen);

    protected String name;
    @Override
    public String toString() {
        return this.name;
    }
}
