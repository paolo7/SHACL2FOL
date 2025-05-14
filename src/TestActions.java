import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import actions.Action;
import actions.PathAction;
import actions.ShapeAction;

public class TestActions {

	
	private static int seed = 1;
	private static Random random = new Random(seed);
	
	public static void main(String[] args) throws Exception {
		runPerformanceCheckScalingShapes(5,70,0.5,20,false);
		runPerformanceCheckScalingActions(5,20,0.5,150,false);
		runPerformanceCheckScalingActions(10,20,0.5,150,true);
		runPerformanceCheckScalingShapes(10,70,0.5,20,true);
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
	
	private static String testShapesLarge = "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n"
			+ "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n"
			+ "@prefix sh: <http://www.w3.org/ns/shacl#> .\n"
			+ "@prefix : <http://e.com/> .\n"
			+ "\n"
			+ ":studentShape a sh:NodeShape ;\n"
			+ "  sh:targetClass :Student ;\n"
			+ "  sh:not :disjFacultyShape .\n"
			+ "\n"
			+ ":disjFacultyShape a\n"
			+ "  sh:PropertyShape ;\n"
			+ "  sh:path (:hasSupervisor :hasFaculty);\n"
			+ "  sh:disjoint :hasFaculty .\n"
			+ "  \n"
			+ ":shapeOne a sh:PropertyShape ;\n"
			+ "  sh:targetClass :A ;\n"
			+ "  sh:path (:hasSupervisor\n"
			+ "    :hasFaculty );\n"
			+ "  sh:minCount 1 .\n"
			+ "  \n"
			+ ":shapeTwo a sh:PropertyShape ;\n"
			+ "  sh:targetClass :B ;\n"
			+ "  sh:path (:hasSupervisor\n"
			+ "    :hasFaculty );\n"
			+ "  sh:maxCount 1 .  ";
	
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
	
	public static String getRandomRelation(int relation_n, boolean fullIRI) {
		int rel = random.nextInt(relation_n);
		if(rel != 0) {
			if(fullIRI) {
				return "http://e.com/r"+rel;
			} else {
				return ":r"+random.nextInt(relation_n)+rel;
			}
		} else {
			if(fullIRI) {
				return "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
			} else {
				return "rdf:type";
			}
		}
	}
	
	public static String createRandomPath(int relation_n) {
		int path_type = random.nextInt(3);
		if(path_type == 0) {
			// alternative path
			return "[ sh:alternativePath ( "+getRandomRelation(relation_n,false)+" "+getRandomRelation(relation_n,false)+" ) ]";
		} else if(path_type == 1) {
			// sequence path 
			return "( "+getRandomRelation(relation_n,false)+" "+getRandomRelation(relation_n,false)+" )";
		} else {
			// transitive path
			return "[ sh:zeroOrMorePath "+getRandomRelation(relation_n,false)+" ]";
		}
	}
	
	// create an alternative, sequence or transitive path
	public static String createPathConstraint(int constant_n, int relation_n) {
			String shape = "  sh:path "+createRandomPath(relation_n)+" ; \n";			
			// check on path: either a class or a hasValue
			shape += getClassorHasValueConstraintSnippet(constant_n,relation_n);
			
			shape += "  a sh:PropertyShape .";
			return shape;
	}
	
	public static String getClassorHasValueConstraintSnippet(int constant_n, int relation_n) {
		String shape = "";
		if(random.nextBoolean()) {
			shape += "  sh:hasValue :c"+random.nextInt(constant_n)+" ; \n";
		} else {
			shape += "  sh:class :c"+random.nextInt(constant_n)+" ; \n";
		}
		return shape;
	}
	
	// sh:equals and min/max count
	public static String createShapeConstraint(int constant_n, int relation_n) {
		String shape = "  sh:path "+getRandomRelation(relation_n,false)+" ; \n";
		if(random.nextBoolean()) {
			// create sh:equals
			shape += "  sh:"+(random.nextBoolean() ? "equals":"disjoint")+" "+getRandomRelation(relation_n,false)+" ; \n";
		} else {
			// create min max count
			if(random.nextBoolean()) {
				shape += " sh:qualifiedMinCount "+random.nextInt(1,3)+" ; \n";
			} else {
				shape += " sh:qualifiedMaxCount "+random.nextInt(2)+" ; \n";
			}
			shape += "  sh:qualifiedValueShape [\n";
			shape += "  "+getClassorHasValueConstraintSnippet(constant_n,relation_n);
			shape += "  ] ; \n";
		}
		shape += "  a sh:PropertyShape .";
		return shape;
	}
	
	public static String createConstraint(int constant_n, int relation_n, int constaintType) {
		int constraintToCreate = constaintType != 2 ? constaintType : random.nextInt(2);
		if(constraintToCreate == 0)
			return createShapeConstraint(constant_n, relation_n);
		else 
			return createPathConstraint(constant_n, relation_n);
	}
	
	private static String createSynteticShape(int constant_n, int relation_n, double shape_ratio, int constaintType) {
		String shape = "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n"
				+ "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n"
				+ "@prefix sh: <http://www.w3.org/ns/shacl#> .\n"
				+ "@prefix : <http://e.com/> .\n\n";
		
		// create Class and Node targets
		for(int i = 0; i < constant_n*shape_ratio; i++) {
			// Create a Class target
			if(i%2 == 0) {
				shape += ":shapeC"+i+" sh:targetClass :c"+i+" . \n";
				shape += ":shapeC"+i+createConstraint(constant_n, relation_n, constaintType);
				shape += "\n\n";
			} else {
				shape += ":shapeC"+i+" sh:targetNode :c"+i+" . \n";
				shape += ":shapeC"+i+createConstraint(constant_n, relation_n, constaintType);
				shape += "\n\n";
			}
		}
		for(int i = 0; i < relation_n*shape_ratio; i++) {
			// Create a Class target
			if(i%2 == 0) {
				shape += ":shapeR"+i+" sh:targetSubjectsOf :r"+i+" . \n";
				shape += ":shapeR"+i+createConstraint(constant_n, relation_n, constaintType);
				shape += "\n\n";
			} else {
				shape += ":shapeR"+i+" sh:targetObjectsOf :r"+i+" . \n";
				shape += ":shapeR"+i+createConstraint(constant_n, relation_n, constaintType);
				shape += "\n\n";
			}
		}
		return shape;
	}
	
	public static PathAction generatePathAction(int relation_n) {
		return new PathAction(random.nextBoolean(), getRandomRelation(relation_n,true), createRandomPath(relation_n));
	}
	
	public static String createActionShapeString(int constant_n, int relation_n) {
		String shape = "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n"
				+ "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n"
				+ "@prefix sh: <http://www.w3.org/ns/shacl#> .\n"
				+ "@prefix : <http://e.com/> .\n";
		shape += ":shapeAC "+createConstraint(constant_n, relation_n, 0);	
		return shape;
	}
	public static String emptyShape = "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n"
			+ "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n"
			+ "@prefix sh: <http://www.w3.org/ns/shacl#> .\n"
			+ "@prefix : <http://e.com/> .\n"
			+ ":shapeAC a sh:NodeShape . ";
	public static ShapeAction generateShapeAction(int constant_n, int relation_n) {
		boolean checkOnSubject = random.nextBoolean();
		return new ShapeAction(random.nextBoolean(), getRandomRelation(relation_n,true), 
				checkOnSubject ? createActionShapeString(constant_n, relation_n) : emptyShape, !checkOnSubject ? createActionShapeString(constant_n, relation_n) : emptyShape);
	}
	
	public static Action generateAction(int constant_n, int relation_n, int constraintType) {
		int constraintToCreate = constraintType != 2 ? constraintType : random.nextInt(2);
		if(constraintToCreate == 1)
			return generatePathAction(relation_n);
		else
			return generateShapeAction(constant_n, relation_n);
	}
	
	public static void runTests() throws Exception {
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
			TestOutput result = SHACLFOLMain.runTestActionsStaticValidation(testShape1, actions, true);
			checkValidityPreserved(result,true);
		}
		{
			// the second action might add more targets not previously accounted for
			List<Action> actions = new LinkedList<Action>();
			actions.add(A_minus_B);
			actions.add(A_plus_B);
			TestOutput result = SHACLFOLMain.runTestActionsStaticValidation(testShape1, actions, true);
			checkValidityPreserved(result,false);
		}
		{
			// we might get more supervisor relations from the manager ones, thus making more
			// nodes satisfy the shape
			List<Action> actions = new LinkedList<Action>();
			actions.add(Supervisor_plus_Manager);
			TestOutput result = SHACLFOLMain.runTestActionsStaticValidation(testShape1, actions, true);
			checkValidityPreserved(result,true);
		}
		{
			// whenever we have manager relations we might lose supervisor ones, thus making fewer 
			// nodes satisfy the shape, potentially making target nodes invalid
			List<Action> actions = new LinkedList<Action>();
			actions.add(Supervisor_minus_Manager);
			TestOutput result = SHACLFOLMain.runTestActionsStaticValidation(testShape1, actions, true);
			checkValidityPreserved(result,false);
		}
		{
			// Manager and employee are not relevant relations to the shape, and thus it should not 
			// affect validation
			List<Action> actions = new LinkedList<Action>();
			actions.add(Manager_minus_Employee);
			TestOutput result = SHACLFOLMain.runTestActionsStaticValidation(testShape1, actions, true);
			checkValidityPreserved(result,true);
		}
		{
			// Manager and employee are not relevant relations to the shape, and thus it should not 
			// affect validation
			List<Action> actions = new LinkedList<Action>();
			actions.add(Manager_plus_Employee);
			TestOutput result = SHACLFOLMain.runTestActionsStaticValidation(testShape1, actions, true);
			checkValidityPreserved(result,true);
		}
		System.out.println("Passed tests: "+passed+" / "+(passed+failed));
		
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
	            TestOutput result = SHACLFOLMain.runTestActionsStaticValidation(shape, actions, true);
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
	            TestOutput result = SHACLFOLMain.runTestActionsStaticValidation(shape, actions, true);
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

	            TestOutput result = SHACLFOLMain.runTestActionsStaticValidation(shape, actions, true);
	            boolean sat = result.isSatisfiable();

	            System.out.println((sat ? "+" : "-") + i);

	            double time = result.getTimeElapsedSeconds();
	            long memory = result.getMemoryUsedKB();

	            writer.printf("%d,%.3f,%d,%s%n", i, time, memory, sat ? "true" : "false");
	        }

