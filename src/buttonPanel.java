import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.util.Timer;

import javax.swing.JButton;
import javax.swing.JPanel;

public class buttonPanel extends JPanel{
    private imagePanel target;
    private int choose = 0;
    private int start_x = 0;
    private int start_y = 0;
    public buttonPanel(imagePanel target)
    {
        this.target = target;
        JButton a1 = new JButton("선그리기");
        a1.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                // TODO Auto-generated method stub
                choose = 1;
            }
        });
        JButton a2 = new JButton("사각형그리기");
        a2.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                // TODO Auto-generated method stub
                choose = 2;
            }
        });
        JButton a3 = new JButton("원그리기");
        a3.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                // TODO Auto-generated method stub
                choose = 3;
            }
        });
        JButton a4 = new JButton("그리기");
        a4.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                // TODO Auto-generated method stub
                choose = 4;
            }
        });
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx=0;
        gbc.gridy=0;
        gbc.weightx=1;
        gbc.weighty=0; // 상대적인 가중치 설정
        add(a1,gbc);
        gbc.gridx=1;
        add(a2,gbc);
        gbc.gridx=2;
        add(a3,gbc);
        gbc.gridx=3;
        add(a4,gbc);



    }

    public void setZero()
    {
        choose = 0;
    }

    public void buttonActionAdder(imagePanel target) {
        target.addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseDragged(MouseEvent e) {
                // TODO Auto-generated method stub
                if(choose == 4)
                {
                    saveLine(e.getX(),e.getY(),e.getX(),e.getY());
                }
            }
        });
        target.addMouseListener(new MouseListener() {


            @Override
            public void mouseClicked(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mousePressed(MouseEvent e) {
                // TODO Auto-generated method stub
                start_x=e.getX();
                start_y=e.getY();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // TODO Auto-generated method stub
                Graphics g = target.getGraphics();
                int end_x = e.getX();
                int end_y = e.getY();
                if(choose == 1)
                {
                    saveLine(start_x,start_y,end_x,end_y);
                }
                else if(choose == 2)
                {

                    saveRect(start_x,start_y,end_x,end_y);
                }
                else if(choose == 3)
                {
                    saveOval(start_x,start_y,end_x,end_y);
                    /*
                     * int width = Math.abs(start_x - end_x); int height = Math.abs(start_y -
                     * end_y); //g.drawOval(Math.min(start_x,end_x)+width/2,Math.min(start_y,
                     * end_y)+height/2, width/2, height/2);
                     * g.drawOval(Math.min(start_x,end_x),Math.min(start_y, end_y), width, height);
                     */
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseExited(MouseEvent e) {
                // TODO Auto-generated method stub

            }
        });
    }

    private void saveLine(int x1, int y1, int x2, int y2)
    {
        target.getGraphics().drawLine(x1, y1, x2, y2);
        if(x2 == x1)
        {
            for(int i = Math.min(y1,y2);i <= Math.max(y1,y2); i++)
            {
                target.modify(x1,i);
            }
            return;
        }
        else if(y2 == y1)
        {
            for(int i = Math.min(x1,x2);i <= Math.max(x1,x2); i++)
            {
                target.modify(i,y1);
            }
            return;
        }
        double slope = (double)(y2 - y1) / (x2 - x1);
        double intercept = y1 - slope * x1;

        for(int i = Math.min(x1,x2);i < Math.max(x1,x2); i++)
        {
            target.modify(i,(int)(slope*i+intercept));
        }
    }
    private void saveRect(int x1, int y1, int x2, int y2)
    {
        saveLine(x1,y1,x2,y1);
        saveLine(x2,y1,x2,y2);
        saveLine(x2,y2,x1,y2);
        saveLine(x1,y2,x1,y1);
    }

    private void saveOval(int x1, int y1, int x2, int y2)
    {
        double angleStep = 0.01; // 각도의 증가량, 조절 가능

        for (double angle = 0; angle < 2 * Math.PI; angle += angleStep) {
            int x = (int) ((x1+x2)/2 + Math.abs(x1-x2)/2 * Math.cos(angle));
            int y = (int) ((y1+y2)/2 + Math.abs(y1-y2)/2 * Math.sin(angle));
            saveLine(x,y,x,y);
        }
    }

}
