import java.util.Arrays;

public class ControllerTest {

    public static void main(String[] args) {

        String input = "echo 'shell hello' 'my   hello'";
        Controller controller = new Controller();

        String[] strings = controller.cleanInput(input);

        System.out.println(Arrays.toString(strings));
    }

}
