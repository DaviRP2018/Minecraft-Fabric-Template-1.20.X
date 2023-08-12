package davi.modid.item.custom;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;


public class MetalDetectorItem extends Item {
    // Este item detecta metais 20 blocos abaixo dele
    public MetalDetectorItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (!context.getWorld().isClient()) {
            BlockPos positionClicked = context.getBlockPos();
            PlayerEntity player = context.getPlayer();
            assert player != null;

            for (int i = 0; i <= 20; i++) {
                BlockPos positionClickedDown = positionClicked.down(i);
                BlockState state = context.getWorld().getBlockState(positionClickedDown);

                if (isMetalOreBlock(state)) {
                    outputMetalOreDetection(player);
                    break;
                }
            }
        }

        context.getStack().damage(
            1,
                Objects.requireNonNull(context.getPlayer()),
            playerEntity -> playerEntity.sendToolBreakStatus(playerEntity.getActiveHand())
        );

        return ActionResult.SUCCESS;
    }

    private void outputMetalOreDetection(PlayerEntity player) {
        player.sendMessage(Text.literal("<beep> <beep>"), false);
    }

    private boolean isMetalOreBlock(BlockState state) {
        return state.isOf(Blocks.IRON_ORE) || state.isOf(Blocks.COPPER_ORE) || state.isOf(Blocks.GOLD_ORE);
    }
}
