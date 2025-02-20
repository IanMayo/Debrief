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
package org.mwc.debrief.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@Suite.SuiteClasses({
	  
	Debrief.GUI.Tote.Painters.Highlighters.RangeHighlighter.testMe.class,
	Debrief.GUI.Tote.Painters.Highlighters.SymbolHighlighter.testMe.class,
  Debrief.GUI.Tote.Painters.PainterManager.testMe.class,
  Debrief.GUI.Tote.Painters.RelativePainter.testMe.class,
  Debrief.GUI.Tote.Painters.SnailDrawFix.testMe.class,
  Debrief.GUI.Tote.Painters.SnailPainter.testMe.class,
  Debrief.GUI.Tote.Painters.TotePainter.testMe.class,
  Debrief.GUI.Tote.StepControl.testMe.class,
  Debrief.GUI.Tote.StepControl.testMe.class,
  Debrief.GUI.Tote.Swing.SwingStepControl.testStepper.class,
  Debrief.ReaderWriter.FlatFile.FlatFileExporter.testMe.class,
  Debrief.ReaderWriter.Replay.ImportFix.testImport.class,
  Debrief.ReaderWriter.Replay.ImportNarrative.testImport.class,
  Debrief.ReaderWriter.Replay.ImportNarrative2.testImport.class,
  //Debrief.ReaderWriter.Replay.ImportPolygon.TestImport.class,
  //Debrief.ReaderWriter.Replay.ImportRectangle.TestImport.class,
  Debrief.ReaderWriter.Replay.ImportReplay.testImport.class,
  //Debrief.ReaderWriter.Replay.ImportSensor2.testMe.class,
  //Debrief.ReaderWriter.Replay.ImportSensor3.testMe.class,
  Debrief.ReaderWriter.Replay.ImportTMA_Pos.testImportTMA_POS.class,
  Debrief.ReaderWriter.Replay.ImportTMA_RngBrg.testImportTMA_RngBrg.class,
  Debrief.ReaderWriter.XML.Shapes.LabelHandler.testMe.class,
  Debrief.ReaderWriter.XML.Tactical.TMAContactHandler.testIt.class,
  Debrief.ReaderWriter.Word.ImportWord.TestImportAIS.class,
  Debrief.Tools.FilterOperations.ReformatFixes.testListOfProperties.class,
  Debrief.Tools.Palette.BuoyPatterns.ArcBuilder.testMe.class,
  Debrief.Tools.Palette.BuoyPatterns.BarrierBuilder.testMe.class,
  Debrief.Tools.Palette.BuoyPatterns.CircleBuilder.testMe.class,
  Debrief.Tools.Palette.BuoyPatterns.FieldBuilder.TestMe.class,
  Debrief.Tools.Palette.BuoyPatterns.WedgeBuilder.testMe.class,
  Debrief.Wrappers.BuoyPatternWrapper.testMe.class,
  Debrief.Wrappers.FixWrapper.testMe.class,
  Debrief.Wrappers.LabelWrapper.testMe.class,
  Debrief.Wrappers.SensorContactWrapper.testSensorContact.class,
  Debrief.Wrappers.SensorWrapper.testSensors.class,
  Debrief.Wrappers.ShapeWrapper.testMe.class,
  Debrief.Wrappers.TMAContactWrapper.TestSensorContact.class,
  Debrief.Wrappers.TMAWrapper.testSolutions.class,
  Debrief.Wrappers.TacticalDataWrapper.testMe.class,
  Debrief.Wrappers.Track.Doublet.testCalc.class,
  Debrief.Wrappers.Track.TrackSegment.testListMgt.class,
  Debrief.Wrappers.Track.TrackWrapper_Test.class,
  Debrief.Wrappers.Track.WormInHoleOffset.testMe.class,
  MWC.Algorithms.Conversions.ConversionsTest.class,
  MWC.Algorithms.EarthModels.CompletelyFlatEarth.FlatEarthTest.class,
  MWC.GUI.BaseLayer.BaseLayerTest.class,
  MWC.GUI.Canvas.CanvasAdaptor.testMe.class,
  MWC.GUI.Canvas.Swing.SwingCanvas.CanvasTest.class,
  MWC.GUI.Chart.Painters.CoastPainter.CoastPainterTest.class,
  MWC.GUI.Chart.Painters.Grid4WPainter.Grid4WTest.class,
  MWC.GUI.Chart.Painters.GridPainter.GridPainterTest.class,
  MWC.GUI.Chart.Painters.LocalGridPainter.LocalGridPainterTest.class,
  MWC.GUI.Chart.Painters.ScalePainter.ScalePainterTest.class,
  MWC.GUI.Chart.Painters.SpatialRasterPainter.RasterPainterTest.class,
  MWC.GUI.ETOPO.ETOPO_2_Minute.Etopo2Test.class,
  MWC.GUI.Layers.LayersTest.class,
  MWC.GUI.Plottables.Grid4WTest.class,
  MWC.GUI.S57.S57Layer.GridPainterTest.class,
  MWC.GUI.Shapes.ArcShape.CircleTest.class,
  MWC.GUI.Shapes.CircleShape.CircleTest.class,
  MWC.GUI.Shapes.EllipseShape.EllipseTest.class,
  MWC.GUI.Shapes.FurthestOnCircleShape.WheelTest.class,
  MWC.GUI.Shapes.LineShape.LineTest.class,
  MWC.GUI.Shapes.RangeRingShape.WheelTest.class,
  MWC.GUI.Shapes.RectangleShape.RectangleTest.class,
  MWC.GUI.Shapes.Symbols.Geog.ReferenceSym.ReferenceTest.class,
  MWC.GUI.Shapes.Symbols.Geog.WreckSym.WreckTest.class,
  MWC.GUI.Shapes.Symbols.SymbolFactory.SymFactoryTest.class,
  MWC.GUI.Shapes.WheelShape.WheelTest.class,
  MWC.GenericData.Duration.DurationTest.class,
  MWC.GenericData.WorldAcceleration.AccelTest.class,
  MWC.GenericData.WorldArea.WorldAreaTest.class,
  MWC.GenericData.WorldDistance.DistWithUnitsTest.class,
  MWC.GenericData.WorldLocation.LocationTest.class,
  MWC.GenericData.WorldPath.PathTest.class,
  MWC.GenericData.WorldSpeed.SpeedTest.class,
  MWC.GenericData.WorldVector.VectorTest.class,
  MWC.GenericData.TimePeriod.TestTimePeriod.class,
  MWC.TacticalData.NarrativeEntry.testMe.class,
  MWC.Utilities.Errors.Testing.EmptyTestCase.class,
  MWC.Utilities.Errors.Testing.TestEmpty.class,
  MWC.Utilities.Errors.Testing.TestEmpty.InnerTestEmpty.class,
  MWC.Utilities.ReaderWriter.json.GNDDocHandler.TestJSON.class,
  MWC.Utilities.ReaderWriter.json.GNDStore.TestDatabase.class,
  MWC.Utilities.TextFormatting.BriefFormatLocation.FormatLocationTest.class,
  MWC.Utilities.TextFormatting.DebriefFormatDateTime.DebriefFormatTest.class,
  org.mwc.cmap.core.CorePlugin.ClipboardTest.class,
  org.mwc.cmap.core.DataTypes.TrackData.TrackManager.testTrackManager.class,
  org.mwc.cmap.core.operations.RightClickCutCopyAdaptor.testCutPaste.class,
  org.mwc.cmap.core.property_support.RightClickSupport.testMe.class,
  org.mwc.cmap.core.ui_support.PartMonitor.TestNarrativeViewer.class,
})

@RunWith(Suite.class)
public class AllTests {

}

