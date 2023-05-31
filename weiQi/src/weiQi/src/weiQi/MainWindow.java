package weiQi;
import javax.swing.*;
import java.awt.*;
//import javax.swing.*;
import java.awt.event.*;
import java.lang.reflect.Array;
import java.net.*;
import java.io.*;
import java.util.Objects;


public class MainWindow extends Frame implements Runnable
{
    panelAndRule panelPAR =new panelAndRule();//��Go������������һ��panelGo����
    Image myImage;
    int PORT;
    int regretRes = -1;
    boolean isFirst = true;
    int isFirst2 = 0;
    boolean isConnectBtnClicked = false;
    boolean isLink = false;//������־
    boolean isHost = false;//������־
    Socket sendSocket;//��������Socket
    PrintWriter writer;//��������message
    boolean stopFlag;
    boolean isInitiative;//�Ƿ���������
    boolean isSingleFirst;//�ǲ��ǵ��˰���Ҫ��ʼ��
    Point messagePoint;
    Point goStartPoint=null;
    Point yellowPoint=null;
    boolean colorFlag = true;
    Point LastPoint=null;//�Ƴ��Ƶ�ʱ���ж�λ�ñ䶯*
    BorderLayout JFrameBL = new BorderLayout();//�߽粼��
    JPanel mainPanel = new JPanel();
    JPanel messagePanel = new JPanel();
    BorderLayout mainpanelBL = new BorderLayout();
    JPanel btnPanel = new JPanel();
    ButtonGroup modeBtnGroup = new ButtonGroup();//ѡ���ķ���
    JRadioButton radioButtonSingle = new JRadioButton("����");
    JRadioButton radioButtonMultiple = new JRadioButton("����");
    JLabel IPLabel = new JLabel("�Է�IP");
    TextField IPTextField = new TextField();
    JButton connectBtn = new JButton("����");//�����ӡ���ť
    JLabel flagQi = new JLabel();
    String[] options = { "����", "����"};//0, 1
    JComboBox<String> selectBox = new JComboBox<>(options);//ѡ��ڰ���
    int yourChoice;//����ģʽѡ�������Ϊʲô
    JButton regretBtn = new JButton("����");
    JButton startBtn = new JButton("��ʼ");
    GridLayout btnPanelGL = new GridLayout();//���ִ�����������������ʽ
    BorderLayout borderLayout3 = new BorderLayout();
    boolean isTiZi = false;//�ǲ������ӽ׶�

