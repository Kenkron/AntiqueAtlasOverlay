package kenkron.antiqueatlasoverlay;

import java.io.File;

import net.minecraft.init.Blocks;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = AntiqueAtlasOverlayMod.MODID, version = AntiqueAtlasOverlayMod.VERSION, name = AntiqueAtlasOverlayMod.MODID, dependencies = "required-after:antiqueatlas")
public class AntiqueAtlasOverlayMod
{
    public static final String MODID = "AntiqueAtlasOverlay";
    public static final String VERSION = "1.0";
    
    AAORenderEventReciever renderer;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event){
    	renderer = new AAORenderEventReciever();
    	AAOConfig.load(new File(event.getModConfigurationDirectory(), "AntiqueAtlasOverlay.cfg"), renderer);
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(renderer);
    }
}
