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
// $RCSfile: CreateLocalGrid.java,v $
// @author $Author: Ian.Mayo $
// @version $Revision: 1.2 $
// $Log: CreateLocalGrid.java,v $
// Revision 1.2  2005/01/20 09:03:07  Ian.Mayo
// Give local grid painter unique name
//
// Revision 1.1  2004/10/19 10:15:03  Ian.Mayo
// Add local grid support
//
// Revision 1.2  2004/05/25 15:44:20  Ian.Mayo
// Commit updates from home
//
// Revision 1.1.1.1  2004/03/04 20:31:26  ian
// no message
//
// Revision 1.1.1.1  2003/07/17 10:07:46  Ian.Mayo
// Initial import
//
// Revision 1.2  2002-05-28 09:26:02+01  ian_mayo
// after switch to new system
//
// Revision 1.1  2002-05-28 09:14:04+01  ian_mayo
// Initial revision
//
// Revision 1.1  2002-04-11 13:03:35+01  ian_mayo
// Initial revision
//
// Revision 1.1  2001-08-23 13:27:55+01  administrator
// Reflect new signature for PlainCreate class, to allow it to fireExtended()
//
// Revision 1.0  2001-07-17 08:42:52+01  administrator
// Initial revision
//
// Revision 1.1  2001-01-03 13:41:39+00  novatech
// Initial revision
//
// Revision 1.1.1.1  2000/12/12 21:51:52  ianmayo
// initial version
//
// Revision 1.3  1999-11-26 15:51:40+00  ian_mayo
// tidying up
//
// Revision 1.2  1999-11-11 18:23:03+00  ian_mayo
// new classes, to allow creation of shapes from palette
//

package MWC.GUI.Tools.Palette;

import MWC.GUI.Chart.Painters.LocalGridPainter;
import MWC.GenericData.WorldLocation;

public class CreateLocalGrid extends PlainCreate
{
  public CreateLocalGrid(final MWC.GUI.ToolParent theParent,
                         final MWC.GUI.Properties.PropertiesPanel thePanel,
                         final MWC.GUI.Layer theLayer,
                         final MWC.GUI.Layers theData,
                         final MWC.GUI.PlainChart theChart)
  {
    super(theParent, thePanel, theLayer, theData, theChart, "Local Grid", "images/local_grid.gif");
  }

  protected MWC.GUI.Plottable createItem(final MWC.GUI.PlainChart theChart)
  {
    final WorldLocation theOrigin = new WorldLocation(theChart.getCanvas().getProjection().getVisibleDataArea().getCentre());
    final LocalGridPainter res = new LocalGridPainter();
    res.setOrigin(theOrigin);
    return res;
  }
}