	        writer.close();
	        System.out.println("Performance data written to performance_metrics.csv");
	    }
	    
	    public static void analyzePerformanceSatCategoryLargeShape(String shape, List<Action> L, int J) throws Exception {
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

	            TestOutput result = SHACLFOLMain.runTestActionsStaticValidation(shape, actions, true);
	            boolean sat = result.isSatisfiable();

	            System.out.println((sat ? "+" : "-") + i);

	            double time = result.getTimeElapsedSeconds();
	            long memory = result.getMemoryUsedKB();

	            writer.printf("%d,%.3f,%d,%s%n", i, time, memory, sat ? "true" : "false");
	        }

	        writer.close();
	        System.out.println("Performance data written to performance_metrics.csv");
	    }
	    
	    private static void runPerformanceCheckWithCasesOld(int constant_n, int relation_n, int action_n, int maxActions) throws Exception {
	        if (constant_n <= 0 || relation_n <= 0 || action_n <= 0 || maxActions < 0) {
	            throw new IllegalArgumentException("Input parameters must be positive and maxActions >= 0");
	        }

	        Random random = new Random();

	        for (int constraintType = 0; constraintType <= 1; constraintType++) {
	        	
	            // Generate shape and base action pool for this constraint type
	            String shape = createSynteticShape(constant_n, relation_n, 0.5, constraintType);
	            List<Action> baseActions = new LinkedList<>();
	            for (int i = 0; i < action_n; i++) {
	                Action a = generateAction(constant_n, relation_n, constraintType);
	                baseActions.add(a);
	            }

	            // Prepare output files for each constraintType
	            String fileName = "performance_metrics_type_" + constraintType + ".csv";
	            PrintWriter writer = new PrintWriter(new FileWriter(fileName));
	            writer.println("ActionListSize,TimeSeconds,MemoryKB");

	            for (int i = 1; i <= maxActions; i++) {
	            	//System.out.println("Case "+constraintType+", i "+i);
	                // Build a list of i actions randomly selected from baseActions (with replacement)
	                List<Action> selectedActions = new ArrayList<>();
	                for (int k = 0; k < i; k++) {
	                    int randomIndex = random.nextInt(baseActions.size());
	                    selectedActions.add(baseActions.get(randomIndex));
	                }

	                ExecutorService executor = Executors.newSingleThreadExecutor();
	                Future<TestOutput> future = executor.submit(() -> SHACLFOLMain.runTestActionsStaticValidation(shape, selectedActions, true));

	                try {
	                    TestOutput result = future.get(30, TimeUnit.SECONDS);

	                    if (result.isSatisfiable())
	                        System.out.println("Type " + constraintType + ": +" + i);
	                    else
	                        System.out.println("Type " + constraintType + ": -" + i);

	                    // Extract time and memory
	                    double time = result.getTimeElapsedSeconds();
	                    long memory = result.getMemoryUsedKB();

	                    // Write to file
	                    writer.printf("%d,%.3f,%d%n", i, time, memory);
	                } catch (TimeoutException e) {
	                    System.out.println("Type " + constraintType + ": Timeout at i = " + i);
	                    future.cancel(true); // Attempt to cancel the task
	                } catch (Exception e) {
	                    System.out.println("Type " + constraintType + ": Error at i = " + i + " -> " + e.getMessage());
	                } finally {
	                    executor.shutdownNow(); // Clean up
	                }

	            }

	            writer.close();
	            System.out.println("Performance data written to " + fileName);
	        }
	    }
	    
	    /**
	     * 
	     * @param trials how many trials to run for each variable configuration (the average of those is computed)
	     * @param constant_n how many constants to use
	     * @param shape_ratio how many shapes to create for each constant
	     * @param maxActions the maximum number of actions to test (it will test from 1 to maxActions at 5 actions intervals
	     * @param fm true if the finite model property is to be enforced on the satisfiability checking
	     * @throws Exception
	     */
	    public static void runPerformanceCheckScalingActions(int trials, int constant_n, double shape_ratio, int maxActions, boolean fm) throws Exception {
	        if (constant_n <= 0 || maxActions < 0) {
	            throw new IllegalArgumentException("Input parameters must be positive and maxActions >= 0");
	        }

	        Random random = new Random();

	        for (int constraintType = 2; constraintType <= 2; constraintType++) {

	            // Prepare output file
	            String fileName = "performance_metrics_type_" + constraintType + (fm ? "T" : "F")+".csv";
	            PrintWriter writer = new PrintWriter(new FileWriter(fileName));
	            writer.println("ActionListSize,AvgTimeSeconds,AvgMemoryKB,AvgTotalTimeSeconds,AvgIsSatisfiable,TimeOuts,Errors");
	            
	            for (int i = 1; i <= maxActions; i+=5) {
	            	
	            	if(i == 6) i--;
                    
	                double totalTime = 0;
	                double totalMemory = 0;
	                double totalWallTime = 0;
	                double satisfiableSum = 0;

	                int time_out_events = 0;
	                int error_events = 0;
	                for (int trial = 0; trial < trials+time_out_events+error_events; trial++) {
	                	
	                	String shape = createSynteticShape(constant_n/2, constant_n/2, shape_ratio, constraintType);
	                	
			            
	                    // Generate a new random sample each time
	                    List<Action> selectedActions = new ArrayList<>();
	                    for (int k = 0; k < i; k++) {
	                    	selectedActions.add(generateAction(constant_n/2, constant_n/2, constraintType));
	                    }

	                    ExecutorService executor = Executors.newSingleThreadExecutor();
	                    long startWallTime = System.nanoTime();
	                    Future<TestOutput> future = executor.submit(() -> SHACLFOLMain.runTestActionsStaticValidation(shape, selectedActions,fm));

	                    try {
	                        TestOutput result = future.get(20, TimeUnit.SECONDS);
	                        long endWallTime = System.nanoTime();
	                        double wallTimeSeconds = (endWallTime - startWallTime) / 1_000_000_000.0;

	                        boolean isSat = result.isSatisfiable();
	                        double time = result.getTimeElapsedSeconds();
	                        long memory = result.getMemoryUsedKB();

	                        totalTime += time;
	                        totalMemory += memory;
	                        totalWallTime += wallTimeSeconds;
	                        satisfiableSum += isSat ? 1 : 0;
	                        
	                    } catch (TimeoutException e) {
	                        System.out.println("Type " + constraintType + ": Timeout at i = " + i + " (trial " + trial + ")");
	                        time_out_events +=1;
	                        future.cancel(true);
	                    } catch (Exception e) {
	                        System.out.println("Type " + constraintType + ": Error at i = " + i + " (trial " + trial + ") -> " + e.getMessage());
	                        error_events += 1;
	                    } finally {
	                        executor.shutdownNow();
	                    }
	                }

	                double avgTime = totalTime / trials;
	                double avgMemory = totalMemory / trials;
	                double avgWallTime = totalWallTime / trials;
	                double avgSat = satisfiableSum / trials;
	                System.out.println("Type " + constraintType + ": +" + i+" sat ratio: "+(avgSat)+" Tot time: "+avgWallTime);
	                writer.printf("%d,%.3f,%.1f,%.3f,%.3f,%d,%d%n", i, avgTime, avgMemory, avgWallTime, avgSat, time_out_events, error_events);
	                writer.flush();
	            }

	            writer.close();
	            System.out.println("Performance data written to " + fileName);
	        }
	    }

	    /**
	     * 
	     * @param trials how many trials to run for each variable configuration (the average of those is computed)
	     * @param maxShapes the maximum number of shapes to consider (it will test from 10 to this number at 10-intervals)
	     * @param shape_ratio how many shapes there will be for every constant
	     * @param action_n the fixed number of actions to use
	     * @param fm true if the finite model property is to be enforced on the satisfiability checking
	     * @throws Exception
	     */
	    public static void runPerformanceCheckScalingShapes(int trials, int maxShapes, double shape_ratio, int action_n, boolean fm) throws Exception {
	        if (maxShapes <= 0 || action_n <= 0) {
	            throw new IllegalArgumentException("Input parameters must be positive");
	        }

	        final int fixedMaxActions = action_n;
	        Random random = new Random();

	        for (int constraintType = 2; constraintType <= 2; constraintType++) {

	            // Prepare output file
	            String fileName = "performance_metrics_type_shape_" + constraintType + (fm ? "T" : "F" ) + ".csv";
	            PrintWriter writer = new PrintWriter(new FileWriter(fileName));
	            writer.println("ConstantRelationSize,ActionListSize,AvgTimeSeconds,AvgMemoryKB,AvgTotalTimeSeconds,AvgIsSatisfiable,TimeOuts,Errors");

	            for (int shapeSize = 10; shapeSize <= maxShapes; shapeSize+=10) {
	                int constant_n = shapeSize;
	                int relation_n = shapeSize;

	                System.out.println("Type " + constraintType + ": Shape size " + shapeSize);

	                double totalTime = 0;
	                double totalMemory = 0;
	                double totalWallTime = 0;
	                double satisfiableSum = 0;

	                int time_out_events = 0;
	                int error_events = 0;

	                for (int trial = 0; trial < trials + time_out_events + error_events; trial++) {

	                    String shape = createSynteticShape(constant_n, relation_n, shape_ratio, constraintType);

	                    List<Action> baseActions = new LinkedList<>();
	                    for (int j = 0; j < fixedMaxActions / 2 + 1; j++) {
	                        Action a = generateAction(constant_n, relation_n, constraintType);
	                        baseActions.add(a);
	                    }

	                    List<Action> selectedActions = new ArrayList<>();
	                    for (int k = 0; k < fixedMaxActions; k++) {
	                        int randomIndex = random.nextInt(baseActions.size());
	                        selectedActions.add(baseActions.get(randomIndex));
	                    }

	                    ExecutorService executor = Executors.newSingleThreadExecutor();
	                    long startWallTime = System.nanoTime();
	                    Future<TestOutput> future = executor.submit(() -> SHACLFOLMain.runTestActionsStaticValidation(shape, selectedActions, fm));

	                    try {
	                        TestOutput result = future.get(150, TimeUnit.SECONDS);
	                        long endWallTime = System.nanoTime();
	                        double wallTimeSeconds = (endWallTime - startWallTime) / 1_000_000_000.0;

	                        boolean isSat = result.isSatisfiable();
	                        double time = result.getTimeElapsedSeconds();
	                        long memory = result.getMemoryUsedKB();

	                        totalTime += time;
	                        totalMemory += memory;
	                        totalWallTime += wallTimeSeconds;
	                        satisfiableSum += isSat ? 1 : 0;

	                    } catch (TimeoutException e) {
	                        System.out.println("Type " + constraintType + ": Timeout at shapeSize = " + shapeSize + " (trial " + trial + ")");
	                        time_out_events += 1;
	                        future.cancel(true);
	                    } catch (Exception e) {
	                        System.out.println("Type " + constraintType + ": Error at shapeSize = " + shapeSize + " (trial " + trial + ") -> " + e.getMessage());
	                        error_events += 1;
	                    } finally {
	                        executor.shutdownNow();
	                    }
	                }

	                double avgTime = totalTime / trials;
	                double avgMemory = totalMemory / trials;
	                double avgWallTime = totalWallTime / trials;
	                double avgSat = satisfiableSum / trials;

	                writer.printf("%d,%d,%.3f,%.1f,%.3f,%.3f,%d,%d%n", shapeSize, fixedMaxActions, avgTime, avgMemory, avgWallTime, avgSat, time_out_events, error_events);
	                writer.flush();
	            }

	            writer.close();
	            System.out.println("Performance data written to " + fileName);
	        }
	    }




}
