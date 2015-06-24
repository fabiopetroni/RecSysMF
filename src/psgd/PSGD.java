// Copyright (C) 2015 Fabio Petroni
// Contact:   http://www.fabiopetroni.com
//
// This file is part of RecSysMF application.
//
// RecSysMF is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// RecSysMF is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with RecSysMF.  If not, see <http://www.gnu.org/licenses/>.
//
// If you use RecSysMF please cite the following paper:
// - Fabio Petroni and Leonardo Querzoni (2014): GASGD: stochastic gradient descent for  
//   distributed asynchronous matrix completion via graph partitioning.
//   In Proceedings of the 8th ACM Conference on Recommender systems (RecSys), 2014.

package psgd;

import application.Globals;
import core.TrainingPoint;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PSGD {
    
    private LinkedList<TrainingPoint> dataset_train;
    private Globals GLOBALS;    
    private State state;
    private double mu;
    private boolean PRINT_LOSS;

    public PSGD(Globals GLOBALS, LinkedList<TrainingPoint> dataset) {
        this.dataset_train = dataset;
        this.GLOBALS = GLOBALS;
        this.mu = GLOBALS.init_mu;
        this.state = new State(GLOBALS.K);
        this.PRINT_LOSS = (GLOBALS.OUTPUT_LOSS_FILE!=null);        
    }

    
    public State run(){
        BufferedWriter bw = null;
        if (PRINT_LOSS){
            try {
                File file = new File(GLOBALS.OUTPUT_LOSS_FILE); 
                if (!file.exists()) { file.createNewFile();}
                FileWriter fw = new FileWriter(file.getAbsoluteFile());
                bw = new BufferedWriter(fw);
            } catch (Exception ex) {
                System.out.println("ERRORE "+ex);
                ex.printStackTrace();
                System.exit(-1);
            } 
        }        
        double old_loss = Double.POSITIVE_INFINITY;
        try {      
            System.out.println("\tepoch\tloss");
            for (int e = 0; e<GLOBALS.iterations; e++){
                Collections.shuffle(dataset_train);
                runEpoch();
                double loss = computeLoss();
                System.out.println("\t"+e+"\t"+loss);
                BoldDriver(old_loss,loss);
                old_loss = loss;                
                if (PRINT_LOSS){ bw.write(e+" "+loss+"\n"); }
            }
        } catch (Exception ex) { ex.printStackTrace(); System.exit(-1); }
        if (PRINT_LOSS){ 
            try {
                bw.close(); 
            } catch (Exception ex) {
                System.out.println("ERRORE "+ex);
                ex.printStackTrace();
                System.exit(-1);
            }
        }
        return state;
    }
    
    public void runEpoch(){
        int processors = Runtime.getRuntime().availableProcessors();
        ExecutorService executor=Executors.newFixedThreadPool(processors);
        int n = dataset_train.size();
        int subSize = n / processors + 1;
        //System.out.println(dataset_train.size()); //DEBUG
        for (int t = 0; t < processors; t++) {
            final int iStart = t * subSize;
            final int iEnd = Math.min((t + 1) * subSize, n);
            if (iEnd>=iStart){
                Runnable x = new UpdateThread(dataset_train.subList(iStart, iEnd),state, GLOBALS.K, GLOBALS.Lambda, mu);
                executor.execute(x);
            }
        }
        try { 
            executor.shutdown();
            executor.awaitTermination(60, TimeUnit.DAYS);
        } catch (InterruptedException ex) {System.out.println("InterruptedException "+ex);ex.printStackTrace();}
    }
    
    public double computeLoss(){
        int processors = Runtime.getRuntime().availableProcessors();
        ExecutorService executor=Executors.newFixedThreadPool(processors);
        ComputeLossThread[] theads = new ComputeLossThread[processors];
        int n = dataset_train.size();
        int subSize = n / processors + 1;
        
        for (int t = 0; t < processors; t++) {
            final int iStart = t * subSize;
            final int iEnd = Math.min((t + 1) * subSize, n);
            if (iEnd>=iStart){
                //System.out.println(dataset_train.subList(iStart, iEnd).size()); //DEBUG
                theads[t] = new ComputeLossThread(dataset_train.subList(iStart, iEnd), GLOBALS.Lambda, state, GLOBALS.K);
                executor.execute(theads[t]);
            }
        }
        //System.out.println(); //DEBUG
        try { 
            executor.shutdown();
            executor.awaitTermination(60, TimeUnit.DAYS);
        } catch (InterruptedException ex) {System.out.println("InterruptedException "+ex);ex.printStackTrace();}
        
        double loss = 0;
        for (int t = 0; t < processors; t++) { loss += theads[t].getResult(); }
        return loss;
    }
    
    /* BOLD_DRIVER: 
     * Starting from an initial step size mu0, we 
     * (1) increase the step size by a small percentage (say, 5%) whenever the loss decreased after one epoch, and 
     * (2) drastically decrease the step size (say, by 50%) if the loss increased. 
     * Within each epoch, the step size remains fixed. 
     * To obtain mu0, we try different step sizes on a small sample (say, 0.1%) of Z and pick the one that works best.
     */
    public void BoldDriver(double old_loss, double new_loss){
        if (old_loss>new_loss){ mu *= 1.05; }
        else{ mu *= 0.5; }
    }
    
}
