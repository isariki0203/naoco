package jp.gr.naoco.chain;

public class Container {
    private final Object object_;

    public Container(Object object) {
        object_ = object;
    }

    public Object get() {
        return object_;
    }
}
