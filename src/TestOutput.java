import java.util.Scanner;

public class TestOutput {
	private String testOutput;

	private Boolean sat = null;
	private Boolean satComputed = null;
	private Double timeElapsed = null;
	private Long memoryUsed = null;

	public TestOutput(String testOutput) {
		this.testOutput = testOutput;
		if (isSATcomputed())
			isSatisfiable();
		getMemoryUsedKB();
		getTimeElapsedSeconds();
	}

	public Boolean isSATcomputed() {
		if (satComputed != null)
			return satComputed;
		if (testOutput.contains("Refutation not found") || testOutput.contains("Time limit reached"))
			satComputed = false;
		else
			satComputed = true;
		return satComputed;
	}
	
	public Boolean hasFiniteModelBeenFound() {
		return this.testOutput.contains("Finite Model Found");
	}

	public Boolean isSatisfiable() {
		if (sat != null)
			return sat;
		if (testOutput.contains("status Unsatisfiable"))
			sat = false;
		if (testOutput.contains("status Satisfiable"))
			sat = true;
		if (sat == null)
			throw new RuntimeException("Failed to parse the output of the theorem prover.");
		return sat;
	}

	public Long getMemoryUsedKB() {
		if (memoryUsed != null)
			return memoryUsed;
		Scanner scanner = new Scanner(testOutput);
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (line.contains("% Memory used [KB]: ")) {
				String KB = line.replace("% Memory used [KB]: ", "");
				memoryUsed = Long.parseLong(KB);
			}
		}
		scanner.close();
		if (memoryUsed == null)
			throw new RuntimeException("Failed to parse the output of the theorem prover.");
		return memoryUsed;
	}

	public Double getTimeElapsedSeconds() {
		if (timeElapsed != null)
			return timeElapsed;
		Scanner scanner = new Scanner(testOutput);
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (line.contains("% Time elapsed: ")) {
				String time = line.replace("% Time elapsed: ", "").replace(" s", "");
				timeElapsed = Double.parseDouble(time);
			}
		}
		scanner.close();
		if (timeElapsed == null)
			throw new RuntimeException("Failed to parse the output of the theorem prover.");
		return timeElapsed;
	}
}
