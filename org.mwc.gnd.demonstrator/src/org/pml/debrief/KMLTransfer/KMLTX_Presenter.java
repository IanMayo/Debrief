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
package org.pml.debrief.KMLTransfer;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import MWC.GenericData.HiResDate;
import MWC.GenericData.TimePeriod;
import MWC.GenericData.WorldArea;
import MWC.GenericData.WorldLocation;
import MWC.Utilities.ReaderWriter.XML.MWCXMLReader;

public class KMLTX_Presenter
{

	private final static String filePath = "/Users/ianmayo/Downloads/portland";
	private static final String DATABASE_ROOT = "jdbc:postgresql://127.0.0.1/ais";
	private static Connection _conn;
	private static PreparedStatement sql;
	private static WorldArea limits;
	private static HashMap<Integer, Vector<NumberedTimePeriod>> map;

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		map = new HashMap<Integer, Vector<NumberedTimePeriod>>();

		try
		{

			WorldLocation tl = new WorldLocation(50.863, -2.5, 0d);
			WorldLocation br = new WorldLocation(50.45, -0.4, 0d);
			limits = new WorldArea(tl, br);

			// check we have data
			File sourceP = new File(filePath);
			if (!sourceP.exists())
			{
				throw new RuntimeException("data directory not found");
			}

			// check we have a database
			connectToDatabase();

			// sort out a helper to read the XML
			MyHandler handler = new MyHandler()
			{
				public void writeThis(String name2, Date date2, Point2D coords2,
						Integer index2, Double course2, Double speed2, String color)
				{
					try
					{
						writeThisToDb(name2, date2, coords2, index2, course2, speed2, color);
					}
					catch (SQLException e)
					{
						e.printStackTrace();
						System.exit(1);
					}
				}
			};

			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();

			//
			// XMLReader parser = XMLReaderFactory
			// .createXMLReader("org.apache.xerces.parsers.SAXParser");
			// parser.setContentHandler(saxer);

			// see about format
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			// ditch gash
			String clearTracks = "delete from wednesday";
			String clearDatasets = "delete from datasets";

			_conn.createStatement().execute(clearTracks);
			_conn.createStatement().execute(clearDatasets);

			// String str =
			// "insert into tracks (latval, longval) values (32.3, 22.4);";
			// Statement st = _conn.createStatement();
			// st.execute(str);
			// System.exit(0);

			String query = "insert into wednesday (dateval, nameval, latval, longval,"
					+ " courseval, speedval, mmsi, dataset, color) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
			System.out.println("query will be:" + query);
			sql = _conn.prepareStatement(query);

			String query2 = "insert into datasets (ivalue, starttime, endtime, mmsi,"
					+ " length, vname) VALUES (?, ?, ?, ?, ?, ?);";
			System.out.println("query2 will be:" + query2);
			PreparedStatement dataquery = _conn.prepareStatement(query2);

			// start looping through files
			File[] fList = sourceP.listFiles();
			Arrays.sort(fList, lastModified);
			int len = fList.length;
			for (int i = 0; i < len; i++)
			{
				File thisF = fList[i];

				// check it's not the duff file
				if (thisF.getName().equals(".DS_Store"))
					continue;

				// unzip it to get the KML
				InputStream is = null;
				try
				{
					ZipFile zip = new ZipFile(thisF);
					ZipEntry contents = zip.entries().nextElement();
					is = zip.getInputStream(contents);
				}
				catch (ZipException zip)
				{
					System.err.println("Failed to read datafile:" + thisF.getName());
				}

				if (is != null)
				{

					// sort out the filename snap_2011-04-11_08/32/00
					String[] legs = thisF.getName().split("_");
					String timeStr = legs[2].substring(0, 8);
					Date theDate = df.parse(legs[1] + " " + timeStr);
					handler.setDate(theDate);

					files++;

//					System.out.println("==" + i + " of " + fList.length + " at:"
//							+ new Date());

					// right, go for it
					processThisFile(is, parser, handler);
				}
			}

			System.out.println("output " + places + " for " + files + " files");

			// now output the datasets
			outputDatesets(map, dataquery);

		}
		catch (RuntimeException re)
		{
			re.printStackTrace();
		}
		catch (ZipException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (SAXException e)
		{
			e.printStackTrace();
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		catch (ParserConfigurationException e)
		{
			e.printStackTrace();
		}
		finally
		{

			// close the databse
			if (_conn != null)
			{
				try
				{
					System.out.println("closing database");
					_conn.close();
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
			}

		}
	}

	private static final Comparator<File> lastModified = new Comparator<File>()
	{
		@Override
		public int compare(File o1, File o2)
		{
			int res = o1.getName().compareTo(o2.getName());
			return res;
		}
	};

	private static void outputDatesets(
			HashMap<Integer, Vector<NumberedTimePeriod>> map2,
			PreparedStatement dataquery) throws SQLException
	{
		Iterator<Integer> iter = map2.keySet().iterator();
		while (iter.hasNext())
		{
			Integer integer = (Integer) iter.next();
			Vector<NumberedTimePeriod> periods = map2.get(integer);
			Iterator<NumberedTimePeriod> pIter = periods.iterator();
			while (pIter.hasNext())
			{
				KMLTX_Presenter.NumberedTimePeriod thisP = (KMLTX_Presenter.NumberedTimePeriod) pIter
						.next();
				dataquery.setInt(1, thisP.getIndex());
				dataquery.setTimestamp(2, new Timestamp(thisP.getStartDTG().getDate()
						.getTime()));
				dataquery.setTimestamp(3, new Timestamp(thisP.getEndDTG().getDate()
						.getTime()));
				dataquery.setInt(4, integer);
				dataquery.setInt(5, thisP.getNumPoints());
				dataquery.setString(6, thisP.getName());

				dataquery.executeUpdate();
			}
		}
	}

	static HashMap<Integer, WorldLocation> lastLocs = new HashMap<Integer, WorldLocation>();

	protected static void writeThisToDb(String name2, Date date2,
			Point2D coords2, Integer index2, Double course2, Double speed2,
			String color) throws SQLException
	{
		// are we in zone?
		WorldLocation loc = new WorldLocation(coords2.getY(), coords2.getX(), 0d);
		if (!limits.contains(loc))
			return;

		// find out if this is a new or old data track
		int thisDataIndex = getIndexFor(name2, index2, date2);

		// ok, have a look at the last location
		WorldLocation lastLoc = lastLocs.get(thisDataIndex);

		if (lastLoc != null)
		{
			if (lastLoc.equals(loc))
			{
				// don't bother it's just a duplicate
				return;
			}
		}

		// remember this loc
		lastLocs.put(thisDataIndex, loc);

		// String query =
		// "insert into AIS_tracks (daveVal, name, latVal, longVal, courseVal, speedVal) VALUES (";
		int ctr = 1;
		sql.setTimestamp(ctr++, new java.sql.Timestamp(date2.getTime()));
		sql.setString(ctr++, name2);
		sql.setDouble(ctr++, coords2.getY());
		sql.setDouble(ctr++, coords2.getX());
		sql.setDouble(ctr++, course2);
		sql.setDouble(ctr++, speed2);
		sql.setInt(ctr++, index2);
		sql.setInt(ctr++, thisDataIndex);
		sql.setString(ctr++, color);
		 sql.executeUpdate();

		places++;
	}

	private static class NumberedTimePeriod extends TimePeriod.BaseTimePeriod
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		static int ctr = 1;
		final private int _myIndex;
		private int _numPoints = 0;
		final private String _myName;

		public NumberedTimePeriod(HiResDate startD, String name)
		{
			super(startD, startD);
			_myName = name;
			_myIndex = ctr++;
		}

		public String getName()
		{
			return _myName;
		}

		public void increment()
		{
			_numPoints++;
		}

		public int getNumPoints()
		{
			return _numPoints;
		}

		public int getIndex()
		{
			return _myIndex;
		}
	}

	private static int getIndexFor(String name2, Integer mmsi, Date date2)
	{
	if (name2.contains("ALBE"))
	{
//		System.out.println("time:" + date2 + " mmsi:" + mmsi + " name:" + name2);
	}

		Vector<NumberedTimePeriod> periods = map.get(mmsi);

		if (periods == null)
		{
			periods = new Vector<NumberedTimePeriod>();
			map.put(mmsi, periods);
		}

		NumberedTimePeriod timeP = null;

		if (periods.size() > 0)
		{
			timeP = periods.lastElement();
			Date lastTime = timeP.getEndDTG().getDate();


			// is it too far back?
			long delta = date2.getTime() - lastTime.getTime();
			if (delta < 1000 * 60 * 30)
			{
				// cool, in period. extend it
				timeP.setEndDTG(new HiResDate(date2));
			}
			else
			{
				// naah, too late. put it in another time period
				timeP = null;
				
				if (name2.contains("ALBE"))
				{
					if (timeP == null)
						System.out.println("time:" + date2 + " state:" + timeP == null);
				}
			}

		}

		if (timeP == null)
		{
			// ok, create a new period for this time
			timeP = new NumberedTimePeriod(new HiResDate(date2), name2);
			periods.add(timeP);
		}

		// increment his counter
		timeP.increment();

		return timeP.getIndex();
	}

	private static void connectToDatabase()
	{
		// driver first
		try
		{
			Class.forName("org.postgresql.Driver");
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException("Failed to load database driver");
		}

		try
		{
			String url = DATABASE_ROOT;
			final String password = System.getenv("pg_pwd");
			if (password == null)
				throw new RuntimeException("database password missing");
			_conn = DriverManager.getConnection(url, "postgres", password);

			// also tell the connection about our new custom data types
			((org.postgresql.PGConnection) _conn).addDataType("geometry",
					org.postgis.PGgeometry.class);
			((org.postgresql.PGConnection) _conn).addDataType("box3d",
					org.postgis.PGbox3d.class);
		}
		catch (SQLException e)
		{
			throw new RuntimeException("failed to create connection");
		}

	}

	public static int places = 0;
	public static int files = 0;

	protected static abstract class MyHandler extends DefaultHandler
	{
		private String name;
		private Point2D coords;
		private Double course;
		private Double speed;
		private Integer mmsi;
		private Date date;
		private String color;

		@Override
		public void endElement(String arg0, String arg1, String arg2)
				throws SAXException
		{
			super.endElement(arg0, arg1, arg2);
			
				// ok, check our datai
			if (name != null)
			{
				if (coords != null)
				{
					if (course != null)
					{
						if (mmsi != null)
						{
							writeThis(name, date, coords, mmsi, course, speed, color);
							
							if(name.contains("ALBEMARLE ISLAND"))
							{
								System.out.println("name:" + name  + " date:" + date + " mmsi:" + mmsi );
							}

							name = null;
							coords = null;
							course = null;
							speed = null;
							mmsi = null;
							color = null;
						}
					}
				}
			}
		}

		abstract public void writeThis(String name2, Date date2, Point2D coords2,
				Integer index2, Double course2, Double speed2, String color2);

		public void setDate(Date finalDate)
		{
			date = finalDate;
		}

		@Override
		public void characters(final char[] ch, final int start, final int length)
				throws SAXException
		{
			String data = "";
			if (isName)
			{
				String name = new String(ch, start, length);
				if (name.length() > 0)
				{
					if (name.equals("MarineTraffic"))
					{
						// just ignore it
					}
					else
					{
						this.name = name;
					}
				}
				isName = false;
			}
			else if (isCoords)
			{
				String[] split = null;
				try
				{
					data = new String(ch, start, length);
					split = data.split(",");
					double longVal = MWCXMLReader.readThisDouble(split[0]);
					double latVal = MWCXMLReader.readThisDouble(split[1]);
					coords = new Point2D.Double(longVal, latVal);
				}
				catch (final ParseException e)
				{
					System.err.println("number format prob reading pos for 1:" + split[0]
							+ " or 2:" + split[1]);
				}
				catch (java.lang.ArrayIndexOutOfBoundsException aw)
				{
					System.err.println("array index prob reading pos for " + name
							+ " from " + data);
				}
				isCoords = false;
			}
			else if (isStyle)
			{
				String details2 = "";
				details2 = new String(ch, start, length);

				int startChar = details2.indexOf("#") + 1;
				int endCharA = details2.indexOf("0", startChar);
				int endCharB = details2.indexOf("1", startChar);
				int endChar;

				if (endCharA == -1)
					endChar = endCharB;
				else if (endCharB == -1)
					endChar = endCharA;
				else
				{
					endChar = Math.min(endCharA, endCharB);
				}

				// just check we found anything
				if (endChar == -1)
					endChar = details2.length();

				try
				{
					color = details2.substring(startChar, endChar);

					if (color.contains("_"))
						System.out.println("here");

				}
				catch (StringIndexOutOfBoundsException re)
				{
					System.err.println("start:" + startChar + " end:" + endChar + " :"
							+ details2);
				}
				isStyle = false;
			}
			else if (isDesc)
			{
				String details = "";
				try
				{
					details = new String(ch, start, length);

					// start off with course & speed
					int startStr = details.indexOf("&nbsp;") + 6;
					int endStr = details.indexOf("&deg;");
					if (endStr == -1)
						return;

					String subStr = details.substring(startStr, endStr);
					String[] components = subStr.split(" ");
					course = MWCXMLReader.readThisDouble(components[3]);
					speed = MWCXMLReader.readThisDouble(components[0]);

					// now the mmsi
					startStr = details.indexOf("mmsi=");
					if (startStr == -1)
						return;
					endStr = details.indexOf("\"", startStr);
					subStr = details.substring(startStr + "mmsi=".length(), endStr);
					mmsi = Integer.valueOf(subStr);
				}
				catch (java.lang.StringIndexOutOfBoundsException aw)
				{
					System.out.println("prob reading desc for " + name);
				} catch (final ParseException e) {
					e.printStackTrace();
				}
				isDesc = false;
			}
		}

		boolean isName = false;
		boolean isCoords = false;
		boolean isDesc = false;
		boolean foundPlacemark = false;
		private boolean isStyle;

		@Override
		public void startElement(String nsURI, String strippedName, String tagName,
				Attributes attributes) throws SAXException
		{
			isName = isCoords = isDesc = false;

			if (tagName.equals("Placemark"))
			{
				foundPlacemark = true;
				return;
			}
			if (foundPlacemark)
			{
				if (tagName.equals("name"))
				{
					isName = true;
					// ok - go for new placemark
					places++;
				}
				else if (tagName.equals("coordinates"))
				{
					isCoords = true;
				}
				else if (tagName.equals("description"))
				{
					isDesc = true;
				}
				else if (tagName.equals("styleUrl"))
				{
					isStyle = true;
				}
			}

		}

	}

	private static void processThisFile(InputStream is, SAXParser parser,
			DefaultHandler handler) throws ZipException, IOException, SAXException
	{
		parser.parse(is, handler);

		// process the KML

		// loop through the observations

		// create position for this obs

		// extract the other fields

		// add this record
	}

}
