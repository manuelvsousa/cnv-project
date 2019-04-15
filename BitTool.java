import BIT.highBIT.*;
import java.io.*;
import java.util.*;


public class BitTool {
   
    /**
     * Thread specific metric Data array
     * long[] -> metric data
     * 0: long instructionCount
     * 1: long loadInstructionCount;
     * 2: long storeInstructionCount;
     * 3: long allocInstructionCount;
     */
    private static ThreadLocal<long[]> metricData = new ThreadLocal<long[]>() {
        @Override public long[] initialValue(){
            return new long[] {0,0,0,0};
        }
    };

    
    private static PrintStream out = null;
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

    public static void addMetricOutputOnSolveImageCall(Routine routine, ClassInfo ci){
        routine.addAfter("BitTool", "writeBitToolOutputToFile", ci.getClassName());
    }


    // Adds LOAD, STORE, ALLOC metric calls to the end of the routine's basic blocks
    public static void addInstructionMetricsToRoutine(Routine routine, ClassInfo ci){
        int loads = 0, stores = 0, allocs = 0;

        for(Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements();){
            Instruction[] routineInstructions = routine.getInstructions();
            BasicBlock bb = (BasicBlock) b.nextElement();
            
            for( int i = bb.getStartAddress(); i < bb.getEndAddress(); i++){
                Instruction instr = routineInstructions[i];
                if(isLoadInstruction(instr)){
                    loads++;
                }else if(isStoreInstruction(instr)){
                    stores++;
                }else if(isAllocInstruction(instr)){
                    allocs++;
                }
            }
           
            bb.addBefore("BitTool", "incLoad", loads);    
            bb.addBefore("BitTool", "incStore", stores);  
            bb.addBefore("BitTool", "incAlloc", allocs);  
            loads = 0;
            stores = 0;
            allocs = 0;
        }
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

    

    public static synchronized void writeBitToolOutputToFile(String className) {
        try{
            PrintWriter writer = new PrintWriter("bitToolOutput.txt", "UTF-8");
            writer.println("Instruction total: " + metricData.get()[0]);
            writer.println("Load instructions: " + metricData.get()[1]);
            writer.println("Store instructions: " + metricData.get()[2]);
            writer.println("Alloc instructions: " + metricData.get()[3]);
            writer.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static synchronized void count(int incr) {
        metricData.get()[0] += incr;
    }

    public static synchronized void incLoad(int incr){
         metricData.get()[1] += incr;
    }
    public static synchronized void incStore(int incr){
         metricData.get()[2] += incr;
    }

    public static synchronized void incAlloc(int incr){
         metricData.get()[3] += incr;
    }
}

