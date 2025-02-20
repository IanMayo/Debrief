/*
 *    Debrief - the Open Source Maritime Analysis Application
 *    http://debrief.info
 *
 *    (C) 2000-2014, PlanetMayo Ltd
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the Eclipse Public License v1.0
 *    (http://www.eclipse.org/legal/epl-v10.html)
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 */
// $RCSfile: TimePeriod.java,v $
// @author $Author: Ian.Mayo $
// @version $Revision: 1.4 $
// $Log: TimePeriod.java,v $
// Revision 1.4  2005/05/18 10:48:47  Ian.Mayo
// Fix serialisation warning
//
// Revision 1.3  2004/11/24 16:05:33  Ian.Mayo
// Switch to hi-res timers
//
// Revision 1.2  2004/05/24 16:27:33  Ian.Mayo
// Commit updates from home
//
// Revision 1.1.1.1  2004/03/04 20:31:13  ian
// no message
//
// Revision 1.1.1.1  2003/07/17 10:07:00  Ian.Mayo
// Initial import
//
// Revision 1.6  2002-10-11 08:34:48+01  ian_mayo
// IntelliJ optimisations
//
// Revision 1.5  2002-07-12 15:46:36+01  ian_mayo
// Use constant to represent error value
//
// Revision 1.4  2002-05-28 09:25:34+01  ian_mayo
// after switch to new system
//
// Revision 1.1  2002-05-28 09:15:15+01  ian_mayo
// Initial revision
//
// Revision 1.1  2002-05-23 13:15:51+01  ian
// end of 3d development
//
// Revision 1.3  2002-05-08 16:06:05+01  ian_mayo
// Modify Contains method so true is returned if data is empty
//
// Revision 1.2  2002-04-22 13:28:01+01  ian_mayo
// Add "contains" method interface, and change to that contains is inclusive, rather than exclusive
//
// Revision 1.1  2002-04-11 14:02:34+01  ian_mayo
// Initial revision
//
// Revision 1.0  2001-07-17 08:46:40+01  administrator
// Initial revision
//

package MWC.GenericData;

import java.util.Date;

import junit.framework.TestCase;
import MWC.Utilities.TextFormatting.DebriefFormatDateTime;

/**
 * interface to represent a time period, together with base class to provide core
 * functionality.  The testing is implemented in BouyPatternWrapper in the Debrief classes.
 */
public interface TimePeriod extends java.io.Serializable, Cloneable
{


  /**
   * *************************************************
   * member constant
   * *************************************************
   */
  final public static HiResDate INVALID_DATE = null;

  final public static long INVALID_TIME = -1;

  /////////////////////////////////////////////////////////////
  // member functions
  ////////////////////////////////////////////////////////////
  /**
   * get the start of this time period
   */
  public HiResDate getStartDTG();

  /**
   * get the end of this time period
   */
  public HiResDate getEndDTG();

  /**
   * set the start of this time period
   */
  public void setStartDTG(HiResDate val);

  /**
   * set the end of this time period
   */
  public void setEndDTG(HiResDate val);

  /**
   * extend the time period to enclose this time
   */
  public void extend(HiResDate val);

  /**
   * see if this time period overlaps the one provided
   */
  public boolean overlaps(TimePeriod other);

  /**
   * see if this time period overlaps the one provided
   */
  public boolean overlaps(HiResDate start, HiResDate end);

  /**
   * see if our period contains this time.  If the start and end times are unset, then
   * contains will always return TRUE
   */
  public boolean contains(HiResDate val);

  /**
   * return this period as a string
   */
  public String toString();


  /////////////////////////////////////////////////
  // sample implementation of a time period
  /////////////////////////////////////////////////
  public class BaseTimePeriod implements TimePeriod
  {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/////////////////////////////////////////////////////////////
    // member variables
    ////////////////////////////////////////////////////////////
    private HiResDate _startDTG = INVALID_DATE;
    private HiResDate _endDTG = INVALID_DATE;

    /////////////////////////////////////////////////////////////
    // constructor
    ////////////////////////////////////////////////////////////
    public BaseTimePeriod(final HiResDate start, final HiResDate end)
    {
      _startDTG = start;
      _endDTG = end;
    }


    /////////////////////////////////////////////////////////////
    // member functions
    ////////////////////////////////////////////////////////////

    /**
     * get the start of this time period
     */
    public final HiResDate getStartDTG()
    {
      return _startDTG;
    }

    /**
     * get the end of this time period
     */
    public final HiResDate getEndDTG()
    {
      return _endDTG;
    }

