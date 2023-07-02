package io.thirdreality.project.weakai.navigation;

import java.util.*;

public class Binding
{
    // Score value to emphasize better or worse ways to a specific neuron as a target in a scenario.
    public long value;

    public Neuron target;

    public Binding(long value, Neuron target)
    {
        // Prevent overflows
        if(value >= (Long.MAX_VALUE-1))
            this.value = Long.MAX_VALUE-1;
        else
            this.value = value;

        this.target = target;
    }

    @Override
    public boolean equals(Object o)
    {
        return ((Binding) o).target.equals(target);
    }
}