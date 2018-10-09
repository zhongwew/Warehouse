package model;

public class Item {
	Coordinate pos;
	double weight;
	double height;
	double length;
	double width;
	Item(Coordinate cor){//construct item from String, e.g. "1, 1, 0"
		//set dimensions to default values
		weight = 0;
		height = 0;
		length = 0;
		width = 0;
		pos = cor;
	}
	void setDimensions(double l, double wi, double h,  double we) {
		weight = we;
		width = wi;
		length = l;
		height = h;
	}
}
