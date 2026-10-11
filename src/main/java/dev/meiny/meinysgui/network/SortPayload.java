package dev.meiny.meinysgui.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record SortPayload(boolean playerInventory) implements CustomPacketPayload {
	public static final Type<SortPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("meinysgui", "sort"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SortPayload> CODEC =
			StreamCodec.composite(ByteBufCodecs.BOOL, SortPayload::playerInventory, SortPayload::new);

	@Override
	public @NonNull Type<SortPayload> type() {
		return TYPE;
	}
}
