package jp.gr.naoco.chain.fork;

import java.util.ArrayList;
import java.util.List;

import jp.gr.naoco.chain.Chain;
import jp.gr.naoco.chain.ConsumerFilter;

public class ForkLegChain extends Chain {

    private ForkLegProducer legProducer_;

    // ////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    public ForkLegChain(ConsumerFilter filter) {
        super(new ForkLegProducer(filter));
        init();
    }

    public ForkLegChain(ConsumerFilter filter, int queueSize) {
        super(new ForkLegProducer(filter), queueSize);
        init();
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////
    // Methods

    protected ForkLegProducer getForkLegConsumerProducer() {
        return legProducer_;
    }

    protected List<Broker> getBrokerList() {
        return new ArrayList<Broker>(brokerList_);
    }

    protected List<Broker> getInterruptBrokerList() {
        if (null == interruptBrokerList_) {
            interruptBrokerList_ = new ArrayList<Broker>(brokerList_);
        }
        return interruptBrokerList_;
    }

    // ////////////////////////////////

    private void init() {
        legProducer_ = (ForkLegProducer) super.getProducer();
    }
}
