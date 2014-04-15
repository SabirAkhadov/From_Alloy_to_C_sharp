package edu.mit.csail.sdg.alloy4compiler.generator;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4.ErrorFatal;
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
			  "public static void Main(string[] args) {");
	  
	  
	  for (Sig s : sigs){
		  if (!s.builtin){
			  String label = s.label.substring(5);
			  out.println("\tvar " + label + "Set = new HashSet<"+label+">();");
			  for (A4Tuple t : solution.eval(s)){
				  String [] fieldNames = t.toString().split("\\$");
				  String fieldName = fieldNames[0] + fieldNames[1];
				  String fieldLabel = t.type().toString().substring(6, t.type().toString().length()-1);
				  
				  if (!inititialisedFields.contains(fieldName))
					  out.println("\t" + fieldLabel + " " + fieldName + ";");
				  
				  if (t.sig(0).isOne != null){
					  if (inititialisedFields.contains(fieldName)){
						  out.println("\t" + label + "Set.Add(" + fieldName+ ");");
					  }
					  else{
						  out.println("\t" + label + "Set.Add(" + fieldName + " = " + fieldLabel + ".Instance);");
						  inititialisedFields.add(fieldName);
					  }
				  }else{
					  if (inititialisedFields.contains(fieldName)){
						  out.println("\t" + label + "Set.Add(" + fieldName + "());");
					  }
					  else{
						  out.println("\t" + label + "Set.Add(" + fieldName + " = new " + label + "());");
						  inititialisedFields.add(fieldName);
					  }
				  }
				  for (Field f : t.sig(0).getFields()){
					  if (f.type().arity() < 3){
					  out.println("\t" + fieldName + "." + f.label + " = 0;");//TODO proper instances, not 0
					  }
				  }
			  }
		  }
	  }

	  out.println();
	  out.println(solution.toString());
	  out.println();
	  out.println("}");
	  out.close();
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

      System.out.println(f);
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
