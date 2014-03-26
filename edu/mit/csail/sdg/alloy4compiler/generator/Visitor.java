package edu.mit.csail.sdg.alloy4compiler.generator;

import java.io.PrintWriter;
import java.text.MessageFormat.Field;

import edu.mit.csail.sdg.alloy4compiler.ast.VisitQuery;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.ast.Func;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.PrimSig;



public class Visitor extends VisitQuery<Object> {
	
	private final PrintWriter out;
	
	public Visitor(PrintWriter out) {
		this.out = out;
	}

	
	
	@Override
	public Object visit (Sig s) {
		
		out.println("class public " + s.toString().substring(5)+ "{\n");
		if (!s.builtin)
		{
			try {
				for (Sig.Field f : s.getFields()) {
					f.accept(this);
				}
			} catch (Exception Err) {}
			finally {}
		}
		out.println("\n}");
		return null;
	}
	
	@Override
	public Object visit (Sig.Field f) {
		out.println(f.toString());
		return null;
		
	}
}
