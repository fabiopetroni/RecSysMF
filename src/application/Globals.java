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

public class Globals {    
    //mandatory SIMULATOR PARAMETERS
    public String INPUT_TRAIN_FILE_NAME;    
    
    //optional SIMULATOR PARAMETERS
    public String INPUT_FILE_SEPARATOR = "\t";  
    
    //optional SGD PARAMETERS
    public int K = 50; //hidden features
    public double Lambda = 0.05; //regularization factor (0.05 NETFLIX, 1 YAHOO)
    public double init_mu = 0.01; //lerning rate   (0.015 NETFLIX, 0.0001 YAHOO)
    public int iterations = 30; //number of iterations
    
    //optional OUTPUT definition
    public String OUTPUT_LOSS_FILE;    
    public String INPUT_TEST_FILE_NAME;
    
    public Globals(String[] args){
        parse_arguments(args);
    }
    
    private void parse_arguments(String[] args){
        try{
            INPUT_TRAIN_FILE_NAME = args[0];
            for(int i=1; i < args.length; i+=2){
                if(args[i].equalsIgnoreCase("-separator")){
                    INPUT_FILE_SEPARATOR = args[i+1];
                }             
                else if(args[i].equalsIgnoreCase("-iterations")){
                    iterations = Integer.parseInt(args[i+1]);
                }
                else if(args[i].equalsIgnoreCase("-lambda")){
                    Lambda = Double.parseDouble(args[i+1]);
                }
                else if(args[i].equalsIgnoreCase("-learning_rate")){
                    init_mu = Double.parseDouble(args[i+1]);
                }
                else if(args[i].equalsIgnoreCase("-rank")){
                    K = Integer.parseInt(args[i+1]);
                }
                else if(args[i].equalsIgnoreCase("-output")){
                    OUTPUT_LOSS_FILE = args[i+1];                    
                }
                else if(args[i].equalsIgnoreCase("-test")){
                    INPUT_TEST_FILE_NAME = args[i+1];                    
                }
                else throw new IllegalArgumentException();
            }
        } catch (Exception e){
            System.out.println("\nInvalid arguments ["+args.length+"]. Aborting.\n");
            System.out.println("Usage:\n MatrixFactorization inputfile [options]\n");
            System.out.println("Parameters:");
            System.out.println(" inputfile: the name of the file that stores the <user,item,rating> triples for train.");
            System.out.println("\nOptions:");
            System.out.println(" -separator string");
            System.out.println("\t specifies the separator between user, item and rating in the input file . Default '\\t'.");
            System.out.println(" -test string");
            System.out.println("\t the name of the file that stores the <user,item,rating> triples for test.");
            System.out.println(" -output string");
            System.out.println("\t specifies the name of the file where the output (the loss) will be stored.");
            System.out.println(" -iterations integer");
            System.out.println("\t specifies how many iterations to be performed by the sgd algorithm. Default 30.");
            System.out.println(" -lambda double");
            System.out.println("\t specifies the regularization parameter for the sgd algorithm. Default 0.05.");
            System.out.println(" -learning_rate double");
            System.out.println("\t specifies the learning rate for the sgd algorithm. Default 0.01.");
            System.out.println(" -rank integer");
            System.out.println("\t specifies the number of latent features for the low rank approximation. Default 50.");
            System.out.println();
            System.exit(-1);
        }   
    }
    
    public void print(){
        System.out.println(this.toString());
    }
    
    @Override
    public String toString(){
        String s = "";
        s+="\ttrain: "+INPUT_TRAIN_FILE_NAME+"\n";
        s+="\tseparator: '"+INPUT_FILE_SEPARATOR+"'"+"\n";
        s+="\toutput: "+OUTPUT_LOSS_FILE+"\n";
        s+="\ttest: "+INPUT_TEST_FILE_NAME+"\n";
        s+="\titerations: "+iterations+"\n";
        s+="\tlambda: "+Lambda+"\n";
        s+="\tlearning_rate: "+init_mu+"\n";
        s+="\trank: "+K+"\n";
        return s;
    }
}
