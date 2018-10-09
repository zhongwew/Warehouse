package model;

import java.io.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;
import java.util.Vector;

public class Simulation {
	//data member of simulation
	public Vector<Order> orders;
	public Robot robot;
	public GridMap map;
	
	private BufferedReader bf;
	public static BufferedWriter bw = null;
	private static Scanner in;
	
	public Simulation(Coordinate startP,Coordinate endP,File initFile) throws IOException{
		//init the simulation environment
		orders = new Vector<Order>();
		robot = new Robot(startP,endP);
		double init_s_time  = System.currentTimeMillis();
		map = new GridMap(initFile,50,50);
		double init_e_time = System.currentTimeMillis();
		System.out.println("map init time: "+(init_e_time-init_s_time));
	}
	//read order from file
	void initOrders(File orderFile) throws IOException {
		bf = new BufferedReader(new FileReader(orderFile));
		String tempStr;
		int id_counter = 1;
		while((tempStr = bf.readLine()) != null) {
			orders.add(new Order(Integer.toString(id_counter), tempStr));
			id_counter++;
		}
		bf.close();
	}
	//read order from command line
	void initOrder(String orderStr) {
		orders.add(new Order(orderStr));
	}
	
	Vector<Coordinate> findPath(Coordinate sta,Coordinate des){
		//using A* algorithm to search path for one destination
		Vector<Coordinate> path = new Vector<Coordinate>();
		Vector<Coordinate> posMove = new Vector<Coordinate>();//used to store possible move,only frontier
		Vector<Coordinate> checkedMove = new Vector<Coordinate>(); //used to stored checked coordinate
		Coordinate curCor = sta;
		while(!(curCor.equal(des))) {//while robot hasn't reach the left/right side of destination
			Coordinate moveCor = null;
			int minDis = 10000;
			if(!addPossibleMove(curCor,posMove,checkedMove)) {//if current position has no possible moves
				//remove that coordination from path and put it into checked for further check
				path.remove(curCor);
				checkedMove.add(curCor);
			}
			for(int i = 0;i<posMove.size();i++) {
				//iterate possible move to find the one has minimum distance to des
				if(posMove.elementAt(i).distanceTo(des) < minDis) {
					minDis = posMove.elementAt(i).distanceTo(des);
					moveCor = posMove.elementAt(i);
				}
			}
			//make move and remove that coordination from possible moves
			path.add(moveCor);
			checkedMove.add(curCor);
			curCor = moveCor;
			posMove.remove(moveCor);
		}
		return path;
	}
	
	boolean addPossibleMove(Coordinate cur,Vector<Coordinate> pMove, Vector<Coordinate> cMove){
		//current coordinate, possible moves and checked moves
		Coordinate l = null;
		Coordinate r = null;
		Coordinate u = null;
		Coordinate d = null;
		boolean flag = false;//indicate if there is move available
		//check if coordinate exists and has been in possible moves and if the move can be made
		if((l = cur.leftPos()) != null && !pMove.contains(l)&& map.checkPass(l.pos_x, l.pos_y)&&!cMove.contains(l) ) {
			pMove.add(l);
			flag = true;
		}
		if((r = cur.rightPos()) != null && !pMove.contains(r) && map.checkPass(r.pos_x, r.pos_y)&&!cMove.contains(r)) {
			pMove.add(r);
			flag = true;
		}
		if((u = cur.upPos()) != null && !pMove.contains(u) && map.checkPass(u.pos_x, u.pos_y)&&!cMove.contains(u)) {
			pMove.add(u);
			flag = true;
		}
		if((d = cur.downPos()) != null && !pMove.contains(d) && map.checkPass(d.pos_x, d.pos_y)&&!cMove.contains(d)) {
			pMove.add(d);
			flag = true;
		}
		return flag;
	}
	
	
	int runOrder(Order order,int type,int time) throws IOException{//type means using which method to run order
		int result = 0;
		robot.pos_cur = robot.pos_start;//reset robot's position
		switch(type){
		case 0:
			//sequentially run order
			result = seqRun(order);
			break;
		case 1:
			//find nearst item in each iteration
			result = findMin(order);
			break;
		case 2:
			//find nearest item in different start points
			result = findMin_iter(order,time);
			break;
		case 3:
			result = branchAndbound(order);
			break;
		default: 
			break;
		}
		//stimulated annealing
		//todo
		//evolutionary algorithm
		//todo
		return result;
	}
	
