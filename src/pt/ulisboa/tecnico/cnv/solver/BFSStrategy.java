package pt.ulisboa.tecnico.cnv.solver;

import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingQueue;

public class BFSStrategy extends AbstractSolverStrategy {
    public BFSStrategy() {
        super.name = SolverFactory.SolverType.BFS.toString();
    }

    @Override
    public void solve(final Solver sol) {

        final LinkedBlockingQueue<Coordinate> queue = new LinkedBlockingQueue<Coordinate>();
        final Stack<Coordinate> solutionPathStack = new Stack<>();

        queue.add(new Coordinate(sol.getStartX(), sol.getStartY()));

        while(!queue.isEmpty()) {
            final Coordinate c = queue.remove();
            solutionPathStack.push(c);

            /*
            if(sol.isDebugging()) {
                System.out.println(String.format("> Pushed (%d, %d).", c.getX(), c.getY()));
            }
            */

            sol.setPos(c.getX(), c.getY(), Solver.EXPLORED);

            if(sol.isSolution(c)) {
                break;
            }
            else {
                final List<Coordinate> neighbors = c.getUnvisitedNeighboors(sol);
                if(neighbors.size() == 0) {
                    // If we are not expanding the current cell and it wasn't the solution, discard it.
                    solutionPathStack.pop();

                    /*
                    if(sol.isDebugging()) {
                        System.out.println(String.format("> Popped (%d, %d).", c.getX(), c.getY()));
                    }
                    */
                }
                else {

                    /*
                    if(sol.isDebugging()) {
                        System.out.println(String.format("> Point (%d, %d) had %d neighbors.", c.getX(), c.getY(), neighbors.size()));
                    }
                    */

                    for (final Coordinate local : neighbors) {
                        if( ! sol.isQueued(local.getX(), local.getY())) {
                            sol.setPos(local.getX(), local.getY(), Solver.QUEUED);
                            queue.add(local);
                        }
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
