package tallestred.goathornblock.common.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import tallestred.goathornblock.GoatHornBlockMod;
import tallestred.goathornblock.common.blocks.GoatHornBlock;

public class GoatHornBlockEntity extends BlockEntity {
    protected final String GOAT_HORN_ITEM = "goat_horn_id";
    protected final String GOAT_SOUND_EVENT = "goat_sound";
    protected ItemStack goatHornItemDrop;
    protected ResourceLocation soundEvent;

    public GoatHornBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(GoatHornBlockMod.GOAT_HORN_BLOCK_ENTITY.get(), pPos, pBlockState);
    }

    public static void serverTick(Level pLevel, BlockPos pPos, BlockState pState, GoatHornBlockEntity pBlockEntity) {
        for (Direction direction : Direction.values()) {
            if (direction == pState.getValue(GoatHornBlock.FACING).getOpposite() && pState.getValue(GoatHornBlock.POWERED)) {
                for (int i = 1; i < 90; ++i) {
                    BlockPos blockpos = pPos.relative(direction, i);
                    BlockState blockstate = pLevel.getBlockState(blockpos);
                    if (blockstate.getBlock() instanceof GoatHornBlock hornBlock) {
                        if (pLevel.getBlockEntity(blockpos) instanceof GoatHornBlockEntity) {
                            pLevel.setBlock(blockpos, blockstate.setValue(GoatHornBlock.SOUND, Boolean.valueOf(true)), 3);
                            if (blockstate.getValue(GoatHornBlock.SOUND) && pBlockEntity.getSoundEvent() != null)
                                hornBlock.setSounds(pBlockEntity, pLevel, blockpos, blockstate);
                            pBlockEntity.setSoundEvent(ResourceLocation.tryParse(""));
                        }
                    }
                }
            }
        }
    }


    @Override
    public BlockEntityType<?> getType() {
        return GoatHornBlockMod.GOAT_HORN_BLOCK_ENTITY.get();
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        if (pTag.contains(GOAT_HORN_ITEM, 10))
            this.setGoatHornItemDrop(ItemStack.of(pTag.getCompound(GOAT_HORN_ITEM)));
        if (pTag.contains(GOAT_SOUND_EVENT))
            this.setSoundEvent(ResourceLocation.tryParse(GOAT_SOUND_EVENT));
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        if (this.getGoatHornItemDrop() != null)
            pTag.put(GOAT_HORN_ITEM, this.getGoatHornItemDrop().save(new CompoundTag()));
        if (this.getSoundEvent() != null) {
            pTag.putString(GOAT_SOUND_EVENT, this.getSoundEvent().toString());
        }
    }

    public ItemStack getGoatHornItemDrop() {
        return goatHornItemDrop;
    }

    public void setGoatHornItemDrop(ItemStack goatHornItemDrop) {
        this.goatHornItemDrop = goatHornItemDrop;
        this.setChanged();
    }

    public ResourceLocation getSoundEvent() {
        return soundEvent;
    }

    public void setSoundEvent(ResourceLocation soundEvent) {
        this.soundEvent = soundEvent;
        this.setChanged();
    }
}

