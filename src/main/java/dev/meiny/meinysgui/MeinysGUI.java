package dev.meiny.meinysgui;

import dev.meiny.meinysgui.sort.InventorySorter;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MeinysGUI implements ModInitializer {
	public static final String MOD_ID = "meinysgui";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		InventorySorter.init();
		LOGGER.info("MeinysGUI initialized!");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
