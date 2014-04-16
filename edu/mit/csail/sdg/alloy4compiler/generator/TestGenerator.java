package edu.mit.csail.sdg.alloy4compiler.generator;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4.ErrorFatal;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprVar;
import edu.mit.csail.sdg.alloy4compiler.ast.Func;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Tuple;
import edu.mit.csail.sdg.alloy4compiler.translator.A4TupleSet;

public final class TestGenerator {

  private TestGenerator(A4Solution solution, Iterable<Sig> sigs, Iterable<Pair<String, Expr>> assertions, Iterable<Command> cmds, String originalFilename, PrintWriter out) throws Err {
	 
	  List <String> inititialisedFields = new ArrayList <String>();
	  out.println("// This C# file is generated from .." + originalFilename);
	  
	  out.println();
	  out.println("using System;\nusing System.Linq;\nusing System.Collections.Generic;\nusing System.Diagnostics.Contracts;");
	  out.println();
	  out.println("public static class Test {\n"+
			  "\tpublic static void Main(string[] args) {\n\t// setup test data");
	  
	  
	  for (Sig s : sigs){
		  if (!s.builtin){
			  String label = s.label.substring(5);
			  out.println("\t\tvar " + label + "Set = new HashSet<"+label+">();");
			  for (A4Tuple t : solution.eval(s)){
				  String fieldName = getRidOfDollar (t.toString());
				  String fieldLabel = t.type().toString().substring(6, t.type().toString().length()-1);
				  if (!inititialisedFields.contains(fieldName))
					  out.println("\t\t" + fieldLabel + " " + fieldName + ";");
				  
				  if (t.sig(0).isOne != null){
					  if (inititialisedFields.contains(fieldName)){
						  out.println("\t\t" + label + "Set.Add(" + fieldName+ ");");
					  }
					  else{
						  out.println("\t\t" + label + "Set.Add(" + fieldName + " = " + fieldLabel + ".Instance);");
						  inititialisedFields.add(fieldName);
					  }
				  }else{
					  if (inititialisedFields.contains(fieldName)){
						  out.println("\t\t" + label + "Set.Add(" + fieldName + "());");
					  }
					  else{
						  out.println("\t\t" + label + "Set.Add(" + fieldName + " = new " + label + "());");
						  inititialisedFields.add(fieldName);
					  }
				  }
			  }
			  for (Field f : s.getFields()){
				  for (A4Tuple at : solution.eval(f)){
					  if (at.arity() < 3){ // it is not a tuple
						  String[] temp = at.toString().split("\\->");
						  String oName = getRidOfDollar(temp[0]);
						  String cName = getRidOfDollar(temp[1]);
						  if (f.decl().expr.setOf().toString().substring(0, 10).contains("set")){ // this field is a set
							 
							  out.println("\t\t" + oName + "." + f.label + " = new HashSet<" + f.type().toString().split("\\{this/|}|->this/")[2] + ">();");
							  out.println("\t\t" + oName +  "." + f.label + ".Add(" + cName + ");");
						  }else{
							  out.println("\t\t" + oName + "." + f.label + " = " + cName + ";");
						  }
					  }else {//tuple
						  String[] temp = at.toString().split("\\->");
						  String oName = getRidOfDollar(temp[0]);
						  String cName1 = getRidOfDollar(temp[1]);
						  String cName2 = getRidOfDollar(temp[2]);
						  String [] types = f.type().toString().split("\\{this/|}|->this/");
						  String type1 = types[2];
						  String type2 = types[3];
						  out.println("\t\t" + oName + "." + f.label + " = new HashSet<Tuple<" + type1 + ", " + type2 +">>();");
						  out.println("\t\t" + oName +  "." + f.label + ".Add(Tuple.Create(" + cName1 + ", " + cName2 + "));");
						  
					  }
				  }
			  }
		  }
	  }
	  out.println();
	  out.println("\t// check test data");
	  for (Pair<String, Expr> aPair : assertions){
		 if (aPair.b.toString().contains("0 = 1")) {
			 out.println("\tContract.Assert(true, \"" + aPair.toString() + "\");");
		 }else {

			  out.print("\tContract.Assert(Contract.");
			  
			  String aName = aPair.toString();// BelowOne
			  String ass = aPair.b.toString().substring(1); //assertion text
			  String pNamesTypes [] [] = new String [100][2];
			  int j = 0;
			  String fLabel = "";
			  for (Func f : aPair.b.findAllFunctions()){	
				  fLabel = f.toString().substring(10);
				  for (ExprVar p : f.params()){
					  String pName = p.toString(); // gives m,n
					  String setName = p.type().toString().substring(6, p.type().toString().length()-1) + "Set"; //gives ManSet
					  pNamesTypes [j][0] = pName;
					  pNamesTypes [j][1] = setName;
					  j++;
				  }
			  }
			  int fstQuant = ass.indexOf(pNamesTypes [0][0]); //gives the position of quantificator all, one etc.
			  int sndQuant = ass.indexOf(pNamesTypes [1][0]);
			  String sndQuantS = ass.substring(fstQuant+1, fstQuant + sndQuant);
			  String fstQuantS = ass.substring(0, fstQuant);
			  if (fstQuantS.contains("all")){
				  out.print("ForAll(");
			  }else if (fstQuantS.contains("one")){
				  out.print("ForOne(");
			  }
			  out.print(pNamesTypes [0][1]+", ");
			  out.print (pNamesTypes [0][0] + " => ");
			  out.print(pNamesTypes [1][1]+".Where(");
			  out.print (pNamesTypes [1][0] + " => FuncClass.");
			  out.print(fLabel + "(");
			  out.print(pNamesTypes [0][0] + ", " + pNamesTypes [1][0] + ")).Count() == ");
			  if (sndQuantS.contains("one")){
				  out.print("1), ");
			  }
			  out.println("\"" + aName + "\");");
		 }
	  }
	  
	  out.println("\t}");
	  out.println("}");
	  out.close();
  }
  
  private String getRidOfDollar (String s){

	  String [] fieldNames = s.toString().split("\\$");
	  return fieldNames[0] + fieldNames[1];
	  
  }

  public static void writeTest(A4Solution solution, Module world, String originalFilename, boolean saveInDist) throws Err {
    try {
      String f;
      String ext = ".tests.cs";

      if (saveInDist) {
        f = ".\\" + new File(originalFilename).getName() + ext;
      }
      else {
        f = originalFilename + ext;
      }
      File file = new File(f);
      if (file.exists()) {
        file.delete();
      }

      PrintWriter out = new PrintWriter(new FileWriter(f, true));
      new TestGenerator(solution, world.getAllReachableSigs(), world.getAllAssertions(), world.getAllCommands(), originalFilename, out);
    }
    catch (Throwable ex) {
      if (ex instanceof Err) {
        throw (Err)ex;
      }
      else {
        throw new ErrorFatal("Error writing the generated C# test file.", ex);
      }
    }
  }
}
