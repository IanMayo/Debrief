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
package org.mwc.cmap.core.operations;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import java.util.Vector;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.mwc.cmap.core.CorePlugin;

import Debrief.Wrappers.FixWrapper;
import Debrief.Wrappers.SensorContactWrapper;
import Debrief.Wrappers.SensorWrapper;
import Debrief.Wrappers.TMAContactWrapper;
import Debrief.Wrappers.TMAWrapper;
import Debrief.Wrappers.TrackWrapper;
import MWC.GUI.Editable;
import MWC.GUI.Layer;
import MWC.GUI.Layers;
import MWC.GUI.NeedsToBeInformedOfRemove;
import MWC.GenericData.HiResDate;
import MWC.GenericData.WorldDistance;
import MWC.GenericData.WorldLocation;
import MWC.GenericData.WorldVector;
import MWC.TacticalData.Fix;

public class RightClickCutCopyAdaptor
{

	/**
	 * embedded class used to convert our Editable objects to/from clipboard
	 * format
	 * 
	 * @author ian.mayo
	 */
	public final static class EditableTransfer extends ByteArrayTransfer
	{

		private static final String MYTYPENAME = "CMAP_EDITABLE";

		public static final int MYTYPEID = registerType(MYTYPENAME);

		/**
		 * singleton instance of ourselves
		 */
		private static EditableTransfer _instance;

		/**
		 * private constructor - so we have to use the 'get instance' method
		 */
		private EditableTransfer()
		{
		}

		/**
		 * accessor, get running.
		 * 
		 * @return
		 */
		public static EditableTransfer getInstance()
		{
			if (_instance == null)
				_instance = new EditableTransfer();

			return _instance;
		}

		/**
		 * ok - convert our object ready to put it on the clipboard
		 * 
		 * @param object
		 * @param transferData
		 */
		public void javaToNative(final Object object, final TransferData transferData)
		{
			if (object == null || !(object instanceof Editable[]))
				return;

			if (isSupportedType(transferData))
			{
				final Editable[] myItem = (Editable[]) object;
				try
				{
					final ByteArrayOutputStream out = new ByteArrayOutputStream();
					final ObjectOutputStream writeOut = new ObjectOutputStream(out);
					writeOut.writeObject(myItem);
					final byte[] buffer = out.toByteArray();
					writeOut.close();

					super.javaToNative(buffer, transferData);

				}
				catch (final IOException e)
				{
					CorePlugin.logError(Status.ERROR,
							"Problem converting object to clipboard format: " + object, e);
				}
			}
		}

		/**
		 * ok, extract our object from the clipboard
		 * 
		 * @param transferData
		 * @return
		 */
		public Object nativeToJava(final TransferData transferData)
		{

			if (isSupportedType(transferData))
			{

				final byte[] buffer = (byte[]) super.nativeToJava(transferData);
				if (buffer == null)
					return null;

				Editable[] myData = null;
				try
				{
					final ByteArrayInputStream in = new ByteArrayInputStream(buffer);
					final ObjectInputStream readIn = new ObjectInputStream(in);
					myData = (Editable[]) readIn.readObject();
					readIn.close();
				}
				catch (final IOException ex)
				{
					CorePlugin.logError(Status.ERROR,
							"Problem converting object to clipboard format", null);
					return null;
				}
				catch (final ClassNotFoundException e)
				{
					CorePlugin.logError(Status.ERROR,
							"Whilst converting from native to java, can't find this class:"
									+ e.getMessage(), null);
				}
				return myData;
			}

			return null;
		}

		protected String[] getTypeNames()
		{
			return new String[]
			{ MYTYPENAME };
		}

		protected int[] getTypeIds()
		{
			return new int[]
			{ MYTYPEID };
		}
	}

	// /////////////////////////////////
	// member variables
	// ////////////////////////////////

	// /////////////////////////////////
	// constructor
	// ////////////////////////////////

