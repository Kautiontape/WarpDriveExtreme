package edu.umbc.teamawesome.warpdriveextreme;

import edu.umbc.teamawesome.warpdriveextreme.GamePhysics.Rect;
import android.graphics.Bitmap;
import android.graphics.Canvas;

public class ExplosionAnimated {
	
    private static final int BMP_COLUMNS = 17;
    private Bitmap bmp;
    private int currentFrame = 0;
    private int width;
    private int height;
    private Rect shipBox;

    public ExplosionAnimated(Bitmap bmp, Rect shipBox) 
    {
          this.bmp = bmp;
          this.width = bmp.getWidth() / BMP_COLUMNS;
          this.height = bmp.getHeight();
          this.shipBox = shipBox;
    }

    public boolean isFinished()
    {
    	return (currentFrame >= BMP_COLUMNS);
    }
    
    private void update() 
    {
          currentFrame += 1;
    }

    public void draw(Canvas canvas) 
    {
          update();
          int srcX = currentFrame * width;
          int srcY = 0;
          android.graphics.Rect src = new android.graphics.Rect(srcX, srcY, srcX + width, srcY + height);
          android.graphics.Rect dst = new android.graphics.Rect((int)shipBox.left(), (int)shipBox.top(), 
        		  (int)shipBox.left() + width, (int)shipBox.top() + height);
          canvas.drawBitmap(bmp, src, dst, null);
    }
}
