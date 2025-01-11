package chr.wgx.drill;

import chr.wgx.builtin.core.data.CoreData;

public final class Drill {
    public static void main(String[] args) {
        CoreData cd = new CoreData();
        CoreData cd1 = cd.clone();

        System.err.println("OK");
    }
}