	int findLowerBound(Order order) throws IOException {
		//implement Prim algorithm
		//1. start from start location to find the nearest neighbor, indicate both as checked
		//2. update frontier
		//3. go back to 1
		//if end point is the same as start point: 
		int lB = 0;
		Vector<Coordinate> checkedVec = new Vector<Coordinate>();
		Vector<Coordinate> vVec = new Vector<Coordinate>();
		int oLength = order.getLength();
		//pre-processing orders to get coordinates
		checkedVec.add(robot.pos_start);
		for(int i = 0; i<oLength; i++) {
			vVec.add(map.items.get(order.order_items[i]).pos); //push order items' coordinations into vector
		}
		//start
		while(!vVec.isEmpty()) {
			int min = 100000;
			int minNum = 0;
			for(int cur = 0; cur<checkedVec.size();cur++)
				for(int i = 0; i<vVec.size(); i++) {
					//to simplify, I use physical distance as priority
					Coordinate tempCor = findAccessCor(vVec.elementAt(i));
					if(tempCor.distanceTo(checkedVec.elementAt(cur)) < min) {
						min = tempCor.distanceTo(checkedVec.elementAt(cur));
						minNum = i;
					}
				}
			lB += min;
			checkedVec.add(vVec.elementAt(minNum));
			vVec.remove(minNum);
		}
		if(robot.pos_end.equal(robot.pos_start)) {
			//find second largest path
			int min = 10000;
			for(int i = 2; i< checkedVec.size(); i++) {
				Coordinate tempCor = findAccessCor(checkedVec.elementAt(i));
				if(tempCor.distanceTo(robot.pos_end) < min) {
					min = tempCor.distanceTo(robot.pos_end);
				}
			}
			lB += min;
		}
		else {
			int min = 10000;
			for(int i = 0; i< checkedVec.size(); i++) {
				Coordinate tempCor = findAccessCor(checkedVec.elementAt(i));
				if(tempCor.distanceTo(robot.pos_end) < min) {
					min = tempCor.distanceTo(robot.pos_end);
				}
			}
			lB += min;
		}
//		for(int i =0; i<checkedVec.size(); i++)
//			checkedVec.elementAt(i).print();
		return lB;
	}
	
	int seqRun(Order order) throws IOException{//run the order sequentially
		Vector<Coordinate> tempPath = null;
		int pathLength = 0;
		for(int i = 0; i< order.order_items.length; i++) {
			Coordinate tempCor = map.items.get(order.order_items[i]).pos;
			tempPath = findPath(robot.pos_cur,tempCor.leftPos());
			//printPath(tempPath);
			pathLength += tempPath.size();
			robot.pos_cur = tempCor.leftPos();
		}
		tempPath = findPath(robot.pos_cur,robot.pos_end);
		pathLength += tempPath.size();
		robot.pos_cur = robot.pos_start;//reset position
		return pathLength;
	}
	
