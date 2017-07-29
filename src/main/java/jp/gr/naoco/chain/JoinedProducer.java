package jp.gr.naoco.chain;

public interface JoinedProducer extends Producer {
    public void join();

    public void reserveInterrupt(Throwable t);
}
