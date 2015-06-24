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

public class ComputeLossThread implements Runnable{
    private List<TrainingPoint> list;
    private double Lambda;
    private State state;
    private int K;
    
    private double result;
    private boolean task_complete;

    public ComputeLossThread(List<TrainingPoint> list, double Lambda, State state, int K) {
        this.list = list;
        this.Lambda = Lambda;
        this.state = state;
        this.K = K;
        
        result = 0;
        task_complete = false;
    }
    
    @Override
    public void run() {
        for (TrainingPoint x : list){
            int user = x.user;
            int item = x.item;
            double rating = x.rating;
            Profile user_profile = state.getUserProfile(user);
            Profile item_profile = state.getItemProfile(item);
            result += localLoss(user_profile, item_profile , rating); 
        }
        task_complete = true;
    }
    
    public double getResult(){
        if (!task_complete){
            System.out.println("ERROR: task not complete!");
            System.exit(-1);
        }
        return result;
    }
    
    private double localLoss(Profile user_profile, Profile item_profile , double rating){
        double err = rating - dotproduct(user_profile,item_profile);
        double local_loss = Math.pow(err, 2);
        local_loss /= 2;

        double regularization = Lambda;
        regularization /=2;
        regularization *= (FrobeniusNorm(user_profile) + FrobeniusNorm(item_profile));

        local_loss += regularization;
        return local_loss;
    }
    
    private double dotproduct(Profile user_profile, Profile item_profile) {
        double sum = 0;
        for (int k = 0; k < K; k++) {
            sum += user_profile.vector[k] * item_profile.vector[k];
        }
        return sum;
    }  
    
    private double FrobeniusNorm(Profile profile){
        double sum = 0;
        for (int k = 0; k < K; k++) {
            sum += Math.pow(profile.vector[k],2);
        }
        return sum;
    }
}
