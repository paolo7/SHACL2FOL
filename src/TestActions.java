import java.io.IOException;

import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;

public class TestActions {

	
	public static void main(String[] args) throws RDFParseException, UnsupportedRDFormatException, IOException {
		
		// test sat
		runCommand(new String[]{"s","./M1.ttl"});
		
		
		// test cont 1
		
		runCommand(new String[]{"c","./M1.ttl","./M1.ttl"});
		
		// test cont 2
		runCommand(new String[]{"c","./M1.ttl","./M2.ttl"});
		// test actions
		runCommand(new String[]{"a","./M1.ttl","./actions.ttl"});
		//String[] parameters = new String[]{"a","./M1.ttl","./actions.ttl"};
		
		//String[] parameters = new String[]{"a","./StudentShapesAlt.ttl","./actions.ttl"};		
		//SHACLFOLMain.main(parameters);
		
	}
	
	private static void runCommand(String[] args) throws RDFParseException, RepositoryException, IOException {
		SHACLFOLMain.main(args);
		System.out.println();
	}
}
