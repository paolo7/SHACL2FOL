import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import converter.Config;
import converter.ShapeReader;
import logic.FOL_Encoder;
import logic.TPTP_Encoder;

public class TestMain {

	public static void main(String[] args) throws RDFParseException, UnsupportedRDFormatException, IOException {
		
		Config.readDefaultConfig();

		String filename = Config.shapeFile;
		String filenameTwo = Config.graphFile;
		String outputFilename = Config.tptpOutputFile;
		String proverCommand = Config.proverPath;
		
		
		//testSAT(filename,outputFilename,proverCommand);
		
		//testValidity(filename,filenameTwo,outputFilename,proverCommand);
		
		SHACLFOLMain.testContainment(filename,filenameTwo,outputFilename,proverCommand);
		
		if(filename.equals(Config.shapeFile))return;
		//String filename = "/home/office/SHACL2FOL/SHACL2FOL/shapes/test_recursion.ttl";
		
		/*System.out.println(ArtificialTestSets.GenerateShapes(1 // random seed
				,10 //numberOfNodeShapes
				,10 //numberOfPredicateShapes
				,10 //numberOfConstantIRI
				,10 //numberOfConstantStrings
				,0.75 // probabilityNodeTarget));
		));*/
		
		for(int i = 2; i < 100; i += 2) {
			AverageResults avg = ArtificialTestSets.testManyShapes(10
					,1 // random seed
					,i //numberOfNodeShapes
					,i //numberOfPredicateShapes
					,6+(i) //numberOfConstantIRI
					,6+(i) //numberOfConstantStrings
					,0.5 // probabilityNodeTarget));
					,2 // path length
					,2 // path min cardinality
					,2 // path max cardinality
					,0.0 // probabilityNegateOtherShape
					,false // allow recursion
					,0.2/i // probabilityNodeKindFilter
					,0.3 // probabilityObjectOfPredicateTarget
			);
			System.out.println("Average seconds: "+avg.getAverageTimeS());
			System.out.println("Average KB: "+avg.getAverageMemoryKB());
			System.out.println("Average sat ratio: "+avg.getAverageSatisfiability());
			System.out.println("Average sat computed: "+avg.getAverageSatComputed());
			System.out.println();
		}
		
	}
	
	private static void testContainment(String pathToSHACLone, String pathToSHACLtwo, String pathToTPTP, String proverCommand) throws RDFParseException, RepositoryException, IOException {
		
		File shapeGraphOne = new File(pathToSHACLone);
		Repository repoSone = new SailRepository(new MemoryStore());
		RepositoryConnection connSone = repoSone.getConnection();
		connSone.add(shapeGraphOne);
		
		File shapeGraphTwo = new File(pathToSHACLtwo);
		Repository repoStwo = new SailRepository(new MemoryStore());
		RepositoryConnection connStwo = repoStwo.getConnection();
		connStwo.add(shapeGraphTwo);
		
		FOL_Encoder encoder = new TPTP_Encoder();
		
		ShapeReader converter = new ShapeReader(connSone,connStwo);
		
		//System.out.println(converter.getDebugPrint());
		
		converter.convert(encoder, 'c');
		
		File outputFile = new File(pathToTPTP);
		outputFile.createNewFile();
		FileOutputStream outStream = new FileOutputStream(outputFile, false); 
		outStream.write(encoder.getEncodingAsString().getBytes(Charset.forName("UTF-8")));
		outStream.close();
		
		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec(new String[] {proverCommand, pathToTPTP});
		InputStream consoleOutput = pr.getInputStream();
		
		StringBuilder textBuilder = new StringBuilder();
		int c = 0;
        while ((c = consoleOutput.read()) != -1) {
            textBuilder.append((char) c);
        }
        String output = textBuilder.toString();
        TestOutput outResult = new TestOutput(output);
        System.out.println(output);
        System.out.println("Is satisfiable? "+outResult.isSatisfiable());
        System.out.println("Memory (KB) "+outResult.getMemoryUsedKB());
        System.out.println("Time (s) "+outResult.getTimeElapsedSeconds());
        
		//System.out.println("\nEncoding:\n"+encoder.getEncodingAsString());
	}

	private static void testValidity(String pathToSHACL, String pathToGraph, String pathToTPTP, String proverCommand) throws RDFParseException, RepositoryException, IOException {
		
		File graph = new File(pathToSHACL);
		Repository repo = new SailRepository(new MemoryStore());
		RepositoryConnection conn = repo.getConnection();
		conn.add(graph);
		
		File dataGraph = new File(pathToGraph);
		Repository repoData = new SailRepository(new MemoryStore());
		RepositoryConnection connData = repoData.getConnection();
		connData.add(dataGraph);
		
		FOL_Encoder encoder = new TPTP_Encoder();
		
		ShapeReader converter = new ShapeReader(conn,connData);
		
		//System.out.println(converter.getDebugPrint());
		
		converter.convert(encoder, 'v');
		
		File outputFile = new File(pathToTPTP);
		outputFile.createNewFile();
		FileOutputStream outStream = new FileOutputStream(outputFile, false); 
		outStream.write(encoder.getEncodingAsString().getBytes(Charset.forName("UTF-8")));
		outStream.close();
		
		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec(new String[] {proverCommand,pathToTPTP});
		InputStream consoleOutput = pr.getInputStream();
		
		StringBuilder textBuilder = new StringBuilder();
		int c = 0;
        while ((c = consoleOutput.read()) != -1) {
            textBuilder.append((char) c);
        }
        String output = textBuilder.toString();
        TestOutput outResult = new TestOutput(output);
        System.out.println(output);
        System.out.println("Is satisfiable? "+outResult.isSatisfiable());
        System.out.println("Memory (KB) "+outResult.getMemoryUsedKB());
        System.out.println("Time (s) "+outResult.getTimeElapsedSeconds());
        
		//System.out.println("\nEncoding:\n"+encoder.getEncodingAsString());
	}
	
	

}
