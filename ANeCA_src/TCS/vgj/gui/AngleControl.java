package TCS.VGJ.gui;

/*
 * File: AngleControl.java
 *
 * 7/13/96   Larry Barowski
 *
*/

import java.awt.Image;
import java.awt.Canvas;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.AWTEvent;
import java.awt.Point;
import java.awt.FontMetrics;
import java.awt.Font;
import java.awt.event.*;

import TCS.VGJ.util.DPoint;




/**
 *     A class that allows control of a 3D viewpoint angle in polar coordinates
 *  (phi, theta).
 *	</p>Here is the <a href="../gui/AngleControl.java">source</a>.
 *
 * @author     Larry Barowski
 * @created    July 2, 2003
 */
public class AngleControl extends Canvas implements MouseListener,MouseMotionListener
{
	/**
	 *	Event indicating the angle has changed.
	 */
	public static int ANGLE     = 38779;
	public static int DONE      = 38780;

	private int width_          = -1, height_          = -1;

	private double theta_, phi_;
	private double markx_, marky_;

	private int preferredW_, preferredH_;

	private Color color_;

	private boolean mousedown_  = false;
	private DragFix dragFix_;
	private Image backImage_    = null;

	private Font font_;


	public AngleControl(int width, int height)
	{
		addMouseListener(this);
		addMouseMotionListener(this);
		dragFix_ = new DragFix(this);

		preferredW_ = width;
		preferredH_ = height;
		theta_ = 0.0;
		phi_ = Math.PI / 2.0;

		color_ = Color.white;

		font_ = new Font("Helvetica", Font.PLAIN, 12);
	}


	private void drawLabels_(Graphics graphics, boolean numbers)
	{
		FontMetrics fm      = graphics.getFontMetrics();
		int thetadeg        = (int) (theta_ * 180 / Math.PI);
		String thetastring  = "theta";
		if (numbers)
		{
			thetastring += " " + thetadeg;
		}
		graphics.drawString(thetastring, 2, (height_ / 2 - 1));

		int phideg          = (int) (phi_ * 180 / Math.PI);
		String phistring    = "phi";
		if (numbers)
		{
			phistring += " " + phideg;
		}
		graphics.drawString(phistring, (width_ / 2 + 1), fm.getAscent() + 2);
	}


	private void drawX_(Graphics graphics)
	{
		graphics.drawLine((int) markx_ - 3, (int) marky_ - 3, (int) markx_ + 3, (int) marky_ + 3);
		graphics.drawLine((int) markx_ - 3, (int) marky_ + 3, (int) markx_ + 3, (int) marky_ - 3);
	}


	double getPhi()
	{
		return phi_;
	}


	double getTheta()
	{
		return theta_;
	}


	public void processEvent(AWTEvent e)
	{
		if(e instanceof TCSEvent){
			if (e.getID() == DragFix.QUEUED)
			{
				super.processEvent((AWTEvent)((TCSEvent)e).getInformation());
				//getParent().postEvent((Event)e.arg);
				return;
			}
			dragFix_.queueEvent(e);
		} else {
			super.processEvent(e);
		}
	}
	
	public void mouseClicked(MouseEvent e){}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mousePressed(MouseEvent e){
		if (mousedown_)
		{
			return;
		}
		mousedown_ = true;
		moveX_(e.getX(), e.getY());
	}
	public void mouseReleased(MouseEvent e){
		mousedown_ = false;
		paintX_();
		getParent().dispatchEvent(new TCSEvent(this, DONE, new DPoint(theta_, phi_)));
	}

	/*public void processMouseEvent(MouseEvent e)
	{
		if(e.getID() == MouseEvent.MOUSE_PRESSED){
			if (mousedown_)
			{
				return;
			}

			mousedown_ = true;

			moveX_(e.getX(), e.getY());

			return;
		} else if(e.getID() == MouseEvent.MOUSE_RELEASED){
			mousedown_ = false;
			paintX_();

			getParent().dispatchEvent(new TCSEvent(this, DONE, new DPoint(theta_, phi_)));
			return;
		}
	}*/

	public void mouseDragged(MouseEvent e){
		if (!mousedown_)
		{
			return;
		}
		moveX_(e.getX(), e.getY());
	}
	public void mouseMoved(MouseEvent e){}
	
	/*public void processMouseMotionEvent(MouseEvent e)
	{
		if(e.getID() == MouseEvent.MOUSE_DRAGGED){
			if (!mousedown_)
			{
				return;
			}
			moveX_(e.getX(), e.getY());
			return;
		}
	}*/


	private void moveX_(int x, int y)
	{
		if (x < 0)
		{
			x = 0;
		}
		if (x > width_ - 1)
		{
			x = width_ - 1;
		}
		if (y < 0)
		{
			y = 0;
		}
		if (y > height_ - 1)
		{
			y = height_ - 1;
		}

		markx_ = (double) x;
		marky_ = (double) y;

		theta_ = markx_ / (double) (width_ - 1) * 2.0 * Math.PI - Math.PI;
		phi_ = Math.PI / 2.0 - marky_ / (double) (height_ - 1) * Math.PI;

		paintX_();

		getParent().dispatchEvent(new TCSEvent( this, ANGLE, new DPoint(theta_, phi_)));
	}


	public synchronized void paint(Graphics graphics)
	{
		graphics.dispose();
		paintOver();
	}


	public synchronized void paintOver()
	{
		Dimension winsize  = getSize();

		if (winsize.width != width_ || winsize.height != height_)
		{
			width_ = winsize.width;
			height_ = winsize.height;

			recompute_();
			backImage_ = null;
		}

		if (backImage_ == null)
		{
			backImage_ = createImage(width_, height_);
		}

		Graphics graphics  = backImage_.getGraphics();

		graphics.setFont(font_);
		graphics.setColor(color_);
		graphics.fillRect(0, 0, width_, height_);
		graphics.setColor(Color.black);
		graphics.setPaintMode();
		graphics.drawRect(0, 0, width_ - 1, height_ - 1);

		graphics.drawLine(width_ / 2, 0, width_ / 2, height_);
		graphics.drawLine(0, height_ / 2, width_, height_ / 2);
		drawLabels_(graphics, false);
		graphics.dispose();

		Graphics screen    = getGraphics();
		screen.drawImage(backImage_, 0, 0, null);
		drawX_(screen);
		screen.setFont(font_);
		drawLabels_(screen, true);
		screen.dispose();
	}


	private synchronized void paintX_()
	{
		Graphics screen  = getGraphics();
		screen.drawImage(backImage_, 0, 0, null);
		drawX_(screen);
		screen.setFont(font_);
		drawLabels_(screen, true);
		screen.dispose();
	}
	// this will give the initial size

	public Dimension preferredSize()
	{
		return new Dimension(preferredW_, preferredH_);
	}


	private void recompute_()
	{
		markx_ = (theta_ + Math.PI) / (2.0 * Math.PI) * (double) width_;
		marky_ = (Math.PI / 2.0 - phi_) / Math.PI * (double) height_;
	}


	public synchronized void removeNotify()
	{
		dragFix_.killThread();
		super.removeNotify();
	}


	void setAngles(double theta, double phi)
	{
		if (theta == theta_ && phi == phi_)
		{
			return;
		}

		theta_ = theta;
		phi_ = phi;

		recompute_();
		paintOver();
	}


	void setColor(Color new_color)
	{
		color_ = new_color;
	}
}
