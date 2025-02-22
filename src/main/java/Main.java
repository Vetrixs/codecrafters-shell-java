import java.io.*;
import java.nio.Buffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws Exception {

        Set<String> buildInCommands = new HashSet<>();
        buildInCommands.add("type");
        buildInCommands.add("exit");
        buildInCommands.add("echo");
        buildInCommands.add("pwd");

        String path = System.getenv("PATH");


        do {
            System.out.print("$ ");

            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();

            String[] input_array = input.split(" ");

            switch (input_array[0]) {
                case "exit":
                    System.exit(0);
                case "echo":
                    System.out.println(Arrays.stream(input_array).skip(1).collect(Collectors.joining(" ")));
                    break;
                case "type":
                    typeCommand(buildInCommands, input_array, path);
                    break;
                case "pwd":
                    System.out.println(Paths.get("").toAbsolutePath());
                    break;
                default:
                    Path executable = searchInPath(path, input_array[0]);
                    if (executable == null) {
                        System.out.printf("%s: command not found%n", input_array[0]);
                        break;
                    }
                    runExternalProgram(input_array);
            }
        } while (true);
    }

    private static void runExternalProgram(String[] input_array) {
        Process process;
        try {
            // Execute the external command.
            process = new ProcessBuilder(input_array).start();

            OutputStream outputStream = process.getOutputStream();

            while (process.isAlive()) {
                int c;
                System.out.println("Normal Stream:");
                while ((c = process.getInputStream().read()) != -1) {
                    System.out.print((char) c);
                }
                int a;
                System.out.println("#####");
                System.out.println("Error Stream:");
                while ((a = process.getErrorStream().read()) != -1) {
                    System.out.print((char) a);
                }
            }
            System.out.println("Exit status: %d".formatted(process.exitValue()));
        } catch (IOException e) {
            System.err.println("Error executing command: " + e.getMessage());
        }
    }

    private static void typeCommand(Set<String> buildInCommands, String[] input_array, String path) {
        if (buildInCommands.contains(input_array[1])) {
            System.out.printf("%s is a shell builtin%n", input_array[1]);
            return;
        }
        Path executable = searchInPath(path, input_array[1]);
        if (executable != null) {
            System.out.printf("%s is %s%n", input_array[1], executable);
        } else {
            System.out.printf("%s: not found%n", input_array[1]);
        }
    }

    public static Path searchInPath(String pathEnv, String command) {
        if (pathEnv == null || pathEnv.isEmpty()) {
            return null;
        }
        String splitter = ":";
        if (System.getProperty("os.name").startsWith("Windows")) {
            splitter = ";";
        }

        // Split the PATH environment variable into individual directories
        String[] paths = pathEnv.split(splitter);
        for (String directory : paths) {
            try {
                // Check if the directory exists
                Path directoryPath = Paths.get(directory);
                if (!Files.exists(directoryPath)) {
                    continue;
                }

                Optional<Path> executable;
                if (command.endsWith(".exe")) {
                    executable = Files.find(directoryPath, 1,
                                    (p, attributes) -> p.getFileName().toString().equals(command)
                                            && Files.isExecutable(p))
                            .findFirst();
                } else {
                    executable = Files.find(directoryPath, 1,
                                    (p, attributes) -> p.getFileName().toString().replaceFirst("\\.exe$", "").equals(command)
                                            && Files.isExecutable(p))
                            .findFirst();
                }

                if (executable.isPresent()) {
                    return executable.get();
                }
            } catch (IOException e) {
                // Handle the case where a directory does not exist
                System.err.println("Error searching in " + directory);
            }
        }

        // If no executable is found, return null
        return null;
    }
}
