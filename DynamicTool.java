package pt.ulisboa.tecnico.cnv.webserver;

import BIT.highBIT.*;
import java.io.File;
import java.util.Enumeration;
import java.util.Vector;
import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;


public class DynamicTool{

		private static int dyn_method_count = 0;
	private static int dyn_bb_count = 0;
	private static int dyn_instr_count = 0;
	
	private static int newcount = 0;
	private static int newarraycount = 0;
	private static int anewarraycount = 0;
	private static int multianewarraycount = 0;

	private static int loadcount = 0;
	private static int storecount = 0;
	private static int fieldloadcount = 0;
	private static int fieldstorecount = 0;

	private static int branch_number;
	private static int branch_pc;
	private static String branch_class_name;
	private static String branch_method_name;



	public static void doDynamic(File in_dir, File out_dir) 
		{
			System.out.println(in_dir);

			String filelist[] = in_dir.list();
			
			for (int i = 0; i < filelist.length; i++) {

				String filename = filelist[i];
				System.out.println(filename);

				if (filename.endsWith(".class")) {

					String in_filename = in_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
					String out_filename = out_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
					ClassInfo ci = new ClassInfo(in_filename);
					for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
						Routine routine = (Routine) e.nextElement();
						routine.addBefore("DynamicTool", "dynMethodCount", new Integer(1));
                    
						for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
							BasicBlock bb = (BasicBlock) b.nextElement();
							bb.addBefore("DynamicTool", "dynInstrCount", new Integer(bb.size()));
						}
					}
					ci.addAfter("DynamicTool", "printDynamic", "null");
					ci.write(out_filename);
				}
			}
		}
	
    public static synchronized void printDynamic(String foo) 
		{
			try{
				 PrintWriter writer = new PrintWriter("met"+Thread.currentThread().getId()+".txt", "UTF-8");

			System.out.println("Dynamic information summary:");
			writer.println("Number of methods:      " + dyn_method_count);
			writer.println("Number of basic blocks: " + dyn_bb_count);
			writer.println("Number of instructions: " + dyn_instr_count);
		
			if (dyn_method_count == 0) {
				return;
			}
		
			float instr_per_bb = (float) dyn_instr_count / (float) dyn_bb_count;
			float instr_per_method = (float) dyn_instr_count / (float) dyn_method_count;
			float bb_per_method = (float) dyn_bb_count / (float) dyn_method_count;
		
			writer.println("Average number of instructions per basic block: " + instr_per_bb);
			writer.println("Average number of instructions per method:      " + instr_per_method);
			writer.println("Average number of basic blocks per method:      " + bb_per_method);

			}catch(FileNotFoundException|UnsupportedEncodingException e){
				e.printStackTrace();
			}
			     
		}
    

    public static synchronized void dynInstrCount(int incr) 
		{
			dyn_instr_count += incr;
			dyn_bb_count++;
		}

    public static synchronized void dynMethodCount(int incr) 
		{
			dyn_method_count++;
		}


	public static void doLoadStore(File in_dir, File out_dir) 
		{
			String filelist[] = in_dir.list();
			
			for (int i = 0; i < filelist.length; i++) {
				String filename = filelist[i];
				if (filename.endsWith(".class")) {
					String in_filename = in_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
					String out_filename = out_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
					ClassInfo ci = new ClassInfo(in_filename);

					for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
						Routine routine = (Routine) e.nextElement();
						
						for (Enumeration instrs = (routine.getInstructionArray()).elements(); instrs.hasMoreElements(); ) {
							Instruction instr = (Instruction) instrs.nextElement();
							int opcode=instr.getOpcode();
							if (opcode == InstructionTable.getfield)
								instr.addBefore("StatisticsTool", "LSFieldCount", new Integer(0));
							else if (opcode == InstructionTable.putfield)
								instr.addBefore("StatisticsTool", "LSFieldCount", new Integer(1));
							else {
								short instr_type = InstructionTable.InstructionTypeTable[opcode];
								if (instr_type == InstructionTable.LOAD_INSTRUCTION) {
									instr.addBefore("StatisticsTool", "LSCount", new Integer(0));
								}
								else if (instr_type == InstructionTable.STORE_INSTRUCTION) {
									instr.addBefore("StatisticsTool", "LSCount", new Integer(1));
								}
							}
						}
					}
					ci.addAfter("StatisticsTool", "printLoadStore", "null");
					ci.write(out_filename);
				}
			}	
		}

	public static synchronized void printLoadStore(String s) 
		{
			System.out.println("Load Store Summary:");
			System.out.println("Field load:    " + fieldloadcount);
			System.out.println("Field store:   " + fieldstorecount);
			System.out.println("Regular load:  " + loadcount);
			System.out.println("Regular store: " + storecount);
		}

	public static synchronized void LSFieldCount(int type) 
		{
			if (type == 0)
				fieldloadcount++;
			else
				fieldstorecount++;
		}

	public static synchronized void LSCount(int type) 
		{
			if (type == 0)
				loadcount++;
			else
				storecount++;
		}

	public static void tryDynamic(File in_dir, File out_dir){

		//if (in_dir.isDirectory() && out_dir.isDirectory()) {
			doDynamic(in_dir, out_dir);

	}



	public static void main(String argv[]) 
		{

				try {
					File in_dir = new File(argv[0]);
					File out_dir = new File(argv[1]);

					if (in_dir.isDirectory() && out_dir.isDirectory()) {
						doDynamic(in_dir, out_dir);
					}
					else {
						printUsage();
					}
				}
				catch (NullPointerException e) {
					printUsage();
				}
		}

			public static void printUsage() 
		{
			System.out.println("Syntax: tryDynamic([in_path] ,[out_path])");}
}