package dev.meiny.meinysgui.client;

import java.util.ArrayList;
import java.util.List;
import dev.meiny.meinysgui.sort.HotbarMode;
import dev.meiny.meinysgui.sort.InventorySorter;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class ClientSorter {
	private final List<Integer> menuSlotIds = new ArrayList<>();
	private final List<ItemStack> simulated = new ArrayList<>();
	private final List<Integer> clicks = new ArrayList<>();
	private ItemStack cursor = ItemStack.EMPTY;

	private ClientSorter() {
	}

	public static void sort(Minecraft client, boolean playerInventory, HotbarMode hotbarMode) {
		Player player = client.player;
		if (player == null || client.gameMode == null || player.isSpectator()) {
			return;
		}

		AbstractContainerMenu menu = player.containerMenu;
		if (!menu.getCarried().isEmpty()) {
			return;
		}
		if (!playerInventory && !InventorySorter.isSortableContainer(menu)) {
			return;
		}

		for (List<Slot> slots : InventorySorter.sortGroups(menu, player.getInventory(), playerInventory, hotbarMode)) {
			ClientSorter sorter = new ClientSorter();
			for (Slot slot : slots) {
				sorter.menuSlotIds.add(menu.slots.indexOf(slot));
				sorter.simulated.add(slot.getItem().copy());
			}

			if (!sorter.plan()) {
				continue;
			}

			for (int slotId : sorter.clicks) {
				client.gameMode.handleContainerInput(menu.containerId, slotId, 0, ContainerInput.PICKUP, player);
			}
		}
	}

	private boolean plan() {
		int size = simulated.size();

		for (int i = 0; i < size; i++) {
			for (int j = i + 1; j < size; j++) {
				ItemStack target = simulated.get(i);
				ItemStack source = simulated.get(j);
				if (target.isEmpty() || isFull(target)) {
					break;
				}
				if (source.isEmpty() || isFull(source) || !ItemStack.isSameItemSameComponents(target, source)) {
					continue;
				}
				click(j);
				click(i);
				if (!cursor.isEmpty()) {
					click(j);
				}
			}
		}

		List<ItemStack> goal = new ArrayList<>();
		for (ItemStack stack : simulated) {
			if (!stack.isEmpty()) {
				goal.add(stack.copy());
			}
		}
		goal = InventorySorter.mergeAndSort(goal);

		for (int i = 0; i < size && i < goal.size(); i++) {
			ItemStack wanted = goal.get(i);
			if (matches(simulated.get(i), wanted)) {
				continue;
			}
			int j = findMatch(wanted, i + 1);
			if (j < 0) {
				continue;
			}
			click(j);
			click(i);
			if (!cursor.isEmpty()) {
				click(j);
			}
		}

		return cursor.isEmpty();
	}

	private void click(int k) {
		ItemStack slot = simulated.get(k);

		if (cursor.isEmpty()) {
			cursor = slot;
			simulated.set(k, ItemStack.EMPTY);
		} else if (slot.isEmpty()) {
			simulated.set(k, cursor);
			cursor = ItemStack.EMPTY;
		} else if (ItemStack.isSameItemSameComponents(slot, cursor)) {
			int amount = Math.min(cursor.getCount(), slot.getMaxStackSize() - slot.getCount());
			slot.grow(amount);
			cursor.shrink(amount);
			if (cursor.isEmpty()) {
				cursor = ItemStack.EMPTY;
			}
		} else {
			simulated.set(k, cursor);
			cursor = slot;
		}

		clicks.add(menuSlotIds.get(k));
	}

	private int findMatch(ItemStack wanted, int from) {
		for (int j = from; j < simulated.size(); j++) {
			if (matches(simulated.get(j), wanted)) {
				return j;
			}
		}
		return -1;
	}

	private static boolean matches(ItemStack a, ItemStack b) {
		if (a.isEmpty() || b.isEmpty()) {
			return a.isEmpty() && b.isEmpty();
		}
		return ItemStack.isSameItemSameComponents(a, b) && a.getCount() == b.getCount();
	}

	private static boolean isFull(ItemStack stack) {
		return stack.getCount() >= stack.getMaxStackSize();
	}
}