	int findMin(Order order) throws IOException{
		//initialize the path vector
		Vector<Coordinate> minPath = new Vector<Coordinate>();
		Vector<Coordinate> curPath = new Vector<Coordinate>();
		String[] optOrder = new String[order.order_items.length];
		int pathLength = 0;
		//to indicate if item has been processed
		boolean[] processed = new boolean[order.order_items.length]; 
		
		for(int i = 0; i<order.order_items.length;i++) {
			//used to indicate length of path
			int minLength = 100000;
			int curLength = 0;
			//making one move
			int moveId = -1;
			Coordinate desPos = null;
			for(int j = 0;j<order.order_items.length;j++) {
				if(processed[j] == true) continue;
				//determine which item to pick
				Coordinate tempCor = map.items.get(order.order_items[j]).pos;
				//tempCor.print();
				//check both side of destination 
				curPath = findPath(robot.pos_cur,tempCor.leftPos());
				curLength = curPath.size();
				if(curLength < minLength) {//record the minimum one
					minLength = curLength;
					minPath = curPath;
					moveId = j;
					desPos = tempCor.leftPos();
				}
				curPath = findPath(robot.pos_cur,tempCor.rightPos());
				curLength = curPath.size();
				if(curLength < minLength) {//record the minimum one
					minLength = curLength;
					minPath = curPath;
					moveId = j;
					desPos = tempCor.rightPos();
				}
			}
			processed[moveId] = true;  //indicate item moveId has been processed
			//process
			robot.pos_cur = desPos; //move robot
			optOrder[i] = order.order_items[moveId];
			printPath(minPath);
			bw.write("get item " + optOrder[i]+ " at shelf "+ map.items.get(optOrder[i]).toString()+"\n");
			pathLength += minPath.size();
		}
		minPath = findPath(robot.pos_cur,robot.pos_end);
		printPath(minPath);
		pathLength += minPath.size();
		//print optimized order
		bw.write("optimized order: \n");
		for(int i = 0; i<optOrder.length; i++) {
			bw.write(optOrder[i] + ",");
		}
		bw.write("\n");
		
		return pathLength; //return the path of processing the whole order
	}

	int findMin_iter(Order order, int num) throws IOException{
		//limit the number of iterations it takes
		//initialize the path vector
		Vector<Coordinate> minPath = new Vector<Coordinate>();
		Vector<Coordinate> curPath = new Vector<Coordinate>();
		String[] f_optOrder = null;
		int minIteLength = 100000;
		double feffort = 0;
		if(order.order_items.length < num)
			num = order.order_items.length;//how many iterations we do
		
		//iterating process
		for(int it = 0; it < num; it++) {
			//start from different point
			//initialize data
			boolean[] processed = new boolean[order.order_items.length]; //to indicate if item has been processed
			double teffort = 0;
			double tweight = 0;
			int pathLength = 0;
			String[] optOrder = new String[order.order_items.length];
			//choose starting point(r/l)
			Coordinate startP = map.items.get(order.order_items[it]).pos;
			robot.pos_cur = findAccessCor(startP);//find which coordinate can be accessed
			optOrder[0] = order.order_items[it];//record the start point
			pathLength += robot.pos_start.distanceTo(robot.pos_cur);
			processed[it] = true;
			for(int i = 1; i<order.order_items.length; i++) {
				//used to indicate length of path
				int minLength = 100000;
				int curLength = 0;
				//making one move
				int moveId = -1;
				Coordinate desPos = null;
				//find nearest neighbor
				for(int j = 0;j<order.order_items.length;j++) {
					if(processed[j] == true) continue;
					//determine which item to pick
					Coordinate tempCor = map.items.get(order.order_items[j]).pos;
					//check both side of destination 
					curPath = findPath(robot.pos_cur,tempCor.leftPos());
					curLength = curPath.size();
					if(curLength < minLength) {//record the minimum one
						minLength = curLength;
						minPath = curPath;
						moveId = j;
						desPos = tempCor.leftPos();
					}
					curPath = findPath(robot.pos_cur,tempCor.rightPos());
					curLength = curPath.size();
					if(curLength < minLength) {//record the minimum one
						minLength = curLength;
						minPath = curPath;
						moveId = j;
						desPos = tempCor.rightPos();
					}
				}
				processed[moveId] = true;  //indicate item moveId has been processed
				//process
				robot.pos_cur = desPos; //move robot
				optOrder[i] = order.order_items[moveId];
				printPath(minPath);
				//bw.write("get item " + optOrder[i]+ " at shelf "+ map.items.get(optOrder[i])+"\n");
				pathLength += minPath.size();
				teffort += tweight*minPath.size();
				tweight += map.items.get(optOrder[i]).weight;
			}
			minPath = findPath(robot.pos_cur,robot.pos_end);
			printPath(minPath);
			pathLength += minPath.size();
			teffort += tweight * minPath.size();
			
			if(pathLength < minIteLength) {
				minIteLength = pathLength;
				f_optOrder = optOrder;
				feffort = teffort;
			}
		}
		//print optimized order
		bw.write("optimized order: \n");
		for(int i = 0; i<f_optOrder.length; i++) {
			bw.write(f_optOrder[i] + ",");
		}
		bw.write("\neffort of order"+order.order_id+" is: "+feffort);
		bw.write("\n");
		
		return minIteLength; //return the path of processing the whole order
	}
	
