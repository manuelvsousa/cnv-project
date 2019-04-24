package pt.ulisboa.tecnico.cnv.lib.request;

/**
 * Represents a HillClimber request, indicating:
 * Search algorithm used
 * mapsize
 * starting point
 * request status
 */
public class Request {
    private SearchAlgorithm searchAlgorithm;
    private Size mapSize;
    private Point startingPoint;
    private Status requestStatus;

    public Request(SearchAlgorithm searchAlgorithm, Size mapSize, Point startingPoint) {
        this.searchAlgorithm = searchAlgorithm;
        this.mapSize = mapSize;
        this.startingPoint = startingPoint;
        this.requestStatus = Status.WAITING;
    }

    public Request(SearchAlgorithm searchAlgorithm, Size mapSize, Point startingPoint, Status requestStatus) {
        this.searchAlgorithm = searchAlgorithm;
        this.mapSize = mapSize;
        this.startingPoint = startingPoint;
        this.requestStatus = requestStatus;
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

    public Status getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(Status requestStatus) {
        this.requestStatus = requestStatus;
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

    public enum Status{
        RUNNING("RUNNING"), WAITING("WAITING"), DONE("DONE");
        private String value;

        Status(String value){
            this.value = value;
        }

        @Override
        public String toString(){
            return value;
        }
    }
}
