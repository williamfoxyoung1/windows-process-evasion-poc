package taskmgr_evasion_poc;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class taskmgr_evasion_poc {

    private static final String TARGET_PROCESS = "Taskmgr.exe";
    private static final long CHECK_INTERVAL_MS = 500;

    // Change this to your benign POC executable.
    // Example:
    // "C:\\Users\\YourName\\Desktop\\BenignPOC.exe"
    private static final String MANAGED_EXE = "notepad.exe";

    // Holds only the process launched by this program.
    private static Process managedProcess = null;

    public static void main(String[] args) {

        printBanner();

        boolean previousTaskManagerState = isTaskManagerRunning();

        if (previousTaskManagerState) {
            System.out.println("[INITIAL] Task Manager is currently OPEN.");
            System.out.println("[STATUS] Managed application will remain stopped.");
        } else {
            System.out.println("[INITIAL] Task Manager is currently CLOSED.");
            runManagedExe();
        }

        while (true) {

            boolean currentTaskManagerState = isTaskManagerRunning();

            // CLOSED -> OPEN
            if (currentTaskManagerState && !previousTaskManagerState) {

                System.out.println();
                System.out.println("[DETECTED] Task Manager has been OPENED.");

                terminateManagedExe();
            }

            // OPEN -> CLOSED
            if (!currentTaskManagerState && previousTaskManagerState) {

                System.out.println();
                System.out.println("[DETECTED] Task Manager has been CLOSED.");

                runManagedExe();
            }

            /*
             * If Task Manager is closed but the user manually closed
             * our managed application, report its state.
             *
             * We intentionally do NOT continuously restart it here.
             */
            if (!currentTaskManagerState &&
                managedProcess != null &&
                !managedProcess.isAlive()) {

                System.out.println(
                    "[STATUS] Managed application is no longer running."
                );

                managedProcess = null;
            }

            previousTaskManagerState = currentTaskManagerState;

            try {

                Thread.sleep(CHECK_INTERVAL_MS);

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();

                System.out.println();
                System.out.println("[STATUS] Monitoring stopped.");

                break;
            }
        }
    }

    /**
     * Prints program information.
     */
    private static void printBanner() {

        System.out.println("========================================");
        System.out.println("       Process Lifecycle Manager");
        System.out.println("========================================");
        System.out.println("Target Monitor : " + TARGET_PROCESS);
        System.out.println("Managed App    : " + MANAGED_EXE);
        System.out.println("Check Interval : "
                + CHECK_INTERVAL_MS + " ms");
        System.out.println();
        System.out.println("Press Ctrl+C to stop.");
        System.out.println();
    }

    /**
     * Determines whether Windows Task Manager is running.
     */
    private static boolean isTaskManagerRunning() {

        ProcessBuilder processBuilder = new ProcessBuilder(
            "tasklist",
            "/FI",
            "IMAGENAME eq " + TARGET_PROCESS,
            "/FO",
            "CSV",
            "/NH"
        );

        processBuilder.redirectErrorStream(true);

        try {

            Process process = processBuilder.start();

            try (BufferedReader reader =
                     new BufferedReader(
                         new InputStreamReader(
                             process.getInputStream()))) {

                String line;

                while ((line = reader.readLine()) != null) {

                    if (line.toLowerCase().contains(
                            TARGET_PROCESS.toLowerCase())) {

                        process.waitFor();
                        return true;
                    }
                }
            }

            process.waitFor();

        } catch (Exception e) {

            System.err.println(
                "[ERROR] Unable to check running processes: "
                + e.getMessage()
            );
        }

        return false;
    }

    /**
     * Launches the managed executable and stores its Process object.
     */
    private static void runManagedExe() {

        /*
         * Prevent duplicate instances launched by this manager.
         */
        if (managedProcess != null && managedProcess.isAlive()) {

            System.out.println(
                "[STATUS] Managed application is already running."
            );

            System.out.println(
                "[PID] " + managedProcess.pid()
            );

            return;
        }

        System.out.println(
            "[ACTION] Starting managed application..."
        );

        try {

            ProcessBuilder processBuilder =
                new ProcessBuilder(MANAGED_EXE);

            managedProcess = processBuilder.start();

            System.out.println(
                "[SUCCESS] Managed application started."
            );

            System.out.println(
                "[PID] " + managedProcess.pid()
            );

        } catch (Exception e) {

            managedProcess = null;

            System.err.println(
                "[ERROR] Failed to start managed application: "
                + e.getMessage()
            );
        }
    }

    /**
     * Terminates ONLY the process instance that this program launched.
     */
    private static void terminateManagedExe() {

        if (managedProcess == null) {

            System.out.println(
                "[STATUS] No managed process exists."
            );

            return;
        }

        if (!managedProcess.isAlive()) {

            System.out.println(
                "[STATUS] Managed application is already stopped."
            );

            managedProcess = null;

            return;
        }

        long pid = managedProcess.pid();

        System.out.println(
            "[ACTION] Terminating managed application..."
        );

        System.out.println(
            "[PID] " + pid
        );

        try {

            /*
             * First request a normal termination.
             */
            managedProcess.destroy();

            /*
             * Give the application a short opportunity to exit.
             */
            Thread.sleep(500);

            /*
             * If it did not terminate, force termination of only
             * this particular process.
             */
            if (managedProcess.isAlive()) {

                System.out.println(
                    "[ACTION] Process did not exit normally."
                );

                System.out.println(
                    "[ACTION] Forcing termination of PID "
                    + pid + "..."
                );

                managedProcess.destroyForcibly();
            }

            managedProcess.onExit().thenRun(() ->
                System.out.println(
                    "[SUCCESS] Managed process PID "
                    + pid + " terminated."
                )
            );

        } catch (Exception e) {

            System.err.println(
                "[ERROR] Failed to terminate managed process: "
                + e.getMessage()
            );
        }
    }
}
