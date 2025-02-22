import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Controller {

    Set<String> validCommands = Set.of(new String[]{"type", "exit", "echo", "pwd", "cd"});

    Path currentDirectory = Paths.get("").toAbsolutePath();

    public void execute(String[] input) {
        switch (input[0]) {
            case "type":
                type(input);
                break;
            case "exit":
                exit();
                break;
            case "echo":
                echo(input);
                break;
            case "pwd":
                pwd();
                break;
            case "cd":
                cd(input);
                break;
            default:
                runExternalProgram(input);
                break;
        }
    }

    private void cd(String[] input_array) {
        if(input_array.length < 2){
            System.out.printf("%s: Missing directory argument%n", input_array[0]);
            return;
        }
        String targetDir = input_array[1];
        if (Files.exists(Path.of(targetDir))) {
            currentDirectory = Path.of(targetDir).toAbsolutePath();
        } else {
            System.out.printf("%s: %s: No such file or directory%n", input_array[0], input_array[1]);
        }
    }


    private void type(String[] input_array) {
        if (validCommands.contains(input_array[1])) {
            System.out.printf("%s is a shell builtin%n", input_array[1]);
            return;
        }
        Path executable = searchInPath(input_array[1]);
        if (executable != null) {
            System.out.printf("%s is %s%n", input_array[1], executable);
        } else {
            System.out.printf("%s: not found%n", input_array[1]);
        }
    }

    private void echo(String[] input_array) {
        System.out.println(Arrays.stream(input_array).skip(1).collect(Collectors.joining(" ")));
    }

    private void exit() {
        System.exit(0);
    }

    private void pwd() {
        System.out.println(currentDirectory);
    }

    public void runExternalProgram(String[] input_array) {
        Path executable = searchInPath(input_array[0]);
        if (executable == null) {
            System.out.printf("%s: command not found%n", input_array[0]);
            return;
        }
        Process process;
        try {
            // Execute the external command.
            process = new ProcessBuilder(input_array).start();
            process.getInputStream().transferTo(System.out);
            process.getErrorStream().transferTo(System.out);
        } catch (IOException e) {
            System.err.println("Error executing command: " + e.getMessage());
        }
    }

    private Path searchInPath(String command) {
        String path = System.getenv("PATH");
        String splitter = ":";
        if (System.getProperty("os.name").startsWith("Windows")) {
            splitter = ";";
        }

        // Split the PATH environment variable into individual directories
        String[] paths = path.split(splitter);
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