    /**
     * set the start of this time period
     */
    public final void setStartDTG(final HiResDate val)
    {
      _startDTG = val;
    }

    /**
     * set the end of this time period
     */
    public final void setEndDTG(final HiResDate val)
    {
      _endDTG = val;
    }

    /**
     * extend the time period to enclose this time
     */
    public final void extend(final HiResDate val)
    {
      // are we using duff values
      if (_startDTG == INVALID_DATE)
        _startDTG = val;
      if (_endDTG == INVALID_DATE)
        _endDTG = val;

      if (_startDTG.greaterThan(val))
      {
        _startDTG = val;
      }

      if (_endDTG.lessThan(val))
      {
        _endDTG = val;
      }
    }

    /**
     * see if our period contains this time.  If the start and end times are unset, then
     * contains will always return TRUE
     */
    public final boolean contains(final HiResDate other)
    {
      boolean res = false;

      if (_startDTG == INVALID_DATE && _endDTG == INVALID_DATE)
      {
        res = true;
      }
      else if ((_startDTG.lessThanOrEqualTo(other)) && (_endDTG.greaterThanOrEqualTo(other)))
      {
        res = true;
      }

      return res;
    }

    /**
     * see if this time period overlaps the one provided
     */
    public final boolean overlaps(final HiResDate start, final HiResDate end)
    {
      boolean res = false;

      // check we have data
      if (_startDTG != INVALID_DATE)
      {
        if (_endDTG != INVALID_DATE)
        {
          // we have both times. check if we overlap
          if ((_startDTG.lessThan(end)) && (_endDTG.greaterThan(start)))
            res = true;
          else
            res = false;
        }
        else
        {
          // we only have start time - see if it is in the other period
          if (new BaseTimePeriod(start, end).contains(_startDTG))
            res = true;
          else
            res = false;
        }

      }
      else
      {
        // we don't have a start time. assume we don't have end time either and
        // return TRUE
        res = true;
      }
      return res;
    }

    /**
     * see if this time period overlaps the one provided
     */
    public final boolean overlaps(final TimePeriod other)
    {
      return overlaps(other.getStartDTG(), other.getEndDTG());
    }

    /**
     * return this period as a string
     */
    public String toString()
    {
      final String res = "Period:" + DebriefFormatDateTime.toStringHiRes(_startDTG) + " to " + DebriefFormatDateTime.toStringHiRes(_endDTG);
      return res;
    }

    public BaseTimePeriod intersects(BaseTimePeriod thisP)
    {
      // sort out the overlap
      long startP = Math.max(this.getStartDTG().getMicros(), thisP.getStartDTG().getMicros());
      long endP = Math.min(this.getEndDTG().getMicros(), thisP.getEndDTG().getMicros());
      BaseTimePeriod result = new BaseTimePeriod(new HiResDate(0, startP), new HiResDate(0, endP));
      return result;
    }


  }

  public static class TestTimePeriod extends TestCase
  {
    @SuppressWarnings("deprecation")
    public void testIntersects()
    {
      HiResDate t1 = new HiResDate(new Date(2016,3,3));
      HiResDate t2 = new HiResDate(new Date(2016,3,4));
      HiResDate t3 = new HiResDate(new Date(2016,3,5));
      HiResDate t4 = new HiResDate(new Date(2016,3,6));
      HiResDate t5 = new HiResDate(new Date(2016,3,7));
      
      BaseTimePeriod p1 = new BaseTimePeriod(t1,  t4);
      BaseTimePeriod p2 = new BaseTimePeriod(t2, t3);
      BaseTimePeriod overlap = p1.intersects(p2);
      assertEquals("inner period", overlap.getStartDTG(), t2);
      assertEquals("inner period", overlap.getEndDTG(), t3);

      p1 = new BaseTimePeriod(t1,  t4);
      p2 = new BaseTimePeriod(t2, t3);
      overlap = p2.intersects(p1);
      assertEquals("inner period", overlap.getStartDTG(), t2);
      assertEquals("inner period", overlap.getEndDTG(), t3);

      p1 = new BaseTimePeriod(t1,  t3);
      p2 = new BaseTimePeriod(t2, t4);
      overlap = p2.intersects(p1);
      assertEquals("inner period", overlap.getStartDTG(), t2);
      assertEquals("inner period", overlap.getEndDTG(), t3);
      
      p1 = new BaseTimePeriod(t1,  t5);
      p2 = new BaseTimePeriod(t2, t4);
      overlap = p2.intersects(p1);
      assertEquals("inner period", overlap.getStartDTG(), t2);
      assertEquals("inner period", overlap.getEndDTG(), t4);
      
      
    }
  }
  
}
