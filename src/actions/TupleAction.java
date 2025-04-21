package actions;

public class TupleAction extends Action {
	
	public String predicate;
	public String subject;
	public String object;
	public boolean isAdd;

	
	public TupleAction(boolean isAdd, String predicate, String subject, String object) {
		this.predicate = predicate;
		this.subject = subject;
		this.object = object;
		this.isAdd = isAdd;
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
		return null;
	}

	@Override
	public String getObjectConstraint() {
		return null;
	}

}
