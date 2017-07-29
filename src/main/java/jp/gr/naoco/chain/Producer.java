package jp.gr.naoco.chain;

import jp.gr.naoco.chain.queue.ProducerQueue;

public interface Producer {
    public void execute(ProducerQueue queue);
}
