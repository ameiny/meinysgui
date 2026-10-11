package dev.meiny.meinysgui.sort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import dev.meiny.meinysgui.network.SortPayload;
import dev.meiny.meinysgui.network.SortPayloadV2;
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
	public static final int HOTBAR_START = 0;
	public static final int MAIN_INVENTORY_START = 9;
	public static final int MAIN_INVENTORY_END = 36;

	private InventorySorter() {
	}

	public static void init() {
		PayloadTypeRegistry.serverboundPlay().register(SortPayload.TYPE, SortPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(SortPayloadV2.TYPE, SortPayloadV2.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(SortPayload.TYPE, (payload, context) -> sort(context.player(), payload.playerInventory(), HotbarMode.IGNORE));
		ServerPlayNetworking.registerGlobalReceiver(SortPayloadV2.TYPE, (payload, context) -> sort(context.player(), payload.playerInventory(), payload.hotbarMode()));
	}

	public static boolean isSortableContainer(AbstractContainerMenu menu) {
		return menu instanceof ChestMenu
				|| menu instanceof ShulkerBoxMenu
				|| menu instanceof HopperMenu
				|| menu instanceof DispenserMenu;
	}

	private static void sort(ServerPlayer player, boolean playerInventory, HotbarMode hotbarMode) {
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

		Set<Container> changedContainers = new HashSet<>();
		for (List<Slot> slots : sortGroups(menu, player.getInventory(), playerInventory, hotbarMode)) {
			sortSlots(slots, changedContainers);
		}

		if (changedContainers.isEmpty()) {
			return;
		}

		changedContainers.forEach(Container::setChanged);
		menu.broadcastChanges();
	}

	private static void sortSlots(List<Slot> slots, Set<Container> changedContainers) {
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

		for (int i = 0; i < slots.size(); i++) {
			Slot slot = slots.get(i);
			ItemStack stack = i < sorted.size() ? sorted.get(i) : ItemStack.EMPTY;
			slot.container.setItem(slot.getContainerSlot(), stack);
			changedContainers.add(slot.container);
		}
	}

	public static List<List<Slot>> sortGroups(AbstractContainerMenu menu, Inventory inventory, boolean playerInventory, HotbarMode hotbarMode) {
		if (!playerInventory) {
			return List.of(collectContainerSlots(menu, inventory));
		}

		List<Slot> main = collectInventorySlots(menu, inventory, MAIN_INVENTORY_START, MAIN_INVENTORY_END);
		List<Slot> hotbar = collectInventorySlots(menu, inventory, HOTBAR_START, MAIN_INVENTORY_START);

		return switch (hotbarMode) {
			case IGNORE -> List.of(main);
			case INCLUDE -> {
				List<Slot> all = new ArrayList<>(main);
				all.addAll(hotbar);
				yield List.of(all);
			}
			case SEPARATE -> List.of(main, hotbar);
		};
	}

	public static List<Slot> collectContainerSlots(AbstractContainerMenu menu, Inventory inventory) {
		List<Slot> result = new ArrayList<>();
		for (Slot slot : menu.slots) {
			if (slot.container != inventory) {
				result.add(slot);
			}
		}
		return result;
	}

	public static List<Slot> collectInventorySlots(AbstractContainerMenu menu, Inventory inventory, int start, int end) {
		List<Slot> result = new ArrayList<>();
		for (Slot slot : menu.slots) {
			int index = slot.getContainerSlot();
			if (slot.container == inventory && index >= start && index < end) {
				result.add(slot);
			}
		}
		result.sort(Comparator.comparingInt(Slot::getContainerSlot));
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
