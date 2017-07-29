package jp.gr.naoco.chain;

import java.util.Random;

import org.apache.log4j.Logger;

import jp.gr.naoco.chain.Chain;
import jp.gr.naoco.chain.Consumer;
import jp.gr.naoco.chain.ConsumerInterface;
import jp.gr.naoco.chain.Container;
import jp.gr.naoco.chain.Factory;
import jp.gr.naoco.chain.Producer;
import jp.gr.naoco.chain.queue.ProducerQueue;
import jp.gr.naoco.core.NaocoCoreInitializer;
import jp.gr.naoco.core.annotation.Transaction;

public class SampleBootstrap03 {
    public static void main(String[] args) throws Exception {
        // naocoの初期化
        NaocoCoreInitializer.initialize("conf.laol01", new OriginalLogAdaptor());
        Producer producer = Factory.producer(SampleProducer.class);
        ConsumerInterface consumer = Factory.consumer(SampleConsumer.class);

        log.debug("***************** parallel execute start");
        Chain chain = new Chain(producer, 10) //
                .parallel(consumer);
        chain.execute();
        log.debug("***************** parallel execute end");
        log.debug("");

        log.debug("***************** direct execute start");
        chain = new Chain(producer) //
                .direct(new SampleConsumer());
        chain.execute();
        log.debug("***************** direct execute end");
        log.debug("");

        log.debug("***************** sequential execute start");
        chain = new Chain(producer) //
                .sequential(consumer);
        chain.execute();
        log.debug("***************** sequential execute end");
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Inner classes

    @Transaction(lookupName = "java:comp/env/jdbc/test")
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

    @Transaction(lookupName = "java:comp/env/jdbc/test")
    public static class SampleConsumer extends Consumer {
        private int counter_ = 0;
        private Random random = new Random();

        @Override
        public void execute_(Container container) {
            String value = (String) container.get();
            log.debug("consumer[" + counter_ + "]:" + value);
            counter_++;
            try {
                Thread.sleep((long) (random.nextDouble() * 130));
            } catch (InterruptedException e) {

            }
        }
    }

    public static final Logger log = Logger.getRootLogger();
}
