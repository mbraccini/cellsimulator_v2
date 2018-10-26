//package interfaces.cell;
//
//import cell.CellImpl;
//import dynamic.SynchronousDynamicsImpl;
//import utility.RandomnessFactory;
//import interfaces.dynamic.Dynamics;
//import interfaces.network.BNClassic;
//import interfaces.state.BinaryState;
//import network.BooleanNetworkFactory;
//
//import java.util.BitSet;
//import java.util.Random;
//
//public class MainCell {
//
//    public static void main(String[] args) {
//       randomGeneratorr = RandomnessFactory.newPseudoRandomGenerator(5);
//
//        BNClassic<BitSet,Boolean> bn = BooleanNetworkFactory.newRBN(BooleanNetworkFactory.BiasType.EXACT,
//                BooleanNetworkFactory.SelfLoop.WITHOUT,
//                5,
//                2 ,
//                0.5,
//                r);
//
//        Dynamics<BinaryState> dynamics = new SynchronousDynamicsImpl(bn);
//
//        Cell<BitSet,Boolean,BinaryState> cell = new CellImpl<>(bn, dynamics, "pluto");
//
//        System.out.println();
//
//  /*     cell.getCustom(cell.compose(StateFunction.withNoise(4,r))::apply,
//                        BinaryState.valueOf("01011"))
//                        .stream()
//                        .limit(10)
//                        .forEach(System.out::println);
//
//
//*/
//
//
//        cell.getDefault(BinaryState.valueOf("00101"))
//                .stream()
//                .limit(10)
//                .forEach(System.out::println);
//
//
//    }
//}
