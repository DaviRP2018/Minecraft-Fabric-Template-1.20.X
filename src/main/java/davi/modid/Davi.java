package davi.modid;

import davi.modid.config.DaviConfig;
import davi.modid.item.ModItemGroups;
import davi.modid.item.ModItems;
import mod.azure.azurelib.AzureLibMod;
import mod.azure.azurelib.config.format.ConfigFormats;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Davi implements ModInitializer {

	public static final String MOD_ID = "davi";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static DaviConfig config;

	@Override
	public void onInitialize() {
		config = AzureLibMod.registerConfig(DaviConfig.class, ConfigFormats.json()).getConfigInstance();
		ModItemGroups.registerItemGroups();
		ModItems.registerModItems();
	}
}