package pt.ulisboa.tecnico.cnv.solver;

public interface SolverStrategy {

    void solve(final Solver sol);

    @Override
    String toString();
}
