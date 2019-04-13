
import BIT.highBIT.*;
import java.io.*;
import java.util.*;

 /**
  * BitTool based on the ICount BIT tool
  */
public class BitTool {
    private static PrintStream out = null;
    private static int i_count = 0, b_count = 0, m_count = 0;

    /* main reads in all the files class files present in the input directory,
     * instruments them, and outputs them to the specified output directory.
     */
    public static void main(String argv[]) {
        // testing
        BitTool.writeBitToolOutputToFile("");


        File file_in = new File(argv[0]);
        String infilenames[] = file_in.list();
        
        for (int i = 0; i < infilenames.length; i++) {
            String infilename = infilenames[i];
            if (infilename.endsWith(".class")) {
				// create class info object
				ClassInfo ci = new ClassInfo(argv[0] + System.getProperty("file.separator") + infilename);
				
                for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
                    Routine routine = (Routine) e.nextElement();
                    
                    for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
                        BasicBlock bb = (BasicBlock) b.nextElement();
                        bb.addBefore("BitTool", "count", new Integer(bb.size()));
                    }
                }
                ci.addAfter("BitTool", "writeBitToolOutputToFile", ci.getClassName());
                ci.write(argv[1] + System.getProperty("file.separator") + infilename);
            }
        }
    }
    
    public static synchronized void writeBitToolOutputToFile(String foo) {
        try{
            PrintWriter writer = new PrintWriter("bitToolOutput.txt", "UTF-8");
            writer.println("# instructions: " + i_count + "\n");
            writer.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public static synchronized void count(int incr) {
        i_count += incr;
        b_count++;
    }
}

