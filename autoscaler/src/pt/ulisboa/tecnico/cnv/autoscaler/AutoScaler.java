package pt.ulisboa.tecnico.cnv.autoscaler;

import java.util.HashMap;
import java.util.Map;

public class AutoScaler {
    private int MAX_VMS = 40;
    private int MIN_VMS = 1;
    private int COOLDOWN_PERIOD = 180; // 3 minutes
    private Map<String,VM> instances;

    public static void main(String[] args) throws  Exception{
        VM asd = new VM();
        asd.launchVM();
        System.out.println("IN GRACE PERIOD " + asd.isInGracePeriod());
        Thread.sleep(90000);
        System.out.println("IN GRACE PERIOD " + asd.isInGracePeriod());
        asd.getCPUUsage();
//        asd.terminate();
    }

	public AutoScaler(){
        this.instances = new HashMap<>();

    }


    public void check(){

        // Lets verify if we have machines that are doing nothing
        for(VM vm : instances.values()){

        }

    }



    public VM getMachine(){
        return new VM();
    }

}

