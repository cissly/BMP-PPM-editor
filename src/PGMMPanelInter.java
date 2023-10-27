import javax.swing.*;
import java.awt.*;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class PGMMPanelInter extends PanelInter {

    private String magicNumber = null;
    private String infilepath = null;
    private Boolean isColor = null;
    public PGMMPanelInter(String temppath, Component tempCom)
    {
        super(tempCom);
        infilepath = temppath;
    }
    public PGMMPanelInter(Component tempCom)
    {
        super(tempCom);
    }
    @Override
    public boolean getIsColor() {
        return isColor;
    }

    @Override
    public int[][] turnOn()
    {
        int[][] imageData =null;
        try (DataInputStream dis = new DataInputStream(new FileInputStream(infilepath)))
        {
            magicNumber = dis.readLine();
            if(magicNumber.equals("P6"))
            {
                isColor = true;
            }

            else if(magicNumber.equals("P5")) {
                isColor = false;
            }

            else
            {
                JOptionPane.showMessageDialog(Target, "내부 형식에 문제가 있습니다.", "Message",JOptionPane.ERROR_MESSAGE );
            }

            String dimensions = dis.readLine();
            String[] dimensionTokens = dimensions.split(" ");
            int width = Integer.parseInt(dimensionTokens[0]);
            int height;
            try
            {
                height = Integer.parseInt(dimensionTokens[1]);
            }
            catch(ArrayIndexOutOfBoundsException e)
            {
                String temp = dis.readLine();
                height =  Integer.parseInt(temp);
            }
            String maxPixelValue = dis.readLine();



            // 이미지 데이터를 저장할 2차원 배열 생성
            imageData = new int[height][width];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    if (isColor == false) {
                        // 바이트를 읽어 int로 배출해내는 함수.
                        imageData[i][j] = dis.readUnsignedByte();
                    } else {
                        int r = 1000000 * dis.readUnsignedByte();
                        int g = 1000 * dis.readUnsignedByte();
                        int b = dis.readUnsignedByte();
                        imageData[i][j] = r + g + b;
                    }
                }
            }
            dis.close();

        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return imageData;
    }

    @Override
    public void saveImage(int[][] newImageData,int[][] drawData,String filePath,Boolean outColor )
    {

        if(outColor)
        {
            filePath = filePath + ".ppm";
        }
        else
        {
            filePath = filePath + ".pgm";
        }

        try(FileOutputStream fos=new FileOutputStream(filePath)){
            StringBuilder sb=new StringBuilder();

            if(outColor)
            {
                sb.append("P6\n");
            }
            else
            {
                sb.append("P5\n");
            }

            sb.append(newImageData[0].length).append(' ').append(newImageData.length).append('\n');
            sb.append("255\n");

            fos.write(sb.toString().getBytes());

            for(int i=0;i<newImageData.length;i++){
                for(int j=0;j<newImageData[i].length;j++){
                    if(!outColor)
                    {
                        byte a = (byte)(newImageData[i][j]%1000);
                        if(drawData[i][j]==255255255) { a = (byte)0; }
                        fos.write(a);
                    }
                    else
                    {
                        byte r=(byte)(newImageData[i][j]/1000000);
                        byte g=(byte)((newImageData[i][j]/1000)%1000);
                        byte b=(byte)(newImageData[i][j]%1000);

                        if(drawData[i][j]==255255255) { r = (byte)0; g = (byte)0; b = (byte)0; }

                        fos.write(r);
                        fos.write(g);
                        fos.write(b);
                    }
                }
            }
        }
        catch(IOException e)
        {
            JOptionPane.showMessageDialog(Target, "파일 생성에 실패하였습니다.", "Message",JOptionPane.ERROR_MESSAGE );
        }
    }
}
