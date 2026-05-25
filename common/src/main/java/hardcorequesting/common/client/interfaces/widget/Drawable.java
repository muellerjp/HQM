package hardcorequesting.common.client.interfaces.widget;

import net.minecraft.client.gui.GuiGraphics;

public interface Drawable {
    void render(GuiGraphics guiGraphics, int mX, int mY);

    default void renderTooltip(GuiGraphics guiGraphics, int mX, int mY) {}
}
