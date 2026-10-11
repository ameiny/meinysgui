package dev.meiny.meinysgui.client;

import java.util.function.Predicate;
import com.mojang.blaze3d.platform.InputConstants;
import dev.meiny.meinysgui.MeinysGUI;
import dev.meiny.meinysgui.client.mixin.AbstractContainerScreenAccessor;
import dev.meiny.meinysgui.sort.InventorySorter;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.inventory.Slot;
import org.lwjgl.sdl.SDLScancode;

public final class InventoryHotkeys {
	private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(MeinysGUI.id("main"));

	private static final KeyMapping SORT = new KeyMapping("key.meinysgui.sort", InputConstants.Type.KEYBOARD, SDLScancode.SDL_SCANCODE_R, CATEGORY);
	private static final KeyMapping DEPOSIT_ALL = unbound("key.meinysgui.deposit_all");
	private static final KeyMapping DEPOSIT_MATCHING = unbound("key.meinysgui.deposit_matching");
	private static final KeyMapping TAKE_ALL = unbound("key.meinysgui.take_all");
	private static final KeyMapping TAKE_MATCHING = unbound("key.meinysgui.take_matching");

	private InventoryHotkeys() {
	}

	public static void init() {
		KeyMappingHelper.registerKeyMapping(SORT);
		KeyMappingHelper.registerKeyMapping(DEPOSIT_ALL);
		KeyMappingHelper.registerKeyMapping(DEPOSIT_MATCHING);
		KeyMappingHelper.registerKeyMapping(TAKE_ALL);
		KeyMappingHelper.registerKeyMapping(TAKE_MATCHING);

		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (!(screen instanceof AbstractContainerScreen<?> containerScreen)) {
				return;
			}

			ScreenKeyboardEvents.afterKeyPress(screen).register((s, event) -> {
				if (!(screen.getFocused() instanceof EditBox)) {
					handle(client, containerScreen, mapping -> mapping.matches(event));
				}
			});

			ScreenMouseEvents.afterMouseClick(screen).register((s, event, consumed) -> {
				handle(client, containerScreen, mapping -> mapping.matchesMouse(event));
				return consumed;
			});
		});
	}

	private static KeyMapping unbound(String name) {
		return new KeyMapping(name, InputConstants.UNKNOWN.getValue(), CATEGORY);
	}

	private static void handle(Minecraft client, AbstractContainerScreen<?> screen, Predicate<KeyMapping> pressed) {
		if (client.player == null) {
			return;
		}
		if (screen instanceof CreativeModeInventoryScreen creativeScreen && !creativeScreen.isInventoryOpen()) {
			return;
		}

		if (pressed.test(SORT)) {
			sort(client, screen);
		} else if (pressed.test(DEPOSIT_ALL)) {
			InventoryActions.depositAll(client);
		} else if (pressed.test(DEPOSIT_MATCHING)) {
			InventoryActions.depositMatching(client);
		} else if (pressed.test(TAKE_ALL)) {
			InventoryActions.takeAll(client);
		} else if (pressed.test(TAKE_MATCHING)) {
			InventoryActions.takeMatching(client);
		}
	}

	private static void sort(Minecraft client, AbstractContainerScreen<?> screen) {
		Slot hovered = ((AbstractContainerScreenAccessor) screen).meinysgui$getHoveredSlot();
		boolean storage = InventorySorter.isSortableContainer(screen.getMenu());

		boolean playerInventory;
		if (hovered != null && hovered.container == client.player.getInventory()) {
			playerInventory = true;
		} else if (hovered != null && storage) {
			playerInventory = false;
		} else {
			playerInventory = !storage;
		}

		InventoryActions.sort(client, playerInventory);
	}
}
