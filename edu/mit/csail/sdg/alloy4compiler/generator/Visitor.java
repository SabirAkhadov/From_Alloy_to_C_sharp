package edu.mit.csail.sdg.alloy4compiler.generator;

import java.io.PrintWriter;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.VisitQuery;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;
import edu.mit.csail.sdg.alloy4compiler.ast.Func;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprHasName;
import edu.mit.csail.sdg.alloy4compiler.ast.Decl;
import edu.mit.csail.sdg.alloy4compiler.ast.Type;
import edu.mit.csail.sdg.alloy4compiler.ast.Type.ProductType;



public class Visitor extends VisitQuery<Object> {
	
	private final PrintWriter out;
	
	public Visitor(PrintWriter out) {
		this.out = out;
	}

	
	
	@Override
	public Object visit (Sig s) {
		
		out.print("public " + s.toString().substring(5) + " ");
		return null;
		
	}
	
	@Override
	public Object visit (Field f) throws Err {
		
		int arity = f.type().arity();
		if (arity < 3)
		{
			f.decl().expr.accept(this);
			out.println(f.label + ";");
		}
		else { // its a tuple
			out.print ("public ISet<Tuple<");
			toCSString (f.decl().expr.type().toString());
			out.println(">> " + f.label + ";");
		}
		return null;
	}
	
	public void toCSString (String s)
	{
		String[] t = s.split("\\{this/|}|->this/");
		for (int i = 1; i < t.length-1; i++) {
			String st = t[i];
			out.print(st + ", ");
		}
		out.print(t[t.length-1]);
	}
}