	TreeNode getUpperbound(TreeNode startnode,Vector<Coordinate> treenodes) throws IOException{
		TreeNode result = new TreeNode(startnode,treenodes);
		//the start has include path 0, the distance matrix has been reduced
		while(result.path.size() < treenodes.size()/2) {
			Vector<TreeNode> tempv = new Vector<TreeNode>();
			boolean[] expandflag = new boolean[treenodes.size()];
			for(int i = 0; i< expandflag.length; i++)
				expandflag[i] = false;
			for(int index : result.path) {
				//System.out.println("index: "+index);
				if(index == 0) continue; //skip the start node
				if(index %2 == 0) {
					expandflag[index] = true;
					expandflag[index-1] = true;
				}
				else {
					expandflag[index] = true;
					expandflag[index+1] = true;
				}
			}
			for(int i = 1; i< treenodes.size()-1; i++) {
				if(expandflag[i]) continue;
				tempv.add(new TreeNode(result,treenodes.elementAt(i),i,treenodes));
			}
			result = tempv.elementAt(0);
			for(TreeNode tn : tempv) {
				//find the oen with smallest bound as new result node
				if(tn.bound < result.bound)
					result = tn;
			}
		}
//		for(int i : result.path)
//			System.out.print(i+" ");
//		System.out.print("\ninitial upperboud:"+result.bound+"\n");
		return result;
	}
	
