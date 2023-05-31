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
    panelAndRule panelPAR =new panelAndRule();//用Go类声明并创建一个panelGo对象
    Image myImage;
    int PORT;
    int regretRes = -1;
    boolean isFirst = true;
    int isFirst2 = 0;
    boolean isConnectBtnClicked = false;
    boolean isLink = false;//联机标志
    boolean isHost = false;//主机标志
    Socket sendSocket;//主动连接Socket
    PrintWriter writer;//用来发送message
    boolean stopFlag;
    boolean isInitiative;//是否主动连接
    boolean isSingleFirst;//是不是单人版需要初始化
    Point messagePoint;
    Point goStartPoint=null;
    Point yellowPoint=null;
    boolean colorFlag = true;
    Point LastPoint=null;//移除黄点时，判断位置变动*
    BorderLayout JFrameBL = new BorderLayout();//边界布局
    JPanel mainPanel = new JPanel();
    JPanel messagePanel = new JPanel();
    BorderLayout mainpanelBL = new BorderLayout();
    JPanel btnPanel = new JPanel();
    ButtonGroup modeBtnGroup = new ButtonGroup();//选择框的分组
    JRadioButton radioButtonSingle = new JRadioButton("单机");
    JRadioButton radioButtonMultiple = new JRadioButton("联机");
    JLabel IPLabel = new JLabel("对方IP");
    TextField IPTextField = new TextField();
    JButton connectBtn = new JButton("连接");//“连接”按钮
    JLabel flagQi = new JLabel();
    String[] options = { "黑棋", "白棋"};//0, 1
    JComboBox<String> selectBox = new JComboBox<>(options);//选择黑白棋
    int yourChoice;//单机模式选择的棋子为什么
    JButton regretBtn = new JButton("悔棋");
    JButton startBtn = new JButton("开始");
    GridLayout btnPanelGL = new GridLayout();//布局处理器，矩形网格形式
    BorderLayout borderLayout3 = new BorderLayout();
    boolean isTiZi = false;//是不是提子阶段

    JPanel jp1 = new JPanel();//开始页面Jpanel
    JPanel jp2 = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Image image = new ImageIcon(System.getProperty("user.dir")+"\\weiQi\\media\\bgi.jpg").getImage();
            g.drawImage(image, 0, 0, this);
        }
    };//存放实质内容的Jpanel，并设置背景图
    JButton startBtn0 = new JButton("开始");//最初页面上的开始按钮
    JButton exitBtn = new JButton("退出");//最初页面上的退出按钮
    JLabel title = new JLabel("围棋游戏");//页面标题


    public MainWindow() {
        try {
            Init();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void Init() throws Exception {
        //设置颜色
        selectBox.setBackground(new Color(246, 234, 216));
        connectBtn.setBackground(new Color(246, 234, 216));
        this.setResizable(false);//不可由用户自己改窗口大小
        new Thread(this).start();//启动监听线程
        this.PORT=1977;//端口号
        this.isInitiative=false;//是否主动连接
        this.stopFlag=false;//是否继续监听的标志
        LastPoint=new Point();
        messagePoint=new Point();
        this.setSize(700,700);//宽高
        this.setTitle("围棋游戏");
        this.panelPAR.setEnabled(false);//开始之前屏蔽掉盘面
        radioButtonSingle.setOpaque(false);
        radioButtonMultiple.setOpaque(false);
        radioButtonSingle.addActionListener(this::dealBtnSingle);//处理单机radio
        radioButtonMultiple.addActionListener(this::dealBtnMultiple);//处理联机radio
        //将选择框放入分组中
        modeBtnGroup.add(radioButtonSingle);
        modeBtnGroup.add(radioButtonMultiple);

        this.goStartPoint=this.panelPAR.getLocation();
        this.setLayout(JFrameBL);
        mainPanel.setLayout(mainpanelBL);
        connectBtn.addActionListener(this::connectBtn_actionPerformed);//添加指定的操作侦听器，按下发生操作事件
        flagQi.setText("  ");
        startBtn.setBackground(new Color(246, 234, 216));
        startBtn.addActionListener(this::startBtn_actionPerformed);
        btnPanel.setLayout(btnPanelGL);//设置的布局管理器
        btnPanelGL.setRows(9);//9行
        btnPanelGL.setColumns(1);//1列
        btnPanelGL.setHgap(100);//组件间水平间距
        btnPanelGL.setVgap(8);//组件间垂直间距
        messagePanel.setLayout(borderLayout3);
        this.messagePanel.setSize(500,70);
        panelPAR.addMouseMotionListener(new MouseMotionAdapter() {//棋盘上的鼠标移动
            public void mouseMoved(MouseEvent e) {
                checkerBoardMove(e);
            }
        });

        panelPAR.addMouseListener(new MouseAdapter() {//棋盘点击
            public void mouseClicked(MouseEvent e) {
                checkerBoardClick(e);
            }
        });

        this.addWindowListener(new WindowAdapter() {//窗口关闭
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
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10,0,0,36));//调整btnPanel位置，使不贴边
        this.mainPanel.add(this.panelPAR, BorderLayout.CENTER);//把panelGo放到1里
        this.mainPanel.add(btnPanel, BorderLayout.EAST);//把btnPanel放入

        CardLayout cl = new CardLayout();
        jp1.setLayout(cl);//设置jp1的卡片布局管理器
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
        this.jp1.add("main", mainPanel);//将main加入jp1
        this.jp2.setLayout(new BoxLayout(jp2, BoxLayout.Y_AXIS));//设置布局管理器
        startBtn0.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("Microsoft YaHei", Font.BOLD, 40));
        title.setForeground(Color.red);
        this.jp2.add(Box.createVerticalStrut(30));//添加空组件 用于添加垂直间距
        this.jp2.add(title);
        this.jp2.add(Box.createVerticalStrut(180));//添加空组件 用于添加垂直间距
        this.jp2.add(startBtn0);
        this.jp2.add(Box.createVerticalStrut(20));//添加空组件 用于添加垂直间距
        this.jp2.add(exitBtn);
        this.jp1.add("welcomePage", jp2);//将jp2放进去
        this.add(messagePanel, BorderLayout.SOUTH);
        this.add(jp1, BorderLayout.CENTER);
        cl.show(jp1, "welcomePage");//设置为默认展示欢迎页

        this.disableLink();//禁用控件
        this.radioButtonSingle.doClick();//设置默认选择按钮1
        this.yellowPoint=new Point(1000,1000);//初始化标识当前鼠标位置的黄点
        this.centerWindow();

        this.setVisible(true);
        myImage=this.createImage(16,16);//(创建图像)用来纪录有黄点之前的图像

    }
    void centerWindow() {
        Dimension d=Toolkit.getDefaultToolkit().getScreenSize();
        int pX=(d.width-this.getWidth())/2;
        int pY=(d.height-this.getHeight())/2;
        this.setLocation(pX,pY);//移到新位置
    }
    //主方法
    public static void main(String[] args) {
        new MainWindow();
    }
    //监听线程
    public void run() {
        try {
            ServerSocket serverSocket=new ServerSocket(PORT);//ServerSocket此类实现服务器套接字，基于请求执行操作，后像请求者返回结果
            Socket receiveSocket;//Socket类实现客户端套接字
            receiveSocket=serverSocket.accept();
            if(this.isInitiative)//如果已在进行，则不接受连接
                this.stopFlag=true;
            this.radioButtonMultiple.setSelected(true);//自动选择联机*
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
                    this.panelPAR.showError("接收连接成功，请点击开始按钮！");
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
            this.panelPAR.showError("意外中断");}

    }

    //处理接收到的信息
    void doMessage(String message) {
        if(message.startsWith("start")){//判断开始
            this.panelPAR.repaint();
            this.panelPAR.showError("对方已开始");
            if(message.equals("start_black"))
                this.selectBox.setSelectedIndex(1);//select选中项设置为指定位置上的项
            else
                this.selectBox.setSelectedIndex(0);

            if(Objects.equals(this.selectBox.getSelectedItem(), "黑棋"))//只要你是黑的，就先走
                this.panelPAR.setEnabled(true);
            this.paintMyColor();//表明颜色
            this.disableLink();
        }
        else if(message.equals("regretreq")){
            regretRes = JOptionPane.showConfirmDialog(this,"要同意对方的悔棋请求吗？","对方请求悔棋",1,1);
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
        else{//下子的信息*
            int color=Integer.parseInt(message.substring(0,1));
            this.messagePoint.x=Integer.parseInt(message.substring(1,3));
            this.messagePoint.y=Integer.parseInt(message.substring(3,5));
            this.panelPAR.setEnabled(true);//解禁
            this.panelPAR.doStep(this.messagePoint,color);
        }
    }

    //为鼠标定位*
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
            this.yellowPoint.x=mousePoint.x;//定位黄点
            this.yellowPoint.y=mousePoint.y;
        }
    }

    //加黄点的范围
    boolean isPlace(Point p) {
        if(p.x>19||p.x<1||p.y<1||p.y>19)
            return false;
        int color;
        pieceStruct pieceStruct;
        pieceStruct = (this.panelPAR.boardHash.get(p));
        color= pieceStruct.color;
        return color == 0;
    }
    void checkerBoardClick(MouseEvent e) {//棋盘点击事件
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
    //悔棋
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
    //点击游戏之后开始
    void startBtn_actionPerformed(ActionEvent e) {
        if(e.getActionCommand().equals("开始")) {

            if(this.isSinglePlayer())
                this.panelPAR.setEnabled(true);
            else{//联机版时
                if(!isConnectBtnClicked&&isHost){
                    this.panelPAR.repaint();
                    this.panelPAR.showError("请先连接！");
                    return;
                }
                if(Objects.equals(this.selectBox.getSelectedItem(), "黑棋")) {
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
            this.startBtn.setText("点棋");
            this.paintMyColor();//表明颜色
            if(this.isSingleFirst&&!isLink){//选择棋子
                this.isSingleFirst = false;
                this.yourChoice = this.selectBox.getSelectedIndex();//0黑 1白
                this.panelPAR.setAIColor(this.yourChoice==0?2:1);
                if(this.yourChoice==1){//AI
                    doSingle();
                }
            }
        } else if(e.getActionCommand().equals("退出")) {
            this.dispose();
            System.exit(0);
        }else if(e.getActionCommand().equals("结束")){
            this.isTiZi = false;
            //这里判断到底多少子的逻辑
            int[] ans;
            String text;
            JDialog dialog = new JDialog(this,"结果",true);
            ans = this.panelPAR.count();
            JLabel jl=new JLabel("黑棋：" + ans[0]);
            JLabel jl2=new JLabel("白棋：" + ans[1]);
            if(ans[0]>=185)text = "黑棋胜";
            else if(ans[1]>=177)text = "白棋胜";
            else text = "还未完成对局";
            JLabel jl3=new JLabel(text);
            dialog.add(jl,BorderLayout.NORTH);
            dialog.add(jl2,BorderLayout.CENTER);
            dialog.add(jl3,BorderLayout.SOUTH);
            dialog.setBounds(200,200,200,150);
            dialog.setVisible(true);
            this.startBtn.setText("退出");//没必要 因为上面提示想要一个对话框，点击退出直接退出
        }else if(e.getActionCommand().equals("点棋")){
            this.isTiZi = true;
            this.startBtn.setText("结束");
        }
    }
    //disable联机时用的控件*
    void disableLink() {
        this.IPTextField.setBackground(new Color(246, 234, 216));
        this.IPTextField.setEnabled(false);//禁用
        this.selectBox.setEnabled(false);//禁用
        this.connectBtn.setEnabled(false);
    }
    //enable联机时的控件*
    void enableLink() {
        this.IPTextField.setBackground(Color.white);
        this.IPTextField.setEnabled(true);
        this.selectBox.setEnabled(true);
        this.connectBtn.setEnabled(true);
    }
    //判断类型 是否为单人模式
    boolean isSinglePlayer() {
        return this.radioButtonSingle.isSelected();
    }

    //加小黄点
    void showMousePoint(Point mousePoint) {

        Graphics g=this.panelPAR.getGraphics();
        g.setColor(Color.yellow);
        g.fillOval(mousePoint.x*30-21,mousePoint.y*30-21,this.panelPAR.INTERVAL-8,this.panelPAR.INTERVAL-8);
        this.yellowPoint.x=mousePoint.x;//定位黄点
        this.yellowPoint.y=mousePoint.y;
        Graphics myG=this.myImage.getGraphics();
        this.createMyImage(myG, this.yellowPoint,0);
    }
    //消除前一个黄点*
    void removeLastMousePoint(Point thatPoint,Point thisPoint) {
        if(thatPoint.x!=thisPoint.x||thatPoint.y!=thisPoint.y) {
            Graphics g=this.panelPAR.getGraphics();
            if(this.yellowPoint!=null&&this.myImage!=null&&this.isPlace(thatPoint))
                g.drawImage(this.myImage,this.yellowPoint.x*30-21,this.yellowPoint.y*30-21,22,22,null);
            assert this.yellowPoint != null;
        }
    }

    //生成图像Image
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
        else if(px==1&&color==0){//四个边
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
        //九个小黑点
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

    //单机版下棋
    void doSingle() {//现在的版本你选白棋的话AI不会自己调自己的 同时怎么调用还是有问题？？？？？？？  再来一个函数？
        if(this.yourChoice==0){//你选择黑棋的话
            //你先走
            if(this.colorFlag){
                this.panelPAR.doStep(this.yellowPoint,1);
            }
            else
                this.panelPAR.AI();//AI走棋
        }else{
            //AI先走
            if(this.colorFlag)//AI先走
                this.panelPAR.AI();//AI走棋
            else{
                this.panelPAR.doStep(this.yellowPoint,2);
            }
        }
        if(!this.panelPAR.errorFlag) {//如果不违例，则换颜色，调AI
            this.colorFlag = !this.colorFlag;
            this.paintThisColor(this.colorFlag);
            if((this.yourChoice==0&&!this.colorFlag)||(this.yourChoice==1&&this.colorFlag)){//如果是人走的 那么调自己，不然不调 记住这里是flag取反了
                this.doSingle();//你点击完直接调AI
            }
        } else {
            this.panelPAR.errorFlag=false;
        }
        this.yellowPoint.x=1000;//刚走的子不至于删掉
        this.yellowPoint.y=1000;
    }
    //联机版走步
    void doMultiple() {
        int color;
        if(Objects.equals(this.selectBox.getSelectedItem(), "黑棋"))
            color=1;
        else
            color=2;

        this.panelPAR.doStep(this.yellowPoint,color);
        //如果走法不对，返回
        if(this.panelPAR.errorFlag) {
            this.panelPAR.errorFlag=false;
            return;
        }
        this.panelPAR.setEnabled(false);
        String message=this.getMessage(color,this.yellowPoint.x,this.yellowPoint.y,0);
        this.writer.println(message);
        isFirst = true;
        this.yellowPoint.x=99;//刚走的子不至于删掉，还要可以解析
        this.yellowPoint.y=99;
    }
    //处理发送字符串*
    String getMessage(int color,int x,int y,int addOrDel) {//addordel-0增1删
        String strColor=String.valueOf(color);
        String strX;
        String strY;
        //如果单数的话，就加一位
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
        this.enableLink();//启用连接组件
        this.isLink = true;
    }

    void dealBtnSingle(ActionEvent e) {
        this.isSingleFirst = true;
        this.disableLink();//禁用连接组件
        this.selectBox.setEnabled(true);
    }

    void connectBtn_actionPerformed(ActionEvent e) {
        this.goToLink(this.IPTextField.getText().trim(), this.PORT);//执行连接
        isConnectBtnClicked = true;


    }
    //连接serverSocket
    void goToLink(String hostName, int port) {
        try {
            this.stopFlag=true;
            this.sendSocket=new Socket(hostName,port);
            this.panelPAR.repaint();
            this.panelPAR.showError("连接成功！！请点击开始按钮");
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
            this.panelPAR.showError("意外中断");}
    }

    //开始时根据颜色绘制己方棋子
    void paintMyColor() {
        Graphics g=this.flagQi.getGraphics();
        if(Objects.equals(this.selectBox.getSelectedItem(), "黑棋"))
            g.fillOval(20,10,30,30);
        else {
            g.setColor(Color.white);
            g.fillOval(20,10,30,30);
            g.setColor(Color.black);
            g.drawOval(20,10,30,30);
        }
    }
    //画棋子
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

    void windowClose(WindowEvent e) {//关闭/退出
        this.dispose();
        System.exit(0);
    }
    //添加自己点掉不行的子，然后现在的问题是鼠标获取不到正确的黄点位置 好像是因为黄点放到棋子上 会自动被去掉？

}
