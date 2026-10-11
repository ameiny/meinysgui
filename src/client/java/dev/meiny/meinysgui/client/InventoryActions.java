package dev.meiny.meinysgui.client;

import dev.meiny.meinysgui.client.config.MeinysGuiConfig;
import dev.meiny.meinysgui.network.SortPayload;
import dev.meiny.meinysgui.sort.HotbarMode;
import dev.meiny.meinysgui.sort.SortMode;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;

public final class InventoryActions {
	private InventoryActions() {
	}

	public static void sort(Minecraft client, boolean playerInventory) {
		HotbarMode hotbarMode = playerInventory ? MeinysGuiConfig.get().sortHotbar : HotbarMode.IGNORE;
		SortMode sortMode = MeinysGuiConfig.get().sortMode;

		if (ClientPlayNetworking.canSend(SortPayload.TYPE)) {
			ClientPlayNetworking.send(new SortPayload(playerInventory, hotbarMode, sortMode));
		} else if (playerInventory && client.gui.screen() instanceof CreativeModeInventoryScreen) {
			CreativeSorter.sort(client, hotbarMode, sortMode);
		} else {
			ClientSorter.sort(client, playerInventory, hotbarMode, sortMode);
		}
	}

	public static void depositAll(Minecraft client) {
		ItemTransfer.depositAll(client, MeinysGuiConfig.get().depositAllHotbar);
	}

	public static void depositMatching(Minecraft client) {
		ItemTransfer.depositMatching(client, MeinysGuiConfig.get().depositMatchingHotbar);
	}

	public static void takeAll(Minecraft client) {
		ItemTransfer.takeAll(client);
	}

	public static void takeMatching(Minecraft client) {
		ItemTransfer.takeMatching(client);
	}
}
