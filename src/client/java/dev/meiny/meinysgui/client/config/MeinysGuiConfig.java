package dev.meiny.meinysgui.client.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import dev.meiny.meinysgui.MeinysGUI;
import dev.meiny.meinysgui.sort.HotbarMode;
import dev.meiny.meinysgui.sort.SortMode;
import net.fabricmc.loader.api.FabricLoader;

public final class MeinysGuiConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve(MeinysGUI.MOD_ID + ".json");

	private static MeinysGuiConfig instance = new MeinysGuiConfig();

	public HotbarMode sortHotbar = HotbarMode.IGNORE;
	public boolean depositAllHotbar = false;
	public boolean depositMatchingHotbar = false;
	public ButtonMode buttons = ButtonMode.ALL;
	public SortMode sortMode = SortMode.DEFAULT;

	public static MeinysGuiConfig get() {
		return instance;
	}

	public static void load() {
		if (!Files.exists(PATH)) {
			save();
			return;
		}

		try (Reader reader = Files.newBufferedReader(PATH)) {
			MeinysGuiConfig loaded = GSON.fromJson(reader, MeinysGuiConfig.class);
			if (loaded != null) {
				instance = loaded;
			}
		} catch (IOException | JsonParseException e) {
			MeinysGUI.LOGGER.warn("Could not read config file {}, using defaults", PATH, e);
		}

		if (instance.sortHotbar == null) {
			instance.sortHotbar = HotbarMode.IGNORE;
		}
		if (instance.buttons == null) {
			instance.buttons = ButtonMode.ALL;
		}
		if (instance.sortMode == null) {
			instance.sortMode = SortMode.DEFAULT;
		}
	}

	public static void save() {
		try {
			Files.createDirectories(PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(PATH)) {
				GSON.toJson(instance, writer);
			}
		} catch (IOException e) {
			MeinysGUI.LOGGER.warn("Could not save config file {}", PATH, e);
		}
	}
}
