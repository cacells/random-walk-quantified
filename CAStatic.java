/*
 * This is a class that will call the CAGrid simulation and will display the results 
 * graphically in a window for the CA version of the model
 */


import java.awt.*;

import javax.swing.*;
import java.awt.event.*;
import java.awt.geom.GeneralPath;

import java.util.*;
 
public class CAStatic extends JFrame implements Runnable, ActionListener {

    CAGridStatic experiment;
    int[][] savedvals;
    int maxRun = 200;
    int[] savedd = new int[maxRun];
    int[] saveddsq = new int[maxRun];
    int runCount = 0;
    int epsCount = 0;
    int newframe = 0;
    Random rand = new Random();    
	volatile Thread runner;
	Image backImg1;
	Graphics backGr1;
	CAImagePanel CApicture;
	CAImagePanel CApicture2;
	JButton startBtn,writeBtn,paramsBtn,wrapBtn;
	JTextArea msgBtn;
	JPanel buttonHolder;
	int scale = 20;
	int iterations;
	int gSize;
	int maxCellType;
	int maxit = 10;
	boolean started = false;
    Colour palette = new Colour();
	int[] colorindices = {0,1,2,54,4,5};
	int nnw = colorindices.length-1;
//    Color[] colours = {Color.white,Color.black,Color.green,Color.blue,Color.yellow,Color.red,Color.pink};
    Color[] javaColours;
    double[][] epsColours;
    
	public CAStatic(int size) {

	    gSize=size;
		//experiment = new CAGridStatic(size, maxC);
	    int tint = (int)Math.ceil((double)(400*maxit)/(double)gSize)+(480-384);

		//add 20 to x and 60 to y bcos not printing onto the full frame
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container mainWindow = getContentPane();
		mainWindow.setLayout(new BorderLayout());
		setSize(400,tint);

		buttonHolder = new JPanel();
		buttonHolder.setLayout(new GridLayout(2,2));
  
		
	    SpinnerNumberModel model3 = new SpinnerNumberModel(50, 0, 100, 5);
	    JSpinner spinner3 = new JSpinner(model3);
        writeBtn = new JButton("Output Postscript");
        writeBtn.addActionListener(this);
 	
        startBtn = new JButton("Start");
        startBtn.addActionListener(this);  
        
        paramsBtn = new JButton("Set Probabilities");
        paramsBtn.addActionListener(this);
        
        wrapBtn = new JButton("Toggle wrap");
        wrapBtn.addActionListener(this);
        
        buttonHolder.add(writeBtn);
		writeBtn.setVisible(false);
        buttonHolder.add(paramsBtn);
        buttonHolder.add(wrapBtn);
        buttonHolder.add(startBtn);
        
        mainWindow.add(buttonHolder,BorderLayout.SOUTH);
		
        msgBtn = new JTextArea(" Default Parameter Values: "+CAGridStatic.params);
        msgBtn.setEditable(false);

        
	    mainWindow.add(msgBtn,BorderLayout.NORTH);
	    
        CApicture = new CAImagePanel();
        mainWindow.add(CApicture,BorderLayout.CENTER);

        
		//not here - doesn't work: CApicture.setScale(gSize,maxit,scale);


//pack();

 
		setVisible(true);
		setpalette();
		iterations = 0;
		savedvals = new int[gSize][maxit];
		
	}
	
    public void setpalette(){
    	int ind = colorindices.length;
    	javaColours = new Color[ind];
    	epsColours = new double[ind][3];
    	for (int i=0;i<ind;i++){
    		System.out.println("color index "+colorindices[i]);
    		javaColours[i] = palette.chooseJavaColour(colorindices[i]);
    		epsColours[i] = palette.chooseEPSColour(colorindices[i]);
    	}
    }

	
	public void saveCA() {
		int a;
		int xv,yv;
		yv = iterations;
		//backGr1.fillRect(0, 0, this.getSize().width, this.getSize().height);
		for (CACell c : experiment.tissue){
			xv = c.home.x;
			a = c.lineage;
			savedvals[xv][yv] = a;
			if (c.type == 1){
				experiment.savedx[iterations] = c.home.x;
				experiment.savedy[iterations] = iterations;
			}
		}
	}
	
