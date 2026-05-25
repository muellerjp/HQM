package hardcorequesting.common.items.crafting;

import hardcorequesting.common.items.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

public class BookCatalystRecipe implements CraftingRecipe {

    private final ShapedRecipe inner;

    public BookCatalystRecipe(ShapedRecipe inner) {
        this.inner = inner;
    }

    public ShapedRecipe inner() {
        return inner;
    }

    @Override
    public boolean matches(CraftingInput container, Level level) {
        return inner.matches(container, level);
    }

    @Override
    public ItemStack assemble(CraftingInput container, HolderLookup.Provider provider) {
        return inner.assemble(container, provider);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return inner.canCraftInDimensions(width, height);
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return inner.getResultItem(provider);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput container) {
        NonNullList<ItemStack> remaining = inner.getRemainingItems(container);

        for (int i = 0; i < remaining.size(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.is(ModItems.book.get()) || stack.is(ModItems.enabledBook.get())) {
                remaining.set(i, stack.copy());
            }
        }
        return remaining;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return inner.getIngredients();
    }

    @Override
    public boolean isSpecial() {
        return inner.isSpecial();
    }

    @Override
    public CraftingBookCategory category() {
        return inner.category();
    }

    @Override
    public String getGroup() {
        return inner.getGroup();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.bookCatalystSerializer.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeType.CRAFTING;
    }
}
