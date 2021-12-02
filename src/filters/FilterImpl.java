package filters;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.repository.RepositoryConnection;

public class FilterImpl implements Filter{

	private String testQuery;
	private IRI fPredicate;
	private Value fValue;
	
	public FilterImpl(String testQuery, IRI fPredicate, Value fValue) {
		this.testQuery = testQuery;
		this.fPredicate = fPredicate;
		this.fValue = fValue;
	}
	
	@Override
	public boolean test(Value v, RepositoryConnection conn) {
		BooleanQuery test = conn.prepareBooleanQuery(testQuery);
		test.setBinding("value", v);
		return test.evaluate();
	}

	@Override
	public IRI getFilterPredicate() {
		return fPredicate;
	}

	@Override
	public Value getFilterObject() {
		return fValue;
	}

}
