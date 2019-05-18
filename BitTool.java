import java.lang.StringBuffer;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.HttpURLConnection;
import java.net.URL;

import BIT.highBIT.*;
import BIT.lowBIT.Local_Variable_Table;
import BIT.lowBIT.Method_Info;
import pt.ulisboa.tecnico.cnv.webserver.WebServer;
import pt.ulisboa.tecnico.cnv.webserver.WebServerHandler;
import pt.ulisboa.tecnico.cnv.lib.request.Request;
import pt.ulisboa.tecnico.cnv.lib.request.RequestMetricData;

import java.io.*;
import java.util.*;

public class BitTool {
    private static ThreadLocal<long[]> complexity = new ThreadLocal<long[]>() {
        @Override public long[] initialValue(){
            return new long[] { 0 };
        }
    };

    // instruction weights compared to single instruction to summarise metricData of all instructions in a single value
    private static final int LOAD_STORE_INST_WEIGHT = 35;
    private static final int ALLOC_INST_WEIGHT = 25;
    private static final int CONDITIONAL_INST_WEIGHT = 10;
    private static final int COMPARISON_INST_WEIGHT = 10;

    private static PrintStream out = null;
    private static int[] allocInstrOpcodes = {InstructionTable.NEW, InstructionTable.newarray
        , InstructionTable.anewarray, InstructionTable.multianewarray};

    /* main reads in all the files class files present in the input directory,
     * instruments them, and outputs them to the specified output directory.
     */
    public static void main(String argv[]) {
        prepareSolverInstrumentation(argv);
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

                        // LOAD, STORE, ALLOC, CONDITIONAL + # instructions
                        addInstructionMetricsToRoutine(routine, ci);
                    }

                    ci.write(argv[1] + System.getProperty("file.separator") + infilename);
                }
            }
        }
    }

    ////////// Add metric call methods /////////////////


    public static void addMetricOutputOnSolveImageCall(Routine routine, ClassInfo ci){
        routine.addAfter("BitTool", "sendMetricData", ci.getClassName());
    }


    // Adds LOAD, STORE, ALLOC metric calls to the end of the routine's basic blocks
    public static void addInstructionMetricsToRoutine(Routine routine, ClassInfo ci){
        int totalLoadStoreWeight = 0, totalAllocWeight = 0, totalConditionalWeight = 0;
        int totalComparisonWeight = 0;

        for(Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements();){
            Instruction[] routineInstructions = routine.getInstructions();
            BasicBlock bb = (BasicBlock) b.nextElement();

            for( int i = bb.getStartAddress(); i < bb.getEndAddress(); i++){
                Instruction instr = routineInstructions[i];
                if(isLoadInstruction(instr) || isStoreInstruction(instr)){
                    totalLoadStoreWeight += LOAD_STORE_INST_WEIGHT;
                }else if(isAllocInstruction(instr)){
                    totalAllocWeight += ALLOC_INST_WEIGHT;
                }else if(isConditionalInstruction(instr)){
                    totalConditionalWeight += CONDITIONAL_INST_WEIGHT;
                }else if(isComparisonInstruction(instr)){
                    totalComparisonWeight += COMPARISON_INST_WEIGHT;
                }
            }

            bb.addBefore("BitTool", "incComplexity", totalLoadStoreWeight + totalConditionalWeight + totalComparisonWeight + bb.size() + totalAllocWeight);
            totalLoadStoreWeight = 0;
            totalAllocWeight = 0;
        }
    }


    //////////  Auxiliary methods for bittool when adding bytecodes

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
        return InstructionTable.InstructionTypeTable[instruction.getOpcode()]
                == InstructionTable.LOAD_INSTRUCTION;
    }

    private static boolean isStoreInstruction(Instruction instruction){
        return InstructionTable.InstructionTypeTable[instruction.getOpcode()]
                == InstructionTable.STORE_INSTRUCTION;
    }

    private static boolean isConditionalInstruction(Instruction instruction){
        return InstructionTable.InstructionTypeTable[instruction.getOpcode()]
                == InstructionTable.CONDITIONAL_INSTRUCTION;
    }

    private static boolean isComparisonInstruction(Instruction instruction){
        return InstructionTable.InstructionTypeTable[instruction.getOpcode()]
                == InstructionTable.COMPARISON_INSTRUCTION;
    }

    //////////////// Added methods to bytecode ///////////

    public static synchronized void sendMetricData(String className) {
        Request request = WebServerHandler.request.get();
        String searchAlgo = request.getSearchAlgorithm().toString();
        String mapSize = request.getMapSize().getWidth()+"_"+request.getMapSize().getHeight();
        request.setMeasuredComplexity(complexity.get()[0]);

        try{
            PrintWriter writer = new PrintWriter("bitToolOutput_"+searchAlgo+"_"+mapSize+".txt", "UTF-8");
            writer.println("Complexity: " + complexity.get()[0]);
            writer.println("Search algorithm: " + WebServerHandler.request.get().getSearchAlgorithm());
            writer.close();

            complexity.get()[0]=0;
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public static synchronized void incComplexity(int weight){
        complexity.get()[0] += weight;
    }




}

