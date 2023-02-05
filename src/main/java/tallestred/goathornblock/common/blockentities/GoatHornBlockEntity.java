package tallestred.goathornblock.common.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import tallestred.goathornblock.GoatHornBlockMod;

public class GoatHornBlockEntity extends BlockEntity {
    protected final String GOAT_HORN_ITEM = "goat_horn_id";
    protected ItemStack goatHornItemDrop;

    public GoatHornBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(GoatHornBlockMod.GOAT_HORN_BLOCK_ENTITY.get(), pPos, pBlockState);
    }

    @Override
    public BlockEntityType<?> getType() {
        return GoatHornBlockMod.GOAT_HORN_BLOCK_ENTITY.get();
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        if (pTag.contains(GOAT_HORN_ITEM, 10)) {
            this.setGoatHornItemDrop(ItemStack.of(pTag.getCompound(GOAT_HORN_ITEM)));
        }
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
    }

    public ItemStack getGoatHornItemDrop() {
        return goatHornItemDrop;
    }

    public void setGoatHornItemDrop(ItemStack goatHornItemDrop) {
        this.goatHornItemDrop = goatHornItemDrop;
        this.setChanged();
    }
}

