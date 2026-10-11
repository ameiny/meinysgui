package dev.meiny.meinysgui.client;

import dev.meiny.meinysgui.client.config.MeinysGuiConfig;
import dev.meiny.meinysgui.network.SortPayload;
import dev.meiny.meinysgui.network.SortPayloadV2;
import dev.meiny.meinysgui.sort.HotbarMode;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;

public final class InventoryActions {
	private InventoryActions() {
	}

	public static void sort(Minecraft client, boolean playerInventory) {
		HotbarMode hotbarMode = playerInventory ? MeinysGuiConfig.get().sortHotbar : HotbarMode.IGNORE;

		if (ClientPlayNetworking.canSend(SortPayloadV2.TYPE)) {
			ClientPlayNetworking.send(new SortPayloadV2(playerInventory, hotbarMode));
		} else if (hotbarMode == HotbarMode.IGNORE && ClientPlayNetworking.canSend(SortPayload.TYPE)) {
			ClientPlayNetworking.send(new SortPayload(playerInventory));
		} else if (playerInventory && client.gui.screen() instanceof CreativeModeInventoryScreen) {
			CreativeSorter.sort(client, hotbarMode);
		} else {
			ClientSorter.sort(client, playerInventory, hotbarMode);
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
