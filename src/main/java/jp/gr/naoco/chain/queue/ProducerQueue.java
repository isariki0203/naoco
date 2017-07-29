package jp.gr.naoco.chain.queue;

import java.util.Iterator;

import jp.gr.naoco.chain.Container;

public interface ProducerQueue {

    public void offer(Container obj);

    public void finish();

    Iterator<Container> consumerIterator();
}
