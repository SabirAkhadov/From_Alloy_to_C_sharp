package kodkod.engine.bool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kodkod.ast.Variable;
import kodkod.ast.operator.Quantifier;
import kodkod.engine.fol2sat.Environment;

public class DefCond {

    /* ------------------------------------------------------------------------------------ */
    /*                         used during translation                                      */
    /* ------------------------------------------------------------------------------------ */
    
    private BooleanValue overflow = BooleanConstant.FALSE;
    private BooleanValue accumOverflow = BooleanConstant.FALSE;
    private Set<Variable> vars = new HashSet<Variable>();
    
    public BooleanValue getOverflow()             { return overflow; }
    public BooleanValue getAccumOverflow()        { return accumOverflow; }
    public void setOverflows(BooleanValue of, BooleanValue accumOF)  { 
        this.overflow = of; 
        this.accumOverflow = accumOF; 
    }
    
    public void addVar(Variable v)                 { vars.add(v); }
    public void addVars(Collection<Variable> vars) { this.vars.addAll(vars); }
    public Set<Variable> vars()                    { return vars; }

    /**
     * ORs overflow circuits of <code>this</code> object (
     * <code>this.mergedOverflow</code>), a given <code>other</code> object (
     * <code>other.mergedOverflow</code>), and a given overflow circuit (
     * <code>of</code>)
     */
    public static BooleanValue merge(BooleanFactory factory, BooleanValue accum, DefCond ... conds) {
        BooleanValue ret = accum; 
        for (DefCond dc : conds) {
            ret = factory.or(ret, dc.accumOverflow);
        }
        return ret;
    }
    
    public static BooleanValue merge(BooleanFactory factory, DefCond ... conds) {
        return merge(factory, BooleanConstant.FALSE, conds);
    }
    
    /**
     * If overflow checking is disabled returns <code>value</code>.  Otherwise, 
     * returns a conjunction of <code>value</code>, <code>lhs.accumOverflow</code>, 
     * and <code>rhs.accumOverflow</code>.
     * 
     * ~~~ NOTE ~~~: Every time a BooleanValue is returned as a result of an operation 
     *               over Ints, one of the <code>ensureNoOverflow</code> methods 
     *               should be called.
     */
    public static BooleanValue ensureDef(BooleanFactory factory, Environment<?, ?> env, 
            BooleanValue value, DefCond ... dcs) {
        if (!factory.noOverflow)
            return value;
        List<DefCond> univQuantInts = new ArrayList<DefCond>(dcs.length);
        List<DefCond> extQuantInts = new ArrayList<DefCond>(dcs.length);
        for (DefCond e : dcs) {
            if (isUnivQuant(env, e))
                univQuantInts.add(e);
            else
                extQuantInts.add(e);
        }
        BooleanValue ret = value; 
        if (!env.isNegated()) {
            for (DefCond e : extQuantInts) ret = factory.and(ret, factory.not(e.getAccumOverflow()));
            for (DefCond e : univQuantInts) ret = factory.or(ret, e.getAccumOverflow());
        } else {
            for (DefCond e : extQuantInts) ret = factory.or(ret, e.getAccumOverflow());
            for (DefCond e : univQuantInts) ret = factory.and(ret, factory.not(e.getAccumOverflow()));
        }
        return ret;
    }
    
    private static boolean isUnivQuant(Environment<?, ?> env, DefCond e) {
        if (env.isEmpty())
            return false;
//        if (!isInt(env.type()))
//            return isUnivQuant(env.parent(), e);
        if (e.vars().contains(env.variable())) {
            return env.envType() == Quantifier.ALL;
        } else {
            return isUnivQuant(env.parent(), e);
        }
    }

//  /**
//  * Returns if this expression represents the Int type.
//  */
// private static boolean isInt(Object expression) {
//     if (expression == null)
//         return false;
//     if (!(expression instanceof Expression))
//         return false;
//     // TODO: this is probably not complete
//     return "ints".equals(expression.toString());
// }

    /* ------------------------------------------------------------------------------------ */
    /*                         used by the evaluator                                        */
    /* ------------------------------------------------------------------------------------ */
    
    private boolean isOverflowFlag = false;
    
    public void setOverflowFlag(boolean overflow) { this.isOverflowFlag = overflow; }
    public boolean isOverflowFlag()               { return this.isOverflowFlag; }
        
}
