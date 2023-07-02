package io.thirdreality.project.weakai.navigation;

import java.util.*;

public class Neuron implements Comparable
{
    public ArrayList<Binding> nextSteps = null;
    public int direction = -1;
    public boolean colliderNorth = false, colliderEast = false, colliderSouth = false, colliderWest = false;
    public boolean success = false;

    public Neuron()
    {
        nextSteps = new ArrayList<Binding>();
    }

    public void decide()
    {
        switch(direction)
        {
            case 0:
            {
                success = !colliderNorth;

                break;
            }
            case 1:
            {
                success = !colliderEast;

                break;
            }
            case 2:
            {
                success = !colliderSouth;

                break;
            }
            case 3:
            {
                success = !colliderWest;

                break;
            }
        }
    }

    @Override
    public boolean equals(Object o)
    {
        Neuron n = ((Neuron) o);

        return n.direction == direction && n.colliderNorth == colliderNorth && n.colliderEast == colliderEast && n.colliderSouth == colliderSouth && n.colliderWest == colliderWest && n.success == success;
    }

    public int compareTo(Object o)
    {
        Neuron n = ((Neuron) o);

        boolean result = n.direction == direction && n.colliderNorth == colliderNorth && n.colliderEast == colliderEast && n.colliderSouth == colliderSouth && n.colliderWest == colliderWest && n.success == success;

        return result ? 1 : 0;
    }

}