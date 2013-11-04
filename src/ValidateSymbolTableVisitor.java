

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import syntaxtree.ClassDeclaration;
import syntaxtree.ClassExtendsDeclaration;
import syntaxtree.ExpressionRest;
import syntaxtree.FormalParameter;
import syntaxtree.Identifier;
import syntaxtree.MainClass;
import syntaxtree.MethodDeclaration;
import syntaxtree.Node;
import syntaxtree.NodeList;
import syntaxtree.NodeListOptional;
import syntaxtree.NodeOptional;
import syntaxtree.NodeSequence;
import syntaxtree.NodeToken;
import syntaxtree.VarDeclaration;
import visitor.DepthFirstVisitor;

//Visitor to run after symbol table is created to validate
//that references to identifiers are valid, no overloading
//occurs
public class ValidateSymbolTableVisitor extends DepthFirstVisitor {
		HashMap<String, ClassBinding> symbolTable;
		public boolean programIsValid;
	 	public ClassBinding currentClassBinding;
	    public MethodBinding currentMethodBinding;

		public ValidateSymbolTableVisitor(HashMap<String, ClassBinding> symbolTable) {
			this.symbolTable = symbolTable;
			this.programIsValid = true;
		}
		
		 /**
		    * Grammar production:
		    * f0 -> "class"
		    * f1 -> Identifier()
		    * f2 -> "{"
		    * f3 -> ( VarDeclaration() )*
		    * f4 -> ( MethodDeclaration() )*
		    * f5 -> "}"
		    */
		   public void visit(ClassDeclaration n) {
			   String identifier = n.f1.f0.toString();
			   currentClassBinding = symbolTable.get(identifier);
			   currentMethodBinding = null;
			   //Visit variable declarations
			   n.f3.accept(this);
			   //Visit method declarations
			   n.f4.accept(this);
			   
			   currentClassBinding = null;
			   currentMethodBinding = null;
		   }
		   
		   /**
		    * f0 -> "class"
		    * f1 -> Identifier()
		    * f2 -> "extends"
		    * f3 -> Identifier()
		    * f4 -> "{"
		    * f5 -> ( VarDeclaration() )*
		    * f6 -> ( MethodDeclaration() )*
		    * f7 -> "}"
		    */
		   public void visit(ClassExtendsDeclaration n) {
			   String identifier = n.f1.f0.toString();
			   currentClassBinding = symbolTable.get(identifier);
			   currentMethodBinding = null;
			   //Check that the class this class extends actually exists
			   String parentId = SymbolTableVisitor.identifierForIdentifierNode(n.f3);
			   ClassBinding parentClassBinding = symbolTable.get(parentId);
			   if(parentClassBinding == null) {
				   System.err.println("Extended class doesn't exist!");
				   programIsValid = false;
				   return;
			   }
			   
			   //Visit variable declarations
			   n.f5.accept(this);
			   //Visit method declarations
			   n.f6.accept(this);
			   
			   //Check that this class doesn't override any ancestor methods
			   for(Map.Entry<String, MethodBinding> v:currentClassBinding.getMethodBindings().entrySet()) {
				   String methodName = v.getKey();
				   parentId = currentClassBinding.parentClass;
				   ClassBinding parentBinding = symbolTable.get(parentId);
				   MethodBinding b = v.getValue();
				   
				   while(parentBinding != null) {
					   MethodBinding parentMethodBinding = parentBinding.getMethodBindings().get(methodName);
					   if(parentMethodBinding != null) {
						   //Check whether this is valid override.
						   boolean methodSignaturesMatch = true;
						   
						   //Same return type?
						   if(b.returnValue.type == VariableType.CLASS) {
							   if(parentMethodBinding.returnValue.type != VariableType.CLASS ||
									   !parentMethodBinding.returnValue.className.equals(b.returnValue.className))
								   methodSignaturesMatch = false;
						   } else if(b.returnValue.type != parentMethodBinding.returnValue.type) {
							   methodSignaturesMatch = false;
						   }
						  
								   
					       //Same parameters?
						   Iterator<Map.Entry<String, VarType>> itr = parentMethodBinding.parameters.entrySet().iterator();
						   for(Map.Entry<String, VarType> param:b.parameters.entrySet()) {
							   VarType bVal = param.getValue();
							   VarType pVal;
							   if(itr.hasNext()) {
								   pVal = itr.next().getValue();
							   } else {
								   methodSignaturesMatch = false;
								   break;
							   }
							   
							   if(bVal.type == VariableType.CLASS) {
								   if(pVal.type != VariableType.CLASS ||
										   !pVal.className.equals(bVal.className))
									   methodSignaturesMatch = false;
							   } else if(bVal.type != pVal.type) {
								   methodSignaturesMatch = false;
							   }
						   }
						
						   if(itr.hasNext()) {
							   methodSignaturesMatch = false;
						   }
								   
								   						   
						   if(!methodSignaturesMatch) {
							  // System.err.println("Method overide!:" + methodName);
							   programIsValid = false;
							   return;
						   }
					   }  
					   parentId = parentBinding.parentClass;
					   parentBinding = symbolTable.get(parentId);
				   }   
			   }
			   
			   currentClassBinding = null;
			   currentMethodBinding = null;
		   }
		   
