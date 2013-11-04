
import java.util.Enumeration;
import java.util.HashMap;

import syntaxtree.*;
import visitor.GJNoArguDepthFirst;

public class VisitFunctionDefinitions extends GJNoArguDepthFirst<String> {
	HashMap<String, ClassBinding> symbolTable;
	JumpTable jumpTable;
	
	public void setCurrentClassBinding(ClassBinding b) {
		currentClassBinding = b;
		typeCalculator.currentClassBinding = currentClassBinding;
	}
	
	public void setCurrentMetodBinding(MethodBinding b) {
		currentMethodBinding = b;
		typeCalculator.currentMethodBinding = currentMethodBinding;
	}
	
	public void setCurrentClass(String id) {
		currentClass = id;
		typeCalculator.currentClass = currentClass;
	}
	
	public VisitFunctionDefinitions(HashMap<String, ClassBinding> symbolTable, JumpTable jumpTable) {
		this.symbolTable = symbolTable;
		this.jumpTable = jumpTable;
	}
	
	//
	   // Auto class visitors--probably don't need to be overridden.
	   //
	   public Boolean visit(NodeList n) {
	      Boolean _ret= true;
	      int _count=0;
	      for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
	         _ret = e.nextElement().accept(this);
	         if(_ret.booleanValue() != true)
	        	 return false;
	         _count++;
	      }
	      return _ret;
	   }

	   public Boolean visit(NodeListOptional n) {
	      if ( n.present() ) {
	         Boolean _ret=true;
	         int _count=0;
	         for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
		         _ret = e.nextElement().accept(this);
		         if(_ret.booleanValue() != true)
		        	 return false;
	            _count++;
	         }
	         return _ret;
	      }
	      else
	         return true;
	   }

	   public Boolean visit(NodeOptional n) {
	      if ( n.present() )
	         return n.node.accept(this);
	      else
	         return true;
	   }

	   public Boolean visit(NodeSequence n) {
	      Boolean _ret=true;
	      int _count=0;
	      for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
		         _ret = e.nextElement().accept(this);
		         if(_ret.booleanValue() != true) 
		        	 return false;
		         _count++;
	      }
	      return _ret;
	   }

	   public Boolean visit(NodeToken n) { System.out.println("At Node Token!"); return true;  }
	
	

	   /**
	    * f0 -> MainClass()
	    * f1 -> ( TypeDeclaration() )*
	    * f2 -> <EOF>
	    */
	   public Boolean visit(Goal n) {
	      Boolean _ret=true;
	      _ret = n.f0.accept(this);
	      if(!_ret)
	    	  return _ret;
	      else
	    	  return n.f1.accept(this);
	   }
	   
	   /**
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
	   public Boolean visit(MainClass n) {
		  String id = SymbolTableVisitor.identifierForIdentifierNode(n.f1);
		  //Set scopes 
		  setCurrentClassBinding(symbolTable.get(id));
		  setCurrentMetodBinding(currentClassBinding.getMethodBindings().get("main"));
		  setCurrentClass(id);
		  Boolean _ret = n.f15.accept(this);
		  
		  //Reset scope
		  setCurrentClassBinding(null);
		  setCurrentMetodBinding(null);
		  setCurrentClass(null);
		  
	      return _ret;
	   }
	   
	   /**
	    * f0 -> ClassDeclaration()
	    *       | ClassExtendsDeclaration()
	    */
	   public Boolean visit(TypeDeclaration n) {
	      return n.f0.accept(this);
	   }
	   
	   /**
	    * f0 -> "class"
	    * f1 -> Identifier()
	    * f2 -> "{"
	    * f3 -> ( VarDeclaration() )*
	    * f4 -> ( MethodDeclaration() )*
	    * f5 -> "}"
	    */
	   public Boolean visit(ClassDeclaration n) {
	      Boolean _ret=true;
		  String id = SymbolTableVisitor.identifierForIdentifierNode(n.f1);
		  
		  //Set scopes 
		  setCurrentClassBinding(symbolTable.get(id));
		  setCurrentMetodBinding(null);
		  setCurrentClass(id);
		  
	      _ret = n.f4.accept(this);
	      
	      //Reset scopes
		  setCurrentClassBinding(null);
		  setCurrentMetodBinding(null);
		  setCurrentClass(null);
		  
	      return _ret;
	   }
	   
	   /**
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
	   public Boolean visit(MethodDeclaration n) {
	      Boolean _ret=false;
		  String id = SymbolTableVisitor.identifierForIdentifierNode(n.f2);
		  setCurrentMetodBinding(currentClassBinding.getMethodBindings().get(id));
		  _ret = n.f8.accept(this);
		  //Need to make sure return type is what it should be
		  VarType v = n.f10.accept(typeCalculator,symbolTable);
		  VarType expectedReturnType = currentMethodBinding.returnValue;
		  
		  if(!VarType.canAssignVarType(expectedReturnType, v, symbolTable))
			  _ret = false;
		  setCurrentMetodBinding(null);
		
	      return _ret;
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
	   public Boolean visit(ClassExtendsDeclaration n) {
		   	  Boolean _ret=true;
			  String id = SymbolTableVisitor.identifierForIdentifierNode(n.f1);
			  
			  //Set scopes 
			  setCurrentClassBinding(symbolTable.get(id));
			  setCurrentMetodBinding(null);
			  setCurrentClass(id);
			  
			  assert(!currentClassBinding.parentClass.isEmpty());
			  
		      _ret = n.f6.accept(this);
		      
		      //Reset scopes
			  setCurrentClassBinding(null);
			  setCurrentMetodBinding(null);
			  setCurrentClass(null);
			  
		      return _ret;
	   }
	   
	   
	   
	   
	   
	   
	   
	   
	   
	   
	   
	   
	   
	   
	   
	   
	   /**
	    * f0 -> Block()
	    *       | AssignmentStatement()
	    *       | ArrayAssignmentStatement()
	    *       | IfStatement()
	    *       | WhileStatement()
	    *       | PrintStatement()
	    */
	   public Boolean visit(Statement n) {
	      return n.f0.accept(this);
	   }
	   
	   /**
	    * f0 -> "{"
	    * f1 -> ( Statement() )*
	    * f2 -> "}"
	    */
	   public Boolean visit(Block n) {
	      return n.f1.accept(this);
	   }
	   
	   
	   /**
	    * f0 -> Identifier()
	    * f1 -> "="
	    * f2 -> Expression()
	    * f3 -> ";"
	    */
	   public Boolean visit(AssignmentStatement n) {
		  Boolean ret = false;
		  VarType v1 = n.f0.accept(typeCalculator, symbolTable);
	      VarType v2 = n.f2.accept(typeCalculator, symbolTable);
	      
		  if(v1 == null || v2 == null)
			  return false;
		  
	      return VarType.canAssignVarType(v1, v2, symbolTable);
	   }
	   
	   /**
	    * f0 -> Identifier()
	    * f1 -> "["
	    * f2 -> Expression()
	    * f3 -> "]"
	    * f4 -> "="
	    * f5 -> Expression()
	    * f6 -> ";"
	    */
	   public Boolean visit(ArrayAssignmentStatement n) {
		  VarType v1 = n.f0.accept(typeCalculator, symbolTable);
		  VarType v2 = n.f2.accept(typeCalculator, symbolTable);
		  VarType v3 = n.f5.accept(typeCalculator, symbolTable);
		  
		  if(v1 == null || v2 == null || v3 == null)
			  return false;

		  if(v1.type == VariableType.INT_ARRAY && v2.type == VariableType.INTEGER
				  && v3.type == VariableType.INTEGER) {
			  return true;
		  } else {
			  return false;
		  }
	   }
	   
	   /**
	    * f0 -> "if"
	    * f1 -> "("
	    * f2 -> Expression()
	    * f3 -> ")"
	    * f4 -> Statement()
	    * f5 -> "else"
	    * f6 -> Statement()
	    */
	   public Boolean visit(IfStatement n) {	  
		  Boolean _ret = true;
		  VarType v1 = n.f2.accept(typeCalculator, symbolTable);
		  if(v1 == null)
			  return false;
		  
		  if(v1.type != VariableType.BOOLEAN)
			  return false;
			  
		  //Make sure f4 and f6 statements all type check
		  Boolean b1 = n.f4.accept(this);
		  Boolean b2 = n.f6.accept(this);
		  
	      return b1 && b2;
	   }
	   
	   /**
	    * f0 -> "while"
	    * f1 -> "("
	    * f2 -> Expression()
	    * f3 -> ")"
	    * f4 -> Statement()
	    */
	   public Boolean visit(WhileStatement n) {
	      VarType v1 = n.f2.accept(typeCalculator, symbolTable);
	      if(v1 == null)
			  return false;
	      if(v1.type != VariableType.BOOLEAN)
			  return false;
	      return n.f4.accept(this);
	   }
	   
	   /**
	    * f0 -> "System.out.println"
	    * f1 -> "("
	    * f2 -> Expression()
	    * f3 -> ")"
	    * f4 -> ";"
	    */
	   public Boolean visit(PrintStatement n) {
	      VarType v1 = n.f2.accept(typeCalculator, symbolTable);
	      if(v1 == null)
			  return false;
	      if(v1.type == VariableType.INTEGER)
	    	  return true;
	      else
	    	  return false;
	   }
	
}
