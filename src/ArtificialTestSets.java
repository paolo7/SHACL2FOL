import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Random;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import converter.Config;
import converter.ShapeReader;
import logic.FOL_Encoder;
import logic.TPTP_Encoder;

public class ArtificialTestSets {

	private static Random rand = new Random();
	
	public static String GenerateShapes(int randomSeed, int numberOfNodeShapes, int numberOfPredicateShapes,
			int numberOfConstantIRI, int numberOfConstantStrings,
			double probabilityNodeTarget, int pathLength,int pathMinCardinality, int pathMaxCardinality, double probNegateOtherShape,
			boolean allowRecursion, double probabilityNodeKindFilter, double probabilityObjectOfPredicateTarget) {
		
		rand = new Random(randomSeed);
				
		String shapes = "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" + 
				"@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n" + 
				"@prefix sh: <http://www.w3.org/ns/shacl#> .\n" + 
				"@prefix : <http://e.com/> .\n\n";
		
		for(int i = 0; i < numberOfNodeShapes; i++) {
			shapes += ":nShape"+toAlphabetic(i)+" a sh:NodeShape . \n";
			if(rand.nextDouble() <= probabilityNodeTarget)
				shapes += ":nShape"+toAlphabetic(i)+" sh:targetNode "+getRandomIRI(numberOfConstantIRI)+" . \n";
			
			if(rand.nextDouble() <= probabilityObjectOfPredicateTarget)
				shapes += ":nShape"+toAlphabetic(i)+(rand.nextBoolean() ? " sh:targetSubjectsOf " : " sh:targetObjectsOf ")+getRandomIRI(numberOfConstantIRI)+" . \n";
			
			if(rand.nextDouble() <= probNegateOtherShape)
				shapes += ":nShape"+toAlphabetic(i)+" sh:not "+getShapeName(i,allowRecursion,numberOfNodeShapes)+" . \n";
			
			if(rand.nextDouble() <= probabilityNodeKindFilter)
				shapes += ":nShape"+toAlphabetic(i)+" sh:nodeKind sh:IRI . \n";
			
			shapes += "\n";
		} 
		for(int i = 0; i < numberOfPredicateShapes; i++) {
			shapes += ":pShape"+toAlphabetic(i)+" a sh:PredicateShape . \n";
			if(rand.nextDouble() <= probabilityNodeTarget)
				shapes += ":nShape"+toAlphabetic(i)+" sh:targetNode "+getRandomIRI(numberOfConstantIRI)+" . \n";
			shapes += ":nShape"+toAlphabetic(i)+" sh:path "+getRandomPath(numberOfConstantIRI,pathLength)+" . \n";
			
			if(rand.nextDouble() <= probabilityObjectOfPredicateTarget)
				shapes += ":nShape"+toAlphabetic(i)+(rand.nextBoolean() ? " sh:targetSubjectsOf " : " sh:targetObjectsOf ")+getRandomIRI(numberOfConstantIRI)+" . \n";
			
			
			boolean doMax = rand.nextBoolean();
			if(doMax) {
				if(pathMaxCardinality >= 0) {
					shapes += ":nShape"+toAlphabetic(i)+" sh:maxCount "+(rand.nextInt(pathMaxCardinality+1))+" . \n";
				}				
			} else {
				if(pathMinCardinality > 0) {
					shapes += ":nShape"+toAlphabetic(i)+" sh:minCount "+(rand.nextInt(pathMinCardinality)+1)+" . \n";
				}
			}
			
			
			if(rand.nextDouble() <= probNegateOtherShape)
				shapes += ":nShape"+toAlphabetic(i)+" sh:not "+getShapeName(i,allowRecursion,numberOfNodeShapes)+" . \n";
			
			if(rand.nextDouble() <= probabilityNodeKindFilter)
				shapes += ":nShape"+toAlphabetic(i)+" sh:nodeKind "+(rand.nextBoolean() ? "sh:IRI" : "sh:Literal")+" . \n";
			
			shapes += "\n";
		} 
		
		return shapes;
		
	}
	
