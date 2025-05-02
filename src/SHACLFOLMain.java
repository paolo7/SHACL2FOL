import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import actions.Action;
import actions.ActionParser;
import actions.PathAction;
import actions.ShapeAction;
import actions.TupleAction;
import converter.Config;
import converter.ShapeReader;
import logic.FOL_Encoder;
import logic.TPTP_Encoder;

public class SHACLFOLMain {

	public static void main(String[] args) throws Exception {
		Config.readDefaultConfig();
		String outputFilename = Config.tptpOutputFile;
		String proverCommand = Config.proverPath;
		String proverArguments = Config.proverArguments;
		if(proverArguments.length()>0)
			proverCommand += " "+proverArguments;
		// testSAT("/home/office/SHACL2FOL/SHACL2FOL/shapes/test.ttl",outputFilename,proverCommand);

		System.out.println("Default prover command " + proverCommand);
		System.out.println("Default file where to store the TPTP output " + outputFilename);
		System.out.println("These defaults can be changed in the config.properties configuration file.");
		if (args.length == 0) {
			System.out.println("Please provide the following arguments:" + "\n To perform a satisfiability check:"
					+ "\n    arg[0] the letter 's'"
					+ "\n    arg[1] the path to the shape graph to check for satisfiability"
					+ "\n To perform a containment check (does the first shape graph contain the second?):"
					+ "\n    arg[0] the letter 'c'" + "\n    arg[1] the path to the first shape graph"
					+ "\n    arg[2] the path to the second shape graph" + "\n To perform a validity check:"
					+ "\n    arg[0] the letter 'v'" + "\n    arg[1] the path to the shape graph"
					+ "\n    arg[2] the path to the data graph" + "");
		} else if (args[0].equals("s")) {
			String pathToShapeGraph = args[1];
			System.out.println("Performing Satisfiability check of " + pathToShapeGraph);
			testSAT(pathToShapeGraph, outputFilename, proverCommand);
		} else if (args[0].equals("v")) {
			String pathToShapeGraph = args[1];
			String pathToDataraph = args[2];
			System.out.println("Performing Validity check of data graph " + pathToDataraph);
			System.out.println("... with respect to shape graph " + pathToShapeGraph);
			testValidity(pathToShapeGraph, pathToDataraph, outputFilename, proverCommand);
		} else if (args[0].equals("c")) {
			String pathToShapeGraph = args[1];
			String pathToSecondShapeGraph = args[2];
			System.out.println("Does shape graph " + pathToShapeGraph);
			System.out.println("... contain shape graph " + pathToSecondShapeGraph + "?");
			testContainment(pathToShapeGraph, pathToSecondShapeGraph, outputFilename, proverCommand);
		} else if (args[0].equals("a")) {
			String pathToShapeGraph = args[1];
			String pathToActions = args[2];
			System.out.println("Is the validation of shape graph " + pathToShapeGraph);
			System.out.println("... affected by actions " + pathToActions + "?");
			testActionsStaticValidation(pathToShapeGraph, pathToActions, outputFilename, proverCommand);
		}

	}

	public static boolean testSAT(String pathToSHACL, String pathToTPTP, String proverCommand)
			throws RDFParseException, RepositoryException, IOException {
		File graph = new File(pathToSHACL);

		Repository repo = new SailRepository(new MemoryStore());
		RepositoryConnection conn = repo.getConnection();
		conn.add(graph);

		FOL_Encoder encoder = new TPTP_Encoder();

		ShapeReader converter = new ShapeReader(conn);

		converter.convert(encoder, 's');

		File outputFile = new File(pathToTPTP);
		outputFile.createNewFile();
		FileOutputStream outStream = new FileOutputStream(outputFile, false);
		outStream.write(encoder.getEncodingAsString().getBytes(Charset.forName("UTF-8")));
		outStream.close();

		Runtime rt = Runtime.getRuntime();
		//Process pr = rt.exec(new String[] { proverCommand, pathToTPTP });
		Process pr = rt.exec((proverCommand+" "+pathToTPTP).split(" "));
		InputStream consoleOutput = pr.getInputStream();

		StringBuilder textBuilder = new StringBuilder();
		int c = 0;
		while ((c = consoleOutput.read()) != -1) {
			textBuilder.append((char) c);
		}
		String output = textBuilder.toString();
		TestOutput outResult = new TestOutput(output);
		// System.out.println(output);
		System.out.println("Is satisfiable? " + outResult.isSatisfiable());
		if(outResult.isSatisfiable()) {
			System.out.println(outResult.hasFiniteModelBeenFound() ? "A finite satisfiable model has been found." : "A satisfiable model has been found, but it might not be finite.");
		} else
			System.out.println("No model has been found.");
		System.out.println("Memory (KB) " + outResult.getMemoryUsedKB());
		System.out.println("Time (s) " + outResult.getTimeElapsedSeconds());
		return outResult.isSatisfiable();
	}

