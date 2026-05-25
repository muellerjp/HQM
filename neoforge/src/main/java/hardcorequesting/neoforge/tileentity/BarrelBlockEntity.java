package hardcorequesting.neoforge.tileentity;

import hardcorequesting.common.quests.data.ItemsTaskData;
import hardcorequesting.common.quests.task.QuestTask;
import hardcorequesting.common.quests.task.item.ConsumeItemTask;
import hardcorequesting.common.tileentity.AbstractBarrelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.UUID;

public class BarrelBlockEntity extends AbstractBarrelBlockEntity {
    public final IFluidHandler fluidHandler = new IFluidHandler() {
        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            QuestTask<?> task = getCurrentTask();
            if (task instanceof ConsumeItemTask consumeTask) {
                return consumeTask.canTakeFluid(stack.getFluid(), getPlayerUUID());
            }
            return false;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            QuestTask<?> task = getCurrentTask();
            if (task instanceof ConsumeItemTask consumeTask) {
                UUID playerUUID = getPlayerUUID();
                FluidStack duplicate = resource.copy();
                if (consumeTask.increaseFluid(
                        dev.architectury.fluid.FluidStack.create(duplicate.getFluid(), duplicate.getAmount()),
                        playerUUID, action.execute()) && action.execute()) {
                    ItemsTaskData data = consumeTask.getData(playerUUID);
                    consumeTask.doCompletionCheck(data, playerUUID);
                    updateState();
                    doSync();
                }
                return resource.getAmount() - duplicate.getAmount();
            }
            return 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        }
    };

    public BarrelBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }
}
