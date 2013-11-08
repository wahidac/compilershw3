
import java.util.HashMap;
import java.util.Map;

//Given a symbol table as input, create
//a jump table that can be used to 
//reference methods declared by classes.
//This table will be returned as a string
//that should be placed at the very top of the
//vapor file (a list of const references to
//code labels).

public class JumpTable {
	private HashMap<String,ClassBinding> symbolTable;
	//Need to track where in jumptable a method is
	public HashMap<String,HashMap<String,Integer>> methodIndexInJumpTable;
	String vaporJumpTable; //String representation of the table
	
	public JumpTable(HashMap<String,ClassBinding> symbolTable) {
		this.symbolTable = symbolTable;
		this.methodIndexInJumpTable = new HashMap<String,HashMap<String,Integer>>();
		vaporJumpTable = "";
		populateBindingsWithSizes();
		createJumpTable();
	}
	
	private void populateBindingsWithSizes() {
		//Go through bindings and add sizing info so we know how much
		//memory to allocate when we hit the new operator
		for(Map.Entry<String, ClassBinding> c:symbolTable.entrySet()) {
			String className = c.getKey();
			ClassBinding binding = c.getValue();
			int numFields = binding.getAllFields(this.symbolTable).size();
			binding.numBytesToRepresent = 4 + 4*numFields;
		}
	}
	
	private void createJumpTable() {
		for(Map.Entry<String, ClassBinding> c:symbolTable.entrySet()) {
			String className = c.getKey();
			ClassBinding binding = c.getValue();
			if(binding.getMethodBindings().size() == 1 &&
				binding.getMethodBinding("main", symbolTable) != null) {
					//Main function
					continue;
				}
			HashMap<String,Integer> methodOffsets = new HashMap<String,Integer>();
			methodIndexInJumpTable.put(className, methodOffsets);
			//Create the jump table for this class
			String table = "const methods_"+className;
			//Iterate through methods.
			int i = 0;
			for(Map.Entry<String, MethodBinding> m:binding.getAllMethods(symbolTable,className).entrySet()) {
				String methodName = m.getKey();
				String entry = ":" + methodName;
				table += "\n  " + entry;
				methodOffsets.put(methodName, 4*i);
				i++;
			}
			vaporJumpTable += "\n\n" + table;
		}
	}
	
	
	
}
