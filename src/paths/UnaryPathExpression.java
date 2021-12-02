package paths;

public class UnaryPathExpression extends PropertyPath{

	private PropertyPath path;
	
	public UnaryPathExpression(PropertyPath path, PathType type) {
		setType(type);
		this.path = path;
	}
	
	public PropertyPath getPath() {
		return path;
	}
	
}
