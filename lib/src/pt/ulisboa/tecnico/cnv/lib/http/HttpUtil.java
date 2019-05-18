package pt.ulisboa.tecnico.cnv.lib.http;

abstract public class HttpUtil {

    public static String buildUrl(String ip, int port){
        return "http://" + ip + ":" + port + "/climb";
    }
}