    JPanel jp1 = new JPanel();//��ʼҳ��Jpanel
    JPanel jp2 = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Image image = new ImageIcon(System.getProperty("user.dir")+"\\weiQi\\media\\bgi.jpg").getImage();
            g.drawImage(image, 0, 0, this);
        }
    };//���ʵ�����ݵ�Jpanel�������ñ���ͼ
    JButton startBtn0 = new JButton("��ʼ");//���ҳ���ϵĿ�ʼ��ť
    JButton exitBtn = new JButton("�˳�");//���ҳ���ϵ��˳���ť
    JLabel title = new JLabel("Χ����Ϸ");//ҳ�����


    public MainWindow() {
        try {
            Init();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void Init() throws Exception {
        //������ɫ
        selectBox.setBackground(new Color(246, 234, 216));
        connectBtn.setBackground(new Color(246, 234, 216));
        this.setResizable(false);//�������û��Լ��Ĵ��ڴ�С
        new Thread(this).start();//���������߳�
        this.PORT=1977;//�˿ں�
        this.isInitiative=false;//�Ƿ���������
        this.stopFlag=false;//�Ƿ���������ı�־
        LastPoint=new Point();
        messagePoint=new Point();
        this.setSize(700,700);//���
        this.setTitle("Χ����Ϸ");
        this.panelPAR.setEnabled(false);//��ʼ֮ǰ���ε�����
        radioButtonSingle.setOpaque(false);
        radioButtonMultiple.setOpaque(false);
        radioButtonSingle.addActionListener(this::dealBtnSingle);//������radio
        radioButtonMultiple.addActionListener(this::dealBtnMultiple);//��������radio
        //��ѡ�����������
        modeBtnGroup.add(radioButtonSingle);
        modeBtnGroup.add(radioButtonMultiple);

        this.goStartPoint=this.panelPAR.getLocation();
        this.setLayout(JFrameBL);
        mainPanel.setLayout(mainpanelBL);
        connectBtn.addActionListener(this::connectBtn_actionPerformed);//���ָ���Ĳ��������������·��������¼�
        flagQi.setText("  ");
        startBtn.setBackground(new Color(246, 234, 216));
        startBtn.addActionListener(this::startBtn_actionPerformed);
        btnPanel.setLayout(btnPanelGL);//���õĲ��ֹ�����
        btnPanelGL.setRows(9);//9��
        btnPanelGL.setColumns(1);//1��
        btnPanelGL.setHgap(100);//�����ˮƽ���
        btnPanelGL.setVgap(8);//����䴹ֱ���
        messagePanel.setLayout(borderLayout3);
        this.messagePanel.setSize(500,70);
        panelPAR.addMouseMotionListener(new MouseMotionAdapter() {//�����ϵ�����ƶ�
            public void mouseMoved(MouseEvent e) {
                checkerBoardMove(e);
            }
        });

        panelPAR.addMouseListener(new MouseAdapter() {//���̵��
            public void mouseClicked(MouseEvent e) {
                checkerBoardClick(e);
            }
        });

        this.addWindowListener(new WindowAdapter() {//���ڹر�
            public void windowClosing(WindowEvent e) {
                windowClose(e);
            }
        });

        regretBtn.addActionListener(e -> {
            regretBtnMove();
        });
        btnPanel.setBackground(new Color(248, 173, 10));
        btnPanel.add(radioButtonSingle, null);
        btnPanel.add(radioButtonMultiple, null);
        btnPanel.add(IPLabel, null);
        btnPanel.add(IPTextField, null);
        btnPanel.add(connectBtn, null);
        btnPanel.add(selectBox, null);
        btnPanel.add(regretBtn, null);
        btnPanel.add(startBtn, null);
        btnPanel.add(flagQi, null);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10,0,0,36));//����btnPanelλ�ã�ʹ������
        this.mainPanel.add(this.panelPAR, BorderLayout.CENTER);//��panelGo�ŵ�1��
        this.mainPanel.add(btnPanel, BorderLayout.EAST);//��btnPanel����

        CardLayout cl = new CardLayout();
        jp1.setLayout(cl);//����jp1�Ŀ�Ƭ���ֹ�����
        startBtn0.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cl.show(jp1, "main");
            }
        });
        startBtn0.setFont(new Font("Microsoft YaHei", Font.PLAIN, 24));
        exitBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.exit(0);
            }
        });
        exitBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 24));
        this.jp1.add("main", mainPanel);//��main����jp1
        this.jp2.setLayout(new BoxLayout(jp2, BoxLayout.Y_AXIS));//���ò��ֹ�����
        startBtn0.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("Microsoft YaHei", Font.BOLD, 40));
        title.setForeground(Color.red);
        this.jp2.add(Box.createVerticalStrut(30));//��ӿ���� ������Ӵ�ֱ���
        this.jp2.add(title);
        this.jp2.add(Box.createVerticalStrut(180));//��ӿ���� ������Ӵ�ֱ���
        this.jp2.add(startBtn0);
        this.jp2.add(Box.createVerticalStrut(20));//��ӿ���� ������Ӵ�ֱ���
        this.jp2.add(exitBtn);
        this.jp1.add("welcomePage", jp2);//��jp2�Ž�ȥ
        this.add(messagePanel, BorderLayout.SOUTH);
        this.add(jp1, BorderLayout.CENTER);
        cl.show(jp1, "welcomePage");//����ΪĬ��չʾ��ӭҳ

        this.disableLink();//���ÿؼ�
        this.radioButtonSingle.doClick();//����Ĭ��ѡ��ť1
        this.yellowPoint=new Point(1000,1000);//��ʼ����ʶ��ǰ���λ�õĻƵ�
        this.centerWindow();

        this.setVisible(true);
        myImage=this.createImage(16,16);//(����ͼ��)������¼�лƵ�֮ǰ��ͼ��

    }
    void centerWindow() {
        Dimension d=Toolkit.getDefaultToolkit().getScreenSize();
        int pX=(d.width-this.getWidth())/2;
        int pY=(d.height-this.getHeight())/2;
        this.setLocation(pX,pY);//�Ƶ���λ��
    }
    //������
    public static void main(String[] args) {
        new MainWindow();
    }
    //�����߳�
    public void run() {
        try {
            ServerSocket serverSocket=new ServerSocket(PORT);//ServerSocket����ʵ�ַ������׽��֣���������ִ�в��������������߷��ؽ��
            Socket receiveSocket;//Socket��ʵ�ֿͻ����׽���
            receiveSocket=serverSocket.accept();
            if(this.isInitiative)//������ڽ��У��򲻽�������
                this.stopFlag=true;
            this.radioButtonMultiple.setSelected(true);//�Զ�ѡ������*
            this.connectBtn.setEnabled(false);
            this.selectBox.setEnabled(true);
            this.IPTextField.setEnabled(false);
            this.radioButtonSingle.setEnabled(false);
            this.radioButtonMultiple.setEnabled(false);
            this.writer=new PrintWriter(receiveSocket.getOutputStream(),true);
            BufferedReader reader=new BufferedReader(new InputStreamReader(receiveSocket.getInputStream()));
            String message;
            while(!this.stopFlag) {
                if(isFirst2==0) {
                    this.panelPAR.showError("�������ӳɹ���������ʼ��ť��");
                    isFirst2++;
                }
                isConnectBtnClicked = true;
                message=reader.readLine();
                this.doMessage(message);
            }
            reader.close();
            receiveSocket.close();
            serverSocket.close();
        }catch(IOException ioe){
            this.panelPAR.repaint();
            this.panelPAR.showError("�����ж�");}

    }

    //������յ�����Ϣ
    void doMessage(String message) {
        if(message.startsWith("start")){//�жϿ�ʼ
            this.panelPAR.repaint();
            this.panelPAR.showError("�Է��ѿ�ʼ");
            if(message.equals("start_black"))
                this.selectBox.setSelectedIndex(1);//selectѡ��������Ϊָ��λ���ϵ���
            else
                this.selectBox.setSelectedIndex(0);

            if(Objects.equals(this.selectBox.getSelectedItem(), "����"))//ֻҪ���Ǻڵģ�������
                this.panelPAR.setEnabled(true);
            this.paintMyColor();//������ɫ
            this.disableLink();
        }
        else if(message.equals("regretreq")){
            regretRes = JOptionPane.showConfirmDialog(this,"Ҫͬ��Է��Ļ���������","�Է��������",1,1);
            writer.println(regretRes);
        }
        else if(message.equals("0")) {
            regretRes = 0;
            regretBtnMove();
            this.panelPAR.setEnabled(true);
        }
        else if(message.charAt(message.length()-1)=='1'){
            Point temp =null;
            this.panelPAR.pointNow = this.panelPAR.pointNowStack.pop();
            this.panelPAR.singleRemove(panelPAR.pointNow);
            this.panelPAR.repaint();
            regretRes = -1;
            if (!this.panelPAR.pointNowStack.isEmpty()) {
                temp = this.panelPAR.pointNowStack.peek();
            }
            if (temp != null) {
                this.panelPAR.pointNow.x = temp.x;
                this.panelPAR.pointNow.y = temp.y;
            }
        }
        else{//���ӵ���Ϣ*
            int color=Integer.parseInt(message.substring(0,1));
            this.messagePoint.x=Integer.parseInt(message.substring(1,3));
            this.messagePoint.y=Integer.parseInt(message.substring(3,5));
            this.panelPAR.setEnabled(true);//���
            this.panelPAR.doStep(this.messagePoint,color);
        }
    }

    //Ϊ��궨λ*
    void checkerBoardMove(MouseEvent e) {
        Point realPoint=e.getPoint();
        Point mousePoint=this.panelPAR.getMousePoint(realPoint,this.goStartPoint);
        this.removeLastMousePoint(this.LastPoint, mousePoint);
        this.LastPoint.x=mousePoint.x;
        this.LastPoint.y=mousePoint.y;
        if(this.isPlace(mousePoint)){
            this.showMousePoint(mousePoint);
        }
        else{
            this.yellowPoint.x=mousePoint.x;//��λ�Ƶ�
            this.yellowPoint.y=mousePoint.y;
        }
    }

    //�ӻƵ�ķ�Χ
    boolean isPlace(Point p) {
        if(p.x>19||p.x<1||p.y<1||p.y>19)
            return false;
        int color;
        pieceStruct pieceStruct;
        pieceStruct = (this.panelPAR.boardHash.get(p));
        color= pieceStruct.color;
        return color == 0;
    }
    void checkerBoardClick(MouseEvent e) {//���̵���¼�
        if(isTiZi){
            if(this.isSinglePlayer()){
                this.panelPAR.singleRemove(this.yellowPoint);
                Graphics myG=this.myImage.getGraphics();
                this.createMyImage(myG, this.yellowPoint,0);
            }
        }else{
            if(this.isSinglePlayer()) {
                this.doSingle();
            }
            else {
                this.doMultiple();
            }
        }
    }
    //����
    void regretBtnMove() {
        Point temp =null;
        if(!this.panelPAR.pointNowStack.isEmpty())
        {
            if (!isLink)
            {
                this.panelPAR.pointNow = this.panelPAR.pointNowStack.pop();
                this.panelPAR.updateValueHash(this.panelPAR.pointNow,false,yourChoice==0?2:1);
                this.panelPAR.singleRemove(panelPAR.pointNow);
                if (!this.panelPAR.pointNowStack.isEmpty()) {
                    this.panelPAR.pointNow = this.panelPAR.pointNowStack.pop();
                    this.panelPAR.updateValueHash(this.panelPAR.pointNow,false,yourChoice+1);
                    this.panelPAR.singleRemove(panelPAR.pointNow);
                    if (!this.panelPAR.pointNowStack.isEmpty()) {
                        temp = this.panelPAR.pointNowStack.peek();
                    }
                }
                this.panelPAR.repaint();
                this.colorFlag = yourChoice == 0 ;
                if (!colorFlag && this.panelPAR.pointNowStack.isEmpty()) this.panelPAR.AI();
                this.paintThisColor(this.colorFlag);
                if (temp != null) {
                    this.panelPAR.pointNow.x = temp.x;
                    this.panelPAR.pointNow.y = temp.y;
                }
            } else
            {
                if(isFirst){
                    String message="regretreq";
                    this.writer.println(message);
                    isFirst = false;
                }
                if(regretRes == 0){
                    this.panelPAR.pointNow = this.panelPAR.pointNowStack.pop();
                    this.panelPAR.singleRemove(panelPAR.pointNow);
                    this.panelPAR.repaint();
                    String mes = getMessage(0,this.panelPAR.pointNow.x,this.panelPAR.pointNow.y,1);
                    writer.println(mes);
                    regretRes = -1;
                    if (!this.panelPAR.pointNowStack.isEmpty()) {
                        temp = this.panelPAR.pointNowStack.peek();
                    }
                    if (temp != null) {
                        this.panelPAR.pointNow.x = temp.x;
                        this.panelPAR.pointNow.y = temp.y;
                    }

                }
            }
        }
    }
    //�����Ϸ֮��ʼ
    void startBtn_actionPerformed(ActionEvent e) {
        if(e.getActionCommand().equals("��ʼ")) {

            if(this.isSinglePlayer())
                this.panelPAR.setEnabled(true);
            else{//������ʱ
                if(!isConnectBtnClicked&&isHost){
                    this.panelPAR.repaint();
                    this.panelPAR.showError("�������ӣ�");
                    return;
                }
                if(Objects.equals(this.selectBox.getSelectedItem(), "����")) {
                    this.writer.println("start_black");
                    this.isLink = true;
                }
                else {
                    this.writer.println("start_white");
                    this.isLink = true;
                }
            }
            this.disableLink();
            this.radioButtonSingle.setEnabled(false);
            this.radioButtonMultiple.setEnabled(false);
            this.startBtn.setText("����");
            this.paintMyColor();//������ɫ
            if(this.isSingleFirst&&!isLink){//ѡ������
                this.isSingleFirst = false;
                this.yourChoice = this.selectBox.getSelectedIndex();//0�� 1��
                this.panelPAR.setAIColor(this.yourChoice==0?2:1);
                if(this.yourChoice==1){//AI
                    doSingle();
                }
            }
        } else if(e.getActionCommand().equals("�˳�")) {
            this.dispose();
            System.exit(0);
        }else if(e.getActionCommand().equals("����")){
            this.isTiZi = false;
            //�����жϵ��׶����ӵ��߼�
            int[] ans;
            String text;
            JDialog dialog = new JDialog(this,"���",true);
            ans = this.panelPAR.count();
            JLabel jl=new JLabel("���壺" + ans[0]);
            JLabel jl2=new JLabel("���壺" + ans[1]);
            if(ans[0]>=185)text = "����ʤ";
            else if(ans[1]>=177)text = "����ʤ";
            else text = "��δ��ɶԾ�";
            JLabel jl3=new JLabel(text);
            dialog.add(jl,BorderLayout.NORTH);
            dialog.add(jl2,BorderLayout.CENTER);
            dialog.add(jl3,BorderLayout.SOUTH);
            dialog.setBounds(200,200,200,150);
            dialog.setVisible(true);
            this.startBtn.setText("�˳�");//û��Ҫ ��Ϊ������ʾ��Ҫһ���Ի��򣬵���˳�ֱ���˳�
        }else if(e.getActionCommand().equals("����")){
            this.isTiZi = true;
            this.startBtn.setText("����");
        }
    }
    //disable����ʱ�õĿؼ�*
    void disableLink() {
        this.IPTextField.setBackground(new Color(246, 234, 216));
        this.IPTextField.setEnabled(false);//����
        this.selectBox.setEnabled(false);//����
        this.connectBtn.setEnabled(false);
    }
    //enable����ʱ�Ŀؼ�*
    void enableLink() {
        this.IPTextField.setBackground(Color.white);
        this.IPTextField.setEnabled(true);
        this.selectBox.setEnabled(true);
        this.connectBtn.setEnabled(true);
    }
    //�ж����� �Ƿ�Ϊ����ģʽ
    boolean isSinglePlayer() {
        return this.radioButtonSingle.isSelected();
    }

    //��С�Ƶ�
    void showMousePoint(Point mousePoint) {

        Graphics g=this.panelPAR.getGraphics();
        g.setColor(Color.yellow);
        g.fillOval(mousePoint.x*30-21,mousePoint.y*30-21,this.panelPAR.INTERVAL-8,this.panelPAR.INTERVAL-8);
        this.yellowPoint.x=mousePoint.x;//��λ�Ƶ�
        this.yellowPoint.y=mousePoint.y;
        Graphics myG=this.myImage.getGraphics();
        this.createMyImage(myG, this.yellowPoint,0);
    }
    //����ǰһ���Ƶ�*
    void removeLastMousePoint(Point thatPoint,Point thisPoint) {
        if(thatPoint.x!=thisPoint.x||thatPoint.y!=thisPoint.y) {
            Graphics g=this.panelPAR.getGraphics();
            if(this.yellowPoint!=null&&this.myImage!=null&&this.isPlace(thatPoint))
                g.drawImage(this.myImage,this.yellowPoint.x*30-21,this.yellowPoint.y*30-21,22,22,null);
            assert this.yellowPoint != null;
        }
    }

    //����ͼ��Image
    void createMyImage(Graphics g,Point thisPoint,int color) {
        int px=thisPoint.x;
        int py=thisPoint.y;
        Color myColor=this.panelPAR.getBackground();

        if(px==1&&py==1&&color==0) {
            g.setColor(myColor);
            g.fillRect(0,0,22,23);
            g.setColor(Color.black);
            g.drawLine(8,8,8,23);

            g.drawLine(8,8,23,8);
//          g.drawLine(5,5,5,26);
//          g.drawLine(5,5,26,5);
        }
        else if(px==1&&py==19&&color==0) {
            g.setColor(myColor);
            g.fillRect(0,0,23,23);
            g.setColor(Color.black);
            g.drawLine(8,8,8,0);
            g.drawLine(8,8,23,8);
//            g.drawLine(5,21,26,21);
//            g.drawLine(5,21,5,0);
        }
        else if(px==19&&py==1&&color==0) {
            g.setColor(myColor);
            g.fillRect(0,0,23,23);
            g.setColor(Color.black);
            g.drawLine(8,8,8,23);
            g.drawLine(8,8,0,8);
//            g.drawLine(21,5,21,26);
//            g.drawLine(21,5,0,5);
        }
        else if(px==19&&py==19&&color==0) {
            g.setColor(myColor);
            g.fillRect(0,0,22,22);
            g.setColor(Color.black);
            g.drawLine(8,8,8,0);
            g.drawLine(8,8,0,8);
            g.drawLine(21,21,21,0);
//            g.drawLine(21,21,0,21);
        }
        else if(px==1&&color==0){//�ĸ���
            g.setColor(myColor);
            g.fillRect(0,0,22,22);
            g.setColor(Color.black);
            g.drawLine(8,8,22,8);
            g.drawLine(8,0,8,22);
//            g.drawLine(6,0,6,26);
        }
        else if(px==19&&color==0) {
            g.setColor(myColor);
            g.fillRect(0,0,22,22);
            g.setColor(Color.black);
            g.drawLine(8,8,0,8);
            g.drawLine(8,0,8,22);
//            g.drawLine(20,0,20,26);
        }
        else if(py==1&&color==0) {
            g.setColor(myColor);
            g.fillRect(0,0,22,22);
            g.setColor(Color.black);
            g.drawLine(8,8,8,22);
            g.drawLine(0,8,22,8);
//            g.drawLine(0,5,26,5);
        }
        else if(py==19&&color==0) {
            g.setColor(myColor);
            g.fillRect(0,0,22,22);
            g.setColor(Color.black);
            g.drawLine(8,8,8,0);
            g.drawLine(0,8,22,8);
//            g.drawLine(0,21,26,21);
        }
        //�Ÿ�С�ڵ�
        else if(color==0&&((px==4&&py==4)||(px==4&&py==10)||(px==4&&py==16)||(px==10&&py==4)||(px==10&&py==10)||(px==10&&py==16)||(px==16&&py==4)||(px==16&&py==10)||(px==16&&py==16))) {
            g.setColor(myColor);
            g.fillRect(0,0,23,23);
            g.setColor(Color.black);
            g.drawLine(0,8,23,8);
            g.drawLine(8,0,8,23);
            g.fillOval(5,5,6,6);
        }
        else if(color==0) {
            g.setColor(myColor);
            g.fillRect(0,0,23,23);
            g.setColor(Color.black);
            g.drawLine(0,8,23,8);
            g.drawLine(8,0,8,23);
        }
    }

    //����������
    void doSingle() {//���ڵİ汾��ѡ����Ļ�AI�����Լ����Լ��� ͬʱ��ô���û��������⣿������������  ����һ��������
        if(this.yourChoice==0){//��ѡ�����Ļ�
            //������
            if(this.colorFlag){
                this.panelPAR.doStep(this.yellowPoint,1);
            }
            else
                this.panelPAR.AI();//AI����
        }else{
            //AI����
            if(this.colorFlag)//AI����
                this.panelPAR.AI();//AI����
            else{
                this.panelPAR.doStep(this.yellowPoint,2);
            }
        }
        if(!this.panelPAR.errorFlag) {//�����Υ��������ɫ����AI
            this.colorFlag = !this.colorFlag;
            this.paintThisColor(this.colorFlag);
            if((this.yourChoice==0&&!this.colorFlag)||(this.yourChoice==1&&this.colorFlag)){//��������ߵ� ��ô���Լ�����Ȼ���� ��ס������flagȡ����
                this.doSingle();//������ֱ�ӵ�AI
            }
        } else {
            this.panelPAR.errorFlag=false;
        }
        this.yellowPoint.x=1000;//���ߵ��Ӳ�����ɾ��
        this.yellowPoint.y=1000;
    }
    //�������߲�
    void doMultiple() {
        int color;
        if(Objects.equals(this.selectBox.getSelectedItem(), "����"))
            color=1;
        else
            color=2;

        this.panelPAR.doStep(this.yellowPoint,color);
        //����߷����ԣ�����
        if(this.panelPAR.errorFlag) {
            this.panelPAR.errorFlag=false;
            return;
        }
        this.panelPAR.setEnabled(false);
        String message=this.getMessage(color,this.yellowPoint.x,this.yellowPoint.y,0);
        this.writer.println(message);
        isFirst = true;
        this.yellowPoint.x=99;//���ߵ��Ӳ�����ɾ������Ҫ���Խ���
        this.yellowPoint.y=99;
    }
    //�������ַ���*
    String getMessage(int color,int x,int y,int addOrDel) {//addordel-0��1ɾ
        String strColor=String.valueOf(color);
        String strX;
        String strY;
        //��������Ļ����ͼ�һλ
        if(x<10)
            strX="0"+String.valueOf(x);
        else
            strX=String.valueOf(x);

        if(y<10)
            strY="0"+String.valueOf(y);
        else
            strY=String.valueOf(y);

        return strColor+strX+strY+addOrDel;
    }


    void dealBtnMultiple(ActionEvent e) {
        this.enableLink();//�����������
        this.isLink = true;
    }

    void dealBtnSingle(ActionEvent e) {
        this.isSingleFirst = true;
        this.disableLink();//�����������
        this.selectBox.setEnabled(true);
    }

    void connectBtn_actionPerformed(ActionEvent e) {
        this.goToLink(this.IPTextField.getText().trim(), this.PORT);//ִ������
        isConnectBtnClicked = true;


    }
    //����serverSocket
    void goToLink(String hostName, int port) {
        try {
            this.stopFlag=true;
            this.sendSocket=new Socket(hostName,port);
            this.panelPAR.repaint();
            this.panelPAR.showError("���ӳɹ�����������ʼ��ť");
//            isConnectBtnClicked = true;
            this.selectBox.setEnabled(true);
            this.connectBtn.setEnabled(false);
            this.radioButtonSingle.setEnabled(false);
            this.radioButtonMultiple.setEnabled(false);
            this.IPTextField.setEnabled(false);
            this.writer=new PrintWriter(this.sendSocket.getOutputStream(),true);
            new IPHandler(sendSocket,this).start();
        }catch(IOException ioe){
            this.panelPAR.repaint();
            this.panelPAR.showError("�����ж�");}
    }

    //��ʼʱ������ɫ���Ƽ�������
    void paintMyColor() {
        Graphics g=this.flagQi.getGraphics();
        if(Objects.equals(this.selectBox.getSelectedItem(), "����"))
            g.fillOval(20,10,30,30);
        else {
            g.setColor(Color.white);
            g.fillOval(20,10,30,30);
            g.setColor(Color.black);
            g.drawOval(20,10,30,30);
        }
    }
    //������
    void paintThisColor(boolean whatColor) {
        Graphics g = this.flagQi.getGraphics();
        if(whatColor)
            g.fillOval(20,10,30,30);
        else {
            g.setColor(Color.white);
            g.fillOval(20,10,30,30);
            g.setColor(Color.black);
            g.drawOval(20,10,30,30);
        }
    }

    void windowClose(WindowEvent e) {//�ر�/�˳�
        this.dispose();
        System.exit(0);
    }
    //����Լ�������е��ӣ�Ȼ�����ڵ�����������ȡ������ȷ�ĻƵ�λ�� ��������Ϊ�Ƶ�ŵ������� ���Զ���ȥ����

}
