package dev.meiny.meinysgui.client;

import dev.meiny.meinysgui.client.config.MeinysGuiConfig;
import net.fabricmc.api.ClientModInitializer;

public class MeinysGUIClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MeinysGuiConfig.load();
		InventoryButtons.init();
		InventoryHotkeys.init();
	}
}