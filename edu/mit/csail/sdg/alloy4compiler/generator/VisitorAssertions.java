package edu.mit.csail.sdg.alloy4compiler.generator;

import java.io.PrintWriter;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprBinary;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprCall;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprConstant;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprQt;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprUnary;
import edu.mit.csail.sdg.alloy4compiler.ast.VisitQuery;


//test commit new rep
public class VisitorAssertions extends VisitorFunc {
	public VisitorAssertions(PrintWriter out) {
		super(out);
	}
	
	@Override 
	public Object visit(ExprUnary x) throws Err {
		if (x.toString().equals("0 = 1")) {
			out.print("true");
			return null;
		}
		switch (x.op) {
			case NOOP: break;
			default: out.print(x.op); break;
		}
		x.sub.accept(this);
		return null;
	}
	@Override
	public Object visit(ExprQt x) throws Err {
		String label = x.decls.get(0).get().label;
		String type = x.decls.get(0).get().type().fold().get(0).get(0).label.substring(5);
		switch (x.op) {
			case ALL:
				out.print("Contract.ForAll(");
				out.print(type + "Set, " + label + " => ");
				x.sub.accept(this);
				out.print(")");
				break;
			case LONE:
			case ONE:
			case SOME:
			case NO:
				out.print(type + "Set.Where(" + label + " => ");
				x.sub.accept(this);
				if (x.op == ExprQt.Op.ONE)
					out.print(").Count() == 1");
				else if (x.op == ExprQt.Op.LONE)
					out.print(").Count() < 1");
				else if (x.op == ExprQt.Op.SOME)
					out.print(").Count() > 0");
				else
					out.print(").Count() == 0");
				break;
			default:
				out.print("(" + label + "," + type + "," + x.op + ")");
				x.sub.accept(this);
				break;
		}

		
		return null;
	}
	@Override
	public Object visit(ExprCall x) throws Err {
		String funcName = x.fun.label.substring(5);
		out.print("FuncClass.");
		out.print(funcName + "(");
		int size = x.fun.decls.get(0).names.size();
		for (int i = 0; i < size; i++) {
			out.print(x.fun.decls.get(0).names.get(i).label);
			if (i < size - 1) {
				out.print(", ");
			}
		}
		out.print(")");
		return null;
	}
	
	@Override
	public Object visit(ExprConstant x) throws Err {
		switch (x.op) {
			case NUMBER:
			case MAX:
			case MIN:
				out.print("(" + x.num + ")");
				break;
			case STRING:
				out.print("\"" + x.string + "\"");
				break;
			default:
				out.print("(" + x.op.toString().toLowerCase() + ")");
				break;
		}
		return null;
	}

	
}