	int branchAndbound(Order order) throws IOException {
		int result = 0;
		int limittime = 10000;
		//preprocess the order: eliminate the items having the same location,
		//add the weight to one item from the same coordinate
		Vector<Item> order_loc = new Vector<Item>();
		for(int i = 0; i< order.order_items.length; i++) {
			boolean flag = false;
			Item tempitem = map.items.get(order.order_items[i]);
			for(int j = 0; j<order_loc.size();j++)
				if(order_loc.elementAt(j).pos.equal(tempitem.pos)) {
					flag = true;
					order_loc.elementAt(j).weight += tempitem.weight;
				}
			if(!flag)
				order_loc.addElement(tempitem);
		}
		//System.out.println("optimized order length: "+order_loc.size());
		//preprocess the eliminated order to get an initial upperbound using nearest neighbor
		
		int itemNum = (order_loc.size()+1)*2; //include start/end points and left/right coordinates for each item
		int [][] dis = new int[itemNum][itemNum];
		for(int i= 0; i<itemNum; i++)
			for(int j = 0; j<itemNum; j++)
				dis[i][j] = -1;//-1 indicate not find or not exist
		
		//preprocessing the data: making a distance matrix
		Vector<Coordinate> treenodes = new Vector<Coordinate>();
		treenodes.add(robot.pos_start);
		for(int i = 0; i< order_loc.size(); i++) {
			Coordinate left = order_loc.elementAt(i).pos.leftPos();
			if(left != null)
				treenodes.add(left);
			else
				treenodes.add(new Coordinate(-1,-1));//indicate the position not exists
			Coordinate right = order_loc.elementAt(i).pos.rightPos();
			if(right != null)
				treenodes.add(right);
			else
				treenodes.add(new Coordinate(-1,-1));
		}
		treenodes.add(robot.pos_end);
//		for(int i = 0; i< treenodes.size();i++) {
//			System.out.print(i+":");
//			treenodes.elementAt(i).print();
//		}
//		System.out.println("\n");
		for(int i = 0; i< itemNum; i++) {
			for(int j = 0; j<itemNum; j++) {
				if(i == j)
					continue; 
				if(dis[i][j] != -1)
					continue; //this distance has been found
				if((i %2 == 1 && i + 1 == j) || (i%2 == 0 && i-1 == j)) {
					//i and j are left and right of the same item
					dis[i][j] = -1;
					dis[j][i] = -1; 
					continue;
				}
				if((treenodes.elementAt(j).pos_x == -1 && treenodes.elementAt(j).pos_y == -1) || 
						(treenodes.elementAt(i).pos_x == -1 && treenodes.elementAt(i).pos_y == -1)) {
					//this coordination doesn't exist
					dis[i][j] = -1;
					dis[j][i] = -1;
				}
				dis[i][j] = findPath(treenodes.elementAt(i),treenodes.elementAt(j)).size();
				dis[j][i] = dis[i][j];
			}
		}
//		//print the matrix
//		for(int i = 0; i< itemNum; i++) {
//			for(int j = 0; j< itemNum; j++)
//				System.out.print(dis[i][j]+"\t");
//			System.out.print("\n");
//		}
//		System.out.println(" ");
		//start from the start point, choose the one with lowest lower bound
		Comparator<TreeNode> com = new Comparator<TreeNode>() {
			@Override
			public int compare(TreeNode o1, TreeNode o2) {
				if(o1.bound == o2.bound)
					return o2.path.size()-o1.path.size();
				return o1.bound - o2.bound;
			}
		};
		Queue<TreeNode> pqueue = new PriorityQueue<TreeNode>(com);
		
		
		TreeNode startnode = new TreeNode(dis, robot.pos_start,new Vector<Integer>());
		startnode.path.add(0);
		int lowerbound = TreeNode.initBound(dis, startnode,treenodes);
		startnode.setBound(lowerbound);
		TreeNode resultnode = getUpperbound(startnode,treenodes);
		int upperbound = resultnode.bound; //initialize it as a big value
		
		//System.out.println("initial lowerbound: "+lowerbound);
		//System.out.println("startnode expand:");
		for(int i = 1; i< treenodes.size()-1; i++) {
			TreeNode tempNode = new TreeNode(startnode,treenodes.elementAt(i),i,treenodes);
			if(tempNode.bound >= upperbound) continue;
			//System.out.print(":"+tempNode.bound+" ");
			pqueue.add(tempNode);
		}
		//System.out.println('\n');
		
		//start iteration
		double starttime = System.currentTimeMillis();
		while(!pqueue.isEmpty()) {
			TreeNode tempnode = pqueue.poll();
			//System.out.println("queue length:"+pqueue.size());
			if(System.currentTimeMillis() - starttime > limittime)
				break;
			if(tempnode.bound >= upperbound)
				break;
			//System.out.println(tempnode.bound + " upperbound: " + upperbound + " path length: "+tempnode.path.size());
			//check if the node is a leaf node
			if(tempnode.path.size() == order_loc.size()+1) {
				//System.out.println(tempnode.bound + " upperbound: " + upperbound + " path length: "+tempnode.path.size());
				resultnode = tempnode;
				upperbound = tempnode.bound;
				//iterate the queue, pop out nodes with higher bound
				Iterator<TreeNode> ite = pqueue.iterator();
				while(ite.hasNext()) {
					TreeNode checknode = ite.next();
					if(checknode.bound >= upperbound) {
						ite.remove();
					}
				}
				continue;
			}
			//expand the treenode with lowest cost, reduce the treenodes set
			//System.out.print("expand node ");
			//tempnode.item.print();
			//System.out.println("at depth: "+tempnode.path.size()+" weight: "+tempnode.bound);
			boolean[] expandflag = new boolean[treenodes.size()];
			for(int i = 0; i< expandflag.length; i++)
				expandflag[i] = false;
			for(int index : tempnode.path) {
				//System.out.println("index: "+index);
				if(index == 0) continue; //skip the start node
				if(index %2 == 0) {
					expandflag[index] = true;
					expandflag[index-1] = true;
				}
				else {
					expandflag[index] = true;
					expandflag[index+1] = true;
				}
			}
			for(int i = 1; i< treenodes.size()-1; i++) {
				if(expandflag[i]) continue;
				TreeNode newnode = new TreeNode(tempnode,treenodes.elementAt(i),i,treenodes);
				if(newnode.bound >= upperbound) continue;
				pqueue.add(newnode);
			}
			//System.out.println("\n");
		}
		//resultnode.printMatrix(treenodes);
		//System.out.println(resultnode.bound);
		
		double tweight = 0;
		double teffort = 0;
		//backtrack to get the real cost and roadcost
		Vector<Coordinate> t_path = new Vector<Coordinate>();
		for(int i = 0; i< resultnode.path.size(); i++) {
			if(i == resultnode.path.size()-1) {
				tweight += order_loc.elementAt((resultnode.path.elementAt(i)-1)/2).weight;
				Vector<Coordinate> path = findPath(treenodes.elementAt(resultnode.path.elementAt(i)),robot.pos_end);
				for(Coordinate c:path)
					t_path.add(c);
				int roadlength = path.size();
				result += roadlength;
				teffort += roadlength * tweight;
				break;
			}
			if(i == 0) 
				tweight = 0;
			else
				tweight += order_loc.elementAt((resultnode.path.elementAt(i)-1)/2).weight;
			Vector<Coordinate> path = findPath(treenodes.elementAt(resultnode.path.elementAt(i)),treenodes.elementAt(resultnode.path.elementAt(i+1)));
			for(Coordinate c:path)
				t_path.add(c);
			int roadlength = path.size();
			result += roadlength;//get the distance
			teffort += roadlength*tweight;
		}
		//System.out.println("cost: "+result);
		//System.out.println("total effort: "+teffort);
		bw.write(order.order_id+"\n");
		printPath(t_path);
		for(int i = 0; i< order.getLength(); i++) {
			bw.write(order.order_items[i]+":");
			map.items.get(order.order_items[i]).pos.print();
		}
		bw.write("\n"+result+"\n");
		bw.write(teffort+"\n");
		return result;
	}
	
