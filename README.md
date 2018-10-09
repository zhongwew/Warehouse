# Warehouse
It's a simulated warehouse where users could choose items and routes will be generated automatically.

#Introduction

This is a simulated warehouse developed using JAVA. 

Warehouse is a space with numerous shelves which loaded different items. Each item has an item ID which help workers to locate them(just like an Amazon warehouse).

Users(workers) could choose different orders which contain multiple items located in different positions of this warehouse. The system could generate an efficient route to fetch all the items efficiently.

The GUI is built up using JAVA FX.

It's a real-world application of Travelling Salesman Problem.

#Algorithm

The core of this system is that I implemented multiple algorithm to support route generating. 

##Nearest Neighbor

Intuitively, Nearest Neighbor(NN) is to find the nearest items every time. It could certainly give a solution no matter how complicated the order is. However, sometimes the solution is not efficient enough.

##Branch and Bound

Branch and Bound(BB) is a heuristic algorithm which set an upperbound and lowerbound for the order. While searching through the nodes in the network, the system will keep updating the lower bound until meet the upperbound.

This is a better method to find optimal solution rather than NN. However, while handling larger network and orders, the efficiency becomes the bottleneck of this algorithm.

