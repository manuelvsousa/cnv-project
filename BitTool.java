
import BIT.highBIT.*;
import java.io.*;
import java.util.*;

 /**
  * BitTool based on the ICount BIT tool
  */
public class BitTool {
    private static PrintStream out = null;
    private static long i_count = 0, load_count = 0, store_count = 0;

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
                    if(routine.getMethodName().equals("solveImage")){
                        // after solveImage() finishes write results to a file 
                        // and reset counter for the next request
                        // TODO measure only one thread, and not all if there is
                        // more than 1 request running at the same time, ruining the result
                        routine.addAfter("BitTool", "writeBitToolOutputToFile", ci.getClassName());
                        routine.addAfter("BitTool", "resetCount", ci.getClassName());
                    }
                    
                    // Count load/stores in each routine
                    Instruction[] routineInstructions = routine.getInstructions();
                    for(Instruction instr : routineInstructions){
                        int opcode = instr.getOpcode();
                        short instr_type = InstructionTable.InstructionTypeTable[opcode];
                        if (instr_type == InstructionTable.LOAD_INSTRUCTION) {
                            instr.addBefore("BitTool", "incLoadStore", new Integer(0));
                        }else if (instr_type == InstructionTable.STORE_INSTRUCTION) {
                            instr.addBefore("BitTool", "incLoadStore", new Integer(1));
                        }
                    }    

                    for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
                        BasicBlock bb = (BasicBlock) b.nextElement();
                        bb.addBefore("BitTool", "count", new Integer(bb.size()));
                    }
                }
                ci.write(argv[1] + System.getProperty("file.separator") + infilename);
            }
        }
    }
    
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

    public static synchronized void resetCount(String className) {
        i_count=0;
    }
}

