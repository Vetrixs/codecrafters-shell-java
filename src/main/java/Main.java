import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws Exception {
        // Uncomment this block to pass the first stage

        Set<String> buildInCommands = new HashSet<>();
        buildInCommands.add("type");
        buildInCommands.add("exit");
        buildInCommands.add("echo");

        String path = System.getenv("PATH");


        do {
            System.out.print("$ ");

            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();

            String[] input_array = input.split(" ", 2);


            switch (input_array[0]) {
                case "exit":
                    System.exit(0);
                case "echo":
                    System.out.println(input_array[1]);
                    break;
                case "type":
                    if (buildInCommands.contains(input_array[1])) {
                        System.out.printf("%s is a shell builtin%n", input_array[1]);
                    }
                    Path executable = searchInPath(path, input_array[0]);
                    if(executable != null) {
                        System.out.printf("%s is %s%n", input_array[1], executable);
                    }
                    {
                        System.out.printf("%s: not found%n", input_array[1]);
                    }

                    break;
            }
        } while (true);
    }

    public static Path searchInPath(String pathEnv, String command) throws IOException {
        if (pathEnv == null) {
            return null;
        }
        String[] paths = pathEnv.split(":");
        for (String path : paths) {
            Optional<Path> first = Files.walk(new File(path).toPath())
                    .filter(Files::isExecutable)
                    .filter(file -> file.getFileName().toString().equalsIgnoreCase(command))
                    .findFirst();

            if (first.isPresent()) {
                return first.get();
            }
        }
        return null;
    }
}
