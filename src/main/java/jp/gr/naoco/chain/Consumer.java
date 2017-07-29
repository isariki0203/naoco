package jp.gr.naoco.chain;

import java.util.Iterator;

public abstract class Consumer implements ConsumerInterface {

    @Override
    public void execute(Iterator<Container> containers, ConsumerFilter filter) {
        init();
        try {
            while (containers.hasNext()) {
                Container container = containers.next();
                if (filter.accept(container)) {
                    execute_(container);
                }
            }
            finish();
        } catch (Throwable t) {
            error(t);
            throw t;
        } finally {
            finalize();
        }
    }

    public void init() {
        // nothing to do
    }

    public void finish() {
        // nothing to do
    }

    public void error(Throwable t) {
        // nothing to do
    }

    @Override
    public void finalize() {
        // nothing to do
    }

    public ConsumerFilter getDefaultFilter() {
        return ConsumerFilter.PASS_THROUGH_FILTER;
    }

    public abstract void execute_(Container container);
}
