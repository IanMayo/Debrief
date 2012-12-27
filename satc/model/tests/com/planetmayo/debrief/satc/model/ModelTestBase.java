package com.planetmayo.debrief.satc.model;

import org.junit.BeforeClass;

import com.planetmayo.debrief.satc.model.support.TestIOService;
import com.planetmayo.debrief.satc.model.support.TestLogService;
import com.planetmayo.debrief.satc.model.support.TestUtilsService;
import com.planetmayo.debrief.satc.support.SupportServices;

public class ModelTestBase
{
	@BeforeClass
	public static void initializeServices() {
		SupportServices.INSTANCE.initialize(
				new TestLogService(), 
				new TestUtilsService(), 
				new TestIOService()
		);
	}
}
