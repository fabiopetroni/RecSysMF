# RecSysMF
Matrix Factorization for Recommender System with Parallel Stochastic Gradient Descent (with locks).

If you use RecSysMF please cite the following paper:
-  F. Petroni and L. Querzoni:
   "GASGD: stochastic gradient descent for distributed asynchronous matrix completion via graph partitioning."
   In: Proceedings of the 8th ACM Conference on Recommender systems (RecSys), 2014.


To run the project from the command line, go to the dist folder and type the following:

java -Xmx§GB -jar RecSysMF.jar inputtrainfile [options]

Parameters:
 - §: number of GB for the java virtual machine
 - inputfile: the name of the file that stores the <user,item,rating> triples for train.

Options:
 - -separator string    ->      specifies the separator between user, item and rating in the input file . Default '\t'.
 - -test string ->  the name of the file that stores the <user,item,rating> triples for test.
 - -output string   ->      specifies the name of the file where the output (the loss) will be stored.
 - -iterations integer  ->      specifies how many iterations to be performed by the sgd algorithm. Default 30.
 - -lambda double       ->      specifies the regularization parameter for the sgd algorithm. Default 0.05.
 - -learning_rate double        ->      specifies the learning rate for the sgd algorithm. Default 0.01.
 - -rank integer        ->      specifies the number of latent features for the low rank approximation. Default 50.

# Example

download the MovieLens 100k dataset from http://grouplens.org/datasets/movielens/ and put the folder ml-100k in the root of the project.

java -jar dist/RecSysMF.jar ml-100k/u1.base -test ml-100k/u1.test -output ml-100k/output_loss.dat -iterations 100 -lambda 0.01 -learning_rate 0.05 -rank 100

