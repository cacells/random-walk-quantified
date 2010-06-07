
import java.awt.*;

import javax.swing.*;


class CAImagePanel extends JPanel {

	    Image backImg;
	    Graphics backGr;
		int colums, rows;
		int xScale, yScale;
		BasicStroke wideStroke = new BasicStroke(5.0f);
		Graphics2D g2;

		public void setScale(int noColums,int noRows, int scale)
		{
			colums = noColums;
			rows = noRows;
			xScale = scale;
			yScale = scale;
			//System.out.println("here"+xScale*colums+" "+yScale*rows);
			backImg= createImage(xScale*colums,yScale*rows);
			//backImg= createImage(20*64,2000);
			//System.out.println("here"+backImg);
			backGr= backImg.getGraphics();
			g2 = (Graphics2D) backGr;
			g2.setStroke(wideStroke);
		}
		
		public void clearCAPanel()
		{
				backGr.setColor(Color.orange);
				backGr.fillRect(0,0,this.colums*xScale,rows*yScale);
				//backGr.fillRect(0,0,20*64,2000);
		}
		public void clearCAPanel(int minx,int miny,int maxx,int maxyy)
		{
				backGr.setColor(Color.orange);
				backGr.fillRect(minx*xScale,miny*yScale,maxx*xScale,maxyy*yScale);
				//backGr.fillRect(0,0,20*64,2000);
		}
		
		public void drawCircleAt(int x, int y, Color colour)
		{
			backGr.setColor(colour);
			backGr.fillOval(x*xScale,y*yScale,xScale,yScale);
		}
		
		public void drawALine(int x1, int y1, int x2, int y2,Color colour)
		{
			backGr.setColor(colour);
			backGr.drawLine(x1*xScale,y1*yScale,x2*xScale,y2*yScale);
		}

		public void updateGraphic() {
		        Graphics g = this.getGraphics();
		        if ((backImg != null) && (g != null)) {		        	
		            g.drawImage(backImg, 0, 0, this.getSize().width, this.getSize().height/2, 0, 0, (int) (xScale * (colums)), (int) (yScale * (rows)), this);
		            g.drawImage(backImg, 0, this.getSize().height/2, this.getSize().width, this.getSize().height, 0, 0, (int) (xScale * (colums)), (int) (yScale * (rows)), this);
		        }
		    }

		    @Override
		    public void paintComponent(Graphics g) {
		        updateGraphic();
		    }

		    @Override
		    public void paint(Graphics g) {
		        updateGraphic();
		    }

	}