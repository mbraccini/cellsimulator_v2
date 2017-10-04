package simulator;

import java.math.BigInteger;

public class MyCountDownLatch {

    private BigInteger counter;
    private boolean canGoOn;

    public MyCountDownLatch(BigInteger limit) {
        this.counter = limit;
        this.canGoOn = false;
    }


    public synchronized void countDown() {
        this.counter = this.counter.subtract(BigInteger.ONE);

        if (this.counter.compareTo(BigInteger.ZERO) <= 0) { //corrisponde a if (this.sample < this.combinations){
            this.canGoOn = true;
            notify();
        }
    }

    public synchronized BigInteger getCount() {
        return this.counter;
    }

    public synchronized void await() {
        while (this.canGoOn == false) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
