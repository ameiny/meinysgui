package dev.meiny.meinysgui.client;

import java.util.ArrayList;
import java.util.List;

import dev.meiny.meinysgui.client.mixin.AbstractContainerScreenAccessor;
import dev.meiny.meinysgui.network.SortPayload;
import dev.meiny.meinysgui.sort.InventorySorter;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

public final class SortButtons {
	private static final int SIZE = 10;

	private SortButtons() {
	}

	public static void init() {
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (screen instanceof AbstractContainerScreen<?> containerScreen) {
				addButtons(client, containerScreen);
			}
		});
	}

	private static void addButtons(Minecraft client, AbstractContainerScreen<?> screen) {
		if (client.player == null) {
			return;
		}

		SortButton containerButton = null;
		if (InventorySorter.isSortableContainer(screen.getMenu())) {
			containerButton = createButton(false, "meinysgui.sort.container");
			Screens.getWidgets(screen).add(containerButton);
		}

		boolean creative = screen instanceof CreativeModeInventoryScreen;
		SortButton inventoryButton = null;
		if (creative || findFirstInventorySlot(screen, client.player) != null) {
			inventoryButton = createButton(true, "meinysgui.sort.inventory");
			Screens.getWidgets(screen).add(inventoryButton);
		}

		SortButton finalContainerButton = containerButton;
		SortButton finalInventoryButton = inventoryButton;
		List<SortButton> buttons = new ArrayList<>();
		if (containerButton != null) buttons.add(containerButton);
		if (inventoryButton != null) buttons.add(inventoryButton);

		updatePositions(client, screen, finalContainerButton, finalInventoryButton);

		ScreenEvents.beforeExtract(screen).register((s, graphics, mouseX, mouseY, tickProgress) ->
				updatePositions(client, screen, finalContainerButton, finalInventoryButton));

		ScreenMouseEvents.afterMouseRelease(screen).register((s, event, consumed) -> {
			for (SortButton button : buttons) {
				button.setPressed(false);
			}
			return consumed;
		});

		ScreenMouseEvents.afterMouseClick(screen).register((s, event, consumed) -> {
			GuiEventListener focused = screen.getFocused();
			if (focused != null && buttons.contains(focused)) {
				screen.setFocused(null);
			}
			return consumed;
		});
	}

	private static SortButton createButton(boolean playerInventory, String translationKey) {
		return new SortButton(SIZE, Component.translatable(translationKey), button -> {
			if (serverHasMod()) {
				ClientPlayNetworking.send(new SortPayload(playerInventory));
			} else if (playerInventory && Minecraft.getInstance().gui.screen() instanceof CreativeModeInventoryScreen) {
				CreativeSorter.sort(Minecraft.getInstance());
			} else {
				ClientSorter.sort(Minecraft.getInstance(), playerInventory);
			}
		});
	}

	private static boolean serverHasMod() {
		return false; //ClientPlayNetworking.canSend(SortPayload.TYPE);
	}

	private static void updatePositions(Minecraft client, AbstractContainerScreen<?> screen, SortButton containerButton, SortButton inventoryButton) {
		AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) screen;
		int left = accessor.meinysgui$getLeftPos();
		int top = accessor.meinysgui$getTopPos();

		if (containerButton != null) {
			containerButton.setPosition(left + accessor.meinysgui$getImageWidth() - 7 - SIZE, top + 10 - SIZE / 2);
		}

		if (inventoryButton != null && client.player != null) {
			Slot first = findFirstInventorySlot(screen, client.player);

			boolean visible = first != null;
			if (screen instanceof CreativeModeInventoryScreen creativeScreen) {
				visible = visible && creativeScreen.isInventoryOpen(); // nur im Tab "Überlebensinventar"
			}
			inventoryButton.visible = visible;

			if (first != null) {
				int rightEdge = first.x + 17;
				for (Slot slot : screen.getMenu().slots) {
					if (slot.y == first.y) {
						rightEdge = Math.max(rightEdge, slot.x + 17);
					}
				}
				inventoryButton.setPosition(left + rightEdge - SIZE, top + first.y - 7 - SIZE / 2);
			}
		}
	}

	private static Slot findFirstInventorySlot(AbstractContainerScreen<?> screen, Player player) {
		Inventory inventory = player.getInventory();
		boolean creative = screen instanceof CreativeModeInventoryScreen;
		Slot first = null;

		for (Slot slot : screen.getMenu().slots) {
			if (slot.container != inventory) {
				continue;
			}

			int index = slot.getContainerSlot();
			if (creative) {
				if (index < 0 || index >= player.inventoryMenu.slots.size()) {
					continue;
				}
				Slot backing = player.inventoryMenu.getSlot(index);
				if (backing.container != inventory) {
					continue;
				}
				index = backing.getContainerSlot();
			}

			if (index >= 9 && index < 36 && (first == null || slot.y < first.y)) {
				first = slot;
			}
		}
		return first;
	}
}
