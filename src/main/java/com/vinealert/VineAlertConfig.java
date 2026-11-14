package com.vinealert;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup(VineAlertConfig.GROUP)
public interface VineAlertConfig extends Config
{
	String GROUP = "voidvinealert";

	@ConfigItem(
		keyName = "onlyMe",
		name = "Only when I'm rooted",
		description = "Only play the sound when the vine roots you, not a teammate",
		position = 0
	)
	default boolean onlyMe()
	{
		return false;
	}

	@ConfigItem(
		keyName = "alertOnShout",
		name = "Early warning on shout",
		description = "Also play when Ocellus shouts the vine line (about 3 ticks before the root). Ignored when 'Only when I'm rooted' is on.",
		position = 1
	)
	default boolean alertOnShout()
	{
		return false;
	}

	@Range(max = 100)
	@ConfigItem(
		keyName = "volume",
		name = "Volume",
		description = "Volume of the alert sound (0-100)",
		position = 2
	)
	default int volume()
	{
		return 80;
	}

	@ConfigItem(
		keyName = "customSoundPath",
		name = "Custom sound file",
		description = "Full path to a .wav file. Leave empty to use the built-in sound.",
		position = 3
	)
	default String customSoundPath()
	{
		return "";
	}

	@ConfigItem(
		keyName = "testSound",
		name = "Play test sound",
		description = "Tick this to hear the alert once (it switches itself back off). You can also type ::vinetest in chat.",
		position = 4
	)
	default boolean testSound()
	{
		return false;
	}
}
