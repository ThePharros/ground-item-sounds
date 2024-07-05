package com.grounditemsounds;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("grounditemsounds")
public interface GroundItemSoundsConfig extends Config
{
	@Range(max=100)
	@ConfigItem(
		keyName = "masterVolume",
		name = "Master Volume",
		description = "Sets the master volume of all ground item sounds",
		position = 0
	)
	default int masterVolume()
	{
		return 50;
	}

	@ConfigItem(
		keyName = "highlightSound",
		name = "Highlight Sound",
		description = "Configure whether or not to play a sound when a highlighted item appears",
		position = 1
	)
	default boolean highlightSound()
	{
		return true;
	}

	@Range(max=100)
	@ConfigItem(
		keyName = "highlightVolume",
		name = "Highlight Volume",
		description = "Sets the sound volume for highlighted ground items",
		position = 2
	)
	default int highlightVolume()
	{
		return 100;
	}

	@ConfigItem(
		keyName = "lowValueSound",
		name = "Low Value Sound",
		description = "Configure whether or not to play a sound when a low-valued item appears",
		position = 3
	)
	default boolean lowValueSound()
	{
		return false;
	}

	@Range(max=100)
	@ConfigItem(
		keyName = "lowValueVolume",
		name = "Low Value Volume",
		description = "Sets the sound volume for low-valued ground items",
		position = 4
	)
	default int lowValueVolume()
	{
		return 100;
	}

	@ConfigItem(
		keyName = "mediumValueSound",
		name = "Medium Value Sound",
		description = "Configure whether or not to play a sound when a medium-valued item appears",
		position = 5
	)
	default boolean mediumValueSound()
	{
		return false;
	}

	@Range(max=100)
	@ConfigItem(
		keyName = "mediumValueVolume",
		name = "Medium Value Volume",
		description = "Sets the sound volume for medium-valued ground items",
		position = 6
	)
	default int mediumValueVolume()
	{
		return 100;
	}

	@ConfigItem(
		keyName = "highValueSound",
		name = "High Value Sound",
		description = "Configure whether or not to play a sound when a high-valued item appears",
		position = 7
	)
	default boolean highValueSound()
	{
		return true;
	}

	@Range(max=100)
	@ConfigItem(
		keyName = "highValueVolume",
		name = "High Value Volume",
		description = "Sets the sound volume for high-valued ground items",
		position = 8
	)
	default int highValueVolume()
	{
		return 100;
	}

	@ConfigItem(
		keyName = "insaneValueSound",
		name = "Insane Value Sound",
		description = "Configure whether or not to play a sound when an insane-valued item appears",
		position = 9
	)
	default boolean insaneValueSound()
	{
		return true;
	}

	@Range(max=100)
	@ConfigItem(
		keyName = "insaneValueVolume",
		name = "Insane Value Volume",
		description = "Sets the sound volume for insane-valued ground items",
		position = 10
	)
	default int insaneValueVolume()
	{
		return 100;
	}
}
