package jp.gr.naoco.chain;

public interface ConsumerFilter {
    public boolean accept(Container container);

    /** 受け渡されたConsumerの状態にかかわらず、すべてConsumerあるいはConsumerProducerの処理を実行するフィルタ */
    public static final ConsumerFilter PASS_THROUGH_FILTER = new ConsumerFilter() {
        @Override
        public boolean accept(Container container) {
            return true;
        }
    };

    /** 受け渡されたConsumerの状態にかかわらず、すべてConsumerあるいはConsumerProducerの処理を実行しないフィルタ */
    public static final ConsumerFilter ALL_SKIP_FILTER = new ConsumerFilter() {
        @Override
        public boolean accept(Container container) {
            return false;
        }
    };
}
