package dev.meiny.meinysgui.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import dev.meiny.meinysgui.sort.HotbarMode;
import dev.meiny.meinysgui.sort.InventorySorter;
import dev.meiny.meinysgui.sort.SortMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class CreativeSorter {
    private static final int HOTBAR_MENU_OFFSET = 36;
    private static final int[] MAIN = IntStream.range(InventorySorter.MAIN_INVENTORY_START, InventorySorter.MAIN_INVENTORY_END).toArray();
    private static final int[] HOTBAR = IntStream.range(InventorySorter.HOTBAR_START, InventorySorter.MAIN_INVENTORY_START).toArray();

    private CreativeSorter() { }

    public static void sort(Minecraft client, HotbarMode hotbarMode, SortMode sortMode) {
        LocalPlayer player = client.player;
        if (player == null || client.gameMode == null || !player.hasInfiniteMaterials()) {
            return;
        }
        if (!player.containerMenu.getCarried().isEmpty()) {
            return;
        }

        for (int[] group : groups(hotbarMode)) {
            sortGroup(client, player.getInventory(), group, sortMode);
        }
    }

    private static List<int[]> groups(HotbarMode hotbarMode) {
        return switch (hotbarMode) {
            case IGNORE -> List.of(MAIN);
            case INCLUDE -> List.of(IntStream.concat(Arrays.stream(MAIN), Arrays.stream(HOTBAR)).toArray());
            case SEPARATE -> List.of(MAIN, HOTBAR);
        };
    }

    private static void sortGroup(Minecraft client, Inventory inventory, int[] indices, SortMode sortMode) {
        List<ItemStack> stacks = new ArrayList<>();
        for (int index : indices) {
            ItemStack stack = inventory.getItem(index);
            if (!stack.isEmpty()) {
                stacks.add(stack.copy());
            }
        }

        List<ItemStack> sorted = InventorySorter.mergeAndSort(stacks, sortMode);

        for (int position = 0; position < indices.length; position++) {
            int index = indices[position];
            ItemStack stack = position < sorted.size() ? sorted.get(position) : ItemStack.EMPTY;
            inventory.setItem(index, stack);
            client.gameMode.handleCreativeModeItemAdd(stack, menuSlot(index));
        }
    }

    private static int menuSlot(int index) {
        return index < InventorySorter.MAIN_INVENTORY_START ? index + HOTBAR_MENU_OFFSET : index;
    }
}
