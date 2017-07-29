package jp.gr.naoco.chain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jp.gr.naoco.chain.queue.ProducerQueue;

public abstract class BufferedConsumerProducer extends ConsumerProducer {
    protected int bufferSize_ = 1000;

    private List<Container> buffer_ = new ArrayList<Container>();

    @Override
    public void execute(Iterator<Container> containers, ProducerQueue queue, ConsumerFilter filter) {
        init();
        try {
            while (containers.hasNext()) {
                Container container = containers.next();
                if (!filter.accept(container)) {
                    queue.offer(container);
                    continue;
                }
                buffer_.add(container);
                if (bufferSize_ < buffer_.size()) {
                    doOffer(queue, filter);
                }
            }
            doOffer(queue, filter);
            finish();
        } catch (Throwable t) {
            error(t);
            throw t;
        } finally {
            finalize();
        }
    }

    private void doOffer(ProducerQueue queue, ConsumerFilter filter) {
        for (Container container : buffer_) {
            execute_(container, queue);
        }
        buffer_.clear();
    }
}
