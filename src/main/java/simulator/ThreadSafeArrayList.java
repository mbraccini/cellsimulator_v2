package simulator;

import interfaces.attractor.AttractorInfo;
import interfaces.state.State;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class ThreadSafeArrayList<T extends State>  {
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private final Lock readLock = readWriteLock.readLock();

    private final Lock writeLock = readWriteLock.writeLock();

    private final List<AttractorInfo<T>> list = new ArrayList<>();

    public boolean add(AttractorInfo<T> o) {
        //System.out.println("Add");
        writeLock.lock();
        try {
            //System.out.println("Adding element by thread" + Thread.currentThread().getName());
            //System.out.println("elemento_ "+ o);
            if (!containsState(o.getStates().get(0))) { // solo se non lo contiene già
                //System.out.println("SI_elemento_ "+ o + ", lista:" + list);

                list.add(o);
                return true;
            } else {
                //System.out.println("NO_elemento_ "+ o);

                return false;
            }
        } finally {
            writeLock.unlock();
        }
    }

    public AttractorInfo<T> get(int i) {
        readLock.lock();
        try {
            //System.out.println("Printing elements by thread" + Thread.currentThread().getName());
            return list.get(i);
        } finally {
            readLock.unlock();
        }
    }

    public List toImmutableList() {
        readLock.lock();
        try {
            //System.out.println("Printing elements by thread: " + Thread.currentThread().getName());
            return Collections.unmodifiableList(list);
        } finally {
            readLock.unlock();
        }
    }

    public boolean containsState(T state) {
        readLock.lock();
        try {
            if (list.stream().anyMatch(x -> x.getStates().contains(state))) {
                //System.out.println("elemento presente! " + list.toString() + Thread.currentThread().getName());
                return true;
            } else {
                return false;
            }
        } finally {
            readLock.unlock();
        }
    }

    public int size() {
        readLock.lock();
        try {
            //System.out.println("Printing elements by thread: " + Thread.currentThread().getName());
            return list.size();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public String toString() {
        return list.toString();
    }
}