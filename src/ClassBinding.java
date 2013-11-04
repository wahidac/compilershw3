
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ClassBinding {
	private HashMap<String, VarType> fields;
	private LinkedHashMap<String,MethodBinding> methods;
	//For when this class extends another class, 
	public String parentClass;
	//numBytesToRepresent = 4 for method table pointer + 4*numFieldsDeclared + 4*numFieldsInherited.
	public int numBytesToRepresent;
	//All fields, including inherited ones
	private HashMap<String,VarType> allFields;
	//All methods, including inherited ones
	private HashMap<String,MethodBinding> allMethods;
	
	public ClassBinding() {
		fields = new HashMap<String,VarType>();
		methods = new LinkedHashMap<String,MethodBinding>();
		parentClass = "";
	}
	
	//Return method binding, considering super-classes if they exist
	public MethodBinding getMethodBinding(String id, HashMap<String,ClassBinding> symbolTable) {
		//Look through the hierarchy to find the binding
		MethodBinding m = methods.get(id);
		if(m == null && !this.parentClass.isEmpty()) {
				ClassBinding c = symbolTable.get(this.parentClass);
				m = c.getMethodBinding(id, symbolTable);
				return m;
		}
		
		return m;
	}
	
	//Return method binding, considering super-classes if they exist
	public VarType getField(String id, HashMap<String,ClassBinding> symbolTable) {
			//Look through the hierarchy to find the field, returning the first one found.
			VarType v = fields.get(id);
			if(v == null && !this.parentClass.isEmpty()) {
					ClassBinding c = symbolTable.get(this.parentClass);
					v= c.getField(id, symbolTable);
					return v;
			}
			
			return v;
	} 
	
	public boolean hasAncestor(String ancestorId, HashMap<String,ClassBinding> symbolTable) {
		//Is there an ancestor w/ the name ancestorId?
		if(parentClass.isEmpty())
			return false;
		
		if(parentClass.equals(ancestorId))
			return true;
		else {
			ClassBinding c = symbolTable.get(this.parentClass);
			return c.hasAncestor(ancestorId, symbolTable);
		}
		
	}
	
	public HashMap<String,MethodBinding> getMethodBindings() {
		return methods;
	}
	
	public HashMap<String,VarType> getFields() {
		return fields;
	}
	
	//Return a map of fields, taking into consideration method overriding
	public HashMap<String,VarType> getAllFields(HashMap<String,ClassBinding> symbolTable) {
		//Take all local fields and put into the map
		if(this.allFields != null)
			return this.allFields;
		this.allFields = new HashMap<String,VarType>();
		this.allFields.putAll(this.fields);
		if(!this.parentClass.isEmpty()) {
			//We inherit fields. Add fields that this class doesn't shadow
			ClassBinding parentBinding = symbolTable.get(this.parentClass);
			HashMap<String,VarType> parentFields = parentBinding.getAllFields(symbolTable);
			for(Map.Entry<String,VarType> v:parentFields.entrySet()) {
				String parentField = v.getKey();
				VarType parentFieldType = v.getValue();
				if(this.allFields.get(parentField) == null) {
					//Class doesn't shadow parent field. 
					this.allFields.put(parentField, parentFieldType);
				}		
			}
		}
		return this.allFields;
	}
	
	//Return a map of fields, taking into consideration method overriding
	public HashMap<String,MethodBinding> getAllMethods(HashMap<String,ClassBinding> symbolTable) {
		//Take all local fields and put into the map
		if(this.allMethods != null)
			return this.allMethods;
		this.allMethods = new HashMap<String,MethodBinding>();
		this.allMethods.putAll(this.methods);
		if(!this.parentClass.isEmpty()) {
			//We inherit methods. Add methods that this class doesn't override
			ClassBinding parentBinding = symbolTable.get(this.parentClass);
			HashMap<String,MethodBinding> parentMethods = parentBinding.getAllMethods(symbolTable);
			for(Map.Entry<String,MethodBinding> v:parentMethods.entrySet()) {
				String parentMethod = v.getKey();
				MethodBinding parentMethodBinding = v.getValue();
				if(this.allMethods.get(parentMethod) == null) {
					//Class doesn't override parent field. 
					this.allMethods.put(parentMethod, parentMethodBinding);
				}		
			}
		}
		return this.allMethods;
	}
	
}