	public static boolean testValidity(String pathToSHACL, String pathToGraph, String pathToTPTP, String proverCommand)
			throws RDFParseException, RepositoryException, IOException {

		File graph = new File(pathToSHACL);
		Repository repo = new SailRepository(new MemoryStore());
		RepositoryConnection conn = repo.getConnection();
		conn.add(graph);

		File dataGraph = new File(pathToGraph);
		Repository repoData = new SailRepository(new MemoryStore());
		RepositoryConnection connData = repoData.getConnection();
		connData.add(dataGraph);

		FOL_Encoder encoder = new TPTP_Encoder();

		ShapeReader converter = new ShapeReader(conn, connData);

		converter.convert(encoder, 'v');

		File outputFile = new File(pathToTPTP);
		outputFile.createNewFile();
		FileOutputStream outStream = new FileOutputStream(outputFile, false);
		outStream.write(encoder.getEncodingAsString().getBytes(Charset.forName("UTF-8")));
		outStream.close();

		Runtime rt = Runtime.getRuntime();
		//Process pr = rt.exec(new String[] { proverCommand, pathToTPTP });
		Process pr = rt.exec((proverCommand+" "+pathToTPTP).split(" "));
		InputStream consoleOutput = pr.getInputStream();

		StringBuilder textBuilder = new StringBuilder();
		int c = 0;
		while ((c = consoleOutput.read()) != -1) {
			textBuilder.append((char) c);
		}
		String output = textBuilder.toString();
		TestOutput outResult = new TestOutput(output);
		// System.out.println(output);
		System.out.println("Is data graph valid? " + outResult.isSatisfiable());
		if(outResult.isSatisfiable()) {
			System.out.println(outResult.hasFiniteModelBeenFound() ? "A finite satisfiable model has been found." : "A satisfiable model has been found, but it might not be finite");
		} else
			System.out.println("No model has been found.");
		System.out.println("Memory (KB) " + outResult.getMemoryUsedKB());
		System.out.println("Time (s) " + outResult.getTimeElapsedSeconds());

		return outResult.isSatisfiable();
	}

	public static boolean testContainment(String pathToSHACLone, String pathToSHACLtwo, String pathToTPTP,
			String proverCommand) throws RDFParseException, RepositoryException, IOException {

		File shapeGraphOne = new File(pathToSHACLone);
		Repository repoSone = new SailRepository(new MemoryStore());
		RepositoryConnection connSone = repoSone.getConnection();
		connSone.add(shapeGraphOne);

		File shapeGraphTwo = new File(pathToSHACLtwo);
		Repository repoStwo = new SailRepository(new MemoryStore());
		RepositoryConnection connStwo = repoStwo.getConnection();
		connStwo.add(shapeGraphTwo);

		FOL_Encoder encoder = new TPTP_Encoder();

		ShapeReader converter = new ShapeReader(connSone, connStwo);

		// System.out.println(converter.getDebugPrint());

		converter.convert(encoder, 'c');

		File outputFile = new File(pathToTPTP);
		outputFile.createNewFile();
		FileOutputStream outStream = new FileOutputStream(outputFile, false);
		outStream.write(encoder.getEncodingAsString().getBytes(Charset.forName("UTF-8")));
		outStream.close();

		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec((proverCommand+" "+pathToTPTP).split(" "));
		//Process pr = rt.exec(new String[] { proverCommand, pathToTPTP });
		InputStream consoleOutput = pr.getInputStream();

		StringBuilder textBuilder = new StringBuilder();
		int c = 0;
		while ((c = consoleOutput.read()) != -1) {
			textBuilder.append((char) c);
		}
		String output = textBuilder.toString();
		TestOutput outResult = new TestOutput(output);
		// System.out.println(output);
		System.out.println("Is the first shape graph contained in the second? " + (!outResult.isSatisfiable()));
		if (!outResult.isSatisfiable())
			System.out.println("Whenever a data graph is validated by the first, it is also validated by the second (no model has been found).");
		else
			System.out.println("There exists a "+(outResult.hasFiniteModelBeenFound() ? "FINITE " : "")+"data graph validated by the first but not by the second.");
		System.out.println("Memory (KB) " + outResult.getMemoryUsedKB());
		System.out.println("Time (s) " + outResult.getTimeElapsedSeconds());

		return (!outResult.isSatisfiable());
	}

