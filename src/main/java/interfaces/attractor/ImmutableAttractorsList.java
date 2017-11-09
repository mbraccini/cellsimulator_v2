package interfaces.attractor;

import attractor.AttractorImpl;
import attractor.ImmutableAttractorsListImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public interface ImmutableAttractorsList<T extends Comparable<? super T>> extends List<LabelledOrderedAttractor<T>>{

    public static <T extends Comparable<? super T>> List<LabelledOrderedAttractor<T>> fromInfoToAttractors(Collection<AttractorInfo<T>> infoCollection) {
        infoCollection.stream().forEach(x -> Collections.sort(x.getStates())); //ordino gli stati dell'attroctorInfo

        List<AttractorInfo<T>> ordered = infoCollection.stream()
                .sorted((x, y) -> x.getStates().get(0).compareTo(y.getStates().get(0)))
                .collect(Collectors.toList());
        List<LabelledOrderedAttractor<T>> temp = new ArrayList<>();
        Integer counter = 1;
        for (AttractorInfo<T> aInfo : ordered) {
            temp.add(new AttractorImpl<>(aInfo, counter));
            counter++;
        }
        return new ImmutableAttractorsListImpl<>(temp);
    }
}
