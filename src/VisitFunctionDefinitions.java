

import java.util.Enumeration;
import java.util.HashMap;

import syntaxtree.*;
import visitor.GJDepthFirst;
import visitor.GJNoArguDepthFirst;

public class VisitFunctionDefinitions extends GJDepthFirst<String,Integer> {
	HashMap<String, ClassBinding> symbolTable;
	JumpTable jumpTable;
	int tempRegisterCounter;
	int nullCheckCounter;
	int ifCounter;
	String indentationSpacing;
	
	
	public VisitFunctionDefinitions(HashMap<String, ClassBinding> symbolTable, JumpTable jumpTable) {
		this.symbolTable = symbolTable;
		this.jumpTable = jumpTable;
		//Keep track of register names we can use
		tempRegisterCounter = 0;
		indentationSpacing = "  ";
	}
	
	public String getIndentation(Integer indentation) {
		String ret = "";
		for(int i = 0; i<indentation;i++) {
			ret += indentationSpacing;
		}
		return ret;
	}
	
	  public String getTempRegister() {
		   String ret = String.valueOf(tempRegisterCounter);
		   tempRegisterCounter += 1;
		   return "t." + ret;
	  }
	   
	
	 //
	   // Auto class visitors--probably don't need to be overridden.
	   //
	   public String visit(NodeList n, Integer indentation) {
	      String _ret="";
	      int _count=0;
	      for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
	         _ret += "\n" + e.nextElement().accept(this,indentation);
	         _count++;
	      }
	      return _ret;
	   }

	   public String visit(NodeListOptional n, Integer indentation) {
	      if ( n.present() ) {
	         String _ret="";
	         int _count=0;
	         for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
	            _ret += "\n"+e.nextElement().accept(this,indentation);
	            _count++;
	         }
	         return _ret;
	      }
	      else
	         return "";
	   }

	   public String visit(NodeOptional n,Integer indentation) {
	      if ( n.present() )
	         return n.node.accept(this, indentation);
	      else
	         return "";
	   }

	   public String visit(NodeSequence n,Integer indentation) {
	      String _ret="";
	      int _count=0;
	      for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
	         _ret += "\n" + e.nextElement().accept(this, indentation);
	         _count++;
	      }
	      return _ret;
	   }

	   public String visit(NodeToken n) {  System.out.println("At Node Token!"); return "";  }
	
	//
	   // User-generated visitor methods below
	   //
	
	   /**
	    * f0 -> MainClass()
	    * f1 -> ( TypeDeclaration() )*
	    * f2 -> <EOF>
	    */
	   public String visit(Goal n, Integer indentation) {
	      String _ret="";
	      _ret = n.f0.accept(this,indentation);
	     // _ret += n.f1.accept(this,indentation);
	      return jumpTable.vaporJumpTable + "\n\n"+ _ret;
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
	   public String visit(MainClass n, Integer indentation) {
	      String _ret="func Main()";
	      //Visit statements.
	      
	      //NOTE: need to consider uninitialized vars? if so, need to visit var declarations and 
	      //initialize the variables.
	      return _ret + n.f15.accept(this,indentation+1) + "\n" + getIndentation(indentation+1)+"ret";
	   }
   
	   /**
	    * f0 -> Block()
	    *       | AssignmentStatement()
	    *       | ArrayAssignmentStatement()
	    *       | IfStatement()
	    *       | WhileStatement()
	    *       | PrintStatement()
	    */
	   public String visit(Statement n, Integer indentation) {
	      return n.f0.accept(this, indentation);
	   }
	   
	   /**
	    * f0 -> Identifier()
	    * f1 -> "="
	    * f2 -> Expression()
	    * f3 -> ";"
	    */
	   public String visit(AssignmentStatement n, Integer indentation) {
	      String expressionResult = n.f2.accept(this, indentation);
	      String registerWithResult = returnExpressionResultRegister(expressionResult);
	      String assignmentStr = SymbolTableVisitor.identifierForIdentifierNode(n.f0) + " = " + registerWithResult;
	      String ret = expressionResult + "\n" + getIndentation(indentation) + assignmentStr;
	      return ret;
	   }
	   
	   
	   
	   //Convert each expression to vapor code and store the result of the
	   //expression in a temporary register at the end of the translated
	   //block of code so that we can use the expression value.
	   //Expressions:
	   /**
	    * f0 -> AndExpression()
	    *       | CompareExpression()
	    *       | PlusExpression()
	    *       | MinusExpression()
	    *       | TimesExpression()
	    *       | ArrayLookup()
	    *       | ArrayLength()
	    *       | MessageSend()
	    *       | PrimaryExpression()
	    */
	   public String visit(Expression n, Integer indentation) {
	      return n.f0.accept(this, indentation);
	   }
	   
	   /**
	    * f0 -> PrimaryExpression()
	    * f1 -> "&&"
	    * f2 -> PrimaryExpression()
	    */
	   public String visit(AndExpression n, Integer indentation) {
	      String v1 = n.f0.accept(this, indentation);
	      String v2 = n.f2.accept(this,indentation);
	      
	      //Get result of v1
	      String resultReg1 = returnExpressionResultRegister(v1);
	      String resultReg2 = returnExpressionResultRegister(v2);
	      String finalResultReg = getTempRegister();
	      
	      //Use if-else statement to evaluate this expression
	      String and = ifStatement(resultReg1, assign(finalResultReg, "1", indentation), assign(finalResultReg, resultReg2, indentation), indentation);
	      //Follow our convention of leaving last line to represent final value of the evaluated expression
	      return concatentateInstructions(v1,concatentateInstructions(v2,concatentateInstructions(and, assign(finalResultReg,finalResultReg,indentation))));
	   }
	   
	   
	   
	   
	      
	   /**
	    * f0 -> IntegerLiteral()
	    *       | TrueLiteral()
	    *       | FalseLiteral()
	    *       | Identifier()
	    *       | ThisExpression()
	    *       | ArrayAllocationExpression()
	    *       | AllocationExpression()
	    *       | NotExpression()
	    *       | BracketExpression()
	    */
	   public String visit(PrimaryExpression n, Integer indentation) {
		   String ret = n.f0.choice.accept(this, indentation);
		   
		   if(n.f0.which == 6) {
			   //These expressions will already assign result to a temp reg.
			   return ret;
		   }
		   String registerName = getTempRegister();
		   return getIndentation(indentation) + registerName + " = " + ret;
	   }
	   
	   public String returnExpressionResultRegister(String expressionInVapor) {
		   String[] lines = expressionInVapor.split("\\n+");
		   String lastLine = lines[lines.length-1];
		   //Last line assigns expression result to a regsiter
		   String[] splitLines = lastLine.split("\\s+");
		   String register = splitLines[0];
		   if(register.isEmpty()) {
			   register = splitLines[1];
		   }
		   
		   assert(register.startsWith("t."));		   
		   return register;
	   }
	   
	   
	   /**
	    * f0 -> <INTEGER_LITERAL>
	    */
	   public String visit(IntegerLiteral n, Integer indentation) {
		   return n.f0.tokenImage;
	   }
	   
	   /**
	    * f0 -> "true"
	    */
	   public String visit(TrueLiteral n, Integer indentation) {
	      return "1";
	   }	   
	   
	   /**
	    * f0 -> "false"
	    */
	   public String visit(FalseLiteral n, Integer indentation) {
		  return "0";
	   }
	   
	   /**
	    * f0 -> <IDENTIFIER>
	    */
	   public String visit(Identifier n, Integer indentation) {
	      return SymbolTableVisitor.identifierForIdentifierNode(n);
	   }
	   
	   /**
	    * f0 -> "this"
	    */
	   public String visit(ThisExpression n, Integer indentation) {
	      return "this";
	   }
	   
	   /**
	    * f0 -> "new"
	    * f1 -> "int"
	    * f2 -> "["
	    * f3 -> Expression()
	    * f4 -> "]"
	    */
	  /* public R visit(ArrayAllocationExpression n) {
	      R _ret=null;
	      n.f0.accept(this);
	      n.f1.accept(this);
	      n.f2.accept(this);
	      n.f3.accept(this);
	      n.f4.accept(this);
	      return _ret;
	   }*/
	   
	   /**
	    * f0 -> "new"
	    * f1 -> Identifier()
	    * f2 -> "("
	    * f3 -> ")"
	    */
	   public String visit(AllocationExpression n, Integer indentation) {
		  String className = SymbolTableVisitor.identifierForIdentifierNode(n.f1);
		  ClassBinding c = symbolTable.get(className);
		  int numBytesToAlloc = c.numBytesToRepresent;
		  String temporaryReg = getTempRegister();
		  //Need to allocate memory on the heap
		  String allocString = assign(temporaryReg,"HeapAllocZ(" + String.valueOf(numBytesToAlloc) + ")",indentation);
		  //Now need to initialize first 4 bytes to point to the corresponding jump table
		  String jumpTableLabel = ":methods_"+className;
		  allocString = concatentateInstructions(allocString,assign(accessMemory(temporaryReg, 0),jumpTableLabel,indentation));
		  allocString = concatentateInstructions(allocString, checkForNull(temporaryReg, indentation));
		  allocString = concatentateInstructions(allocString, assign(getTempRegister(), temporaryReg, indentation));
		  //NOTE: need to consider case in which main class is allocated?
	      return allocString;
	   }
	   
	
	   /**
	    * f0 -> "!"
	    * f1 -> Expression()
	    */
	/*   public String visit(NotExpression n, Integer indentation) {
	      R _ret=null;
	      n.f0.accept(this);
	      n.f1.accept(this);
	      return _ret;
	   }
	  */
	   
	   /**
	    * f0 -> "("
	    * f1 -> Expression()
	    * f2 -> ")"
	    */
	 /*  public R visit(BracketExpression n) {
	      R _ret=null;
	      n.f0.accept(this);
	      n.f1.accept(this);
	      n.f2.accept(this);
	      return _ret;
	   }*/
	   
	   
	   //Commonly used strings
	   private String accessMemory(String var, int offset) {
		   return "[" + var + "+" + String.valueOf(offset) + "]";
	   }
	   
	   private String assign(String v1, String v2, Integer indentation) {
		   return getIndentation(indentation) + v1 + " = " + v2;
	   }
	   
	   private String concatentateInstructions(String v1, String v2) {
		   return v1 + "\n" + v2;
	   }
	   
	   private String checkForNull(String var, Integer indentation) {
		   String nullLabel = "null"+String.valueOf(nullCheckCounter);
		   nullCheckCounter += 1;
		   String ret = getIndentation(indentation) + "if " + var + " goto :" + nullLabel;
		   String errorString = getIndentation(indentation+1)+"Error(\"null pointer\")";
		   String jumpLabel = getIndentation(indentation) + nullLabel + ":";
		   return concatentateInstructions(concatentateInstructions(ret,errorString),jumpLabel);
	   }
	   
	   //Simulate an if else statement using vapor jumps
	   private String ifStatement(String t1, String t2, String t3, Integer indentation) {
		   String jumpLabelElse = "else"+String.valueOf(ifCounter);
		   String jumpLabelElseEnd = "else_end"+String.valueOf(ifCounter);
		   //if false, move to else
		   String ifString = getIndentation(indentation) + "if0 " + t1 + " goto " + ":" +jumpLabelElse;
		   ifString = concatentateInstructions(ifString, indentationSpacing + t2);
		   String elseString = getIndentation(indentation) + ":"+jumpLabelElse;
		   elseString = concatentateInstructions(concatentateInstructions(elseString, indentationSpacing + t3),getIndentation(indentation)+jumpLabelElseEnd + ":");
		   return concatentateInstructions(ifString, elseString);
	   }
	   

}
