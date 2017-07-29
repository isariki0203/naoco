package jp.gr.naoco.chain.queue;

import java.util.Iterator;
import java.util.LinkedList;

import jp.gr.naoco.chain.Container;

public class SequentialProducerQueue implements ProducerQueue {

    private final LinkedList<Container> queue_ = new LinkedList<Container>();

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    public SequentialProducerQueue() {
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Methods

    @Override
    public void offer(Container obj) {
        queue_.offer(obj);
    }

    @Override
    public void finish() {
        // nothing to do
    }

    @Override
    public Iterator<Container> consumerIterator() {
        return queue_.iterator();
    }
}
