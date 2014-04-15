package edu.mit.csail.sdg.alloy4compiler.generator;

import java.io.PrintWriter;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;
import edu.mit.csail.sdg.alloy4compiler.ast.VisitQuery;



public class Visitor extends VisitQuery<Object> {
	
	private final PrintWriter out;
	
	private static boolean print = true;
	
	public Visitor(PrintWriter out) {
		this.out = out;
	}

	
	
	@Override
	public Object visit (Sig s) {
		if (s.toString().length() > 5){
			out.print("\tpublic " + s.toString().substring(5) + " ");
		}else{
			out.print("\tpublic " + s.toString().toLowerCase() + " "); //it is probably int
		}
		return null;
	}
	 
	
	/* Complicated Fields
	 * sig Man {
	    ceiling, floor: one Platform,
	    between: floor -> ceiling,
	    test: Man,
	    testtest: test,
	    testtesttest: testtest
	   }
	 * 
	 * should be: 
	 * public Man test;
	 * public Man testtest;
	 * public Man testtesttest;
	 * 
	 */
	
	
	
	@Override
	public Object visit (Field f) throws Err {
		Expr e = f.decl().expr;
		int arity = f.type().arity();
		if (arity < 3)
		{
			if (e.getDepth() > 3){ 
				if (print){
					print = false;
					e.accept(this);
					out.println(f.label + ";");
					print = true;
				}
				else{
					e.accept(this);
				}
			}else {
				e.accept(this);
				if (print){
				out.println(f.label + ";");
				}
			}
		}
		else { // its a tuple
			out.print ("\tpublic ISet<Tuple<");
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
