package dev.meiny.meinysgui.client;

import java.util.ArrayList;
import java.util.List;
import dev.meiny.meinysgui.MeinysGUI;
import dev.meiny.meinysgui.client.config.ButtonMode;
import dev.meiny.meinysgui.client.config.ConfigScreen;
import dev.meiny.meinysgui.client.config.MeinysGuiConfig;
import dev.meiny.meinysgui.client.mixin.AbstractContainerScreenAccessor;
import dev.meiny.meinysgui.sort.InventorySorter;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

public final class InventoryButtons {
	private static final int SIZE = 10;
	private static final int GAP = 2;

	private static final Identifier SORT_ICON = MeinysGUI.id("sort");
	private static final Identifier DEPOSIT_ALL_ICON = MeinysGUI.id("deposit_all");
	private static final Identifier DEPOSIT_MATCHING_ICON = MeinysGUI.id("deposit_matching");
	private static final Identifier TAKE_ALL_ICON = MeinysGUI.id("take_all");
	private static final Identifier TAKE_MATCHING_ICON = MeinysGUI.id("take_matching");
	private static final Identifier SETTINGS_ICON = MeinysGUI.id("settings");

	private InventoryButtons() {
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

		ButtonMode buttonMode = MeinysGuiConfig.get().buttons;
		boolean actions = buttonMode != ButtonMode.NONE;
		boolean storage = InventorySorter.isSortableContainer(screen.getMenu());
		boolean creative = screen instanceof CreativeModeInventoryScreen;
		boolean transfer = storage && buttonMode == ButtonMode.ALL;

		List<IconButton> containerRow = new ArrayList<>();
		if (storage && actions) {
			if (transfer) {
				containerRow.add(createButton(TAKE_ALL_ICON, "meinysgui.take_all", () -> InventoryActions.takeAll(client)));
				containerRow.add(createButton(TAKE_MATCHING_ICON, "meinysgui.take_matching", () -> InventoryActions.takeMatching(client)));
			}
			containerRow.add(createButton(SORT_ICON, "meinysgui.sort.container", () -> InventoryActions.sort(client, false)));
		}

		List<IconButton> inventoryRow = new ArrayList<>();
		if (creative || findFirstInventorySlot(screen, client.player) != null) {
			if (transfer) {
				inventoryRow.add(createButton(DEPOSIT_ALL_ICON, "meinysgui.deposit_all", () -> InventoryActions.depositAll(client)));
				inventoryRow.add(createButton(DEPOSIT_MATCHING_ICON, "meinysgui.deposit_matching", () -> InventoryActions.depositMatching(client)));
			}
			if (actions) {
				inventoryRow.add(createButton(SORT_ICON, "meinysgui.sort.inventory", () -> InventoryActions.sort(client, true)));
			}
			inventoryRow.add(createButton(SETTINGS_ICON, "meinysgui.settings", () -> openSettings(client, screen)));
		}

		List<IconButton> buttons = new ArrayList<>();
		buttons.addAll(containerRow);
		buttons.addAll(inventoryRow);
		for (IconButton button : buttons) {
			Screens.getWidgets(screen).add(button);
		}

		updatePositions(client, screen, containerRow, inventoryRow);

		ScreenEvents.beforeExtract(screen).register((s, graphics, mouseX, mouseY, tickProgress) ->
				updatePositions(client, screen, containerRow, inventoryRow));

		ScreenMouseEvents.afterMouseRelease(screen).register((s, event, consumed) -> {
			for (IconButton button : buttons) {
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

	private static IconButton createButton(Identifier icon, String translationKey, Runnable action) {
		return new IconButton(SIZE, icon, Component.translatable(translationKey), button -> action.run());
	}

	private static void openSettings(Minecraft client, AbstractContainerScreen<?> screen) {
		if (InventorySorter.isSortableContainer(screen.getMenu())) {
			client.gui.setScreen(new ConfigScreen(screen));
		} else {
			screen.onClose();
			client.gui.setScreen(new ConfigScreen(null));
		}
	}

	private static void updatePositions(Minecraft client, AbstractContainerScreen<?> screen, List<IconButton> containerRow, List<IconButton> inventoryRow) {
		AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) screen;
		int left = accessor.meinysgui$getLeftPos();
		int top = accessor.meinysgui$getTopPos();

		placeRow(containerRow, left + accessor.meinysgui$getImageWidth() - 7, top + 10 - SIZE / 2);

		if (inventoryRow.isEmpty() || client.player == null) {
			return;
		}

		Slot first = findFirstInventorySlot(screen, client.player);

		boolean visible = first != null;
		if (screen instanceof CreativeModeInventoryScreen creativeScreen) {
			visible = visible && creativeScreen.isInventoryOpen();
		}
		for (IconButton button : inventoryRow) {
			button.visible = visible;
		}

		if (first != null) {
			int rightEdge = first.x + 17;
			for (Slot slot : screen.getMenu().slots) {
				if (slot.y == first.y) {
					rightEdge = Math.max(rightEdge, slot.x + 17);
				}
			}
			placeRow(inventoryRow, left + rightEdge, top + first.y - 7 - SIZE / 2);
		}
	}

	private static void placeRow(List<IconButton> row, int right, int y) {
		int x = right - SIZE;
		for (int i = row.size() - 1; i >= 0; i--) {
			row.get(i).setPosition(x, y);
			x -= SIZE + GAP;
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
