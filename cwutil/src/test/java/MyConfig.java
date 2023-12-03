import tech.icey.util.IniField;

public class MyConfig {
    private @IniField int intValue;
    private @IniField String stringValue;
    private @IniField boolean booleanValue;

    public int getIntValue() {
        return intValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public boolean getBooleanValue() {
        return booleanValue;
    }
}
