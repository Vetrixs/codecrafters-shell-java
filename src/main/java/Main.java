import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        // Uncomment this block to pass the first stage
        do {
            System.out.print("$ ");

            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();

            String[] input_array = input.split(" ",2);
            if(input_array[0].toLowerCase().startsWith("exit")) {
                System.exit(0);
            } else if (input_array[0].contains("echo")) {
                System.out.println(input_array[1]);
            } else {
                System.out.printf("%s: command not found%n", input);
            }
        } while (true);
    }
}
