package actions;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.Values;

public class SimpleAction implements Action{
	
	public final IRI leftOperand;
	public final IRI rightOperand;
	public final boolean isAdd;
	public final boolean isClass;
	
	public SimpleAction(String leftOperand, String rightOperand, boolean isAdd, boolean isClass) {
		this.leftOperand = Values.iri(leftOperand);
		this.rightOperand = Values.iri(rightOperand);
		this.isAdd = isAdd;
		this.isClass = isClass;
	}
}
