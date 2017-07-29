package jp.gr.naoco.chain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jp.gr.naoco.chain.queue.DirectProducerQueue;
import jp.gr.naoco.chain.queue.ParallelProducerQueue;
import jp.gr.naoco.chain.queue.ProducerQueue;
import jp.gr.naoco.chain.queue.SequentialProducerQueue;
import jp.gr.naoco.core.log.LaolLogger;

public class Chain {

    protected ArrayList<Broker> interruptBrokerList_;

    protected ArrayList<Broker> brokerList_;

    private Producer producer_;

    private ConsumerProducerInterface consumerProducer_;

    private Chain nextChain_;

    private int queueSize_ = MAX_QUEUE_SIZE;

    private static final int MAX_QUEUE_SIZE = 1000;

    private static final long WAIT_TIME_MINTUES = 120L;

    private static final Consumer TERMINATE_CONSUMER = new Consumer() {
        @Override
        public void execute_(Container container) {
            // nothing to do
        }
    };

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    public Chain(Producer producer) {
        producer_ = producer;
        brokerList_ = new ArrayList<Broker>();
    }

    public Chain(Producer producer, int queueSize) {
        this(producer);
        queueSize_ = queueSize;
    }

    private Chain(ConsumerProducerInterface consumerProducer, ArrayList<Broker> brokerList, int queueSize) {
        consumerProducer_ = consumerProducer;
        brokerList_ = brokerList;
        queueSize_ = queueSize;
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Methods

    public Chain direct(Consumer consumer) {
        return direct(consumer, consumer.getDefaultFilter());
    }

    public Chain direct(Consumer consumer, ConsumerFilter filter) {
        if (null != producer_) {
            brokerList_.add(new DirectBroker(producer_, consumer, filter));
        } else {
            brokerList_.add(new DirectBroker(consumerProducer_, consumer, filter));
        }
        interruptBrokerList_ = (ArrayList<Broker>) brokerList_.clone();
        return this;
    }

    public Chain direct(ConsumerProducer consumerProducer) {
        return direct(consumerProducer, consumerProducer.getDefaultFilter());
    }

    public Chain direct(ConsumerProducer consumerProducer, ConsumerFilter filter) {
        if (null != producer_) {
            brokerList_.add(new DirectBroker(producer_, consumerProducer, filter));
        } else {
            brokerList_.add(new DirectBroker(consumerProducer_, consumerProducer, filter));
        }
        nextChain_ = new Chain(consumerProducer, brokerList_, queueSize_);
        return nextChain_;
    }

    public Chain sequential(ConsumerInterface consumer) {
        if (consumer instanceof Consumer) {
            return sequential(consumer, ((Consumer) consumer).getDefaultFilter());
        } else {
            return sequential(consumer, ConsumerFilter.PASS_THROUGH_FILTER);
        }
    }

    public Chain sequential(ConsumerInterface consumer, ConsumerFilter filter) {
        if (null != producer_) {
            brokerList_.add(new SequentialBroker(producer_, consumer, filter));
        } else {
            brokerList_.add(new SequentialBroker(consumerProducer_, consumer, filter));
        }
        interruptBrokerList_ = (ArrayList<Broker>) brokerList_.clone();
        return this;
    }

    public Chain sequential(ConsumerProducerInterface consumerProducer) {
        if (consumerProducer instanceof ConsumerProducer) {
            return sequential(consumerProducer, ((ConsumerProducer) consumerProducer).getDefaultFilter());
        } else {
            return sequential(consumerProducer, ConsumerFilter.PASS_THROUGH_FILTER);
        }
    }

    public Chain sequential(ConsumerProducerInterface consumerProducer, ConsumerFilter filter) {
        if (null != producer_) {
            brokerList_.add(new SequentialBroker(producer_, consumerProducer, filter));
        } else {
            brokerList_.add(new SequentialBroker(consumerProducer_, consumerProducer, filter));
        }
        nextChain_ = new Chain(consumerProducer, brokerList_, queueSize_);
        return nextChain_;
    }

    public Chain parallel(ConsumerInterface consumer) {
        if (consumer instanceof Consumer) {
            return parallel(consumer, ((Consumer) consumer).getDefaultFilter());
        } else {
            return parallel(consumer, ConsumerFilter.PASS_THROUGH_FILTER);
        }
    }

    public Chain parallel(ConsumerInterface consumer, ConsumerFilter filter) {
        if (null != producer_) {
            brokerList_.add(new ParallelBroker(producer_, consumer, filter, queueSize_));
        } else {
            brokerList_.add(new ParallelBroker(consumerProducer_, consumer, filter, queueSize_));
        }
        interruptBrokerList_ = (ArrayList<Broker>) brokerList_.clone();
        return this;
    }

    public Chain parallel(ConsumerProducerInterface consumerProducer) {
        if (consumerProducer instanceof ConsumerProducer) {
            return parallel(consumerProducer, ((ConsumerProducer) consumerProducer).getDefaultFilter());
        } else {
            return parallel(consumerProducer, ConsumerFilter.PASS_THROUGH_FILTER);
        }
    }

    public Chain parallel(ConsumerProducerInterface consumerProducer, ConsumerFilter filter) {
        if (null != producer_) {
            brokerList_.add(new ParallelBroker(producer_, consumerProducer, filter, queueSize_));
        } else {
            brokerList_.add(new ParallelBroker(consumerProducer_, consumerProducer, filter, queueSize_));
        }
        nextChain_ = new Chain(consumerProducer, brokerList_, queueSize_);
        return nextChain_;
    }

    public Chain terminate() {
        return direct(TERMINATE_CONSUMER);
    }

    public void execute() {
        LOG.debug("start");
        this.executeWithoutJoin();
        this.join();
        LOG.debug("end");
    }

    public void executeWithoutJoin() {
        if (brokerList_.isEmpty()) {
            throw new IllegalStateException("Chain has no Consumer or ConsumerProducer.");
        }

        // 初期化はブローカリストの末尾から順に実行
        // （ブローカに次のブローカを渡す必要があるため）
        LOG.debug("init");
        int size = brokerList_.size();
        Broker next = null;
        for (int i = (size - 1); 0 <= i; i--) {
            Broker broker = brokerList_.get(i);
            broker.init(next, interruptBrokerList_);
            broker.setRootThread(Thread.currentThread());
            next = broker;
        }

        // スレッドチェーンのINFO出力
        StringBuilder builder = new StringBuilder();
        builder.append(Thread.currentThread().getName());
        for (Broker broker : brokerList_) {
            if (broker instanceof ParallelBroker) {
                builder.append(" => ");
                builder.append(((ParallelBroker) broker).consumerThread_.getName());
            }
        }
        LOG.info("Thread Chain:[" + this.getClass().getSimpleName() + "] " + builder.toString());

        // 先頭のブローカを実行
        LOG.debug("execute");
        brokerList_.get(0).execute(null, null);
    }

    public void finishProducer() {
        brokerList_.get(0).finishProducer();
    }

    public void join() {
        brokerList_.get(0).join(null);
    }

    protected Producer getProducer() {
        return producer_;
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Inner classes

    public static interface Broker {
        public void init(final Broker nextBroker, List<Broker> brokerList);

        public void execute(Iterator<Container> iterator, ConsumerFilter filter);

        public void join(Iterator<Container> iterator);

        public ProducerQueue getQueue();

        public void finishProducer();

        public void interrupt(Throwable t);

        public void setRootThread(Thread rootThread);

        public Producer getProducer();
    }

    private static class DirectBroker implements Broker {
        private Object producer_;
        private Object consumer_;
        private DirectProducerQueue queue_;
        private Broker nextBroker_;
        private List<Broker> brokerList_;
        private ConsumerFilter filter_;

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Constructor

        private DirectBroker(Producer producer, Consumer consumer, ConsumerFilter filter) {
            producer_ = producer;
            consumer_ = consumer;
            filter_ = filter;
        }

        private DirectBroker(Producer producer, ConsumerProducer consumer, ConsumerFilter filter) {
            producer_ = producer;
            consumer_ = consumer;
            filter_ = filter;
        }

        private DirectBroker(ConsumerProducerInterface producer, Consumer consumer, ConsumerFilter filter) {
            producer_ = producer;
            consumer_ = consumer;
            filter_ = filter;
        }

        private DirectBroker(ConsumerProducerInterface producer, ConsumerProducer consumer, ConsumerFilter filter) {
            producer_ = producer;
            consumer_ = consumer;
            filter_ = filter;
        }

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Methods

        @Override
        public void init(final Broker nextBroker, List<Broker> brokerList) {
            brokerList_ = brokerList;
            if (consumer_ instanceof ConsumerProducerInterface) {
                queue_ = new DirectProducerQueue((ConsumerProducer) consumer_, nextBroker.getQueue(), filter_);
            } else {
                queue_ = new DirectProducerQueue((Consumer) consumer_, filter_);
            }
            nextBroker_ = nextBroker;
        }

        @Override
        public void execute(Iterator<Container> iterator, ConsumerFilter prevFilter) {
            try {
                try {
                    // プロデューサの実行
                    if (producer_ instanceof Producer) {
                        ((Producer) producer_).execute(queue_);
                    } else if (null == iterator) {
                        // プロデューサがConsumerProducerで、引数nullの場合は、既にプロデューサは実行済みであるため、特に何もしない。
                        // キューのフィニッシュは、一個前のブローカが担当
                    } else {
                        ((ConsumerProducer) producer_).execute(iterator, queue_, prevFilter);
                    }
                } catch (Throwable t) {
                    interrupt(t);
                    throw t;
                }

                // コンシューマはプロデューサの実行からキュー経由で呼び出されているので、ここでは呼び出さない。

                // コンシューマがConsumerProducerの場合は次のブローカの呼出し
                if (consumer_ instanceof ConsumerProducerInterface) {
                    nextBroker_.execute(null, filter_);
                }
            } finally {
                // コンシューマの終了処理（プロデューサの処理と同期のため、ここで呼出す必要あり）
                if (consumer_ instanceof ConsumerProducer) {
                    ((ConsumerProducer) consumer_).finalize();
                } else if (consumer_ instanceof Consumer) {
                    ((Consumer) consumer_).finalize();
                }
            }
        }

        @Override
        public void join(Iterator<Container> iterator) {
            if (producer_ instanceof Producer) {
                if (producer_ instanceof JoinedProducer) {
                    ((JoinedProducer) producer_).join();
                }
                finishProducer();
            } else if (null == iterator) {
                // プロデューサがConsumerProducerで、引数nullの場合は、既にプロデューサは実行済みであるため、特に何もしない。
                // キューのフィニッシュは、一個前のブローカが担当
            } else {
                finishProducer();
            }
            if (consumer_ instanceof ConsumerProducerInterface) {
                nextBroker_.join(null);
            }
        }

        @Override
        public ProducerQueue getQueue() {
            return queue_;
        }

        @Override
        public void finishProducer() {
            queue_.finish();
            if (null != nextBroker_) {
                nextBroker_.finishProducer();
            }
        }

        @Override
        public void interrupt(Throwable t) {
            if (null == brokerList_) {
                return;
            }
            LOG.warn("start");
            for (Broker broker : brokerList_) {
                Producer producer = broker.getProducer();
                if ((null != producer) && (producer instanceof JoinedProducer)) {
                    JoinedProducer joined = (JoinedProducer) producer;
                    joined.reserveInterrupt(t);
                }
                if ((broker instanceof ParallelBroker)) {
                    ((ParallelBroker) broker).consumerThread_.interrupt();
                    ((ParallelBroker) broker).queue_.reserveInterrupt(t);
                }
            }
            LOG.warn("end");
        }

        @Override
        public void setRootThread(Thread rootThread) {
            // nothing to do
        }

        @Override
        public Producer getProducer() {
            if (producer_ instanceof Producer) {
                return (Producer) producer_;
            }
            return null;
        }
    }

    private static class SequentialBroker implements Broker {
        private Object producer_;
        private Object consumer_;
        private SequentialProducerQueue queue_;
        private Broker nextBroker_;
        private List<Broker> brokerList_;
        private ConsumerFilter filter_;

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Constructor

        private SequentialBroker(Producer producer, ConsumerInterface consumer, ConsumerFilter filter) {
            producer_ = producer;
            consumer_ = consumer;
            filter_ = filter;
        }

        private SequentialBroker(Producer producer, ConsumerProducerInterface consumer, ConsumerFilter filter) {
            producer_ = producer;
            consumer_ = consumer;
            filter_ = filter;
        }

        private SequentialBroker(ConsumerProducerInterface producer, ConsumerInterface consumer,
                ConsumerFilter filter) {
            producer_ = producer;
            consumer_ = consumer;
            filter_ = filter;
        }

        private SequentialBroker(ConsumerProducerInterface producer, ConsumerProducerInterface consumer,
                ConsumerFilter filter) {
            producer_ = producer;
            consumer_ = consumer;
            filter_ = filter;
        }

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Methods

        @Override
        public void init(final Broker nextBroker, List<Broker> brokerList) {
            brokerList_ = brokerList;
            queue_ = new SequentialProducerQueue();
            nextBroker_ = nextBroker;
        }

        @Override
        public void execute(Iterator<Container> iterator, ConsumerFilter prevFilter) {
            // プロデューサの実行
            try {
                if (producer_ instanceof Producer) {
                    ((Producer) producer_).execute(queue_);
                } else if (null == iterator) {
                    // プロデューサがConsumerProducerで、引数nullの場合は、既にプロデューサは実行済みであるため、特に何もしない。
                    // キューのフィニッシュは、一個前のブローカが担当
                } else {
                    ((ConsumerProducer) producer_).execute(iterator, queue_, prevFilter);
                }
            } catch (Throwable t) {
                interrupt(t);
                throw t;
            }

            // 次のブローカの呼出し（このブローカが最後の場合はコンシューマの呼出し）
            Iterator<Container> containers = queue_.consumerIterator();
            if (consumer_ instanceof ConsumerInterface) {
                try {
                    ((ConsumerInterface) consumer_).execute(containers, filter_);
                } catch (Throwable t) {
                    interrupt(t);
                    throw t;
                }
            } else {
                nextBroker_.execute(containers, filter_);
            }
        }

        @Override
        public void join(Iterator<Container> iterator) {
            if (producer_ instanceof Producer) {
                if (producer_ instanceof JoinedProducer) {
                    ((JoinedProducer) producer_).join();
                } else {
                    finishProducer();
                }
            } else if (null == iterator) {
                // プロデューサがConsumerProducerで、引数nullの場合は、既にプロデューサは実行済みであるため、特に何もしない。
                // キューのフィニッシュは、一個前のブローカが担当
            } else {
                finishProducer();
            }

            if (!(consumer_ instanceof ConsumerInterface)) {
                nextBroker_.join(iterator);
            }
        }

        @Override
        public ProducerQueue getQueue() {
            return queue_;
        }

        @Override
        public void finishProducer() {
            queue_.finish();
        }

        @Override
        public void interrupt(Throwable t) {
            LOG.warn("start");
            for (Broker broker : brokerList_) {
                if ((broker instanceof ParallelBroker)) {
                    ((ParallelBroker) broker).consumerThread_.interrupt();
                    ((ParallelBroker) broker).queue_.reserveInterrupt();
                }
            }
            LOG.warn("end");
        }

        @Override
        public void setRootThread(Thread rootThread) {
            // nothing to do
        }

        @Override
        public Producer getProducer() {
            if (producer_ instanceof Producer) {
                return (Producer) producer_;
            }
            return null;
        }
    }

    private static class ParallelBroker implements Broker {
        protected ParallelProducerQueue queue_;
        protected Thread consumerThread_;
        private Object producer_;
        private Object consumer_;
        private Broker nextBroker_;
        private List<Broker> brokerList_;
        private Thread rootThread_;
        private ConsumerFilter filter_;
        private int queueSize_;

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Constructor

        private ParallelBroker() {
            // nothing to do
        }

        private ParallelBroker(Producer producer, ConsumerInterface consumer, ConsumerFilter filter, int queueSize) {
            producer_ = producer;
            consumer_ = consumer;
            filter_ = filter;
            queueSize_ = queueSize;
        }

        private ParallelBroker(Producer producer, ConsumerProducerInterface consumer, ConsumerFilter filter,
                int queueSize) {
            producer_ = producer;
            consumer_ = consumer;
            filter_ = filter;
            queueSize_ = queueSize;
        }

        private ParallelBroker(ConsumerProducerInterface producer, ConsumerInterface consumer, ConsumerFilter filter,
                int queueSize) {
            producer_ = producer;
            consumer_ = consumer;
            filter_ = filter;
            queueSize_ = queueSize;
        }

        private ParallelBroker(ConsumerProducerInterface producer, ConsumerProducerInterface consumer,
                ConsumerFilter filter, int queueSize) {
            producer_ = producer;
            consumer_ = consumer;
            filter_ = filter;
            queueSize_ = queueSize;
        }

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Methods

        @Override
        public void init(final Broker nextBroker, List<Broker> brokerList) {
            brokerList_ = brokerList;
            queue_ = new ParallelProducerQueue(queueSize_, WAIT_TIME_MINTUES);
            nextBroker_ = nextBroker;
            final Iterator<Container> iterator = queue_.consumerIterator();

            // コンシューマ実行スレッドの準備
            consumerThread_ = new Thread(new Runnable() {
                @Override
                public void run() {
                    LOG.info("thread start " + consumer_.getClass().getName());
                    try {
                        if (consumer_ instanceof ConsumerInterface) {
                            try {
                                ((ConsumerInterface) consumer_).execute(iterator, filter_);
                            } catch (Throwable t) {
                                interrupt(t);
                                throw t;
                            }
                        } else {
                            nextBroker_.execute(iterator, filter_);
                        }
                        nextBroker_.join(iterator);
                    } catch (Throwable t) {
                        LOG.fatal(t.getMessage(), t);
                        throw t;
                    }
                    LOG.info("thread exit");
                }
            });

            // コンシューマスレッドの起動
            consumerThread_.start();
        }

        @Override
        public void execute(Iterator<Container> iterator, ConsumerFilter prevFilter) {
            // プロデューサの実行
            try {
                if (producer_ instanceof Producer) {
                    ((Producer) producer_).execute(queue_);
                } else if (null == iterator) {
                    // プロデューサがConsumerProducerで、引数nullの場合は、既にプロデューサは実行済みであるため、特に何もしない。
                } else {
                    ((ConsumerProducerInterface) producer_).execute(iterator, queue_, prevFilter);
                }
            } catch (Throwable t) {
                LOG.error(t.getMessage(), t);
                consumerThread_.interrupt();
                queue_.reserveInterrupt();
                interrupt(t);
                throw t;
            }
        }

        @Override
        public void join(Iterator<Container> iterator) {
            if (producer_ instanceof Producer) {
                if (producer_ instanceof JoinedProducer) {
                    ((JoinedProducer) producer_).join();
                }
                finishProducer();
            } else if (null == iterator) {
                // プロデューサがConsumerProducerで、引数nullの場合は、既にプロデューサは実行済みであるため、特に何もしない。
            } else {
                finishProducer();
            }

            // コンシューマ実行スレッドの終了待機
            try {
                consumerThread_.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public ProducerQueue getQueue() {
            return queue_;
        }

        @Override
        public void finishProducer() {
            queue_.finish();
        }

        @Override
        public void interrupt(Throwable t) {
            LOG.warn("start");
            for (Broker broker : brokerList_) {
                if ((broker instanceof ParallelBroker) && !broker.equals(this)) {
                    ((ParallelBroker) broker).consumerThread_.interrupt();
                    ((ParallelBroker) broker).queue_.reserveInterrupt(t);
                }
            }
            rootThread_.interrupt();
            LOG.warn("end");
        }

        @Override
        public void setRootThread(Thread rootThread) {
            rootThread_ = rootThread;
        }

        @Override
        public Producer getProducer() {
            if (producer_ instanceof Producer) {
                return (Producer) producer_;
            }
            return null;
        }
    }

    protected static class ExternalParallelBroker extends ParallelBroker {

        private Broker nextBroker_;

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Constructor

        public ExternalParallelBroker(Thread consumerThread, ParallelProducerQueue queue) {
            consumerThread_ = consumerThread;
            queue_ = queue;
        }

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Methods

        @Override
        public void init(final Broker nextBroker, List<Broker> brokerList) {
            nextBroker_ = nextBroker;
        }

        @Override
        public void execute(Iterator<Container> iterator, ConsumerFilter prevFilter) {
            nextBroker_.execute(iterator, prevFilter);
        }
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Logger

    private static final LaolLogger LOG = new LaolLogger(Chain.class.getName());
}
