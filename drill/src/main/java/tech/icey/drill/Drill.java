package tech.icey.drill;

import tech.icey.rfc6455.Client;
import tech.icey.rfc6455.Connection;
import tech.icey.rfc6455.RFC6455Callback;
import tech.icey.util.Either;
import tech.icey.util.Optional;

import java.io.IOException;
import java.util.Objects;

public class Drill {
    public static void main(String[] args) {
        try (Connection websocketConnection = Client.connect("127.0.0.1", 6455, "/", Optional.some(new RFC6455Callback() {
            @Override
            public void onData(Either<byte[], String> data) {
                System.out.println(Objects.requireNonNull(data).right());
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        }))) {
            websocketConnection.write("告诉你一个生活开心的秘诀：少责备自己，多辱骂别人。被你骂的人都罪有应得！但别人骂你肯定是没素质");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
