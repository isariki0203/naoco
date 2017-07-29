package jp.gr.naoco.chain;

import java.util.Random;

import org.apache.log4j.Logger;

import jp.gr.naoco.chain.Chain;
import jp.gr.naoco.chain.Consumer;
import jp.gr.naoco.chain.ConsumerProducer;
import jp.gr.naoco.chain.Container;
import jp.gr.naoco.chain.Producer;
import jp.gr.naoco.chain.queue.ProducerQueue;
import jp.gr.naoco.core.NaocoCoreInitializer;

public class SampleBootstrap02 {

    public static void main(String[] args) throws Exception {
        // naocoの初期化
        NaocoCoreInitializer.initialize("conf.laol01", new OriginalLogAdaptor());

        Counter producerCounter = new Counter();
        EndPointCounter consumerCounter = new EndPointCounter();
        Chain chain = new Chain(new SampleProducer(), 10) //
                .direct(producerCounter) //
                .parallel(new SampleConsumerProducer()) //
                .direct(consumerCounter);
        chain.execute();

        log.debug("producer couter result:" + producerCounter.getCount());
        log.debug("consumer couter result:" + consumerCounter.getCount());
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Inner classes

    public static class SampleProducer implements Producer {

        @Override
        public void execute(ProducerQueue queue) {
            Random random = new Random();
            for (int i = 0; i < 30; i++) {
                String value = Long.toString(random.nextLong());
                log.debug("producer[" + i + "]:" + value);
                queue.offer(new Container(value));
                try {
                    Thread.sleep((long) (random.nextDouble() * 100));
                } catch (InterruptedException e) {

                }
            }
        }
    }

    public static class SampleConsumerProducer extends ConsumerProducer {
        private int counter_ = 0;
        private Random random = new Random();

        @Override
        public void execute_(Container container, ProducerQueue queue) {
            String value = (String) container.get();
            log.debug("consumer[" + counter_ + "]:" + value);
            counter_++;
            queue.offer(new Container(null));
            try {
                Thread.sleep((long) (random.nextDouble() * 130));
            } catch (InterruptedException e) {

            }
        }
    }

    public static class Counter extends ConsumerProducer {
        private int counter_ = 0;

        @Override
        public void execute_(Container container, ProducerQueue queue) {
            counter_++;
            queue.offer(container);
        }

        public int getCount() {
            return counter_;
        }
    }

    public static class EndPointCounter extends Consumer {
        private int counter_ = 0;

        @Override
        public void execute_(Container container) {
            counter_++;
        }

        public int getCount() {
            return counter_;
        }
    }

    public static final Logger log = Logger.getRootLogger();
}
