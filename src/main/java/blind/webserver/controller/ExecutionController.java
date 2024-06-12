package blind.webserver.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Entry point for receiving an execution request and authorize it.
 */
@RestController
public class ExecutionController {

	/**
	 * Data to show in console.
	 */
	private static final List<String> CONSOLE = new ArrayList<>();
	static {
		CONSOLE.add("Welcome to Blind webserver.");
		CONSOLE.add("Please enter your setup above to start.");
	}
	
	/**
	 * Current running process.
	 */
	private static Process CURRENT_PROCESS = null;
	
	/**
	 * Requested setup.
	 */
	private static String SETUP = null;
	
	/**
	 * Requests execution of a setup.
	 */
	@PostMapping("/requestExecution")
	public void requestExecution(@RequestBody ExecutionRequest request) throws Exception {
		ExecutionController.SETUP = request.setup;
		CONSOLE.add("New setup request received, waiting for approval...");
	}
	
	/**
	 * Allows execution of current setup.
	 */
	@PostMapping("/allowExecution")
	public void allowExecution(@RequestBody ApprovalRequest request) throws Exception {
		if (CURRENT_PROCESS != null) {
			CURRENT_PROCESS.destroyForcibly();
		}
		CONSOLE.add("Setup approved, starting...");
		CURRENT_PROCESS = runSetup(SETUP, request.key);
		inheritIO(CURRENT_PROCESS, s -> processLog(s));
	}

	private void processLog(String s) {
		try {
			final JsonNode node = new ObjectMapper().readTree(s);
			final JsonNode progress = node.get("progress");
			if (progress != null) {
				CONSOLE.add("progress : " + progress.asText() + "%");
			}
			final JsonNode match = node.get("match");
			if (match != null) {
				CONSOLE.add("match : " + new ObjectMapper().writeValueAsString(match));
			}
		} catch (Exception e) {
			// silently discard node 
		}
	}
	
	/**
	 * Retrieves console output.
	 */
	@GetMapping("/console")
	public String console() throws Exception {
		return CONSOLE.stream().collect(Collectors.joining("\n"));
	}
	
	/**
	 * Runs the specified setup using the given cipherKey.
	 * The security manager ensures no external calls can be made from the started java code.
	 */
	private static Process runSetup(String setup, String cipherKey) {
		try {
			final Process p = new ProcessBuilder().command("java", "-Djava.security.manager", "-Djava.security.policy=jvm.policy", "-jar", "encapsulate.jar").start();
			p.getOutputStream().write((cipherKey + "\n" + setup + "\n<EOF>\n").getBytes());
			p.getOutputStream().flush();
			return p;
		} catch (Exception e) {
			throw new IllegalStateException("Error during execution", e);
		}
	}
	
	/**
	 * Updates the console when new data is available.
	 */
	private static void inheritIO(Process p, Consumer<String> consoleUpdater) {
	    new Thread(new Runnable() {
	        public void run() {
	            Scanner sc = new Scanner(p.getInputStream());
	            while (sc.hasNextLine()) {
	            	consoleUpdater.accept(sc.nextLine());
	            }
	        }
	    }).start();
	    new Thread(new Runnable() {
	        public void run() {
	            Scanner sc = new Scanner(p.getErrorStream());
	            while (sc.hasNextLine()) {
	            	consoleUpdater.accept(sc.nextLine());
	            }
	        }
	    }).start();
	}
}