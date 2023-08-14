package davi.modid.item.weapons;

import davi.modid.Davi;
import mod.azure.azurelib.animatable.GeoItem;
import mod.azure.azurelib.animatable.SingletonGeoAnimatable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.function.Supplier;

public class PistolItem extends AnimatedItem {

    private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);

    public PistolItem() {
        super(new Item.Settings().maxCount(1).maxDamage(7));
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World worldIn, LivingEntity livingEntity, int remainingUseTicks) {
        if (livingEntity instanceof PlayerEntity playerEntity) {
            if (stack.getDamage() < (stack.getMaxDamage() - 1)) {
                playerEntity.getItemCooldownManager().set(this, 5);
                if (!worldIn.isClient) {
                    stack.damage(1, livingEntity, p -> p.sendToolBreakStatus(livingEntity.getActiveHand()));
                    var result = HWGGunBase.hitScanTrace(playerEntity, 64, 1.0F);
                    if (result != null) {
                        if (result.getEntity() instanceof LivingEntity livingEntity1)
                            livingEntity1.damage(playerEntity.getDamageSources().playerAttack(playerEntity), Davi.config.pistol_damage);
                    } else {
                        var bullet createArrow(worldIn, stack, playerEntity);
                    }
                }
            }
        }
    }

}

public BulletEntity createArrow(World worldIn, ItemStack stack, LivingEntity shooter) {
    var bullet = new
}