	public void saveStats() {
        int val;
		//backGr1.fillRect(0, 0, this.getSize().width, this.getSize().height);
		for (CACell c : experiment.tissue){
			if (c.type == 1){
				val = c.home.x - 31;
				savedd[runCount] = val;
				saveddsq[runCount] = val*val;
			}
		}
	}
	
	public void showStats(){
		int sumd = 0,sumdsq = 0;
		int maxd = 0,mind=gSize-1;
		for (int i=0;i<maxRun;i++){
			sumd = sumd + savedd[i];
			sumdsq = sumdsq + saveddsq[i];
			if (savedd[i] < mind) mind = savedd[i];
			if (savedd[i] > maxd) maxd = savedd[i];
		}
		System.out.println("av d: "+sumd/maxRun+" av d sq "+((float)sumdsq)/((float)maxRun));
		System.out.println("range of d: "+ mind + " to " + maxd);
	}
	
	public void outputEPS(){
	  epsCount++;
	  String probstring = CAGridStatic.params.filename();	  
	  postscriptPrint("CA"+iterations+"."+probstring+"."+epsCount+".eps");
	}
	
	public void changeParameters(){
		CAGridStatic.params.SetParamVals();
	    System.out.println("param vals: "+CAGridStatic.params);
	    msgBtn.setText(" Parameter Values: "+CAGridStatic.params);
	}
	
	public void changeWrap(){
		CAGridStatic.params.nowrap = !CAGridStatic.params.nowrap;
	}
	
	public void drawCA() {
        int a;
      	//CApicture.clearCAPanel();
		for (CACell c : experiment.tissue){
			a = c.lineage;
			if (a > 0) a = (a-1)%nnw+1;
			if(a<7){
				CApicture.drawCircleAt(c.home.x,iterations,javaColours[a]);
			}else{
				CApicture.drawCircleAt(c.home.x,iterations,Color.orange);
			}
		}
	    CApicture.updateGraphic();
	}
	public void drawLines() {
      	//CApicture.clearCAPanel();
		for (CACell c : experiment.tissue){
			if ((iterations > 0) && (c.type == 1))
				CApicture.drawALine(experiment.savedx[iterations-1],iterations-1,
						experiment.savedx[iterations],iterations,javaColours[1]);
		}
	    CApicture.updateGraphic();
	}

	public void start() {
        //initialise();
        started = true;
		if (runner == null) {
			runner = new Thread(this);
		}
		runner.start();//not the same as this method!
		startBtn.setText("Stop");
		writeBtn.setVisible(false);
		paramsBtn.setVisible(false);
	}
	public void stop(){
		started = false;
		runner = null;

		startBtn.setText("Start");
		writeBtn.setVisible(true);
		paramsBtn.setVisible(true);
	}
	public void actionPerformed(ActionEvent e){
		if(e.getSource() == startBtn){
			if(startBtn.getText()=="Start"){
				if (started) {
//					paramsBtn.setText("change parameters and rerun");
//					started = false;
				}
				start();
			}else{
				stop();

			}	
			}
		else if(e.getSource() == writeBtn){
             outputEPS();	
			}
		else if(e.getSource() == paramsBtn){
			changeParameters();
			start();

			}
		else if(e.getSource() == wrapBtn){
			changeWrap();
		}
	}
	
