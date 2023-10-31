import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

public class BMPPanelInter extends PanelInter{
    private String infilepath = null;
    private Boolean isColor = false;
    ArrayList<Integer> buffer = new ArrayList<Integer>();
    public BMPPanelInter(String temppath, Component tempCom) // 파일의 입력을 받아 출력할때 사용하는 생성자
    {
        super(tempCom);
        infilepath = temppath;
    }
    public BMPPanelInter(Component tempCom)
    {
        super(tempCom);
    } // 출력에서 이 클래스의 saveImage부분만 필요할때 사용되는 생성자

    @Override
    public boolean getIsColor() {
        return isColor;
    } // 클래스의 isColor를 가져가는 함수

    @Override
    public int[][] turnOn() {// 파일의 헤더를 읽어 어떤 함수를 이용해서 파일을 읽을지 결정하는 함수
        int[][] imageData =null;
        try (FileInputStream fis = new FileInputStream(infilepath)) {
            // BMP 파일 헤더 읽기 54바이트
            byte[] header = new byte[54];
            fis.read(header);

            // 매직 넘버를 문자열로 변환합니다.
            byte[] temp = Arrays.copyOfRange(header, 0, 2);
            String magicNumber = new String(temp, "US-ASCII");
            // "BM"인지 확인합니다.
            if (!"BM".equals(magicNumber)) {
                return imageData;
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
            if ( pixelPerBit == 24) // 24비트 트루컬러 파일일때의 처리
            {
                imageData = truecolorBMPreader(imageDataByte,imageData,dataOffSet);
            }
            else if(Compression == 0) // 일반 BMP 처리
            {
                imageData = normalBMPreader(imageDataByte,imageData,dataOffSet,pixelPerBit, colorIndex);
            }
            else if(Compression == 1) // RLE8파일 처리
            {
                imageData = RLE8BMPreader(imageDataByte,imageData,dataOffSet, fileSize);
            }
            else if(Compression == 2) // RLE4파일 처리
            {
                imageData = RLE4BMPreader(imageDataByte,imageData,dataOffSet, fileSize);
            }



        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return imageData;
    }

    private int[][] RLE4BMPreader(byte[] data, int[][] target, int start, int size) // RLE4 파일을 읽는 함수
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
                    setColor(target, 4);
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
    private double round(double a) // 반올림 함수
    {
        double temp = (int) a;
        if(a - (int)a == 0.5)
        {
            temp = (int)a +1;
        }
        return temp;
    }
    private int[][] truecolorBMPreader(byte[] data,int[][] target,int start) // 24bit 트루컬러를 읽는 함수
    {
        isColor = true;
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
    private int[][] RLE8BMPreader(byte[] data, int[][] target, int start, int size) // RLE8 파일을 읽는 함수
    {
        int wIndex = 0;
        int hIndex = target.length-1;
        while(start < size)
        {
            int fst = data[start++] &0xff; // RLE8 압축은 두비트 단위로 읽으면 잘 해독할 수 있다.
            int sec = data[start++] &0xff;
            if(fst != 0) { // 첫바이트가 00이 아닐경우 첫바이트가 나타대는 수만큼 두번째 바이트가 한 픽셀의 이미지 데이터를 나타낸다.
                for (int i = 0; i < fst; i++)
                {
                    target[hIndex][wIndex++] = sec;
                }
            }
            else
            {
                if(sec == 00) // 00 00 일경우 다음 줄로 넘어간다.
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
                else if (sec == 01) // 00 01 일 경우 이미지 데이터의 끝이다.
                {

                    return target;
                }
                else if (sec == 02) // 00 02 일 경우 현재 행과 열 위치를 이동한다.
                {
                    wIndex += data[start++] & 0xFF;
                    hIndex -= data[start++] & 0xFF;
                }
                else if(sec >= 03) // 두번째 바이트가 3 이상일 경우 두번째 바이트가 나타내는 수만큼 다음 바이트가 각 RGB를 나타낸다.
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

    private int[][] normalBMPreader(byte[] data,int[][] target,int start, int bit, int colorIndex) // 일반 BMP파일을 읽는 함수
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
            isColor = true;
        }
/*         else
        {
            setColor(target,bit);
        }*/
        changeColor(target,data);
        return target;
    }

    private void setColor(int[][] target, int bit) // 흑백일 경우 각 위치의 값을 설정하는 과정
    {
        for(int i = 0; i < target.length; i++)
        {
            for(int j = 0; j < target[0].length; j++)
            {
                if(bit == 1)
                {
                    target[i][j] = (target[i][j] == 0 ? 1 : 0);
                }
                target[i][j] = (int)((double)target[i][j]/(Math.pow(2,bit))*255);
            }
        }
    }
    private void changeColor(int[][]target, byte[] data) // 컬러테이블에 존재하는 컬러를 2차원 배열에 넣기 적합하게 세팅하는 과정
    {
        for(int i = 0; i < target.length; i++)
        {
            for(int j = 0; j < target[0].length; j++)
            {// b g r 순서로 컬러테이블에 존재
                target[i][j] = (data[4*target[i][j]]& 0xff) + (1000 * (data[4*target[i][j] + 1]& 0xff)) + (1000000 *(data[4*target[i][j]+2]& 0xff));
            }
        }
    }
    public void saveImage(int[][] newImageData, int[][] drawData, String filePath, Boolean outColor) // 일반 BMP파일과 24비트 트루컬러 곧 압축되지 않은 이미지를 출력하는 함수.
    {
        int bit = bitCount(newImageData, drawData);
        if(bit < 4)
        {
            bit =4;
        }
        if(bit == 8)
        {
            int answer = JOptionPane.showConfirmDialog(Target,"RLE8 압축을 하시겠습니까?", "BMP파일 출력", JOptionPane.YES_NO_OPTION);
            if( answer == 1 || answer == -1)// 1일때는 No -1일때는 X누름
            {
                // No일때 그냥 넘기기
            }
            else// Yes일때
            {
                saveImageRLE(newImageData, bit, filePath, outColor);
                return;
            }

        }
        ArrayList<Byte> imagedata = new ArrayList<Byte>();
        if(bit == 24)
        {
            outColor = false;
            for(int i  = newImageData.length - 1; i >= 0 ; i--)
            {
                for(int j = 0; j < newImageData[0].length; j++)
                {
                    int r = newImageData[i][j]/1000000;
                    int g = newImageData[i][j]/1000 - r*1000;
                    int b = newImageData[i][j]%1000;
                    if(drawData[i][j] != 0)
                    {
                        r = 255;
                        g = 255;
                        b = 255;
                    }
                    imagedata.add((byte)b);
                    imagedata.add((byte)g);
                    imagedata.add((byte)r);
                }
/*                while((imagedata.size() % 4) != 0)
                {
                    imagedata.add((byte)0);
                }*/
            }
        }
        else
        {
            if(outColor)
            {
                for (int i = newImageData.length - 1; i >=0; i--)
                {
                    for (int j = 0; j < newImageData[0].length;)
                    {
                        byte temp = 0 ;
                        for (int k = (8 / bit)-1; k >= 0; k--, j++)
                        {
                            temp += (byte) ((byte) buffer.indexOf(newImageData[i][j]) << (bit*k));
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
                for (int i = newImageData.length - 1; i >=0; i--)
                {
                    for (int j = 0; j < newImageData[0].length;)
                    {
                        byte temp = 0 ;
                        for (int k = (8 / bit)-1; k >= 0; k--, j++)
                        {
                            temp += (byte) ((byte) ((newImageData[i][j] % 1000)/ Math.pow(2,8-bit)) << (bit*k));

                        }
                        if(bit == 1)
                        {
                            temp = (byte) ~temp;
                        }
                        imagedata.add(temp);
                        /*for (int k = 0; k < (8 / bit); k++, j++)
                        {
                            imagedata.add((byte) );
                        }*/
                    }
                    while((imagedata.size() % 4) != 0)
                    {
                        imagedata.add((byte)0);
                    }
                }
            }
        }

        byte[] header = headerMake(imagedata.size(), newImageData, bit, outColor, 0);
        byte[] colortable = null;
        if(outColor)//outColor
        {
 /*           colortable = new byte[buffer.size()*4];
            for(int i = 0,j = 0; i < buffer.size()*4;)
            {
                int b = buffer.get(j) %1000;
                int g = (buffer.get(j) %1000000 - b ) /1000;
                int r = buffer.get(j) / 1000000;
                colortable[i++] = (byte) (b & 0xff);
                colortable[i++] = (byte) (g & 0xff);
                colortable[i++] = (byte) (r & 0xff);
                colortable[i++] = (byte) 0;
                j++;
            }*/
            colortable = new byte[buffer.size() * 4];
            for (int i = 0,j = 0; j < buffer.size();)
            {
                int a = buffer.get(j);

                int b = a %1000;
                int g = (a %1000000 - b ) /1000;
                int r = a/ 1000000;
                colortable[i++] = (byte) (b & 0xff);
                colortable[i++] = (byte) (g & 0xff);
                colortable[i++] = (byte) (r & 0xff);
                colortable[i++] = (byte) 0;
                j++;
            }
        }
        else if(!outColor && bit != 24)
        {
            if(bit == 1)
            {
                colortable = new byte[8];
                for(int i = 0; i < 3 ; i ++)
                {
                    colortable[i] = (byte) 0xff;
                }
            }
            else {
                colortable = new byte[(int) Math.pow(2, bit) * 4];
                for (int i = 0,j = 0; i < (int) Math.pow(2, bit) * 4;)
                {
                    int a = (int) (Math.pow(2, 8 - bit) * j);
                    if(bit != 8)
                    {
                        a += j;
                    }
                    colortable[i++] = (byte) (a & 0xff);
                    colortable[i++] = (byte) (a & 0xff);
                    colortable[i++] = (byte) (a & 0xff);
                    colortable[i++] = (byte) 0;
                    j++;
                }
            }
        }



        try(FileOutputStream fos=new FileOutputStream(filePath + ".bmp")){
            fos.write(header);
            if(bit != 24)
            {
                fos.write(colortable);
            }

            byte[] data = new byte[imagedata.size()];
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) imagedata.get(i);
            }
            fos.write(data);
            byte[] end = {00,00};
            fos.write(end);

        }
        catch(IOException e)
        {

        }
    }

    private byte[] headerMake(int a , int[][] data,int bit, boolean outColor, int Compression) // 헤더부분을 구성하는 함수.
    {
        int temp = (int) Math.pow(2,bit) * 4;
        if(bit == 24)
        {
            temp = 0;
        }
        if(outColor)
        {
            temp = buffer.size()*4;
        }
        int biSizeImage = data.length * data[0].length *bit / 8 +2 ;
        int number = a+ 54 + temp + 2;
        int dataoffset = 54 + temp;

        byte[] header = new byte[54];
        header[0] = 0x42; //Magicnumber
        header[1] = 0x4D; // 매직넘버 끝
        header[2] = (byte) (number & 0xFF);// 파일의 크기
        header[3] = (byte) ((number >> 8) & 0xFF);
        header[4] = (byte) ((number >> 16) & 0xFF);
        header[5] = (byte) ((number >> 24) & 0xFF); // 파일의 크기 끝
        header[10] = (byte) (dataoffset & 0xFF); // 이미지 데이터의 시작지점
        header[11] = (byte) ((dataoffset >> 8) & 0xFF);
        header[12] = (byte) ((dataoffset >> 16) & 0xFF);
        header[13] = (byte) ((dataoffset >> 24) & 0xFF); // 이미지 데이터의 시작지점 끝
        header[14] = 0x28; //정보헤더의 크기  14~17
        header[18] = (byte) (data[0].length & 0xFF); // 넓이 18~21
        header[19] = (byte) ((data[0].length  >> 8) & 0xFF);
        header[20] = (byte) ((data[0].length  >> 16) & 0xFF);
        header[21] = (byte) ((data[0].length  >> 24) & 0xFF);
        header[22] = (byte) (data.length & 0xFF); // 높이 22~25
        header[23] = (byte) ((data.length  >> 8) & 0xFF);
        header[24] = (byte) ((data.length  >> 16) & 0xFF);
        header[25] = (byte) ((data.length  >> 24) & 0xFF);
        header[26] = 0x01;// biplanes 항상 1인 부분
        header[28] = (byte) (bit & 0xFF);// 한 픽섹을 표현하기 위한 비트수 28~29
        header[30] = (byte) (Compression& 0xFF); // 압축 형식 표시 30~33
        header[34] = (byte) (biSizeImage & 0xFF); // 픽셀 데이터 저장공간
        header[35] = (byte) ((biSizeImage  >> 8) & 0xFF);
        header[36] = (byte) ((biSizeImage  >> 16) & 0xFF);
        header[37] = (byte) ((biSizeImage  >> 24) & 0xFF);
        header[38] = 0x12;// 가로 해상도 38~41
        header[39] = 0x0B;
        header[42] = 0x12;// 세로 해상도 38~41
        header[43] = 0x0B;
        if(outColor)
        {
            temp /= 4;
        header[46] = (byte) (temp & 0xFF); // 컬러테이블 크기
        header[47] = (byte) ((temp  >> 8) & 0xFF);
        header[48] = (byte) ((temp  >> 16) & 0xFF);
        header[49] = (byte) ((temp  >> 24) & 0xFF);
        }
        header[50] = (byte) 0xff;
        return header;
    }


    private int bitCount(int[][] imageData, int[][] drawData) // 몇비트로 파일을 만들면 가장 효율적인지 확인하는 함수
    {
        int height = imageData.length;
        int width = imageData[0].length;
        for(int i = 0; i < height; i++)
        {
            for(int j = 0; j < width; j++)
            {
                if(drawData[i][j] != 0)
                {
                    imageData[i][j] = drawData[i][j];
                }
                if(!buffer.contains(imageData[i][j]))
                {
                    buffer.add(imageData[i][j]);
                }
            }
        }
        if(buffer.size() > 256)
        {
            return 24;
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

    public void saveImageRLE(int[][] newImageData, int bit, String filePath, Boolean outColor) // RLE 이미지를 출력하는 함수
    {
        ArrayList<Byte> imagedata = new ArrayList<Byte>();
        for (int i = newImageData.length - 1; i >=0; i--)
        {
            Boolean switched = false;
            int j = 0;
            int mode = 0; // Undefined Mode
            ArrayList<Integer> temp = new ArrayList<Integer>();
            while( j < newImageData[0].length) {
                if(j == newImageData[0].length-1)
                {
                    temp.add(newImageData[i][j]);
                    EncodeRLE8(imagedata,temp);
                    break;
                }
                if (newImageData[i][j] == newImageData[i][j + 1])
                {
                    switched = mode == 2;
                    mode = 1; // Encoded Mode

                } else
                {
                    switched = mode == 1;
                    mode = 2; // Absolute Mode
                }

                if(switched)
                {
                    if(mode == 2)
                    {
                        temp.add(newImageData[i][j]);
                    }
                    EncodeRLE8(imagedata,temp);
                    temp.clear();
                    if(mode == 1)
                    {
                        temp.add(newImageData[i][j]);
                    }

                }
                else
                {
                    temp.add(newImageData[i][j]);
                }
                j++;
            }
            imagedata.add((byte) 0);
            imagedata.add((byte) 0);
        } // 이부분을 통해 파일 출력 이미지데이터를 만든다.

        byte[] header = headerMake(imagedata.size(), newImageData, bit, outColor, 1);
        byte[] colortable = null;
        if(outColor)
        {
            colortable = new byte[buffer.size() * 4];
            for (int i = 0, j = 0; i < buffer.size() * 4; ) {
                int b = buffer.get(j) % 1000;
                int g = (buffer.get(j) % 1000000 - b) / 1000;
                int r = buffer.get(j) / 1000000;
                colortable[i++] = (byte) (b & 0xff);
                colortable[i++] = (byte) (g & 0xff);
                colortable[i++] = (byte) (r & 0xff);
                colortable[i++] = (byte) 0;
                j++;
            }
        }
        else
        {
            colortable = new byte[(int) Math.pow(2, bit) * 4];
            for (int i = 0, j = 0; i < (int) Math.pow(2, bit) * 4; ) {
                int a = (int) (Math.pow(2, 8 - bit) * j);
                if (bit != 8) {
                    a += j;
                }
                colortable[i++] = (byte) (a & 0xff);
                colortable[i++] = (byte) (a & 0xff);
                colortable[i++] = (byte) (a & 0xff);
                colortable[i++] = (byte) 0;
                j++;
            }
        }

        try(FileOutputStream fos=new FileOutputStream(filePath + "RLE8.bmp")){
            fos.write(header);
            fos.write(colortable);

            byte[] data = new byte[imagedata.size()];
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) imagedata.get(i);
            }
            fos.write(data);
            byte[] end = {00,01};
            fos.write(end);
        }
        catch(IOException e)
        {

        }
    }



    private int[][] dataEncoder(int[][] newImageData) {
        int[][] encodeddata = new int[newImageData.length][newImageData[0].length/2];
        for(int i = 0; i < newImageData.length; i++)
        {
            for(int j = 0; j < newImageData[0].length; j += 2)
            {
                int a = buffer.indexOf(newImageData[i][j]);
                int b = buffer.indexOf(newImageData[i][j+1]);
                a = (a<<4);
                encodeddata[i][(int)j/2] = a+b;
            }
        }
        return encodeddata;
    }

    private void EncodeRLE8(ArrayList<Byte> imagedata, ArrayList<Integer> temp) // Absolute 모드 한뭉치 또는 Endoced모드 한뭉치씩 입력을 받아 압축시킨다.
    {
        if(temp.size() == 0)// Encoded 모드 에서 Encoded 모드로 넘어갈때 ex) 5 5 5 5 6 6 6 6 일때 그냥 넘어가는 코드
        {
            return;
        }
        if(temp.size() == 1)// 하나가 있으면 하나를 추가하는 형식
        {
            imagedata.add((byte) temp.size());
            imagedata.add((byte) buffer.indexOf(temp.get(0)));
        }
        else if (temp.size()==2 && !Objects.equals(temp.get(0),temp.get(1))) // 두개가 있을때 00 02 가 만들어질 경우 쪼개서 압축하는 방식
        {
            imagedata.add((byte) 1);
            imagedata.add((byte) buffer.indexOf(temp.get(0)));
            imagedata.add((byte) 1);
            imagedata.add((byte) buffer.indexOf(temp.get(1)));
        }
        else if(Objects.equals(temp.get(0), temp.get(1))) // Encode 모드
        {
            imagedata.add((byte) temp.size());
            imagedata.add((byte) buffer.indexOf(temp.get(0)));
        }
        else//Absolute 모드
        {
            imagedata.add((byte) 0);
            imagedata.add((byte) temp.size());
            for(int i = 0; i < temp.size(); i++)
            {
                if(temp.get(i)==0)
                {
                    imagedata.add((byte) 0);
                    continue;
                }
                imagedata.add((byte) buffer.indexOf(temp.get(i)));
            }
            if((temp.size() %2) == 1)
            {
                imagedata.add((byte) 0);
            }
        }
    }


}
