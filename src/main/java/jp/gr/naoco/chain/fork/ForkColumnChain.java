package jp.gr.naoco.chain.fork;

import java.util.ArrayList;
import java.util.List;

import jp.gr.naoco.chain.Chain;
import jp.gr.naoco.chain.Consumer;
import jp.gr.naoco.chain.ConsumerFilter;
import jp.gr.naoco.chain.ConsumerInterface;
import jp.gr.naoco.chain.Producer;
import jp.gr.naoco.core.log.LaolLogger;

public class ForkColumnChain extends Chain {
    private List<ForkLegChain> legList_;

    // ////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    public ForkColumnChain(Producer producer) {
        super(producer);
    }

    public ForkColumnChain(Producer producer, int queueSize) {
        super(producer, queueSize);
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////
    // Methods

    @Override
    public Chain direct(Consumer consumer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Chain direct(Consumer consumer, ConsumerFilter filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Chain sequential(ConsumerInterface consumer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Chain sequential(ConsumerInterface consumer, ConsumerFilter filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Chain parallel(ConsumerInterface consumer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Chain parallel(ConsumerInterface consumer, ConsumerFilter filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void execute() {
        for (ForkLegChain leg : legList_) {
            leg.executeWithoutJoin();
        }
        super.execute();
        for (ForkLegChain leg : legList_) {
            try {
                leg.join();
            } catch (Throwable t) {
                LOG.fatal(t.getMessage(), t);
            }
        }
    }

    @Override
    public Chain terminate() {
        throw new UnsupportedOperationException();
    }

    public void connectLegs(List<ForkLegChain> legList) {
        legList_ = legList;
        super.direct(new ForkColumnConsumer(legList), ConsumerFilter.PASS_THROUGH_FILTER);

        ArrayList<Broker> mergedList = new ArrayList<Broker>();
        mergedList.addAll(interruptBrokerList_);
        for (ForkLegChain leg : legList) {
            mergedList.addAll(leg.getInterruptBrokerList());
        }
        for (ForkLegChain leg : legList) {
            List<Broker> legBrokerList = leg.getInterruptBrokerList();
            legBrokerList.clear();
            legBrokerList.addAll(interruptBrokerList_);
        }
        interruptBrokerList_ = mergedList;
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Logger

    private static final LaolLogger LOG = new LaolLogger(ForkColumnChain.class.getName());
}
