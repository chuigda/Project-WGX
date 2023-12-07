package tech.icey.wgx;

import tech.icey.util.Pair;
import tech.icey.wgx.babel.*;
import tech.icey.wgx.ui.PluginWindow;

import java.util.List;

class Component1 implements UIProvider, DataConsumer {
    @Override
    public void initialise(Masterpiece masterpiece) {}

    @Override
    public void consume(Masterpiece masterpiece) {}

    @Override
    public List<Pair<String, UIComponent>> provide() {
        return List.of();
    }
}

class Component2 implements DataManipulator, DataPublisher, DataConsumer {
    @Override
    public void consume(Masterpiece masterpiece) {

    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public void initialise(Masterpiece masterpiece) {

    }

    @Override
    public void publish() {

    }

    @Override
    public void manipulate(Masterpiece masterpiece) {

    }
}

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
        return List.of(
                new Component1(),
                new Component2()
        );
    }
}

public class Drill {
    public static void main(String[] args) {
        ExamplePlugin p = new ExamplePlugin();

        PluginWindow mgmt = new PluginWindow(List.of(p), List.of(p.getComponents()));
        mgmt.setDefaultCloseOperation(PluginWindow.EXIT_ON_CLOSE);
        mgmt.setVisible(true);
    }
}
