package pt.ulisboa.tecnico.cnv.lib.query;

import pt.ulisboa.tecnico.cnv.hillclimber.solver.SolverArgumentParser;
import pt.ulisboa.tecnico.cnv.lib.request.Point;
import pt.ulisboa.tecnico.cnv.lib.request.Request;

import java.util.ArrayList;

public class QueryParser {
    private String query;
    private SolverArgumentParser solverArgumentParser;
    private Request request;

    private String instanceId;
    private double requestProgress;
    private int requestId;
    private long estimatedComplexity;

    public QueryParser(String query){
        this.query = query;
        this.solverArgumentParser = parse(query);
        Request.SearchAlgorithm algo = Request.SearchAlgorithm.valueOf(solverArgumentParser.getSolverStrategy().toString());
        this.request = new Request(algo,
                new Point(solverArgumentParser.getStartX(), solverArgumentParser.getStartY()),
                new Point(solverArgumentParser.getX0(), solverArgumentParser.getY0()),
                new Point(solverArgumentParser.getX1(), solverArgumentParser.getY1()));
        this.request.setProgress(requestProgress);
        this.request.setId(requestId);
        this.request.setEstimatedComplexity(estimatedComplexity);
    }

    private SolverArgumentParser parse(String query){
        System.out.println("> Query:\t" + query);
        // Break it down into String[].
        final String[] params = query.split("&");

        // Store as if it was a direct call to SolverMain.
        final ArrayList<String> newArgs = new ArrayList<>();
        for (final String p : params) {
            final String[] splitParam = p.split("=");
            if(splitParam[0].equals("progress")){
                this.requestProgress = Double.parseDouble(splitParam[1]);
            }else if(splitParam[0].equals("reqid")){
                this.requestId = Integer.parseInt(splitParam[1]);
            }else if(splitParam[0].equals("instanceId")){
                this.instanceId = splitParam[1];
            }else if(splitParam[0].equals("estimatedComplexity")){
                this.estimatedComplexity = Long.parseLong(splitParam[1]);
            }else{
                newArgs.add("-" + splitParam[0]);
                newArgs.add(splitParam[1]);
            }



        }
        newArgs.add("-d");

        // Store from ArrayList into regular String[].
        final String[] args = new String[newArgs.size()];
        int i = 0;
        for(String arg: newArgs) {
            args[i] = arg;
            i++;
        }

        SolverArgumentParser ap = null;
        try {
            // Get user-provided flags.
            ap = new SolverArgumentParser(args);
        }catch(Exception e) {
            System.out.println(e);
            return null;
        }

        return ap;
    }

    public String getQuery() {
        return query;
    }

    public SolverArgumentParser getSolverArgumentParser() {
        return solverArgumentParser;
    }

    public Request getRequest() {
        return request;
    }

    public String getInstanceId() {
        return instanceId;
    }
}
