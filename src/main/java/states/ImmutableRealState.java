package states;

import interfaces.state.RealState;
import interfaces.state.State;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Objects;

public class ImmutableRealState implements RealState {

    private final double[] state;

    public ImmutableRealState(double[] s){
        this.state = Arrays.copyOf(s, s.length);
    }

    public ImmutableRealState(ImmutableRealState s){
        this.state = Arrays.copyOf(s.state, s.state.length);
    }

    @Override
    public String getStringRepresentation() {
        DecimalFormat df = new DecimalFormat("#0.00");

        StringBuilder builder = new StringBuilder();
        builder.append('[');
        for(int i = 0; i < state.length; i++)
        {
            String output = df.format(state[i]);
            builder.append(output);
            if (i != state.length -1){
                builder.append(", ");
            } else {
                builder.append("]");
            }
        }

        return builder.toString();
    }

    @Override
    public Integer getLength() {
        return state.length;
    }

    @Override
    public int compareTo(State o) {
        if (Objects.isNull(o)) {
            throw new NullPointerException();
        }

        ImmutableRealState that = (ImmutableRealState) o;

        return Arrays.compare(state, that.state);
    }

    @Override
    public String toString() {
        return getStringRepresentation() ;
    }

    @Override
    public Double getNodeValue(Integer index) {
        //l'indice 0 è a destra!
        return state[state.length - 1 - index];
    }
}
