package hardcorequesting.neoforge;

import com.mojang.brigadier.CommandDispatcher;
import dev.architectury.fluid.FluidStack;
import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.blocks.ModBlocks;
import hardcorequesting.common.config.HQMConfig;
import hardcorequesting.common.items.ModItems;
import hardcorequesting.common.platform.AbstractPlatform;
import hardcorequesting.common.platform.NetworkManager;
import hardcorequesting.common.recipe.BookCatalystRecipeSerializer;
import hardcorequesting.common.tileentity.AbstractBarrelBlockEntity;
import hardcorequesting.common.util.Fraction;
import hardcorequesting.neoforge.tileentity.BarrelBlockEntity;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.AnimalTameEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.entity.player.AnvilRepairEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.util.TriConsumer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Mod("hardcorequesting")
public class HardcoreQuestingNeoForge implements AbstractPlatform {
    private final NetworkManager networkManager = new NeoNetworkingManager();
    private final DeferredRegister<SoundEvent> sounds = DeferredRegister.create(Registries.SOUND_EVENT, HardcoreQuestingCore.ID);
    private final DeferredRegister<Block> block = DeferredRegister.create(Registries.BLOCK, HardcoreQuestingCore.ID);
    private final DeferredRegister<Item> item = DeferredRegister.create(Registries.ITEM, HardcoreQuestingCore.ID);
    private final DeferredRegister<RecipeSerializer<?>> recipe = DeferredRegister.create(Registries.RECIPE_SERIALIZER, HardcoreQuestingCore.ID);
    private final DeferredRegister<BlockEntityType<?>> tileEntityType = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, HardcoreQuestingCore.ID);
    private final DeferredRegister<CreativeModeTab> creativeTabs = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, HardcoreQuestingCore.ID);
    private DeferredHolder<CreativeModeTab, CreativeModeTab> hqmTab;

    public HardcoreQuestingNeoForge(IEventBus modEventBus) {
        sounds.register(modEventBus);
        block.register(modEventBus);
        item.register(modEventBus);
        recipe.register(modEventBus);
        tileEntityType.register(modEventBus);
        creativeTabs.register(modEventBus);

        modEventBus.addListener(this::onRegisterPayloads);
        modEventBus.addListener(this::onRegisterCapabilities);
        modEventBus.addListener(this::onBuildCreativeTab);

        HardcoreQuestingCore.initialize(this);

        hqmTab = creativeTabs.register("hardcorequesting", () ->
                CreativeModeTab.builder()
                        .icon(() -> new ItemStack(ModItems.book.get()))
                        .title(net.minecraft.network.chat.Component.translatable("itemGroup.hardcorequesting.hardcorequesting"))
                        .build());

        NeoForge.EVENT_BUS.<LivingDropsEvent>addListener(event -> {
            if (!(event.getEntity() instanceof Player player)) return;
            if (player instanceof FakePlayer
                    || event.isCanceled()
                    || player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)
                    || HQMConfig.getInstance().LOSE_QUEST_BOOK) {
                return;
            }
            Iterator<ItemEntity> iter = event.getDrops().iterator();
            while (iter.hasNext()) {
                ItemEntity entityItem = iter.next();
                ItemStack stack = entityItem.getItem();
                if (stack.is(ModItems.book.get())) {
                    player.getInventory().add(stack);
                    iter.remove();
                }
            }
        });

        NeoForge.EVENT_BUS.<PlayerEvent.Clone>addListener(event -> {
            if (event.getEntity() == null || event.getEntity() instanceof FakePlayer
                    || !event.isWasDeath()
                    || event.getEntity().level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)
                    || HQMConfig.getInstance().LOSE_QUEST_BOOK) {
                return;
            }
            ItemStack bookStack = new ItemStack(ModItems.book.get());
            if (event.getOriginal().getInventory().contains(bookStack)) {
                for (ItemStack stack : event.getOriginal().getInventory().items) {
                    if (stack.is(ModItems.book.get())) {
                        bookStack = stack.copy();
                        break;
                    }
                }
                event.getEntity().getInventory().add(bookStack);
            }
        });
    }

    private void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playBidirectional(
                HQMPacketPayload.TYPE,
                HQMPacketPayload.CODEC,
                new DirectionalPayloadHandler<>(
                        NeoNetworkingManager::handleClientBound,
                        NeoNetworkingManager::handleServerBound));
    }

    @SuppressWarnings("unchecked")
    private void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                (BlockEntityType<BarrelBlockEntity>) (BlockEntityType<?>) ModBlocks.typeBarrel.get(),
                (be, side) -> be.fluidHandler);
    }

    private void onBuildCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (hqmTab != null && event.getTabKey() == hqmTab.getKey()) {
            event.accept(ModItems.book.get());
            event.accept(ModItems.enabledBook.get());
            event.accept(ModItems.basicBag.get());
            event.accept(ModItems.goodBag.get());
            event.accept(ModItems.greaterBag.get());
            event.accept(ModItems.epicBag.get());
            event.accept(ModItems.legendaryBag.get());
            event.accept(ModItems.quarterHeart.get());
            event.accept(ModItems.halfHeart.get());
            event.accept(ModItems.threeQuartsHeart.get());
            event.accept(ModItems.heart.get());
            event.accept(ModItems.rottenHeart.get());
        }
    }

    @Override
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public NetworkManager getNetworkManager() {
        return networkManager;
    }

    @Override
    public MinecraftServer getServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }

    @Override
    public String getModVersion() {
        return net.neoforged.fml.ModList.get()
                .getModContainerById(HardcoreQuestingCore.ID)
                .map(c -> c.getModInfo().getVersion().toString())
                .orElse("unknown");
    }

    @Override
    public boolean isClient() {
        return FMLEnvironment.dist == Dist.CLIENT;
    }

    @Override
    public void registerOnCommandRegistration(Consumer<CommandDispatcher<CommandSourceStack>> consumer) {
        NeoForge.EVENT_BUS.<RegisterCommandsEvent>addListener(event -> consumer.accept(event.getDispatcher()));
    }

    @Override
    public void registerOnWorldLoad(BiConsumer<ResourceKey<Level>, ServerLevel> biConsumer) {
        NeoForge.EVENT_BUS.<LevelEvent.Load>addListener(event -> {
            if (event.getLevel() instanceof ServerLevel serverLevel)
                biConsumer.accept(serverLevel.dimension(), serverLevel);
        });
    }

    @Override
    public void registerOnWorldSave(Consumer<ServerLevel> consumer) {
        NeoForge.EVENT_BUS.<LevelEvent.Save>addListener(event -> {
            if (event.getLevel() instanceof ServerLevel serverLevel)
                consumer.accept(serverLevel);
        });
    }

    @Override
    public void registerOnPlayerJoin(Consumer<ServerPlayer> consumer) {
        NeoForge.EVENT_BUS.<PlayerEvent.PlayerLoggedInEvent>addListener(event ->
                consumer.accept((ServerPlayer) event.getEntity()));
    }

    @Override
    public void registerOnServerTick(Consumer<MinecraftServer> consumer) {
        NeoForge.EVENT_BUS.<ServerTickEvent.Post>addListener(event -> consumer.accept(event.getServer()));
    }

    @Override
    public void registerOnClientTick(Consumer<Minecraft> consumer) {
        if (FMLEnvironment.dist.isClient()) {
            NeoClientEvents.registerClientTick(consumer);
        }
    }

    @Override
    public void registerOnWorldTick(Consumer<Level> consumer) {
        NeoForge.EVENT_BUS.<LevelTickEvent.Post>addListener(event -> {
            if (event.getLevel() instanceof ServerLevel serverLevel)
                consumer.accept(serverLevel);
        });
    }

    @Override
    public void registerOnUseItem(TriConsumer<Player, Level, InteractionHand> triConsumer) {
        NeoForge.EVENT_BUS.<PlayerInteractEvent.RightClickItem>addListener(event ->
                triConsumer.accept(event.getEntity(), event.getLevel(), event.getHand()));
    }

    @Override
    public void registerOnBlockPlace(BlockPlaced blockPlaced) {
        NeoForge.EVENT_BUS.<BlockEvent.EntityPlaceEvent>addListener(event -> {
            if (event.getEntity() instanceof LivingEntity living && event.getLevel() instanceof Level level)
                blockPlaced.onBlockPlaced(level, event.getPos(), event.getPlacedBlock(), living);
        });
    }

    @Override
    public void registerOnBlockUse(BlockUsed blockUsed) {
        NeoForge.EVENT_BUS.<PlayerInteractEvent.RightClickBlock>addListener(event ->
                blockUsed.onBlockUsed(event.getEntity(), event.getLevel(), event.getHand(),
                        event.getPos(), event.getFace()));
    }

    @Override
    public void registerOnBlockBreak(BlockBroken blockBroken) {
        NeoForge.EVENT_BUS.<BlockEvent.BreakEvent>addListener(event ->
                blockBroken.onBlockBroken(event.getLevel(), event.getPos(), event.getState(), event.getPlayer()));
    }

    @Override
    public void registerOnItemPickup(BiConsumer<Player, ItemStack> biConsumer) {
        NeoForge.EVENT_BUS.<ItemEntityPickupEvent.Post>addListener(event ->
                biConsumer.accept(event.getPlayer(), event.getOriginalStack()));
    }

    @Override
    public void registerOnLivingDeath(BiConsumer<LivingEntity, DamageSource> biConsumer) {
        NeoForge.EVENT_BUS.<LivingDeathEvent>addListener(event ->
                biConsumer.accept(event.getEntity(), event.getSource()));
    }

    @Override
    public void registerOnCrafting(BiConsumer<Player, ItemStack> biConsumer) {
        NeoForge.EVENT_BUS.<PlayerEvent.ItemCraftedEvent>addListener(event ->
                biConsumer.accept(event.getEntity(), event.getCrafting()));
    }

    @Override
    public void registerOnAnvilCrafting(BiConsumer<Player, ItemStack> biConsumer) {
        NeoForge.EVENT_BUS.<AnvilRepairEvent>addListener(event ->
                biConsumer.accept(event.getEntity(), event.getOutput()));
    }

    @Override
    public void registerOnSmelting(BiConsumer<Player, ItemStack> biConsumer) {
        NeoForge.EVENT_BUS.<PlayerEvent.ItemSmeltedEvent>addListener(event ->
                biConsumer.accept(event.getEntity(), event.getSmelting()));
    }

    @Override
    public void registerOnAdvancement(BiConsumer<ServerPlayer, Advancement> biConsumer) {
        NeoForge.EVENT_BUS.<AdvancementEvent.AdvancementEarnEvent>addListener(event -> {
            if (event.getEntity() instanceof ServerPlayer serverPlayer)
                biConsumer.accept(serverPlayer, event.getAdvancement().value());
        });
    }

    @Override
    public void registerOnAnimalTame(BiConsumer<Player, Entity> biConsumer) {
        NeoForge.EVENT_BUS.<AnimalTameEvent>addListener(event ->
                biConsumer.accept(event.getTamer(), event.getAnimal()));
    }

    @Override
    public CompoundTag getPlayerExtraTag(Player player) {
        return player.getPersistentData();
    }

    @Override
    public CreativeModeTab createTab(ResourceLocation resourceLocation, Supplier<ItemStack> supplier) {
        return null;
    }

    @Override
    public AbstractBarrelBlockEntity createBarrelBlockEntity(BlockPos pos, BlockState state) {
        return new BarrelBlockEntity(pos, state);
    }

    @Override
    public List<FluidStack> findFluidsIn(ItemStack stack) {
        IFluidHandlerItem handler = stack.getCapability(Capabilities.FluidHandler.ITEM);
        if (handler == null) return Collections.emptyList();
        List<FluidStack> fluids = new ArrayList<>();
        for (int tank = 0; tank < handler.getTanks(); tank++) {
            net.neoforged.neoforge.fluids.FluidStack fluid = handler.getFluidInTank(tank);
            if (!fluid.isEmpty())
                fluids.add(FluidStack.create(fluid.getFluid(), fluid.getAmount()));
        }
        return fluids;
    }

    @Override
    public Fraction getBucketAmount() {
        return Fraction.ofWhole(FluidType.BUCKET_VOLUME);
    }

    @Override
    public <T extends Block> Supplier<T> registerBlock(String id, Supplier<T> supplier) {
        return block.register(id, supplier);
    }

    @Override
    public Supplier<SoundEvent> registerSound(String id, Supplier<SoundEvent> supplier) {
        return sounds.register(id, supplier);
    }

    @Override
    public <T extends Item> Supplier<T> registerItem(String id, Supplier<T> supplier) {
        return item.register(id, supplier);
    }

    @Override
    public <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(
            String id, BiFunction<BlockPos, BlockState, T> constructor, Supplier<Block> validBlock) {
        return tileEntityType.register(id, () ->
                BlockEntityType.Builder.of(constructor::apply, validBlock.get()).build(null));
    }

    @Override
    public Supplier<RecipeSerializer<?>> registerBookRecipeSerializer(String id) {
        return recipe.register(id, BookCatalystRecipeSerializer::new);
    }
}
