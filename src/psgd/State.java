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

import java.util.HashMap;

public class State {
    private HashMap<Integer,Profile> user_map;
    private HashMap<Integer,Profile> item_map;
    private int K;

    public State(int K) {
        this.K = K;
        user_map = new HashMap<Integer,Profile>();
        item_map = new HashMap<Integer,Profile>();
    }
    
    public synchronized Profile getUserProfile(int user){
        if (!user_map.containsKey(user)){
            user_map.put(user, new Profile(K));
        }
        return user_map.get(user);
    }
    
    public synchronized Profile getItemProfile(int item){
        if (!item_map.containsKey(item)){
            item_map.put(item, new Profile(K));
        }
        return item_map.get(item);
    }
}
