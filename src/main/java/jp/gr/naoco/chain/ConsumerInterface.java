package jp.gr.naoco.chain;

import java.util.Iterator;

public interface ConsumerInterface {
    public void execute(Iterator<Container> containers, ConsumerFilter filter);
}
