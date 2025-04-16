package converter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {


	public static String tptpPrefix = "fof";
	public static boolean encodeUNA = true;
	
	public static String shapeFile = null;
	public static String graphFile = null;
	public static String tptpOutputFile = null;
	public static String proverPath = "./vampire";
	
	public static void readDefaultConfig() throws IOException{
	InputStream input = new FileInputStream("config.properties");
    Properties prop = new Properties();
    prop.load(input);
    if(prop.containsKey("encodeUNA"))
    	encodeUNA = Boolean.valueOf(prop.getProperty("encodeUNA"));
    if(prop.containsKey("tptpPrefix"))
    	tptpPrefix = prop.getProperty("tptpPrefix");
   
    if(prop.containsKey("shapeFile"))
    	shapeFile = prop.getProperty("shapeFile");
    if(prop.containsKey("graphFile"))
    	graphFile = prop.getProperty("graphFile");
    if(prop.containsKey("tptpOutputFile"))
    	tptpOutputFile = prop.getProperty("tptpOutputFile");
    if(prop.containsKey("proverPath")) {
    	proverPath = prop.getProperty("proverPath");  	
    }
    
	}
}
