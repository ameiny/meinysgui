package dev.meiny.meinysgui.network;

import dev.meiny.meinysgui.sort.HotbarMode;
import dev.meiny.meinysgui.sort.SortMode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record SortPayload(boolean playerInventory, HotbarMode hotbarMode, SortMode sortMode) implements CustomPacketPayload {
	public static final Type<SortPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("meinysgui", "sort_v2"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SortPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.BOOL, SortPayload::playerInventory,
			ByteBufCodecs.VAR_INT, (SortPayload payload) -> payload.hotbarMode().ordinal(),
			ByteBufCodecs.VAR_INT, (SortPayload payload) -> payload.sortMode().ordinal(),
			(Boolean playerInventory, Integer hotbarMode, Integer sortMode) ->
					new SortPayload(playerInventory, HotbarMode.byId(hotbarMode), SortMode.byId(sortMode)));

	@Override
	public @NonNull Type<SortPayload> type() {
		return TYPE;
	}
}
