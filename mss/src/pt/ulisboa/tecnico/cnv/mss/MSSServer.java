package pt.ulisboa.tecnico.cnv.mss;

import com.sun.net.httpserver.HttpServer;
import pt.ulisboa.tecnico.cnv.mss.operations.addMetrics;
import pt.ulisboa.tecnico.cnv.mss.operations.getMetrics;

import java.net.InetSocketAddress;

public class MSSServer {
    private static MSSServer instance = null;
    private int PORT = 8001;
    private MSSDynamo mssDynamo;

    private MSSServer() throws Exception {
        this.mssDynamo = new MSSDynamo();
    }

    public static void main(String[] args) throws Exception {
        MSSServer.getInstance().startServer();
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
