package kenkron.antiqueatlasoverlay;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
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
	 * to take. Fraction of image devoted to each border.
	 */
	public float BORDER_X = 0.0625f, BORDER_Y = 0.0625f;
	public int TILE_SIZE = 8;

	/** Position of the minimap relative to it's corner. */
	public int X = 2, Y = 2;

	/** Dimensions of the minimap */
	public int WIDTH = GuiAtlas.WIDTH / 2, HEIGHT = GuiAtlas.HEIGHT / 2;

	/** Determines which corner to align to*/
	public boolean ALIGN_RIGHT = true, ALIGN_BOTTOM = false;

	/** If true, the minimap will render only while the atlas is held,
	 * instead of rendering whenever it's in the hotbar.*/
	public boolean REQUIRES_HOLD = true;
	
	@SubscribeEvent(priority = cpw.mods.fml.common.eventhandler.EventPriority.NORMAL)
	public void eventHandler(RenderGameOverlayEvent event) {
		EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
		Integer atlas = null;
		if (REQUIRES_HOLD){
			ItemStack stack = player.getHeldItem();
			if (stack != null && stack.getItem() == AntiqueAtlasMod.itemAtlas) {
				atlas = new Integer(stack.getItemDamage());
			}
		}else{
			atlas = getPlayerAtlas(player);
		}
		if (atlas != null) {
			int gamewidth = event.resolution.getScaledWidth();
			int gameheight = event.resolution.getScaledHeight();
			// remember, y=0 is at the top
			Rect bounds = new Rect().setOrigin(X, Y);
			if (ALIGN_RIGHT) {
				bounds.minX = gamewidth - (WIDTH + X);
			}
			if (ALIGN_BOTTOM) {
				bounds.minY = gameheight - (HEIGHT + Y);
			}
			bounds.setSize(WIDTH, HEIGHT);
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
		Rect innerShape = new Rect(
				// stop it eclipse
				shape.minX + Math.round(BORDER_X * shape.getWidth()),
				shape.minY + Math.round(BORDER_Y * shape.getHeight()),
				shape.maxX - Math.round(BORDER_X * shape.getWidth()),
				shape.maxY - Math.round(BORDER_Y * shape.getHeight()));
		drawTiles(innerShape, atlasID, position, dimension, res);
	}

	public void drawTiles(Rect shape, int atlasID, Vec3 position,
			int dimension, ScaledResolution res) {
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		// glScissor uses the default window coordinates,
		// the display window does not. We need to fix this
		int mcHeight = Minecraft.getMinecraft().displayHeight;
		float scissorScaleX = Minecraft.getMinecraft().displayWidth * 1.0f
				/ res.getScaledWidth();
		float scissorScaleY = mcHeight * 1.0f / res.getScaledHeight();
		GL11.glScissor((int) (shape.minX * scissorScaleX),
				(int) (mcHeight - shape.maxY * scissorScaleY),
				(int) (shape.getWidth() * scissorScaleX),
				(int) (shape.getHeight() * scissorScaleY));
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		DimensionData biomeData = AntiqueAtlasMod.atlasData.getAtlasData(
				atlasID, Minecraft.getMinecraft().theWorld).getDimensionData(
				dimension);

		int CHUNKSIZE = 16;

		TileRenderIterator iter = new TileRenderIterator(biomeData);
		int minChunkX = (int) Math.floor(position.xCoord / CHUNKSIZE
				- shape.getWidth() / (2 * TILE_SIZE));
		minChunkX -= 1;// IDK
		int minChunkY = (int) Math.floor(position.zCoord / CHUNKSIZE
				- shape.getHeight() / (2 * TILE_SIZE));
		minChunkY -= 1;// IDK
		int maxChunkX = (int) Math.ceil(position.xCoord / CHUNKSIZE
				+ shape.getWidth() / (2 * TILE_SIZE));
		int maxChunkY = (int) Math.ceil(position.zCoord / CHUNKSIZE
				+ shape.getHeight() / (2 * TILE_SIZE));
		Rect iteratorScope = new Rect(minChunkX, minChunkY, maxChunkX,
				maxChunkY);
		iter.setScope(iteratorScope);

		iter.setStep(1);
		Vec3 chunkPosition = Vec3.createVectorHelper(position.xCoord
				/ CHUNKSIZE, position.yCoord / CHUNKSIZE, position.zCoord
				/ CHUNKSIZE);
		int shapeMiddleX = (shape.minX + shape.maxX) / 2;
		int shapeMiddleY = (shape.minY + shape.maxY) / 2;
		SetTileRenderer renderer = new SetTileRenderer(TILE_SIZE / 2);

		while (iter.hasNext()) {
			SubTileQuartet subtiles = iter.next();
			for (SubTile subtile : subtiles) {
				if (subtile == null || subtile.tile == null)
					continue;
				// Position of this subtile (measured in chunks) relative to the
				// player
				float relativeChunkPositionX = (float) (subtile.x / 2.0
						+ iteratorScope.minX - chunkPosition.xCoord);
				float relativeChunkPositionY = (float) (subtile.y / 2.0
						+ iteratorScope.minY - chunkPosition.zCoord);
				// TODO: this is slow
				renderer.addTileCorner(
						BiomeTextureMap.instance().getTexture(subtile.tile),
						shapeMiddleX
								+ (int) Math.round(relativeChunkPositionX
										* TILE_SIZE),
						shapeMiddleY
								+ (int) Math.round(relativeChunkPositionY
										* TILE_SIZE), subtile.getTextureU(),
						subtile.getTextureV());/**/
			}
		}
		renderer.draw();
		// get GL back to normal
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
		GL11.glColor4f(1, 1, 1, 1);
	}

	public void drawMarkers(Rect shape, int atlasID, Vec3 position,
			int dimension, ScaledResolution res) {
		/*
		 * if (!state.is(HIDING_MARKERS)) { int markersStartX =
		 * MathUtil.roundToBase(mapStartX, MarkersData.CHUNK_STEP) /
		 * MarkersData.CHUNK_STEP - 1; int markersStartZ =
		 * MathUtil.roundToBase(mapStartZ, MarkersData.CHUNK_STEP) /
		 * MarkersData.CHUNK_STEP - 1; int markersEndX =
		 * MathUtil.roundToBase(mapEndX, MarkersData.CHUNK_STEP) /
		 * MarkersData.CHUNK_STEP + 1; int markersEndZ =
		 * MathUtil.roundToBase(mapEndZ, MarkersData.CHUNK_STEP) /
		 * MarkersData.CHUNK_STEP + 1; double iconScale = getIconScale();
		 * 
		 * // Draw global markers: for (int x = markersStartX; x <= markersEndX;
		 * x++) { for (int z = markersStartZ; z <= markersEndZ; z++) {
		 * List<Marker> markers = globalMarkersData.getMarkersAtChunk( x, z); if
		 * (markers == null) continue; for (Marker marker : markers) {
		 * renderMarker(marker, iconScale); } } }
		 * 
		 * // Draw local markers: if (localMarkersData != null) { for (int x =
		 * markersStartX; x <= markersEndX; x++) { for (int z = markersStartZ; z
		 * <= markersEndZ; z++) { List<Marker> markers = localMarkersData
		 * .getMarkersAtChunk(x, z); if (markers == null) continue; for (Marker
		 * marker : markers) { renderMarker(marker, iconScale); } } } } }
		 * 
		 * GL11.glDisable(GL11.GL_SCISSOR_TEST);
		 * 
		 * // Overlay the frame so that edges of the map are smooth:
		 * GL11.glColor4f(1, 1, 1, 1);
		 * AtlasRenderHelper.drawFullTexture(Textures.BOOK_FRAME, getGuiX(),
		 * getGuiY(), WIDTH, HEIGHT); double iconScale = getIconScale();
		 * 
		 * // Draw player icon: if (!state.is(HIDING_MARKERS)) { // How much the
		 * player has moved from the top left corner of the // map, in pixels:
		 * int playerOffsetX = (int) (player.posX * mapScale) + mapOffsetX; int
		 * playerOffsetZ = (int) (player.posZ * mapScale) + mapOffsetY; if
		 * (playerOffsetX < -MAP_WIDTH / 2) playerOffsetX = -MAP_WIDTH / 2; if
		 * (playerOffsetX > MAP_WIDTH / 2) playerOffsetX = MAP_WIDTH / 2; if
		 * (playerOffsetZ < -MAP_HEIGHT / 2) playerOffsetZ = -MAP_HEIGHT / 2; if
		 * (playerOffsetZ > MAP_HEIGHT / 2 - 2) playerOffsetZ = MAP_HEIGHT / 2 -
		 * 2; // Draw the icon: GL11.glColor4f(1, 1, 1, state.is(PLACING_MARKER)
		 * ? 0.5f : 1); GL11.glPushMatrix(); GL11.glTranslated(getGuiX() + WIDTH
		 * / 2 + playerOffsetX, getGuiY() + HEIGHT / 2 + playerOffsetZ, 0);
		 * float playerRotation = (float) Math.round(player.rotationYaw / 360f
		 * PLAYER_ROTATION_STEPS) / PLAYER_ROTATION_STEPS * 360f;
		 * GL11.glRotatef(180 + playerRotation, 0, 0, 1);
		 * GL11.glTranslated(-PLAYER_ICON_WIDTH / 2 * iconScale,
		 * -PLAYER_ICON_HEIGHT / 2 * iconScale, 0);
		 * AtlasRenderHelper.drawFullTexture(Textures.PLAYER, 0, 0, (int)
		 * Math.round(PLAYER_ICON_WIDTH * iconScale), (int)
		 * Math.round(PLAYER_ICON_HEIGHT * iconScale)); GL11.glPopMatrix();
		 * GL11.glColor4f(1, 1, 1, 1); }
		 * 
		 * // Draw buttons: super.drawScreen(mouseX, mouseY, par3);
		 * 
		 * // Draw the semi-transparent marker attached to the cursor when
		 * placing // a new marker: GL11.glEnable(GL11.GL_BLEND);
		 * GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA); if
		 * (state.is(PLACING_MARKER)) { GL11.glColor4f(1, 1, 1, 0.5f);
		 * AtlasRenderHelper.drawFullTexture(MarkerTextureMap.instance()
		 * .getTexture(markerFinalizer.selectedType), mouseX - MARKER_SIZE / 2 *
		 * iconScale, mouseY - MARKER_SIZE / 2 iconScale, (int)
		 * Math.round(MARKER_SIZE * iconScale), (int) Math.round(MARKER_SIZE *
		 * iconScale)); GL11.glColor4f(1, 1, 1, 1); }
		 * 
		 * // Draw progress overlay: if (state.is(EXPORTING_IMAGE)) {
		 * drawDefaultBackground(); progressBar.draw((width - 100) / 2, height /
		 * 2 - 34); }
		 */
	}

	/**
	 * Convenience method that returns the first atlas ID for all atlas items
	 * the player is currently carrying in the hotbar. Returns null if there are
	 * none.
	 **/
	public static Integer getPlayerAtlas(EntityPlayer player) {
		for (int i = 0; i < 9; i++) {
			ItemStack stack = player.inventory.mainInventory[i];
			if (stack != null && stack.getItem() == AntiqueAtlasMod.itemAtlas) {
				return new Integer(stack.getItemDamage());
			}
		}
		return null;
	}
}
