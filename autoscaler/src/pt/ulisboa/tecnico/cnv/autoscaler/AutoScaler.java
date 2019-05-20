package pt.ulisboa.tecnico.cnv.autoscaler;

import java.util.*;

public class AutoScaler {
    private int MAX_VMS = 40;
    private int MIN_VMS = 1;
    private int COOLDOWN_PERIOD = 120 * 1000; // 3 minutes
    private long lastCoolDown;
    private int MAX_ALLOWED_CPU = 70;
    private int MIN_ALLOWED_CPU = 40;
    private List<Integer> avgRecords;


    private List<VM> instances;

    public static void main(String[] args) throws  Exception{
        AutoScaler as = new AutoScaler();
        as.launchInstance();
        while(true){
            System.out.println("----------------------------- NEW TICK ---------------------------");
            System.out.println("------------------------------------------------------------------\n");
            as.check();
            System.out.println("\n\n------------------------------------------------------------------");
            System.out.println("------------------------------------------------------------------\n\n");
            Thread.sleep(30000);
        }
//        asd.terminate();
    }

	public AutoScaler(){
        this.instances = new ArrayList<>();
        this.avgRecords = new ArrayList<>();
    }


    public void check(){
        // Verify what is the machine doing
        VM lessLoad = null;
        int minL = 1000000;

        for(VM vm : instances){
            vm.tick();
            System.out.println("------------- MACHINE " + vm.getID() + "-------------");
            System.out.println("Avg CPU in the last 4 minutes: " + (vm.getCPUUsage() == -1 ? "None" : vm.getCPUUsage() + "%"));
            System.out.println("Health Records:" + vm.getHealthRecords());
            System.out.println("Requests History:" + vm.getRequestsHistory());
            System.out.println("-----------------------------------------------------");
            System.out.println();
            if(Collections.frequency(vm.getHealthRecords(), false) == 1){ // 1 health checks failed in the last 3 minutes
                this.instances.remove(vm);
                vm.terminate();
            }
        }
        int avg = 0;
        int doNotCountThese = 0;
        for(VM vm : instances){
            if(vm.getLastRecordedCPU() >= 0){ //this is to account machines that are still in grace period. They wont take part in the math
                avg += vm.getLastRecordedCPU(); // avg from the last 4 minutes of this machine
                if(avg < minL && !vm.isBusy()){
                    minL = avg;
                    lessLoad = vm;
                }
            } else {
                doNotCountThese++;
            }
        }
        int numberOfInstancesWithMetrics  = getNumberOfRunningInstances() - doNotCountThese;
        if(numberOfInstancesWithMetrics == 0){
            return;
        }
        avg = avg / numberOfInstancesWithMetrics;
        avgRecords.add(avg);
        System.out.println("AVG CPU ON INSTANCES: " + avg + "% .Taking part in calculation: " + numberOfInstancesWithMetrics + " machines\n\n");

        if(!coolDownOver()){
            System.out.println("In cooldown period. No changes will be made in the machines");
            return;
        }

        if(avg > MAX_ALLOWED_CPU && !isDecreasing() && getNumberOfRunningInstances() < MAX_VMS){
            launchInstance();
        } else if(avg < MIN_ALLOWED_CPU && isDecreasing() && getNumberOfRunningInstances() > MIN_VMS){
            if(!lessLoad.isBusy()){ //double check
                this.instances.remove(lessLoad);
                lessLoad.terminate();
            }
        }

    }

    private int getNumberOfRunningInstances(){
        return this.instances.size();
    }

    private Boolean isDecreasing(){
        if(this.avgRecords.size() >= 2){
            if(avgRecords.get(avgRecords.size() - 1) < avgRecords.get(avgRecords.size() - 2)){
                return true;
            }
        }
        return false;
    }

    public void launchInstance(){
        VM vm = new VM();
        vm.launchVM();
        this.lastCoolDown = (new Date().getTime());
        this.instances.add(vm);
    }

    public boolean coolDownOver(){
        return (this.lastCoolDown + COOLDOWN_PERIOD) < (new Date().getTime());
    }

}

