package ir.sharif.aic.hideandseek.algorithms;

import java.util.ArrayList;
import java.util.Comparator;

public class Test {
    public static void main(String[] args) {
        ArrayList<NODO> doubles = new ArrayList<>();
        doubles.add(new NODO(2.2));
        doubles.add(new NODO(234.4));
        doubles.add(new NODO(1.2));

        doubles.sort((Comparator.comparingDouble(o -> o.value)));
        System.out.println(doubles.get(0).value);


        ArrayList<Integer> ints = new ArrayList<>();
        ints.add(2);
        ints.add(4);
        System.out.println(ints);
    }
}


class NODO{
    double value;
    NODO(double value){
        this.value = value;
    }
}