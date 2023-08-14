package davi.modid.entity.projectiles;

import davi.modid.Davi;
import davi.modid.item.ModItems;
import davi.modid.util.registry.ProjectilesEntityRegister;

import mod.azure.azurelib.animatable.GeoEntity;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.object.PlayState;
import mod.azure.azurelib.network.packet.EntityPacket;
import mod.azure.azurelib.util.AzureLibUtil;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

public class BulletEntity extends ArrowEntity implements GeoEntity {

    public static final TrackedData<Float> FORCED_YAW =
            DataTracker.registerData(BulletEntity.class, TrackedDataHandlerRegistry.FLOAT);
    protected static float bulletDamage;
    private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);
    public SoundEvent hitSound = this.getHitSound();
    protected int ticksInAir;

    public BulletEntity(EntityType<? extends BulletEntity> entityType, World world) {
        super(entityType, world);
        this.pickupType = PickupPermission.DISALLOWED;
    }

    public BulletEntity(World world, LivingEntity owner, Float damage) {
        super(ProjectilesEntityRegister.BULLETS, owner, world);
        bulletDamage = damage;
    }

    protected BulletEntity(EntityType<? extends BulletEntity> type, double x, double y, double z, World world) {
        this(type, world);
    }

    public BulletEntity(World world, double x, double y, double z) {
        super(ProjectilesEntityRegister.BULLETS, x, y, z, world);
        this.setNoGravity(true);
        this.setDamage(0);
    }

    protected BulletEntity(EntityType<? extends BulletEntity> type, LivingEntity owner, World world) {
        this(type, owner.getX(), owner.getEyeY() - 0.10000000149011612D, owner.getZ(), world);
        this.setOwner(owner);
        if (owner instanceof PlayerEntity) this.pickupType = PickupPermission.ALLOWED;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(
                        this,
                        event -> {
                            return PlayState.CONTINUE;
                        }));
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket() {
        return EntityPacket.createPacket(this);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    protected void onHit(LivingEntity living) {
        super.onHit(living);
        if (Davi.config.bullets_disable_iframes_on_players || !(living instanceof PlayerEntity)) {
            living.timeUntilRegen = 0;
            living.setVelocity(0, 0, 0);
        }
    }

    @Override
    public void age() {
        ++this.ticksInAir;
        if (this.ticksInAir >= 40) this.remove(RemovalReason.DISCARDED);
    }

    @Override
    public void setVelocity(double x, double y, double z, float speed, float divergence) {
        super.setVelocity(x, y, z, speed, divergence);
        this.ticksInAir = 0;
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.getDataTracker().set(FORCED_YAW, 0f);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound tag) {
        super.writeCustomDataToNbt(tag);
        tag.putShort("life", (short) this.ticksInAir);
        tag.putFloat("ForcedYaw", dataTracker.get(FORCED_YAW));
    }

    @Override
    public void tick() {
        super.tick();
        ++this.ticksInAir;
        if (this.ticksInAir >= 40) this.remove(RemovalReason.DISCARDED);
        if (this.getWorld().isClient()) {
            double x = this.getX() + (this.random.nextDouble()) * (double) this.getWidth() * 0.50;
            double z = this.getZ() + (this.random.nextDouble()) * (double) this.getWidth() * 0.50;
            this.getWorld().addParticle(ParticleTypes.SMOKE, true, x, this.getY(), z, 0, 0, 0);
        }
        if (getOwner() instanceof PlayerEntity owner) setYaw(dataTracker.get(FORCED_YAW));
    }

    @Override
    public boolean hasNoGravity() {
        return !this.isSubmergedInWater();
    }

    @Override
    public void setSound(SoundEvent soundIn) {
        this.hitSound = soundIn;
    }

    @Override
    protected SoundEvent getHitSound() {
        return SoundEvents.ITEM_ARMOR_EQUIP_IRON;
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        var entity = entityHitResult.getEntity();
        if (entityHitResult.getType() != HitResult.Type.ENTITY
                || !((EntityHitResult) entityHitResult).getEntity().isPartOf(entity))
            if (!this.getWorld().isClient()) this.remove(RemovalReason.DISCARDED);
        var entity2 = this.getOwner();
        DamageSource damageSource2;
        if (entity2 == null) damageSource2 = getDamageSources().arrow(this, this);
        else {
            damageSource2 = getDamageSources().arrow(this, entity2);
            if (entity2 instanceof LivingEntity) ((LivingEntity) entity2).onAttacking(entity);
        }
        if (entity.damage(damageSource2, bulletDamage)) {
            if (entity instanceof LivingEntity livingEntity) {
                if (!this.getWorld().isClient() && entity2 instanceof LivingEntity) {
                    EnchantmentHelper.onTargetDamaged(livingEntity, entity2);
                    EnchantmentHelper.onUserDamaged((LivingEntity) entity2, livingEntity);
                }
                this.onHit(livingEntity);
                if (livingEntity != entity2
                        && livingEntity instanceof PlayerEntity
                        && entity2 instanceof ServerPlayerEntity
                        && !this.isSilent())
                    ((ServerPlayerEntity) entity2)
                            .networkHandler.sendPacket(
                                    new GameStateChangeS2CPacket(
                                            GameStateChangeS2CPacket.PROJECTILE_HIT_PLAYER, 0.0F));
            }
        } else if (!this.getWorld().isClient()) this.remove(RemovalReason.DISCARDED);
    }

    @Override
    public ItemStack asItemStack() { return new ItemStack(ModItems.BULLETS); }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean shouldRender(double distance) {
        return true;
    }
}
