package dev.meiny.meinysgui.client.config;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import dev.meiny.meinysgui.sort.HotbarMode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

public class ConfigScreen extends OptionsSubScreen {
	private static final int BUTTON_WIDTH = 150;
	private static final Boolean[] BOOLEANS = {false, true};

	public ConfigScreen(Screen parent) {
		super(parent, Minecraft.getInstance().options, Component.translatable("meinysgui.config.title"));
	}

	@Override
	protected void addOptions() {
		MeinysGuiConfig config = MeinysGuiConfig.get();

		list.addHeader(header("meinysgui.config.hotbar"));
		list.addSmall(List.<AbstractWidget>of(
				cycleButton("meinysgui.config.hotbar.sort", HotbarMode.values(),
						() -> config.sortHotbar, value -> config.sortHotbar = value, ConfigScreen::enumId),
				cycleButton("meinysgui.config.hotbar.deposit_all", BOOLEANS,
						() -> config.depositAllHotbar, value -> config.depositAllHotbar = value, ConfigScreen::hotbarUsageId),
				cycleButton("meinysgui.config.hotbar.deposit_matching", BOOLEANS,
						() -> config.depositMatchingHotbar, value -> config.depositMatchingHotbar = value, ConfigScreen::hotbarUsageId)));

		list.addHeader(header("meinysgui.config.buttons"));
		list.addSmall(List.<AbstractWidget>of(
				cycleButton("meinysgui.config.buttons.show", ButtonMode.values(),
						() -> config.buttons, value -> config.buttons = value, ConfigScreen::enumId)));
	}

	@Override
	public void removed() {
		super.removed();
		MeinysGuiConfig.save();
	}

	private static Component header(String key) {
		return Component.translatable(key).withStyle(ChatFormatting.UNDERLINE, ChatFormatting.BOLD);
	}

	private static <T> Button cycleButton(String key, T[] values, Supplier<T> getter, Consumer<T> setter, Function<T, String> valueId) {
		String initialId = valueId.apply(getter.get());
		Button cycleButton = Button.builder(optionLabel(key, initialId), button -> {
			T next = values[(indexOf(values, getter.get()) + 1) % values.length];
			setter.accept(next);
			String nextId = valueId.apply(next);
			button.setMessage(optionLabel(key, nextId));
			button.setTooltip(optionTooltip(key, nextId));
		}).width(BUTTON_WIDTH).build();
		cycleButton.setTooltip(optionTooltip(key, initialId));
		return cycleButton;
	}

	private static Component optionLabel(String key, String valueId) {
		return Component.translatable(key).append(": ").append(Component.translatable("meinysgui.config.value." + valueId));
	}

	private static Tooltip optionTooltip(String key, String valueId) {
		return Tooltip.create(Component.translatable(key + ".tooltip." + valueId));
	}

	private static <T> int indexOf(T[] values, T value) {
		for (int i = 0; i < values.length; i++) {
			if (values[i].equals(value)) {
				return i;
			}
		}
		return -1;
	}

	private static String enumId(Enum<?> value) {
		return value.name().toLowerCase(Locale.ROOT);
	}

	private static String hotbarUsageId(Boolean include) {
		return include ? "include" : "ignore";
	}
}
