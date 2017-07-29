package jp.gr.naoco.chain;

import java.util.ArrayList;
import java.util.Iterator;

import jp.gr.naoco.chain.queue.ProducerQueue;

public class ChainElementConverter {

    private static final ProducerQueue DUMMY_QUEUE = new ProducerQueue() {
        @Override
        public void offer(Container obj) {
            // nothing to do
        }

        @Override
        public void finish() {
            // nothing to do
        }

        @Override
        public Iterator<Container> consumerIterator() {
            // nothing to do
            return new ArrayList<Container>(0).iterator();
        }
    };

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    private ChainElementConverter() {
        // nothing to do
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Methods

    /**
     * Producer から ConsumerProducer を生成する。
     * <p>
     * 生成したConsumerProducerは、前段処理のOfferの回数が1件以上の場合、
     * Containerを無視して一回だけProducerを実行し、キューの先頭にofferをする。
     * </p>
     * <p>
     * 指定したProducerが複数のContainerをofferする場合、Producerのofferがすべて
     * 終了したのちに、前段処理から送られたContainerをそのままofferして次の処理に引き渡す。
     * </p>
     * 
     * @param producer
     * @return
     */
    public static ConsumerProducer producer2ConsumerProducer(final Producer producer) {
        return new ConsumerProducer() {
            private Executer executer_ = new Executer() {
                @Override
                public void execute(ProducerQueue queue) {
                    producer.execute(queue);
                }
            };

            @Override
            public void execute_(Container container, ProducerQueue queue) {
                executer_.execute(queue);
                queue.offer(container);
                executer_ = Executer.NO_EXECUTER;
            }
        };
    }

    /**
     * ConsumerProducerからConsumerを生成する。
     * <p>
     * ConsumerProducerからContainerの受け取りのみを行い、Queueへのofferを無視するConsumerを生成する。
     * </p>
     * 
     * @param consumerProducer
     * @return
     */
    public static ConsumerInterface consumerProducer2Consumer(final ConsumerProducerInterface consumerProducer) {
        return new ConsumerInterface() {
            @Override
            public void execute(Iterator<Container> containers, ConsumerFilter filter) {
                consumerProducer.execute(containers, DUMMY_QUEUE, filter);
            }
        };
    }

    /**
     * ConsumerProducerからConsumerを生成する。
     * <p>
     * ConsumerProducerからContainerの受け取りのみを行い、Queueへのofferを無視するConsumerを生成する。
     * </p>
     * 
     * @param consumerProducer
     * @return
     */
    public static Consumer consumerProducer2Consumer(final ConsumerProducer consumerProducer) {
        return new Consumer() {
            @Override
            public void execute_(Container container) {
                consumerProducer.execute_(container, DUMMY_QUEUE);
            }
        };
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Interface

    private static interface Executer {
        public void execute(ProducerQueue queue);

        public static final Executer NO_EXECUTER = new Executer() {
            @Override
            public void execute(ProducerQueue queue) {
                // nothing to do
            }
        };

    }
}
