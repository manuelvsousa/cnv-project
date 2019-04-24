
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

	private static StatisticsBranch[] branch_info;

	private static int branch_number;
	private static int branch_pc;
	private static String branch_class_name;
	private static String branch_method_name;

	private static String class_name_;
	private static String method_name_;
	private static int pc_;
	private static int taken_;
	private static int not_taken_;



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
						System.out.println("Dynamic information summary:");

			try{
				 PrintWriter writer = new PrintWriter("met"+Thread.currentThread().getId()+".txt", "UTF-8");

						System.out.println("metodos" + dyn_method_count);

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
			writer.close();


			}catch(FileNotFoundException|UnsupportedEncodingException e){
				e.printStackTrace();
			}
			     
		}
    

    public static synchronized void dynInstrCount(int incr) 
		{
		//System.out.println(dyn_instr_count);

			dyn_instr_count += incr;
			dyn_bb_count++;
		}

    public static synchronized void dynMethodCount(int incr) 
		{
			dyn_method_count++;
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
						doBranch(in_dir, out_dir);
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

		public static void doBranch(File in_dir, File out_dir) 
		{
			String filelist[] = in_dir.list();
			int k = 0;
			int total = 0;
			
			for (int i = 0; i < filelist.length; i++) {
				String filename = filelist[i];
				if (filename.endsWith(".class")) {
					String in_filename = in_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
					ClassInfo ci = new ClassInfo(in_filename);

					for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
						Routine routine = (Routine) e.nextElement();
						InstructionArray instructions = routine.getInstructionArray();
						for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
							BasicBlock bb = (BasicBlock) b.nextElement();
							Instruction instr = (Instruction) instructions.elementAt(bb.getEndAddress());
							short instr_type = InstructionTable.InstructionTypeTable[instr.getOpcode()];
							if (instr_type == InstructionTable.CONDITIONAL_INSTRUCTION) {
								total++;
							}
						}
					}
				}
			}
			
			for (int i = 0; i < filelist.length; i++) {
				String filename = filelist[i];
				if (filename.endsWith(".class")) {
					String in_filename = in_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
					String out_filename = out_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
					ClassInfo ci = new ClassInfo(in_filename);

					for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
						Routine routine = (Routine) e.nextElement();
						routine.addBefore("DynamicTool", "setBranchMethodName", routine.getMethodName());
						InstructionArray instructions = routine.getInstructionArray();
						for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
							BasicBlock bb = (BasicBlock) b.nextElement();
							Instruction instr = (Instruction) instructions.elementAt(bb.getEndAddress());
							short instr_type = InstructionTable.InstructionTypeTable[instr.getOpcode()];
							if (instr_type == InstructionTable.CONDITIONAL_INSTRUCTION) {
								instr.addBefore("DynamicTool", "setBranchPC", new Integer(instr.getOffset()));
								instr.addBefore("DynamicTool", "updateBranchNumber", new Integer(k));
								instr.addBefore("DynamicTool", "updateBranchOutcome", "BranchOutcome");
								k++;
							}
						}
					}
					ci.addBefore("DynamicTool", "setBranchClassName", ci.getClassName());
					ci.addBefore("DynamicTool", "branchInit", new Integer(total));
					ci.addAfter("DynamicTool", "printBranch", "null");
					ci.write(out_filename);
				}
			}	
		}

	public static synchronized void setBranchClassName(String name)
		{
			branch_class_name = name;
		}

	public static synchronized void setBranchMethodName(String name) 
		{
			branch_method_name = name;
		}
	
	public static synchronized void setBranchPC(int pc)
		{
			branch_pc = pc;
		}
	
	public static synchronized void branchInit(int n) 
		{
			if (branch_info == null) {
				branch_info = new StatisticsBranch[n];
			}
		}

	public static synchronized void updateBranchNumber(int n)
		{
			branch_number = n;
			
			if (branch_info[branch_number] == null) {
				branch_info[branch_number] = new StatisticsBranch(branch_class_name, branch_method_name, branch_pc);
			}
		}

	public static synchronized void updateBranchOutcome(int br_outcome)
		{
			if (br_outcome == 0) {
				branch_info[branch_number].incrNotTaken();
			}
			else {
				branch_info[branch_number].incrTaken();
			}
		}

	public static synchronized void printBranch(String foo)
		{
			System.out.println("Branch summary:");
			System.out.println("CLASS NAME" + '\t' + "METHOD" + '\t' + "PC" + '\t' + "TAKEN" + '\t' + "NOT_TAKEN");
			
			for (int i = 0; i < branch_info.length; i++) {
				if (branch_info[i] != null) {
					System.out.println(branch_info[i].print());
				}
			}
		}


	private static class StatisticsBranch{ 
		private StatisticsBranch(String class_name, String method_name, int pc) 
			{
				class_name_ = class_name;
				method_name_ = method_name;
				pc_ = pc;
				taken_ = 0;
				not_taken_ = 0;
			}

		private String print() 
			{
				return class_name_ + '\t' + method_name_ + '\t' + pc_ + '\t' + taken_ + '\t' + not_taken_;
			}
		
		private void incrTaken()
			{
				taken_++;
			}

		private void incrNotTaken() 
			{
				not_taken_++;
			}
	}
}