package jp.gr.naoco.chain.queue;

import java.util.Iterator;

import jp.gr.naoco.chain.Consumer;
import jp.gr.naoco.chain.ConsumerFilter;
import jp.gr.naoco.chain.ConsumerProducer;
import jp.gr.naoco.chain.Container;

public class DirectProducerQueue implements ProducerQueue {
    private final Object instance_;

    private ProducerQueue queue_ = null;

    private ConsumerFilter filter_;

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    public DirectProducerQueue(Consumer consumer, ConsumerFilter filter) {
        instance_ = consumer;
        filter_ = filter;
    }

    public DirectProducerQueue(ConsumerProducer consumerProducer, ProducerQueue queue, ConsumerFilter filter) {
        instance_ = consumerProducer;
        queue_ = queue;
        filter_ = filter;
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Methods

    @Override
    public void offer(Container obj) {
        if (null == queue_) {
            if (filter_.accept(obj)) {
                Consumer c = (Consumer) instance_;
                try {
                    c.init();
                    c.execute_(obj);
                    c.finish();
                } catch (Throwable t) {
                    c.error(t);
                    throw t;
                }
            }
        } else {
            if (filter_.accept(obj)) {
                ConsumerProducer cp = (ConsumerProducer) instance_;
                try {
                    cp.init();
                    cp.execute_(obj, queue_);
                    cp.finish();
                } catch (Throwable t) {
                    cp.error(t);
                    throw t;
                }
            } else {
                // フィルタで実行対象外と判定した場合は、queue_のofferを呼出して次に進める。
                queue_.offer(obj);
            }
        }
    }

    @Override
    public void finish() {
        // nothing to do
    }

    @Override
    public Iterator<Container> consumerIterator() {
        throw new UnsupportedOperationException();
    }

}
