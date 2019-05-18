package pt.ulisboa.tecnico.cnv.lib.request;

/**
 * Represents a HillClimber request
 */
public class Request {
    private int id;
    private SearchAlgorithm searchAlgorithm;
    private Point startingPoint;
    private Point point0; // upper left
    private Point point1; // lower right
    private double progress;

    private String query;

    private static int ID_COUNTER = 0;

    // defined by the loadbalancer as an estimate of the complexity of this request before execution
    private int estimatedComplexity;

    // measured from executing the request through instrumentation
    private long measuredComplexity;

    public Request(){
        this.id = ID_COUNTER;
        ID_COUNTER++;
    }

    public Request(SearchAlgorithm searchAlgorithm, Point startingPoint, Point point0, Point point1) {
        this();
        this.searchAlgorithm = searchAlgorithm;
        this.startingPoint = startingPoint;
        this.point0 = point0;
        this.point1 = point1;
    }

    public Request(SearchAlgorithm searchAlgorithm, Point startingPoint, Point point0, Point point1, int estimatedComplexity) {
        this();
        this.searchAlgorithm = searchAlgorithm;
        this.startingPoint = startingPoint;
        this.point0 = point0;
        this.point1 = point1;
        this.estimatedComplexity = estimatedComplexity;
    }

    public SearchAlgorithm getSearchAlgorithm() {
        return searchAlgorithm;
    }

    public void setSearchAlgorithm(SearchAlgorithm searchAlgorithm) {
        this.searchAlgorithm = searchAlgorithm;
    }

    public Point getStartingPoint() {
        return startingPoint;
    }

    public void setStartingPoint(Point startingPoint) {
        this.startingPoint = startingPoint;
    }

    public int getEstimatedComplexity() {
        return estimatedComplexity;
    }

    public void setEstimatedComplexity(int estimatedComplexity) {
        this.estimatedComplexity = estimatedComplexity;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public void setId(int requestId) {
        this.id = requestId;
    }

    public int getId() {
        return id;
    }

    public Point getPoint0() {
        return point0;
    }

    public Point getPoint1() {
        return point1;
    }

    public double getProgress() {
        return progress;
    }

    public long getMeasuredComplexity() {
        return measuredComplexity;
    }

    public void setMeasuredComplexity(long measuredComplexity) {
        this.measuredComplexity = measuredComplexity;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public enum SearchAlgorithm {
        ASTAR("ASTAR"), DFS("DFS"), BFS("BFS");
        private String value;

        SearchAlgorithm(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
