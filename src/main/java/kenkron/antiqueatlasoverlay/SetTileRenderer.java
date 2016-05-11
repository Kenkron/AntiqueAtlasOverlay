package kenkron.antiqueatlasoverlay;

import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

/**The minimap render is a bit slow.  The function that really takes time is
 * AtlasRenderHelper.drawAutotileCorner(...).  This class makes it faster by
 * sorting the draw commands by texture, then
 * rendering all of the same textures of a map at once without re-binding.*/
public class SetTileRenderer {
	
	public class TileCorner{
		public int x, y, u, v;
		public TileCorner(int x, int y, int u, int v){
			this.x = x; this.y = y; this.u = u; this.v = v;
		}
	}
	
	public int tileHalfSize=8;
	
	HashMap<ResourceLocation, ArrayList<TileCorner>> subjects;
	
	public SetTileRenderer(int tileHalfSize){
		this.tileHalfSize=tileHalfSize;
		subjects = new HashMap<ResourceLocation, ArrayList<TileCorner>>();
	}

	public void addTileCorner(ResourceLocation texture, int x, int y, int u, int v){
		ArrayList<TileCorner> set = subjects.get(texture);
		if (set == null){
			set = new ArrayList<TileCorner>();
			subjects.put(texture, set);
		}
		set.add(new TileCorner(x, y, u, v));
	}
	
	public void draw(){
		for (ResourceLocation key: subjects.keySet()){
			ArrayList<TileCorner> tca = subjects.get(key);
			Minecraft.getMinecraft().renderEngine.bindTexture(key);
			GL11.glBegin(GL11.GL_QUADS);
			for (TileCorner tc: tca){
				drawInlineAutotileCorner(tc.x, tc.y, tc.u, tc.v);
			}
			GL11.glEnd();
		}
	}
	
	protected void drawInlineAutotileCorner(int x, int y, int u, int v) {
		//Effectively a call to GL11.glBindTexture(GL11.GL_TEXTURE_2D, p_94277_0_);
		float minU = u / 4f;
		float maxU =(u + 1) / 4f;
		float minV = v / 6f;
		float maxV =(v + 1) / 6f;
		GL11.glTexCoord2f(maxU, maxV);
		GL11.glVertex2f(x+tileHalfSize, y+ tileHalfSize);
		GL11.glTexCoord2f(maxU, minV);
		GL11.glVertex2f(x+tileHalfSize,y);
		GL11.glTexCoord2f(minU, minV);
		GL11.glVertex2f(x,y);
		GL11.glTexCoord2f(minU, maxV);
		GL11.glVertex2f(x,y+ tileHalfSize);
	}
	
	/**This does not improve framerate...*/
	public static void drawTexture(ResourceLocation texture, int x, int y, int w, int h) {
		//Effectively a call to GL11.glBindTexture(GL11.GL_TEXTURE_2D, p_94277_0_);
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2f(1, 1);
		GL11.glVertex2f(x+w, y+h);
		GL11.glTexCoord2f(1, 0);
		GL11.glVertex2f(x+w,y);
		GL11.glTexCoord2f(0, 0);
		GL11.glVertex2f(x,y);
		GL11.glTexCoord2f(0, 1);
		GL11.glVertex2f(x,y+h);
		GL11.glEnd();
	}
}
