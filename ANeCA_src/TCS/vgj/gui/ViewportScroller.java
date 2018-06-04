package TCS.VGJ.gui;

/*
 * File: ViewportScroller.java
 *
 * 5/29/96   Larry Barowski
 *
*/
import java.awt.Image;
import java.awt.Canvas;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.AWTEvent;
import java.awt.event.*;
import java.awt.Point;




/**
 *	A ViewportScroller is a window used to scroll a window through a
 * larger area of content. The content is shown as a white rectangle, with
 * the window being represented by a black rectangle outline within it. A
 * SCROLL event is sent when the user drags the "window" with the mouse.
 *	</p>Here is the <a href="../gui/ViewportScroller.java">source</a>.
 *
 * @author     Larry Barowski
 * @created    July 2, 2003
 */
public class ViewportScroller extends Canvas implements MouseListener,MouseMotionListener
{
	/**
	 *	Event indicating the scroller has been moved.
	 */
	public static int SCROLL        = 38773;
	public static int DONE          = 38774;
	public static double offsetX_, offsetY_;

	private int width_              = -1, height_              = -1;

	private double portWidth_, portHeight_;
	private double contentWidth_, contentHeight_;

	private Rectangle portRect_;
	private Rectangle contentRect_;

	private int preferredW_, preferredH_;

	private Color color_;

	private boolean mousedown_      = false;

	private int dragOffsetX_, dragOffsetY_;

	private DragFix dragFix_;

	private Image backImage_        = null;



	public ViewportScroller(int width, int height, double contentw, double contenth, double portw, double porth, double offsx, double offsy)
	{
		dragFix_ = new DragFix(this);

		preferredW_ = width;
		preferredH_ = height;
		contentWidth_ = contentw;
		contentHeight_ = contenth;
		portWidth_ = portw;
		portHeight_ = porth;
		offsetX_ = offsx;
		offsetY_ = offsy;

		contentRect_ = new Rectangle();
		portRect_ = new Rectangle();

		color_ = Color.white;
		addMouseListener(this);
		addMouseMotionListener(this);
	}


	double getOffsetX()
	{
		return offsetX_;
	}


	double getOffsetY()
	{
		return offsetY_;
	}


	public void processEvent(AWTEvent e)
	{
		if(e instanceof TCSEvent){
			if (e.getID() == DragFix.QUEUED)
			{
				super.processEvent((AWTEvent) ((TCSEvent)e).getInformation());
				getParent().dispatchEvent((AWTEvent) ((TCSEvent) e).getInformation());
				return;
			}
			dragFix_.queueEvent(e);
			return;
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
		dragOffsetX_ = e.getX() - portRect_.x + contentRect_.x;
		dragOffsetY_ = e.getY() - portRect_.y + contentRect_.y;
		return;
	}
	public void mouseReleased(MouseEvent e){
		mousedown_ = false;
		paintOver();
		// Post an event indicating we are done scrolling.
		getParent().dispatchEvent(new TCSEvent((Object) this, DONE, (Object) this));
	}


/*	public void processMouseEvent(MouseEvent e){
		if(e.getID() == MouseEvent.MOUSE_PRESSED){
			if (mousedown_)
			{
				return;
			}

			mousedown_ = true;

			dragOffsetX_ = e.getX() - portRect_.x + contentRect_.x;
			dragOffsetY_ = e.getY() - portRect_.y + contentRect_.y;

			return;
		} else if(e.getID() == MouseEvent.MOUSE_RELEASED) {			
			mousedown_ = false;
			paintOver();

			// Post an event indicating we are done scrolling.
			getParent().dispatchEvent(new TCSEvent((Object) this, DONE, (Object) this));

			return;
		}
	}
*/	

	public void mouseMoved(MouseEvent e){}
	public void mouseDragged(MouseEvent e){
		int x = e.getX();
		int y = e.getY();
		if (!mousedown_)
		{
			return;
		}
		if (x < dragOffsetX_)
		{
			x = dragOffsetX_;
		}
		if (x - dragOffsetX_ + portRect_.width > contentRect_.width)
		{
		x = contentRect_.width + dragOffsetX_ - portRect_.width;
		}
		if (y < dragOffsetY_)
		{
			y = dragOffsetY_;
		}
		if (y - dragOffsetY_ + portRect_.height > contentRect_.height)
		{
			y = contentRect_.height + dragOffsetY_ - portRect_.height;
		}
		Graphics graphics   = getGraphics();
		getGraphics().drawImage(backImage_, 0, 0, null);
		portRect_.x = x - dragOffsetX_ + contentRect_.x;
		portRect_.y = y - dragOffsetY_ + contentRect_.y;
		graphics.drawRect(portRect_.x, portRect_.y, portRect_.width, portRect_.height);
		graphics.dispose();
		double scale_ratio  = contentWidth_ / (double) (contentRect_.width);
		offsetX_ = (double) (portRect_.x - contentRect_.x) * scale_ratio + 0.5;
		offsetY_ = (double) (portRect_.y - contentRect_.y) * scale_ratio + 0.5;
		// Post an event indicating a scroll.
		getParent().dispatchEvent(new TCSEvent((Object) this, SCROLL, (Object) this));
	}
	/*public void processMouseMotionEvent(MouseEvent e)
	{
		int x = e.getX();
		int y = e.getY();
		if(e.getID() == MouseEvent.MOUSE_DRAGGED){
			if (!mousedown_)
			{
				return;
			}
	
			if (x < dragOffsetX_)
			{
				x = dragOffsetX_;
			}

			if (x - dragOffsetX_ + portRect_.width > contentRect_.width)
			{
				x = contentRect_.width + dragOffsetX_ - portRect_.width;
			}

			if (y < dragOffsetY_)
			{
				y = dragOffsetY_;
			}
	
			if (y - dragOffsetY_ + portRect_.height > contentRect_.height)
			{
				y = contentRect_.height + dragOffsetY_ - portRect_.height;
			}
	
			Graphics graphics   = getGraphics();
			getGraphics().drawImage(backImage_, 0, 0, null);

			portRect_.x = x - dragOffsetX_ + contentRect_.x;
			portRect_.y = y - dragOffsetY_ + contentRect_.y;

			graphics.drawRect(portRect_.x, portRect_.y, portRect_.width, portRect_.height);
			graphics.dispose();

			double scale_ratio  = contentWidth_ / (double) (contentRect_.width);
			offsetX_ = (double) (portRect_.x - contentRect_.x) * scale_ratio + 0.5;
			offsetY_ = (double) (portRect_.y - contentRect_.y) * scale_ratio + 0.5;

			// Post an event indicating a scroll.
			getParent().dispatchEvent(new TCSEvent((Object) this, SCROLL, (Object) this));

			return;
		}
	}*/




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

		graphics.setColor(getBackground());
		graphics.fillRect(0, 0, width_, height_);
		graphics.setColor(color_);
		graphics.fillRect(contentRect_.x, contentRect_.y, contentRect_.width, contentRect_.height);
		graphics.setColor(Color.black);
		graphics.setPaintMode();
		graphics.drawRect(contentRect_.x, contentRect_.y, contentRect_.width, contentRect_.height);
		graphics.dispose();

		Graphics screen    = getGraphics();
		screen.drawImage(backImage_, 0, 0, null);
		screen.drawRect(portRect_.x, portRect_.y, portRect_.width, portRect_.height);
		screen.dispose();
	}
	// this will give the initial size

