

import java.util.Enumeration;
import java.util.HashMap;

import syntaxtree.*;
import visitor.GJDepthFirst;
import visitor.GJNoArguDepthFirst;

public class VisitFunctionDefinitions extends GJDepthFirst<String,Integer> {
	HashMap<String, ClassBinding> symbolTable;
	String currentClass;
	JumpTable jumpTable;
	int tempRegisterCounter;
	int nullCheckCounter;
	int ifCounter;
	int whileCounter;
	String indentationSpacing;
	
	
	public VisitFunctionDefinitions(HashMap<String, ClassBinding> symbolTable, JumpTable jumpTable) {
		this.symbolTable = symbolTable;
		this.jumpTable = jumpTable;
		//Keep track of register names we can use
		tempRegisterCounter = 0;
		indentationSpacing = "  ";
		currentClass = "";
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
	      _ret += n.f1.accept(this,indentation);
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
	    * f0 -> ClassDeclaration()
	    *       | ClassExtendsDeclaration()
	    */
	   public String visit(TypeDeclaration n, Integer indentation) {
	     return n.f0.accept(this,indentation);
	   }
	   
	   /**
	    * f0 -> "class"
	    * f1 -> Identifier()
	    * f2 -> "{"
	    * f3 -> ( VarDeclaration() )*
	    * f4 -> ( MethodDeclaration() )*
	    * f5 -> "}"
	    */
	   public String visit(ClassDeclaration n, Integer indentation) {
	      //Set what class we're in
		  currentClass = SymbolTableVisitor.identifierForIdentifierNode(n.f1);
		  //NOTE: if uninitialized vars allowed, where do we initialize class fields?
		  return n.f4.accept(this,indentation);
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
	   public String visit(ClassExtendsDeclaration n, Integer indentation) {
		   //Set what class we're in
		   //NOTE: need way to copy the extended method declarations
		   currentClass = SymbolTableVisitor.identifierForIdentifierNode(n.f1);
	       //NOTE: if uninitialized vars allowed, where do we initialize class fields?
		   return n.f6.accept(this,indentation);
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
	   public String visit(MethodDeclaration n, Integer indentation) {
		   String methodName = SymbolTableVisitor.identifierForIdentifierNode(n.f2);
		   //NOTE: unitialized variables?
		   assert(!currentClass.isEmpty());
		   methodName = currentClass + "." + methodName;
		   String functionArguments = n.f4.accept(this,indentation);
		   String methodDeclaration = getIndentation(indentation) + "func" + " " + methodName + "(this " + functionArguments + ")";
		   
		   String statements = n.f8.accept(this, indentation + 1);
		   String evalReturnExpression = n.f10.accept(this,indentation+1);
		   String methodString = concatentateInstructions(methodDeclaration, statements);
		   methodString = concatentateInstructions(methodString, evalReturnExpression);
		   String finalRegResult = returnExpressionResultRegister(evalReturnExpression);
		   String returnString = getIndentation(indentation+1) + "ret " + finalRegResult;
		   methodString = concatentateInstructions(methodString, returnString);
	       return methodString;
	   }
	   
	   /**
	    * f0 -> FormalParameter()
	    * f1 -> ( FormalParameterRest() )*
	    */
	   public String visit(FormalParameterList n, Integer indentation) {
		  String param = n.f0.accept(this, indentation);
		  String parameters = param;
		  Enumeration<Node> e = n.f1.elements();
		  while(e.hasMoreElements()) {
			  param = e.nextElement().accept(this,indentation);
			  parameters += " " + param;
		  }
		  return parameters;
	   }
	   
	   /**
	    * f0 -> Type()
	    * f1 -> Identifier()
	    */
	   public String visit(FormalParameter n, Integer indentation) {
	      return SymbolTableVisitor.identifierForIdentifierNode(n.f1);
	   }

	   /**
	    * f0 -> ","
	    * f1 -> FormalParameter()
	    */
	   public String visit(FormalParameterRest n, Integer indentation) {
	      return n.f1.accept(this, indentation);
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
	    * f0 -> "{"
	    * f1 -> ( Statement() )*
	    * f2 -> "}"
	    */
	   public String visit(Block n, Integer indentation) {
	      return n.f1.accept(this,indentation);
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
	   
	   /**
	    * f0 -> "if"
	    * f1 -> "("
	    * f2 -> Expression()
	    * f3 -> ")"
	    * f4 -> Statement()
	    * f5 -> "else"
	    * f6 -> Statement()
	    */
	   public String visit(IfStatement n, Integer indentation) {
	      String evalExpression1 = n.f2.accept(this,indentation);
	      String resultReg = returnExpressionResultRegister(evalExpression1);
	      String stmt1 = n.f4.accept(this,indentation+1);
	      String stmt2 = n.f6.accept(this,indentation+1);
	      return concatentateInstructions(evalExpression1, ifStatement(resultReg, stmt1, stmt2, indentation));
	   }
	   
	   /**
	    * f0 -> "while"
	    * f1 -> "("
	    * f2 -> Expression()
	    * f3 -> ")"
	    * f4 -> Statement()
	    */
	   public String visit(WhileStatement n, Integer indentation ) {
	      String evalExpression = n.f2.accept(this,indentation+1);
	      String resultReg = returnExpressionResultRegister(evalExpression);
	      String statement = n.f4.accept(this,indentation+2);
	      
	      String whileLabel = "while" + String.valueOf(whileCounter);
	      whileCounter++;
	      String whileString = getIndentation(indentation) + whileLabel + "_top:";
	      whileString = concatentateInstructions(whileString, evalExpression);
	      //If result is true, execute while loop statement, else goto end
	      String checkCondition = ifStatement(resultReg,statement,gotoLabel(whileLabel+"_end", indentation+2),indentation+1);
	      checkCondition = concatentateInstructions(checkCondition, gotoLabel(whileLabel + "_top", indentation+1));
	      checkCondition = concatentateInstructions(checkCondition,getIndentation(indentation) + whileLabel + "_end:");
	      checkCondition = concatentateInstructions(whileString, checkCondition);
	      return checkCondition;
	   }

	   
	   
	   /**
	    * f0 -> "System.out.println"
	    * f1 -> "("
	    * f2 -> Expression()
	    * f3 -> ")"
	    * f4 -> ";"
	    */
	   public String visit(PrintStatement n, Integer indentation) {
	      String evalExpression = n.f2.accept(this,indentation);
	      String resultReg = returnExpressionResultRegister(evalExpression);
	      String printString = getIndentation(indentation) + "PrintIntS("+resultReg+")";
	      return concatentateInstructions(evalExpression,printString);
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
	      String and = ifStatement(resultReg1, assign(finalResultReg, resultReg2, indentation+1), assign(finalResultReg, "0", indentation+1), indentation);
	      //Follow our convention of leaving last line to represent final value of the evaluated expression
	      return concatentateInstructions(v1,concatentateInstructions(v2,concatentateInstructions(and, assign(finalResultReg,finalResultReg,indentation))));
	   }
	   
	   /**
	    * f0 -> PrimaryExpression()
	    * f1 -> "<"
	    * f2 -> PrimaryExpression()
	    */
	   public String visit(CompareExpression n, Integer indentation) {
		   String v1 = n.f0.accept(this, indentation);
		   String v2 = n.f2.accept(this,indentation);
		      
		   //Get result of v1
		   String resultReg1 = returnExpressionResultRegister(v1);
		   String resultReg2 = returnExpressionResultRegister(v2);
		   String finalResultReg = getTempRegister();
		   String comparison = assign(finalResultReg,"LtS(" +resultReg1 + " " + resultReg2 + ")",indentation);
		   return concatentateInstructions(v1, concatentateInstructions(v2, comparison));
	   }
	   
	   /**
	    * f0 -> PrimaryExpression()
	    * f1 -> "+"
	    * f2 -> PrimaryExpression()
	    */
	   public String visit(PlusExpression n, Integer indentation) {
		   String v1 = n.f0.accept(this, indentation);
		   String v2 = n.f2.accept(this,indentation);
		      
		   //Get result of v1
		   String resultReg1 = returnExpressionResultRegister(v1);
		   String resultReg2 = returnExpressionResultRegister(v2);
		   String finalResultReg = getTempRegister();
		   String addition = assign(finalResultReg,"Add(" + resultReg1 + " " + resultReg2 + ")",indentation);
		   return concatentateInstructions(v1, concatentateInstructions(v2, addition));
	   }	   
	   
	   /**
	    * f0 -> PrimaryExpression()
	    * f1 -> "-"
	    * f2 -> PrimaryExpression()
	    */
	   public String visit(MinusExpression n, Integer indentation) {
		   String v1 = n.f0.accept(this, indentation);
		   String v2 = n.f2.accept(this,indentation);
		      
		   //Get result of v1
		   String resultReg1 = returnExpressionResultRegister(v1);
		   String resultReg2 = returnExpressionResultRegister(v2);
		   String finalResultReg = getTempRegister();
		   String subtraction = assign(finalResultReg,"Sub(" + resultReg1 + " " + resultReg2 + ")",indentation);
		   return concatentateInstructions(v1, concatentateInstructions(v2, subtraction));
	   }
	   
	   /**
	    * f0 -> PrimaryExpression()
	    * f1 -> "*"
	    * f2 -> PrimaryExpression()
	    */
	   public String visit(TimesExpression n, Integer indentation) {
		   String v1 = n.f0.accept(this, indentation);
		   String v2 = n.f2.accept(this,indentation);
		      
		   //Get result of v1
		   String resultReg1 = returnExpressionResultRegister(v1);
		   String resultReg2 = returnExpressionResultRegister(v2);
		   String finalResultReg = getTempRegister();
		   String multiplication = assign(finalResultReg,"MulS(" + resultReg1 + " " + resultReg2 + ")",indentation);
		   return concatentateInstructions(v1, concatentateInstructions(v2, multiplication));
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
		   
		   if(n.f0.which == 6 || n.f0.which == 7 || n.f0.which == 8)  {
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
	   public String visit(NotExpression n, Integer indentation) {
	      String evalExpression = n.f1.accept(this,indentation);
	      String resultReg = returnExpressionResultRegister(evalExpression);
	      String finalResultReg = getTempRegister();
	      String not = assign(finalResultReg,"Sub(1 " + resultReg + ")",indentation);  
		  return concatentateInstructions(evalExpression,not);
	   }
	  
	   
	   /**
	    * f0 -> "("
	    * f1 -> Expression()
	    * f2 -> ")"
	    */
	   public String visit(BracketExpression n, Integer indentation) {
	      return n.f1.accept(this,indentation);
	   }
	   
	   
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
	   
	   //Simulate an if else statement using vapor jumps. t1 must be a boolean and
	   //t2 and t3 can be arbitrary blocks of code
	   private String ifStatement(String t1, String t2, String t3, Integer indentation) {
		   String jumpLabelElse = "else"+String.valueOf(ifCounter);
		   String jumpLabelElseEnd = "else_end"+String.valueOf(ifCounter);
		   ifCounter += 1;
		   //if false, move to else
		   String ifString = getIndentation(indentation) + "if0 " + t1 + " goto " + ":" +jumpLabelElse;
		   ifString = concatentateInstructions(ifString, concatentateInstructions(t2,getIndentation(indentation+1)+"goto " +":" +jumpLabelElseEnd));
		   String elseString = getIndentation(indentation) + jumpLabelElse + ":";
		   elseString = concatentateInstructions(concatentateInstructions(elseString,t3),getIndentation(indentation)+jumpLabelElseEnd + ":");
		   return concatentateInstructions(ifString, elseString);
	   }
	   
	   private String gotoLabel(String label,Integer indentation) {
		   return getIndentation(indentation) + "goto " + ":" + label;
	   }

}
