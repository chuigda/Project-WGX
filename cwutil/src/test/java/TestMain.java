import tech.icey.util.IniParser;

public class TestMain {
    public static void main(String[] args) {
        String ini = """
intValue = 1
stringValue = hello
booleanValue = true
""";
        var parseResult = IniParser.parse(ini);
        var iniContent = parseResult.first;
        assert parseResult.second.isEmpty();

        var deserialiseResult = IniParser.deserialise(MyConfig.class, iniContent);
        assert deserialiseResult.second.isEmpty();

        var config = deserialiseResult.first;
        assert config.getIntValue() == 1;
        assert config.getStringValue().equals("hello");
        assert config.getBooleanValue();
    }
}
