package attractor;

import interfaces.attractor.ImmutableList;
import interfaces.state.BinaryState;
import interfaces.state.Immutable;
import states.ImmutableBinaryState;

import java.util.*;
import java.util.function.UnaryOperator;

public class ImmutableListImpl<T extends Immutable> implements ImmutableList<T> {

    private List<T> internalList;

    public ImmutableListImpl(List<T> list) {
        internalList = Collections.unmodifiableList(new ArrayList<>(list));
    }

    @Override
    public int size() {
        return internalList.size();
    }

    @Override
    public boolean isEmpty() {
        return internalList.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return internalList.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return internalList.iterator();
    }

    @Override
    public Object[] toArray() {
        return internalList.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return internalList.toArray(a);
    }

    @Override
    public boolean add(T t) {
        return internalList.add(t);
    }

    @Override
    public boolean remove(Object o) {
        return internalList.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return internalList.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return internalList.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        return internalList.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return internalList.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return internalList.retainAll(c);
    }

    @Override
    public void replaceAll(UnaryOperator<T> operator) {
        internalList.replaceAll(operator);
    }

    @Override
    public void sort(Comparator<? super T> c) {
        internalList.sort(c);
    }

    @Override
    public void clear() {
        internalList.clear();
    }

    @Override
    public T get(int index) {
        return internalList.get(index);
    }

    @Override
    public T set(int index, T element) {
        return internalList.set(index, element);
    }

    @Override
    public void add(int index, T element) {
        internalList.add(index, element);
    }

    @Override
    public T remove(int index) {
        return internalList.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return internalList.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return internalList.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return internalList.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return internalList.listIterator(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return internalList.subList(fromIndex, toIndex);
    }

    @Override
    public Spliterator<T> spliterator() {
        return internalList.spliterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImmutableListImpl<?> that = (ImmutableListImpl<?>) o;
        return Objects.equals(internalList, that.internalList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(internalList);
    }

    @Override
    public String toString() {
        return "ImmutableListImpl{" + internalList + '}';
    }

/*@Override
    public String toString() {
        return "immutableAttList:::\n"
                + internalList
                    .stream().map(x -> x.getFirstState().toString() + " --<>-- " + x.toString())
                                .collect(Collectors.joining("\n"));
    }*/

    public static void main(String[]arg ){
        BinaryState a = new ImmutableBinaryState(4, 0, 1, 2, 3);
        BinaryState b = new ImmutableBinaryState(4);
        List<BinaryState> c = new ArrayList<>();
        c.add(a);
        c.add(b);

        System.out.println("C" + c);
        List<BinaryState> imm = new ImmutableListImpl<>(c);
        System.out.println("IMM " + imm);
        Collections.sort(c);
        System.out.println("C sorted" + c);
        System.out.println("IMM after" + imm);

    }

}
