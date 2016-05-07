package kenkron.antiqueatlasoverlay;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
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
import hunternif.mc.atlas.client.gui.GuiAtlas;
import hunternif.mc.atlas.core.DimensionData;
import hunternif.mc.atlas.marker.Marker;
import hunternif.mc.atlas.marker.MarkerTextureMap;
import hunternif.mc.atlas.marker.MarkersData;
import hunternif.mc.atlas.util.AtlasRenderHelper;
import hunternif.mc.atlas.util.MathUtil;
import hunternif.mc.atlas.util.Rect;

public class AAORenderEventReciever {

	/**
	 * I know public variables can be messed with, but that's a risk I'm willing
	 * to take
	 */
	public static int BORDER_X = 8, BORDER_Y = 6;

	public static final int TILE_SIZE = 16;
	public static final int HALF_TILE_SIZE = TILE_SIZE/2;
	
	@SubscribeEvent(priority = cpw.mods.fml.common.eventhandler.EventPriority.NORMAL)
	public void eventHandler(RenderGameOverlayEvent event) {
		EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
		Integer atlas = getPlayerAtlas(player);
		//Atlas must be in the hotbar
		if (atlas != null) {
			int gamewidth = event.resolution.getScaledWidth();
			int gameheight = event.resolution.getScaledHeight();
			// the inherant size of the map is a bit too large
			int width = GuiAtlas.WIDTH / 2;
			int height = GuiAtlas.HEIGHT / 2;
			// remember, y=0 is at the top
			Rect bounds = new Rect().setOrigin(gamewidth - width, 0).setSize(
					width, height);
			drawMinimap(bounds, atlas.intValue(), player.getPosition(1),
					player.dimension, event.resolution);
		}
	}

