package io.thirdreality.project.weakai.navigation;

import java.lang.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import java.io.*;

public class Navigation extends JFrame implements Serializable
{
    // Sensorik-Daten, Aktion / Handeln, Erfolg der Handlung merken sowie Folge davon.
    // Bei Fehler zurück auf Ausgangsposition.

    private boolean[][] fields;
    private JButton[][] buttons;

    // current position
    private int xLoc = 0, yLoc = 0;

    // spawn point
    private int xSpawn, ySpawn;

    private int targetX = 0, targetY = 0;

    private final int worldSize = 8;
    private final int countRandomCollisionObjects = (worldSize * worldSize) / 2;

    private void generateRandomWorld()
    {
        fields = new boolean[worldSize][worldSize];

        int x = 0, y = 0;

        for(int n = 0; n < countRandomCollisionObjects; n++)
        {
            x = (int) (Math.random() * worldSize);
            y = (int) (Math.random() * worldSize);

            // spawn point
            if(n == 0 || (x == xLoc && y == yLoc))
            {
                xLoc = x;
                yLoc = y;

                xSpawn = x;
                ySpawn = y;

                buttons[x][y].setBackground(Color.YELLOW);

                continue;
            }

            if(n == countRandomCollisionObjects - 1)
            {
                targetX = x;
                targetY = y;

                buttons[targetX][targetY].setBackground(Color.ORANGE);

                continue;
            }

            // make collision object:
            fields[x][y] = true;

            buttons[x][y].setBackground(Color.RED);
        }

        // I assume the spawn point is not surrounded by collision objects..
    }

    class JPlayerButton extends JButton
    {
        public int x,y;

        public JPlayerButton(String title)
        {
            super(title);
        }

        @Override
        public void paint(Graphics g)
        {
            super.paint(g);

            if(xLoc == x && yLoc == y)
            {
                g.setColor(Color.decode("#990099"));
                g.fillOval(0,0,g.getClipBounds().width,g.getClipBounds().height);
            }
        }
    }

    private void initButtons(JPanel panel)
    {
        buttons = new JPlayerButton[worldSize][worldSize];

        for(int y = 0; y < worldSize; y++)
        {
            for(int x = 0; x < worldSize; x++)
            {
                JPlayerButton field = new JPlayerButton("(" + x + "|" + y + ")");
                field.x = x;
                field.y = y;

                field.setBackground(Color.GREEN);

                buttons[x][y] = field;

                panel.add(field);
            }
        }
    }

    public Navigation()
    {
        setSize(640, 640);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("AI training");
        setResizable(false);

        JPanel panel = new JPanel();

        GridLayout layout = new GridLayout(worldSize, worldSize);
        panel.setLayout(layout);

        initButtons(panel);
        generateRandomWorld();

        add(panel);
    }

    private void move(int direction)
    {
        switch(direction)
        {
            case 0:
                yLoc = (yLoc + 1) < fields.length ? yLoc + 1 : yLoc;
                break;
            case 1:
                xLoc = (xLoc + 1) < fields.length ? xLoc + 1 : xLoc;
                break;
            case 2:
                yLoc = (yLoc - 1) >= 0 ? yLoc - 1 : 0;
                break;
            case 3:
                xLoc = (xLoc - 1) >= 0 ? xLoc - 1 : 0;
                break;
        }

        System.out.println("Moved to: " + xLoc + "|" + yLoc + " due to " + direction);
    }

    private static void pause(long ms)
    {
        long msStart = System.nanoTime() / (1000 * 1000);

        while((System.nanoTime() / (1000 * 1000)) < (msStart + ms));
    }

    // TODO Begin every training at this point. Needs to be re-determined / calculated for every scenario (no random math!).
    // private Neuron root = null;

    /**
     * This List contains all neurons known to the AI.
     * There are no duplicates.
     */
    private ArrayList<Neuron> neurons = new ArrayList<>();

    /**
     * Tries to find senseful information from the environment.
     *
     * @return Neuron representing the information gathered from the environment.
     */
    public Neuron analyze()
    {
        Neuron n = null;

        do
        {
            n = new Neuron();

            int rand = (int) (Math.random() * 4);

            n.direction = rand;

            boolean colliderNorth = (yLoc + 1) < fields.length ? fields[xLoc][yLoc + 1] : false,
                    colliderEast = (xLoc + 1) < fields.length ? fields[xLoc + 1][yLoc] : false,
                    colliderSouth = (yLoc - 1) >= 0 ? fields[xLoc][yLoc - 1] : false,
                    colliderWest = (xLoc - 1) >= 0 ? fields[xLoc - 1][yLoc] : false;

            n.colliderNorth = colliderNorth;
            n.colliderEast = colliderEast;
            n.colliderSouth = colliderSouth;
            n.colliderWest = colliderWest;

            n.decide();
        }
        while(!n.success);

        return n;
    }

