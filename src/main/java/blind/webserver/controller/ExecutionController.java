package blind.webserver.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Entry point for receiving an execution request and authorize it.
 */
@RestController
public class ExecutionController {

	/**
	 * Data to show in console.
	 */
	private static final List<String> CONSOLE = new ArrayList<>();
	
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
	public void execute(@RequestBody ExecutionRequest request) throws Exception {
		ExecutionController.SETUP = request.setup;
		CONSOLE.clear();
		CONSOLE.add("New setup request received !");
	}
	
	/**
	 * Allows execution of current setup.
	 */
	@PostMapping("/allowExecution")
	public void execute(@RequestParam String key) throws Exception {
		if (CURRENT_PROCESS != null) {
			CURRENT_PROCESS.destroyForcibly();
		}
		CONSOLE.add("Setup allowed, starting...");
		CURRENT_PROCESS = runSetup(SETUP, key);
		inheritIO(CURRENT_PROCESS, s -> CONSOLE.add(s));
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
			final Process p = new ProcessBuilder().command("java", "-Djava.security.manager", "-Djava.security.policy=blockNetwork.policy", "-jar", "haystack.jar").start();
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