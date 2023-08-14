package davi.modid.item.weapons;

import mod.azure.azurelib.AzureLibMod;
import mod.azure.azurelib.animatable.GeoItem;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager.ControllerRegistrar;
import mod.azure.azurelib.core.animation.Animation.LoopType;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.animation.RawAnimation;
import mod.azure.azurelib.core.object.PlayState;
import mod.azure.azurelib.entities.TickingLightEntity;
import mod.azure.azurelib.util.AzureLibUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class AnimatedItem extends HWGGunBase implements GeoItem {

    private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);
    private BlockPos lightBlockPos = null;

    public AnimatedItem(Settings settings) { super(settings); }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        var itemStack = user.getStackInHand(hand);
        user.setCurrentHand(hand);
        return TypedActionResult.consume(itemStack);
    }

    @Override
    public void registerControllers(ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "shoot_controller", event -> PlayState.CONTINUE).triggerableAnim("firing", RawAnimation.begin().then("firing", LoopType.PLAY_ONCE)).triggerableAnim("reload", RawAnimation.begin().then("reload", LoopType.PLAY_ONCE)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    protected void spawnLightSource(Entity entity, boolean isInWaterBlock) {
        if (lightBlockPos == null) {
            lightBlockPos = findFreeSpace(entity.getWorld(), entity.getBlockPos(), 2);
            if (lightBlockPos == null)
                return;
            entity.getWorld().setBlockState(lightBlockPos, AzureLibMod.TICKING_LIGHT_BLOCK.getDefaultState());
        } else if (checkDistance(lightBlockPos, entity.getBlockPos(), 2)) {
            var blockEntity = entity.getWorld().getBlockEntity(lightBlockPos);
            if (blockEntity instanceof TickingLightEntity)
                ((TickingLightEntity) blockEntity).refresh(isInWaterBlock ? 20 : 0);
            else
                lightBlockPos = null;
        } else
            lightBlockPos = null;
    }

    private boolean checkDistance(BlockPos blockPosA, BlockPos blockPosB, int distance) {
        return Math.abs(blockPosA.getX() - blockPosB.getX()) <= distance && Math.abs(blockPosA.getY() - blockPosB.getY()) <= distance && Math.abs(blockPosA.getZ() - blockPosB.getZ()) <= distance;
    }

    private BlockPos findFreeSpace(World world, BlockPos blockPos, int maxDistance) {
        if (blockPos == null)
            return null;

        var offsets = new int[maxDistance * 2 + 1];
        offsets[0] = 0;
        for (int i = 2; i <= maxDistance * 2; i += 2) {
            offsets[i - 1] = i / 2;
            offsets[i] = -i / 2;
        }
        for (int x : offsets)
            for (int y : offsets)
                for (int z : offsets) {
                    BlockPos offsetPos = blockPos.add(x, y, z);
                    BlockState state = world.getBlockState(offsetPos);
                    if (state.isAir() || state.getBlock().equals(AzureLibMod.TICKING_LIGHT_BLOCK))
                        return offsetPos;
                }

        return null;
    }
}