    /**
     * Links new information.
     *
     * @param analyzed New neuron including its information to recognize.
     * @return Neuron that matches the given neuron indeed (yet) or was newly recognized.
     */
    public Neuron recognize(Neuron analyzed)
    {
        if(!neurons.contains(analyzed))
        {
            // Remember the information generally (not the recognition).
            neurons.add(analyzed);

            return analyzed;
        }

        // Recognition part:
        int index = neurons.indexOf(analyzed);

        // This is the actual neuron which contains the same information as the given Neuron n.
        Neuron matchingNeuron = neurons.get(index);

        return matchingNeuron;
    }

    /**
     * Links the neurons in the execution order,
     * so they know each other.
     *
     * @param before Neuron from last cycle
     * @param recognized Neuron from current cycle
     * @return Binding that was used to connect both neurons (not necessarily a new Binding but possibly also an existing one). Can be null if this is the first program cycle with no neuron before from a last cycle.
     */
    private Binding link(Neuron before, Neuron recognized)
    {
        // Create neutral binding (value = 0) to the recognized neuron.
        Binding binding = new Binding(0, recognized);

        // Make sure before linking them,
        // they do not know each other yet (prevent duplicates).
        // Consider: 'before' only knows 'recognized' but not the other way around.
        if(before != null)
        {
            if(!before.nextSteps.contains(binding))
            {
                // Linking done.
                before.nextSteps.add(binding);

                return binding;
            }
            else // Return the binding that does exist yet.. (only making the method safer cuz I don't if this case should occur at all => I don't think so).
            {
                int index = before.nextSteps.indexOf(binding);

                return before.nextSteps.get(index);
            }
        }

        // Return null if no neuron from the last cycle does exist yet (because it is simply the first program cycle).
        return null;
    }

    private void act(Neuron recognized)
    {
        move(recognized.direction);
        repaint();
    }

    /**
     * This will train the AI until it has found a solution.
     * Multiple calls to the train() method can hence improve the AI skills
     * if the caller of this method uses the returned List to
     * supply scores to quick result paths (Bindings).
     *
     * @return Path or rather the Bindings that were used to connect the neurons until a solution was found.
     */
    private ArrayList<Binding> train()
    {
        // Used to take or give score to neurons in the current attempt.
        ArrayList<Binding> scenario = new ArrayList<>();

        Neuron before = null;

        // Only train until the destination has been reached (condition needed).
        while(xLoc != targetX || yLoc != targetY)
        {
            // Create an impressional neuron from the environment with a possible reaction.
            Neuron analyzed = analyze();

            // This is the correct neuron to use.
            Neuron recognized = recognize(analyzed);

            // Link neurons from before and now.
            Binding binding = link(before, recognized);

            // Make sure you only remember a binding when there was a neuron from the last cycle.
            if(binding != null)
            {
                // Remember the neuron binding for evaluation of the route cost later.
                scenario.add(binding);
            }

            act(recognized);

            before = recognized;
        }

        // Return all neurons used to go the way to the result (target point).
        return scenario;
    }

    /**
     * You can give a scenario some treats (points) if the
     * scenario describes a shorter result path
     * or longer one (take away treats = negative number).
     *
     * @param scenario Scenario to give treats.
     * @param treats Treats or points to give or take away (negative).
     */
    private void treat(ArrayList<Binding> scenario, long treats)
    {
        for(Binding binding : scenario)
        {
            binding.value += treats;
        }
    }

    /** Remembers to the length of the shortest known result path / scenario */
    public long bestScenarioLength = Long.MAX_VALUE;

    /**
     * Evaluates the gained data from a training.
     * This method will give score points to Bindings
     * if the result path was shorter than a different one.
     *
     * @param scenario Training data, including a lot of Bindings ideally.
     */
    private void evaluate(ArrayList<Binding> scenario)
    {
        if(scenario.size() < bestScenarioLength)
        {
            bestScenarioLength = scenario.size();

            // Give the scenario some treats for finding a solution faster..
            treat(scenario, 1);
        }

        // There is no taking away for treats for whole scenarios because this would cause the AI
        // to avoid routes in general but it should rather prefer (!) routes with a higher score.
    }

    public static void main(String[] args) throws IllegalArgumentException, NumberFormatException
    {
        if(args.length != 1)
        {
            throw new IllegalArgumentException("Expecting at least one parameter for console execution! (max steps for training)");
        }

        int maxStepsTraining = maxStepsTraining = Integer.parseInt(args[0]);

        Navigation navi = new Navigation();
        navi.setVisible(true);

        for(int steps = 0; steps < maxStepsTraining; steps++)
        {
            ArrayList<Binding> bindingsNeeded = navi.train();
            navi.evaluate(bindingsNeeded);
            System.out.println("Training done (" + bindingsNeeded.size() + " bindings needed, shortest is currently " + navi.bestScenarioLength + ")!");
            pause(2000);

            navi.xLoc = navi.xSpawn;
            navi.yLoc = navi.ySpawn;
        }

        System.out.println("Training done!");
    }
}