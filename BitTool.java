
import BIT.highBIT.*;
import java.io.*;
import java.util.*;

 /**
  * BitTool based on the ICount BIT tool
  */
public class BitTool {
    private static HashMap<Long, RequestMetricData> metricData = new HashMap<>();
    private static PrintStream out = null;
    private static long i_count = 0, load_count = 0, store_count = 0;
    private static long allocBytes_count = 0, alloc_count = 0;

    private static int[] allocInstrOpcodes = {InstructionTable.NEW, InstructionTable.newarray 
                                , InstructionTable.anewarray, InstructionTable.multianewarray};

    /* main reads in all the files class files present in the input directory,
     * instruments them, and outputs them to the specified output directory.
     */
    public static void main(String argv[]) {
        File file_in = new File(argv[0]);
        String infilenames[] = file_in.list();
        
        for (int i = 0; i < infilenames.length; i++) {
            String infilename = infilenames[i];
            if (infilename.endsWith(".class")) {
				// create class info object
				ClassInfo ci = new ClassInfo(argv[0] + System.getProperty("file.separator") + infilename);
				
                for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
                    Routine routine = (Routine) e.nextElement();

                    // Add write output method call when image solving is finished
                    if(isSolveImageRoutine(routine)){
                        // add output metrics method call first
                        addMetricOutputOnSolveImageCall(routine, ci);
                        // add init and remove calls after
                        addInitRequestMetricEntryCall(routine, ci);
                        addRemoveRequestMetricCall(routine, ci);
                    }
                    
                    // LOAD, STORE, ALLOC instruction metrics
                    addInstructionMetricsToRoutine(routine, ci);
                    
                    // Instruction count metric
                    addInstructionCountMetricToRoutine(routine);
                }
                ci.write(argv[1] + System.getProperty("file.separator") + infilename);
            }
        }
    }

    ////////// Add metric call methods /////////////////

    // add instructions to initialize an entry of metric data in the hashmap according
    // to the thread id, this is run once for every request
    public static void addInitRequestMetricEntryCall(Routine routine, ClassInfo ci){
        routine.addBefore("BitTool", "initRequestMetricDataEntry", ci.getClassName());
    }

    // add instructions to remove an entry of metric data from the hashmap according
    // to the thread id, this is run once for every request
    public static void addRemoveRequestMetricCall(Routine routine, ClassInfo ci){
        routine.addAfter("BitTool", "removeRequestMetricDataEntry", ci.getClassName());
    }

  
    public static void addMetricOutputOnSolveImageCall(Routine routine, ClassInfo ci){
        routine.addAfter("BitTool", "writeBitToolOutputToFile", ci.getClassName());
    }


    // Adds LOAD, STORE, ALLOC metric calls to the end of the routine
    public static void addInstructionMetricsToRoutine(Routine routine, ClassInfo ci){
        int loads = 0, stores = 0, allocs = 0;

        Instruction[] instructions = routine.getInstructions();
        for(Instruction instr : instructions){
            if(isLoadInstruction(instr)){
                loads++;
            }else if(isStoreInstruction(instr)){
                stores++;
            }else if(isAllocInstruction(instr)){
                allocs++;
            }
        }
        // after routine is finished, update load store and alloc instruction counters
        routine.addAfter("BitTool", "incLoad", loads);    
        routine.addAfter("BitTool", "incStore", stores);  
        routine.addAfter("BitTool", "incAlloc", allocs);  
    }

    // add instruction count metric call to a routine
    public static void addInstructionCountMetricToRoutine(Routine routine){
        for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
            BasicBlock bb = (BasicBlock) b.nextElement();
            bb.addBefore("BitTool", "count", new Integer(bb.size()));
        }
    }

    /////////////////////////////////////////////
    
    private static boolean isSolveImageRoutine(Routine routine){
        return routine.getMethodName().equals("solveImage");
    }

    private static boolean isAllocInstruction(Instruction instruction){
        int opcode = instruction.getOpcode();
        for(int i = 0; i < allocInstrOpcodes.length; i++){
            if(opcode == allocInstrOpcodes[i]){
                return true;
            }
        }
        return false;
    }

    private static boolean isLoadInstruction(Instruction instruction){
        int opcode = instruction.getOpcode();
        return InstructionTable.InstructionTypeTable[opcode] == InstructionTable.LOAD_INSTRUCTION;
    }

    private static boolean isStoreInstruction(Instruction instruction){
        int opcode = instruction.getOpcode();
        return InstructionTable.InstructionTypeTable[opcode] == InstructionTable.STORE_INSTRUCTION;
    }

    
    
    //////////////// Added methods to bytecode ///////////

    
    // initialize request metric data entry in hashmap 
    public static synchronized void initRequestMetricDataEntry(String className){
        // TODO query data
        metricData.put(Thread.currentThread().getId(), new RequestMetricData("",""));
    }
    
    // remove request metric data entry from hashmap 
    public static synchronized void removeRequestMetricDataEntry(String className){
        metricData.remove(Thread.currentThread().getId());
    }


    public static synchronized void writeBitToolOutputToFile(String className) {
        RequestMetricData reqMetricData = metricData.get(Thread.currentThread().getId());
        try{
            PrintWriter writer = new PrintWriter("bitToolOutput.txt", "UTF-8");
            writer.println("Instruction total: " + reqMetricData.instructionCount);
            writer.println("Load instructions: " + reqMetricData.loadInstructionCount);
            writer.println("Store instructions: " + reqMetricData.storeInstructionCount);
            writer.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static synchronized void count(int incr) {
        RequestMetricData requestMetricData = metricData.get(Thread.currentThread().getId());
        
        // inevitable null guard due to instrumentation bytecode being inserted at compile time
        if(requestMetricData != null){
            requestMetricData.instructionCount+=incr;
        }
    }

    public static synchronized void incLoad(int incr){
        RequestMetricData requestMetricData = metricData.get(Thread.currentThread().getId());

        // inevitable null guard due to instrumentation bytecode being inserted at compile time
        if(requestMetricData != null){
            requestMetricData.loadInstructionCount+=incr;
        }
    }

    public static synchronized void incStore(int incr){
        RequestMetricData requestMetricData = metricData.get(Thread.currentThread().getId());

        // inevitable null guard due to instrumentation bytecode being inserted at compile time
        if(requestMetricData != null){
            requestMetricData.storeInstructionCount+=incr;
        }
    }


    public static synchronized void incAlloc(int incr){
        RequestMetricData requestMetricData = metricData.get(Thread.currentThread().getId());

        // inevitable null guard due to instrumentation bytecode being inserted at compile time
        if(requestMetricData != null){
            requestMetricData.allocInstructionCount+=incr;
        }
    }
}

// TODO change to object array for efficiency
class RequestMetricData{
        public String query; // responsible for carrying out the request processing
        public String searchAlgorithm;
        public long dataSetSize;
        public long searchRectSize;
        public long instructionCount;
        public long loadInstructionCount;
        public long storeInstructionCount;
        public long allocInstructionCount;
        public long allocByteCount;

        public RequestMetricData(String query, String searchAlgorithm){
            this.query = query;
            this.searchAlgorithm = searchAlgorithm;
            this.dataSetSize = 0;
            this.searchRectSize = 0;
            this.instructionCount = 0;
            this.loadInstructionCount = 0;
            this.storeInstructionCount = 0;
            this.allocInstructionCount = 0;
            this.allocByteCount = 0;
        }

    }