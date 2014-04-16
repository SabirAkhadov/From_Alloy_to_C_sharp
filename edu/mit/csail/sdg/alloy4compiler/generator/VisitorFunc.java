package edu.mit.csail.sdg.alloy4compiler.generator;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprBinary;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprUnary;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprVar;
import edu.mit.csail.sdg.alloy4compiler.ast.Func;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.PrimSig;
import edu.mit.csail.sdg.alloy4compiler.ast.VisitQuery;


//test commit new rep
public class VisitorFunc extends VisitQuery<Object> {
	private final PrintWriter out;
	
	public VisitorFunc(PrintWriter out) {
		this.out = out;
	}
	
	private boolean isPrimitiveType(String type) {
		return "bool".equals(type) || "int".equals(type);
	}
	public class Argument {
		public final boolean isPrimitive;
		public final String type;
		public final String name; 
		public Argument(String type, String name) {
			isPrimitive = isPrimitiveType(type);
			this.type = type; 
		    this.name = name; 
		} 
		@Override
		public String toString() {
			return type + " " + name;
		}
	}
	
	public String argumentsNotNullContracts(List<Argument> args) {
		StringBuilder res = new StringBuilder();
		for (Argument arg : args) {
			if (!arg.isPrimitive) {
				res.append("Contract.Requires(");
				res.append(arg.name);
				res.append(" != null);\n");
			}
		}
		return res.toString();
	}
	public String returnValueNotNullContract(String type) {
		if (!isPrimitiveType(type)) {
			return "Contract.Ensures(Contract.Result<" + type + ">() != null);";
		}
		return "";
	}

	
	public String joinArgumentList (List<Argument> args) {
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < args.size(); i++) {
			out.append(args.get(i).toString());
			if (i < args.size() -1) {
				out.append(", ");
			}
		}
		return out.toString();
	}
	
	public String parseReturnType(Func f) {
		if (f.isPred) {
			return "bool";
		}
		else {
			return parseSigListToType(f.returnDecl.type().fold().get(0));
		}
	}
	public String parseSigListToType(List<PrimSig> types) {
		StringBuilder res = new StringBuilder();
		if (types.size() == 2) {
			res.append("ISet<Tuple<");
			res.append(types.get(0).label.substring(5));
			res.append(", ");
			res.append(types.get(1).label.substring(5));
			res.append(">>");
		}
		else {
			res.append(types.get(0).label.substring(5));
		}
		return res.toString();
	}
	
	public List<Argument> parseFuncParams(Func f) {
		List<Argument> res = new ArrayList<Argument>();
		for (int i = 0; i < f.params().size(); i++) {
			ExprVar p = f.params().get(i);
			res.add(new Argument(parseSigListToType(p.type().fold().get(0)), p.label));
		}
		return res;
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
