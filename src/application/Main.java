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

package application;

import core.TrainingPoint;
import input.Input;
import java.util.LinkedList;
import psgd.PSGD;
import psgd.State;
import psgd.Test;

public class Main {
    
    public static void main(String [] args){
        long TIME_0 = System.currentTimeMillis();
        
        System.out.println("\n-----------------------------------------------------------------");
        System.out.println(" RecSysMF: Matrix Factorization for Recommender System\n with Parallel Stochastic Gradient Descent (with locks).\n");
        System.out.println(" author: Fabio Petroni (http://www.fabiopetroni.com)");
        System.out.println("-----------------------------------------------------------------\n");
        Globals GLOBALS = new Globals(args);
        System.out.println("    Parameters:");
        GLOBALS.print();
        System.out.print("\n Loading input into main memory... ");
        //YahooInput input_train = new YahooInput(GLOBALS);
        Input input_train = new Input(GLOBALS.INPUT_TRAIN_FILE_NAME, GLOBALS.INPUT_FILE_SEPARATOR);        
                 
        LinkedList<TrainingPoint> dataset_train = input_train.getDataset();
        
        long TIME_1 = System.currentTimeMillis();        
        long time = TIME_1-TIME_0;
        time /= 1000; //sec
        System.out.println((int) time +" seconds");   
        
        System.out.println("\n Running Parallel Stochastic Gradient Descent... ");
        PSGD sgd = new PSGD(GLOBALS, dataset_train);
        State state = sgd.run();
        
        long TIME_2 = System.currentTimeMillis();
        time = TIME_2-TIME_1;
        time /= 1000; //sec
        System.out.println("     ..."+(int) time +" seconds");
        
        if (GLOBALS.INPUT_TEST_FILE_NAME!=null){
            System.out.print("\n Computing RMSE... ");
            Input input_test = new Input(GLOBALS.INPUT_TEST_FILE_NAME, GLOBALS.INPUT_FILE_SEPARATOR);     
            LinkedList<TrainingPoint> dataset_test = input_train.getDataset();
            Test test = new Test(dataset_test, state, GLOBALS);
            double RMSE = test.run();
            
            long TIME_3 = System.currentTimeMillis();        
            time = TIME_3-TIME_2;
            time /= 1000; //sec
            System.out.println((int) time +" seconds");   
            
            System.out.println("\n\t RMSE = "+RMSE);
        }
    }
}
