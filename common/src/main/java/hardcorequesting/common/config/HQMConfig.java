package hardcorequesting.common.config;

import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.client.KeyboardHandler;
import hardcorequesting.common.io.SaveHandler;
import hardcorequesting.common.items.BagItem;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.team.RewardSetting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class HQMConfig {
    private static transient HQMConfig instance;
    
    // Settings related to hardcore mode
    public Hardcore Hardcore = new Hardcore();
    // Settings related to server start & modes
    public Starting Starting = new Starting();
    // Settings related to loot bags and loot tiers
    public Loot Loot = new Loot();
    // Settings related to the interface
    public Interface Interface = new Interface();

    // Enable this to cause the player to be granted a gift on spawning into the world
    public boolean SPAWN_BOOK = false;

    // Loose the quest book when you die, if set to false it will stay in your inventory
    public boolean LOSE_QUEST_BOOK = true;

    // Allow every single player in a party to claim the reward for a quest. Setting this to false will give the party one set of rewards to share.
    public boolean MULTI_REWARD = true;

    // Allow teams, currently extremely buggy!
    public boolean ENABLE_TEAMS = true;

    // Use this to specify NBT tags that should be ignored when comparing items with NBT subset
    public String[] NBT_SUBSET_FILTER = new String[]{"RepairCost"};

    // Settings related to messages sent from the server
    public Message Message = new Message();
    // Settings related to edit mode
    public Editing Editing = new Editing();
    
    public static int OVERLAY_XPOS;
    
    public static int OVERLAY_YPOS;
    
    public static int OVERLAY_XPOSDEFAULT = 2;
    
    public static int OVERLAY_YPOSDEFAULT = 2;
    
    public static int CURRENTLY_MODIFYING_QUEST_SET = 0x4040dd;
    
    public static int COMPLETED_SELECTED_IN_BOUNDS_SET = 0x40bb40;
    
    public static int COMPLETED_SELECTED_OUT_OF_BOUNDS_SET = 0x40a040;
    
    public static int COMPLETED_UNSELECTED_IN_BOUNDS_SET = 0x10a010;
    
    public static int COMPLETED_UNSELECTED_OUT_OF_BOUNDS_SET = 0x107010;
    
    public static int UNCOMPLETED_SELECTED_IN_BOUNDS_SET = 0xaaaaaa;
    
    public static int UNCOMPLETED_SELECTED_OUT_OF_BOUNDS_SET = 0x888888;
    
    public static int UNCOMPLETED_UNSELECTED_IN_BOUNDS_SET = 0x666666;
    
    public static int UNCOMPLETED_UNSELECTED_OUT_OF_BOUNDS_SET = 0x404040;
    
    public static int DISABLED_SET = 0xdddddd;
    
    public static int QUEST_INVISIBLE = 0x55FFFFFF;
    
    public static int QUEST_DISABLED = 0xFF888888;
    
    public static int QUEST_COMPLETE = 0xFFFFFFFF;
    
    public static int QUEST_COMPLETE_REPEATABLE = 0xFFFFFFCC;
    
    public static int QUEST_AVAILABLE = 0x554286f4;
    
    public static HQMConfig getInstance() {
        if (instance == null) {
            Path path = HardcoreQuestingCore.configDir.resolve("config.json");
            try {
                if (!Files.exists(path.getParent()))
                    Files.createDirectories(path.getParent());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            instance = SaveHandler.load(path, HQMConfig.class).orElse(new HQMConfig());
            SaveHandler.save(path, SaveHandler.GSON.toJson(instance));
        }
        return instance;
    }
    
    public static void parseSetColours() {
        try {
            COMPLETED_SELECTED_IN_BOUNDS_SET = Long.decode(getInstance().Interface.QuestSets.COMPLETED_SELECTED_IN_BOUNDS_SET.toLowerCase()).intValue();
            COMPLETED_UNSELECTED_IN_BOUNDS_SET = Long.decode(getInstance().Interface.QuestSets.COMPLETED_UNSELECTED_IN_BOUNDS_SET.toLowerCase()).intValue();
            UNCOMPLETED_SELECTED_IN_BOUNDS_SET = Long.decode(getInstance().Interface.QuestSets.UNCOMPLETED_SELECTED_IN_BOUNDS_SET.toLowerCase()).intValue();
            UNCOMPLETED_UNSELECTED_IN_BOUNDS_SET = Long.decode(getInstance().Interface.QuestSets.UNCOMPLETED_UNSELECTED_IN_BOUNDS_SET.toLowerCase()).intValue();
            DISABLED_SET = Long.decode(getInstance().Interface.QuestSets.DISABLED_SET.toLowerCase()).intValue();
        } catch (NumberFormatException e) {
            HardcoreQuestingCore.LOGGER.error("Unable to parse set colours", e);
        }
    }
    
    public static void parseQuestColours() {
        try {
            QUEST_INVISIBLE = Long.decode(getInstance().Interface.Quests.QUEST_INVISIBLE.toLowerCase()).intValue();
            QUEST_DISABLED = Long.decode(getInstance().Interface.Quests.QUEST_DISABLED.toLowerCase()).intValue();
            QUEST_COMPLETE = Long.decode(getInstance().Interface.Quests.QUEST_COMPLETE.toLowerCase()).intValue();
            QUEST_COMPLETE_REPEATABLE = Long.decode(getInstance().Interface.Quests.QUEST_COMPLETE_REPEATABLE.toLowerCase()).intValue();
            QUEST_AVAILABLE = Long.decode(getInstance().Interface.Quests.QUEST_AVAILABLE.toLowerCase()).intValue();
        } catch (NumberFormatException e) {
            HardcoreQuestingCore.LOGGER.error("Unable to parse quest colours", e);
        }
    }
    
    @SuppressWarnings("deprecation")
    public static void loadConfig() {
        parseSetColours();
        parseQuestColours();
        
        RewardSetting.isAllModeEnabled = getInstance().MULTI_REWARD;
        BagItem.displayGui = getInstance().Loot.REWARD_INTERFACE;
        
        Quest.isEditing = getInstance().Editing.USE_EDITOR;
        if (HardcoreQuestingCore.proxy.isClient()) {
            KeyboardHandler.clear();
            KeyboardHandler.initDefault();
        }
    }
    
    public static class Hardcore {
        // How many lives players should start with.
        public int DEFAULT_LIVES = 3;

        // Define in seconds how long the rot timer is.
        public int HEART_ROT_TIME = 120;

        // Set to true to enable the heart rot timer
        public boolean HEART_ROT_ENABLE = false;

        // Use this to set the maximum lives obtainable
        public int MAX_LIVES = 20;
    }
    
    public static class Starting {
        // If set to true, new worlds will automatically activate Hardcore mode
        public boolean AUTO_HARDCORE = false;
        // If set to true, new worlds will automatically activate Questing mode
        public boolean AUTO_QUESTING = true;
    }
    
    public static class Loot {
        // Always display the tier name, instead of the individual bag's name, when opening a reward bag.
        public boolean ALWAYS_USE_TIER = false;
        // Set to true to display an interface with the contents of the reward bag when you open it.
        public boolean REWARD_INTERFACE = true;
    }
    
    public static class Message {
        // Set to true to enable sending a status message if Hardcore Questing mode is off
        public boolean NO_HARDCORE_MESSAGE = false;

        // Set to false to prevent the 'use /hqm op instead' message when operators use '/hqm edit' instead.
        public boolean OP_REMINDER = true;
    }
    
    public static class Editing {
        // Set to true to automatically enable edit mode when entering worlds in single-player. Has no effect in multiplayer.
        public boolean USE_EDITOR = false;
    }
    
    public static class Interface {
        // Colour settings for quest set rendering
        public QuestSets QuestSets = new QuestSets();
        // Colour settings for quests
        public Quests Quests = new Quests();

        public static class QuestSets {
            // Use the HTML format, e.g.: #ffffff
            public String COMPLETED_SELECTED_IN_BOUNDS_SET = "#40bb40";
            // Use the HTML format, e.g.: #ffffff
            public String COMPLETED_UNSELECTED_IN_BOUNDS_SET = "#10a010";
            // Use the HTML format, e.g.: #ffffff
            public String UNCOMPLETED_SELECTED_IN_BOUNDS_SET = "#aaaaaa";
            // Use the HTML format, e.g.: #ffffff
            public String UNCOMPLETED_UNSELECTED_IN_BOUNDS_SET = "#666666";
            // Use the HTML format, e.g.: #ffffff
            public String DISABLED_SET = "#dddddd";
        }

        public static class Quests {
            public String QUEST_INVISIBLE = "#55FFFFFF";
            // Use the HTML format with alpha, e.g.: #55FFFFFF
            public String QUEST_DISABLED = "#FF888888";
            // Use the HTML format with alpha, e.g.: #55FFFFFF
            public String QUEST_COMPLETE = "#FFFFFFFF";
            // Use the HTML format with alpha, e.g.: #55FFFFFF
            public String QUEST_COMPLETE_REPEATABLE = "#FFFFFFCC";
            // Use the HTML format with alpha, e.g: #55FFFFFF
            public String QUEST_AVAILABLE = "#554286f4";
            // Set to true to disable the default colour pulse and use the colour specified above
            public boolean SINGLE_COLOUR = false;
        }
    }
}
