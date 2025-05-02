package actions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ShapeAction extends Action{

	public String predicate;
	public String objectShape;
	public String subjectShape;
	
	public boolean isAdd;
	
	
	/**
	 * 
	 * @param isAdd
	 * @param predicate
	 * @param subjectShape
	 * @param isSubject
	 * @param objectShape a SHACL graph in turtle format, containing only one named shape (only one shape that has an IRI as a shape name). There can be other shapes but they must be identified by blank nodes.
	 */
	@JsonCreator
	public ShapeAction(@JsonProperty("isAdd") boolean isAdd, @JsonProperty("predicate") String predicate, @JsonProperty("subjectShape") String subjectShape, @JsonProperty("objectShape") String objectShape) {
		this.predicate = predicate;
		this.objectShape = objectShape.replace("\\n", "\n");;
		this.subjectShape = subjectShape.replace("\\n", "\n");;
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
