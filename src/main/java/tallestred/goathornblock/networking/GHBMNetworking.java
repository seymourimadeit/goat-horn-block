package tallestred.goathornblock.networking;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import tallestred.goathornblock.GoatHornBlockMod;
import tallestred.goathornblock.common.blockentities.GoatHornBlockEntity;
import tallestred.goathornblock.networking.packets.SoundInfoPacket;

public class GHBMNetworking {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(GoatHornBlockMod.MODID, "main"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    public static void registerPackets() {
        int id = 0;
        INSTANCE.registerMessage(id++, SoundInfoPacket.class, SoundInfoPacket::encode, SoundInfoPacket::decode, SoundInfoPacket::handle);
    }

    @OnlyIn(Dist.CLIENT)
    public static void transferSoundFromPacket(SoundInfoPacket packet) {
        LocalPlayer player = Minecraft.getInstance().player;
        Level level = player.getLevel();
        if (level.getBlockEntity(packet.getPos()) instanceof GoatHornBlockEntity entity) {
            entity.setSoundEvent(packet.getLocation());
        }
    }
}
