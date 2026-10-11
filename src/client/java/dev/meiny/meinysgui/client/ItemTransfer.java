package dev.meiny.meinysgui.client;

import java.util.ArrayList;
import java.util.List;
import dev.meiny.meinysgui.sort.InventorySorter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class ItemTransfer {
	private static final int INVENTORY_END = 36;

	private ItemTransfer() {
	}

	public static void depositAll(Minecraft client) {
		transfer(client, true, false);
	}

	public static void depositMatching(Minecraft client) {
		transfer(client, true, true);
	}

	public static void takeAll(Minecraft client) {
		transfer(client, false, false);
	}

	public static void takeMatching(Minecraft client) {
		transfer(client, false, true);
	}

	private static void transfer(Minecraft client, boolean toContainer, boolean matchingOnly) {
		LocalPlayer player = client.player;
		if (player == null || client.gameMode == null || player.isSpectator()) {
			return;
		}

		AbstractContainerMenu menu = player.containerMenu;
		if (!menu.getCarried().isEmpty() || !InventorySorter.isSortableContainer(menu)) {
			return;
		}

		Inventory inventory = player.getInventory();
		List<Slot> playerSlots = InventorySorter.collectSlots(menu, inventory, true);
		List<Slot> containerSlots = InventorySorter.collectSlots(menu, inventory, false);

		List<Slot> sources = toContainer ? playerSlots : containerSlots;
		List<ItemStack> targets = toContainer ? copyStacks(containerSlots) : copyInventory(inventory);

		for (Slot slot : sources) {
			ItemStack stack = slot.getItem();
			if (stack.isEmpty()) {
				continue;
			}
			if (matchingOnly && !containsMatch(targets, stack)) {
				continue;
			}
			client.gameMode.handleContainerInput(menu.containerId, menu.slots.indexOf(slot), 0, ContainerInput.QUICK_MOVE, player);
		}
	}

	private static List<ItemStack> copyStacks(List<Slot> slots) {
		List<ItemStack> stacks = new ArrayList<>();
		for (Slot slot : slots) {
			if (!slot.getItem().isEmpty()) {
				stacks.add(slot.getItem().copy());
			}
		}
		return stacks;
	}

	private static List<ItemStack> copyInventory(Inventory inventory) {
		List<ItemStack> stacks = new ArrayList<>();
		for (int i = 0; i < INVENTORY_END; i++) {
			ItemStack stack = inventory.getItem(i);
			if (!stack.isEmpty()) {
				stacks.add(stack.copy());
			}
		}
		return stacks;
	}

	private static boolean containsMatch(List<ItemStack> stacks, ItemStack stack) {
		for (ItemStack candidate : stacks) {
			if (ItemStack.isSameItemSameComponents(candidate, stack)) {
				return true;
			}
		}
		return false;
	}
}
