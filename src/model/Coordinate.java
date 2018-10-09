package model;

import java.io.IOException;

public class Coordinate {
	int pos_x;
	int pos_y;
	Coordinate(int x, int y){
		pos_x = x;
		pos_y = y;
	}
	public Coordinate(String str){
		String[] strs = str.split(",");
		pos_x = Integer.parseInt(strs[0]);
		pos_y = Integer.parseInt(strs[1]);
	}
	public int getX() {
		return pos_x;
	}
	public int getY() {
		return pos_y;
	}
	int distanceTo(Coordinate des) {
		if(des.pos_x == -1 && des.pos_y == -1)
			return -1;
		return Math.abs(des.pos_x-this.pos_x)+Math.abs(des.pos_y-this.pos_y);
	}
	boolean equal(Coordinate des) {
		if(pos_x == des.pos_x && pos_y == des.pos_y) 
			return true;
		return false;
	}
	Coordinate leftPos() {
		if(pos_x == 0) return null;
		return new Coordinate(pos_x-1,pos_y);
	}
	Coordinate rightPos() {
		if(pos_x == GridMap.size_x) return null;
		return new Coordinate(pos_x+1,pos_y);
	}
	Coordinate downPos() {
		if(pos_y == 0) return null;
		return new Coordinate(pos_x,pos_y-1);
	}
	Coordinate upPos() {
		if(pos_y == GridMap.size_y) return null;
		return new Coordinate(pos_x,pos_y+1);
	}
	void print() throws IOException {
		Simulation.bw.write("<"+pos_x+","+pos_y+"> ");
		//System.out.print("<"+pos_x+","+pos_y+"> ");
	}
	public String toString() {
		return new String("<"+pos_x+","+pos_y+">");
	}
}
