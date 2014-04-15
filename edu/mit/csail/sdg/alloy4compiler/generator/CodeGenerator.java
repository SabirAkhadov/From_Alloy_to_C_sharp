package edu.mit.csail.sdg.alloy4compiler.generator;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.ErrorFatal;
import edu.mit.csail.sdg.alloy4.SafeList;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprVar;
import edu.mit.csail.sdg.alloy4compiler.ast.Func;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;


public final class CodeGenerator {

  private CodeGenerator(Iterable<Sig> sigs, SafeList<Func> funcs, String originalFilename, PrintWriter out, boolean checkContracts) throws Err {
	  
	  Visitor visitor = new Visitor(out);
	  VisitorFunc visitorFunc = new VisitorFunc(out);
	  
	  out.println("// This C# file is generated from .." + originalFilename);
	  out.println();
	  out.println("#undef CONTRACTS_FULL"); // so that the contracts  != null will not be violated after the creation
	  out.println();
	  out.println("using System;\nusing System.Linq;\nusing System.Collections.Generic;\nusing System.Diagnostics.Contracts;");
	  out.println();
	  
	  for (Sig s : sigs){
		  if (!s.builtin){
			  //here we first print all classes and then visit each field.
			  if (s.isTopLevel()){
				  if (s.isAbstract != null){
					  out.println("abstract public class " + s.toString().substring(5)+ " {");
					  try {
							for (Sig.Field f : s.getFields()) {
								f.accept(visitor);
							}
						} catch (Exception Err) {}
				  }
				  else {
				  out.println("public class " + s.toString().substring(5)+ " {");
				  
					try {
						for (Sig.Field f : s.getFields()) {
							f.accept(visitor);
						}
					} catch (Exception Err) {}
				  }
			  }
			  else { // this sig inherits and has subnodes.
				 
				  out.println("public class "+ s.toString().substring(5)+ " : " +s.getSubnodes().get(0).getHTML().substring(24) + " {");
				  try {
						for (Sig.Field f : s.getFields()) {
							f.accept(visitor);
						}
					} catch (Exception Err) {}
					finally {}	
				  if (s.isOne != null){//singleton
					  String name = s.toString().substring(5);
					  out.println("\tprivate static " + name + " instance;");
					  out.println("\tprivate " + name + "() {}");
					  out.println("\tpublic static " + name + " Instance {");
					  out.println("\t\tget {");
					  out.println("\t\t\tif (instance == null) {");
					  out.println("\t\t\t\tinstance = new " + name + "();");
					  out.println("\t\t\t}");
					  out.println("\t\treturn instance;");
					  out.println("\t\t}");
					  out.println("\t}");
				  }
			  }
			   
			out.println("}");
			out.println();
		  }
	  }
	  
	  
	  
	  
	  
	  // functions
	  String closeBracket = "";
	  if (funcs.size() > 0) {
		  out.println("public static class FuncClass {");
		  closeBracket = "}";
	  }
	  for (Func f : funcs) {
		  if (f.isPred) {
			  out.print("\tpublic static bool ");
			  out.print(f.label.substring(5));
			  out.print(" (");
			  for (int i = 0; i < f.params().size(); i++) {
				  ExprVar p = f.params().get(i);
				  String type = p.type().toString();
				  type = type.substring(6, type.length()-1);
				  out.print(type);
				  out.print(" ");
				  out.print(p.label);
				  if (i+1 < f.params().size()) {
					  out.print(", ");
				  }
			  }
			  
			  out.println(") {");
			  out.print("\t\treturn ");
			  f.getBody().accept(visitorFunc);
			  out.println(";\n\t}");
		  }
		  else {
			  //functions
		  }
	  }
	  
	  out.println(closeBracket);
	  out.close();
  }

  public static void writeCode(Module world, String originalFilename, boolean checkContracts, boolean saveInDist) throws Err {
    try {
      String f;
      String ext = ".cs";
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
      new CodeGenerator(world.getAllReachableSigs(), world.getAllFunc(), originalFilename, out, checkContracts);
    }
    catch (Throwable ex) {
      if (ex instanceof Err) {
        throw (Err)ex;
      }
      else {
        throw new ErrorFatal("Error writing the generated C# code file.", ex);
      }
    }
  }
}