	void splitOrders() {
		for(Order o : orders) {
			//get a order weight for every order
			for(int i = 0; i< o.order_items.length; i++) {
				o.order_weight += map.items.get(o.order_items[i]).weight;
			}
		}
		//iterate through orders, if the order is larger than worker's capacity, split it into half size
		for(int i = 0; i< orders.size(); i++){
			Order tempo = orders.elementAt(i);
			System.out.println(tempo.order_items.length);
			if(tempo.order_items.length == 1)
				continue;
			if(tempo.order_weight > robot.max_weight) {
				//split the order and put them into order vector
				String nstr1 = null;
				//String nstr2 = null;
				double nweight = 0;
				int subcounter = 1;
				for(String item: tempo.order_items) {
					if(nweight + map.items.get(item).weight > robot.max_weight) {
						if(nstr1 != null) 
							orders.add(new Order(nstr1,nweight,tempo.order_id+"_"+subcounter));
						nstr1 = item;
						nweight = map.items.get(item).weight;
						subcounter++;
					}
					else {
						if(nstr1 == null)
							nstr1 = item;
						else
							nstr1 = nstr1+"\t"+item;
						nweight += map.items.get(item).weight;
					}
				}
//				for(String item : tempo.order_items) {
//					if(nweight + map.items.get(item).weight <= tempo.order_weight/2+1) {
//						nweight += map.items.get(item).weight;
//						if (nstr1 == null) {
//							nstr1 = item;
//							continue;
//						}
//						nstr1 = nstr1+'\t'+item;
//					}
//					else {
//						if (nstr2 == null) {
//							nstr2 = item;
//							continue;
//						}
//						nstr2 = nstr2+'\t'+item;
//					}
//				}
				orders.add(new Order(nstr1,nweight,tempo.order_id+"_"+subcounter));
				//orders.add(new Order(nstr2,tempo.order_weight-nweight,tempo.order_id+"_2"));
			}
		}
		Iterator<Order> ite = orders.iterator();
		while(ite.hasNext()) {
			Order temp = ite.next();
			if(temp.order_weight > robot.max_weight) {
				ite.remove();
			}
		}
		//sort the orders from light to heavy 
		Collections.sort(orders, new Comparator<Order>() {
			@Override
			public int compare(Order o1, Order o2) {
				return (int)o1.order_weight - (int)o2.order_weight;
			}
		});
		//combine the small orders from the start, mark the id as "1+2"(order 1 is combined with order 2)
		Order tempo = null;
		boolean[] ditems = new boolean[orders.size()];
		for(boolean i : ditems)
			i = false;
		for(int i = 0; i< orders.size(); i++) {
			if(tempo == null)
				tempo = orders.elementAt(i);
			else if(tempo.order_weight + orders.elementAt(i).order_weight >= robot.max_weight/5 || tempo.order_items.length>8) {
				tempo = orders.elementAt(i);
			}
			else{
				tempo.order_id = tempo.order_id +"+"+ orders.elementAt(i).order_id;//combine id
				String[] tempItems = new String[tempo.order_items.length+orders.elementAt(i).order_items.length];
				for(int j = 0; j<tempItems.length; j++) {
					if(j < tempo.order_items.length)
						tempItems[j] = tempo.order_items[j];
					else
						tempItems[j] = orders.elementAt(i).order_items[j-tempo.order_items.length];
				}
				tempo.order_items = tempItems; //combine order items
				tempo.order_weight += orders.elementAt(i).order_weight;
				ditems[i] = true;
			}
		}
		//delete the combined orders
		int counter = 0;
		ite = orders.iterator();
		while(ite.hasNext()) {
			Order temp = ite.next();
			if(ditems[counter]) {
				ite.remove();
			}
			counter++;
		}
		for(Order o : orders) {
			System.out.println(o.order_id +"'s weight is "+o.order_weight);
		}
	}
	
	
	Coordinate findAccessCor(Coordinate cor) {
		if(cor.leftPos() == null)
			return cor.rightPos();
		else if(cor.rightPos() == null)
			return cor.leftPos();
		else if(cor.leftPos().distanceTo(robot.pos_start) > cor.rightPos().distanceTo(robot.pos_start))
			return cor.rightPos();
		else
			return cor.leftPos();
	}
	
//	void appendPath(Vector<Coordinate> path1, Vector<Coordinate> path2) {
//		for(int i = 0;i<path2.size();i++) {
//			path1.add(path2.elementAt(i));
//		}
//	}
	
