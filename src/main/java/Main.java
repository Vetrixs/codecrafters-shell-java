import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws Exception {
        // Uncomment this block to pass the first stage

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
            process = Runtime.getRuntime().exec(input_array);

            // Get streams for reading from and writing to the process
            InputStream inputStream = process.getInputStream();
            OutputStream outputStream = process.getOutputStream();

            // Read console input and write to the process
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));

            // Read output from the process and print it to console.
            int c;
            while ((c = inputStream.read()) != -1) {
                System.out.print((char) c);
            }

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

                // Search for an executable with the given name in this directory
                Optional<Path> executable = Files.find(directoryPath, 1,
                                (p, attributes) -> p.getFileName().toString().equals(command)
                                        && Files.isExecutable(p))
                        .findFirst();

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
