package kenkron.antiqueatlasoverlay;

import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import hunternif.mc.atlas.util.AtlasRenderHelper;

/**The minimap render is a bit slow.  The function that really makes a difference is
 * AtlasRenderHelper.drawAutotileCorner(...).  This will try to make it faster by
 * rendering all of the same texture of a map without rebinding.*/
public class OptimizedTileRenderer {
	
	public class TileCorner{
		public int x, y, u, v, tileHalfSize;
		public TileCorner(int x, int y, int u, int v, int tileHalfSize){
			this.x = x; this.y = y; this.u = y; this.v = v;
			this.tileHalfSize = tileHalfSize;
		}
	}
	
	HashMap<String, ArrayList<TileCorner>> subjects;
	
	public OptimizedTileRenderer(){
		
	}

	/**This does not improve framerate...*/
	public static void drawIndividualAutotileCorner(ResourceLocation texture, int x, int y, int u, int v, int tileHalfSize) {
		//Effectively a call to GL11.glBindTexture(GL11.GL_TEXTURE_2D, p_94277_0_);
		//that uses a string and has no 
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);
		float minU = u / 4f;
		float maxU =(u + 1) / 4f;
		float minV = v / 6f;
		float maxV =(v + 1) / 6f;
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2f(maxU, maxV);
		GL11.glVertex2f(x+tileHalfSize, y+ tileHalfSize);
		GL11.glTexCoord2f(maxU, minV);
		GL11.glVertex2f(x+tileHalfSize,y);
		GL11.glTexCoord2f(minU, minV);
		GL11.glVertex2f(x,y);
		GL11.glTexCoord2f(minU, maxV);
		GL11.glVertex2f(x,y+ tileHalfSize);
		GL11.glEnd();
	}
}
