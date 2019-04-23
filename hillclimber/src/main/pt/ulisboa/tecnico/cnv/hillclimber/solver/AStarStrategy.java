package pt.ulisboa.tecnico.cnv.solver;

import java.util.*;

public class AStarStrategy extends AbstractSolverStrategy {

    private Node[][] nodesByCoord;



    private class Node {

        public Node cameFrom;
        private Coordinate coordinate;
        private double totalCost, costToArrive, costToGoal;

        public Node(int x, int y, double totalCost, double costToArrive, double costToGoal) {
            this.coordinate = new Coordinate(x, y);
            this.totalCost = totalCost;
            this.costToArrive = costToArrive;
            this.costToGoal = costToGoal;
            this.cameFrom = null;
        }

        public int getX() {
            return this.coordinate.getX();
        }

        public int getY() {
            return this.coordinate.getY();
        }

        public double linearDistance(Node node) {
            double c1 = 0, c2 = 0;
            if(this.getX() >= node.getX()) {
                c1 = this.getX() - node.getX();
            } else {
                c1 = node.getX() - this.getX();
            }

            if(this.getY() >= node.getY()) {
                c2 = this.getY() - node.getY();
            } else {
                c2 = node.getY() - this.getY();
            }

            return Math.sqrt(Math.pow(c1, 2) + Math.pow(c2, 2));
        }

        List<Node> getNeighboors(final Solver sol) {
            final List<Coordinate> resCoordinate = this.coordinate.getAllNeighboors(sol);
            final List<Node> resNode = new LinkedList<Node>();

            for(final Coordinate c : resCoordinate) {
                if(nodesByCoord[c.getX()][c.getY()] != null) {
                   resNode.add(nodesByCoord[c.getX()][c.getY()]);
                }
                else {
                    final Node newNode = new Node(c.getX(), c.getY(), 0, 0, 0);
                    resNode.add(newNode);
                    nodesByCoord[c.getX()][c.getY()] = newNode;
                }
            }

            return resNode;
        }
    }

    private class NodeComparator implements Comparator<Node> {

        @Override
        public int compare(Node node1, Node node2) {
            return Double.compare(node1.totalCost, node2.totalCost);
        }

    }

    public AStarStrategy() {
        super.name = SolverFactory.SolverType.ASTAR.toString();
    }

    @Override
    public void solve(final Solver sol) {


        this.nodesByCoord = new Node[sol.getWidth()][sol.getHeight()];
        for(int i = 0; i < sol.getWidth(); i++) {
            for(int j = 0; j < sol.getHeight(); j++) {
                this.nodesByCoord[i][j] = null;
            }
        }

        // https://en.wikipedia.org/wiki/A*_search_algorithm
        final Node finalNode = new Node(sol.getTargetX(), sol.getTargetY(), 0, 0, 0);

        final HashSet<Node> openMap = new HashSet<Node>();
        final HashSet<Node> closedMap = new HashSet<Node>();

        // https://docs.oracle.com/javase/7/docs/api/java/util/PriorityQueue.html
        final NodeComparator nc = new NodeComparator();
        final PriorityQueue<Node> pq = new PriorityQueue<>(1, nc);

        final Node initialNode = new Node(sol.getStartX(), sol.getStartY(), 0, 0, 0);
        pq.add(initialNode);
        this.nodesByCoord[initialNode.getX()][initialNode.getY()] = initialNode;
        openMap.add(initialNode);

        Node solutionNode = null;

        while( ! pq.isEmpty()) {
            final Node examiningNode = pq.poll();
            openMap.remove(examiningNode);

            final List<Node> neighbors = examiningNode.getNeighboors(sol);

            for(final Node neighbor: neighbors) {

                // Termination condition.
                if(sol.isSolution(neighbor.coordinate)) {
                    sol.setPos(neighbor.getX(), neighbor.getY(), Solver.EXPLORED);
                    solutionNode = neighbor;
                    neighbor.cameFrom = examiningNode;
                    break;
                }

                // If the current neighbor was already closed, continue.
                if(closedMap.contains(neighbor)) {
                    continue;
                }

                final double tentativeScore = examiningNode.costToArrive + neighbor.linearDistance(examiningNode);

                if( ! openMap.contains(neighbor)) {
                    // Update neighbor distance values.
                    neighbor.costToArrive = tentativeScore;
                    neighbor.costToGoal = finalNode.linearDistance(neighbor);
                    neighbor.totalCost = neighbor.costToArrive + neighbor.costToGoal;
                    neighbor.cameFrom = examiningNode;

                    openMap.add(neighbor);
                    pq.add(neighbor);
                }
                else if(tentativeScore >= neighbor.costToArrive) {
                    continue;
                }
            }


            // We saw all neighbors of `examiningNode`, we can consider it closed now.
            closedMap.add(examiningNode);
            sol.run();
        }

        // Store the solution path from the starting position to the target position.
        Node node = solutionNode;
        while( node != null)  {
            sol.getPath().add(node.coordinate);
            node = node.cameFrom;
        }

        Collections.reverse(sol.getPath());
    }
}
