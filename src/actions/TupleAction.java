package actions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TupleAction extends Action {
	
	public String predicate;
	public String subject;
	public String object;
	public boolean isAdd;

	@JsonCreator
	public TupleAction( @JsonProperty("isAdd") boolean isAdd, @JsonProperty("predicate") String predicate, @JsonProperty("subject") String subject, @JsonProperty("object") String object) {
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

}
