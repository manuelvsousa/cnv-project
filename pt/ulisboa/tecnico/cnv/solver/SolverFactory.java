package pt.ulisboa.tecnico.cnv.solver;

public class SolverFactory {

    public enum SolverType {
        BFS("BFS"),
        DFS("DFS"),
        ASTAR("ASTAR");

        private final String text;
        SolverType(final String text) {
            this.text = text;
        }

        /* (non-Javadoc)
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString() {
            return text;
        }

        static public Boolean isValid(final String strategyString) {

            for (SolverType enumStrat : SolverType.values()) {
                if(enumStrat.toString().equals(strategyString)) {
                    return true;
                }
            }
            return false;
        }
    }

    // Singleton.
    static private SolverFactory instance = null;
    private SolverFactory(){}
    static public SolverFactory getInstance() {
        if(instance == null) {
            instance = new SolverFactory();
        }
        return instance;
    }

    public Solver makeSolver(final SolverArgumentParser ap) {

        final SolverType t = ap.getSolverStrategy();

        if(t == SolverType.BFS) {
            return new Solver(ap, new BFSStrategy());
        }
        else if(t == SolverType.DFS) {
            return new Solver(ap, new DFSStrategy());
        }
        else if(t == SolverType.ASTAR) {
            return new Solver(ap, new AStarStrategy());
        }
        else {
            throw new IllegalArgumentException();
        }
    }
}
