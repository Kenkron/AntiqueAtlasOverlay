package kenkron.antiqueatlasoverlay;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import cpw.mods.fml.common.eventhandler.*;

import hunternif.mc.atlas.AntiqueAtlasMod;
import hunternif.mc.atlas.api.AtlasAPI;
import hunternif.mc.atlas.client.BiomeTextureMap;
import hunternif.mc.atlas.client.SubTile;
import hunternif.mc.atlas.client.SubTileQuartet;
import hunternif.mc.atlas.client.Textures;
import hunternif.mc.atlas.client.TileRenderIterator;
import hunternif.mc.atlas.marker.Marker;
import hunternif.mc.atlas.marker.MarkerTextureMap;
import hunternif.mc.atlas.marker.MarkersData;
import hunternif.mc.atlas.util.AtlasRenderHelper;
import hunternif.mc.atlas.util.MathUtil;
import hunternif.mc.atlas.util.Rect;

public class AAORenderEventReciever {
	
		
		public static final int BOOK_WIDTH = 310;
		public static final int BOOK_HEIGHT = 218;
		
	    @SubscribeEvent(priority = cpw.mods.fml.common.eventhandler.EventPriority.NORMAL)
	    public void eventHandler(RenderGameOverlayEvent event){
	    	EntityPlayerSP player= Minecraft.getMinecraft().thePlayer;
	    	Integer atlas = getPlayerAtlas(player);
	    	int x,y,width,height;
	    	if (atlas != null){
		    	int gamewidth = event.resolution.getScaledWidth();
		    	int gameheight = event.resolution.getScaledHeight();
	    		System.out.println("I HAVE AN ATLAS!"+gamewidth+", "+gameheight);
	    		//remember, y=0 is at the top
	    		drawMinimap(gamewidth-BOOK_WIDTH/2,0,BOOK_WIDTH/2,BOOK_HEIGHT/2,0);
	    	}
	    }
	    
