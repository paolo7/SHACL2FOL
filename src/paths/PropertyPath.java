package paths;

public abstract class PropertyPath {

	private PathType type;
	
	protected void setType(PathType type) {
		this.type = type;
	}
	
	public PathType getType() {
		return type;
	}
}
