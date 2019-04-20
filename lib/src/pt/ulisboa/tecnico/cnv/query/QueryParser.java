package pt.ulisboa.tecnico.cnv.query;

import pt.ulisboa.tecnico.cnv.hillclimber.solver.SolverArgumentParser;

import java.util.ArrayList;

abstract public class QueryParser {

    public static SolverArgumentParser parse(String query){
        System.out.println("> Query:\t" + query);
        // Break it down into String[].
        final String[] params = query.split("&");

        // Store as if it was a direct call to SolverMain.
        final ArrayList<String> newArgs = new ArrayList<>();
        for (final String p : params) {
            final String[] splitParam = p.split("=");
            newArgs.add("-" + splitParam[0]);
            newArgs.add(splitParam[1]);
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
}
