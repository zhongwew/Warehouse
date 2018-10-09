package model;

import java.util.Vector;

public class Robot {
	double max_weight;
	Coordinate pos_start;
	Coordinate pos_end;
	Coordinate pos_cur;
	Robot(Coordinate start,Coordinate end){
		max_weight = 50;//initial to be a big number
		pos_start = start;
		pos_end = end;
		pos_cur = start;
	}
	void setMaxweight(double w) {
		max_weight = w;
	}

}
