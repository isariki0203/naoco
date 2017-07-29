package jp.gr.naoco.chain.fork;

import java.util.ArrayList;
import java.util.List;

import jp.gr.naoco.chain.Consumer;
import jp.gr.naoco.chain.Container;
import jp.gr.naoco.core.log.LaolLogger;

public class ForkColumnConsumer extends Consumer {

    private List<ForkLegProducer> legProducerList_;

    // ////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    protected ForkColumnConsumer(List<ForkLegChain> legChainList) {
        legProducerList_ = new ArrayList<ForkLegProducer>(legChainList.size());
        for (ForkLegChain legChain : legChainList) {
            legProducerList_.add(legChain.getForkLegConsumerProducer());
        }
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////
    // Methods

    @Override
    public void execute_(Container container) {
        for (ForkLegProducer producer : legProducerList_) {
            producer.connect(container);
        }
    }

    @Override
    public void finalize() {
        Throwable t = null;
        for (ForkLegProducer producer : legProducerList_) {
            try {
                producer.finish();
                LOG.debug("finish to " + producer.getThread().getName());
            } catch (Throwable e) {
                t = e;
            }
        }
        if (null != t) {
            throw new RuntimeException(t);
        }
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Logger

    private static final LaolLogger LOG = new LaolLogger(ForkColumnConsumer.class.getName());
}
