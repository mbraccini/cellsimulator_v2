package attractor;

import interfaces.attractor.AttractorInfo;
import interfaces.attractor.LabelledOrderedAttractor;
import interfaces.state.State;

import java.util.*;

public class AttractorsUtility {
    private AttractorsUtility(){}

    public static <T extends State> List<LabelledOrderedAttractor<T>> fromInfoToAttractors(final Collection<AttractorInfo<T>> infoCollection) {
        List<AttractorInfo<T>> copyOfInfoCollection = new ArrayList<>(infoCollection);

        copyOfInfoCollection.stream().forEach(x -> Collections.sort(x.getStates())); //ordino gli stati dell'attroctorInfo

        Collections.sort(copyOfInfoCollection, (x, y) -> x.getStates().get(0).compareTo(y.getStates().get(0)));

        List<LabelledOrderedAttractor<T>> temp = new ArrayList<>();
        Integer counter = 1;
        for (AttractorInfo<T> aInfo : copyOfInfoCollection) {
            temp.add(new AttractorImpl<>(aInfo, counter));
            counter++;
        }

        //List<LabelledOrderedAttractor<T>> result = new ImmutableListImpl<>(temp);
        //temp = null;
        return new ImmutableListImpl<>(temp);
    }
}
