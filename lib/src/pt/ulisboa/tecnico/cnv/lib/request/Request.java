package pt.ulisboa.tecnico.cnv.lib.request;

/**
 * Represents a HillClimber request, indicating:
 * Search algorithm used
 * mapsize
 * starting point
 */
public class Request {
    private SearchAlgorithm searchAlgorithm;
    private Size mapSize;
    private Point startingPoint;

    private int estimatedTimeComplexity;
    private int estimatedSpaceComplexity;

    public Request(SearchAlgorithm searchAlgorithm, Size mapSize, Point startingPoint) {
        this.searchAlgorithm = searchAlgorithm;
        this.mapSize = mapSize;
        this.startingPoint = startingPoint;
    }
    public SearchAlgorithm getSearchAlgorithm() {
        return searchAlgorithm;
    }

    public void setSearchAlgorithm(SearchAlgorithm searchAlgorithm) {
        this.searchAlgorithm = searchAlgorithm;
    }

    public Size getMapSize() {
        return mapSize;
    }

    public void setMapSize(Size mapSize) {
        this.mapSize = mapSize;
    }

    public Point getStartingPoint() {
        return startingPoint;
    }

    public void setStartingPoint(Point startingPoint) {
        this.startingPoint = startingPoint;
    }

    public enum SearchAlgorithm{
        ASTAR("ASTAR"), DFS("DFS"), BFS("BFS");
        private String value;

        SearchAlgorithm(String value){
            this.value = value;
        }

        @Override
        public String toString(){
            return value;
        }
    };

    public int getEstimatedTimeComplexity() {
        return estimatedTimeComplexity;
    }

    public void setEstimatedTimeComplexity(int estimatedTimeComplexity) {
        this.estimatedTimeComplexity = estimatedTimeComplexity;
    }

    public int getEstimatedSpaceComplexity() {
        return estimatedSpaceComplexity;
    }

    public void setEstimatedSpaceComplexity(int estimatedSpaceComplexity) {
        this.estimatedSpaceComplexity = estimatedSpaceComplexity;
    }
}
