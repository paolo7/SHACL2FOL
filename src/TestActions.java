import java.io.IOException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;

public class TestActions {

	
	public static void main(String[] args) throws RDFParseException, UnsupportedRDFormatException, IOException {
		
		String[] parameters = new String[]{"a","./M1.ttl","./actions.ttl"};
		SHACLFOLMain.main(parameters);
		
	}
}
