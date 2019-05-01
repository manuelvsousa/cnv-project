package pt.ulisboa.tecnico.cnv.mss;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.util.EC2MetadataUtils;
import com.sun.net.httpserver.HttpServer;
import pt.ulisboa.tecnico.cnv.lib.ec2.InstanceManager;
import pt.ulisboa.tecnico.cnv.mss.operations.addMetrics;
import pt.ulisboa.tecnico.cnv.mss.operations.getMetrics;

import java.net.InetSocketAddress;

public class MSSServer {
    private static MSSServer instance = null;
    private static InstanceManager instanceManager = new InstanceManager();
    private int PORT = 8001;
    private MSSDynamo mssDynamo;

    private static boolean isTestingLocally = false;

    private MSSServer() throws Exception {
        this.mssDynamo = new MSSDynamo();
    }

    public static void main(String[] args) throws Exception {
        MSSServer.getInstance().startServer();

        // if testing on single machine use localhost ip's
        if(args.length == 1 && args[0].equals("-localhost")){
            isTestingLocally = true;
            System.out.println("Running MSSServer on localhost.");
        }else{
            // clear previous tags and set loadbalancer tag for instance identification
            Instance instance = instanceManager.getInstanceById(EC2MetadataUtils.getInstanceId());
            instanceManager.clearInstanceTags(instance);
            instanceManager.tagInstanceAsMSS(instance);
        }
    }

    public static MSSServer getInstance() throws Exception {
        if (instance == null)
            instance = new MSSServer();
        return instance;
    }

    public void startServer() throws Exception {
        mssDynamo.search(4);
        HttpServer server = HttpServer.create(new InetSocketAddress(this.PORT), 0);
        server.createContext("/getMetrics", new getMetrics(mssDynamo));
        server.createContext("/addMetrics", new addMetrics(mssDynamo));
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("Service running at port  " + this.PORT);
    }

}
