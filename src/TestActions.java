import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import actions.Action;
import actions.PathAction;
import actions.ShapeAction;

public class TestActions {

	
	public static void main(String[] args) throws Exception {
		runTests();
		/*
		// test sat
		runCommand(new String[]{"s","./M1.ttl"});
		
		
		// test cont 1
		
		runCommand(new String[]{"c","./M1.ttl","./M1.ttl"});
		
		// test cont 2
		runCommand(new String[]{"c","./M1.ttl","./M2.ttl"});
		// test actions
		runCommand(new String[]{"a","./M1.ttl","./actions1.json"});
		//String[] parameters = new String[]{"a","./M1.ttl","./actions.ttl"};
		
		//String[] parameters = new String[]{"a","./StudentShapesAlt.ttl","./actions.ttl"};		
		//SHACLFOLMain.main(parameters);
		*/
	}
	
	private static void runCommand(String[] args) throws Exception {
		SHACLFOLMain.main(args);
		System.out.println();
	}
	
	// SHAPES
	
	private static String testShape1 = "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n"
			+ "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n"
			+ "@prefix sh: <http://www.w3.org/ns/shacl#> .\n"
			+ "@prefix : <http://e.com/> .\n"
			+ "\n"
			+ ":shapeOne a sh:PropertyShape ;\n"
			+ "  sh:targetClass :A ;\n"
			+ "sh:path (:hasSupervisor\n"
			+ ":hasFaculty );\n"
			+ "sh:minCount 1 .";

	private static String testShape2 = "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n"
			+ "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n"
			+ "@prefix sh: <http://www.w3.org/ns/shacl#> .\n"
			+ "@prefix : <http://e.com/> .\n"
			+ "\n"
			+ ":studentShape a sh:NodeShape ;\n"
			+ "  sh:targetClass :Student ;\n"
			+ "  sh:targetNode :Alex ;\n"
			+ "  sh:not :shapeOne .\n"
			+ ":shapeOne a sh:PropertyShape ;\n"
			+ "  sh:targetClass :A ;\n"
			+ "sh:path (:hasSupervisor\n"
			+ ":hasFaculty );\n"
			+ "sh:minCount 1 .";
	
	// ACTION SHAPES
	
	private static String isAshape = "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n"
			+ "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n"
			+ "@prefix sh: <http://www.w3.org/ns/shacl#> .\n"
			+ "@prefix : <http://e.com/> .\n"
			+ "\n"
			+ ":shapeX a sh:NodeShape ;\n"
			+ "  sh:hasValue :A .\n";
	private static String isBshape = "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n"
			+ "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n"
			+ "@prefix sh: <http://www.w3.org/ns/shacl#> .\n"
			+ "@prefix : <http://e.com/> .\n"
			+ "\n"
			+ ":shapeX a sh:NodeShape ;\n"
			+ "  sh:hasValue :B .\n";	
	private static String ofClassBshape = "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n"
				+ "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n"
				+ "@prefix sh: <http://www.w3.org/ns/shacl#> .\n"
				+ "@prefix : <http://e.com/> .\n"
				+ "\n"
				+ ":shapeX a sh:PropertyShape ;\n"
				+ "  sh:class :B .\n";
	private static String ofClassAshape = "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n"
			+ "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n"
			+ "@prefix sh: <http://www.w3.org/ns/shacl#> .\n"
			+ "@prefix : <http://e.com/> .\n"
			+ "\n"
			+ ":shapeX a sh:PropertyShape ;\n"
			+ "  sh:class :A .\n";	
	private static String rdfType = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
	
	private static String hasSupervisor = "http://e.com/hasSupervisor";
	private static String hasManager = "http://e.com/hasManager";
	private static String hasEmployee = "http://e.com/hasEmployee";
	private static int passed = 0;
	private static int failed = 0;
	
	private static boolean checkValidityPreserved(TestOutput result, boolean expected) {
		if(result.isSatisfiable()) {
			// there exists a graph that, after being updated, violates the shape
			if(expected == false) passed += 1;
			else failed += 1;
			return expected == false;
		} else {
			// the updates do not violate the shapes
			if(expected == true) passed += 1;
			else failed += 1;
			return expected == true;
		}
	}
	private static void runTests() throws Exception {
		passed = 0;
		failed = 0;
		System.out.println("Testing Actions");
		Action A_plus_B = new ShapeAction(true, rdfType, ofClassBshape, isAshape);
		Action A_minus_B = new ShapeAction(false, rdfType, ofClassBshape, isAshape);
		Action Supervisor_plus_Manager = new PathAction(true,hasSupervisor,"<"+hasManager+">");
		Action Supervisor_minus_Manager = new PathAction(false,hasSupervisor,"<"+hasManager+">");
		Action Manager_minus_Employee = new PathAction(false,hasManager,"<"+hasEmployee+">");
		Action Manager_plus_Employee = new PathAction(true,hasManager,"<"+hasEmployee+">");
		{
			// the second action reduces the number of targets of the shape to a subset of the original
			List<Action> actions = new LinkedList<Action>();
			actions.add(A_plus_B);
			actions.add(A_minus_B);
			TestOutput result = SHACLFOLMain.runTestActionsStaticValidation(testShape1, actions);
			checkValidityPreserved(result,true);
		}
		{
			// the second action might add more targets not previously accounted for
			List<Action> actions = new LinkedList<Action>();
			actions.add(A_minus_B);
			actions.add(A_plus_B);
			TestOutput result = SHACLFOLMain.runTestActionsStaticValidation(testShape1, actions);
			checkValidityPreserved(result,false);
		}
		{
			// we might get more supervisor relations from the manager ones, thus making more
			// nodes satisfy the shape
			List<Action> actions = new LinkedList<Action>();
			actions.add(Supervisor_plus_Manager);
			TestOutput result = SHACLFOLMain.runTestActionsStaticValidation(testShape1, actions);
			checkValidityPreserved(result,true);
		}
		{
			// whenever we have manager relations we might lose supervisor ones, thus making fewer 
			// nodes satisfy the shape, potentially making target nodes invalid
			List<Action> actions = new LinkedList<Action>();
			actions.add(Supervisor_minus_Manager);
			TestOutput result = SHACLFOLMain.runTestActionsStaticValidation(testShape1, actions);
			checkValidityPreserved(result,false);
		}
		{
			// Manager and employee are not relevant relations to the shape, and thus it should not 
			// affect validation
			List<Action> actions = new LinkedList<Action>();
			actions.add(Manager_minus_Employee);
			TestOutput result = SHACLFOLMain.runTestActionsStaticValidation(testShape1, actions);
			checkValidityPreserved(result,true);
		}
		{
			// Manager and employee are not relevant relations to the shape, and thus it should not 
			// affect validation
			List<Action> actions = new LinkedList<Action>();
			actions.add(Manager_plus_Employee);
			TestOutput result = SHACLFOLMain.runTestActionsStaticValidation(testShape1, actions);
			checkValidityPreserved(result,true);
		}
		System.out.println("Passed tests: "+passed+" / "+(passed+failed));
		
		// run performance check
		{
			List<Action> allRelevantActions = new LinkedList<Action>();
			allRelevantActions.add(A_plus_B);
			allRelevantActions.add(A_minus_B);
			allRelevantActions.add(Supervisor_plus_Manager);
			allRelevantActions.add(Supervisor_minus_Manager);
			analyzePerformanceSatCategory(testShape2, allRelevantActions, 500);
		}
		
	}
	
	
	

