package model;

import java.io.IOException;
import java.util.Vector;

public class TreeNode {
	int [][] dis;
	Coordinate item;
	int item_num; //used to identify the number of item in matrix
	int bound;
	Vector<Integer> path; //record the access order of items
	TreeNode(int [][] d, Coordinate i, Vector<Integer> p){
		dis = d;
		item = i;
		path = p;
		bound = 0;
	}
	TreeNode(TreeNode node,Vector<Coordinate> treenodes){
		dis = new int[treenodes.size()][treenodes.size()];
		for(int i = 0; i< treenodes.size();i++)
			for(int j = 0; j<treenodes.size();j++)
				dis[i][j] = node.dis[i][j];
		item = node.item;
		item_num = node.item_num;
		path = new Vector<Integer>();
		for(Integer i : node.path)
			path.add(i);
		bound = node.bound;
	}
	
	void setBound(int b) {
		bound = b;
	}
	
	void setNum(int n) {
		item_num = n;
	}
	
	TreeNode(TreeNode parent, Coordinate des, int des_num, Vector<Coordinate> treenodes) throws IOException{
		//initialize data members
		item_num = des_num;
		this.bound = parent.bound;
		this.item = des;
		this.path = new Vector<Integer>();
		for(int i = 0; i< parent.path.size(); i++)
			this.path.add(parent.path.elementAt(i));
		this.path.add(des_num);
		this.dis = new int[treenodes.size()][treenodes.size()];
		for(int i = 0; i< treenodes.size(); i++)
			for(int j = 0; j< treenodes.size(); j++)
				this.dis[i][j] = parent.dis[i][j];//copy the parent's distance matrix
		
		bound += dis[parent.item_num][des_num];
		//System.out.println(dis[parent.item_num][des_num]);
		//process the distance matrix
		//cross the source row
		if(parent.item_num == 0)
			for(int j = 0; j<treenodes.size(); j++) 
				dis[parent.item_num][j] = -1;
		else {//make both left/right to be infinity
			for(int j = 0; j<treenodes.size(); j++) {
				dis[parent.item_num][j] = -1;
				if(parent.item_num % 2 == 1)
					dis[parent.item_num+1][j] = -1;
				else
					dis[parent.item_num-1][j] = -1;
			}
		}
//		for(int i = 0; i< treenodes.size(); i++) {
//			//cross out the source row
//			if(parent.item.equal(treenodes.elementAt(i))) {
//				if(i == 0)
//					for(int j = 0; j<treenodes.size(); j++) {
//						if(des.equal(treenodes.elementAt(j))) {
//							des_pos = j;
//							bound += dis[i][j];
//						}
//						dis[i][j] = -1;
//					}
//				else {//make both left/right to be infinity
//					for(int j = 0; j<treenodes.size(); j++) {
//						if(des.equal(treenodes.elementAt(j))) {
//							des_pos = j;
//							System.out.println("destinaiton distance: "+dis[i][j]);
//							bound += dis[i][j];
//						}
//						dis[i][j] = -1;
//						if(i%2 == 1)
//							dis[i+1][j] = -1;
//						else
//							dis[i-1][j] = -1;
//					}
//				}
//				break;
//			}
//		}
		
			//cross out the destination column
		for(int j = 0; j<treenodes.size(); j++) {
			dis[j][des_num] = -1;
			if(des_num %2 == 1)
				dis[j][des_num+1] = -1;
			else
				dis[j][des_num-1] = -1;
		}
		

		//calculate the new bound
		for(int i = 0; i< treenodes.size(); i++) {
			int min = 10000;
			//find the minimum value of this column
			for(int j = 0; j< treenodes.size(); j++) {
				if(dis[i][j] < 0 || (j == i+1 && i%2 == 1) || (j == i-1 && i %2 ==0 && i != 0)) continue;
				if(dis[i][j] < min)
					min = dis[i][j];
			}
			for(int j = 0; j< treenodes.size(); j++) {
				if(dis[i][j] < 0) continue;
				dis[i][j] = dis[i][j] - min;
			}
			if(min == 10000)
				min = 0;
			bound += min;
		}
		for(int i = 0; i< treenodes.size(); i++) {
			int min = 10000;
			//find the minimum value of this column
			for(int j = 0; j< treenodes.size(); j++) {
				if(dis[j][i] < 0 || (j == i+1 && i%2 == 1) || (j == i-1 && i %2 ==0 && i != 0)) continue;
				if(dis[j][i] < min)
					min = dis[j][i];
			}
			for(int j = 0; j< treenodes.size(); j++) {
				if(dis[j][i] < 0) continue;
				dis[j][i] = dis[j][i] - min;
			}
			if(min == 10000)
				min = 0;
			bound += min;
		}
		
//		//print the matrix
//		for(int i = 0; i< treenodes.size(); i++) {
//			for(int j = 0; j< treenodes.size(); j++)
//				System.out.print(dis[i][j]+"\t");
//			System.out.print("\n");
//		}
//		System.out.println("\n");
//		
	}
	