	public static boolean testActionsStaticValidation(String pathToSHACLone, String pathToActionJson, String pathToTPTP,
			String proverCommand) throws Exception {

		File shapeGraphOne = new File(pathToSHACLone);
		Repository repoSone = new SailRepository(new MemoryStore());
		RepositoryConnection connSone = repoSone.getConnection();
		connSone.add(shapeGraphOne);
		List<Action> actions = ActionParser.readActionsFromFile(pathToActionJson);

		File shapeGraphTwo = new File(pathToSHACLone);
		Repository repoStwo = new SailRepository(new MemoryStore());
		RepositoryConnection connStwo = repoStwo.getConnection();
		connStwo.add(shapeGraphTwo);

		// change shape names in the second version
		String updateQuery = """
				    PREFIX sh: <http://www.w3.org/ns/shacl#>

				    DELETE {
				        ?shape ?p ?o .
				    }
				    INSERT {
				        ?newShape ?p ?o .
				    }
				    WHERE {
				        ?shape ?p ?o .
				        FILTER EXISTS {
				            ?shape a ?shapeType .
				            FILTER (?shapeType IN (sh:NodeShape, sh:PropertyShape))
				        }
				        BIND(IRI(CONCAT(STR(?shape), "II")) AS ?newShape)
				    };

				    # Preserve all other triples
				    INSERT {
				        ?s ?p ?o .
				    }
				    WHERE {
				        ?s ?p ?o .
				        FILTER NOT EXISTS {
				            ?s a ?shapeType .
				            FILTER (?shapeType IN (sh:NodeShape, sh:PropertyShape))
				        }
				    }
				""";

		connStwo.prepareUpdate(updateQuery).execute();

		FOL_Encoder encoder = new TPTP_Encoder(actions);

		ShapeReader converter = new ShapeReader(connSone, connStwo, actions);

		// System.out.println(converter.getDebugPrint());

		converter.convert(encoder, 'a');

		File outputFile = new File(pathToTPTP);
		outputFile.createNewFile();
		FileOutputStream outStream = new FileOutputStream(outputFile, false);
		outStream.write(encoder.getEncodingAsString().getBytes(Charset.forName("UTF-8")));
		outStream.close();

		Runtime rt = Runtime.getRuntime();
		// sat --sat_solver fmf input_file.p
		//--finite_model_find on --smt_min_domain_size 1 --smt_max_domain_size 10
		String[] a = (proverCommand+" "+pathToTPTP).split(" ");
		Process pr = rt.exec((proverCommand+" "+pathToTPTP).split(" "));
		//Process pr = rt.exec(new String[] { proverCommand, "--saturation_algorithm","fmb", pathToTPTP });
		//Process pr = rt.exec(new String[] { proverCommand, pathToTPTP });
		InputStream consoleOutput = pr.getInputStream();

		StringBuilder textBuilder = new StringBuilder();
		int c = 0;
		while ((c = consoleOutput.read()) != -1) {
			textBuilder.append((char) c);
		}
		String output = textBuilder.toString();
		TestOutput outResult = new TestOutput(output);
		// System.out.println(output);
		System.out.println("Is validation of the shape graph maintained after performing the actions? " + (!outResult.isSatisfiable()));
		if (!outResult.isSatisfiable()) {
			System.out.println("Whenever a valid data graph is subject to the actions, validity is maintained (no model has been found).");
			}
		else
			System.out.println("There exists a "+(outResult.hasFiniteModelBeenFound() ? "FINITE " : "")+"valid data graph that, after the actions are performed, becomes invalid.");
		System.out.println("Memory (KB) " + outResult.getMemoryUsedKB());
		System.out.println("Time (s) " + outResult.getTimeElapsedSeconds());

		return (!outResult.isSatisfiable());
	}
}
