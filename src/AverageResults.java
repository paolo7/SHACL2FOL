import java.util.LinkedList;
import java.util.List;

public class AverageResults {

	public List<TestOutput> outputs = new LinkedList<TestOutput>();
	
	public double getAverageTimeS() {
		double time = 0;
		for (TestOutput t : outputs) {
			time += t.getTimeElapsedSeconds();
		}
		return time/((double) outputs.size());
	}
	public double getAverageMemoryKB() {
		double memory = 0;
		for (TestOutput t : outputs) {
			memory += t.getMemoryUsedKB();
		}
		return memory/((double) outputs.size());
	}
	public double getAverageSatisfiability() {
		double sat = 0;
		double nonSatComputed = 0;
		for (TestOutput t : outputs) {
			if(t.isSATcomputed()){				
				if(t.isSatisfiable()){
					sat += 1;
				} 
			}else nonSatComputed++;
		}
		return sat/((double) outputs.size()-nonSatComputed);
	}
	public double getAverageSatComputed() {
		double sat = 0;
		for (TestOutput t : outputs) if(t.isSATcomputed()){
			sat += 1;
		}
		return sat/((double) outputs.size());
	}
}
