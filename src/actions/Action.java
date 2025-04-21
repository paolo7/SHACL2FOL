package actions;

public abstract class Action {

	private boolean hasBeenDefined = false;
	
	public abstract boolean isAdd();
	
	public abstract String getLeftOperandProperty();
	
	public abstract String getSubjectConstraint();
	
	public abstract String getObjectConstraint();

	/**
	 * This is a helper function to keep track of whether the action has already been applied somewhere
	 * and in that case its definition already exists.
	 * @param value
	 */
	public void setHasBeenDefined(boolean hasBeenDefined) {
		this.hasBeenDefined = hasBeenDefined;
	}
	public boolean hasBeenDefined() {
		return hasBeenDefined;
	}
}