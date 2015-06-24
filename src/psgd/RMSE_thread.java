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

import core.TrainingPoint;
import java.util.List;

public class RMSE_thread implements Runnable{

    private List<TrainingPoint> list;
    private State state;
    private int K;
    private double MSE;
    private boolean executed;
    
    public RMSE_thread(List<TrainingPoint> list, State state, int K) {
        this.list = list;
        this.state = state;
        this.K = K;
        this.MSE = 0;
        this.executed = false;
    }

    @Override
    public void run() {
        for (TrainingPoint x : list){
            int user = x.user;
            int item = x.item;
            double rating = x.rating;            
            Profile user_profile = state.getUserProfile(user);
            Profile item_profile = state.getItemProfile(item);
            double prediction = Profile.dotproduct(user_profile, item_profile,K);
            double err = rating - prediction;
            MSE += Math.pow(err, 2);
        }
        executed = true;
    }
    
    public double getMSE(){
        if (!executed){
            System.out.println("ERROR: thread still runnning;");
            System.exit(-1);
        }
        return MSE;
    }    
}