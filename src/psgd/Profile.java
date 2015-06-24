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

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Profile {
    public double [] vector;
    private AtomicInteger lock;
    
    public Profile(int features) {
        vector = new double[features];
        Random random = new Random();
        for (int f = 0; f < features; f++) { 
            // The starting points were chosen by taking i.i.d. samples from the Uniform(-0.5,0.5) distribution
            //nextDouble() Returns the next pseudorandom, uniformly distributed double value between 0.0 and 1.0 from this random number generator's sequence.
            vector[f] = random.nextDouble() - 0.5; 
        }
        lock = new AtomicInteger(1);
    } 
    
    public boolean getLock(){
        return lock.compareAndSet(1, 0);
    }
    
    public boolean releaseLock(){
        return lock.compareAndSet(0, 1);
    }
    
    protected static double dotproduct(Profile user_profile, Profile item_profile, int K) {
        double sum = 0;
        for (int k = 0; k < K; k++) {
            sum += user_profile.vector[k] * item_profile.vector[k];
        }
        return sum;
    }  
}
