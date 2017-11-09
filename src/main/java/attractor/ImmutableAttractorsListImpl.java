package attractor;

import interfaces.attractor.AttractorInfo;
import interfaces.attractor.ImmutableAttractorsList;
import interfaces.attractor.LabelledOrderedAttractor;

import java.util.*;
import java.util.stream.Collectors;

public class ImmutableAttractorsListImpl<T extends Comparable<? super T>> implements ImmutableAttractorsList<T> {

    private List<LabelledOrderedAttractor<T>> labelledAttractors;

    public ImmutableAttractorsListImpl(List<LabelledOrderedAttractor<T>> attractors) {
        labelledAttractors = Collections.unmodifiableList(attractors);
    }

    @Override
    public int size() {
        return labelledAttractors.size();
    }

    @Override
    public boolean isEmpty() {
        return labelledAttractors.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return labelledAttractors.contains(o);
    }

    @Override
    public Iterator<LabelledOrderedAttractor<T>> iterator() {
        return labelledAttractors.iterator();
    }

    @Override
    public Object[] toArray() {
        return labelledAttractors.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return labelledAttractors.toArray(a);
    }

    @Override
    public boolean add(LabelledOrderedAttractor<T> tLabelledOrderedAttractor) {
        return labelledAttractors.add(tLabelledOrderedAttractor);
    }

    @Override
    public boolean remove(Object o) {
        return labelledAttractors.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return labelledAttractors.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends LabelledOrderedAttractor<T>> c) {
        return labelledAttractors.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends LabelledOrderedAttractor<T>> c) {
        return labelledAttractors.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return labelledAttractors.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return labelledAttractors.retainAll(c);
    }

    @Override
    public void clear() {
        labelledAttractors.clear();
    }

    @Override
    public LabelledOrderedAttractor<T> get(int index) {
        return labelledAttractors.get(index);
    }

    @Override
    public LabelledOrderedAttractor<T> set(int index, LabelledOrderedAttractor<T> element) {
        return labelledAttractors.set(index, element);
    }

    @Override
    public void add(int index, LabelledOrderedAttractor<T> element) {
        labelledAttractors.add(index, element);
    }

    @Override
    public LabelledOrderedAttractor<T> remove(int index) {
        return labelledAttractors.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return labelledAttractors.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return labelledAttractors.lastIndexOf(o);
    }

    @Override
    public ListIterator<LabelledOrderedAttractor<T>> listIterator() {
        return labelledAttractors.listIterator();
    }

    @Override
    public ListIterator<LabelledOrderedAttractor<T>> listIterator(int index) {
        return labelledAttractors.listIterator(index);
    }

    @Override
    public List<LabelledOrderedAttractor<T>> subList(int fromIndex, int toIndex) {
        return labelledAttractors.subList(fromIndex, toIndex);
    }

    @Override
    public String toString() {
        return "immutableAttList:::\n"
                + labelledAttractors
                    .stream().map(x -> x.getFirstState().toString() + " --<>-- " + x.toString())
                                .collect(Collectors.joining("\n"));
    }
}
