package paths;

import org.eclipse.rdf4j.model.IRI;

public class IRI_Path extends PropertyPath{
	
	private IRI iri;
	
	public IRI_Path(IRI iri) {
		setType(PathType.PREDICATE);
		this.iri = iri;
	}
	
	public IRI getIRI() {
		return iri;
	}
}
