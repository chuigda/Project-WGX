package tech.icey.wgx;

import tech.icey.babel.BabelPlugin;

import java.util.List;

class ExamplePlugin implements BabelPlugin {
    @Override
    public String getName() {
        return "示例插件";
    }

    @Override
    public String getDescription() {
        return """
<html>
    <h2>这是一个示例插件</h2>
    <br />
    <p>这是一个示例插件，它不提供任何功能，只是用来演示插件系统的使用方法。这一行非常长，不过应该会被自动换行</p>
    <a href="https://www.youtube.com/watch?v=dQw4w9WgXcQ">Watch this video</a>
    
    <p>大烟杆嘴里塞，我只抽第五代</p>
    <p>不用打火真痛快</p>
    <p>byd 快尝尝，我现在肺痒痒</p>
    <p>你想抽芙蓉王？</p>
    <p>还不如抽喜之郎！</p>
    <p>抽~</p>
    <p>抽~</p>
    <p>抽~</p>
    <p>抽~</p>
    <p>我测试你的二维码~</p>
</html>
                    """;
    }

    @Override
    public List<Object> getComponents() {
        return List.of();
    }
}

public class Drill {
    public static void main(String[] args) {
        PluginManagement mgmt = new PluginManagement(List.of(new ExamplePlugin()));
        mgmt.setDefaultCloseOperation(PluginManagement.EXIT_ON_CLOSE);
        mgmt.setVisible(true);
    }
}
