package dev.meiny.meinysgui.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class SortButton extends Button.Plain {
	private static final int PRESSED_OVERLAY = 0x60000000;
	private static final Identifier ICON = Identifier.fromNamespaceAndPath("meinysgui", "sort");
	private static final int ICON_SIZE = 8;

	private boolean pressed;

	public SortButton(int size, Component tooltip, OnPress onPress) {
		super(0, 0, size, size, Component.empty(), button -> {
			((SortButton) button).pressed = true;
			onPress.onPress(button);
		}, DEFAULT_NARRATION);
		setTooltip(Tooltip.create(tooltip));
	}

	public void setPressed(boolean pressed) {
		this.pressed = pressed;
	}

	@Override
	public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float tickProgress) {
		super.extractContents(graphics, mouseX, mouseY, tickProgress);

		if (pressed && isHovered()) {
			graphics.fill(getX() + 1, getY() + 1, getX() + getWidth() - 1, getY() + getHeight() - 1, PRESSED_OVERLAY);
		}

		int iconX = getX() + (getWidth() - ICON_SIZE) / 2;
		int iconY = getY() + (getHeight() - ICON_SIZE) / 2;
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ICON, iconX, iconY, ICON_SIZE, ICON_SIZE);
	}
}
