package actions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PathAction extends Action{

	public String predicate;
	public String path;
	public boolean isAdd;
	
	@JsonCreator
	public PathAction(@JsonProperty("isAdd") boolean isAdd, @JsonProperty("predicate") String predicate, @JsonProperty("path") String path) {
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

}
