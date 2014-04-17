package edu.mit.csail.sdg.alloy4compiler.generator;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4compiler.ast.Decl;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprBinary;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprCall;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprConstant;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprHasName;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprList;
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
	public String SpecialFunctions = "";
	public String specialPreConditions = "";
	public String specialPostConditions = "";
	public String returnType = "";
	public final List<Argument> arguments = new ArrayList<Argument>();
	private String makeClosureFunction(String LType, String RType) {
		StringBuilder out = new StringBuilder();
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
		return out.toString();
	}
	
	private String[] getReturnType(Expr a, Expr b) {
		String[] returnType = {"", "", ""};
		ArgVisitor ava = new ArgVisitor();
		ArgVisitor avb = new ArgVisitor();
		try {
			a.accept(ava);
			b.accept(avb);
		} catch (Err e) {
			e.printStackTrace();
		}
		if ((ava.type2 == null && avb.type2 != null) || (ava.type2 != null && avb.type2 == null)) {
			returnType[0] = "ISet<Object>";
		}
		else {
			String type1 = getParent(ava.type1, avb.type1);
			if (ava.type2 != null) {
				String type2 = getParent(ava.type2, avb.type2);
				returnType[0] = "ISet<Tuple<" + type1 + "," + type2 + ">>"; 
			}
			else {
				returnType[0] = "ISet<" + type1 + ">";
			}
		}
		returnType[1] = getType(ava);
		returnType[2] = getType(ava);
		if (!returnType[1].startsWith("ISet<")) {
			returnType[1] = "ISet<" + returnType[1] + ">";
		}
		if (!returnType[2].startsWith("ISet<")) {
			returnType[2] = "ISet<" + returnType[2] + ">";
		}
		return returnType;
	}
	
	private String makeIntersetFunction(Expr a, Expr b) {
		String[] types = getReturnType(a, b);
		StringBuilder res = new StringBuilder();
		res.append("\tpublic static " + types[0] + " MyIntersect(" + types[1] + " a, " + types[2] + " b) {\n");
        res.append("\t\t" + types[0] + " res = new HashSet<" + types[0].substring(5, types[0].length()-1) + ">();\n");
        res.append("\t\tforeach (" + types[1].substring(5, types[1].length()-1) + " tmp in a)\n");
        res.append("\t\t\tres.Add(tmp);\n");
        res.append("\t\tres.IntersectWith(b);\n");
        res.append("\t\treturn res;\n");
        res.append("\t}\n");
        return res.toString();
	}
	private String makeMinusFunction(Expr a, Expr b) {
		String[] types = getReturnType(a, b);
		StringBuilder res = new StringBuilder();
		res.append("\tpublic static " + types[1] + " MyMinus(" + types[1] + " a, " + types[2] + " b) {\n");
        res.append("\t\t" + types[1] + " res = new HashSet<" + types[1].substring(5, types[1].length()-1) + ">();\n");
        res.append("\t\tforeach (" + types[1].substring(5, types[1].length()-1) + " tmp in a)\n");
        res.append("\t\t\tres.Add(tmp);\n");
        res.append("\t\tres.ExceptWith(b);\n");
        res.append("\t\treturn res;\n");
        res.append("\t}\n");
        return res.toString();
	}
	private String makePlusFunction(Expr a, Expr b) {
		String[] types = getReturnType(a, b);
		StringBuilder res = new StringBuilder();
		res.append("\tpublic static " + types[0] + " MyPlus(" + types[1] + " a, " + types[2] + " b) {\n");
        res.append("\t\t" + types[0] + " res = new HashSet<" + types[0].substring(5, types[0].length()-1) + ">();\n");
        res.append("\t\tforeach (" + types[1].substring(5, types[1].length()-1) + " tmp in a)\n");
        res.append("\t\t\tres.Add(tmp);\n");
        res.append("\t\tres.UnionWith(b);\n");
        res.append("\t\treturn res;\n");
        res.append("\t}\n");
        return res.toString();
	}

	
	class ArgVisitor extends VisitQuery<Object> {
		public String contractStr = null;
		public boolean isSet = false;
		public PrimSig type1 = null;
		public PrimSig type2 = null;
		
		@Override
		public Object visit(ExprUnary x) {
			type1 = x.type().fold().get(0).get(0);
			switch(x.op){
				case SOME:
				case SOMEOF:
				case SETOF:
					isSet = true;
					return null;
				default:
					return null;
			}
		}
		
		@Override
		public Object visit (ExprBinary x) {
			type1 = ((ExprUnary)x.left).type().fold().get(0).get(0);
			type2 = ((ExprUnary)x.right).type().fold().get(0).get(0);
			switch (x.op) {
				case ARROW:
					isSet = true;
					return null;
				case SOME_ARROW_SOME:
				case SOME_ARROW_ANY:
					isSet = true;
					return null;
				case SOME_ARROW_LONE:
					isSet = true;
					contractStr = "__expr__.All(el => (__expr__.First().Item2 == null || __expr__.First().Item2.Equals(el.Item2)))";
					return null;
				case SOME_ARROW_ONE: 
					isSet = true;
					return null;
				case ANY_ARROW_LONE:
					isSet = true;
					contractStr = "__expr__.All(el => (__expr__.First().Item2 == null || __expr__.First().Item2.Equals(el.Item2)))";
					return null;
				case ANY_ARROW_ONE:
					isSet = true;
					contractStr = "__expr__.All(el => (__expr__.First().Item2.Equals(el.Item2)))";
					return null;
				case ANY_ARROW_SOME:
					isSet = true;
					return null;
				case ONE_ARROW_ANY:
					isSet = true;
				case ONE_ARROW_SOME:
					isSet = true;
					contractStr = "__expr__.All(el => (__expr__.First().Item1.Equals(el.Item1)))";
					return null;
				case ONE_ARROW_LONE:
					contractStr = "__expr__.All(el => (__expr__.First().Item1 == null || __expr__.First().Item2.Equals(el.Item1)))";
					return null;
				case ONE_ARROW_ONE:
					contractStr = "__expr__.All(el => (__expr__.First().Item1.Equals(el.Item1) && __expr__.First().Item2.Equals(el.Item2)))";
					return null;
				default:
					return null;
			}
		}
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
	
	
	
	public String argumentsNotNullContracts() {
		StringBuilder res = new StringBuilder();
		for (Argument arg : arguments ) {
			if (!arg.isPrimitive) {
				res.append("\t\tContract.Requires(");
				res.append(arg.name);
				res.append(" != null);\n");
			}
		}
		return res.toString();
	}
	public String returnValueNotNullContract() {
		if (!isPrimitiveType(returnType)) {
			return "\t\tContract.Ensures(Contract.Result<" + returnType + ">() != null);";
		}
		return "";
	}

	
	public String joinArgumentList () {
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < arguments.size(); i++) {
			out.append(arguments.get(i).toString());
			if (i < arguments.size() -1) {
				out.append(", ");
			}
		}
		return out.toString();
	}
	
	public void parseReturnType(Func f) {
		if (f.isPred) {
			returnType = "bool";
			return;
		}
		ArgVisitor argVisitor = new ArgVisitor();
		try {
			f.returnDecl.accept(argVisitor);
		} catch (Err e) {
			e.printStackTrace();
		}
		String type = getType(argVisitor);
		if (argVisitor.contractStr != null) {
			specialPostConditions += "\t\tContract.Ensures(" + argVisitor.contractStr.replace("__expr__", "Contract.Result<" + type + ">()") + ");\n";
		}
		returnType = type;
	}
	
	@Deprecated
	public String parseSigListToType(List<PrimSig> types) {
		StringBuilder res = new StringBuilder();
		if (types.size() == 2) {
			res.append("Tuple<");
			if (types.get(0).equals(PrimSig.UNIV)) {
				res.append("Object");
			}
			else {
				res.append(types.get(0).label.substring(5));
			}
			res.append(", ");
			if (types.get(1).equals(PrimSig.UNIV)) {
				res.append("Object");
			}
			else {
				res.append(types.get(1).label.substring(5));
			}
			res.append(">");
		}
		else {
			if (types.get(0).equals(PrimSig.UNIV)) {
				res.append("Object");
			}
			else {
				res.append(types.get(0).label.substring(5));
			}
		}
		return res.toString();
	}
	
	public String getType(ArgVisitor argVisitor) {
		String type = "";
		if (argVisitor.type1 != null && argVisitor.type2 != null) {
			type = "Tuple<" + argVisitor.type1.label.substring(5) + "," + argVisitor.type2.label.substring(5) + ">";
		}
		else {
			type = argVisitor.type1.label.substring(5);
		}
		if (argVisitor.isSet) {
			type = "ISet<" + type + ">"; 
		}
		return type;
	}
	
	
	public void parseFuncParams(Func f) throws Err {
		for (Decl decl : f.decls) {
			ArgVisitor argVisitor = new ArgVisitor();
			try {
				decl.expr.accept(argVisitor);
			}
			catch (Exception e){
				e.printStackTrace();
			}
			String type = getType(argVisitor);
			for (ExprHasName name : decl.names) {
				arguments.add(new Argument(type, name.label));
				if (argVisitor.contractStr != null) {
					specialPreConditions += "\t\tContract.Requires(" + argVisitor.contractStr.replace("__expr__", name.label) + ");\n";
				}
			}
			
		}
	}
	
	public String getParent(PrimSig a, PrimSig b) {
		PrimSig parent = a.leastParent(b);
		if (parent == PrimSig.UNIV) {
			return "Object";
		}
		return parent.label.substring(5);
	}
	
	public Object visit(ExprList x) throws Err {
		out.print("(");
		for (int i = 0; i < x.args.size(); i++) {
			Expr a = x.args.get(i);
			a.accept(this);
			if (i < x.args.size()-1) {
				switch (x.op) {
					case AND:
						out.print(" && ");
						break;
					case OR:
						out.print(" || ");
						break;
				}
			}
		}
		out.print(")");
		return null;
	}
	
	@Override 
	public Object visit(ExprBinary x) throws Err {
		switch (x.op) {
			case NOT_EQUALS:
			case IMPLIES:
			case NOT_IN:
				out.print("!");
				break;
			case INTERSECT:
				SpecialFunctions += makeIntersetFunction(x.left, x.right);
				out.print("Helper.MyIntersect");
				break;
			case MINUS:
				SpecialFunctions += makeMinusFunction(x.left, x.right);
				out.print("Helper.MyMinux");
				break;
			case PLUS:
				SpecialFunctions += makePlusFunction(x.left, x.right);
				out.print("Helper.MyPlus");
				break;
			default: break;
		}
		out.print("(");
		switch (x.op) {
			case IN:
			case NOT_IN:
				x.right.accept(this);
				break;
			default:
				x.left.accept(this);
				break;
		}
		
		switch (x.op) {
			case EQUALS:
			case NOT_EQUALS:
				out.print(".Equals("); 
				break;
			case NOT_IN:
			case IN: 
				out.print(".Contains("); 
				break;
			case IMINUS: out.print(" - "); break;
			case IPLUS: out.print(" + "); break;
			case NOT_GT: out.print("<="); break;
			case NOT_GTE: out.print("<"); break;
			case NOT_LT: out.print(">="); break;
			case NOT_LTE: out.print(">"); break;
			case IMPLIES: out.print(") || ("); break;
			case INTERSECT: out.print(", "); break;
			case MINUS: out.print(", "); break;
			case PLUS: out.print(", ");
			case AND: out.print(" && ");
			case OR: out.print(" || ");
			
			default: out.print(x.op); break;
		}
		switch (x.op) {
			case IN:
			case NOT_IN:
				x.left.accept(this);
				break;
			default:
				x.right.accept(this);
				break;
		}
		out.print(")");
		switch (x.op) {
			case EQUALS:
			case NOT_EQUALS:
			case NOT_IN:
			case IN:
				out.print(")");
				break;
		}
		return null;
	}
	@Override 
	public Object visit(ExprUnary x) throws Err {
		switch (x.op) { 
			case NOOP: 
				x.sub.accept(this); 
				break;
			case RCLOSURE:
			case CLOSURE:
				if (x.op == Op.RCLOSURE)
					out.print("Helper.RClosure(");
				else
					out.print("Helper.Closure(");
				x.sub.accept(this);
				out.print(")");
				SpecialFunctions += makeClosureFunction(x.type().fold().get(0).get(0).label.substring(5), x.type().fold().get(0).get(1).label.substring(5));
				break;
			default:
				out.print("unknown ExprUnary encounted:" + x.op + "\n");
				break;
		}
		return null;
	}
	
	@Override 
	public Object visit(ExprCall x) throws Err {
		out.print("(FuncClass.");
		out.print(x.fun.label.substring(5));
		out.print("(");
		for (int i = 0; i < x.args.size(); i++) {
			out.print(((ExprVar)((ExprUnary)x.args.get(i)).sub).label);
			if (i < x.args.size()-1) {
				out.print(", ");
			}
		}
		out.print("))");
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
