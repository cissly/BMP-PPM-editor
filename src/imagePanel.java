
import javax.swing.*;
import javax.swing.border.Border;
import java.util.List;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Robot;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class imagePanel extends JPanel {
    private PanelInter interDataClass = null;
    private Boolean invers = false;
    private Boolean flip_x= false;
    private Boolean flip_y= false;
    private Boolean gray = false;
    private Boolean isColor = null;
    private int[][] imageData = null;
    private int[][] newImageData =null;
    private int[][] drawData = null;
    private String filepath = null;

    public imagePanel(){ // 이미지 패널의 기본적인 구성요소들을 추가하기 위한 생성자
        this.setBackground(new Color(255,255,255));
        float[] dashPattern = {10, 10};  // 줄을 넣어주기 위한 기본적인 요소들
        Border dashedBorder = BorderFactory.createStrokeBorder(
                new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dashPattern, 0),Color.BLACK);
        setBorder(dashedBorder);
        setSize(400,400);
        buttonPanel b_panel = new buttonPanel(this);
        b_panel.buttonActionAdder(this);
        add (b_panel);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                // TODO Auto-generated method stub
                b_panel.setZero();
            }
        });
    }






    //스위치 함수들
    public void inversSwitch()
    {
        invers = !invers;
    }

    public void flipxSwitch()
    {
        flip_x= !flip_x;
    }

    public void flipySwitch()
    {
        flip_y= !flip_y;
    }

    public void turnGray()
    {
        gray = !gray;
    }

    // Image처리 함수들
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (imageData != null) {
            //그림을 응용프로그램 중앙에 그리기 위한 전처리 부분
            int height= imageData.length;
            int width= imageData[0].length;
            int com_height = getHeight();
            int com_width = getWidth();
            int start_height =(com_height/2) - (height/2);
            int start_width = (com_width/2) - (width/2);
            for(int y=0;y<height;y++)
            {
                for(int x=0;x<width;x++){
                    int pixelValue=imageData[y][x];
                    int x_point = start_width+x;
                    int y_point = start_height+y;
                    Color color = new Color(0,0,0);
                    if(flip_x == true)//좌우 반전시 시작점을 수정하기 위한 코드
                    {
                        x_point = start_width + width-1 -x;
                    }
                    if(flip_y == true)// 상하 반전시 시작점을 수정하기 위한 코드
                    {
                        y_point = start_height + height-1 - y;
                    }
                    if(isColor == true)// PPM 파일일 경우 하나의 int 값에 들어있는 rgb 각 값들을 빼내기 위한 과정
                    {
                        int rgb_b = pixelValue%1000;
                        int rgb_g = (pixelValue%1000000 - rgb_b)/1000;
                        int rgb_r = pixelValue/1000000;
                        if(invers == true) // PPM의 경우에 색반전을 수행하기 위한 코드
                        {
                            rgb_b = 255-rgb_b;
                            rgb_g = 255-rgb_g;
                            rgb_r = 255-rgb_r;
                        }
                        color =new Color(rgb_r,rgb_g,rgb_b);
                        if(gray == true)// 추가기능으로서 컬러사진을 흑백사진으로 만드는 코드
                        {
                            int rgb_gray = (rgb_b + rgb_g + rgb_r)/3;
                            color = new Color(rgb_gray,rgb_gray,rgb_gray);
                        }

                    }
                    else if(isColor == false) { // PGM파일일 경우 에 색의 값을 뽑아내기 위한 코드
                        if(invers == true) // PGM의 경우 색반전을 수행하기 위한 코드
                        {
                            pixelValue = 255- pixelValue;
                        }
                        pixelValue %= 1000;
                        color =new Color(pixelValue,pixelValue,pixelValue);
                    }

                    if(drawData != null && drawData[y_point-start_height][x_point-start_width] == 255255255)
                    {
                        color = new Color(0,0,0);
                    }
                    g.setColor(color);
                    g.drawLine(x_point,y_point,x_point,y_point);


                    newImageData[y_point-start_height][x_point-start_width] = 1000000*color.getRed()+1000*color.getGreen()+color.getBlue();
                }
            }
        }
    }




    public void remove()
    {
        imageData = null;
        drawData = null;
        removeAttributes();
        revalidate();
        repaint();
    }

    public void removeAttributes()
    {
        invers = false;
        flip_x= false;
        flip_y= false;
        gray = false;
        drawData = null;
    }

    public void modify(int x,int y)
    {
        if(imageData != null)
        {
            int height= imageData.length;

            int width= imageData[0].length;
            int com_height = getHeight();
            int com_width = getWidth();
            int start_height =(com_height/2) - (height/2);
            int start_width = (com_width/2) - (height/2);
            x -= start_width;
            y -= start_height;
            if(0<= x && x< imageData[0].length && 0<=y && y <imageData.length)
            {
                drawData[y][x] = 255255255;
            }
        }
    }
    //드래그 앤 드롭 기능 추가를 위한 함수
    public void setupDragAndDrop() // 드래그 앤 드롭으로 파일을 받아오기 위해 이벤트를 추가하는 함수
    {
        imagePanel a = this;
        new DropTarget(this,new DropTargetAdapter()
        {

            @Override
            public void drop(DropTargetDropEvent dtde)
            {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_LINK); // 드롭을 받으면 링크를 받아들이게 설정하는 부분
                    Transferable transferable = dtde.getTransferable(); // 받은 링크를 transferable 에 넣은 부분
                    if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
                    {
                        // transferable에 들어있는 데이터를 파일을 담는 리스트에 담는 모습 다중파일 링크도 받을 수 있으나 본 응용프로그램은 하나의 파일만 입력받도록 만들었음.
                        List<File> files=(List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                        // 절대위치로 파일 위치를 String 값으로 받는 부분
                        filepath=files.get(0).getAbsolutePath();
                        //확장자명을 통해서 PPM인지 PGM인지 구분하여 설정값들을 변경시켜주는 부분
                        //추후에 JPG와 관련된 과제가 나온다면 재활용하기 위해 jpg의 확장자도 읽을 수 있게 만들어 놓았다.
                        //이외의 파일은 경고창을 띄우게 만들어 놓았음
                        if ((filepath.length()-4)==filepath.indexOf(".pgm") ||(filepath.length()-4)==filepath.indexOf(".ppm"))
                        {
                            removeAttributes();
                            interDataClass = new PGMMPanelInter(filepath,a);
                            imageData = interDataClass.turnOn();
                            newImageData = new int[imageData.length][imageData[0].length];
                            drawData = new int[imageData.length][imageData[0].length];
                            isColor = interDataClass.getIsColor();
                            revalidate();
                            repaint();
                        }
                        else if((filepath.length()-4)==filepath.indexOf(".BMP") || (filepath.length()-4)==filepath.indexOf(".bmp") )
                        {
                            removeAttributes();
                            interDataClass = new BMPPanelInter(filepath,a);
                            imageData = interDataClass.turnOn();
                            newImageData = new int[imageData.length][imageData[0].length];
                            drawData = new int[imageData.length][imageData[0].length];
                            isColor = interDataClass.getIsColor();
                            revalidate();
                            repaint();
                        }
                        else
                        {
                            JOptionPane.showMessageDialog(a, "허용되지 않은 형식의 파일입니다.", "Message",JOptionPane.ERROR_MESSAGE );
                        }
                    }

                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }

        });
    }


    public void saveBMPImage(String answer)
    {
        if(isColor == true && gray == true)
        {
            isColor = false;
        }
        imagePanel a = this;
        BMPPanelInter temp = new BMPPanelInter(a);
        temp.saveImage(newImageData,drawData,answer,isColor);
    }

    public void savePPMImage(String answer)
    {
        if(isColor == true && gray == true)
        {
            isColor = false;
        }
        imagePanel a = this;
        PGMMPanelInter temp = new PGMMPanelInter(a);
        temp.saveImage(newImageData,drawData,answer,isColor);
    }
}