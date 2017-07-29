package jp.gr.naoco.chain.fork;

import java.util.Iterator;

import jp.gr.naoco.chain.ConsumerFilter;
import jp.gr.naoco.chain.Container;
import jp.gr.naoco.chain.JoinedProducer;
import jp.gr.naoco.chain.queue.ParallelProducerQueue;
import jp.gr.naoco.chain.queue.ProducerQueue;
import jp.gr.naoco.core.log.LaolLogger;

public class ForkLegProducer implements JoinedProducer {

    private final ParallelProducerQueue queue_ = new ParallelProducerQueue(100, Integer.MAX_VALUE);

    private ConsumerFilter filter_;

    private Thread thread_ = null;

    private volatile Throwable inThreadError_ = null;

    // ////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    protected ForkLegProducer(ConsumerFilter filter) {
        filter_ = filter;
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////
    // Methods

    @Override
    public void execute(ProducerQueue queue) {
        final ProducerQueue nextQueue = queue;
        try {
            final Iterator<Container> containeres = queue_.consumerIterator();
            Runnable runner = new Runnable() {
                @Override
                public void run() {
                    LOG.info("thread start ForkLegProducer");
                    while (containeres.hasNext()) {
                        nextQueue.offer(containeres.next());
                    }
                    nextQueue.finish();
                    LOG.info("thread exit");
                }
            };
            thread_ = new Thread(runner);
            thread_.start();
        } catch (Throwable t) {
            inThreadError_ = t;
            LOG.fatal(t.getMessage(), t);
            throw t;
        }
    }

    @Override
    public void join() {
        try {
            thread_.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void reserveInterrupt(Throwable t) {
        queue_.reserveInterrupt(t);
        if (null != thread_) {
            thread_.interrupt();
        }
        if (null != inThreadError_) {
            throw new RuntimeException(inThreadError_);
        }
    }

    // ////////////////////////////////

    protected void connect(Container container) {
        if (filter_.accept(container)) {
            queue_.offer(container);
        }
        if (null != inThreadError_) {
            throw new RuntimeException(inThreadError_);
        }
    }

    protected void finish() {
        queue_.finish();
        if (null != inThreadError_) {
            throw new RuntimeException(inThreadError_);
        }
    }

    // /////////////////////////////////

    ParallelProducerQueue getQueue() {
        return queue_;
    }

    Thread getThread() {
        return thread_;
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Logger

    private static final LaolLogger LOG = new LaolLogger(ForkLegProducer.class.getName());

}
