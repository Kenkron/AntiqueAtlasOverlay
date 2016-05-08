package kenkron.antiqueatlasoverlay;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

public class OptimisticTileRenderer {

	public ResourceLocation currentTexture;
	
	public OptimisticTileRenderer(){
	}
	
	/**A replacement for AtlasRenderHelper.drawAutotileCorner.
	 * If currentTexture is not null, this method assumes that there have been no new
	 * texture bindings since the last call. Thus, if texture is the same as last time,
	 * it will not waste time binding the texture again.*/
	public void drawAutotileCorner(ResourceLocation texture, int x, int y, int u, int v, int tileHalfSize) {
		//Effectively a call to GL11.glBindTexture(GL11.GL_TEXTURE_2D, p_94277_0_);
		if (!texture.equals(currentTexture)){
			GL11.glEnd();
			Minecraft.getMinecraft().renderEngine.bindTexture(texture);
			currentTexture = texture;
			GL11.glBegin(GL11.GL_QUADS);
		}else{
			System.out.print(".");
		}
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
}
