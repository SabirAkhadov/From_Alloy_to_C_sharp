package edu.mit.csail.sdg.alloy4compiler.generator;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import kodkod.ast.Decls;
import edu.mit.csail.sdg.alloy4.ConstList;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4compiler.ast.Decl;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprBinary;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprConstant;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprHasName;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprUnary;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprUnary.Op;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprVar;
import edu.mit.csail.sdg.alloy4compiler.ast.Func;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.PrimSig;
import edu.mit.csail.sdg.alloy4compiler.ast.VisitQuery;


//test commit new rep
public class VisitorFunc extends VisitQuery<Object> {
	protected final PrintWriter out;
	public String Closurefunction = "";
	private String makeClosureFunction(String LType, String RType) {
		StringBuilder out = new StringBuilder();
		out.append("public static class Helper {\n");
		out.append("\tpublic static ISet<Tuple<" + LType + ", " + RType + ">> Closure(ISet<Tuple<" + LType + ", " + RType + ">> set) {\n");
		out.append("\t\tISet<Tuple<" + LType + ", " + RType + ">> newItems = new HashSet<Tuple<" + LType + ", " + RType + ">>();\n");
		out.append("\t\tdo {\n");
		out.append("\t\t\tnewItems.Clear();\n");
		out.append("\t\t\tforeach (Tuple<" + LType + ", " + RType + "> tup in set)\n");
		out.append("\t\t\t\tforeach (Tuple<" + LType + ", " + RType + "> tup2 in set)\n");
		out.append("\t\t\t\t\tif (tup.Item2.Equals(tup2.Item1)) {\n");
		out.append("\t\t\t\t\t\tTuple<int, int> tmp = new Tuple<" + LType + ", " + RType + ">(tup.Item1, tup2.Item2);\n");
		out.append("\t\t\t\t\t\tif (!set.Contains(tmp))\n");
		out.append("\t\t\t\t\t\t\tnewItems.Add(tmp);\n");
		out.append("\t\t\t\t\t}\n");
		out.append("\t\tset.UnionWith(newItems);\n");
		out.append("\t\t} while (newItems.Count > 0);\n");
		out.append("\t\treturn set;\n");
		out.append("\t}\n\n");
		out.append("\tpublic static ISet<Tuple<" + LType + ", " + RType + ">> RClosure(ISet<Tuple<" + LType + ", " + RType + ">> set) {\n");
		out.append("\t\tISet<Tuple<int, int>> newItems = new HashSet<Tuple<" + LType + ", " + RType + ">>();\n");
		out.append("\t\tforeach (Tuple<" + LType + ", " + RType + "> tup in set) {\n");
		out.append("\t\t\tnewItems.Add(new Tuple<" + LType + ", " + RType + ">(tup.Item1, tup.Item1));\n");
		out.append("\t\t\tnewItems.Add(new Tuple<" + LType + ", " + RType + ">(tup.Item2, tup.Item2));\n");
		out.append("\t\t}\n");
		out.append("\t\tset.UnionWith(newItems);\n");
		out.append("\t\treturn Closure(set);\n");
		out.append("\t}\n");
		out.append("}\n");
		return out.toString();
	}
	
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
		else if (f.returnDecl instanceof ExprUnary) {
			ExprUnary expr = ((ExprUnary)f.returnDecl);
			if (expr.op == Op.SOME || expr.op == Op.SOMEOF) {
				return "ISet<" + parseSigListToType(f.returnDecl.type().fold().get(0)) + ">";
			}
		}
		return parseSigListToType(f.returnDecl.type().fold().get(0));
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
	
	public List<Argument> parseFuncParams(Func f) throws Err {
		List<Argument> res = new ArrayList<Argument>();
		List<String> isSome = new ArrayList<String>(); 
		for (Decl decl : f.decls) {
			if (!(decl.expr instanceof ExprUnary)) {
				continue;
			}
			ExprUnary expr = ((ExprUnary)decl.expr);
			if (expr.op == Op.SOME || expr.op == Op.SOMEOF) {
				for (ExprHasName name : decl.names) {
					isSome.add(name.label);
				}
			}
		}
		for (int i = 0; i < f.params().size(); i++) {
			ExprVar p = f.params().get(i);
			String type = parseSigListToType(p.type().fold().get(0));
			if (isSome.contains(p.label)) {
				res.add(new Argument("ISet<" + type + ">", p.label));
			}
			else {
				res.add(new Argument(type, p.label));
			}
		}
		return res;
	}
	
	@Override 
	public Object visit(ExprBinary x) throws Err {
		boolean twoConsts = (x.left instanceof ExprConstant && x.right instanceof ExprConstant);
		switch (x.op) {
			case NOT_EQUALS:
			case IMPLIES:
				if (!twoConsts)
					out.print("!");
				break;
			case EQUALS:
				//this is really weird but otherwise Alloy 0 == 1 should be translated to "true" - this is the hack to do it
				if (twoConsts)
					out.print("!");
				break;
		}
		out.print("(");
		x.left.accept(this);
		switch (x.op) {
			case EQUALS:
			case NOT_EQUALS:
				out.print(".Equals"); 
				break;
			case IMINUS: out.print(" - "); break;
			case IPLUS: out.print(" + "); break;
			case NOT_GT: out.print("<="); break;
			case NOT_GTE: out.print("<"); break;
			case NOT_LT: out.print(">="); break;
			case NOT_LTE: out.print(">"); break;
			case IMPLIES: out.print(" || "); break;
			default: out.print(x.op); break;
		}
		x.right.accept(this);
		out.print(")");
		return null;
	}
	@Override 
	public Object visit(ExprUnary x) throws Err {
		switch (x.op) { 
			case NOOP: x.sub.accept(this); break;
			case RCLOSURE:
			case CLOSURE:
				if (x.op == Op.RCLOSURE)
					out.print("Helper.RClosure(");
				else
					out.print("Helper.Closure(");
				x.sub.accept(this);
				Closurefunction = makeClosureFunction(x.type().fold().get(0).get(0).label.substring(5), x.type().fold().get(0).get(1).label.substring(5));
				break;
			default:
				out.print("unknown ExprUnary encounted:" + x.op + "\n");
				break;
		}
		
		
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
