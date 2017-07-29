package jp.gr.naoco.chain.queue;

import java.util.Iterator;
import java.util.LinkedList;

import jp.gr.naoco.chain.Container;
import jp.gr.naoco.core.log.LaolLogger;

/**
 * Producer-Consumerマルチスレッドパターンのキュー実装
 * <p>
 * 本クラスは、ProducerとConsumerがそれぞれ1スレッドのみの場合を想定して、パフォーマンスが最大となる設計となっている。</br>
 * ProducerあるいはConsumerが複数スレッドある場合は、ブロック機能が正しく動作せず、RuntimeExceptionが発生する可能性がある。
 * </p>
 * <p>
 * Producer側が本クラスのオブジェクトに要素をofferメソッドで設定し、要素の設定が完了したらfinishメソッドを呼び出して、 処理の完了をConsumer側に通知する。<br/>
 * 内部のキューがキューの最大要素数を超えた場合、offerメソッドはキューの要素が最大要素数の半分以下に減るまで、あるいは待機時間まで処理を待機する。
 * </p>
 * </p>
 * <p>
 * Consumer側は本クラスのオブジェクトのconsumerIteratorメソッドを呼び出してIteratorを取得し、hasNextがtrueを返却する間、 nextメソッドで要素を受け取る。<br/>
 * Producer側がfinishメソッドを呼び出していないのに、内部のキューが空になった場合、hasNextメソッドはキューに要素が最大要素数の半分以上入るまで、あるいは待機時間まで、処理を待機する。
 * </p>
 * <p>
 * キューの最大要素数を少なく指定すると、ProducerとConsumerの待機が頻繁に発生し、パフォーマンスが悪化する。実行環境のヒープ容量とアプリ全体のヒープ使用量から、許す限り大きい値を設定るすべきである。
 * </p>
 * <p>
 * 指定した待機時間中にProducerとConsumerのいずれも、100回のリトライを行いキューの要素数が最大要素数の半分を過ぎれば処理を再開する。<br/>
 * 待機時間の指定は分単位であるが、ProducerとConsumerが1要素の処理に費やす最大時間の100倍よりかなり大きいの値を指定するのが望ましい。
 * </p>
 */
public class ParallelProducerQueue implements ProducerQueue {

    private volatile LinkedList<Container> queue_ = new LinkedList<Container>();

    private volatile Blocker producerBlocker_;

    private volatile Blocker consumerBlocker_;

    private final int maxQueueSize_;

    private final int notifyLine_;

    private volatile boolean isFinished_ = false;

    private volatile boolean reserveInterrupt_ = false;

    private Throwable interruptCause_ = null;

    private static final int EMERGENCY_WAIT_TIME = 300;

    private static final int RETRY_COUNT = 100;