		   /**
		    * Grammar production:
		    * f0 -> "public"
		    * f1 -> Type()
		    * f2 -> Identifier()
		    * f3 -> "("
		    * f4 -> ( FormalParameterList() )?
		    * f5 -> ")"
		    * f6 -> "{"
		    * f7 -> ( VarDeclaration() )*
		    * f8 -> ( Statement() )*
		    * f9 -> "return"
		    * f10 -> Expression()
		    * f11 -> ";"
		    * f12 -> "}"
		    */
		   public void visit(MethodDeclaration n) {
			   String identifier = n.f2.f0.toString();
			   currentMethodBinding = currentClassBinding.getMethodBindings().get(identifier);
			   VarType retVal = currentMethodBinding.returnValue;
			   if(retVal.type == VariableType.CLASS) {
				   if(symbolTable.get(retVal.className) == null) {
					  // System.err.println("Returned class type does not exist!");
					   programIsValid = false;
					   return;
				   }
					   
			   }
			   
			   //Visit the parameter list
			   n.f4.accept(this);
			   
			   //Visit the local variable declarations
			   n.f7.accept(this);
			   		   
			   currentMethodBinding = null;
		   }
		   
		   /**
		    * f0 -> Type()
		    * f1 -> Identifier()
		    */
		   public void visit(FormalParameter n) {
			   if(currentMethodBinding == null) {
				   System.err.println("No current method defined for parameter list!");
				   System.exit(0);
			   } 
			   
			   String identifier = SymbolTableVisitor.identifierForIdentifierNode(n.f1);
			   VarType type = currentMethodBinding.parameters.get(identifier);
			   if(type.type == VariableType.CLASS ) {
				   assert(n.f0.f0.choice instanceof Identifier);
				   //Make sure this class exists
				   ClassBinding c = symbolTable.get(type.className);
				   if(c == null) {
					   System.err.println("Parameter type does not exist");
					   programIsValid = false;
					   return;
				   }
		   		}				   
		   }
		
		   /**
		    * Grammar production:
		    * f0 -> "class"
		    * f1 -> Identifier()
		    * f2 -> "{"
		    * f3 -> "public"
		    * f4 -> "static"
		    * f5 -> "void"
		    * f6 -> "main"
		    * f7 -> "("
		    * f8 -> "String"
		    * f9 -> "["
		    * f10 -> "]"
		    * f11 -> Identifier()
		    * f12 -> ")"
		    * f13 -> "{"
		    * f14 -> ( VarDeclaration() )*
		    * f15 -> ( Statement() )*
		    * f16 -> "}"
		    * f17 -> "}"
		    */
		   public void visit(MainClass n) {
			   //The class name
			   String identifier = SymbolTableVisitor.identifierForIdentifierNode(n.f1);
			   currentClassBinding = symbolTable.get(identifier);
			   currentMethodBinding = currentClassBinding.getMethodBindings().get("main");
			   			   
			  //Visit the variable declarations
			   n.f14.accept(this);
			   
			   currentClassBinding = null;
			   currentMethodBinding = null;
		   }
		   
		   /**
		    * Grammar production:
		    * f0 -> Type()
		    * f1 -> Identifier()
		    * f2 -> ";"
		    */
		   public void visit(VarDeclaration n) {
			   	  //Get variable type and add it to the correct binding of the
			   	  //current class
				  String identifier = SymbolTableVisitor.identifierForIdentifierNode(n.f1);
				  VarType type;
				  if(this.currentMethodBinding != null) {
					  //Var declaration for method in current class
					  type = currentMethodBinding.locals.get(identifier);
				  } else {
					  //Var declaration for a field in the class
					  type = currentClassBinding.getFields().get(identifier);
				  }
				  
				  if(type.type == VariableType.CLASS ) {
					   //Make sure this class exists
					   ClassBinding c = symbolTable.get(type.className);
					   if(c == null) {
						   System.err.println("Declared variable type does not exist!");
						   programIsValid = false;
						   return;
					   }
			   	  }	
		   }
		   
}