	static int initBound(int [][] dis, TreeNode node, Vector<Coordinate> treenodes) {
		int b = 0;
//		boolean[] skipnodes = new boolean[treenodes.size()];
//		for(int i = 0; i<treenodes.size(); i++)
//			skipnodes[i] = false;
		//process the matrix, skip the columns and rows that have been visited
//		if(!node.parentsNodes.isEmpty()) {
//			for(int i = 0; i < treenodes.size();i++) {
//				for (int j = 0; j< node.parentsNodes.size(); j++)
//					if(node.parentsNodes.elementAt(j).equal(treenodes.elementAt(i))) {
//						skipnodes[i] = true;
//						continue;
//					}
//			}
//		}
		
		for(int i = 0; i<treenodes.size(); i++)
			dis[i][0] = -1;		
		
		if(dis[0][treenodes.size()-1] == 0) {//if destination is the same as start point
			dis[0][treenodes.size()-1] = -1;
			dis[treenodes.size()-1][0] = -1;
		}
		for(int i = 0; i< treenodes.size(); i++) {
			int min = 10000;
			//find the minimum value of this column
			for(int j = 0; j< treenodes.size(); j++) {
				//if(dis[i][j] < 0 || (j == i+1 && i%2 == 1) || (j == i-1 && i %2 ==0 && i != 0)) continue;
				if(dis[i][j] < 0) continue;
				if(dis[i][j] < min)
					min = dis[i][j];
			}
			if(min == 10000) continue;
			for(int j = 0; j< treenodes.size(); j++) {
				if(dis[i][j] < 0) continue;
				dis[i][j] = dis[i][j] - min;
			}
			b += min;
		}
//		
//		//print the matrix
//		for(int i = 0; i< treenodes.size(); i++) {
//			for(int j = 0; j< treenodes.size(); j++)
//				System.out.print(dis[i][j]+"\t");
//			System.out.print("\n");
//		}
//		System.out.println("\n");
//		
		for(int i = 0; i< treenodes.size(); i++) {
			int min = 10000;
			//find the minimum value of this column
			for(int j = 0; j< treenodes.size(); j++) {
				//if(dis[j][i] < 0 || (j == i+1 && i%2 == 1) || (j == i-1 && i %2 ==0 && i != 0)) continue;
				if(dis[j][i] < 0) continue;
				if(dis[j][i] < min)
					min = dis[j][i];
			}
			if(min == 10000) continue;
			for(int j = 0; j< treenodes.size(); j++) {
				if(dis[j][i] < 0) continue;
				dis[j][i] = dis[j][i] - min;
			}
			b += min;
		}
		
		
//		//print the matrix
//		for(int i = 0; i< treenodes.size(); i++) {
//			for(int j = 0; j< treenodes.size(); j++)
//				System.out.print(dis[i][j]+"\t");
//			System.out.print("\n");
//		}
		return b;
	}
	
	void printMatrix(Vector<Coordinate> treenodes) {
		//print the matrix
		for(int i = 0; i< treenodes.size(); i++) {
			for(int j = 0; j< treenodes.size(); j++)
				System.out.print(dis[i][j]+"\t");
			System.out.print("\n");
		}
		System.out.println("\n");
	}
	
}

