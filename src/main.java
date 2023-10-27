import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;

//32832
//32770

//131384
//131073

//131192
//131074
public class main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            mainFrame m_frame = new mainFrame();
            imagePanel before=new imagePanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;

            JPanel pn1 = new JPanel();
            String[] bt_name = {"색반전", "좌우반전", "상하반전","흑백전환"};
            JButton[] bt = new JButton[bt_name.length];
            for (int i = 0; i < bt_name.length; i++) {
                bt[i] = new JButton(bt_name[i]);
                buttonActionAdder(bt[i],before,i);
                pn1.add(bt[i]);
            }
            gbc.gridx=0;
            gbc.gridy=0;
            gbc.weightx=1;
            gbc.weighty=0; // 상대적인 가중치 설정
            m_frame.add(pn1,gbc);


            gbc.gridx=0;
            gbc.gridy=1;
            gbc.weighty=0.8;
            gbc.insets=new Insets(10,10,10,10); // 상하좌우 모두 10px의 여백 설정
            m_frame.add(before,gbc);


            JPanel pn3 = new JPanel();
            JButton[] bt3 = new JButton[4];
            String[] bt_name3 = {"지우기","원상복구","BMP저장하기","PGM저장하기"};
            for (int i = 0; i < bt_name3.length; i++) {
                bt3[i] = new JButton(bt_name3[i]);
                buttonActionAdder3(bt3[i],before,i);
                pn3.add(bt3[i]);
            }
            gbc.gridx=0;
            gbc.gridy=2;
            gbc.weightx=1;
            gbc.weighty=0; // 상대적인 가중치 설정
            m_frame.add(pn3,gbc);

            // 드래그 앤 드롭 기능 추가
            before.setupDragAndDrop();

        });
    }

    private static void buttonActionAdder(JButton a,imagePanel target,int i) {
        if( i== 0)
        {
            a.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    target.inversSwitch();
                    target.revalidate();
                    target.repaint();
                }

            });
        }
        else if( i== 1)
        {
            a.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    // TODO Auto-generated method stub
                    target.flipxSwitch();
                    target.revalidate();
                    target.repaint();
                }

            });
        }
        else if( i== 2)
        {
            a.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    // TODO Auto-generated method stub
                    target.flipySwitch();
                    target.revalidate();
                    target.repaint();
                }

            });
        }
        else if( i== 3)
        {
            a.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    target.turnGray();
                    target.revalidate();
                    target.repaint();
                }

            });
        }
        else if( i== 4)
        {
            a.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                }

            });
        }

    }
    private static void buttonActionAdder3 (JButton bt,imagePanel target,int i) {
        if( i== 0)
        {
            bt.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    target.remove();
                }

            });
        }
        else if( i==1)
        {
            bt.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    target.removeAttributes();
                    target.repaint();
                }

            });
        }
        else if( i==2)
        {
            bt.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    String answer = JOptionPane.showInputDialog(null, "이름을 입력하세요", "BMP저장", 0);
                    if (answer != null)
                    {
                        target.saveBMPImage(answer);
                    }
                }

            });
        }

        else if( i==3)
        {
            bt.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    String answer = JOptionPane.showInputDialog(null, "이름을 입력하세요", "BMP저장", 0);
                    if (answer != null)
                    {
                        target.savePPMImage(answer);
                    }
                }

            });
        }
    }
}


class mainFrame extends JFrame{
    public mainFrame()
    {
        setLayout(new GridBagLayout());
        setSize(1200,800);
        setTitle("INVER");

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);

    }
}
