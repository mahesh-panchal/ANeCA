package TCS.VGJ.gui;

/*
	File: DragFix.java
	3/7/97    Larry Barowski
*/

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.AdjustmentEvent;
//is this class really needed anymore? MP

/**
 *	Drag fix queues events and eliminates repeated mouse
 *      drag events and scrollbar events (most annoying on Win95).
 *      Be sure the Component doesn't post an event to itself
 *      after every mouse drag or scrollbar event, or this will do
 *      no good. I suggest getParent().postEvent() instead.</p>
 *
 *      The constructor for the Component that uses it should have
 *      (to be safe, as the first line):</p>
 *
 *      <PRE>dragFix_ = new DragFix(this);  // dragFix_ is a member variable.</PRE>
 *
 *
 *      <br><br>The handleEvent() function should look like this:</p>
 *
 *      <PRE>public boolean handleEvent(Event e)
 *      {
 *         if(e.id == DragFix.QUEUED)
 *         {
 *            deal with (Event)e.arg
 *            if necessary, super.handleEvent((Event)e.arg);
 *            if necessary, getParent().postEvent((Event)e.arg);
 *            return true;
 *         }
 *         dragFix_.queueEvent(e);
 *         return true;
 *      }</PRE>
 *
 *     <br><br>and removeNotify should look like this, to kill the thread
 *     immediately - otherwise it will be there (asleep) until
 *     finalize() gets called (if ever):</p>
 *
 *     <PRE>public synchronized void removeNotify()
 *     {
 *        dragFix_.killThread();
 *        super.removeNotify();
 *     }</pre></p><br><br>
 *
 *
 *
 *	</p>Here is the <a href="../gui/DragFix.java">source</a>.
 *
 * @author     Larry Barowski
 * @created    July 3, 2003
 */
public class DragFix implements Runnable
{  // More than enough, in most cases.
	private final static int eventQueueLimit_  = 20000;

	public final static int QUEUED             = 179583;
	private Component user_;
	private Thread thread_;

	private AWTEvent[] eventQueue_                = null;
	private int eventQueueHead_                = 0, eventQueueTail_                = 0;
	private int eventQueueSize_                = 20;
	private Object lock_                       = new Object();
	private boolean should_run = true;


	public DragFix(Component user)
	{
		user_ = user;
		eventQueue_ = new AWTEvent[eventQueueSize_ + 1];

		thread_ = new Thread(this);
		thread_.start();
	}


	protected void finalize()
	{
		killThread();
	}


	public void killThread()
	{
		//thread_.stop();
		should_run = false;
	}


	/**
	 * Queue or dequeue an event.
	 *
	 * @param  e  Description of the Parameter
	 * @return    Description of the Return Value
	 */
	private AWTEvent Queue(AWTEvent e)
	{
		synchronized (lock_)
		{
			if (e == null)
			{  // Dequeue

				if (eventQueueHead_ != eventQueueTail_)
				{  // Not empty.

					eventQueueTail_ = (eventQueueTail_ + 1) % eventQueueSize_;
					return eventQueue_[eventQueueTail_];
				}
				return null;  // To indicate empty.
			}

			// Add to queue.
			if (eventQueueHead_ != eventQueueTail_ && e.getSource() ==
					eventQueue_[eventQueueHead_].getSource())
			{
				int newid  = eventQueue_[eventQueueHead_].getID();
				if ((e.getID() == MouseEvent.MOUSE_DRAGGED && newid == MouseEvent.MOUSE_DRAGGED) ||
					/*((e.getID() == Event.SCROLL_ABSOLUTE || 
					  e.getID() == Event.SCROLL_LINE_DOWN ||
					  e.getID() == Event.SCROLL_LINE_UP ||
					  e.getID() == Event.SCROLL_PAGE_DOWN ||
					  e.getID() == Event.SCROLL_PAGE_UP) &&
						(newid == Event.SCROLL_ABSOLUTE ||
						newid == Event.SCROLL_LINE_DOWN ||
						newid == Event.SCROLL_LINE_UP ||
						newid == Event.SCROLL_PAGE_DOWN ||
						newid == Event.SCROLL_PAGE_UP)))*/
					/* The scroll events are not that clear cut so here's my try*/
					((e.getID() == AdjustmentEvent.ADJUSTMENT_VALUE_CHANGED || 
					  e.getID() == AdjustmentEvent.UNIT_DECREMENT ||
					  e.getID() == AdjustmentEvent.UNIT_INCREMENT ||
					  e.getID() == AdjustmentEvent.BLOCK_DECREMENT ||
					  e.getID() == AdjustmentEvent.BLOCK_INCREMENT) &&
						(newid == AdjustmentEvent.ADJUSTMENT_VALUE_CHANGED ||
						newid == AdjustmentEvent.UNIT_DECREMENT ||
						newid == AdjustmentEvent.UNIT_INCREMENT ||
						newid == AdjustmentEvent.BLOCK_DECREMENT ||
						newid == AdjustmentEvent.BLOCK_INCREMENT)))
				{
					// Eliminate the previous mouse drag or scroll.
					// This assumes all scroll handling will use
					// absolute positions, instead of "clicks".
					eventQueue_[eventQueueHead_] = e;
					return null;
				}
			}

			eventQueue_[(eventQueueHead_ + 1) % eventQueueSize_] = e;
			eventQueueHead_ = (eventQueueHead_ + 1) % eventQueueSize_;

			lock_.notify();

			if ((eventQueueHead_ + 1) % eventQueueSize_ ==
					eventQueueTail_)
			{
				// Queue is full, grow it.
				int new_size       = eventQueueSize_ * 2;
				if (new_size > eventQueueLimit_)
				{
					throw new Error("DragFix event queue size limit " + "exceeded.");
				}

				AWTEvent[] new_queue  = new AWTEvent[new_size + 1];
				int i              = 0;
				int j;
				for (j = eventQueueTail_; j != eventQueueHead_;
						j = (j + 1) % eventQueueSize_, i++)
				{
					new_queue[i] = eventQueue_[j];
				}
				new_queue[i] = eventQueue_[j];

				eventQueue_ = new_queue;
				eventQueueHead_ = i;
				eventQueueTail_ = 0;
				eventQueueSize_ = new_size;
			}
			return null;
		}
	}


	/**
	 * Queue or ignore an event. Call this from handleEvent()
	 * and return true.
	 *
	 * @param  e  Description of the Parameter
	 */
	public void queueEvent(AWTEvent e)
	{
		Queue(e);
	}


	/**
	 * Process queued events.
	 */
	public synchronized void run()
	{
		AWTEvent e;
		while (should_run)
		{

			// Process events until the queue is empty.
			while ((e = Queue(null)) != null)
			{
				user_.dispatchEvent(new TCSEvent(e, QUEUED, e));
			}

			// Now that it is empty, wait for the next event.
			synchronized (lock_)
			{
				try
				{
					lock_.wait();
				}
				catch (InterruptedException ex)
				{
				}
			}
		}
	}
}
