package actions;

public abstract class Action {

	public abstract boolean isAdd();
	
	public abstract String getLeftOperandProperty();
	
	public abstract String getSubjectConstraint();
	
	public abstract String getObjectConstraint();
	
}