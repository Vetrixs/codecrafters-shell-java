import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Controller {

    Set<String> validCommands = Set.of(new String[]{"type", "exit", "echo", "pwd", "cd", "ls"});

    Path currentDirectory = Paths.get("").toAbsolutePath();

    public void execute(String rawInput) {
        String[] input = cleanInput(rawInput);
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
            case "ls":
                ls();
                break;
            default:
                runExternalProgram(input);
                break;
        }
    }

    public String[] cleanInput(String rawInput) {
        List<String> argsList = new ArrayList<>();
        String commandString = "";
        int i = 0;
        StringBuilder sb = new StringBuilder();
        while (i < rawInput.length()) {
            // remove preceding whitespace
            while (Character.isWhitespace(rawInput.charAt(i))) {
                if (!sb.isEmpty()) {
                    argsList.add(sb.toString());
                    sb = new StringBuilder();
                }
                i++;
            }
            // get command
            if (commandString.isEmpty()) {
                while (i < rawInput.length() && !Character.isWhitespace(rawInput.charAt(i))) {
                    sb.append(rawInput.charAt(i));
                    i++;
                }
                commandString = sb.toString();
                sb = new StringBuilder();
            }
            // get double quote arg
            if (i < rawInput.length() && rawInput.charAt(i) == '\"') {
                i++;
                while (rawInput.charAt(i) != '\"') {
                    sb.append(rawInput.charAt(i));
                    i++;
                }
            }

            // get single quote arg
            if (i < rawInput.length() && rawInput.charAt(i) == '\'') {
                i++;
                while (rawInput.charAt(i) != '\'') {
                    sb.append(rawInput.charAt(i));
                    i++;
                }
            }
            // get unquoted arg
            if (i < rawInput.length() && !Character.isWhitespace(rawInput.charAt(i))
                    && rawInput.charAt(i) != '\''
                    && rawInput.charAt(i) != '\"') {
                while (i < rawInput.length() && !Character.isWhitespace(rawInput.charAt(i))) {
                    sb.append(rawInput.charAt(i));
                    i++;
                }
            }
            i++;
        }
        if (!commandString.isEmpty() && !sb.isEmpty()) {
            argsList.add(sb.toString());
        }
        List<String> inputList = new ArrayList<>();
        inputList.add(commandString);
        inputList.addAll(argsList);
        return inputList.toArray(new String[0]);
    }

    private void ls() {
        try {
            Files.list(currentDirectory).forEach(System.out::println);
        } catch (IOException ioException) {
            System.err.println("Error printing files: " + ioException.getMessage());
        }
    }

    private void cd(String[] input_array) {

        if (input_array.length < 2) {
            System.out.printf("%s: Missing directory argument%n", input_array[0]);
            return;
        }
        String targetDir = input_array[1];
        Path targetAbsolutePath;
        if (targetDir.startsWith("~")) {
            // System.getProperties("user.home");
            targetAbsolutePath = Path.of(System.getenv("HOME"));
        } else if (targetDir.startsWith("./")) {
            targetDir = targetDir.replace("./", "");
            targetAbsolutePath = Path.of(currentDirectory.toString(), targetDir).toAbsolutePath();
        } else if (targetDir.startsWith("../")) {
            // count occurrences
            int i = targetDir.split("\\.\\./", -1).length - 1;
            List<String> list = Arrays.stream(currentDirectory.toString().split(Pattern.quote(File.separator))).collect(Collectors.toList());
            for (int j = 0; j < i; j++) {
                list.removeLast();
            }
            String newBasePath = String.join(File.separator, list);
            targetAbsolutePath = Path.of(newBasePath);
        } else {
            targetAbsolutePath = Path.of(targetDir).toAbsolutePath();
        }
        if (Files.exists(targetAbsolutePath)) {
            currentDirectory = targetAbsolutePath;
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
