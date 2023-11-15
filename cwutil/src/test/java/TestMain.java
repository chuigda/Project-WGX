import tech.icey.rfc6455.Client;
import tech.icey.rfc6455.Connection;

public class TestMain {
    public static void main(String[] args) {
        try (Connection connection = Client.connect("127.0.0.1", 6455, "/")) {
            connection.write("wcsndm");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
