package com.grounditemsounds;

import com.google.inject.Provides;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.TileItem;
import static net.runelite.api.TileItem.OWNERSHIP_GROUP;
import static net.runelite.api.TileItem.OWNERSHIP_OTHER;
import static net.runelite.api.TileItem.OWNERSHIP_SELF;
import net.runelite.api.events.ItemSpawned;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.audio.AudioPlayer;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.grounditems.GroundItemsConfig;
import net.runelite.client.plugins.grounditems.config.OwnershipFilterMode;
import net.runelite.client.util.Filepath;
import net.runelite.client.util.Text;
import net.runelite.client.util.WildcardMatcher;

@Slf4j
@PluginDescriptor(
	name = "Ground Item Sounds",
	internalName = "ground-item-sounds",
	legacyDataDirectory = "ground-item-sounds"
)
public class GroundItemSoundsPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private GroundItemSoundsConfig config;

	@Provides
	GroundItemSoundsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(GroundItemSoundsConfig.class);
	}

	@Provides
	GroundItemsConfig provideGroundItemsConfig(ConfigManager configManager)
	{
		return configManager.getConfig(GroundItemsConfig.class);
	}

	@Inject
	private GroundItemsConfig groundItemsConfig;

	@Inject
	private ItemManager itemManager;

	private final AudioPlayer audioPlayer = new AudioPlayer();
	private static final String HIGHLIGHTED_SOUND_FILE = "highlighted_sound.wav";
	private static final String LOW_SOUND_FILE = "low_sound.wav";
	private static final String MEDIUM_SOUND_FILE = "medium_sound.wav";
	private static final String HIGH_SOUND_FILE = "high_sound.wav";
	private static final String INSANE_SOUND_FILE = "insane_sound.wav";
	private static final String[] SOUND_FILES = new String[]{
		HIGHLIGHTED_SOUND_FILE,
		LOW_SOUND_FILE,
		MEDIUM_SOUND_FILE,
		HIGH_SOUND_FILE,
		INSANE_SOUND_FILE
	};
	private Filepath soundsDirectory;
	private List<String> highlightedItemsList = Collections.emptyList();

	@Override
	protected void startUp() throws IOException
	{
		soundsDirectory = getPluginDirectory();
		initSoundFiles();
		updateHighlightedItemsList();
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

		if (config.useOwnershipFilter() && !shouldPlaySound(groundItemsConfig.ownershipFilterMode(), item.getOwnership(), client.getVarbitValue(VarbitID.IRONMAN)))
		{
			return;
		}

		if (config.highlightSound() && highlightedItemsList.stream().anyMatch(a -> WildcardMatcher.matches(a, name)))
		{
			playSound(HIGHLIGHTED_SOUND_FILE, config.highlightVolume());
			return;
		}

		final int quantity = item.getQuantity();
		final long gePrice = itemManager.getItemPrice(id) * quantity;
		final long haPrice = (long) itemComposition.getHaPrice() * quantity;
		final long value = getValueByMode(gePrice, haPrice);

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

	private void playSound(String fileName, int volume)
	{
		try
		{
			audioPlayer.play(soundsDirectory.joinSegment(fileName), linearTodB(volume));
		}
		catch (LineUnavailableException | UnsupportedAudioFileException | IOException e)
		{
			log.warn("Sound file error", e);
		}
	}

	private boolean shouldPlaySound(OwnershipFilterMode filterMode, int ownership, int accountType)
	{
		switch (filterMode)
		{
			case DROPS:
				return ownership == OWNERSHIP_SELF || ownership == OWNERSHIP_GROUP;
			case TAKEABLE:
				return ownership != OWNERSHIP_OTHER || accountType == 0; // Mains can always take items
			default:
				return true;
		}
	}

	// converts linear power ratio to dB (e.g. 50% -> -3.01dB)
	private float linearTodB(int volume)
	{
		float vol = volume/100.0f;
		vol *= config.masterVolume()/100.0f;
		return 20.0f * (float) Math.log10(vol);
	}

	private void initSoundFiles() throws IOException
	{
		soundsDirectory.createDirectories();

		for (String fileName : SOUND_FILES)
		{
			Filepath soundFile = soundsDirectory.joinSegment(fileName);
			if (soundFile.exists())
			{
				continue;
			}

			try (InputStream defaultSound = GroundItemSoundsPlugin.class.getResourceAsStream("/" + fileName))
			{
				soundFile.write(defaultSound.readAllBytes());
			}
			catch (IOException e)
			{
				log.warn("Unable to create default sound file {}", soundFile, e);
			}
		}
	}

	private long getValueByMode(long gePrice, long haPrice)
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
		highlightedItemsList = Text.fromCSV(groundItemsConfig.getHighlightItems().toLowerCase());
	}
}
