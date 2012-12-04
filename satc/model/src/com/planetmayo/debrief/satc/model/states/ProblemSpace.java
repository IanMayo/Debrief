package com.planetmayo.debrief.satc.model.states;

import java.util.Collection;
import java.util.Date;
import java.util.TreeMap;

import com.planetmayo.debrief.satc.model.VehicleType;
import com.planetmayo.debrief.satc.model.states.BaseRange.IncompatibleStateException;
import com.planetmayo.debrief.satc.util.ObjectUtils;

public class ProblemSpace
{
	public static final String VEHICLE_TYPE = "vType";
	
	/**
	 * this map of bounded states, stored by time
	 * 
	 */
	private TreeMap<Date, BoundedState> _boundedStates;
	
	/**
	 * the performance characeristics of the subject vehicle
	 * 
	 */
	private VehicleType _vType;

	public ProblemSpace()
	{
		_boundedStates = new TreeMap<Date, BoundedState>();
	}

	/**
	 * set the subject vehicle type
	 * 
	 * @param vType
	 */
	public void setVehicleType(VehicleType vType)
	{
		_vType = vType;
	}

	/**
	 * get the subject vehicle type
	 * 
	 * @return
	 */
	public VehicleType getVehicleType()
	{
		return _vType;
	}

	/**
	 * add a new bounded state
	 * 
	 * @param newState
	 */
	public void add(BoundedState newState) throws IncompatibleStateException
	{

		// check if this has a date - if it doesn't we'll give it our start/end
		// times
		if (newState.getTime() == null)
		{
			if (size() == 0)
				throw new RuntimeException(
						"we can't accept a null time state, since we don't know our period yet");

			// ok, we'll just apply this state to our start and end times
			_boundedStates.get(_boundedStates.firstKey()).constrainTo(newState);
			_boundedStates.get(_boundedStates.lastKey()).constrainTo(newState);
		}
		else
			_boundedStates.put(newState.getTime(), newState);

		// ok, constrain the new state to our vehicle performance, if we have one
		if (_vType != null)
		{
			SpeedRange sr = new SpeedRange(_vType.getMinSpeed(), _vType.getMaxSpeed());
			newState.constrainTo(sr);
		}
	}

	/**
	 * forget our set of bounded states
	 * 
	 */
	public void clear()
	{
		_boundedStates.clear();
	}

	/**
	 * return the bounded state at this time (or null)
	 * 
	 * @param theTime
	 *          the time we're searching for
	 * @return
	 */
	public BoundedState getBoundedStateAt(Date theTime)
	{
		return _boundedStates.get(theTime);
	}
	
	/**
	 * return the bounded state at this time (or null)
	 * 
	 * @param theTime
	 *          the time we're searching for
	 * @return
	 */
	public Collection<BoundedState> getBoundedStatesBetween(Date startDate, Date finishDate)
	{
		if (_boundedStates.isEmpty()) 
		{
			return _boundedStates.values();
		}
		startDate = ObjectUtils.safe(startDate, _boundedStates.firstKey());
		finishDate = ObjectUtils.safe(finishDate, _boundedStates.lastKey());
		// TODO:  the two 'true' attributes had to be removed from the next line to make it GWT compliant
		return _boundedStates.subMap(startDate, finishDate).values();
	}	

	protected Date getFinishDate()
	{
		return _boundedStates.isEmpty() ? null : _boundedStates.lastKey();
	}

	protected Date getStartDate()
	{
		return _boundedStates.isEmpty() ? null : _boundedStates.firstKey();
	}

	public int size()
	{
		return _boundedStates.size();
	}

	/**
	 * iterator through the set of bounded states
	 * 
	 * @return
	 */
	public Collection<BoundedState> states()
	{
		return _boundedStates.values();
	}
}
