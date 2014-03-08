// This C# file is generated from ..\edu\mit\csail\sdg\alloy4compiler\generator\tests0.als

#undef CONTRACTS_FULL

using System;
using System.Linq;
using System.Collections.Generic;
using System.Diagnostics.Contracts;

public class Platform {
}

public class Man {
  public ISet<Tuple<Platform, Platform>> between;
  public Platform ceiling;
  public Platform floor;

  [ContractInvariantMethod]
  private void ObjectInvariant() {
    Contract.Invariant(between != null);
    Contract.Invariant(ceiling != null);
    Contract.Invariant(floor != null);
    Contract.Invariant(between != null && floor != null && Contract.ForAll(between, e1 => floor.Equals(e1.Item1)));
    Contract.Invariant(between != null && ceiling != null && Contract.ForAll(between, e1 => ceiling.Equals(e1.Item2)));
  }
}

public class Date {
}

abstract public class Person {
}

public class Woman : Person {
}

public class Eve : Woman {
  private static Eve instance;
  private Eve() { }
  public static Eve Instance {
    get {
      if (instance == null) {
        instance = new Eve();
      }
      return instance;
    }
  }
}

public static class FuncClass {
  public static bool Above(Man m, Man n) {
    return ((m.floor).Equals((n.ceiling)));
  }
  public static ISet<Tuple<Date, Date>> Closure(ISet<Tuple<Date, Date>> date) {
    Contract.Requires(date != null);
    Contract.Ensures(Contract.Result<ISet<Tuple<Date, Date>>>() != null);

    return Helper.Closure(date);
  }
}

public static class Helper {
  public static ISet<Tuple<L, R>> Closure<L, R>(ISet<Tuple<L, R>> set) {
    ...
  }
  public static ISet<Tuple<L, R>> RClosure<L, R>(ISet<Tuple<L, R>> set) {
    ...
  }
}