	public Dimension preferredSize()
	{
		return new Dimension(preferredW_, preferredH_);
	}


	private void recompute_()
	{
		double d_width      = (double) width_ - 1.0;
		double d_height     = (double) height_ - 1.0;

		if (d_width * contentHeight_ > d_height * contentWidth_)
		{  // Canvas is proportionally wider than content.

			contentRect_.y = 0;
			contentRect_.height = (int) d_height;

			contentRect_.width = (int) (d_height * contentWidth_ /
					contentHeight_);
			contentRect_.x = (int) ((d_width - contentRect_.width) / 2.0);
		}
		else
		{  // Canvas is proportional with or proportionally taller thatn
			// content.

			contentRect_.x = 0;
			contentRect_.width = (int) d_width;

			contentRect_.height = (int) (d_width * contentHeight_ /
					contentWidth_);
			contentRect_.y = (int) ((d_height - contentRect_.height) / 2.0);
		}

		double scale_ratio  = ((double) contentRect_.width) / contentWidth_;

		portRect_.x = contentRect_.x + (int) (offsetX_ * scale_ratio);
		portRect_.y = contentRect_.y + (int) (offsetY_ * scale_ratio);
		portRect_.width = (int) (portWidth_ * scale_ratio) + 1;
		portRect_.height = (int) (portHeight_ * scale_ratio) + 1;

		if (portRect_.x < contentRect_.x)
		{
			portRect_.x = contentRect_.x;
		}
		if (portRect_.y < contentRect_.y)
		{
			portRect_.y = contentRect_.y;
		}
		if (portRect_.x + portRect_.width > contentRect_.x + contentRect_.width)
		{
			portRect_.x = contentRect_.x + contentRect_.width - portRect_.width;
		}
		if (portRect_.y + portRect_.height > contentRect_.y + contentRect_.height)
		{
			portRect_.y = contentRect_.y + contentRect_.height - portRect_.height;
		}

	}


	public synchronized void removeNotify()
	{
		dragFix_.killThread();
		super.removeNotify();
	}


	public void setContentSize(double width, double height)
	{
		if (contentWidth_ == width && contentHeight_ == height)
		{
			return;
		}

		contentWidth_ = width;
		contentHeight_ = height;

		recompute_();
		repaint();
	}


	public void setOffset(double x, double y)
	{
		if ((int) offsetX_ == (int) x && (int) offsetY_ == (int) y)
		{
			return;
		}

		offsetX_ = x;
		offsetY_ = y;

		recompute_();
		paintOver();
	}


	public void setPortSize(double width, double height)
	{
		if (portWidth_ == width && portHeight_ == height)
		{
			return;
		}

		portWidth_ = width;
		portHeight_ = height;

		recompute_();
		repaint();
	}


	void setTheColor(Color new_color)
	{
		color_ = new_color;
	}
}