	public void drawMinimap(Rect shape, int atlasID, Vec3 position,
			int dimension, ScaledResolution res) {
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0); // So light detail on tiles is
												// visible
		AtlasRenderHelper.drawFullTexture(Textures.BOOK, shape.minX,
				shape.minY, shape.getWidth(), shape.getHeight());
		Rect innerShape = new Rect(shape.minX + BORDER_X,
				shape.minY + BORDER_Y, shape.maxX - BORDER_X, shape.maxY
						- BORDER_Y);
		drawTiles(innerShape, atlasID, position, dimension, res);
	}

	public void drawTiles(Rect shape, int atlasID,
			Vec3 position, int dimension, ScaledResolution res) {
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		//glScissor uses the default window coordinates,
		//the display window does not.  We need to fix this
		int mcHeight = Minecraft.getMinecraft().displayHeight;
		float scissorScaleX = Minecraft.getMinecraft().displayWidth*1.0f/res.getScaledWidth();
		float scissorScaleY = mcHeight*1.0f/res.getScaledHeight();
		GL11.glScissor((int)(shape.minX*scissorScaleX), (int)(mcHeight-shape.maxY*scissorScaleY), (int)(shape.getWidth()*scissorScaleX), (int)(shape.getHeight()*scissorScaleY));
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		DimensionData biomeData = AntiqueAtlasMod.atlasData.getAtlasData(
				atlasID, Minecraft.getMinecraft().theWorld).getDimensionData(
				dimension);

		int CHUNKSIZE = 16;
		
		TileRenderIterator iter = new TileRenderIterator(biomeData);
		Rect iteratorScope = new Rect((int)position.xCoord/CHUNKSIZE-6,(int)position.zCoord/CHUNKSIZE-3,(int)position.xCoord/CHUNKSIZE+4,(int)position.zCoord/CHUNKSIZE+3);
		iter.setScope(iteratorScope);
		
		iter.setStep(1);
		Vec3 chunkPosition = Vec3.createVectorHelper(position.xCoord/CHUNKSIZE,position.yCoord/CHUNKSIZE,position.zCoord/CHUNKSIZE);
		int shapeMiddleX = (shape.minX+shape.maxX)/2;
		int shapeMiddleY = (shape.minY+shape.maxY)/2;
		while (iter.hasNext()) {
			SubTileQuartet subtiles = iter.next();
			for (SubTile subtile : subtiles) {
				if (subtile == null || subtile.tile == null)
					continue;
				//Position of this subtile (measured in chunks) relative to the player
				float relativeChunkPositionX = (float) (subtile.x/2.0+iteratorScope.minX-chunkPosition.xCoord);
				float relativeChunkPositionY = (float) (subtile.y/2.0+iteratorScope.minY-chunkPosition.zCoord);
				AtlasRenderHelper.drawAutotileCorner(BiomeTextureMap.instance()
						.getTexture(subtile.tile), 
						shapeMiddleX+(int)(relativeChunkPositionX*TILE_SIZE),
						shapeMiddleY+(int)(relativeChunkPositionY*TILE_SIZE),
						subtile.getTextureU(), subtile
						.getTextureV(), TILE_SIZE/2);
			}
		}

		//get GL back to normal
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
		GL11.glColor4f(1, 1, 1, 1);
		/*if (!state.is(HIDING_MARKERS)) {
			int markersStartX = MathUtil.roundToBase(mapStartX,
					MarkersData.CHUNK_STEP) / MarkersData.CHUNK_STEP - 1;
			int markersStartZ = MathUtil.roundToBase(mapStartZ,
					MarkersData.CHUNK_STEP) / MarkersData.CHUNK_STEP - 1;
			int markersEndX = MathUtil.roundToBase(mapEndX,
					MarkersData.CHUNK_STEP) / MarkersData.CHUNK_STEP + 1;
			int markersEndZ = MathUtil.roundToBase(mapEndZ,
					MarkersData.CHUNK_STEP) / MarkersData.CHUNK_STEP + 1;
			double iconScale = getIconScale();

			// Draw global markers:
			for (int x = markersStartX; x <= markersEndX; x++) {
				for (int z = markersStartZ; z <= markersEndZ; z++) {
					List<Marker> markers = globalMarkersData.getMarkersAtChunk(
							x, z);
					if (markers == null)
						continue;
					for (Marker marker : markers) {
						renderMarker(marker, iconScale);
					}
				}
			}

			// Draw local markers:
			if (localMarkersData != null) {
				for (int x = markersStartX; x <= markersEndX; x++) {
					for (int z = markersStartZ; z <= markersEndZ; z++) {
						List<Marker> markers = localMarkersData
								.getMarkersAtChunk(x, z);
						if (markers == null)
							continue;
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
		AtlasRenderHelper.drawFullTexture(Textures.BOOK_FRAME, getGuiX(),
				getGuiY(), WIDTH, HEIGHT);
		double iconScale = getIconScale();

		// Draw player icon:
		if (!state.is(HIDING_MARKERS)) {
			// How much the player has moved from the top left corner of the
			// map, in pixels:
			int playerOffsetX = (int) (player.posX * mapScale) + mapOffsetX;
			int playerOffsetZ = (int) (player.posZ * mapScale) + mapOffsetY;
			if (playerOffsetX < -MAP_WIDTH / 2)
				playerOffsetX = -MAP_WIDTH / 2;
			if (playerOffsetX > MAP_WIDTH / 2)
				playerOffsetX = MAP_WIDTH / 2;
			if (playerOffsetZ < -MAP_HEIGHT / 2)
				playerOffsetZ = -MAP_HEIGHT / 2;
			if (playerOffsetZ > MAP_HEIGHT / 2 - 2)
				playerOffsetZ = MAP_HEIGHT / 2 - 2;
			// Draw the icon:
			GL11.glColor4f(1, 1, 1, state.is(PLACING_MARKER) ? 0.5f : 1);
			GL11.glPushMatrix();
			GL11.glTranslated(getGuiX() + WIDTH / 2 + playerOffsetX, getGuiY()
					+ HEIGHT / 2 + playerOffsetZ, 0);
			float playerRotation = (float) Math.round(player.rotationYaw / 360f
					* PLAYER_ROTATION_STEPS)
					/ PLAYER_ROTATION_STEPS * 360f;
			GL11.glRotatef(180 + playerRotation, 0, 0, 1);
			GL11.glTranslated(-PLAYER_ICON_WIDTH / 2 * iconScale,
					-PLAYER_ICON_HEIGHT / 2 * iconScale, 0);
			AtlasRenderHelper.drawFullTexture(Textures.PLAYER, 0, 0,
					(int) Math.round(PLAYER_ICON_WIDTH * iconScale),
					(int) Math.round(PLAYER_ICON_HEIGHT * iconScale));
			GL11.glPopMatrix();
			GL11.glColor4f(1, 1, 1, 1);
		}

		// Draw buttons:
		super.drawScreen(mouseX, mouseY, par3);

		// Draw the semi-transparent marker attached to the cursor when placing
		// a new marker:
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		if (state.is(PLACING_MARKER)) {
			GL11.glColor4f(1, 1, 1, 0.5f);
			AtlasRenderHelper.drawFullTexture(MarkerTextureMap.instance()
					.getTexture(markerFinalizer.selectedType), mouseX
					- MARKER_SIZE / 2 * iconScale, mouseY - MARKER_SIZE / 2
					* iconScale, (int) Math.round(MARKER_SIZE * iconScale),
					(int) Math.round(MARKER_SIZE * iconScale));
			GL11.glColor4f(1, 1, 1, 1);
		}

		// Draw progress overlay:
		if (state.is(EXPORTING_IMAGE)) {
			drawDefaultBackground();
			progressBar.draw((width - 100) / 2, height / 2 - 34);
		}*/
	}

	/**
	 * Convenience method that returns the first atlas ID for all atlas items
	 * the player is currently carrying in the hotbar. Returns null if there are none.
	 **/
	public static Integer getPlayerAtlas(EntityPlayer player) {
		for (int i = 0; i<9; i++) {
			ItemStack stack = player.inventory.mainInventory[i];
			if (stack != null && stack.getItem() == AntiqueAtlasMod.itemAtlas) {
				return new Integer(stack.getItemDamage());
			}
		}
		return null;
	}
}
