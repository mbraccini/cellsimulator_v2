package network;

import interfaces.network.Context;
import interfaces.network.Table;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ContextImpl<K,V> implements Context<K,V> {

    protected Map<Integer,Table<K,V>> m;
    private ContextImpl(Map<Integer,Table<K,V>> m){
        this.m = m;
    }

    @Override
    public Map<Integer, Table<K, V>> context() {
        return m;
    }

    @Override
    public String toString() {
        return "Ctx{\n" +
                     m      .entrySet()
                            .stream()
                             .map(entry -> "Id: " + entry.getKey() + " - " + entry.getValue())
                             .collect(Collectors.joining("\n")) +
                "\n}";
    }

    public static <K,V> ContextBuilder<K,V> builder(){
        return new ContextBuilder<>();
    }

    public static class ContextBuilder<K,V> {

        private Map<Integer,Table<K,V>> m = new HashMap<>();

        public ContextBuilder<K,V> add(Integer a, Table<K,V> b) {
            m.put(a,b);
            return this;
        }

        public Context<K,V> build() {
            return new ContextImpl<>(m);
        }

    }
}
