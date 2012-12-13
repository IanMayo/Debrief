package com.planetmayo.debrief.satc.model.contributions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.planetmayo.debrief.satc.model.GeoPoint;
import com.planetmayo.debrief.satc.model.states.ProblemSpace;
import com.planetmayo.debrief.satc.model.states.BaseRange.IncompatibleStateException;
import com.planetmayo.debrief.satc.support.SupportServices;
import com.planetmayo.debrief.satc.util.GeoSupport;

public class FrequencyMeasurementContribution extends BaseContribution
{
	private static final long serialVersionUID = 1L;

	public static final String FREQUENCY_ERROR = "frequencyError";

	/**
	 * the allowable frequency error (in hertz)
	 * 
	 */
	private Double _frequencyError = 0d;

	/**
	 * the set of measurements we store
	 * 
	 */
	private ArrayList<FMeasurement> _measurements = new ArrayList<FMeasurement>();

	@Override
	public void actUpon(ProblemSpace space) throws IncompatibleStateException
	{
		// do something...
	}

	public void addEstimate(double lat, double lon, Date date, double brg,
			double range)
	{
		GeoPoint loc = new GeoPoint(lat, lon);
		FMeasurement measure = new FMeasurement(loc, brg, date, range);
		addThis(measure);
		firePropertyChange(ESTIMATE, _measurements.size(), _measurements.size());
	}

	/**
	 * store this new measurement
	 * 
	 * @param measure
	 */
	public void addThis(FMeasurement measure)
	{
		// extend the time period accordingly
		if (this.getStartDate() == null)
		{
			this.setStartDate(measure._time);
			this.setFinishDate(measure._time);
		}
		else
		{
			long newTime = measure._time.getTime();
			if (this.getStartDate().getTime() > newTime)
				this.setStartDate(measure._time);
			if (this.getFinishDate().getTime() < newTime)
				this.setFinishDate(measure._time);
		}

		_measurements.add(measure);
	}

	public double getFrequencyError()
	{
		return _frequencyError;
	}

	@Override
	public ContributionDataType getDataType()
	{
		return ContributionDataType.MEASUREMENT;
	}

	public int getEstimate()
	{
		return _measurements.size();
	}

	/**
	 * whether this contribution has any measurements yet
	 * 
	 * @return
	 */
	public boolean hasData()
	{
		return _measurements.size() > 0;
	}

	public void loadFrom(List<String> lines)
	{
		// load from this source
		// ;;IGNORE YYMMDD HHMMSS IGNORE IGNORE LAT_DEG LAT_MIN LAT_SEC LAT_HEM
		// LONG_DEG LONG_MIN LONG_SEC LONG_HEM BEARING MAX_RNG
		// ;SENSOR: 100112 121329 SENSOR @A 0 3 57.38 S 30 0 8.65 W 1.5 15000

		// Read File Line By Line
		for (String strLine : lines)
		{
			// hey, is this a comment line?
			if (strLine.startsWith(";;"))
			{
				continue;
			}
			// ok, get parseing it
			String[] elements = strLine.split("\\s+");

			// now the date
			String date = elements[1];

			// and the time
			String time = elements[2];

			String latDegs = elements[5];
			String latMins = elements[6];
			String latSecs = elements[7];
			String latHemi = elements[8];

			String lonDegs = elements[9];
			String lonMins = elements[10];
			String lonSecs = elements[11];
			String lonHemi = elements[12];

			// and the beraing
			String bearing = elements[13];

			// and the range
			String range = elements[14];

			// ok,now construct the date=time
			Date theDate = SupportServices.INSTANCE.parseDate("yyMMdd hhmmss", date
					+ " " + time);

			// and the location
			double lat = Double.valueOf(latDegs) + Double.valueOf(latMins) / 60d
					+ Double.valueOf(latSecs) / 60d / 60d;
			if (latHemi.toUpperCase().equals("S"))
				lat = -lat;
			double lon = Double.valueOf(lonDegs) + Double.valueOf(lonMins) / 60d
					+ Double.valueOf(lonSecs) / 60d / 60d;
			if (lonHemi.toUpperCase().equals("W"))
				lon = -lon;

			GeoPoint theLoc = new GeoPoint(lat, lon);
			FMeasurement measure = new FMeasurement(theLoc, Math.toRadians(Double.valueOf(bearing)),
					theDate, GeoSupport.m2deg(Double.valueOf(range)));

			addThis(measure);

		}
		this.setFrequencyError(2d);
		// TODO: set the start/end times = just for tidiness
	}

	public void setFrequencyError(double frequencyError)
	{
		double old = _frequencyError;
		this._frequencyError = frequencyError;
		firePropertyChange(FREQUENCY_ERROR, old, frequencyError);
		firePropertyChange(HARD_CONSTRAINTS, old, frequencyError);
	}
	
	/**
	 * utility class for storing a measurement
	 * 
	 * @author ian
	 * 
	 */
	public static class FMeasurement
	{
		@SuppressWarnings("unused")
		private final GeoPoint _origin;
		@SuppressWarnings("unused")
		private final double _bearingAngle;
		private final Date _time;
		/**
		 * the (optional) maximum range for this measurement
		 * 
		 */
		@SuppressWarnings("unused")
		private final Double _theRange;

		public FMeasurement(GeoPoint loc, double bearing, Date time, Double theRange)
		{
			_origin = loc;
			_bearingAngle = bearing;
			_time = time;
			_theRange = theRange;
		}
	}	
}
