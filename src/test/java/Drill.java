import chr.wgx.Config;
import com.google.gson.Gson;

public final class Drill {
    public static void main(String[] args) {
        var config = new Config();

        Gson gson = new Gson();
        try {
            String configText = config.toPrettyJSON();
            System.out.println(configText);

            var newConfig = gson.fromJson(configText, Config.class);
            System.out.println(newConfig.toPrettyJSON());

            assert newConfig.toPrettyJSON().equals(configText);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
