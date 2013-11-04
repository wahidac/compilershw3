import java.io.FileInputStream;
import java.io.FileNotFoundException;

import syntaxtree.Node;


public class J2V {
   public static void main(String [] args) {
      try {
    	 Node root;
    	 if(args.length == 1) {
    		FileInputStream stream = new FileInputStream(args[0]); 
    		root = new MiniJavaParser(stream).Goal();
    	 } else {
            root = new MiniJavaParser(System.in).Goal();
    	 }
    	 
         SymbolTableVisitor symVisitor = new SymbolTableVisitor();
         //Create the symbol table
         root.accept(symVisitor);
         JumpTable c = new JumpTable(symVisitor.table);
         //Now implement the functions
         System.out.print(c.vaporJumpTable);
      }
      catch (ParseException e) {
         System.out.println(e.toString());
      } catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
   }
}
