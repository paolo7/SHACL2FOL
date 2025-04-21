package actions;

public class PathAction extends Action{

	public String predicate;
	public String path;
	public boolean isAdd;
	
	public PathAction(boolean isAdd, String predicate, String path) {
		this.predicate = predicate;
		this.path = path;
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
