package com.grounditemsounds;

import com.google.inject.Provides;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.inject.Inject;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemComposition;
import net.runelite.api.TileItem;
import net.runelite.api.events.ItemSpawned;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.grounditems.GroundItemsConfig;
import net.runelite.client.plugins.grounditems.GroundItemsPlugin;
import net.runelite.client.util.Text;
import net.runelite.client.util.WildcardMatcher;

@Slf4j
@PluginDescriptor(
	name = "Ground Item Sounds"
)
@PluginDependency(GroundItemsPlugin.class)
public class GroundItemSoundsPlugin extends Plugin
{
	@Inject
	private GroundItemSoundsConfig config;

	@Provides
	GroundItemSoundsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(GroundItemSoundsConfig.class);
	}

	@Inject
	private GroundItemsConfig groundItemsConfig;

	@Inject
	private ItemManager itemManager;

	private static final File GROUND_ITEM_SOUNDS_DIR = new File(RuneLite.RUNELITE_DIR.getPath() + File.separator + "ground-item-sounds");
	private static final File HIGHLIGHTED_SOUND_FILE = new File(GROUND_ITEM_SOUNDS_DIR, "highlighted_sound.wav");
	private static final File LOW_SOUND_FILE = new File(GROUND_ITEM_SOUNDS_DIR, "low_sound.wav");
	private static final File MEDIUM_SOUND_FILE = new File(GROUND_ITEM_SOUNDS_DIR, "medium_sound.wav");
	private static final File HIGH_SOUND_FILE = new File(GROUND_ITEM_SOUNDS_DIR, "high_sound.wav");
	private static final File INSANE_SOUND_FILE = new File(GROUND_ITEM_SOUNDS_DIR, "insane_sound.wav");
	private static final File[] SOUND_FILES = new File[]{
		HIGHLIGHTED_SOUND_FILE,
		LOW_SOUND_FILE,
		MEDIUM_SOUND_FILE,
		HIGH_SOUND_FILE,
		INSANE_SOUND_FILE
	};
	private List<String> highlightedItemsList = new CopyOnWriteArrayList<>();
	private Clip clip = null;

	@Override
	protected void startUp()
	{
		initSoundFiles();
		updateHighlightedItemsList();
	}

	@Override
	protected void shutDown()
	{
		clip.close();
		clip = null;
		highlightedItemsList = null;
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged)
	{
		if (configChanged.getGroup().equals("grounditems") && configChanged.getKey().equals("highlightedItems"))
		{
			updateHighlightedItemsList();
		}
	}

	@Subscribe
	public void onItemSpawned(ItemSpawned itemSpawned)
	{
		final TileItem item = itemSpawned.getItem();
		final int id = item.getId();
		final ItemComposition itemComposition = itemManager.getItemComposition(id);
		final String name = itemComposition.getName().toLowerCase();

		if (config.highlightSound() && highlightedItemsList.stream().anyMatch(a -> WildcardMatcher.matches(a, name)))
		{
			playSound(HIGHLIGHTED_SOUND_FILE, config.highlightVolume());
		}

		final int quantity = item.getQuantity();
		final int gePrice = itemManager.getItemPrice(id) * quantity;
		final int haPrice = itemComposition.getHaPrice() * quantity;
		final int value = getValueByMode(gePrice, haPrice);

		if (config.lowValueSound() && value >= groundItemsConfig.lowValuePrice() && value < groundItemsConfig.mediumValuePrice())
		{
			playSound(LOW_SOUND_FILE, config.lowValueVolume());
		}
		if (config.mediumValueSound() && value >= groundItemsConfig.mediumValuePrice() && value < groundItemsConfig.highValuePrice())
		{
			playSound(MEDIUM_SOUND_FILE, config.mediumValueVolume());
		}
		if (config.highValueSound() && value >= groundItemsConfig.highValuePrice() && value < groundItemsConfig.insaneValuePrice())
		{
			playSound(HIGH_SOUND_FILE, config.highValueVolume());
		}
		if (config.insaneValueSound() && value >= groundItemsConfig.insaneValuePrice())
		{
			playSound(INSANE_SOUND_FILE, config.insaneValueVolume());
		}
	}

	private void playSound(File f, int volume)
	{
		try
		{
			/* Leaving this removed for now. Calling this too many times causes client to hang.
			if (clip != null)
			{
				clip.close();
			}
			 */

			AudioInputStream is = AudioSystem.getAudioInputStream(f);
			AudioFormat format = is.getFormat();
			DataLine.Info info = new DataLine.Info(Clip.class, format);
			clip = (Clip) AudioSystem.getLine(info);
			clip.open(is);
			setVolume(volume);
			clip.start();
		}
		catch (LineUnavailableException | UnsupportedAudioFileException | IOException e)
		{
			log.warn("Sound file error", e);
		}
	}

	// sets volume using dB to linear conversion
	private void setVolume(int volume)
	{
		float vol = volume/100.0f;
		vol *= config.masterVolume()/100.0f;
		FloatControl gainControl = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
		gainControl.setValue(20.0f * (float) Math.log10(vol));
	}

	// initialize sound files if they haven't been created yet
	private void initSoundFiles()
	{
		if (!GROUND_ITEM_SOUNDS_DIR.exists())
		{
			GROUND_ITEM_SOUNDS_DIR.mkdirs();
		}

		for (File f : SOUND_FILES)
		{
			try
			{
				if (f.exists()) {
					continue;
				}
				InputStream stream = GroundItemSoundsPlugin.class.getClassLoader().getResourceAsStream(f.getName());
				OutputStream out = new FileOutputStream(f);
				byte[] buffer = new byte[8 * 1024];
				int bytesRead;
				while ((bytesRead = stream.read(buffer)) != -1) {
					out.write(buffer, 0, bytesRead);
				}
				out.close();
				stream.close();
			}  catch (Exception e) {
				log.debug("GroundItemSoundsPlugin - " + e + ": " + f);
			}
		}
	}

	private int getValueByMode(int gePrice, int haPrice)
	{
		switch (groundItemsConfig.valueCalculationMode())
		{
			case GE:
				return gePrice;
			case HA:
				return haPrice;
			default: // Highest
				return Math.max(gePrice, haPrice);
		}
	}

	private void updateHighlightedItemsList()
	{
		if (!groundItemsConfig.getHighlightItems().isEmpty())
		{
			highlightedItemsList = Text.fromCSV(groundItemsConfig.getHighlightItems().toLowerCase());
		}
	}
}
