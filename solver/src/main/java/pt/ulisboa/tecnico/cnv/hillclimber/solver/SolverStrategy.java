package pt.ulisboa.tecnico.cnv.hillclimber.solver;

public interface SolverStrategy {

    void solve(final Solver sol);

    @Override
    String toString();
}
