package paths;

import java.util.List;

public class List_Path extends PropertyPath{
	private List<PropertyPath> pathList;
	
	public List_Path(List<PropertyPath> pathList, PathType type) {
		setType(type);
		this.pathList = pathList;
	}
	
	public List<PropertyPath> getPathList() {
		return pathList;
	}
}
