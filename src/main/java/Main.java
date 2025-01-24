import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        // Uncomment this block to pass the first stage
        do {
            System.out.print("$ ");

            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();


            if(input.toLowerCase().startsWith("exit")) {
                System.exit(0);
            }
            System.out.printf("%s: command not found%n", input);
        } while (true);
    }
}