	// /////////////////////////////////
	// member functions
	// ////////////////////////////////
	static public void getDropdownListFor(final IMenuManager manager,
			final Editable[] editables, final Layer[] updateLayers, final Layer[] parentLayers,
			final Layers theLayers, final Clipboard _clipboard)
	{
		// do we have any editables?
		if (editables.length == 0)
			return;

		// get the editable item
		final Editable data = editables[0];

		CutItem cutter = null;
		CopyItem copier = null;
		DeleteItem deleter = null;

		// just check is trying to operate on the layers object itself
		if (data instanceof MWC.GUI.Layers)
		{
			// do nothing, we can't copy the layers itself
		}
		else
		{

			// first the cut action
			cutter = new CutItem(editables, _clipboard, parentLayers, theLayers,
					updateLayers);

			// now the copy action
			copier = new CopyItem(editables, _clipboard, parentLayers, theLayers,
					updateLayers);

			// and the delete
			deleter = new DeleteItem(editables, parentLayers, theLayers, updateLayers);

			// create the menu items

			// add to the menu
			manager.add(new Separator());
			manager.add(cutter);

			// try the copier
			if (copier != null)
			{
				manager.add(copier);
			}

			manager.add(deleter);
		}

	}

	// /////////////////////////////////
	// nested classes
	// ////////////////////////////////

	// ////////////////////////////////////////////
	//
	// ///////////////////////////////////////////////
	public static class CutItem extends Action
	{
		protected Editable[] _data;

		protected Clipboard _myClipboard;

		protected Layer[] _theParent;

		protected Layers _theLayers;

		protected Object _oldContents;

		protected Layer[] _updateLayer;

		public CutItem(final Editable[] data, final Clipboard clipboard, final Layer[] theParent,
				final Layers theLayers, final Layer[] updateLayer)
		{
			// remember parameters
			_data = data;
			_myClipboard = clipboard;
			_theParent = theParent;
			_theLayers = theLayers;
			_updateLayer = updateLayer;

			// formatting
			super.setText("Cut " + toString());
			setActionDefinitionId(ActionFactory.CUT.getCommandId());
			// and the icon
			setImageIcon();

		}

		protected void setImageIcon()
		{
			super.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
					.getImageDescriptor(ISharedImages.IMG_TOOL_CUT));
		}

		// remember what used to be on the clipboard
		protected void rememberPreviousContents()
		{
		  if(_myClipboard!=null)
      {
  			// copy in the new data
  			final EditableTransfer transfer = EditableTransfer.getInstance();
  			_oldContents = _myClipboard.getContents(transfer);
      }
		}

		// restore the previous contents of the clipboard
		protected void restorePreviousContents()
		{
		  if(_myClipboard!=null)
		  {
		 // just check that there were some previous contents
	      if (_oldContents != null)
	      {
	        // copy in the new data
	        final EditableTransfer transfer = EditableTransfer.getInstance();
	        _myClipboard.setContents(new Object[]
	        { _oldContents }, new Transfer[]
	        { transfer });
	      }
	      // and forget what we're holding
	      _oldContents = null;
		  }
			
		}

