
import BIT.highBIT.*;
import java.io.*;
import java.util.*;

 /**
  * BitTool based on the ICount BIT tool
  */
public class BitTool {
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
        routine.addAfter("BitTool", "resetCount", ci.getClassName());
    }


    // Adds LOAD, STORE, ALLOC metric calls to a routine
    public static void addInstructionMetricsToRoutine(Routine routine, ClassInfo ci){
        Instruction[] instructions = routine.getInstructions();
        for(Instruction instr : instructions){
            if(isLoadInstruction(instr)){
                instr.addBefore("BitTool", "incLoadStore", new Integer(0));
            }else if(isStoreInstruction(instr)){
                instr.addBefore("BitTool", "incLoadStore", new Integer(1));
            }else if(isAllocInstruction(instr)){
                instr.addBefore("BitTool", "incAlloc", ci.getClassName());
            }
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
            writer.println("Instruction total: " + i_count);
            writer.println("Load instructions: " + load_count);
            writer.println("Store instructions: " + store_count);
            writer.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static synchronized void count(int incr) {
        i_count += incr;
    }

    public static synchronized void incLoadStore(int type){
        if(type == 0){
            load_count++;
        }else if(type == 1){
            store_count++;
        }
    }

    public static synchronized void incAlloc(String className){
        alloc_count++;
        // TODO get length operand * size of type for total # bytes allocated
    }

    public static synchronized void resetCount(String className) {
        i_count=0;
    }
    //////////////////////////////////////////////////
}

