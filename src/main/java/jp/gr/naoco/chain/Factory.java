package jp.gr.naoco.chain;

import jp.gr.naoco.core.factory.SingletoneInstanceFactory;

public class Factory {

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    private Factory() {
        // nothing to do
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Methods

    public static ConsumerInterface consumer(final Class<? extends ConsumerInterface> instanceClass) {
        SingletoneInstanceFactory<ConsumerInterface> factory = new SingletoneInstanceFactory<ConsumerInterface>() {
            @Override
            protected Class<?> getInstanceClass() {
                return instanceClass;
            }

            @Override
            protected Class<ConsumerInterface> getInterfaceClass() {
                return ConsumerInterface.class;
            }
        };
        return factory.getInsatnce();
    }

    public static Producer producer(final Class<? extends Producer> instanceClass) {
        SingletoneInstanceFactory<Producer> factory = new SingletoneInstanceFactory<Producer>() {
            @Override
            protected Class<?> getInstanceClass() {
                return instanceClass;
            }

            @Override
            protected Class<Producer> getInterfaceClass() {
                return Producer.class;
            }
        };
        return factory.getInsatnce();
    }

    public static ConsumerProducerInterface consumerProducer(
            final Class<? extends ConsumerProducerInterface> instanceClass) {
        SingletoneInstanceFactory<ConsumerProducerInterface> factory = new SingletoneInstanceFactory<ConsumerProducerInterface>() {
            @Override
            protected Class<?> getInstanceClass() {
                return instanceClass;
            }

            @Override
            protected Class<ConsumerProducerInterface> getInterfaceClass() {
                return ConsumerProducerInterface.class;
            }
        };
        return factory.getInsatnce();
    }
}
