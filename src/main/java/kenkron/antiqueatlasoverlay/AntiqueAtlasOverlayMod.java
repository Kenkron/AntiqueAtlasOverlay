package kenkron.antiqueatlasoverlay;

import net.minecraft.init.Blocks;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;

@Mod(modid = AntiqueAtlasOverlayMod.MODID, version = AntiqueAtlasOverlayMod.VERSION)
public class AntiqueAtlasOverlayMod
{
    public static final String MODID = "AntiqueAtlasOverlay";
    public static final String VERSION = "1.0";
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
		// some example code
        System.out.println("DIRT BLOCK >> "+Blocks.dirt.getUnlocalizedName());
        MinecraftForge.EVENT_BUS.register(new AAORenderEventReciever());
    }
}
