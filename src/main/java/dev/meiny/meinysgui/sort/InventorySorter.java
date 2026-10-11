package dev.meiny.meinysgui.sort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import dev.meiny.meinysgui.network.SortPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class InventorySorter {
	private static final int MAIN_INVENTORY_START = 9;
	private static final int MAIN_INVENTORY_END = 36;

	private InventorySorter() {
	}

	public static void init() {
		PayloadTypeRegistry.serverboundPlay().register(SortPayload.TYPE, SortPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(SortPayload.TYPE, (payload, context) -> sort(context.player(), payload.playerInventory()));
	}

	public static boolean isSortableContainer(AbstractContainerMenu menu) {
		return menu instanceof ChestMenu
				|| menu instanceof ShulkerBoxMenu
				|| menu instanceof HopperMenu
				|| menu instanceof DispenserMenu;
	}

	private static void sort(ServerPlayer player, boolean playerInventory) {
		if (player.isSpectator()) {
			return;
		}

		AbstractContainerMenu menu = player.containerMenu;
		if (!menu.stillValid(player)) {
			return;
		}
		if (!playerInventory && !isSortableContainer(menu)) {
			return;
		}

		List<Slot> slots = collectSlots(menu, player.getInventory(), playerInventory);
		if (slots.isEmpty()) {
			return;
		}

		List<ItemStack> stacks = new ArrayList<>();
		for (Slot slot : slots) {
			ItemStack stack = slot.getItem();
			if (!stack.isEmpty()) {
				stacks.add(stack.copy());
			}
		}

		List<ItemStack> sorted = mergeAndSort(stacks);
		if (sorted.size() > slots.size()) {
			return;
		}

		Set<Container> changedContainers = new HashSet<>();
		for (int i = 0; i < slots.size(); i++) {
			Slot slot = slots.get(i);
			ItemStack stack = i < sorted.size() ? sorted.get(i) : ItemStack.EMPTY;
			slot.container.setItem(slot.getContainerSlot(), stack);
			changedContainers.add(slot.container);
		}

		changedContainers.forEach(Container::setChanged);
		menu.broadcastChanges();
	}

	public static List<Slot> collectSlots(AbstractContainerMenu menu, Inventory inventory, boolean playerInventory) {
		List<Slot> result = new ArrayList<>();
		for (Slot slot : menu.slots) {
			boolean isPlayerSlot = slot.container == inventory;
			if (playerInventory) {
				int index = slot.getContainerSlot();
				if (isPlayerSlot && index >= MAIN_INVENTORY_START && index < MAIN_INVENTORY_END) {
					result.add(slot);
				}
			} else if (!isPlayerSlot) {
				result.add(slot);
			}
		}
		return result;
	}

	public static List<ItemStack> mergeAndSort(List<ItemStack> stacks) {
		List<ItemStack> merged = new ArrayList<>();

		for (ItemStack stack : stacks) {
			for (ItemStack target : merged) {
				if (stack.isEmpty()) {
					break;
				}
				if (ItemStack.isSameItemSameComponents(target, stack) && target.getCount() < target.getMaxStackSize()) {
					int amount = Math.min(stack.getCount(), target.getMaxStackSize() - target.getCount());
					target.grow(amount);
					stack.shrink(amount);
				}
			}
			if (!stack.isEmpty()) {
				merged.add(stack);
			}
		}

		merged.sort(Comparator
				.comparingInt((ItemStack s) -> BuiltInRegistries.ITEM.getId(s.getItem()))
				.thenComparing((ItemStack s) -> s.getHoverName().getString())
				.thenComparing(Comparator.comparingInt(ItemStack::getCount).reversed()));

		return merged;
	}
}
