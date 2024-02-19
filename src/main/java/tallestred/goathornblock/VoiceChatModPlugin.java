package tallestred.goathornblock;

import de.maxhenkel.voicechat.api.*;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.plugins.impl.PositionImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import tallestred.goathornblock.common.blockentities.GoatHornBlockEntity;
import tallestred.goathornblock.common.blocks.GoatHornBlock;
import tallestred.goathornblock.config.GHBMConfig;

@ForgeVoicechatPlugin
public class VoiceChatModPlugin implements VoicechatPlugin {
    @Override
    public String getPluginId() {
        return GoatHornBlockMod.MODID;
    }

    @Override
    public void initialize(VoicechatApi api) {
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophone);
    }

    // If a player is near a goat horn the sound from the packet will be transmitted near the block on the other end
    private void onMicrophone(MicrophonePacketEvent event) {
        VoicechatConnection connection = event.getSenderConnection();
        if (connection != null) {
            if (event.getSenderConnection().getPlayer().getPlayer() instanceof Player player) {
                VoicechatServerApi api = event.getVoicechat();
                Level level = player.level();
                BlockPos soundPosition = new BlockPos(player.blockPosition());
                int soundRange = GHBMConfig.COMMON.goatHornSoundRange.get();
                ChunkAccess soundChunk = level.getChunkSource().getChunkNow(SectionPos.blockToSectionCoord(soundPosition.getX()), SectionPos.blockToSectionCoord(soundPosition.getZ()));
                if (soundChunk == null || !soundChunk.getStatus().isOrAfter(ChunkStatus.FULL))
                    return;
                BlockPos blockPos = BlockPos.findClosestMatch(soundPosition, soundRange, soundRange, (block) -> level.getChunkSource().getChunkNow(SectionPos.blockToSectionCoord(block.getX()), SectionPos.blockToSectionCoord(block.getZ())) != null && level.getChunkSource().getChunkNow(SectionPos.blockToSectionCoord(block.getX()), SectionPos.blockToSectionCoord(block.getZ())).getStatus().isOrAfter(ChunkStatus.FULL) && level.getChunkSource().getChunkNow(SectionPos.blockToSectionCoord(block.getX()), SectionPos.blockToSectionCoord(block.getZ())).getBlockState(block).getBlock() instanceof GoatHornBlock).orElse(null);
                if (blockPos != null) {
                    ChunkAccess goatPosChunk = level.getChunkSource().getChunkNow(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getZ()));
                    if (goatPosChunk != null && goatPosChunk.getStatus().isOrAfter(ChunkStatus.FULL) && goatPosChunk.getBlockEntity(blockPos) instanceof GoatHornBlockEntity goatHornBlockEntity && level.getBestNeighborSignal(blockPos) >= 1) {
                        this.repeatSoundToEnd(event, level.getBlockState(blockPos), level, blockPos, api);
                    }
                }
            }
        }
    }

    private void repeatSoundToEnd(MicrophonePacketEvent event, BlockState pState, Level pLevel, BlockPos pPos, VoicechatServerApi api) {
        for (Direction direction : Direction.values()) {
            Direction oppositeFacingDirection = pState.getValue(GoatHornBlock.FACING).getOpposite();
            if (direction == oppositeFacingDirection && pState.getValue(GoatHornBlock.POWERED)) {
                for (int i = 1; i < 90; ++i) {
                    BlockPos blockpos = pPos.relative(direction, i);
                    BlockState blockstate = pLevel.getBlockState(blockpos);
                    if (blockstate.getBlock() instanceof GoatHornBlock && pLevel.getBlockState(blockpos.relative(oppositeFacingDirection.getOpposite(), 1)).getBlock() instanceof RedStoneWireBlock) {
                        PositionImpl positionForSound = new PositionImpl(blockpos.getX(), blockpos.getY(), blockpos.getZ());
                        for (ServerPlayer player : api.getPlayersInRange(event.getSenderConnection().getPlayer().getServerLevel(), positionForSound, 5)) {
                            VoicechatConnection otherConnection = api.getConnectionOf(player);
                            api.sendLocationalSoundPacketTo(otherConnection, event.getPacket().locationalSoundPacketBuilder().position(positionForSound).build());
                        }
                    }
                }
            }
        }
    }
}
