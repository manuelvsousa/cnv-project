package pt.ulisboa.tecnico.cnv.solver;

import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class DFSStrategy extends AbstractSolverStrategy {
    public DFSStrategy() {
        super.name = SolverFactory.SolverType.DFS.toString();
    }

    @Override
    public void solve(final Solver sol) {

        final Stack<Coordinate> dfsStack = new Stack<>();
        final Stack<Coordinate> solutionPathStack = new Stack<>();

        final Coordinate startCoord = new Coordinate(sol.getStartX(), sol.getStartY());

        dfsStack.push(startCoord);

        while( ! dfsStack.isEmpty()) {
            final Coordinate c = dfsStack.pop();
            solutionPathStack.push(c);
            sol.setPos(c.getX(), c.getY(), Solver.EXPLORED);

            // Have we arrived at the solution?
            if(sol.isSolution(c)) {
                break;
            }
            else {
                final List<Coordinate> neighbors = c.getUnvisitedNeighboors(sol);
                if(neighbors.size() == 0) {
                    // If we are not expanding the current cell and it wasn't the solution, discard it.
                    solutionPathStack.pop();
                }
                else {
                    for (final Coordinate local : neighbors) {
                        dfsStack.push(local);
                    }
                }
            }

            sol.run();
        }

        // Store the solution path from the starting position to the target position.
        while( ! solutionPathStack.empty()) {
            final Coordinate solutionCoordinate = solutionPathStack.pop();
            sol.getPath().add(solutionCoordinate);
        }
        Collections.reverse(sol.getPath());
    }
}
