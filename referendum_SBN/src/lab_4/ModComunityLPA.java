package lab_4;

import it.stilo.g.structures.WeightedGraph;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModComunityLPA implements Runnable {

    private static final Logger logger = LogManager.getLogger(ModComunityLPA.class);

    private static Random rnd;
    private WeightedGraph g;

    private int chunk;
    private int runner;
    private CountDownLatch barrier;

    private int[] labels;
    private int[] list = null;
    private List<Integer> yes_list;
    private List<Integer> no_list;


    private ModComunityLPA(WeightedGraph g, CountDownLatch cb, int[] labels, int chunk, int runner, List<Integer> seeds, List<Integer> seeds2) {
        this.g = g;
        this.runner = runner;
        this.barrier = cb;
        this.labels = labels;
        this.chunk = chunk;
        this.yes_list = seeds;
        this.no_list = seeds2;
    }

    private boolean initList() {
        if (list == null) {

            list = new int[(g.in.length / runner) + runner];

            int j = 0;

            for (int i = chunk; i < g.in.length; i += runner) {
                if (g.in[i] != null) {
                	if(yes_list.contains(i)) //CHANGED: if in yes_list, label = 1
                		labels[i] = 1;
                	else if(no_list.contains(i)) //CHANGED: if in no_list, label = 0
                		labels[i] = 0;
                	else
                		labels[i] = i; //otherwise, label = node number
                    list[j] = i;
                    j++;
                } else {
                    if (g.out[i] != null) {
                    	if(yes_list.contains(i)) //CHANGED: if in yes_list, label = 1
                    		labels[i] = 1;
                    	else if(no_list.contains(i)) //CHANGED: if in no_list, label = 0
                    		labels[i] = 0;
                    	else
                    		labels[i] = i; //otherwise, label = node number
                    } else {
                        labels[i] = -1; //If node isolated
                    }
                }
            }
            list = Arrays.copyOf(list, j);

            //Shuffle
            for (int i = 0; i < list.length; i++) {
                for (int z = 0; z < 10; z++) {
                    int randomPosition = rnd.nextInt(list.length);
                    int temp = list[i];
                    list[i] = list[randomPosition];
                    list[randomPosition] = temp;
                }
            }

            return true;
        }
        return false;
    }

    public void run() {
        if (!initList()) {
            for (int i = 0; i < list.length; i++) {
                int[] near = g.in[list[i]];
                int[] nearLabs = new int[near.length];
                for (int x = 0; x < near.length; x++) {
                    nearLabs[x] = labels[near[x]];
                }
                labels[list[i]] = bestLabel(nearLabs);
            }
        }
        barrier.countDown();
    }

    public static int bestLabel(int[] neighborhood) {
        Arrays.sort(neighborhood);
        int best = -1;
        int maxCount = -1;
        int counter = 0;
        int last = -1;
        for (int i = 0; i < neighborhood.length; i++) {
            if (maxCount > (neighborhood.length - i)) {
                break;
            }

            if (neighborhood[i] == last) {
                counter++;
                if (counter > maxCount) {
                    maxCount = counter;
                    best = last;
                }
            } else {
                counter = 0;
                last = neighborhood[i];
            }
        }

        if (maxCount <= 1) {
            return neighborhood[rnd.nextInt(neighborhood.length)];
        }
        return best;
    }

    public static int[] compute(final WeightedGraph g, List<Integer> seeds, List<Integer> seeds2, double threshold, int runner) {

        ModComunityLPA.rnd = new Random(System.currentTimeMillis());

        int[] labels = new int[g.size];
        int[] newLabels = labels;
        int iter = 0;

        long time = System.nanoTime();
        CountDownLatch latch = null;

        ModComunityLPA[] runners = new ModComunityLPA[runner];

        for (int i = 0; i < runner; i++) {
            runners[i] = new ModComunityLPA(g, latch, labels, i, runner, seeds, seeds2);
        }

        ExecutorService ex = Executors.newFixedThreadPool(runner);

        do {
            iter++;
            labels = newLabels;
            newLabels = Arrays.copyOf(labels, labels.length);
            latch = new CountDownLatch(runner);

            for (int i = 0; i < runner; i++) {
                runners[i].barrier = latch;
                runners[i].labels = newLabels;
                ex.submit(runners[i]);
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                logger.debug(e);
            }

        } while (smoothEnd(labels, newLabels, iter, threshold));

        ex.shutdown();

        logger.info(((System.nanoTime() - time) / 1000000000d) + "\ts");
        return labels;
    }

    private static boolean smoothEnd(int[] labels, int[] newLabels, int iter, double threshold) {
        if (iter < 2) {
            return true;
        }

        int k = 3;

        if (iter > k) {
            int equality = 0;

            for (int i = 0; i < labels.length; i++) {
                if (labels[i] == newLabels[i]) {
                    equality++;
                }
            }
            double currentT = (equality / ((double) labels.length));

            return !(currentT >= threshold);
        }
        return !Arrays.equals(labels, newLabels);
    }
}