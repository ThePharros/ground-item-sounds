package com.grounditemsounds;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class GroundItemSoundsPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(GroundItemSoundsPlugin.class);
		RuneLite.main(args);
	}
}