import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;


class BMPPanelInterTest {
    private  String infilepath = "image/lena_8bit_color.bmp";
    private int[][] data ;
    ArrayList<Integer> buffer = new ArrayList<Integer>();
    @Test
    void getIsColor() {
    }

    int[][] turnOn() {
        int[][] imageData;
        try (FileInputStream fis = new FileInputStream(infilepath)) {
            // BMP 파일 헤더 읽기 54바이트
            byte[] header = new byte[54];
            fis.read(header);

            // 매직 넘버를 문자열로 변환합니다.
            byte[] temp = Arrays.copyOfRange(header, 0, 2);
            String magicNumber = new String(temp, "US-ASCII");
            // "BM"인지 확인합니다.
            if (!"BM".equals(magicNumber)) {
                 return null;
            }

            int fileSize = (header[2] & 0xFF) | ((header[3] & 0xFF) << 8) | ((header[4] & 0xFF) << 16) | ((header[5] & 0xFF) << 24);
            int dataOffSet = (header[10] & 0xFF) | ((header[11] & 0xFF) << 8) | ((header[12] & 0xFF) << 16) | ((header[13] & 0xFF) << 24);
            int sizeOfDataHeader = (header[14] & 0xFF) | ((header[15] & 0xFF) << 8) | ((header[16] & 0xFF) << 16) | ((header[17] & 0xFF) << 24);
            int width = (header[18] & 0xFF) | ((header[19] & 0xFF) << 8) | ((header[20] & 0xFF) << 16) | ((header[21] & 0xFF) << 24);
            int height = (header[22] & 0xFF) | ((header[23] & 0xFF) << 8) | ((header[24] & 0xFF) << 16) | ((header[25] & 0xFF) << 24);
            int pixelPerBit = (header[28] & 0xFF) | ((header[29] & 0xFF) << 8);
            int Compression = (header[30] & 0xFF) | ((header[31] & 0xFF) << 8) | ((header[32] & 0xFF) << 16) | ((header[33] & 0xFF) << 24);
            int colorIndex = (header[46] & 0xFF) | ((header[47] & 0xFF) << 8) | ((header[48] & 0xFF) << 16) | ((header[49] & 0xFF) << 24);
            imageData = new int[height][width];

            System.out.println("데이터오프셋: " + dataOffSet + " 픽셀당 비트: " + pixelPerBit + " 컬러테이블: " + colorIndex + " 압축: " + Compression + " 사이즈: " + fileSize + " 정보헤더의 크기: "+sizeOfDataHeader);
            System.out.println("높이" + height + "넓이" + width);
            // BMP 데이터 읽기
            int imageDataSize = fileSize - 54;
            byte[] imageDataByte = new byte[imageDataSize];
            fis.read(imageDataByte);
            dataOffSet -= 54;
            if ( pixelPerBit == 24)
            {
                imageData = truecolorBMPreader(imageDataByte,imageData,dataOffSet);
            }
            else if(Compression == 0)
            {
                imageData = normalBMPreader(imageDataByte,imageData,dataOffSet,pixelPerBit, colorIndex);
            }
            else if(Compression == 1)
            {
                imageData = RLE8BMPreader(imageDataByte,imageData,dataOffSet, fileSize);
            }
            else if (Compression == 2)
            {
                imageData = RLE4BMPreader(imageDataByte,imageData,dataOffSet, fileSize);
            }

  /*          for(int i = 0; i < imageData.length; i++)
            {
                for(int j = 0; j < imageData[0].length; j++)
                {
                    System.out.print(imageData[i][j]+ " ");
                }
                System.out.println();
            }*/

        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return imageData;
    }

    private int[][] truecolorBMPreader(byte[] data,int[][] target,int start)
    {
        int height = target.length;
        int width = target[0].length;
        int padding = 0;
        while((width+padding)%4 != 0){ padding++;}
        int dataIndex = start;
        for(int hIndex = height - 1; hIndex >= 0; hIndex--)
        {
            for(int wIndex = 0; wIndex < width; wIndex++)
            {
                target[hIndex][wIndex] = (data[dataIndex++]&0xff) + (1000*(data[dataIndex++]&0xff)) + (1000000*(data[dataIndex++]&0xff));
            }
        }
        return target;
    }
    private int[][] RLE4BMPreader(byte[] data, int[][] target, int start, int size)
    {
        int wIndex = 0;
        int hIndex = target.length-1;
        while(start < size) {
            int fst = data[start++] & 0xff;
            byte temp = data[start++];
            int sec = temp & 0xff;
            int a = (temp & 0xf0) >> 4;
            int b = temp & 0xf;
            if(fst != 0 )
            {
                for(int i = 0; i < fst; i++)
                {
                    if (i % 2 == 0)
                    {
                        target[hIndex][wIndex++] = a;
                    }
                    else
                    {
                        target[hIndex][wIndex++] = b;
                    }
                }
            }
            else
            {
                if(sec == 0)
                {
                    if(wIndex != target[0].length - 1)
                    {
                        for(; wIndex < target[0].length; wIndex++)
                        {
                            target[hIndex][wIndex] = 0;
                        }
                        hIndex--;

                        wIndex = 0;
                    }
                }
                else if (sec == 1)
                {
                    return target;
                }
                else if (sec == 2)
                {
                    wIndex += data[start++] & 0xFF;
                    hIndex -= data[start++] & 0xFF;
                }
                else if (sec >= 3)
                {
                    for(int i = 0; i < sec; i++)
                    {
                        if(i%2 == 0)
                        {
                            target[hIndex][wIndex++] = (data[start]&0xf0) >>4 ;
                        }
                        else
                        {
                            target[hIndex][wIndex++] = (data[start++]&0xf0) >>4 ;
                        }
                    }
                    if(sec%2 == 1)
                    {
                        start++;
                    }

                    if(round(sec/2)%2 == 1)//짝수 맞춰주기
                    {
                        start++;
                    }
                }
            }
        }
        return target;
    }

    private double round(double a)
    {
        double temp = (int) a;
        if(a - (int)a == 0.5)
        {
            temp = (int)a +1;
        }
        return temp;
    }
    private int[][] RLE8BMPreader(byte[] data, int[][] target, int start, int size)
    {
        int wIndex = 0;
        int hIndex = target.length-1;
        while(start < size)
        {
            int fst = data[start++] &0xff;
            int sec = data[start++] &0xff;
            if(fst != 0) {
                for (int i = 0; i < fst; i++)
                {
                    target[hIndex][wIndex++] = sec;
                }
            }
            else
            {
                if(sec == 00)
                {
                    if(wIndex != target[0].length - 1)
                    {
                        for(; wIndex < target[0].length; wIndex++)
                        {
                            target[hIndex][wIndex] = 0;
                            //target[hIndex][wIndex] = target[hIndex][wIndex-1];
                        }
                        hIndex--;
                        wIndex = 0;
                    }
                }
                else if (sec == 01)
                {
                    return target;
                }
                else if (sec == 02) {
                    wIndex += data[start++] & 0xFF;
                    hIndex -= data[start++] & 0xFF;
                }
                else if(sec >= 03)
                {
                    if (sec%2 == 0)// 짝수이다
                    {
                        for(int i = 0 ; i < sec; i++)
                        {
                            target[hIndex][wIndex++] = data[start++] &0xff;
                        }
                    }
                    else
                    {
                        for(int i = 0 ; i < sec; i++)
                        {
                            target[hIndex][wIndex++] = data[start++] &0xff;
                        }
                        start++;
                    }
                }
            }

        }
        return target;
    }

    private int[][] normalBMPreader(byte[] data,int[][] target,int start, int bit, int colorIndex)
    {
        int width = target[0].length;
        int height = target.length;
        int padding = 0;
        while((width+padding)%4 != 0){ padding++;}
        int dataIndex = start;
        byte[] buffer = new byte[bit*width];
        int bufferIndex = 0;
        for(int hIndex = height - 1; hIndex >= 0; hIndex--)
        {
            bufferIndex = 0;
            //한 바이트에서 1비트씩 버퍼에 추가한다.
            for(int temp = 0 ; temp < width/8*bit;temp++)
            {
                //String temp = Integer.toString((data[dataIndex] & 0xff)+0x100, 16).substring(1);
                for (int i = 7; i >= 0; i--) {
                    buffer[bufferIndex++] = (byte) ((data[dataIndex + temp] & (1 << i)) >> i);
                }
            }

            dataIndex += width/8*bit;
            dataIndex += padding; // 빈공간인 패딩만큼 데이터를 뽑을 위치를 이동시킨다.

            for(int wIndex = 0,index = 0; index < width*bit;wIndex++)
            {
                byte temp = 0;
                for (int i = 0; i < bit; i++) //픽셀당 비트가 몇인지에 따라 이미지데이터의 한 픽셀에 들어가는 값을 변경시킨다.
                {
                    temp +=  (buffer[index++] << (bit-1 - i));
                }
                target[hIndex][wIndex] = temp &0xff;
            }
        }
        if(colorIndex != 0)
        {
            changeColor(target, data);
        }
        else
        {
            setColor(target,bit);
        }
        return target;
    }

    private void setColor(int[][] target, int bit)
    {
        for(int i = 0; i < target.length; i++)
        {
            for(int j = 0; j < target[0].length; j++)
            {
                target[i][j] = (int)((double)target[i][j]/(Math.pow(2,bit)-1)*256);
            }
        }
    }

    private void changeColor(int[][]target, byte[] data)
    {
        for(int i = 0; i < target.length; i++)
        {
            for(int j = 0; j < target[0].length; j++)
            {// b g r 순서로 컬러테이블에 존재
                target[i][j] = (data[4*target[i][j]]& 0xff) + (1000 * (data[4*target[i][j] + 1]& 0xff)) + (1000000 *(data[4*target[i][j]+2]& 0xff));
            }
        }
    }
    //

    @Test
    public void saveImage()//(int[][] newImageData, int[][] drawData, String filePath, Boolean outColor)
    {
        data = turnOn();

        int bit = bitCount(data);
        ArrayList<Byte> imagedata = new ArrayList<Byte>();
        if(bit == 24)
        {

            for(int i = 0; i < data.length; i++)
            {
                for(int j = 0; j < data[0].length; j++)
                {
                    int r = data[i][j]/1000000;
                    int g = data[i][j]/1000 - r*1000;
                    int b = data[i][j]%1000;
                    imagedata.add((byte)b);
                    imagedata.add((byte)g);
                    imagedata.add((byte)r);
                }
                while((imagedata.size() % 4) != 0)
                {
                    imagedata.add((byte)0);
                }
            }
        }
        else
        {
            if(true)
            {
                for (int i = data.length - 1; i >=0; i--)
                {
                    for (int j = 0; j < data[0].length; j++)
                    {
                        byte temp = 0 ;
                        for (int k = (8 / bit)-1; k >= 0; k--, j++)
                        {
                            temp += (byte) ((byte) buffer.indexOf(data[i][j]) << (bit*k));
                        }
                        imagedata.add(temp);
                    }
                    while((imagedata.size() % 4) != 0)
                    {
                        imagedata.add((byte)0);
                    }
                }
            }
            else// 흑백일때
            {
                for (int i = 0; i < data.length; i++)
                {
                    for (int j = 0; j < data[0].length; j++)
                    {
                        for (int k = 0; k < (8 / bit); k++, j++)
                        {
                            imagedata.add((byte) (data[i][j] % 1000 / (255 / bit)));
                        }
                    }
                    while((imagedata.size() % 4) != 0)
                    {
                        imagedata.add((byte)0);
                    }
                }
            }
        }

        byte[] header = headerMake(imagedata.size(), data , bit);
        byte[] colortable = null;
        if(true)//outColor
        {

        }

        try(FileOutputStream fos=new FileOutputStream("abcd.bmp")){
            fos.write(header);
            if(true)
            {
                fos.write(colortable);
            }
            byte[] data = new byte[imagedata.size()];
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) imagedata.get(i);
            }
            fos.write(data);

        }
        catch(IOException e)
        {

        }
    }

    private byte[] headerMake(int a , int[][] data,int bit)
    {
        int temp = 0;
        if(true)//outColor
        {
            temp = buffer.size();
        }
        int number = a+ 54 + temp;
        int dataoffset = 54+temp;
        byte[] header = new byte[54];
        header[0] = 0x42;
        header[1] = 0x4D;
        header[2] = (byte) (number & 0xFF);// 파일의 크기
        header[3] = (byte) ((number >> 8) & 0xFF);
        header[4] = (byte) ((number >> 16) & 0xFF);
        header[5] = (byte) ((number >> 24) & 0xFF);
        header[10] = (byte) (dataoffset & 0xFF); // 이미지 데이터의 시작지점
        header[11] = (byte) ((dataoffset >> 8) & 0xFF);
        header[12] = (byte) ((dataoffset >> 16) & 0xFF);
        header[13] = (byte) ((dataoffset >> 24) & 0xFF);
        header[14] = 0x28; //정보헤더의 크기
        header[18] = (byte) (data[0].length & 0xFF); // 넓이
        header[19] = (byte) ((data[0].length  >> 8) & 0xFF);
        header[20] = (byte) ((data[0].length  >> 16) & 0xFF);
        header[21] = (byte) ((data[0].length  >> 24) & 0xFF);
        header[22] = (byte) (data.length & 0xFF); // 높이
        header[23] = (byte) ((data.length  >> 8) & 0xFF);
        header[24] = (byte) ((data.length  >> 16) & 0xFF);
        header[25] = (byte) ((data.length  >> 24) & 0xFF);
        header[26] = (byte) (temp & 0xFF); // 색상 팔레트 수 2바이트
        header[27] = (byte) ((temp >> 8) & 0xFF);
        header[28] = (byte) (bit & 0xFF);
        header[29] = (byte) ((bit >> 8)& 0xFF);
        header[46] = (byte) 0xff;
        header[50] = (byte) 0xff;
        return header;
    }

    private int bitCount(int[][] imageData)
    {
        int height = imageData.length;
        int width = imageData[0].length;
        for(int i = 0; i < height; i++)
        {
            for(int j = 0; j < width; j++)
            {
                if(!buffer.contains(imageData[i][j]))
                {
                    if(buffer.size() == 256)
                    {
                        return 24;
                    }
                    buffer.add(imageData[i][j]);
                }
            }
        }
        for(int i = 0; i < 8; i++)
        {
            if(Math.pow(2,i) <= buffer.size() && buffer.size() <= Math.pow(2,i+1))
            {
                buffer.sort(Comparator.naturalOrder());
                return i+1;
            }
        }
        return -1;
    }
}