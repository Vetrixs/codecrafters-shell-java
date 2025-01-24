import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws Exception {
        // Uncomment this block to pass the first stage

        Set<String> commands = new HashSet<>();
        commands.add("type");
        commands.add("exit");
        commands.add("echo");
        do {
            System.out.print("$ ");

            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();

            String[] input_array = input.split(" ", 2);

            if (input_array[0].equalsIgnoreCase("exit")) {
                System.exit(0);
            } else if (input_array[0].equalsIgnoreCase("echo")) {
                System.out.println(input_array[1]);
            } else if (input_array[0].equalsIgnoreCase("type")) {
                if (commands.contains(input_array[1])) {
                    System.out.printf("%s is a shell builtin%n", input_array[1]);
                } else {
                    System.out.printf("%s: command not found%n", input_array[1]);
                }
            } else {
                System.out.printf("%s: command not found%n", input_array[0]);
            }
        } while (true);
    }
}
