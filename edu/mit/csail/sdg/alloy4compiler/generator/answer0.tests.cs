// This C# file is generated from ..\edu\mit\csail\sdg\alloy4compiler\generator\tests0.als

using System;
using System.Linq;
using System.Collections.Generic;
using System.Diagnostics.Contracts;

public static class Test {
  public static void Main(string[] args) {
    // setup test data
    var PlatformSet = new HashSet<Platform>();
    Platform Platform0;
    PlatformSet.Add(Platform0 = new Platform());
    var ManSet = new HashSet<Man>();
    Man Man0;
    ManSet.Add(Man0 = new Man());
    Man Man1;
    ManSet.Add(Man1 = new Man());
    Man0.ceiling = Platform0;
    Man1.ceiling = Platform0;
    Man0.floor = Platform0;
    Man1.floor = Platform0;
    var DateSet = new HashSet<Date>();
    var PersonSet = new HashSet<Person>();
    Eve Eve0;
    PersonSet.Add(Eve0 = Eve.Instance);
    var WomanSet = new HashSet<Woman>();
    WomanSet.Add(Eve0);
    var EveSet = new HashSet<Eve>();
    EveSet.Add(Eve0);

    // check test data
    Contract.Assert(Contract.ForAll(ManSet, m => ManSet.Where(n => FuncClass.Above(m, n)).Count() == 1), "BelowToo");
  }
}
