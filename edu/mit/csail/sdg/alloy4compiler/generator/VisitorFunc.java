package edu.mit.csail.sdg.alloy4compiler.generator;

import java.io.PrintWriter;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprBinary;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprUnary;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprVar;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;
import edu.mit.csail.sdg.alloy4compiler.ast.VisitQuery;



public class VisitorFunc extends VisitQuery<Object> {
	
	private final PrintWriter out;
	
	public VisitorFunc(PrintWriter out) {
		this.out = out;
	}
	
	@Override 
	public Object visit(ExprBinary x) throws Err {
		out.print("(");
		x.left.accept(this);
		switch (x.op) {
			case EQUALS: out.print(".equals"); break;
			case JOIN: out.print("."); break;
			case IMINUS: out.print(" - "); break;
			case IPLUS: out.print(" + "); break;
			default: out.print(x.op); break;
		}
		x.right.accept(this);
		out.print(")");
		return null;
	}
	@Override 
	public Object visit(ExprUnary x) throws Err {
		x.sub.accept(this);
		return null;
	}
	
	@Override
	public Object visit(ExprVar x) throws Err {
		out.print(x.label);
		return null;
	}
	
	@Override
	public Object visit (Sig s) throws Err {
		String type = s.type().toString().substring(6);
		type = type.substring(0, type.length() -1);
		out.print(type);
		return null;
	}
	
	@Override
	public Object visit (Field f) throws Err {
		out.print(f.label);
		return null;
	}
	
	
}
