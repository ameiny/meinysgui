package dev.meiny.meinysgui.network;

import dev.meiny.meinysgui.sort.HotbarMode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record SortPayloadV2(boolean playerInventory, HotbarMode hotbarMode) implements CustomPacketPayload {
	public static final Type<SortPayloadV2> TYPE = new Type<>(Identifier.fromNamespaceAndPath("meinysgui", "sort_v2"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SortPayloadV2> CODEC = StreamCodec.composite(
			ByteBufCodecs.BOOL, SortPayloadV2::playerInventory,
			ByteBufCodecs.VAR_INT, (SortPayloadV2 payload) -> payload.hotbarMode().ordinal(),
			(Boolean playerInventory, Integer hotbarMode) -> new SortPayloadV2(playerInventory, HotbarMode.byId(hotbarMode)));

	@Override
	public @NonNull Type<SortPayloadV2> type() {
		return TYPE;
	}
}
