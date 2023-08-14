package davi.modid.util.registry;

import davi.modid.Davi;
import davi.modid.entity.projectiles.BulletEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.LinkedList;
import java.util.List;

public class ProjectilesEntityRegister {

    public static List<EntityType<? extends Entity>> ENTITY_TYPES = new LinkedList();
    public static List<EntityType<? extends Entity>> ENTITY_THAT_USE_ITEM_RENDERS = new LinkedList();

    public static EntityType<BulletEntity> BULLETS = projectile(BulletEntity::new, "bullets");

    private static <T extends Entity> EntityType<T> projectile(EntityType.EntityFactory<T> factory, String id) {
        return projectile(factory, id, true);
    }

    private static <T extends Entity> EntityType<T> projectile(EntityType.EntityFactory<T> factory, String id, boolean itemRender) {

        EntityType<T> type = FabricEntityTypeBuilder.<T>create(SpawnGroup.MISC, factory).dimensions(new EntityDimensions(0.5F, 0.5F, true)).disableSummon().spawnableFarFromPlayer().trackRangeBlocks(90).trackedUpdateRate(1).build();

        Registry.register(Registries.ENTITY_TYPE, new Identifier(Davi.MOD_ID, id), type);

        ENTITY_TYPES.add(type);

        if (itemRender)
            ENTITY_THAT_USE_ITEM_RENDERS.add(type);

        return type;
    }
}