		public void drawMinimap(int x,int y,int width, int height, int dimension) {
			
			GL11.glColor4f(1, 1, 1, 1);
			GL11.glAlphaFunc(GL11.GL_GREATER, 0); // So light detail on tiles is visible
			AtlasRenderHelper.drawFullTexture(Textures.BOOK, x, y, width, height);
			
			
			/*
			if (stack == null || biomeData == null) return;
			
			
			if (state.is(DELETING_MARKER)) {
				GL11.glColor4f(1, 1, 1, 0.5f);
			}
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			GL11.glScissor((getGuiX() + CONTENT_X)*screenScale,
					mc.displayHeight - (getGuiY() + CONTENT_Y + MAP_HEIGHT)*screenScale,
					MAP_WIDTH*screenScale, MAP_HEIGHT*screenScale);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			// Find chunk coordinates of the top left corner of the map.
			// The 'roundToBase' is required so that when the map scales below the
			// threshold the tiles don't change when map position changes slightly.
			// The +-2 at the end provide margin so that tiles at the edges of
			// the page have their stitched texture correct.
			int mapStartX = MathUtil.roundToBase((int)Math.floor(-((double)MAP_WIDTH/2d + mapOffsetX + 2*tileHalfSize) / mapScale / 16d), tile2ChunkScale);
			int mapStartZ = MathUtil.roundToBase((int)Math.floor(-((double)MAP_HEIGHT/2d + mapOffsetY + 2*tileHalfSize) / mapScale / 16d), tile2ChunkScale);
			int mapEndX = MathUtil.roundToBase((int)Math.ceil(((double)MAP_WIDTH/2d - mapOffsetX + 2*tileHalfSize) / mapScale / 16d), tile2ChunkScale);
			int mapEndZ = MathUtil.roundToBase((int)Math.ceil(((double)MAP_HEIGHT/2d - mapOffsetY + 2*tileHalfSize) / mapScale / 16d), tile2ChunkScale);
			int mapStartScreenX = getGuiX() + WIDTH/2 + (int)((mapStartX << 4) * mapScale) + mapOffsetX;
			int mapStartScreenY = getGuiY() + HEIGHT/2 + (int)((mapStartZ << 4) * mapScale) + mapOffsetY;
			
			TileRenderIterator iter = new TileRenderIterator(biomeData);
			iter.setScope(new Rect().setOrigin(mapStartX, mapStartZ).
					set(mapStartX, mapStartZ, mapEndX, mapEndZ));
			iter.setStep(tile2ChunkScale);
			while (iter.hasNext()) {
				SubTileQuartet subtiles = iter.next();
				for (SubTile subtile : subtiles) {
					if (subtile == null || subtile.tile == null) continue;
					AtlasRenderHelper.drawAutotileCorner(
							BiomeTextureMap.instance().getTexture(subtile.tile),
							mapStartScreenX + subtile.x * tileHalfSize,
							mapStartScreenY + subtile.y * tileHalfSize,
							subtile.getTextureU(), subtile.getTextureV(), tileHalfSize);
				}
			}
			
			if (!state.is(HIDING_MARKERS)) {
				int markersStartX = MathUtil.roundToBase(mapStartX, MarkersData.CHUNK_STEP) / MarkersData.CHUNK_STEP - 1;
				int markersStartZ = MathUtil.roundToBase(mapStartZ, MarkersData.CHUNK_STEP) / MarkersData.CHUNK_STEP - 1;
				int markersEndX = MathUtil.roundToBase(mapEndX, MarkersData.CHUNK_STEP) / MarkersData.CHUNK_STEP + 1;
				int markersEndZ = MathUtil.roundToBase(mapEndZ, MarkersData.CHUNK_STEP) / MarkersData.CHUNK_STEP + 1;
				double iconScale = getIconScale();
				
				// Draw global markers:
				for (int x = markersStartX; x <= markersEndX; x++) {
					for (int z = markersStartZ; z <= markersEndZ; z++) {
						List<Marker> markers = globalMarkersData.getMarkersAtChunk(x, z);
						if (markers == null) continue;
						for (Marker marker : markers) {
							renderMarker(marker, iconScale);
						}
					}
				}
				
				// Draw local markers:
				if (localMarkersData != null) {
					for (int x = markersStartX; x <= markersEndX; x++) {
						for (int z = markersStartZ; z <= markersEndZ; z++) {
							List<Marker> markers = localMarkersData.getMarkersAtChunk(x, z);
							if (markers == null) continue;
							for (Marker marker : markers) {
								renderMarker(marker, iconScale);
							}
						}
					}
				}
			}
			
			GL11.glDisable(GL11.GL_SCISSOR_TEST);
			
			// Overlay the frame so that edges of the map are smooth:
			GL11.glColor4f(1, 1, 1, 1);
			AtlasRenderHelper.drawFullTexture(Textures.BOOK_FRAME, getGuiX(), getGuiY(), WIDTH, HEIGHT);
			double iconScale = getIconScale();
			
			// Draw player icon:
			if (!state.is(HIDING_MARKERS)) {
				// How much the player has moved from the top left corner of the map, in pixels:
				int playerOffsetX = (int)(player.posX * mapScale) + mapOffsetX;
				int playerOffsetZ = (int)(player.posZ * mapScale) + mapOffsetY;
				if (playerOffsetX < -MAP_WIDTH/2) playerOffsetX = -MAP_WIDTH/2;
				if (playerOffsetX > MAP_WIDTH/2) playerOffsetX = MAP_WIDTH/2;
				if (playerOffsetZ < -MAP_HEIGHT/2) playerOffsetZ = -MAP_HEIGHT/2;
				if (playerOffsetZ > MAP_HEIGHT/2 - 2) playerOffsetZ = MAP_HEIGHT/2 - 2;
				// Draw the icon:
				GL11.glColor4f(1, 1, 1, state.is(PLACING_MARKER) ? 0.5f : 1);
				GL11.glPushMatrix();
				GL11.glTranslated(getGuiX() + WIDTH/2 + playerOffsetX, getGuiY() + HEIGHT/2 + playerOffsetZ, 0);
				float playerRotation = (float) Math.round(player.rotationYaw / 360f * PLAYER_ROTATION_STEPS) / PLAYER_ROTATION_STEPS * 360f;
				GL11.glRotatef(180 + playerRotation, 0, 0, 1);
				GL11.glTranslated(-PLAYER_ICON_WIDTH/2*iconScale, -PLAYER_ICON_HEIGHT/2*iconScale, 0);
				AtlasRenderHelper.drawFullTexture(Textures.PLAYER, 0, 0,
						(int)Math.round(PLAYER_ICON_WIDTH*iconScale), (int)Math.round(PLAYER_ICON_HEIGHT*iconScale));
				GL11.glPopMatrix();
				GL11.glColor4f(1, 1, 1, 1);
			}
			
			// Draw buttons:
			super.drawScreen(mouseX, mouseY, par3);
			
			// Draw the semi-transparent marker attached to the cursor when placing a new marker:
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			if (state.is(PLACING_MARKER)) {
				GL11.glColor4f(1, 1, 1, 0.5f);
				AtlasRenderHelper.drawFullTexture(
						MarkerTextureMap.instance().getTexture(markerFinalizer.selectedType),
						mouseX - MARKER_SIZE/2*iconScale, mouseY - MARKER_SIZE/2*iconScale,
						(int)Math.round(MARKER_SIZE*iconScale), (int)Math.round(MARKER_SIZE*iconScale));
				GL11.glColor4f(1, 1, 1, 1);
			}
			
			// Draw progress overlay:
			if (state.is(EXPORTING_IMAGE)) {
				drawDefaultBackground();
				progressBar.draw((width - 100)/2, height/2 - 34);
			}*/
		}
	    
		/** Convenience method that returns the first atlas ID for all atlas items
		 * the player is currently carrying. Returns null if there are none.**/
		public static Integer getPlayerAtlas(EntityPlayer player) {
			for (ItemStack stack : player.inventory.mainInventory) {
				if (stack != null && stack.getItem() == AntiqueAtlasMod.itemAtlas) {
					return new Integer(stack.getItemDamage());
				}
			}
			return null;
		}
}
