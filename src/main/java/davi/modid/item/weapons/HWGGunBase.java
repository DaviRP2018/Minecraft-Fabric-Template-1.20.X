package davi.modid.item.weapons;


import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.List;

public class HWGGunBase extends Item {

    private BlockPos lightBlockPos = null;

    public HWGGunBase(Settings settings) { super(settings); }

    public void removeAmmo(Item ammo, PlayerEntity playerEntity) {
        if (!playerEntity.isCreative()) {
            for (ItemStack item : playerEntity.getInventory().offHand) {
                if (item.getItem() == ammo) {
                    item.decrement(1);
                    break;
                }
                for (ItemStack item1 : playerEntity.getInventory().main) {
                    if (item1.getItem() == ammo) {
                        item1.decrement(1);;
                        break;
                    }
                }
            }
        }
    }

    @Override
    public boolean hasGlint(ItemStack stack) { return false; }

    @Override
    public boolean isEnchantable(ItemStack stack) { return false; }

    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        return super.canRepair(stack, ingredient);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("Ammo: " + (stack.getMaxDamage() - stack.getDamage() - 1) + " / " + (stack.getMaxDamage() - 1)).formatted(Formatting.ITALIC));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        var itemStack = user.getStackInHand(hand);
        user.setCurrentHand(hand);
        return TypedActionResult.consume(itemStack);
    }

    protected void spawnLightSource(Entity entity, boolean isInWaterBlock) {
        if (lightBlockPos == null) {
            lightBlockPos = findFreeSpace(entity.getWorld(), entity.getBlockPos(), 2);
            if (lightBlockPos == null)
                return;
            entity.getWorld().setBlockState(lightBlockPos, AzureLibMod.TICKING_LIGHT_BLOCK.defaultBlockState());
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

    public static EntityHitResult hitScanTrace(PlayerEntity player, double range, float ticks) {
        var look = player.getOppositeRotationVector(ticks);
        var start = player.getCameraPosVec(ticks);
        var end = new Vec3d(player.getX() + look.x * range, player.getEyeY() + look.y * range, player.getZ() + look.z * range);
        var traceDistance = player.getWorld().raycast(new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player)).getPos().squaredDistanceTo(end);
        for (var possible : player.getWorld().getOtherEntities(player, player.getBoundingBox().stretch(look.multiply(traceDistance)).expand(3.00, 3.00, 3.00), (target -> !target.isSpectator() && player.canHit() && player.canSee(target)))) {
            if (possible.getBoundingBox().expand(0.3D).raycast(start, end).isPresent())
                if (start.squaredDistanceTo(possible.getBoundingBox().expand(0.3D).raycast(start, end).get()) < traceDistance)
                    return ProjectileUtil.getEntityCollision(player.getWorld(), player, start, end, player.getBoundingBox().stretch(look.multiply(traceDistance)).expand(3.00, 3.00, 3.00), (target) -> !target.isSpectator() && player.isAttackable() && player.canSee(target));
        }
        return null;
    }
}