	private static String getShapeName(int i, boolean allowRecursion, int numberOfNodeShapes) {
		return (rand.nextBoolean() ? ":nShape" : ":pShape")+toAlphabetic(    
				allowRecursion ? (rand.nextInt(numberOfNodeShapes)) : (i+1+rand.nextInt(numberOfNodeShapes-i))
				);
	}
	
	public static AverageResults testManyShapes(int times, int randomSeed, int numberOfNodeShapes, int numberOfPredicateShapes,
			int numberOfConstantIRI, int numberOfConstantStrings,
			double probabilityNodeTarget,int pathLength, int pathMinCardinality, int pathMaxCardinality, double probNegateOtherShape,
			boolean allowRecursion, double probabilityNodeKindFilter, double probabilityObjectOfPredicateTarget) throws IOException {
		
		AverageResults avg = new AverageResults();
		for(int i = 0; i < times; i++) {
			avg.outputs.add(test(GenerateShapes(i, numberOfNodeShapes, numberOfPredicateShapes, numberOfConstantIRI, numberOfConstantStrings, probabilityNodeTarget,
					pathLength,pathMinCardinality,pathMaxCardinality,probNegateOtherShape,allowRecursion, probabilityNodeKindFilter, probabilityObjectOfPredicateTarget)));
		} 
		return avg;
		
	}
	
	public static String tempShapeFile = "shapesTemp.ttl";
	public static String tempOutFile = "SCLTemp.tptp";
	
	
	public static TestOutput test(String shapes) throws IOException {
		// write the shapes to file
		File shapesOutputFile = new File(tempShapeFile);
		shapesOutputFile.createNewFile();
		FileOutputStream shapesOutStream = new FileOutputStream(shapesOutputFile, false); 
		shapesOutStream.write(shapes.getBytes(Charset.forName("UTF-8")));
		shapesOutStream.close();
		
		// load the graph
		File graph = new File(tempShapeFile);
		InputStream input = new FileInputStream(tempShapeFile);
		Repository repo = new SailRepository(new MemoryStore());
		RepositoryConnection conn = repo.getConnection();
		conn.add(graph);
		
		FOL_Encoder encoder = new TPTP_Encoder();
		
		ShapeReader converter = new ShapeReader(conn);
		
		converter.convert(encoder,'s');
		
		File outputFile = new File(tempOutFile);
		outputFile.createNewFile();
		FileOutputStream outStream = new FileOutputStream(outputFile, false); 
		outStream.write(encoder.getEncodingAsString().getBytes(Charset.forName("UTF-8")));
		outStream.close();
		
		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec(new String[] {Config.proverPath, outputFile.getAbsolutePath()});
		InputStream consoleOutput = pr.getInputStream();
		
		StringBuilder textBuilder = new StringBuilder();
		int c = 0;
        while ((c = consoleOutput.read()) != -1) {
            textBuilder.append((char) c);
        }
        String output = textBuilder.toString();
        TestOutput outResult = new TestOutput(output);
        input.close();
        return outResult;
	}
	
	private static String getRandomPath(int numberOfConstantIRI, int pathLength) {
		String path = "(";
		for(int i = 0; i < pathLength; i++) {
			path += getRandomIRI(numberOfConstantIRI)+" ";
		}
		path += ")";			
		
		return path;
	}
	
	private static String getRandomIRI(int numberOfConstantIRI) {
		return ":iri_"+toAlphabetic(rand.nextInt(numberOfConstantIRI));
	}
	
	private static String getRandomStringLiteral(int numberOfConstantStrings) {
		return "\""+toAlphabetic(rand.nextInt(numberOfConstantStrings))+"\"";
	}
	
	// code adapted from https://stackoverflow.com/questions/10813154/how-do-i-convert-a-number-to-a-letter-in-java
		private static String toAlphabetic(int i) {
		    int quot = i/26;
		    int rem = i%26;
		    char letter = (char)((int)'A' + rem);
		    if( quot == 0 ) {
		        return ""+letter;
		    } else {
		        return toAlphabetic(quot-1) + letter;
		    }
		}
}