	void printPath(Vector<Coordinate> path) throws IOException {
//		bw.write("optimized path: \n");
//		if(path.size() == 0) {
//			bw.write("the same shelf\n");
//			return;
//		}
		for(int i = 0;i<path.size();i++) {
			path.elementAt(i).print();
		}
		bw.write("\n");
	}

	public static void batch_run(String sstr, String estr, File inputF, boolean flag, int max_weight) throws IOException {
		String outfile = "/Users/wang/Desktop/output_test.txt";
		String itemdimension = "/Users/wang/Documents/courses/CS221/item-dimensions-tabbed.txt";
		bw = new BufferedWriter(new FileWriter(new File(outfile)));
		bw.write(sstr+"\t"+estr+"\n");
		Simulation sim = new Simulation(new Coordinate(sstr),new Coordinate(estr),new File("/Users/wang/Documents/courses/CS221/warehouse-grid.csv"));
		sim.map.loadDimensions(new File(itemdimension));
		sim.initOrders(inputF);
		sim.robot.setMaxweight(max_weight);
		if(flag)
			sim.splitOrders();
		double s_time = System.currentTimeMillis();
		//process batch file
		for(int i = 0; i< sim.orders.size();i++) {
			sim.branchAndbound(sim.orders.elementAt(i));
			System.out.println("order "+(i+1)+" complete");
		}
		//sim.branchAndbound(sim.orders.elementAt(8));
		double e_time = System.currentTimeMillis();
		System.out.println("process time: " +(e_time-s_time));
		bw.close();
	}
	
	public static void online_run(String sstr, String estr, String orderstr) throws IOException {
		String outfile = "/Users/wang/Desktop/tempoutput.txt";
		String itemdimension = "/Users/wang/Documents/courses/CS221/item-dimensions-tabbed.txt";
		bw = new BufferedWriter(new FileWriter(new File(outfile)));
//test use code
		bw.write(sstr+"\t"+estr+"\n");
		Simulation sim = new Simulation(new Coordinate(sstr),new Coordinate(estr),new File("/Users/wang/Documents/courses/CS221/warehouse-grid.csv"));
		sim.map.loadDimensions(new File(itemdimension));
		double s_time = System.currentTimeMillis();
		sim.branchAndbound(new Order(orderstr));
		double e_time = System.currentTimeMillis();
		System.out.println("process time: " +(e_time-s_time));
		bw.close();
	}

}
