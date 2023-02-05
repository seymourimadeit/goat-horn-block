package tallestred.goathornblock.common.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
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
        if (this.getGoatHornItemDrop() != null) {
            if (pTag.contains(GOAT_HORN_ITEM, 10)) {
                this.setGoatHornItemDrop(new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation((GOAT_HORN_ITEM)))));
            }
        }
    }

    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        if (this.getGoatHornItemDrop() != null)
            pTag.putString(GOAT_HORN_ITEM, this.getGoatHornItemDrop().toString());
    }

    public ItemStack getGoatHornItemDrop() {
        return goatHornItemDrop;
    }

    public void setGoatHornItemDrop(ItemStack goatHornItemDrop) {
        this.goatHornItemDrop = goatHornItemDrop;
        this.setChanged();
    }
}