	public void run() {


        for (runCount=0;runCount<maxRun;runCount++){
        initialise();
		saveCA();
		while ((iterations < maxit) && (runner == Thread.currentThread())) {
			
			experiment.iterate();
			saveCA();
			//drawCA();
			if ((runCount%10) == 0) drawLines();
			iterations++;
			//newframe = 0;		
			//while(newframe<500000) newframe++;
			//if((iterations%5)==0)postscriptPrint("CA"+iterations+".eps");
			// This will produce a postscript output of the tissue
		}
        saveStats();
        }
		//this will print out aborted results
		//to stop that check if maxit was achieved
        showStats();
        stop();
	}


	public void postscriptPrint(String fileName) {
		int xx;
		int yy;
		int a;
		int state;
		int xsize = gSize*4;
		int ysize = (int)Math.ceil((double)(iterations*xsize)/(double)gSize) + 30;
		xsize = xsize + 30;
		System.out.println("ysize = "+ysize);
		boolean flag;
		double[] col = new double[3];//tmp colour holder
		try {
			java.io.FileWriter file = new java.io.FileWriter(fileName);
			java.io.BufferedWriter buffer = new java.io.BufferedWriter(file);
			System.out.println(fileName);
			buffer.write("%!PS-Adobe-2.0 EPSF-2.0");
			buffer.newLine();
			buffer.write("%%Title: test.eps");
			buffer.newLine();
			buffer.write("%%Creator: gnuplot 4.2 patchlevel 4");
			buffer.newLine();
			buffer.write("%%CreationDate: Thu Jun  4 14:16:00 2009");
			buffer.newLine();
			buffer.write("%%DocumentFonts: (atend)");
			buffer.newLine();
			buffer.write("%%BoundingBox: 0 0 "+xsize+" "+ysize);
			buffer.newLine();
			buffer.write("%%EndComments");
			buffer.newLine();
			for (xx = 0;xx < nnw+1; xx++){
                col = epsColours[xx];
                buffer.write("/sc"+xx+" {"+col[0]+" "+col[1]+" "+col[2]+" setrgbcolor} bind def\n");
			}

			buffer.write("/dodot {/yval exch def /xval exch def newpath xval yval 1.5 0 360 arc fill} def\n");
			//for (CACell c : experiment.tissue){
			for (xx = 0; xx < gSize; xx++) {
				for (yy = 0; yy < iterations; yy++) {
					a = savedvals[xx][yy];
					if (a > 0) {//don't print the white ones
						a = (a-1)%nnw+1;
						buffer.write("sc"+a+"\n");
					    buffer.write( (xx*4+20) + " " + (ysize-yy*4-20) + " dodot\n");	
					}
				}
			}

/*				if(c.type>0){
					xx = (c.home.x * 4) + 20;
					yy = (c.home.y * 4) + 20;
					if (c.canStay) {
						buffer.write("newpath " + xx + " " + yy + " 1.5 0 360 arc fill\n");
						buffer.write("0 setgray\n");
						buffer.write("newpath " + xx + " " + yy + " 1.5 0 360 arc  stroke\n");
					} else {
						buffer.write("0.75 setgray\n");
						buffer.write("newpath " + xx + " " + yy + " 1.5 0 360 arc fill\n");
					}
				}*/
//			}
			buffer.write("showpage");
			buffer.newLine();
			buffer.write("%%Trailer");
			buffer.newLine();
			buffer.write("%%DocumentFonts: Helvetica");
			buffer.newLine();
			buffer.close();
		} catch (java.io.IOException e) {
			System.out.println(e.toString());
		}
	}

    public void initialise(){
		experiment = new CAGridStatic(gSize,maxit);
		if (runCount < 1) CApicture.setScale(gSize,maxit,scale);
		iterations=0;

	}
    
	public static void main(String args[]) {


		double initalSeed = 0.1;
		if(args.length>0){
			initalSeed = Double.parseDouble(args[0]);
			CAStatic s = new CAStatic(64);
/*	        s.initialise();
			s.start();*/
		}else{
			CAStatic s = new CAStatic(64);
/*	        s.initialise();
			s.start();*/
			System.out.println("finished");//one thread gets here
		}
	}


	
	}



