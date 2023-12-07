import tech.icey.util.Either;

public class TestMain {
    public static void main(String[] args) {
        class ClassA {
            @Override
            public String toString() {
                return "已知 A 班今天有若干人想上";
            }
        }
        class ClassB {
            @Override
            public String toString() {
                return "问今天这个 B 班有多少人想上";
            }
        }

        Either<ClassA, ClassB> e1 = Either.left(new ClassA());
        Either<ClassA, ClassB> e2 = Either.right(new ClassB());

        System.err.println(e1);
        System.err.println(e2);
    }
}
