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

package input;

import core.TrainingPoint;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.TreeSet;

public class Input {
    private String FILE_NAME;
    private String SEPARATOR;
    private LinkedList<TrainingPoint> dataset;
    
    public Input(String filename, String separator){
        this.FILE_NAME = filename;
        this.SEPARATOR = separator;
        this.dataset = new LinkedList<TrainingPoint>();    
        loadDatasetInMainMemory();
    }

    private void loadDatasetInMainMemory(){
        readDatasetFromFile();
    }   
    
    private TreeSet<Integer> users;
    private TreeSet<Integer> items;
    private void readDatasetFromFile(){
        users = new TreeSet<Integer>();
        items = new TreeSet<Integer>();
        try {
            FileInputStream fis = new FileInputStream(new File(FILE_NAME));
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader in = new BufferedReader(isr);
            String line;
            while((line = in.readLine())!=null){
                String values[] = line.split(SEPARATOR);
                int user = Integer.parseInt(values[0]);
                int item = Integer.parseInt(values[1]);
                users.add(user);
                items.add(item);
                double aux = Double.parseDouble(values[2]);
                byte rating = (byte) aux;
                TrainingPoint t = new TrainingPoint(user,item,rating);
                dataset.add(t);
            }
            users.clear();
            items.clear();           
            in.close();
        } catch (IOException ex) {
            System.out.println("\nError: loadDatasetInMainMemory.\n\n");
            ex.printStackTrace();
            System.exit(-1);
        }   
    }
    
    public LinkedList<TrainingPoint> getDataset(){
        return dataset;
    }
}