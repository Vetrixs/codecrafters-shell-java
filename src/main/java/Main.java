import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {

        Controller controller = new Controller();

        do {
            System.out.print("$ ");

            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();

            controller.execute(input);
        } while (true);
    }
}