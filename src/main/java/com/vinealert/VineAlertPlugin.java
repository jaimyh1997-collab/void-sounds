package com.vinealert;

import com.google.inject.Provides;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import javax.inject.Inject;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.SoundEffectID;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.OverheadTextChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PluginDescriptor(
	name = "Void Vine Alert",
	description = "Plays a sound when a player gets rooted by Ocellus' Void Vine in The Fractured Archive",
	tags = {"fractured", "archive", "ocellus", "raid", "raids 4", "vine", "sound", "alert"}
)
public class VineAlertPlugin extends Plugin
{
	private static final Logger log = LoggerFactory.getLogger(VineAlertPlugin.class);

	// Void Vine NPC id (phase 3 of Ocellus)
	static final int VOID_VINE_ID = 16681;

	// Built-in sound: put your file here as vine.wav
	private static final String BUILTIN_SOUND = "/com/vinealert/vine.wav";

	// Don't play twice for the same vine (shout + spawn are ~3 ticks apart)
	private static final int DEDUPE_TICKS = 5;

	@Inject
	private Client client;

	@Inject
	private VineAlertConfig config;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ClientThread clientThread;

	private Clip clip;
	private int lastAlertTick = -100;

	@Provides
	VineAlertConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(VineAlertConfig.class);
	}

	@Override
	protected void startUp()
	{
		loadClip();
	}

	@Override
	protected void shutDown()
	{
		closeClip();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!VineAlertConfig.GROUP.equals(event.getGroup()))
		{
			return;
		}

		// Test toggle: play once, then switch itself back off
		if ("testSound".equals(event.getKey()))
		{
			if ("true".equals(event.getNewValue()))
			{
				playSound();
				configManager.setConfiguration(VineAlertConfig.GROUP, "testSound", false);
			}
			return;
		}

		loadClip();
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		NPC npc = event.getNpc();
		// Match on id, with the name as a backup in case the id is different
		if (npc.getId() != VOID_VINE_ID && !"Void Vine".equalsIgnoreCase(npc.getName()))
		{
			return;
		}

		if (config.onlyMe())
		{
			Player me = client.getLocalPlayer();
			// The vine spawns on the tile of the rooted player
			if (me == null || !npc.getWorldLocation().equals(me.getWorldLocation()))
			{
				return;
			}
		}

		alert();
	}

	@Subscribe
	public void onOverheadTextChanged(OverheadTextChanged event)
	{
		// The shout doesn't say who gets rooted, so skip it in "only me" mode
		if (!config.alertOnShout() || config.onlyMe())
		{
			return;
		}

		Actor actor = event.getActor();
		if (!(actor instanceof NPC))
		{
			return;
		}

		String name = actor.getName();
		String text = event.getOverheadText();
		if (name == null || text == null || !name.contains("Ocellus"))
		{
			return;
		}

		text = Text.removeTags(text).toLowerCase();
		if (text.contains("will take you") || text.contains("your panic"))
		{
			alert();
		}
	}

	@Subscribe
	public void onCommandExecuted(CommandExecuted event)
	{
		// Type ::vinetest in the chatbox to hear the alert
		if ("vinetest".equalsIgnoreCase(event.getCommand()))
		{
			playSound();
		}
	}

	private void alert()
	{
		int tick = client.getTickCount();
		if (tick - lastAlertTick < DEDUPE_TICKS)
		{
			return;
		}
		lastAlertTick = tick;
		playSound();
	}

	private void playSound()
	{
		if (clip != null)
		{
			clip.stop();
			clip.setFramePosition(0);
			clip.start();
		}
		else
		{
			// Fallback until a sound file is added
			clientThread.invoke(() -> client.playSoundEffect(SoundEffectID.GE_ADD_OFFER_DINGALING));
		}
	}

	private void loadClip()
	{
		closeClip();

		try
		{
			AudioInputStream stream;
			String path = config.customSoundPath().trim();

			if (!path.isEmpty())
			{
				stream = AudioSystem.getAudioInputStream(new File(path));
			}
			else
			{
				InputStream in = getClass().getResourceAsStream(BUILTIN_SOUND);
				if (in == null)
				{
					log.debug("No built-in vine.wav found, using fallback game sound");
					return;
				}
				stream = AudioSystem.getAudioInputStream(new BufferedInputStream(in));
			}

			clip = AudioSystem.getClip();
			clip.open(stream);
			applyVolume();
		}
		catch (Exception e)
		{
			log.warn("Couldn't load Void Vine alert sound", e);
			closeClip();
		}
	}

	private void applyVolume()
	{
		if (clip == null || !clip.isControlSupported(FloatControl.Type.MASTER_GAIN))
		{
			return;
		}

		FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
		int volume = config.volume();
		float db = volume <= 0
			? gain.getMinimum()
			: (float) (20.0 * Math.log10(volume / 100.0));
		gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), db)));
	}

	private void closeClip()
	{
		if (clip != null)
		{
			clip.stop();
			clip.close();
			clip = null;
		}
	}
}
