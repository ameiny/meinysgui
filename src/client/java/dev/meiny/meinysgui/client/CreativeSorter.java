package dev.meiny.meinysgui.client;

import java.util.ArrayList;
import java.util.List;
import dev.meiny.meinysgui.sort.InventorySorter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class CreativeSorter {
    private static final int MAIN_INVENTORY_START = 9;
    private static final int MAIN_INVENTORY_END = 36;

    private CreativeSorter() { }

    public static void sort(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || client.gameMode == null || !player.hasInfiniteMaterials()) {
            return;
        }
        if (!player.containerMenu.getCarried().isEmpty()) {
            return;
        }

        Inventory inventory = player.getInventory();
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = MAIN_INVENTORY_START; i < MAIN_INVENTORY_END; i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) {
                stacks.add(stack.copy());
            }
        }

        List<ItemStack> sorted = InventorySorter.mergeAndSort(stacks);

        for (int i = MAIN_INVENTORY_START; i < MAIN_INVENTORY_END; i++) {
            int position = i - MAIN_INVENTORY_START;
            ItemStack stack = position < sorted.size() ? sorted.get(position) : ItemStack.EMPTY;
            inventory.setItem(i, stack);
            client.gameMode.handleCreativeModeItemAdd(stack, i);
        }
    }
}