package edu.mit.csail.sdg.alloy4compiler.generator;

import java.io.*;
import java.lang.Runtime;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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

	enum ProcessResult {
		SUCCESS, FAILURE, TIMEOUT
	}
	
	public static void main(String[] args) throws Exception {

		String path = "..\\edu\\mit\\csail\\sdg\\alloy4compiler\\generator\\";
		boolean[] contracts = {true,                    false,                   false,                   true,                    true,                     false,                    false,                    true,                     true,                     false,                    false,                    true};
		boolean[] expected =  {false,                   true,                    true,                    false,                   true,                     true,                     true,                     true,                     false,                    true,                     true,                     true};
		String[] error =      {"Invariant failed",      "",                      "",                      "Assertion failed",      "",                       "",                       "",                       "",                       "Postcondition failed",   "",                       "",                       ""};
		String[] code =       {path + "tests2.als.sol", "",                      "",                      path + "tests8.als.sol", "",                       "",                       "",                       "",                       "",                       "",                       "",                       ""};
		String[] test =       {"",                      path + "tests5.als.sol", path + "tests7.als.sol", "",                      path + "tests11.als.sol", path + "tests12.als.sol", path + "tests13.als.sol", path + "tests15.als.sol", path + "tests17.als.sol", path + "tests19.als.sol", path + "tests22.als.sol", path + "tests23.als.sol"};

		// initial test suite
		int[] testNumber = {2, 5, 7, 8, 11, 12, 13, 15, 17, 19, 22, 23};

		int timeout = 20;
		
		int grade = 0;

		// specify the number of tests here
		for (int index = 0; index < testNumber.length; index++) {
			boolean okay = true;
		    int i = testNumber[index];
			try {
				System.out.println("------- test" + i + " -------");
				String f = filename + i + alloyExt;
				String acf = answerCodeFile + i + codeExt;
				String atf = answerTestFile + i + testExt;

				// parse
				CompModule world = world = CompUtil.parseEverything_fromFile(A4Reporter.NOP, null, f);

				// generate C# code
				CodeGeneratorTask cgt = new CodeGeneratorTask(world, f, contracts[index], timeout);
				boolean cgr = cgt.run();
				System.out.println("Code generation: " + (cgr ? "SUCCESS" : "Failed"));
				if (!cgr) {
					okay = false;
				}

			    // compile code
				TaskWithTimeout t = new TaskWithTimeout("C:\\Windows\\Microsoft.NET\\Framework64\\v4.0.30319\\csc.exe /R:System.Diagnostics.Contracts.dll /nologo /target:library /out:" + f + codeCompExt + " " + f + codeExt, timeout); 
			    ProcessResult r = t.run();
			    System.out.print("Code compilation: ");
			    if (r == ProcessResult.SUCCESS) {
			    	System.out.println("SUCCESS");
			    }
			    else {
			    	okay = false;
			    	if (r == ProcessResult.FAILURE) {
			    		System.out.println("Failed");
			    	}
			    	else {
			    		System.out.println("Timed out");
			    	}
			    }

			    // generate C# tests
			    ConstList<Command> cmds = world.getAllCommands();
			    A4Solution ai = TranslateAlloyToKodkod.execute_commandFromBook(A4Reporter.NOP, world.getAllReachableSigs(), cmds.get(cmds.size() - 1), new A4Options());
			    TestGeneratorTask tgt = new TestGeneratorTask(ai, world, f, timeout);
				boolean tgr = tgt.run();
				System.out.println("Test generation: " + (tgr ? "SUCCESS" : "Failed"));
				if (!tgr) {
					okay = false;
				}

			    // compile code and tests
			    t = new TaskWithTimeout("C:\\Windows\\Microsoft.NET\\Framework64\\v4.0.30319\\csc.exe /R:System.Diagnostics.Contracts.dll /nologo /D:CONTRACTS_FULL /out:" + f + testCompExt + " " + f + codeExt + " " + f + testExt, timeout); 
			    r = t.run();
			    System.out.print("Code and test compilation: ");
			    if (r == ProcessResult.SUCCESS) {
			    	System.out.println("SUCCESS");
			    }
			    else {
			    	okay = false;
			    	if (r == ProcessResult.FAILURE) {
			    		System.out.println("Failed");
			    	}
			    	else {
			    		System.out.println("Timed out");
			    	}
			    }
			    if (!code[index].equals("") || !test[index].equals("")) {
			    	t = new TaskWithTimeout("C:\\Windows\\Microsoft.NET\\Framework64\\v4.0.30319\\csc.exe /R:System.Diagnostics.Contracts.dll /nologo /D:CONTRACTS_FULL /out:" + f + testCompExt + " " + (code[index].equals("") ? f : code[index]) + codeExt + " " + (test[index].equals("") ? f : test[index]) + testExt, timeout);
			    	r = t.run();
			    	System.out.print("Code and test compilation (modified): ");
			    	if (r == ProcessResult.SUCCESS) {
				    	System.out.println("SUCCESS");
				    }
				    else {
				    	okay = false;
				    	if (r == ProcessResult.FAILURE) {
				    		System.out.println("Failed");
				    	}
				    	else {
				    		System.out.println("Timed out");
				    	}
				    }
			    }

			    // rewrite code and tests
			    t = new TaskWithTimeout("\"C:\\Program Files (x86)\\Microsoft\\Contracts\\Bin\\ccrewrite.exe\" -nologo -nobox -throwOnFailure -allowRewritten \"-libPaths:C:\\Program Files (x86)\\Reference Assemblies\\Microsoft\\Framework\\.NETFramework\\v4.0;C:\\Program Files (x86)\\Reference Assemblies\\Microsoft\\Framework\\.NETFramework\\v4.0\\CodeContracts\" \"-libPaths:C:\\Program Files (x86)\\Microsoft\\Contracts\\Contracts\\.NETFramework\\v4.0\" \"-libPaths:CodeContracts\" " + f + testCompExt, timeout);
		    	r = t.run();
			    System.out.print("Code and test rewriting: ");
			    if (r == ProcessResult.SUCCESS) {
			    	System.out.println("SUCCESS");
			    }
			    else {
			    	okay = false;
			    	if (r == ProcessResult.FAILURE) {
			    		System.out.println("Failed");
			    	}
			    	else {
			    		System.out.println("Timed out");
			    	}
			    }

			    // execute code and tests
			    t = new TaskWithTimeout(f + testCompExt, timeout);
		    	r = t.run();
			    System.out.print("Code and test execution: ");
			    if (expected[index]) {
			    	if (r == ProcessResult.SUCCESS) {
			    		if (okay) {
			    			grade++;
			    		}
				    	System.out.println("SUCCESS");
				    }
				    else {
				    	if (r == ProcessResult.FAILURE) {
				    		System.out.println("Failed");
				    	}
				    	else {
				    		System.out.println("Timed out");
				    	}
				    }
			    }
			    else {
			    	if (r == ProcessResult.SUCCESS) {
			    		System.out.println("Failed");
			    	}
			    	else if (r == ProcessResult.TIMEOUT) {
			    		System.out.println("Timed out");
			    	}
			    	else {
			            if (t.errorOutput.indexOf(error[index]) != -1) {
			            	if (okay) {
			            		grade++;
			            	}
			            	System.out.println("SUCCESS");
			            }
			            else {
			            	System.out.println("Failed");
			            }
			    	}
			    }
			}
			catch (Throwable ex) {
				System.out.println("Error running the tests.");
			}
		}
		System.out.println("Grade: " + grade + "/" + testNumber.length);
	}
	
	private static class TaskWithTimeout implements Callable<ProcessResult> {
	    private String cmd;
	    private int timeout;
	    String errorOutput;

	    TaskWithTimeout(String cmd, int timeout) {
	        this.cmd = cmd;
	        this.timeout = timeout;
	    }

	    ProcessResult run() {
	        ExecutorService threadsPool = Executors.newSingleThreadExecutor();
	        ProcessResult res = ProcessResult.FAILURE;
	        Future<ProcessResult> future = threadsPool.submit(this);
	        try {
	            res = future.get(timeout, TimeUnit.SECONDS);
	        } catch (java.util.concurrent.TimeoutException te) {
	            boolean bc = future.cancel(true);
	            res = ProcessResult.TIMEOUT;
	        } catch (InterruptedException e) { }
	        catch (ExecutionException e) { }
	        threadsPool.shutdownNow();
	        return res;
	    }

	    @Override
	    public ProcessResult call() throws Exception {
	        Runtime rt = Runtime.getRuntime();
	        Process pr = rt.exec(cmd);
	        int rv = pr.waitFor();
	        if (rv == 0) {
	            return ProcessResult.SUCCESS;
	        }
	        else {
	        	BufferedReader b = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
				String s = "", l = "";
				while ((l = b.readLine()) != null)
			    {
			        s += l;
			    }
				errorOutput = s;
	            return ProcessResult.FAILURE;
	        }
	    }
	}
	
	private static class CodeGeneratorTask implements Callable<Boolean> {
		private CompModule world;
		private String file;
		private boolean contracts;
	    private int timeout;
		
		CodeGeneratorTask(CompModule world, String file, boolean contracts, int timeout) {
	        this.world = world;
	        this.file = file;
	        this.contracts = contracts;
	        this.timeout = timeout;
	    }
		
		boolean run() {
	        ExecutorService threadsPool = Executors.newSingleThreadExecutor();
	        Boolean res = false;
	        Future<Boolean> future = threadsPool.submit(this);
	        try {
	            res = future.get(timeout, TimeUnit.SECONDS);
	        } catch (java.util.concurrent.TimeoutException te) {
	            boolean bc = future.cancel(true);
	        } catch (InterruptedException e) { }
	        catch (ExecutionException e) { }
	        threadsPool.shutdownNow();
	        return res;
	    }
		
	    @Override
	    public Boolean call() throws Exception {
	    	CodeGenerator.writeCode(world, file, contracts, false);
	    	return true;
	    }
	}
	
	private static class TestGeneratorTask implements Callable<Boolean> {
		private CompModule world;
		private String file;
		private A4Solution ai;
	    private int timeout;
		
	    TestGeneratorTask(A4Solution ai, CompModule world, String file, int timeout) {
	        this.world = world;
	        this.file = file;
	        this.ai = ai;
	        this.timeout = timeout;
	    }
		
		boolean run() {
	        ExecutorService threadsPool = Executors.newSingleThreadExecutor();
	        Boolean res = false;
	        Future<Boolean> future = threadsPool.submit(this);
	        try {
	            res = future.get(timeout, TimeUnit.SECONDS);
	        } catch (java.util.concurrent.TimeoutException te) {
	            boolean bc = future.cancel(true);
	        } catch (InterruptedException e) { }
	        catch (ExecutionException e) { }
	        threadsPool.shutdownNow();
	        return res;
	    }
		
	    @Override
	    public Boolean call() throws Exception {
	    	TestGenerator.writeTest(ai, world, file, false);
	    	return true;
	    }
	}
}

