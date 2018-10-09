package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class GridMap {
	//data member of Map
	public static int size_x,size_y;
	public static boolean[][] gridStat; // used to determine if grid can be passed
	Map<String,Item> items;  //shelf stores items
	//initialize the map
	GridMap(File inputF,int x,int y) throws IOException{
		size_x = x;
		size_y = y;
		BufferedReader reader = new BufferedReader(new FileReader(inputF));
		gridStat = new boolean[x][y];
		for(int i = 0;i<x;i++)
			for(int j = 0;j<y;j++)
				if(i%2 == 0 && j%2 == 0)
					gridStat[i][j] = false;
				else
					gridStat[i][j] = true;
		items = new HashMap<String, Item>();
		//init the grid from file
		String tempStr;
		while((tempStr = reader.readLine())!=null) {
			String[] pars = tempStr.split(",");
			double pos_x = Double.parseDouble(pars[1]);
			double pos_y = Double.parseDouble(pars[2]);
			//get round value
			if((pos_x%1)>=0.5) pos_x += 0.5;
			if((pos_y%1)>=0.5) pos_y += 0.5;
			items.put(pars[0], new Item(new Coordinate((int)pos_x*2,(int)pos_y*2)));//position * 2 to create path	
			gridStat[(int) pos_x*2][(int) pos_y*2] = false;  //can not pass through shelves
		}
		//printMap();
	}
	
	public void loadDimensions(File inputF) throws IOException {
		if(gridStat == null) {
			System.out.println("you need to init map first");
			return;
		}
		BufferedReader reader = new BufferedReader(new FileReader(inputF));
		String tempstr = reader.readLine();//skip the headline
		while((tempstr = reader.readLine()) != null) {
			String[] args = tempstr.split("\t");
			Item tempitem = items.get(args[0]);
			if(tempitem == null) continue;
			tempitem.setDimensions(Double.parseDouble(args[1]), Double.parseDouble(args[2]), Double.parseDouble(args[3]), Double.parseDouble(args[4]));
		}
	}
	
	boolean checkPass(int x,int y) {
		return gridStat[x][y];
	}
	void printMap() {
		for(int i = 0;i<size_x;i++) {
			for(int j = 0; j<size_y; j++) {
				if(gridStat[i][j]) 
					System.out.print("o ");
				else 
					System.out.print("* ");
			}
			System.out.print("\n");
		}
	}
}
