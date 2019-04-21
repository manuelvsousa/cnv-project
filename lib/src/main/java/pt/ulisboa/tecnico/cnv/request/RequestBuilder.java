package pt.ulisboa.tecnico.cnv.request;

import pt.ulisboa.tecnico.cnv.hillclimber.solver.SolverArgumentParser;
import pt.ulisboa.tecnico.cnv.query.QueryParser;

/**
 * Helper class to build request objects
 */
public class RequestBuilder {

    /**
     * Create request from a query string
     * @param query web query
     * @return Request
     */
    public static Request fromQuery(String query){
        // parse query
        SolverArgumentParser ap = QueryParser.parse(query);

        // build request object
        Request.SearchAlgorithm searchAlgorithm = Request.SearchAlgorithm.valueOf(ap.getSolverStrategy().toString());
        Size mapSize = new Size(ap.getX1() - ap.getX0(), ap.getY1() - ap.getY0());
        Point startingPoint = new Point(ap.getStartX(), ap.getStartY());
        return new Request(searchAlgorithm, mapSize, startingPoint);
    }
}
