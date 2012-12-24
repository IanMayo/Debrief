package com.planetmayo.debrief.satc.gwt.client.services;

import java.util.Date;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.planetmayo.debrief.satc.support.UtilsService;

public class GWTUtilsService implements UtilsService
{

	@Override
	public String formatDate(String pattern, Date date)
	{
		return DateTimeFormat.getFormat(pattern).format(date);
	}

	@Override
	public Date parseDate(String pattern, String text)
	{
		return DateTimeFormat.getFormat(pattern).parse(text);
	}

	@Override
	public <T> T higherElement(TreeSet<T> set, T currentElement)
	{
		if (set == null || set.isEmpty())
		{
			return null;
		}
		if (currentElement == null)
		{
			return set.first();
		}
		SortedSet<T> tail = set.tailSet(currentElement);
		if (tail.size() <= 1)
		{
			return null;
		}
		Iterator<T> iterator = tail.iterator();
		iterator.next();
		return iterator.next();
	}
}
