import java.awt.*;

public abstract class PanelInter {
    protected boolean isColor;
    protected Component Target= null;

    public PanelInter(Component temp)
    {
        Target = temp;
    }
    abstract public boolean getIsColor();

    abstract  public int[][] turnOn();
    abstract public void saveImage(int[][] newImageData,int[][] drawData,String filePath,Boolean outColor);
}
