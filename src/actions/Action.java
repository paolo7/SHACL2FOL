package actions;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = "type"
)
@JsonSubTypes({
  @JsonSubTypes.Type(value = PathAction.class, name = "PathAction"),
  @JsonSubTypes.Type(value = ShapeAction.class, name = "ShapeAction"),
  @JsonSubTypes.Type(value = TupleAction.class, name = "TupleAction")
})

public abstract class Action {

	private boolean hasBeenDefined = false;
	public String shapePlaceholder = null;
	
	public abstract boolean isAdd();
	
	public abstract String getLeftOperandProperty();

	/**
	 * This is a helper function to keep track of whether the action has already been applied somewhere
	 * and in that case its definition already exists.
	 * @param value
	 */
	public void setHasBeenDefined(boolean hasBeenDefined) {
		this.hasBeenDefined = hasBeenDefined;
		this.shapePlaceholder = null;
	}
	public boolean hasBeenDefined() {
		return hasBeenDefined;
	}
}