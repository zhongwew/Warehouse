package model;

import java.io.IOException;

public class Order {
	String order_id;
	String[] order_items;
	double order_weight;
	Order(String id, String orderStr){
		order_id = id;
		order_weight = 0;
		order_items = orderStr.split("\t");
	}
	Order(String orderStr){
		order_id = "0";//default number
		order_weight = 0;
		order_items = orderStr.split(",");
	}
	Order(String orderStr, double w,String id){
		order_id = id;//default number
		order_weight = w;
		System.out.println(orderStr);
		order_items = orderStr.split("\t");
	}
	void print() throws IOException {
		for(int i = 0; i<order_items.length ;i++)
			Simulation.bw.write(order_items[i]+",");
		Simulation.bw.write("\n");
	}
	int getLength() {
		return order_items.length;
	}
}
