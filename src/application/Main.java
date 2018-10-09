package application;
	
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import model.*;

public class Main extends Application {
	
	Simulation sim = null;
	final String outfile = "/Users/wang/Desktop/output_test.txt";
	final String orderstr = "/Users/wang/Documents/courses/CS221/warehouse-orders-v02-tabbed.txt";
	final String itemdimension = "/Users/wang/Documents/courses/CS221/item-dimensions-tabbed.txt";
	Ellipse startp = null;
	Ellipse endp = null;
	Coordinate sC = null;
	Coordinate eC = null;
	
	Path mpath = null;
	
	public void parsePath(String path,Group root) {
		//draw start and end point
		startp = new Ellipse();
		endp = new Ellipse();
		startp.setCenterX(500-390+sC.getX()*20);
		startp.setCenterY(50+(20*sC.getY()));
		startp.setRadiusX(15);
		startp.setRadiusY(15);
		startp.setFill(Color.DARKORANGE);
		
		endp.setCenterX(500-390+eC.getX()*20);
		endp.setCenterY(50+20*eC.getY());
		endp.setRadiusX(15);
		endp.setRadiusY(15);
		endp.setFill(Color.DARKCYAN);
		//draw path
		String[] moves = path.split(" ");
		mpath = new Path();
		mpath.setStrokeWidth(5);
		String startC = moves[0];
		double startX = Double.parseDouble(startC.substring(1, startC.indexOf(",")));
		double startY = Double.parseDouble(startC.substring(startC.indexOf(',')+1,startC.length()-1));
		mpath.getElements().add(new MoveTo(500-390+(startX*20),50+(20*startY)));
		for(int i = 1; i<moves.length;i++) {
			String movestr = moves[i];
			double moveX = Double.parseDouble(movestr.substring(1, movestr.indexOf(",")));
			double moveY = Double.parseDouble(movestr.substring(movestr.indexOf(',')+1,movestr.length()-1));
			mpath.getElements().add(new LineTo(500-390+(moveX*20),50+(20*moveY)));
		}
		root.getChildren().add(mpath);
		root.getChildren().add(startp);
		root.getChildren().add(endp);
	}
	Label[] nls = null;
	public void parseItems(String is, Group root, Rectangle[][] grids) {
		String[] items = is.split(" ");
		int hcounter = 0;
		nls = new Label[items.length];
		for(String item : items) {
			String cor = item.substring(item.indexOf(":")+1,item.length());
			System.out.println(cor);
			int CordX = Integer.parseInt(cor.substring(1, cor.indexOf(",")));
			int CordY = Integer.parseInt(cor.substring(cor.indexOf(',')+1,cor.length()-1));
			grids[CordY/2][CordX/2].setFill(Color.CORNFLOWERBLUE);
			nls[hcounter] = new Label(item.substring(0,item.indexOf(":"))+":<"+(CordX/2+1)+","+ (CordY/2+1)+">");
			nls[hcounter].setLayoutX(500+120);
			nls[hcounter].setLayoutY(485+hcounter*20);
			root.getChildren().add(nls[hcounter]);
			hcounter++;
		}
	}
	Label cost_lab = null;
	public void parseCost(String is, Group root) {
		cost_lab = new Label();
		cost_lab.setLayoutX(790);
		cost_lab.setLayoutY(485);
		cost_lab.setText(is);
		root.getChildren().add(cost_lab);
	}
	Label effort_lab = null;
	public void parseEffort(String is, Group root) {
		effort_lab = new Label();
		effort_lab.setLayoutX(790);
		effort_lab.setLayoutY(575);
		effort_lab.setText(is);
		root.getChildren().add(effort_lab);
	}
	public void loadBatchFile(File bfile, Group root, Rectangle[][] rgroup) throws IOException {
		if(!bfile.exists()) return;
		BufferedReader bfr = new BufferedReader(new FileReader(bfile));
		//skip the headline
		bfr.readLine();
		String templine = null;
		int linecounter = 0;
		ArrayList<String> itemId = new ArrayList<String>();
		while((templine = bfr.readLine()) != null) {
			if(linecounter%5 == 0) {
				itemId.add(templine);
			}
			linecounter ++;
		}
		final ChoiceBox<String> box = new ChoiceBox<String>(FXCollections.observableArrayList(itemId));
		box.setLayoutX(40);
		box.setLayoutY(455);
		box.setTooltip(new Tooltip("select order"));
		box.setValue("English");
		box.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				try {
					resetGrid(root,rgroup);
					System.out.println(newValue.intValue());
					readBatchFile(bfile,newValue.intValue(),root,rgroup);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		root.getChildren().add(box);
	}
	
	void resetGrid(Group root, Rectangle[][] rgroup) {
		if(nls != null) {
			for(Label l : nls)
				root.getChildren().remove(l);
		}
		if(mpath != null)
			root.getChildren().remove(mpath);
		if(effort_lab != null)
			root.getChildren().remove(effort_lab);
		if(cost_lab != null)
			root.getChildren().remove(cost_lab);
		if(startp!= null) {
			root.getChildren().remove(startp);
			root.getChildren().remove(endp);
		}
		for(int i=0; i<10; i++)
			for(int j=0;j<20;j++)
				rgroup[i][j].setFill(Color.BROWN);
	}
	
	public void readBatchFile(File bfile, int order_num, Group root, Rectangle[][] grids) throws IOException {
		if(!bfile.exists()) return;
		System.out.println(order_num);
		BufferedReader bfr = new BufferedReader(new FileReader(bfile));
		//skip the headline
		String[] se_C = bfr.readLine().split("\t");
		sC = new Coordinate(se_C[0]);
		eC = new Coordinate(se_C[1]);
		int linecounter = 0;
		//get order id
		while((bfr.readLine()) != null) {
			if(linecounter/5 == order_num) {
				parsePath(bfr.readLine(),root);
				parseItems(bfr.readLine(),root,grids);
				parseCost(bfr.readLine(),root);
				parseEffort(bfr.readLine(),root);
				break;
			}
			linecounter++;
		}
	}
	@Override
	public void start(Stage primaryStage) {
		try {
			primaryStage.setTitle("Warehouse");
			//BorderPane root = new BorderPane();
			//why can't use borderpane here? todo
			Group root = new Group();
			Scene scene = new Scene(root,1000,800);
			Rectangle grid_back = new Rectangle();
			grid_back.setLayoutX(scene.getWidth()/2-430);
			grid_back.setLayoutY(10);
			grid_back.setWidth(840);
			grid_back.setHeight(425);
			grid_back.setFill(Color.ALICEBLUE);
			root.getChildren().add(grid_back);
			//draw the axis
			Text[] axis_x = new Text[20];
			for(int i = 0; i< 20; i++) {
				axis_x[i] = new Text();
				axis_x[i].setText(Integer.toString(i+1));
				axis_x[i].setX(scene.getWidth()/2-400+(i*40));
				axis_x[i].setY(30);
				root.getChildren().add(axis_x[i]);
			}
			final Label[] axis_y = new Label[10];
			for(int i = 0; i< 10; i++) {
				axis_y[i] = new Label();
				axis_y[i].setText(Integer.toString(i+1));
				axis_y[i].setLayoutY(45+(i*40));
				axis_y[i].setLayoutX(scene.getWidth()/2-415);
				root.getChildren().add(axis_y[i]);
			}
			//draw the items grid
			Rectangle[][] rgroup = new Rectangle[10][20];
			for(int i = 0; i< 10; i++)
				for(int j = 0; j< 20; j++) {
					rgroup[i][j] = new Rectangle();
					rgroup[i][j].setHeight(20);
					rgroup[i][j].setWidth(20);
					rgroup[i][j].setX(scene.getWidth()/2-400+(j*40));
					rgroup[i][j].setY(40+(i*40));
					rgroup[i][j].setFill(Color.BROWN);
					root.getChildren().add(rgroup[i][j]);
				}
			
			//control buttons
			int start_y = 455;
			int start_x = 40;
			//order info panel
			Rectangle oinfo = new Rectangle();
			oinfo.setLayoutX(scene.getWidth()/2+100);
			oinfo.setLayoutY(455);
			oinfo.setWidth(300);
			oinfo.setHeight(300);
			oinfo.setFill(Color.CORNSILK);
			root.getChildren().add(oinfo);
			Label orderl = new Label("Order Information:");
			orderl.setLayoutX(scene.getWidth()/2+120);
			orderl.setLayoutY(465);
			root.getChildren().add(orderl);
			
			//display effort and cost
			final Label clabel = new Label("cost: ");
			clabel.setLayoutX(scene.getWidth()/2+270);
			clabel.setLayoutY(465);
			final Label elabel = new Label("effort: ");
			elabel.setLayoutX(scene.getWidth()/2+270);
			elabel.setLayoutY(555);
			root.getChildren().add(clabel);
			root.getChildren().add(elabel);
			
//			//draw the choose toggle button to choose between command line and file
//			ToggleGroup tg = new ToggleGroup();
//			ToggleButton tb1 = new ToggleButton("Real-time");
//			tb1.setToggleGroup(tg);
//			tb1.setSelected(true);
//			ToggleButton tb2 = new ToggleButton("Batch processing");
//			tb2.setToggleGroup(tg);
//			tb1.setUserData("batch");
//			tb2.setUserData("realtime");
//			tb1.setLayoutY(start_y);
//			tb2.setLayoutY(start_y);
//			tb1.setLayoutX(start_x);
//			tb2.setLayoutX(80+start_x);
//			root.getChildren().add(tb1);
//			root.getChildren().add(tb2);
//			tg.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
//				@Override
//				public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
//					if(newValue == null)
//						oinfo.setVisible(false);
//					else
//						oinfo.setVisible(true);
//				}
//			});
			
			//tb1: set the file loader
			
			TextField startinput = new TextField();
			TextField endinput = new TextField();
			startinput.setLayoutX(start_x+100);
			startinput.setLayoutY(start_y+50);
			endinput.setLayoutX(start_x+100);
			endinput.setLayoutY(start_y+100);
			Label stlab = new Label("start location");
			stlab.setLayoutX(start_x);
			stlab.setLayoutY(start_y+50);
			Label enlab = new Label("end location");
			enlab.setLayoutX(start_x);
			enlab.setLayoutY(start_y+100);
			
			final RadioButton split_btn = new RadioButton("split orders");
			split_btn.setLayoutX(start_x+350);
			split_btn.setLayoutY(start_y+140);
			final TextField weight = new TextField();
			weight.setLayoutX(start_x+350);
			weight.setLayoutY(start_y+170);
			weight.setText("20");
			
			final Button load_btn = new Button();
			load_btn.setLayoutX(start_x+100);
			load_btn.setLayoutY(start_y+150);
			load_btn.setText("run batch");
			load_btn.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					FileChooser fc = new FileChooser();
					File loadf = fc.showOpenDialog(primaryStage);
					//create a button to load data
					try {
						boolean flag = false;
						if(split_btn.isSelected())
							flag = true;
						Simulation.batch_run(startinput.getText(),endinput.getText(),loadf,flag,Integer.parseInt(weight.getText()));						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			final Button batch_btn = new Button();
			batch_btn.setLayoutX(start_x+200);
			batch_btn.setLayoutY(start_y+150);
			batch_btn.setText("load batch file");
			batch_btn.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					FileChooser fc = new FileChooser();
					File batfile = fc.showOpenDialog(primaryStage);
					try {
						loadBatchFile(batfile,root,rgroup);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			
			
			final Label rlab = new Label("real time order");
			rlab.setLayoutX(start_x);
			rlab.setLayoutY(start_y+200);
			final TextField rinput = new TextField();
			rinput.setLayoutX(start_x+150);
			rinput.setLayoutY(start_y+200);
			final Button rbtn = new Button();
			rbtn.setLayoutX(start_x+100);
			rbtn.setLayoutY(start_y+250);
			rbtn.setText("real time process");
			rbtn.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					try {
						resetGrid(root,rgroup);
						Simulation.online_run(startinput.getText(), endinput.getText(), rinput.getText());
						readBatchFile(new File("/Users/wang/Desktop/tempoutput.txt"),0,root,rgroup);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			
			root.getChildren().add(rinput);
			root.getChildren().add(rlab);
			root.getChildren().add(rbtn);
			root.getChildren().add(batch_btn);
			root.getChildren().add(load_btn);
			root.getChildren().add(split_btn);
			root.getChildren().add(startinput);
			root.getChildren().add(endinput);
			root.getChildren().add(stlab);
			root.getChildren().add(enlab);
			root.getChildren().add(weight);
			
			
			
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
