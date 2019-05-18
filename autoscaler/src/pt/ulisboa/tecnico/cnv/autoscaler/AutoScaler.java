package pt.ulisboa.tecnico.cnv.autoscaler;

import java.util.Map;

public class AutoScaler {
    private int MAX_VMS = 40;
    private int MIN_VMS = 1;
    private Map<String,VM> instances;

    public static void main(String[] args) throws  Exception{
        VM asd = new VM();
        asd.launchVM();
        asd.getStatistics();
        Thread.sleep(40000);
        asd.getStatistics();
        asd.terminate();
    }

	public AutoScaler(){

    }


    public void check(){





    }



    public VM getMachine(){
        return new VM();
    }

}

