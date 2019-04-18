package pt.ulisboa.tecnico.cnv.hill.mss;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class MSSServer {
    private static MSSServer instance = null;
    private int PORT = 8000;
    private MSSDynamo mssDynamo = null;

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
        HttpServer server = HttpServer.create(new InetSocketAddress(this.PORT), 0);
        server.createContext("/getMetrics", new getMetrics());
        server.createContext("/addMetrics", new addMetrics());
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("Service running at port  " + this.PORT);
        System.out.println("Type [CTRL]+[C] to quit!");
    }


    static class getMetrics implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "This is the response";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }


    static class addMetrics implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "This is thedasadsdas response";
            t.sendResponseHeaders(200, 0);
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
