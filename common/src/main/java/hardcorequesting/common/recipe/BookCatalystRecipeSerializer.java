package hardcorequesting.common.recipe;

import com.mojang.serialization.MapCodec;
import hardcorequesting.common.items.crafting.BookCatalystRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

public class BookCatalystRecipeSerializer implements RecipeSerializer<BookCatalystRecipe> {

    private static final MapCodec<BookCatalystRecipe> CODEC = RecipeSerializer.SHAPED_RECIPE.codec().xmap(
            BookCatalystRecipe::new,
            BookCatalystRecipe::inner
    );

    private static final StreamCodec<RegistryFriendlyByteBuf, BookCatalystRecipe> STREAM_CODEC =
            RecipeSerializer.SHAPED_RECIPE.streamCodec().map(
                    BookCatalystRecipe::new,
                    BookCatalystRecipe::inner
            );

    @Override
    public MapCodec<BookCatalystRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, BookCatalystRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
