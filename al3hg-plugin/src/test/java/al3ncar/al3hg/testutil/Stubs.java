package al3ncar.al3hg.testutil;

import java.lang.reflect.Proxy;

/** Dublês via java.lang.reflect.Proxy (sem Mockito/MockBukkit). Métodos não tratados devolvem valores neutros. */
public final class Stubs {

    /** Devolva isto de um {@link Answer} para usar o comportamento neutro padrão. */
    public static final Object PASS = new Object();

    @FunctionalInterface
    public interface Answer {
        Object apply(String method, Object[] args);
    }

    private Stubs() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T stub(Class<T> type, Answer answer) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (proxy, m, args) -> {
            Object result = answer.apply(m.getName(), args == null ? new Object[0] : args);
            if (result != PASS) {
                return result;
            }
            return switch (m.getName()) {
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == args[0];
                case "toString" -> type.getSimpleName() + "Stub";
                default -> neutral(m.getReturnType());
            };
        });
    }

    private static Object neutral(Class<?> t) {
        if (t == boolean.class) return false;
        if (t == int.class) return 0;
        if (t == long.class) return 0L;
        if (t == double.class) return 0d;
        if (t == float.class) return 0f;
        return null;
    }
}
