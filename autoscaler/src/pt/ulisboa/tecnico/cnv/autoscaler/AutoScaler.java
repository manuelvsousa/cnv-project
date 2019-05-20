package pt.ulisboa.tecnico.cnv.autoscaler;

import java.util.*;

public class AutoScaler {
    private int MAX_VMS = 40;
    private int MIN_VMS = 1;
    private int COOLDOWN_PERIOD = 180; // 3 minutes
    private int lastCoolDown;

    private List<VM> instances;

    public static void main(String[] args) throws  Exception{
        AutoScaler as = new AutoScaler();
        as.launchInstance();
        while(true){
            as.check();
            Thread.sleep(30000);
        }
//        asd.terminate();
    }

	public AutoScaler(){
        this.instances = new ArrayList<>();
    }


    public void check(){
        // Verify what is the machine doing
        for(VM vm : instances){
            vm.tick();
            System.out.println(vm.getCPUUsage());
            System.out.println(vm.getHealthRecords());
            System.out.println(vm.getRequestsHistory());
//            if(Collections.frequency(vm.getHealthRecords(), false) == 1){ // 1 health checks failed in the last 3 minutes
//                this.instances.remove(vm);
//                vm.terminate();
//            }
        }
//        int avg = 0;
//        int doNotCountThese = 0;
//        for(VM vm : instances){
//            if(vm.getLastRecordedCPU() > 0){
//                avg += vm.getLastRecordedCPU();
//            } else {
//                doNotCountThese++;
//            }
//        }
//        int numberOfInstancesWithMetrics  = getNumberOfRunningInstances() - doNotCountThese;
//        if(numberOfInstancesWithMetrics <= 0){
//            return;
//        }
//        avg = avg / numberOfInstancesWithMetrics;
//        System.out.println("AVG CPU ON INSTANCES " + avg);
//        if(avg > 70){
//            launchInstance();
//        }

    }

    private int getNumberOfRunningInstances(){
        return this.instances.size();
    }

    private Boolean isGoingUpOnCPU(List <Double> cpus){
        if(cpus.size() != 3){
            return false;
        } else {
            if(cpus.get(2) >= cpus.get(1) && cpus.get(1) >= cpus.get(0)){
                return true;
            } else {
                return false;
            }
        }
    }


    public void launchInstance(){
        VM vm = new VM();
        vm.launchVM();
        this.instances.add(vm);
    }

}

