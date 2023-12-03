import tech.icey.util.IniParser;

public class TestMain {
    public static void main(String[] args) {
        String ini = """
intValue = 1
stringValue = hello
booleanValue = true
""";
        var parseResult = IniParser.parse(ini);
        var iniContent = parseResult.first();
        var parseErrors = parseResult.second();
        assert parseErrors.isEmpty();

        var config = IniParser.deserialise(MyConfig.class, iniContent);
        assert config.getIntValue() == 1;
        assert config.getStringValue().equals("hello");
        assert config.getBooleanValue();
    }
}
