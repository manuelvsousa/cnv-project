package pt.ulisboa.tecnico.cnv.bittool;

import BIT.highBIT.*;
import BIT.lowBIT.Local_Variable_Table;
import BIT.lowBIT.Method_Info;
import org.apache.commons.lang3.ArrayUtils;

import java.io.*;
import java.util.*;

public class BitTool {
    /* long[] { time, space } */
    private static ThreadLocal<long[]> complexity = new ThreadLocal<long[]>() {
        @Override public long[] initialValue(){
            return new long[] { 0, 0 };
        }
    };

    // instruction weights compared to single instruction to summarise complexity of all instructions in a single value
    private static int LOAD_STORE_INST_WEIGHT = 15;
    private static int ALLOC_INST_WEIGHT = 25;
    
    private static PrintStream out = null;
    private static int[] allocInstrOpcodes = {InstructionTable.NEW, InstructionTable.newarray 
        , InstructionTable.anewarray, InstructionTable.multianewarray};

    /* main reads in all the files class files present in the input directory,
     * instruments them, and outputs them to the specified output directory.
     */
    public static void main(String argv[]) {
        prepareSolverInstrumentation(argv);
        prepareWebServerInstrumentation(argv);
    }

    /**
     * Add Instrumentation calls to the solver classes
     * @param argv
     */
    public static void prepareSolverInstrumentation(String argv[]){
        File files_in_solver = new File(argv[0]);
        String infilenames[] = files_in_solver.list();

        if(infilenames != null){
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
    }

    /**
     * Add instrumentation calls to the web server classes
     * @param argv
     */
    public static void prepareWebServerInstrumentation(String argv[]){
        // Web server instrumentation
        File files_in_webserver = new File(argv[2]);
        String[] infilenames = files_in_webserver.list();

        if(infilenames != null){
            for (int i = 0; i < infilenames.length; i++) {
                String infilename = infilenames[i];
                if (infilename.endsWith(".class")) {
                    // create class info object
                    ClassInfo ci = new ClassInfo(argv[2] + System.getProperty("file.separator") + infilename);

                    System.out.println(ci.getClassName());

                    for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
                        Routine routine = (Routine) e.nextElement();

                        if(routine.getMethodName().equals("handle")){
                            //routine.addAfter("MyHandler", "bitTest" , 42);
                        }
                    }

                    ci.write(argv[3] + System.getProperty("file.separator") + infilename);
                }
            }
        }
    }

    ////////// Add metric call methods /////////////////

    public static void addMetricOutputOnSolveImageCall(Routine routine, ClassInfo ci){
        routine.addAfter("BitTool", "writeBitToolOutputToFile", ci.getClassName());
        Local_Variable_Table[] lvt = routine.getLVT();
        Method_Info method_info = routine.getMethodInfo();
    }


    // Adds LOAD, STORE, ALLOC metric calls to the end of the routine's basic blocks
    public static void addInstructionMetricsToRoutine(Routine routine, ClassInfo ci){
        int totalLoadStoreWeight = 0, totalAllocWeight = 0;

        for(Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements();){
            Instruction[] routineInstructions = routine.getInstructions();
            BasicBlock bb = (BasicBlock) b.nextElement();

            for( int i = bb.getStartAddress(); i < bb.getEndAddress(); i++){
                Instruction instr = routineInstructions[i];
                if(isLoadInstruction(instr) || isStoreInstruction(instr)){
                    totalLoadStoreWeight += LOAD_STORE_INST_WEIGHT;
                }else if(isAllocInstruction(instr)){
                    totalAllocWeight += ALLOC_INST_WEIGHT;
                }
            }

            bb.addBefore("BitTool", "incTimeComplexity", totalLoadStoreWeight);
            bb.addBefore("BitTool", "incSpaceComplexity", totalAllocWeight);
            totalLoadStoreWeight = 0;
            totalAllocWeight = 0;
        }
    }

    // add instruction count metric call to a routine
    public static void addInstructionCountMetricToRoutine(Routine routine){
        for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
            BasicBlock bb = (BasicBlock) b.nextElement();
            bb.addBefore("BitTool", "incTimeComplexity", new Integer(bb.size()));
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
            writer.println("Time Complexity: " + complexity.get()[0]);
            writer.println("Space Complexity: " + complexity.get()[1]);
            writer.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static synchronized void incTimeComplexity(int weight){
        complexity.get()[0] += weight;
    }
    public static synchronized void incSpaceComplexity(int weight){
        complexity.get()[1] += weight;
    }
}

