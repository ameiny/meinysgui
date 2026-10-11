package dev.meiny.meinysgui.client;

import net.fabricmc.api.ClientModInitializer;

public class MeinysGUIClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		SortButtons.init();
	}
}