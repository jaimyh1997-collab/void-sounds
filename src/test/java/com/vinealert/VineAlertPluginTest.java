package com.vinealert;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class VineAlertPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(VineAlertPlugin.class);
		RuneLite.main(args);
	}
}
