package hardcorequesting.neoforge;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
class NeoClientEvents {
    static void registerClientTick(Consumer<Minecraft> consumer) {
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) ->
                consumer.accept(Minecraft.getInstance()));
    }
}