	    public static void analyzePerformanceSequential(String shape, List<Action> L, int J) throws Exception {
	        if (J <= 0 || L.isEmpty()) {
	            throw new IllegalArgumentException("J must be > 0 and action list must not be empty");
	        }

	        // Prepare output file
	        PrintWriter writer = new PrintWriter(new FileWriter("performance_metrics.csv"));
	        writer.println("ActionListSize,TimeSeconds,MemoryKB");

	        for (int i = 0; i <= J; i++) {
	        	
	            // Build a list of i actions, wrapping around the original list
	            List<Action> actions = new ArrayList<>();
	            for (int k = 0; k < i; k++) {
	                actions.add(L.get(k % L.size()));
	            }

	            // Run the test
	            TestOutput result = SHACLFOLMain.runTestActionsStaticValidation(shape, actions);
	            if(result.isSatisfiable())
	            	System.out.println("+"+i);
	            else
	            	System.out.println("-"+i);
	            // Extract time and memory
	            double time = result.getTimeElapsedSeconds();
	            long memory = result.getMemoryUsedKB();

	            // Write to file
	            writer.printf("%d,%.3f,%d%n", i, time, memory);
	        }

	        writer.close();
	        System.out.println("Performance data written to performance_metrics.csv");
	    }
	    
	    public static void analyzePerformanceRandom(String shape, List<Action> L, int J) throws Exception {
	        if (J <= 0 || L.isEmpty()) {
	            throw new IllegalArgumentException("J must be > 0 and action list must not be empty");
	        }

	        Random random = new Random(); // Random instance for selection

	        // Prepare output file
	        PrintWriter writer = new PrintWriter(new FileWriter("performance_metrics.csv"));
	        writer.println("ActionListSize,TimeSeconds,MemoryKB");

	        for (int i = 0; i <= J; i++) {

	            // Build a list of i actions randomly selected from L (with replacement)
	            List<Action> actions = new ArrayList<>();
	            for (int k = 0; k < i; k++) {
	                int randomIndex = random.nextInt(L.size());
	                actions.add(L.get(randomIndex));
	            }

	            // Run the test
	            TestOutput result = SHACLFOLMain.runTestActionsStaticValidation(shape, actions);
	            if (result.isSatisfiable())
	                System.out.println("+" + i);
	            else
	                System.out.println("-" + i);

	            // Extract time and memory
	            double time = result.getTimeElapsedSeconds();
	            long memory = result.getMemoryUsedKB();

	            // Write to file
	            writer.printf("%d,%.3f,%d%n", i, time, memory);
	        }

	        writer.close();
	        System.out.println("Performance data written to performance_metrics.csv");
	    }
	    
	    
	    public static void analyzePerformanceSatCategory(String shape, List<Action> L, int J) throws Exception {
	        if (J <= 0 || L.isEmpty()) {
	            throw new IllegalArgumentException("J must be > 0 and action list must not be empty");
	        }

	        Random random = new Random();

	        PrintWriter writer = new PrintWriter(new FileWriter("performance_metrics.csv"));
	        writer.println("ActionListSize,TimeSeconds,MemoryKB,Satisfiable");

	        for (int i = 0; i <= J; i++) {
	            List<Action> actions = new ArrayList<>();
	            for (int k = 0; k < i; k++) {
	                int randomIndex = random.nextInt(L.size());
	                actions.add(L.get(randomIndex));
	            }

	            TestOutput result = SHACLFOLMain.runTestActionsStaticValidation(shape, actions);
	            boolean sat = result.isSatisfiable();

	            System.out.println((sat ? "+" : "-") + i);

	            double time = result.getTimeElapsedSeconds();
	            long memory = result.getMemoryUsedKB();

	            writer.printf("%d,%.3f,%d,%s%n", i, time, memory, sat ? "true" : "false");
	        }

	        writer.close();
	        System.out.println("Performance data written to performance_metrics.csv");
	    }

}
