package tallestred.goathornblock.networking.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import tallestred.goathornblock.networking.GHBMNetworking;

import java.util.function.Supplier;

public class SoundInfoPacket {
    private final BlockPos pos;
    private final ResourceLocation location;

    public SoundInfoPacket(BlockPos pos, ResourceLocation location) {
        this.pos = pos;
        this.location = location;
    }

    public static SoundInfoPacket decode(FriendlyByteBuf buf) {
        return new SoundInfoPacket(buf.readBlockPos(), buf.readResourceLocation());
    }

    public static void encode(SoundInfoPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeResourceLocation(msg.location);
    }

    public static void handle(SoundInfoPacket msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            GHBMNetworking.transferSoundFromPacket(msg);
        });
        context.get().setPacketHandled(true);
    }

    public ResourceLocation getLocation() {
        return location;
    }

    public BlockPos getPos() {
        return pos;
    }
}
