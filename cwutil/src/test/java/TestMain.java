import tech.icey.rfc6455.Client;
import tech.icey.rfc6455.Connection;

public class TestMain {
    public static void main(String[] args) {
        try (Connection connection = Client.connect("127.0.0.1", 6455, "/")) {
            connection.write("我测试你的二维码");
            var readData = connection.read();
            assert readData != null;
            assert readData.isRight();

            var right = readData.right();
            assert right != null;
            System.err.println("read data: " + right);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
