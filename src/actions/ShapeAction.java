package actions;

public class ShapeAction extends Action{

	public String predicate;
	public String shape;
	public String constant;
	public boolean isSubject;
	public boolean isAdd;
	
	/**
	 * 
	 * @param isAdd
	 * @param predicate
	 * @param constant
	 * @param isSubject
	 * @param shape a SHACL graph in turtle format, containing only one named shape (only one shape that has an IRI as a shape name). There can be other shapes but they must be identified by blank nodes.
	 */
	public ShapeAction(boolean isAdd, String predicate, String constant, boolean isSubject, String shape) {
		this.predicate = predicate;
		this.shape = shape;
		this.isAdd = isAdd;
		this.constant = constant;
		this.isSubject = isSubject;
	}
	
	@Override
	public boolean isAdd() {
		return isAdd;
	}

	@Override
	public String getLeftOperandProperty() {
		return predicate;
	}

	@Override
	public String getSubjectConstraint() {
		if(isSubject) 
			return constant;
		return null;
	}

	@Override
	public String getObjectConstraint() {
		if(!isSubject)
			return constant;
		return null;
	}

}
