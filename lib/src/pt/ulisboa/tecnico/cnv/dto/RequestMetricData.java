package pt.ulisboa.tecnico.cnv.dto;

public class RequestMetricData {
    // In request parameters
    private SearchAlgorithm searchAlgorithm;
    private Size mapSize;
    private Point startingPoint;

    // instrumented metrics
    private long timeComplexity;
    private long spaceComplexity;

    public RequestMetricData(SearchAlgorithm searchAlgorithm, Size mapSize, Point startingPoint, long timeComplexity, long spaceComplexity) {
        this.searchAlgorithm = searchAlgorithm;
        this.mapSize = mapSize;
        this.startingPoint = startingPoint;
        this.timeComplexity = timeComplexity;
        this.spaceComplexity = spaceComplexity;
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

    public long getTimeComplexity() {
        return timeComplexity;
    }

    public void setTimeComplexity(long timeComplexity) {
        this.timeComplexity = timeComplexity;
    }

    public long getSpaceComplexity() {
        return spaceComplexity;
    }

    public void setSpaceComplexity(long spaceComplexity) {
        this.spaceComplexity = spaceComplexity;
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
}


