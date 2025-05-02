public class TestActions {

	
	public static void main(String[] args) throws Exception {
		
		// test sat
		runCommand(new String[]{"s","./M1.ttl"});
		
		
		// test cont 1
		
		runCommand(new String[]{"c","./M1.ttl","./M1.ttl"});
		
		// test cont 2
		runCommand(new String[]{"c","./M1.ttl","./M2.ttl"});
		// test actions
		runCommand(new String[]{"a","./M1.ttl","./actions1.json"});
		//String[] parameters = new String[]{"a","./M1.ttl","./actions.ttl"};
		
		//String[] parameters = new String[]{"a","./StudentShapesAlt.ttl","./actions.ttl"};		
		//SHACLFOLMain.main(parameters);
		
	}
	
	private static void runCommand(String[] args) throws Exception {
		SHACLFOLMain.main(args);
		System.out.println();
	}
}
