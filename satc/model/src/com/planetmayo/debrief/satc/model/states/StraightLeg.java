package com.planetmayo.debrief.satc.model.states;

import java.util.ArrayList;
import java.util.Iterator;

import com.planetmayo.debrief.satc.util.MakeGrid;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.util.AffineTransformation;

public class StraightLeg
{
	/*
	 * the routeimport com.planetmayo.debrief.satc.model.states.StraightLeg;
 permutations through the leg This array will always be
	 * rectangular
	 */
	Route[][] myRoutes;

	/**
	 * how many points there are in the start polygon
	 * 
	 */
	private int _startLen;

	/**
	 * how many points there are in the end polygon
	 * 
	 */
	private int _endLen;

	/** a name for the leg
	 * 
	 */
	private final String _name;

	/** the set of bounded states
	 * 
	 */
	private final ArrayList<BoundedState> _states;

	/** create a straight leg.
	 * 
	 * @param name what to call the leg
	 * @param states the set of bounded states that comprise the leg
	 */
	public StraightLeg(String name, ArrayList<BoundedState> states)
	{
		_states = states;
		_name = name;

		// how many cells per end-state?
		int gridNum = 5;

		// produce the grid of cells
		ArrayList<Point> startP = MakeGrid.ST_Tile(getFirst().getLocation()
				.getGeometry(), gridNum, 6);
		ArrayList<Point> endP = MakeGrid.ST_Tile(getLast().getLocation()
				.getGeometry(), gridNum, 6);

		// ok, now generate the array of routes
		_startLen = startP.size();
		_endLen = endP.size();

		// ok, create the array
		myRoutes = new Route[_startLen][_endLen];

		// now populate it
		int ctr = 1;
		for (int i = 0; i < _startLen; i++)
		{
			for (int j = 0; j < _endLen; j++)
			{
				String thisName = _name + "_" + ctr++;
				Route newRoute = new Route(thisName, startP.get(i), getFirst()
						.getTime(), endP.get(j), getLast().getTime());
				
				System.out.println(startP.get(i) + " " + endP.get(j));

				// tell the route to decimate itself
				newRoute.generateSegments(states);

				// and store the route
				myRoutes[i][j] = newRoute;
			}
		}
	}

	private BoundedState getFirst()
	{
		return _states.get(0);
	}

	private BoundedState getLast()
	{
		return _states.get(_states.size() - 1);
	}

	/**
	 * use a simple speed/time decision to decide if it's possible to navigate a
	 * route
	 */
	public void decideAchievableRoutes()
	{
		StraightLeg.RouteOperator speed = new RouteOperator()
		{

			@Override
			public void process(Route theRoute)
			{
				double distance = theRoute.getDistance();
				double elapsed = theRoute.getElapsedTime();
				double speed = distance / elapsed;

				SpeedRange speedR = _states.get(0).getSpeed();
				if(!speedR.allows(speed))
				{
					theRoute.setImpossible();
				}
			}
		};

		StraightLeg.RouteOperator scaledPolygons = new RouteOperator()
		{

			@Override
			public void process(Route theRoute)
			{
				// do we already know this isn't possible?
				if (!theRoute.isPossible())
					return;

				// bugger, we'll have to get on with our hard sums then

				// sort out the origin.
				State startState = theRoute.getStates().get(0);
				Coordinate startCoord = startState.getLocation().getCoordinate();

				// also sort out the end state
				State endState = theRoute.getStates().get(
						theRoute.getStates().size() - 1);
				Point endPt = endState.getLocation();

				// remeber the start time
				long tZero = startState.getTime().getTime();

				// how long is the total run?
				long elapsed = theRoute.getElapsedTime();

				// loop through our states
				Iterator<BoundedState> iter = _states.iterator();
				while (iter.hasNext())
				{
					BoundedState thisB = iter.next();

					LocationRange thisL = thisB.getLocation();
					if (thisL != null)
					{
						// ok, what's the time difference
						long tDelta = (thisB.getTime().getTime() - tZero) / 1000;

						// is this our first state
						if (tDelta > 0)
						{
							double scale = (1d * elapsed) / tDelta;

							// ok, project the shape forwards
							AffineTransformation st = AffineTransformation.scaleInstance(
									scale, scale, startCoord.x, startCoord.y);

							// ok, apply the transform to the location
							Geometry newGeom = st.transform(thisL.getGeometry());

							// see if the end point is in the new geometry
							if (newGeom.disjoint(endPt))
							{
								theRoute.setImpossible();
								break;
							}

						}

					}
				}
			}

		};

		applyToRoutes(speed);
		applyToRoutes(scaledPolygons);
	}

	/**
	 * apply the operator to all my routes
	 * 
	 * @param operator
	 */
	public void applyToRoutes(StraightLeg.RouteOperator operator)
	{
		for (int i = 0; i < _startLen; i++)
		{
			for (int j = 0; j < _endLen; j++)
			{
				operator.process(myRoutes[i][j]);
			}
		}
	}

	/**
	 * find out how many achievable routes there are through the area
	 * 
	 * @return how many
	 */
	public int getNumAchievable()
	{

		StraightLeg.CountPossible isPossible = new CountPossible();
		applyToRoutes(isPossible);

		return isPossible.getCount();
	}

	/**
	 * interface for a generic operation that acts on a route
	 * 
	 * @author Ian
	 * 
	 */
	public static interface RouteOperator
	{
		/**
		 * apply the processing to this route
		 * 
		 * @param theRoute
		 */
		public void process(Route theRoute);
	}

	public Route[][] getRoutes()
	{
		return myRoutes;
	}
	
	/** utility class to count how many routes are possible
	 * 
	 * @author Ian
	 *
	 */
	private static class CountPossible implements StraightLeg.RouteOperator
	{
		int res = 0;

		@Override
		public void process(Route theRoute)
		{
			if (theRoute.isPossible())
				res++;
		}

		public int getCount()
		{
			return res;
		}
	}

}