		/**
		 * 
		 */
		public void run()
		{
			final AbstractOperation myOperation = new AbstractOperation(getText())
			{
				public IStatus execute(final IProgressMonitor monitor, final IAdaptable info)
						throws ExecutionException
				{
					doCut();
					return Status.OK_STATUS;
				}

				public IStatus redo(final IProgressMonitor monitor, final IAdaptable info)
						throws ExecutionException
				{
					doCut();
					return Status.OK_STATUS;
				}

				public IStatus undo(final IProgressMonitor monitor, final IAdaptable info)
						throws ExecutionException
				{
					// ok, place our items back in their layers

					boolean multipleLayersModified = false;
					Layer lastLayerModified = null;

					for (int i = 0; i < _data.length; i++)
					{
						final Editable thisE = _data[i];
						final Layer parentLayer = _theParent[i];

						// is the parent the data object itself?
						if (parentLayer == null)
						{
							// no, it must be the top layers object
							_theLayers.addThisLayer((Layer) thisE);

							// so, we know we've got to remove items from multiple layers
							multipleLayersModified = true;
						}
						else
						{
							// replace the data it's parent
							parentLayer.add(thisE);

							// let's see if we're editing multiple layers
							if (!multipleLayersModified)
							{
								if (lastLayerModified == null)
									lastLayerModified = parentLayer;
								else
								{
									if (lastLayerModified != parentLayer)
										multipleLayersModified = true;
								}
							}
						}

					}

					// and fire an update
					if (multipleLayersModified)
						_theLayers.fireExtended();
					else
						_theLayers.fireExtended(null, lastLayerModified);

					// and restore the previous contents
					restorePreviousContents();

					return Status.OK_STATUS;
				}

				/**
				 * the cut operation is common for execute and redo operations - so
				 * factor it out to here...
				 * 
				 */
				private void doCut()
				{
					final Vector<Layer> changedLayers = new Vector<Layer>();

					// remember the previous contents
					rememberPreviousContents();

					// copy in the new data
					final EditableTransfer transfer = EditableTransfer.getInstance();
					if(_myClipboard!=null)
					{
					  _myClipboard.setContents(new Object[]
			          { _data }, new Transfer[]
			          { transfer });
					}
					

					for (int i = 0; i < _data.length; i++)
					{
						final Editable thisE = _data[i];
						final Layer parentLayer = _theParent[i];

						// is the parent the data object itself?
						if (parentLayer == null)
						{
							// no, it must be the top layers object
							_theLayers.removeThisLayer((Layer) thisE);

							// no need to remember the layer. the "removeThisLayer" will have
							// fired updates
						}
						else
						{
							// remove the new data from it's parent
							parentLayer.removeElement(thisE);
							
							// some objects want to know when they're removed
              if (thisE instanceof NeedsToBeInformedOfRemove)
              {
                // spread the good news
                ((NeedsToBeInformedOfRemove) thisE).beingRemoved();
              }

              // see if we need to track this layer change
							if (!changedLayers.contains(parentLayer))
							{
								changedLayers.add(parentLayer);
							}
						}
					}

					if (changedLayers.size() > 1)
						_theLayers.fireExtended();
					else if (changedLayers.size() == 1)
						_theLayers.fireExtended(null, changedLayers.firstElement());
					else
					{
						// zero layers listed as changed. no 'firing' necessary
					}
				}

			};
			// put in the global context, for some reason
			if (CorePlugin.getUndoContext() != null) {
				myOperation.addContext(CorePlugin.getUndoContext());
			}
			CorePlugin.run(myOperation);
		}