    private String producerThreadName_ = "";
    private String consumerThreadName_ = "";

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    /**
     * コンストラクタ
     * 新しく {@link ParallelProducerQueue} オブジェクトを構築します。
     * 
     * @param maxQueueSize キューの最大要素数（10以上を設定すること）
     * @param waitTimeMinutes 最大待機時間（分）
     */
    public ParallelProducerQueue(int maxQueueSize, long waitTimeMinutes) {
        maxQueueSize_ = (maxQueueSize < 10 ? 10 : maxQueueSize);
        notifyLine_ = (maxQueueSize_ / 2) + 1;
        if (waitTimeMinutes < 1) {
            waitTimeMinutes = 1;
        }
        long waitTime = ((waitTimeMinutes * 60 * 1000) / RETRY_COUNT);
        producerBlocker_ = new Blocker(waitTime);
        consumerBlocker_ = new Blocker(waitTime);
        producerBlocker_.setOpposite(consumerBlocker_);
        consumerBlocker_.setOpposite(producerBlocker_);
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Methods

    @Override
    public void offer(Container obj) {
        producerThreadName_ = Thread.currentThread().getName();
        interrupted();
        BLOCK_PRODUCER: if (maxQueueSize_ < queue_.size()) {
            try {
                producerBlocker_.openEmergency();
                if (queue_.size() <= notifyLine_) {
                    break BLOCK_PRODUCER;
                }
                for (int i = 0; i < RETRY_COUNT; i++) {
                    interrupted();
                    producerBlocker_.block();
                    if (queue_.size() <= notifyLine_) {
                        break BLOCK_PRODUCER;
                    }
                }
                if (maxQueueSize_ < queue_.size()) {
                    producerBlocker_.clear();
                    consumerBlocker_.clear();
                    throw new RuntimeException("time over by offer.");
                }
            } catch (InterruptedException e) {
                producerBlocker_.clear();
                consumerBlocker_.clear();
                throw new RuntimeException(e);
            }
        }
        synchronized (queue_) {
            queue_.offer(obj);
        }
        if (notifyLine_ <= queue_.size()) {
            consumerBlocker_.open();
        }
    }

    @Override
    public void finish() {
        isFinished_ = true;
        consumerBlocker_.open();
    }

    @Override
    public Iterator<Container> consumerIterator() {
        return new ConsumerIterator();
    }

    public void reserveInterrupt() {
        reserveInterrupt(null);
    }

    public void reserveInterrupt(Throwable interruptCause) {
        reserveInterrupt_ = true;
        interruptCause_ = interruptCause;
    }

    private void interrupted() {
        if (reserveInterrupt_) {
            producerBlocker_.clear();
            consumerBlocker_.clear();
            if (null == interruptCause_) {
                throw new RuntimeException("this thread is interrupted.");
            } else {
                throw new RuntimeException("this thread is interrupted.", interruptCause_);
            }
        }
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Inner classes

    public class ConsumerIterator implements Iterator<Container> {

        @Override
        public boolean hasNext() {
            consumerThreadName_ = Thread.currentThread().getName();
            interrupted();
            BLOCK_CONSUMER: if (!isFinished_ && queue_.isEmpty()) {
                try {
                    consumerBlocker_.openEmergency();
                    if (isFinished_ || (notifyLine_ <= queue_.size())) {
                        break BLOCK_CONSUMER;
                    }
                    for (int i = 0; i < RETRY_COUNT; i++) {
                        interrupted();
                        consumerBlocker_.block();
                        if (isFinished_ || (notifyLine_ <= queue_.size())) {
                            break BLOCK_CONSUMER;
                        }
                    }
                    if (!isFinished_ && queue_.isEmpty()) {
                        producerBlocker_.clear();
                        consumerBlocker_.clear();
                        throw new RuntimeException("time over by hasNext.");
                    }
                } catch (InterruptedException e) {
                    producerBlocker_.clear();
                    consumerBlocker_.clear();
                    throw new RuntimeException(e);
                }
            }
            boolean result = !queue_.isEmpty();
            if (!result) {
                producerBlocker_.clear();
                consumerBlocker_.clear();
            }
            return result;
        }

        @Override
        public Container next() {
            Container container = null;
            synchronized (queue_) {
                while (null == (container = queue_.poll())) {
                    interrupted();
                    producerBlocker_.openEmergency();
                }
            }
            if (queue_.size() <= notifyLine_) {
                producerBlocker_.open();
            }
            return container;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    // ///////////////////////

    private static class Blocker {
        private long waitTime_;

        private volatile Blocker opposite_;

        private volatile boolean isWaiting_;

        private volatile boolean existBlockBorder_;

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Constructor

        public Blocker(long waitTime) {
            waitTime_ = waitTime;
            isWaiting_ = false;
            existBlockBorder_ = false;
        }

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Mehotds

        public void setOpposite(Blocker opposite) {
            opposite_ = opposite;
        }

        public void clear() {
            if (null == opposite_) {
                return;
            }
            opposite_.opposite_ = null;
            opposite_ = null;
        }

        public void block() throws InterruptedException {
            existBlockBorder_ = true;
            opposite_.openInner();
            synchronized (this) {
                existBlockBorder_ = false;
                isWaiting_ = true;
                this.wait(waitTime_);
                isWaiting_ = false;
            }
        }

        public void open() {
            for (int i = 0; i < 10; i++) {
                if (!existBlockBorder_) {
                    break;
                }
                synchronized (this) {
                    try {
                        this.wait(1);
                    } catch (InterruptedException e) {
                        clear();
                        throw new RuntimeException(e);
                    }
                }
            }
            openInner();
        }

        public void openEmergency() {
            existBlockBorder_ = true;
            opposite_.openInner();
            synchronized (this) {
                existBlockBorder_ = false;
                isWaiting_ = true;
                try {
                    this.wait(EMERGENCY_WAIT_TIME);
                } catch (InterruptedException e) {
                    clear();
                    throw new RuntimeException(e);
                }
                isWaiting_ = false;
            }
        }

        private void openInner() {
            if (isWaiting_) {
                synchronized (this) {
                    this.notifyAll();
                }
            }
        }
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Logger

    private static final LaolLogger LOG = new LaolLogger(ParallelProducerQueue.class.getName());
}
