package filters;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.repository.RepositoryConnection;

public interface Filter {
	public boolean test(Value v, RepositoryConnection con);
	public IRI getFilterPredicate();
	public Value getFilterObject();
}