		public String toString()
		{
			String res = "";
			if (_data.length > 1)
				res += _data.length + " selected items";
			else
				res += _data[0].getName();
			return res;
		}

	}

	// ////////////////////////////////////////////
	//
	// ///////////////////////////////////////////////
	public static class CopyItem extends CutItem
	{
		public CopyItem(final Editable[] data, final Clipboard clipboard, final Layer[] theParent,
				final Layers theLayers, final Layer[] updateLayer)
		{
			super(data, clipboard, theParent, theLayers, updateLayer);

			super.setText(toString());
			setActionDefinitionId(ActionFactory.COPY.getCommandId());
			super.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
					.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));

		}

		public String toString()
		{
			String res = "Copy ";
			if (_data.length > 1)
				res += _data.length + " selected items";
			else
				res += _data[0].getName();
			return res;
		}

		public void run()
		{
			final AbstractOperation myOperation = new AbstractOperation(getText())
			{
				public IStatus execute(final IProgressMonitor monitor, final IAdaptable info)
						throws ExecutionException
				{

					// we stick a CLONE on the clipboard - we
					// clone this item when we do a PASTE, so that multiple paste
					// operations can be performed

					doCopy();

					return Status.OK_STATUS;
				}

				public IStatus redo(final IProgressMonitor monitor, final IAdaptable info)
						throws ExecutionException
				{

					doCopy();

					return Status.OK_STATUS;
				}

				public IStatus undo(final IProgressMonitor monitor, final IAdaptable info)
						throws ExecutionException
				{
					// just restore the previous clipboard contents
					restorePreviousContents();

					return Status.OK_STATUS;
				}

				/**
				 * the Copy bit is common to execute and redo methods - so factor it out
				 * to here...
				 * 
				 */
				private void doCopy()
				{
					// remember the old contents
					rememberPreviousContents();

					_data = cloneThese(_data);

					// we stick a pointer to the ACTUAL item on the clipboard - we
					// clone this item when we do a PASTE, so that multiple paste
					// operations can be performed

					// copy in the new data
					final EditableTransfer transfer = EditableTransfer.getInstance();
					_myClipboard.setContents(new Object[]
					{ _data }, new Transfer[]
					{ transfer });
				}
			};
			if (CorePlugin.getUndoContext() != null) {
				myOperation.addContext(CorePlugin.getUndoContext());
			}
			CorePlugin.run(myOperation);
		}

		public void execute()
		{

			// store the old data
			// storeOld();

			// we stick a pointer to the ACTUAL item on the clipboard - we
			// clone this item when we do a PASTE, so that multiple paste
			// operations can be performed

			// copy in the new data
			final EditableTransfer transfer = EditableTransfer.getInstance();
			_myClipboard.setContents(new Object[]
			{ _data }, new Transfer[]
			{ transfer });
		}
	}

	/**
	 * create duplicates of this series of items
	 */
	static public Editable[] cloneThese(final Editable[] items)
	{
		final Editable[] res = new Editable[items.length];
		for (int i = 0; i < items.length; i++)
		{
			final Editable thisOne = items[i];
			final Editable clonedItem = cloneThis(thisOne);
			res[i] = clonedItem;

			// see if we can rename it
			if (clonedItem instanceof Layer)
			{
				final Layer thisL = (Layer) clonedItem;
				thisL.setName("Copy of " + clonedItem.getName());
			}
		}
		return res;
	}

	/**
	 * duplicate this item
	 * 
	 * @param item
	 * @return
	 */
	static public Editable cloneThis(final Editable item)
	{
		Editable res = null;
		try
		{
			final java.io.ByteArrayOutputStream bas = new ByteArrayOutputStream();
			final java.io.ObjectOutputStream oos = new ObjectOutputStream(bas);
			oos.writeObject(item);
			// get closure
			oos.close();
			bas.close();

			// now get the item
			final byte[] bt = bas.toByteArray();

			// and read it back in as a new item
			final java.io.ByteArrayInputStream bis = new ByteArrayInputStream(bt);

			// create the reader
			final java.io.ObjectInputStream iis = new ObjectInputStream(bis);

			// and read it in
			final Object oj = iis.readObject();

			// get more closure
			bis.close();
			iis.close();

			if (oj instanceof Editable)
			{
				res = (Editable) oj;
			}
		}
		catch (final Exception e)
		{
			MWC.Utilities.Errors.Trace.trace(e);
		}
		return res;
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////
	// testing for this class
	// ////////////////////////////////////////////////////////////////////////////////////////////////
	static public final class testCutPaste extends junit.framework.TestCase
	{

		private void doUndo()
		{
			final IOperationHistory history = CorePlugin.getHistory();
			try
			{
				history.undo(CorePlugin.getUndoContext(), null, null);
			}
			catch (final ExecutionException e)
			{
				CorePlugin.logError(Status.ERROR, "Problem with undo for test", e);
				assertTrue("threw assertion", e == null);
			}
		}

		private boolean isPositionThere(final TrackWrapper tw, final FixWrapper fw2)
		{
			boolean itemFound;
			final Enumeration<Editable> enumer = tw.getPositions();
			itemFound = false;
			while (enumer.hasMoreElements())
			{
				final Editable ee = enumer.nextElement();
				if (ee.equals(fw2))
				{
					itemFound = true;
					continue;
				}
			}
			return itemFound;
		}

		private boolean isSensorThere(final TrackWrapper tw,
				final SensorContactWrapper scwa1)
		{
			boolean itemFound;
			final Enumeration<Editable> enumer = tw.getSensors().elements();
			itemFound = false;
			while (enumer.hasMoreElements())
			{
				final SensorWrapper ee = (SensorWrapper) enumer.nextElement();
				final Enumeration<Editable> contacts = ee.elements();
				while (contacts.hasMoreElements())
				{
					final Editable thisC = contacts.nextElement();
					if (thisC.equals(scwa1))
					{
						itemFound = true;
						continue;
					}
				}
			}
			return itemFound;
		}

		private boolean isContactThere(final TrackWrapper tw,
				final TMAContactWrapper scwa1)
		{
			boolean itemFound;
			final Enumeration<Editable> enumer = tw.getSolutions().elements();
			itemFound = false;
			while (enumer.hasMoreElements())
			{
				final TMAWrapper ee = (TMAWrapper) enumer.nextElement();
				final Enumeration<Editable> contacts = ee.elements();
				while (contacts.hasMoreElements())
				{
					final Editable thisC = contacts.nextElement();
					if (thisC.equals(scwa1))
					{
						itemFound = true;
						continue;
					}
				}
			}
			return itemFound;
		}

		private static class MyCutItem extends CutItem
		{

			public MyCutItem(final Editable[] data, final Clipboard clipboard, final Layer[] theParent,
					final Layers theLayers, final Layer[] updateLayer)
			{
				super(data, clipboard, theParent, theLayers, updateLayer);
			}

			@Override
			protected void setImageIcon()
			{
				// don't bother, we haven't got enough platform running
			}

		}

		public void testDummy() {
			
		}

		// TODO FIX-TEST
		public void NtestCut()
		{
			// create the data
			final TrackWrapper tw = new TrackWrapper();

			final WorldLocation loc_1 = new WorldLocation(0, 0, 0);
			final FixWrapper fw1 = new FixWrapper(new Fix(new HiResDate(100, 10000),
					loc_1.add(new WorldVector(33, new WorldDistance(100,
							WorldDistance.METRES), null)), 10, 110));
			fw1.setLabel("fw1");
			final FixWrapper fw2 = new FixWrapper(new Fix(new HiResDate(200, 20000),
					loc_1.add(new WorldVector(33, new WorldDistance(200,
							WorldDistance.METRES), null)), 20, 120));
			fw2.setLabel("fw2");
			final FixWrapper fw3 = new FixWrapper(new Fix(new HiResDate(300, 30000),
					loc_1.add(new WorldVector(33, new WorldDistance(300,
							WorldDistance.METRES), null)), 30, 130));
			fw3.setLabel("fw3");
			final FixWrapper fw4 = new FixWrapper(new Fix(new HiResDate(400, 40000),
					loc_1.add(new WorldVector(33, new WorldDistance(400,
							WorldDistance.METRES), null)), 40, 140));
			fw4.setLabel("fw4");
			final FixWrapper fw5 = new FixWrapper(new Fix(new HiResDate(500, 50000),
					loc_1.add(new WorldVector(33, new WorldDistance(500,
							WorldDistance.METRES), null)), 50, 150));
			fw5.setLabel("fw5");
			tw.addFix(fw1);
			tw.addFix(fw2);
			tw.addFix(fw3);
			tw.addFix(fw4);
			tw.addFix(fw5);
			// also give it some sensor data
			final SensorWrapper swa = new SensorWrapper("title one");
			final SensorContactWrapper scwa1 = new SensorContactWrapper("aaa",
					new HiResDate(150, 0), null, null, null, null, null, 0, null);
			final SensorContactWrapper scwa2 = new SensorContactWrapper("bbb",
					new HiResDate(180, 0), null, null, null, null, null, 0, null);
			final SensorContactWrapper scwa3 = new SensorContactWrapper("ccc",
					new HiResDate(250, 0), null, null, null, null, null, 0, null);
			swa.add(scwa1);
			swa.add(scwa2);
			swa.add(scwa3);
			tw.add(swa);
			final SensorWrapper sw = new SensorWrapper("title two");
			final SensorContactWrapper scw1 = new SensorContactWrapper("ddd",
					new HiResDate(260, 0), null, null, null, null, null, 0, null);
			final SensorContactWrapper scw2 = new SensorContactWrapper("eee",
					new HiResDate(280, 0), null, null, null, null, null, 0, null);
			final SensorContactWrapper scw3 = new SensorContactWrapper("fff",
					new HiResDate(350, 0), null, null, null, null, null, 0, null);
			sw.add(scw1);
			sw.add(scw2);
			sw.add(scw3);
			tw.add(sw);

			final TMAWrapper mwa = new TMAWrapper("bb");
			final TMAContactWrapper tcwa1 = new TMAContactWrapper("aaa", "bbb",
					new HiResDate(130), null, 0, 0, 0, null, null, null, null);
			final TMAContactWrapper tcwa2 = new TMAContactWrapper("bbb", "bbb",
					new HiResDate(190), null, 0, 0, 0, null, null, null, null);
			final TMAContactWrapper tcwa3 = new TMAContactWrapper("ccc", "bbb",
					new HiResDate(230), null, 0, 0, 0, null, null, null, null);
			mwa.add(tcwa1);
			mwa.add(tcwa2);
			mwa.add(tcwa3);
			tw.add(mwa);
			final TMAWrapper mw = new TMAWrapper("cc");
			final TMAContactWrapper tcw1 = new TMAContactWrapper("ddd", "bbb",
					new HiResDate(230), null, 0, 0, 0, null, null, null, null);
			final TMAContactWrapper tcw2 = new TMAContactWrapper("eee", "bbb",
					new HiResDate(330), null, 0, 0, 0, null, null, null, null);
			final TMAContactWrapper tcw3 = new TMAContactWrapper("fff", "bbb",
					new HiResDate(390), null, 0, 0, 0, null, null, null, null);
			mw.add(tcw1);
			mw.add(tcw2);
			mw.add(tcw3);
			tw.add(mw);

			// now fiddle with it
			final Layers updateLayers = new Layers();
			updateLayers.addThisLayer(tw);
			final Clipboard clipboard = new Clipboard(Display.getDefault());
			Layer[] parentLayer = new Layer[]
			{ tw };
			final CutItem ci = new MyCutItem(new Editable[]
			{ fw2 }, clipboard, parentLayer, updateLayers, parentLayer);
			// check our item's in there
			assertTrue("item there before op", isPositionThere(tw, fw2));
			assertTrue("item there before op", isPositionThere(tw, fw3));

			// now do the cut
			ci.run();
			assertFalse("item gone after op", isPositionThere(tw, fw2));
			assertTrue("item there after op", isPositionThere(tw, fw3));

			doUndo();
			assertTrue("item back again after op", isPositionThere(tw, fw2));

			// now let's try two items
			parentLayer = new Layer[]
			{ tw, tw };
			final CutItem c2 = new MyCutItem(new Editable[]
			{ fw2, fw4 }, clipboard, parentLayer, updateLayers, parentLayer);
			assertTrue("item there before op", isPositionThere(tw, fw2));
			assertTrue("item there before op", isPositionThere(tw, fw3));
			assertTrue("item there before op", isPositionThere(tw, fw4));
			// now do the cut
			c2.run();
			assertFalse("item gone after op", isPositionThere(tw, fw2));
			assertTrue("item there after op", isPositionThere(tw, fw3));
			assertFalse("item gone after op", isPositionThere(tw, fw4));

			doUndo();
			assertTrue("item back again after op", isPositionThere(tw, fw2));
			assertTrue("item still there after op", isPositionThere(tw, fw3));
			assertTrue("item back again after op", isPositionThere(tw, fw4));

			// right, now let's try to delete a sensor item
			parentLayer = new Layer[]
			{ swa };
			CutItem c3 = new MyCutItem(new Editable[]
			{ scwa1 }, clipboard, parentLayer, updateLayers, parentLayer);
			assertTrue("item there before op", isSensorThere(tw, scwa1));
			assertTrue("item there before op", isSensorThere(tw, scwa2));
			assertTrue("item there before op", isSensorThere(tw, scwa3));
			c3.run();
			assertFalse("item not there after op", isSensorThere(tw, scwa1));
			assertTrue("item there after op", isSensorThere(tw, scwa2));
			assertTrue("item there after op", isSensorThere(tw, scwa3));
			doUndo();
			assertTrue("item back again after op", isSensorThere(tw, scwa1));
			assertTrue("item back again after op", isSensorThere(tw, scwa2));
			assertTrue("item back again after op", isSensorThere(tw, scwa3));
			// now let's try two items
			parentLayer = new Layer[]
			{ swa, swa };
			c3 = new MyCutItem(new Editable[]
			{ scwa1, scwa2 }, clipboard, parentLayer, updateLayers, parentLayer);
			assertTrue("item there before op", isSensorThere(tw, scwa1));
			assertTrue("item there before op", isSensorThere(tw, scwa2));
			assertTrue("item there before op", isSensorThere(tw, scwa3));
			c3.run();
			assertFalse("item not there after op", isSensorThere(tw, scwa1));
			assertFalse("item not there after op", isSensorThere(tw, scwa2));
			assertTrue("item there after op", isSensorThere(tw, scwa3));
			doUndo();
			assertTrue("item back again after op", isSensorThere(tw, scwa1));
			assertTrue("item back again after op", isSensorThere(tw, scwa2));
			assertTrue("item back again after op", isSensorThere(tw, scwa3));
			// now let's try two items in different layers
			parentLayer = new Layer[]
			{ swa, sw };
			c3 = new MyCutItem(new Editable[]
			{ scwa1, scw2 }, clipboard, parentLayer, updateLayers, parentLayer);
			assertTrue("item there before op", isSensorThere(tw, scwa1));
			assertTrue("item there before op", isSensorThere(tw, scw2));
			assertTrue("item there before op", isSensorThere(tw, scwa3));
			c3.run();
			assertFalse("item not there after op", isSensorThere(tw, scwa1));
			assertFalse("item not there after op", isSensorThere(tw, scw2));
			assertTrue("item there after op", isSensorThere(tw, scwa3));
			doUndo();
			assertTrue("item back again after op", isSensorThere(tw, scwa1));
			assertTrue("item back again after op", isSensorThere(tw, scw2));
			assertTrue("item back again after op", isSensorThere(tw, scwa3));

			// //////////////////////////
			// now for TMA!

			// right, now let's try to delete a sensor item
			parentLayer = new Layer[]
			{ mwa };
			c3 = new MyCutItem(new Editable[]
			{ tcwa1 }, clipboard, parentLayer, updateLayers, parentLayer);
			assertTrue("item there before op", isContactThere(tw, tcwa1));
			assertTrue("item there before op", isContactThere(tw, tcwa2));
			assertTrue("item there before op", isContactThere(tw, tcwa3));
			c3.run();
			assertFalse("item not there after op", isContactThere(tw, tcwa1));
			assertTrue("item there after op", isContactThere(tw, tcwa2));
			assertTrue("item there after op", isContactThere(tw, tcwa3));
			doUndo();
			assertTrue("item back again after op", isContactThere(tw, tcwa1));
			assertTrue("item back again after op", isContactThere(tw, tcwa2));
			assertTrue("item back again after op", isContactThere(tw, tcwa3));
			// now let's try two items
			parentLayer = new Layer[]
			{ mwa, mwa };
			c3 = new MyCutItem(new Editable[]
			{ tcwa1, tcwa2 }, clipboard, parentLayer, updateLayers, parentLayer);
			assertTrue("item there before op", isContactThere(tw, tcwa1));
			assertTrue("item there before op", isContactThere(tw, tcwa2));
			assertTrue("item there before op", isContactThere(tw, tcwa3));
			c3.run();
			assertFalse("item not there after op", isContactThere(tw, tcwa1));
			assertFalse("item not there after op", isContactThere(tw, tcwa2));
			assertTrue("item there after op", isContactThere(tw, tcwa3));
			doUndo();
			assertTrue("item back again after op", isContactThere(tw, tcwa1));
			assertTrue("item back again after op", isContactThere(tw, tcwa2));
			assertTrue("item back again after op", isContactThere(tw, tcwa3));
			// now let's try two items in different layers
			parentLayer = new Layer[]
			{ mwa, mw };
			c3 = new MyCutItem(new Editable[]
			{ tcwa1, tcw2 }, clipboard, parentLayer, updateLayers, parentLayer);
			assertTrue("item there before op", isContactThere(tw, tcwa1));
			assertTrue("item there before op", isContactThere(tw, tcw2));
			assertTrue("item there before op", isContactThere(tw, tcwa3));
			c3.run();
			assertFalse("item not there after op", isContactThere(tw, tcwa1));
			assertFalse("item not there after op", isContactThere(tw, tcw2));
			assertTrue("item there after op", isContactThere(tw, tcwa3));
			doUndo();
			assertTrue("item back again after op", isContactThere(tw, tcwa1));
			assertTrue("item back again after op", isContactThere(tw, tcw2));
			assertTrue("item back again after op", isContactThere(tw, tcwa3));

		}

	}

	// /////////////////////////////////
	// nested classes
	// ////////////////////////////////

	// ////////////////////////////////////////////
	//
	// ///////////////////////////////////////////////
	public static class DeleteItem extends CutItem
	{
		

		public DeleteItem(final Editable[] data, final Layer[] theParent, final Layers theLayers,
				final Layer[] updateLayer)
		{
			super(data, null, theParent, theLayers, updateLayer);

			// formatting
			super.setText("Delete " + toString());

			
			setActionDefinitionId(ActionFactory.DELETE.getCommandId());
			
			// and the icon
			setImageIcon();

		}

		protected void setImageIcon()
		{
			super.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
					.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		}


		public String toString()
		{
			String res = "";
			if (_data.length > 1)
				res += _data.length + " selected items";
			else
				res += _data[0].getName();
			return res;
		}

	}
}
