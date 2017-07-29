package jp.gr.naoco.chain;

import java.util.Iterator;

import jp.gr.naoco.chain.queue.ProducerQueue;

public interface ConsumerProducerInterface {
    public void execute(Iterator<Container> containers, ProducerQueue queue, ConsumerFilter filter);
}
