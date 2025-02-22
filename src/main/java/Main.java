import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {

        Controller controller = new Controller();

        do {
            System.out.print("$ ");

            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();

            String[] input_array = input.split(" ");
            controller.execute(input_array);
        } while (true);
    }
}