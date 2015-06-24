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
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Test {
    private LinkedList<TrainingPoint> dataset_test;    
    private State state;
    private Globals GLOBALS;    

    public Test(LinkedList<TrainingPoint> dataset_test, State state, Globals GLOBALS) {
        this.dataset_test = dataset_test;
        this.state = state;
        this.GLOBALS = GLOBALS;
    }
    
    public double run(){
        int processors = Runtime.getRuntime().availableProcessors();
        ExecutorService executor=Executors.newFixedThreadPool(processors);
        int n = dataset_test.size();
        int subSize = n / processors + 1;
        RMSE_thread[] threads = new RMSE_thread[processors];
        for (int t = 0; t < processors; t++) {
            final int iStart = t * subSize;
            final int iEnd = Math.min((t + 1) * subSize, n);
            if (iEnd>=iStart){
                threads[t] = new RMSE_thread(dataset_test.subList(iStart, iEnd), state, GLOBALS.K);
                executor.execute(threads[t]);
            }
        }
        try { 
            executor.shutdown();
            executor.awaitTermination(60, TimeUnit.DAYS);
        } catch (InterruptedException ex) {System.out.println("InterruptedException "+ex);ex.printStackTrace();}        
        double RMSE = 0;
        for (int t = 0; t < processors; t++) {
//            System.out.println(t+" - "+threads[t].getMSE());
            RMSE += threads[t].getMSE();
        }
        RMSE /= n;
        RMSE = Math.sqrt(RMSE);
        return RMSE;
    }
}
