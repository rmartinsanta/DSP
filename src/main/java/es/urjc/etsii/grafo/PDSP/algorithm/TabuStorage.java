package es.urjc.etsii.grafo.PDSP.algorithm;

import com.google.common.collect.Maps;

import java.util.LinkedHashMap;

public class TabuStorage<K> {

    private final LinkedHashMap<K, Integer> tabuItems;
    private final int tabuTenureIterations;

    public TabuStorage(int tabuTenureIterations) {
        this.tabuTenureIterations = tabuTenureIterations;
        this.tabuItems = Maps.newLinkedHashMapWithExpectedSize(10_000);
    }

    public boolean contains(K property) {
        return tabuItems.containsKey(property);
    }

    public void checkExpiration(int currentIteration) {
        // The list has to be iterated in order to remove the expired elements.
        tabuItems.entrySet().removeIf(e -> e.getValue() <= currentIteration);
    }

    public void add(K property, int currentIteration, int tenureMultiplier) {
        // Add the element with the expiration date, which is the current iteration plus the max iterations
        tabuItems.put(property, Math.addExact(currentIteration, tabuTenureIterations * tenureMultiplier));
    }
}
