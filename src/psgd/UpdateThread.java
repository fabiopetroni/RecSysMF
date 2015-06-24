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
import java.util.LinkedList;
import java.util.List;

public class UpdateThread implements Runnable{

    private List<TrainingPoint> list;
    private State state;
    private int K; //number of features
    private double Lambda;
    private double mu;

    public UpdateThread(List<TrainingPoint> list, State state, int K, double Lambda, double mu) {
        this.list = list;
        this.state = state;
        this.K = K;
        this.Lambda = Lambda;
        this.mu = mu;
    }
    
    @Override
    public void run() {
        int sleep = 2;
        LinkedList<TrainingPoint> pending = new LinkedList<TrainingPoint>();
        
        for (TrainingPoint x : list){
            int user = x.user;
            int item = x.item;
            double rating = x.rating;
            
            Profile user_profile = state.getUserProfile(user);
            Profile item_profile = state.getItemProfile(item);
            if (user_profile.getLock()){
                if (item_profile.getLock()){
                    //WE HAVE THE LOCKS
                    this.update(user_profile,item_profile,rating);
                    item_profile.releaseLock();
                } else{ pending.add(x); }
                user_profile.releaseLock();
            } else{ pending.add(x); }
        }
        
        //OPTIMISTIC LIST OF PENDING TRANSACTIONS
        for (TrainingPoint y : pending){
            int user = y.user;
            int item = y.item;
            double rating = y.rating;
            Profile user_profile = state.getUserProfile(user);
            Profile item_profile = state.getItemProfile(item);
            sleep = 2; while (!user_profile.getLock()){ try{ Thread.sleep(sleep); }catch(Exception e){} sleep = (int) Math.pow(sleep, 2); }
            sleep = 2; while (!item_profile.getLock()){ try{ Thread.sleep(sleep); }catch(Exception e){} sleep = (int) Math.pow(sleep, 2); }
            this.update(user_profile,item_profile,rating);
            item_profile.releaseLock();
            user_profile.releaseLock();
        }
    }
    
    private void update(Profile user_profile, Profile item_profile , double rating){
        double prediction = Profile.dotproduct(user_profile, item_profile,K);
        double err = rating - prediction;
        
        for (int k = 0; k < K; k++) {
            double userFeature = user_profile.vector[k];
            double itemFeature = item_profile.vector[k];
            
            double user_std_increment = err * itemFeature;
            double item_std_increment = err * userFeature;
            double user_regularization_increment = Lambda * userFeature;
            double item_regularization_increment = Lambda * itemFeature;
            double user_increment = mu * (user_std_increment - user_regularization_increment);
            double item_increment = mu * (item_std_increment - item_regularization_increment);
            
            user_profile.vector[k] += user_increment;
            item_profile.vector[k] += item_increment;
            
            //CHECK IF THE USER VECTOR OVERFLOWS
            if (Double.isNaN(user_profile.vector[k]) || user_profile.vector[k]==Double.POSITIVE_INFINITY || user_profile.vector[k]==Double.NEGATIVE_INFINITY){
                long id = Thread.currentThread().getId();
                System.out.println("T"+id+": ERROR: user_profile.vector["+k+"]="+user_profile.vector[k]);
                System.out.println("T"+id+": old_user_profile.vector["+k+"]="+userFeature);
                System.out.println("T"+id+": err="+err);
                System.out.println("T"+id+": increment="+user_increment);
                System.out.println("T"+id+": user_std_increment="+user_std_increment);
                System.out.println("T"+id+": user_regularization_increment="+user_regularization_increment);
                System.out.println("T"+id+": itemFeature="+itemFeature);
                System.out.println();
                System.exit(-1);
            }
            
            //CHECK IF THE ITEM VECTOR OVERFLOWS
            if (Double.isNaN(item_profile.vector[k]) || item_profile.vector[k]==Double.POSITIVE_INFINITY || item_profile.vector[k]==Double.NEGATIVE_INFINITY){
                long id = Thread.currentThread().getId();
                System.out.println("T"+id+": ERROR: item_profile.vector["+k+"] Nan");
                System.out.println("T"+id+": old_item_profile.vector["+k+"]="+itemFeature);
                System.out.println("T"+id+": err="+err);
                System.out.println("T"+id+": increment="+item_increment);
                System.out.println("T"+id+": item_std_increment="+item_std_increment);
                System.out.println("T"+id+": item_regularization_increment="+item_regularization_increment);
                System.out.println("T"+id+": userFeature="+userFeature);
                System.out.println();
                System.exit(-1);
            }
        }  
    }
}
