package edu.mit.csail.sdg.alloy4compiler.generator;

import java.io.*;
import java.lang.Runtime;

import edu.mit.csail.sdg.alloy4.ConstList;
import edu.mit.csail.sdg.alloy4.A4Reporter;

import edu.mit.csail.sdg.alloy4compiler.ast.Command;

import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
import edu.mit.csail.sdg.alloy4compiler.parser.CompModule;

import edu.mit.csail.sdg.alloy4compiler.generator.CodeGenerator;
import edu.mit.csail.sdg.alloy4compiler.generator.TestGenerator;

import edu.mit.csail.sdg.alloy4compiler.translator.A4Options;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.mit.csail.sdg.alloy4compiler.translator.TranslateAlloyToKodkod;


// to run the tests: java -jar alloy4.2tests.jar

public final class CSharpGeneratorTests {

  private static String filename = "..\\edu\\mit\\csail\\sdg\\alloy4compiler\\generator\\tests";

  private static String answerCodeFile = "..\\edu\\mit\\csail\\sdg\\alloy4compiler\\generator\\answer";

  private static String answerTestFile = "..\\edu\\mit\\csail\\sdg\\alloy4compiler\\generator\\answer";

  private static String alloyExt = ".als";

  private static String codeExt = ".cs";

  private static String testExt = ".tests.cs";

  private static String codeCompExt = ".dll";

  private static String testCompExt = ".tests.exe";

  public static void main(String[] args) throws Exception {

    String path = "..\\edu\\mit\\csail\\sdg\\alloy4compiler\\generator\\";
    // specify whether to define Code Contracts checking in the code file
    boolean[] contracts = {false};
    // specify whether the test should succeed
    boolean[] expected = {false};
    // specify the type of the error
    String[] error = {"Assertion failed"};
    // specify the code file
    String[] code = {""};
    // specify the test file
    String[] test = {""};

    // determines whether there were failures during the execution of the entire test suite
    boolean okay = true;

    // specify the number of tests here
    for (int i = 0; i < 1; i++) {
      try {
        System.out.println("------- test" + i + " -------");
        String f = filename + i + alloyExt;
        String acf = answerCodeFile + i + codeExt;
        String atf = answerTestFile + i + testExt;

        // parse
        CompModule world = world = CompUtil.parseEverything_fromFile(A4Reporter.NOP, null, f);

        // generate C# code
        CodeGenerator.writeCode(world, f, contracts[i], false);

        // read code files to strings
        File f1 = new File(f + codeExt);
        File f2 = new File(acf);

        BufferedReader b1 = new BufferedReader(new FileReader(f1));
        BufferedReader b2 = new BufferedReader(new FileReader(f2));

        String s1 = "", s2 = "", l = "";
        while ((l = b1.readLine()) != null) {
          s1 += l;
        }
        while ((l = b2.readLine()) != null) {
          s2 += l;
        }

        // compare code
        System.out.print("Code generation: ");
        if (s1.equals(s2)) {
          System.out.println("SUCCESS");
        } else {
          okay = false;
          System.out.println("Failed");
        }

        // compile code
        Runtime rt = Runtime.getRuntime();
        Process pr = rt.exec("C:\\Windows\\Microsoft.NET\\Framework64\\v4.0.30319\\csc.exe /R:System.Diagnostics.Contracts.dll /nologo /target:library /out:" + f + codeCompExt + " " + f + codeExt);
        System.out.print("Code compilation: ");
        if (pr.waitFor() == 0) {
          System.out.println("SUCCESS");
        }
        else {
          okay = false;
          System.out.println("Failed");
        }

        // generate C# tests
        ConstList<Command> cmds = world.getAllCommands();
        A4Solution ai = TranslateAlloyToKodkod.execute_commandFromBook(A4Reporter.NOP, world.getAllReachableSigs(), cmds.get(cmds.size() - 1), new A4Options());
        TestGenerator.writeTest(ai, world, f, false);

        // read test files to strings
        f1 = new File(f + testExt);
        f2 = new File(atf);

        b1 = new BufferedReader(new FileReader(f1));
        b2 = new BufferedReader(new FileReader(f2));

        s1 = ""; s2 = "";
        while ((l = b1.readLine()) != null) {
          s1 += l;
        }
        while ((l = b2.readLine()) != null) {
          s2 += l;
        }

        // compare tests
        System.out.print("Test generation: ");
        if (s1.equals(s2)) {
          System.out.println("SUCCESS");
        } else {
          okay = false;
          System.out.println("Failed");
        }

        // compile code and tests
        pr = rt.exec("C:\\Windows\\Microsoft.NET\\Framework64\\v4.0.30319\\csc.exe /R:System.Diagnostics.Contracts.dll /nologo /D:CONTRACTS_FULL /out:" + f + testCompExt + " " + f + codeExt + " " + f + testExt);
        System.out.print("Code and test compilation: ");
        if (pr.waitFor() == 0) {
          System.out.println("SUCCESS");
        }
        else {
          okay = false;
          System.out.println("Failed");
        }
        if (!code[i].equals("") || !test[i].equals("")) {
          pr = rt.exec("C:\\Windows\\Microsoft.NET\\Framework64\\v4.0.30319\\csc.exe /R:System.Diagnostics.Contracts.dll /nologo /D:CONTRACTS_FULL /out:" + f + testCompExt + " " + (code[i].equals("") ? f : code[i]) + codeExt + " " + (test[i].equals("") ? f : test[i]) + testExt);
          System.out.print("Code and test compilation (modified): ");
          if (pr.waitFor() == 0) {
            System.out.println("SUCCESS");
          }
          else {
            okay = false;
            System.out.println("Failed");
          }
        }

        // rewrite code and tests
        pr = rt.exec("\"C:\\Program Files (x86)\\Microsoft\\Contracts\\Bin\\ccrewrite.exe\" -nologo -nobox -throwOnFailure -allowRewritten \"-libPaths:C:\\Program Files (x86)\\Reference Assemblies\\Microsoft\\Framework\\.NETFramework\\v4.0;C:\\Program Files (x86)\\Reference Assemblies\\Microsoft\\Framework\\.NETFramework\\v4.0\\CodeContracts\" \"-libPaths:C:\\Program Files (x86)\\Microsoft\\Contracts\\Contracts\\.NETFramework\\v4.0\" \"-libPaths:CodeContracts\" " + f + testCompExt);
        System.out.print("Code and test rewriting: ");
        if (pr.waitFor() == 0) {
          System.out.println("SUCCESS");
        }
        else {
          okay = false;
          System.out.println("Failed");
        }

        // execute code and tests
        pr = rt.exec(f + testCompExt);
        System.out.print("Code and test execution: ");
        int exitCode = pr.waitFor();
        if (expected[i]) {
          if (exitCode == 0) {
            System.out.println("SUCCESS");
          }
          else {
            okay = false;
            System.out.println("Failed");
          }
        }
        else {
          if (exitCode == 0) {
            okay = false;
            System.out.println("Failed");
          }
          else {
            BufferedReader b = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
            String s = "";
            while ((l = b.readLine()) != null) {
              s += l;
            }
            if (s.indexOf(error[i]) != -1) {
              System.out.println("SUCCESS");
            }
            else {
              okay = false;
              System.out.println("Failed");
            }
          }
        }
      }
      catch (Throwable ex) {
        okay = false;
        System.out.println("Error running the tests.");
      }
    }
    if (okay) {
      System.out.println("------- SUCCESS -------");
    }
    else {
      System.out.println("------- Failed -------");
    }
  }
}
