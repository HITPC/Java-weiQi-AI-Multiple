package weiQi;
import java.awt.*;
import java.sql.Struct;
import java.util.*;

public class panelAndRule extends Panel
{
    int whichStep;
    Hashtable<Point, pieceStruct> boardHash;//���̹�ϣ��
    Hashtable<Point, Integer> valueHash = new Hashtable<>();//ÿ��λ�õ�Ȩֵ�Ĺ�ϣ��Խ�����������Խ�á���update��ɾ��...���漰��Ҫ�޸ģ�
    Point pointNow;//��ǰ�ĵ�
    Point STARTPOINT;//��ʼ��
    int INTERVAL;//�ȸ߾�
    Vector<Point> vec;//����ʸ��
    Point robPoint;//��ٵ�
    Point mousePoint;//����
    boolean errorFlag;//��������־
    int AIColor;//AI����ɫ
    Stack<Point> pointNowStack;//����ÿһ������λ��


    public panelAndRule() {
        super();
        pointNow=new Point(1000,1000);//�ѳ�ʼ��㻭������
        errorFlag=false;//��������־
        whichStep=0;
        STARTPOINT=new Point(20,20);
        INTERVAL=30;//���̼��
        boardHash= new Hashtable<>();
        robPoint=null;//��ٵ�
        mousePoint=new Point();//���������ڴ�
        vec=new Vector<>();//���У�����
        this.initHash(STARTPOINT, INTERVAL);
        try {
            init();
        }
        catch(Exception e) {
            e.printStackTrace();
        }

    }
    //��ʼ��hashtable
    void initHash(Point startPoint,int interval) {
        pieceStruct pieceStruct;
        Point key;//�߼����־
        int i,j;
        for(i=1;i<=19;i++)
            for(j=1;j<=19;j++) {
                key=new Point(i,j);
                pieceStruct =new pieceStruct();
                pieceStruct.posX=startPoint.x+(i-1)*interval;
                pieceStruct.posY=startPoint.y+(j-1)*interval;
                //��ȡ���ڵ�
                pieceStruct.pointAround[0]=new Point(i,j-1);//��
                pieceStruct.pointAround[1]=new Point(i,j+1);//��
                pieceStruct.pointAround[2]=new Point(i-1,j);//��
                pieceStruct.pointAround[3]=new Point(i+1,j);//��
                if(i==1) pieceStruct.pointAround[2]= pieceStruct.OUT;//�����Χ
                if(i==19) pieceStruct.pointAround[3]= pieceStruct.OUT;//�ҵ���Χ
                if(j==1) pieceStruct.pointAround[0]= pieceStruct.OUT;//�ϵ���Χ
                if(j==19) pieceStruct.pointAround[1]= pieceStruct.OUT;//�µ���Χ

                boardHash.put(key, pieceStruct);

                //value��ϣ�ĳ�ʼ��
                if((i<9&&j>11)||(i>11&&j>11)||(i>11&&j<9)||(i<9&&j<9)){//�߽����ȼ��ϸ�
                    if(i!=19&&i!=1&&j!=19&&j!=1){
                        valueHash.put(new Point(i, j), 5);
                    }else{
                        valueHash.put(new Point(i, j), 1);
                    }
                }else {
                    valueHash.put(new Point(i, j), 2);
                }
            }
        this.pointNowStack = new Stack<>();
    }


    //��������
    public void paint(Graphics g) {
        Point startPoint=STARTPOINT;
        int interval=INTERVAL;
        this.paintChessboard(g,startPoint,interval);//���̵Ĳ���
        this.paintChessman(g,startPoint,interval);//���ӵĲ���
    }
    //������
    void paintChessboard(Graphics g,Point startPoint,int interval) {
        int pX=startPoint.x;
        int pY=startPoint.y;
        int LINELENGTH=interval*18;
        int i;
        for(i=0;i<19;i++) {
            g.drawLine(pX+i*interval,pY,pX+i*interval,pY+LINELENGTH);
            g.drawLine(pX,pY+i*interval,pX+LINELENGTH,pY+i*interval);//����
        }
        g.fillOval(pX+interval*3-4,pY+interval*3-4,(int)(interval-22),(int)(interval-22));
        g.fillOval(pX+interval*9-4,pY+interval*3-4,(int)(interval-22),(int)(interval-22));
        g.fillOval(pX+interval*15-4,pY+interval*3-4,(int)(interval-22),(int)(interval-22));
        g.fillOval(pX+interval*3-4,pY+interval*9-4,(int)(interval-22),(int)(interval-22));
        g.fillOval(pX+interval*9-4,pY+interval*9-4,(int)(interval-22),(int)(interval-22));
        g.fillOval(pX+interval*15-4,pY+interval*9-4,(int)(interval-22),(int)(interval-22));
        g.fillOval(pX+interval*3-4,pY+interval*15-4,(int)(interval-22),(int)(interval-22));
        g.fillOval(pX+interval*9-4,pY+interval*15-4,(int)(interval-22),(int)(interval-22));
        g.fillOval(pX+interval*15-4,pY+interval*15-4,(int)(interval-22),(int)(interval-22));//�Ÿ���

//        g.drawRect(pX-3,pY-3,546,546);//��������ο�
    }
    //������*
    void paintChessman(Graphics g,Point startPoint,int interval) {
        int pX=startPoint.x;
        int pY=startPoint.y;
        Enumeration<pieceStruct> enum2=boardHash.elements();
        while(enum2.hasMoreElements()) {
            pieceStruct pieceStruct = enum2.nextElement();
            if(pieceStruct.color!= pieceStruct.BLANK) {
                if(pieceStruct.color== pieceStruct.BLACK)
                    g.setColor(Color.black);
                else if(pieceStruct.color== pieceStruct.WHITE)
                    g.setColor(Color.white);
                else
                    break;
                g.fillOval(pieceStruct.posX-11, pieceStruct.posY-11,interval-8,interval-8);//���

                g.setColor(Color.black);
                g.drawOval(pieceStruct.posX-11, pieceStruct.posY-11,interval-8,interval-8);//��Ȧ
            }
        }
        g.setColor(Color.red);//�����
        g.fillOval(this.pointNow.x*30-18,this.pointNow.y*30-18,16,16);
    }
    //����ÿһ��
    void doStep(Point whatPoint, int whatColor) {

        //����������⣬����
        if(whatPoint.x<1||whatPoint.x>19||whatPoint.y<1||whatPoint.y>19) {
            this.showError("�������ڴ˴�");
            this.errorFlag=true;
            return;
        }
        //����������ӣ��򷵻�
        if((boardHash.get(whatPoint)).color!=0) {
            this.showError("�˴�������");
            this.errorFlag=true;
            return;
        }
        if(this.isRob(whatPoint)) {
            this.showError("�Ѿ����٣�����Ӧ��");
            this.errorFlag=true;
            return;
        }
        this.updateHash(whatPoint, whatColor);
        this.getRival(whatPoint, whatColor);

        //���û����Ҳû�м���
        if(!this.isLink(whatPoint,whatColor)&&!this.isLink(whatPoint,0)){
            this.showError("�˴����ɷ���");
            this.errorFlag=true;
            this.singleRemove(whatPoint);
            return;
        }
        this.pointNow.x=whatPoint.x;
        this.pointNow.y=whatPoint.y;//�õ���ǰ��
        Point temp = new Point(pointNow.x,pointNow.y);
        this.pointNowStack.push(temp);
        this.repaint();
    }

    //ȡ���ಢ�ж�ִ�г���*
    void getRival(Point whatPoint,int whatColor) {
        boolean removeFlag=false;//�ж���һ�����׳�û����
        pieceStruct pieceStruct;
        pieceStruct = (this.boardHash.get(whatPoint));
        Point[] otherPoint = pieceStruct.pointAround;
        int i;
        for(i=0;i<4;i++) {
            pieceStruct otherPieceStruct = (this.boardHash.get(otherPoint[i]));//�ٳ�����ʵ��
            if(!otherPoint[i].equals(pieceStruct.OUT))
                if(otherPieceStruct.color!= pieceStruct.BLANK&& otherPieceStruct.color!=whatColor) {
                    if(this.isRemove(otherPoint[i]))//�������
                        this.vec.clear();
                    else {
                        this.makeRobber(otherPoint[i]);//�Ե����ɽٵ�
                        this.doRemove();
                        this.vec.clear();//���
                        removeFlag=true;//����
                    }
                }
        }
        if(!removeFlag)
            this.robPoint=null;//���û���ӵĻ�������ٵ�

    }
    //�ж��Ƿ����ٲ�����
    boolean isRob(Point p) {
        if(this.robPoint==null)
            return false;
        return this.robPoint.x == p.x && this.robPoint.y == p.y;
    }
    //������ٵ�*
    void makeRobber(Point point) {
        if(this.vec.size()==1)
            this.robPoint=point;//�����´�ٵ�
        else
            this.robPoint=null;//�Զ���Ļ�������ٵ�
    }
    //�жϳ���
    boolean isRemove(Point point) {
        if(this.vec.contains(point))
            return false;
        if(this.isLink(point,0))//�����Ļ�
            return true;
        this.vec.add(point);//û�����ͼ��������
        pieceStruct pieceStruct;
        pieceStruct = (this.boardHash.get(point));
        Point[] otherPoint = pieceStruct.pointAround;
        int i;
        for(i=0;i<4;i++) {
            pieceStruct otherPieceStruct = (this.boardHash.get(otherPoint[i]));//�ٳ�ͬ��ʵ��
            if(!otherPoint[i].equals(pieceStruct.OUT))
                if(otherPieceStruct.color== pieceStruct.color)
                    if(this.isRemove(otherPoint[i]))//����ݹ�
                        return true;
        }
        return false;

    }
    //ִ������*
    void doRemove() {
        Enumeration<Point> enum2=this.vec.elements();
        while(enum2.hasMoreElements()) {
            Point point= enum2.nextElement();
            this.singleRemove(point);
        }
    }
    //��������
    void singleRemove(Point point) {
        pieceStruct pieceStruct = (this.boardHash.get(point));
        pieceStruct.isthere=false;
        updateValueHash(point, false, pieceStruct.color);//����Ȩֵ��ϣ��
        pieceStruct.color= pieceStruct.BLANK;
        Graphics g=this.getGraphics();
        g.clearRect(point.x*30-21,point.y*30-21,23,23);//ɾ�������ϵ���
    }

    //�ж�����
    boolean isLink(Point point,int color) {
        pieceStruct pieceStruct;
        pieceStruct = (this.boardHash.get(point));
        Point[] otherPoint = pieceStruct.pointAround;
        int i;
        for(i=0;i<4;i++) {
            pieceStruct otherPieceStruct = (this.boardHash.get(otherPoint[i]));
            if(!otherPoint[i].equals(pieceStruct.OUT))
                if(otherPieceStruct.color==color) {
                    return true;
                }
        }
        return false;
    }

    //ÿһ������boardHash
    void updateHash(Point whatPoint, int whatColor) {
        pieceStruct pieceStruct = (this.boardHash.get(whatPoint));
        pieceStruct.isthere=true;
        pieceStruct.color=whatColor;
        this.whichStep=this.whichStep+1;
        pieceStruct.whichStep=this.whichStep;
        //ͬʱ����valueBoard
        //1��2��
        updateValueHash(whatPoint, true, whatColor);
    }

    void handleUpdateHash(Point whatPoint, boolean isAdd, boolean isAI){//�ǲ���AI������
        Point temp;
        int x = whatPoint.x;
        int y = whatPoint.y;
        int leave;//��������Ȩֵ
        if(isAdd){
            //���������һȦ��Ȩֵ�������Լ���б�Խ���Ȩֵ��һ��
            if(isAI){
                leave = 2;
                if(x==19){
                    if(y==1){
                        temp = new Point(x-1, y);
                        valueHash.put(temp, valueHash.get(temp)+1);
                        temp = new Point(x, y+1);
                        valueHash.put(temp, valueHash.get(temp)+1);
                        temp = new Point(x, y+1);
                        valueHash.put(temp, valueHash.get(temp)+1);
                    }else if(y==19){
                        temp = new Point(x-1, y);
                        valueHash.put(temp, valueHash.get(temp)+1);
                        temp = new Point(x-1, y-1);
                        valueHash.put(temp, valueHash.get(temp)+1);
                        temp = new Point(x, y-1);
                        valueHash.put(temp, valueHash.get(temp)+1);
                    }else{
                        temp = new Point(x-1, y);
                        valueHash.put(temp, valueHash.get(temp)+1);
                        temp = new Point(x-1, y+1);
                        valueHash.put(temp, valueHash.get(temp)+1);
                        temp = new Point(x, y+1);
                        valueHash.put(temp, valueHash.get(temp)+1);
                        temp = new Point(x-1, y-1);
                        valueHash.put(temp, valueHash.get(temp)+1);
                        temp = new Point(x, y-1);
                        valueHash.put(temp, valueHash.get(temp)+1);
                    }
                } else if(x==1){
                    if(y==1){
                        temp = new Point(x+1, y);
                        valueHash.put(temp, valueHash.get(temp)+1);
                        temp = new Point(x+1, y+1);
                        valueHash.put(temp, valueHash.get(temp)+1);
                        temp = new Point(x, y+1);
                        valueHash.put(temp, valueHash.get(temp)+1);
                    }else if(y==19){
                        temp = new Point(x, y-1);
                        valueHash.put(temp, valueHash.get(temp)+1);
                        temp = new Point(x+1, y-1);
                        valueHash.put(temp, valueHash.get(temp)+1);
                        temp = new Point(x+1, y);
                        valueHash.put(temp, valueHash.get(temp)+1);
                    }else{
                        temp = new Point(x+1, y);
                        valueHash.put(temp, valueHash.get(temp)+1);
                        temp = new Point(x+1, y+1);
                        valueHash.put(temp, valueHash.get(temp)+1);
                        temp = new Point(x, y+1);
                        valueHash.put(temp, valueHash.get(temp)+1);
                        temp = new Point(x, y-1);
                        valueHash.put(temp, valueHash.get(temp)+1);
                        temp = new Point(x+1, y-1);
                        valueHash.put(temp, valueHash.get(temp)+1);
                    }
                }else{
                    if(y==1){
                        temp = new Point(x-1, y);
                        valueHash.put(temp, valueHash.get(temp)+1);
                        temp = new Point(x+1, y+1);
                        valueHash.put(temp, valueHash.get(temp)+1);
                        temp = new Point(x, y+1);
                        valueHash.put(temp, valueHash.get(temp)+1);
                        temp = new Point(x+1, y+1);
                        valueHash.put(temp, valueHash.get(temp)+1);
                        temp = new Point(x+1, y);
                        valueHash.put(temp, valueHash.get(temp)+1);
                    }else if(y==19){
                        temp = new Point(x-1, y);
                        valueHash.put(temp, valueHash.get(temp)+1);
                        temp = new Point(x-1, y-1);
                        valueHash.put(temp, valueHash.get(temp)+1);
                        temp = new Point(x, y-1);
                        valueHash.put(temp, valueHash.get(temp)+1);
                        temp = new Point(x+1, y-1);
                        valueHash.put(temp, valueHash.get(temp)+1);
                        temp = new Point(x+1, y);
                        valueHash.put(temp, valueHash.get(temp)+1);
                    }else{
                        //����б�Խ�����
                        if(x-2>=1){
                            temp = new Point(x-2, y);
                            valueHash.put(temp, valueHash.get(temp)+2+leave);
                        }
                        if(y+2<=19){
                            temp = new Point(x, y+2);
                            valueHash.put(temp, valueHash.get(temp)+2+leave);
                        }
                        if(y-2>=1){
                            temp = new Point(x, y-2);
                            valueHash.put(temp, valueHash.get(temp)+2+leave);
                        }
                        if(x+2<=19){
                            temp = new Point(x+2, y);
                            valueHash.put(temp, valueHash.get(temp)+2+leave);
                        }
                        temp = new Point(x+1, y);
                        valueHash.put(temp, valueHash.get(temp)+1);
                        temp = new Point(x+1, y+1);
                        valueHash.put(temp, valueHash.get(temp)+1);
                        temp = new Point(x, y+1);
                        valueHash.put(temp, valueHash.get(temp)+1);
                        temp = new Point(x, y-1);
                        valueHash.put(temp, valueHash.get(temp)+1);
                        temp = new Point(x+1, y-1);
                        valueHash.put(temp, valueHash.get(temp)+1);
                        temp = new Point(x-1, y);
                        valueHash.put(temp, valueHash.get(temp)+1);
                        temp = new Point(x-1, y-1);
                        valueHash.put(temp, valueHash.get(temp)+1);
                        temp = new Point(x-1, y+1);
                        valueHash.put(temp, valueHash.get(temp)+1);
                    }
                }
            } else {//����AI������£���б�Ű��Է������ȼ���
                leave = 3;
                if(x!=19&&x!=1){
                    if(y==1){
                        temp = new Point(x-1, y);
                        valueHash.put(temp, valueHash.get(temp)+2);
                        temp = new Point(x+1, y+1);
                        valueHash.put(temp, valueHash.get(temp)+2);
                        temp = new Point(x, y+1);
                        valueHash.put(temp, valueHash.get(temp)+2);
                        temp = new Point(x+1, y+1);
                        valueHash.put(temp, valueHash.get(temp)+2);
                        temp = new Point(x+1, y);
                        valueHash.put(temp, valueHash.get(temp)+2);
                    }else if(y==19){
                        temp = new Point(x-1, y);
                        valueHash.put(temp, valueHash.get(temp)+2);
                        temp = new Point(x-1, y-1);
                        valueHash.put(temp, valueHash.get(temp)+2);
                        temp = new Point(x, y-1);
                        valueHash.put(temp, valueHash.get(temp)+2);
                        temp = new Point(x+1, y-1);
                        valueHash.put(temp, valueHash.get(temp)+2);
                        temp = new Point(x+1, y);
                        valueHash.put(temp, valueHash.get(temp)+2);
                    }else{
                        //����б�Խ�����
                        if(x-2>=1){
                            temp = new Point(x-2, y);
                            valueHash.put(temp, valueHash.get(temp)+2+leave);
                        }
                        if(y+2<=19){
                            temp = new Point(x, y+2);
                            valueHash.put(temp, valueHash.get(temp)+2+leave);
                        }
                        if(y-2>=1){
                            temp = new Point(x, y-2);
                            valueHash.put(temp, valueHash.get(temp)+2+leave);
                        }
                        if(x+2<=19){
                            temp = new Point(x+2, y);
                            valueHash.put(temp, valueHash.get(temp)+2+leave);
                        }
                        temp = new Point(x+1, y);
                        valueHash.put(temp, valueHash.get(temp)+2);
                        temp = new Point(x+1, y+1);
                        valueHash.put(temp, valueHash.get(temp)+2);
                        temp = new Point(x, y+1);
                        valueHash.put(temp, valueHash.get(temp)+2);
                        temp = new Point(x, y-1);
                        valueHash.put(temp, valueHash.get(temp)+2);
                        temp = new Point(x+1, y-1);
                        valueHash.put(temp, valueHash.get(temp)+2);
                        temp = new Point(x-1, y);
                        valueHash.put(temp, valueHash.get(temp)+2);
                        temp = new Point(x-1, y-1);
                        valueHash.put(temp, valueHash.get(temp)+2);
                        temp = new Point(x-1, y+1);
                        valueHash.put(temp, valueHash.get(temp)+2);
                    }
                }
            }
        }else{//ɾ���ӵĻ�����ɾ��������Χ����Ȩֵ
            if(isAI){
                if(x==19){
                    if(y==1){
                        temp = new Point(x-1, y);
                        valueHash.put(temp, valueHash.get(temp)-1==0?1:valueHash.get(temp)-1);
                        temp = new Point(x, y+1);
                        valueHash.put(temp, valueHash.get(temp)-1==0?1:valueHash.get(temp)-1);
                        temp = new Point(x, y+1);
                        valueHash.put(temp, valueHash.get(temp)-1==0?1:valueHash.get(temp)-1);
                    }else if(y==19){
                        temp = new Point(x-1, y);
                        valueHash.put(temp, valueHash.get(temp)-1==0?1:valueHash.get(temp)-1);
                        temp = new Point(x-1, y-1);
                        valueHash.put(temp, valueHash.get(temp)-1==0?1:valueHash.get(temp)-1);
                        temp = new Point(x, y-1);
                        valueHash.put(temp, valueHash.get(temp)-1==0?1:valueHash.get(temp)-1);
                    }else{
                        temp = new Point(x-1, y);
                        valueHash.put(temp, valueHash.get(temp)-1==0?1:valueHash.get(temp)-1);
                        temp = new Point(x-1, y+1);
                        valueHash.put(temp, valueHash.get(temp)-1==0?1:valueHash.get(temp)-1);
                        temp = new Point(x, y+1);
                        valueHash.put(temp, valueHash.get(temp)-1==0?1:valueHash.get(temp)-1);
                        temp = new Point(x-1, y-1);
                        valueHash.put(temp, valueHash.get(temp)-1==0?1:valueHash.get(temp)-1);
                        temp = new Point(x, y-1);
                        valueHash.put(temp, valueHash.get(temp)-1==0?1:valueHash.get(temp)-1);
                    }
                } else if(x==1){
                    if(y==1){
                        temp = new Point(x+1, y);
                        valueHash.put(temp, valueHash.get(temp)-1==0?1:valueHash.get(temp)-1);
                        temp = new Point(x+1, y+1);
                        valueHash.put(temp, valueHash.get(temp)-1==0?1:valueHash.get(temp)-1);
                        temp = new Point(x, y+1);
                        valueHash.put(temp, valueHash.get(temp)-1==0?1:valueHash.get(temp)-1);
                    }else if(y==19){
                        temp = new Point(x, y-1);
                        valueHash.put(temp, valueHash.get(temp)-1==0?1:valueHash.get(temp)-1);
                        temp = new Point(x+1, y-1);
                        valueHash.put(temp, valueHash.get(temp)-1==0?1:valueHash.get(temp)-1);
                        temp = new Point(x+1, y);
                        valueHash.put(temp, valueHash.get(temp)-1==0?1:valueHash.get(temp)-1);
                    }else{
                        temp = new Point(x+1, y);
                        valueHash.put(temp, valueHash.get(temp)-1==0?1:valueHash.get(temp)-1);
                        temp = new Point(x+1, y+1);
                        valueHash.put(temp, valueHash.get(temp)-1==0?1:valueHash.get(temp)-1);
                        temp = new Point(x, y+1);
                        valueHash.put(temp, valueHash.get(temp)-1==0?1:valueHash.get(temp)-1);
                        temp = new Point(x, y-1);
                        valueHash.put(temp, valueHash.get(temp)-1==0?1:valueHash.get(temp)-1);
                        temp = new Point(x+1, y-1);
                        valueHash.put(temp, valueHash.get(temp)-1==0?1:valueHash.get(temp)-1);
                    }
                }else{
                    if(y==1){
                        temp = new Point(x-1, y);
                        valueHash.put(temp, valueHash.get(temp)-1==0?1:valueHash.get(temp)-1);
                        temp = new Point(x+1, y+1);
                        valueHash.put(temp, valueHash.get(temp)-1==0?1:valueHash.get(temp)-1);
                        temp = new Point(x, y+1);
                        valueHash.put(temp, valueHash.get(temp)-1==0?1:valueHash.get(temp)-1);
                        temp = new Point(x+1, y+1);
                        valueHash.put(temp, valueHash.get(temp)-1==0?1:valueHash.get(temp)-1);
                        temp = new Point(x+1, y);
                        valueHash.put(temp, valueHash.get(temp)-1==0?1:valueHash.get(temp)-1);
                    }else if(y==19){
                        temp = new Point(x-1, y);
                        valueHash.put(temp, valueHash.get(temp)-1==0?1:valueHash.get(temp)-1);
                        temp = new Point(x-1, y-1);
                        valueHash.put(temp, valueHash.get(temp)-1==0?1:valueHash.get(temp)-1);
                        temp = new Point(x, y-1);
                        valueHash.put(temp, valueHash.get(temp)-1==0?1:valueHash.get(temp)-1);
                        temp = new Point(x+1, y-1);
                        valueHash.put(temp, valueHash.get(temp)-1==0?1:valueHash.get(temp)-1);
                        temp = new Point(x+1, y);
                        valueHash.put(temp, valueHash.get(temp)-1==0?1:valueHash.get(temp)-1);
                    }else{
                        temp = new Point(x+1, y);
                        valueHash.put(temp, valueHash.get(temp)-1==0?1:valueHash.get(temp)-1);
                        temp = new Point(x+1, y+1);
                        valueHash.put(temp, valueHash.get(temp)-1==0?1:valueHash.get(temp)-1);
                        temp = new Point(x, y+1);
                        valueHash.put(temp, valueHash.get(temp)-1==0?1:valueHash.get(temp)-1);
                        temp = new Point(x, y-1);
                        valueHash.put(temp, valueHash.get(temp)-1==0?1:valueHash.get(temp)-1);
                        temp = new Point(x+1, y-1);
                        valueHash.put(temp, valueHash.get(temp)-1==0?1:valueHash.get(temp)-1);
                        temp = new Point(x-1, y);
                        valueHash.put(temp, valueHash.get(temp)-1==0?1:valueHash.get(temp)-1);
                        temp = new Point(x-1, y-1);
                        valueHash.put(temp, valueHash.get(temp)-1==0?1:valueHash.get(temp)-1);
                        temp = new Point(x-1, y+1);
                        valueHash.put(temp, valueHash.get(temp)-1==0?1:valueHash.get(temp)-1);
                    }
                }
            }
        }

    }

    int[] count() {
        int[] ans = {0, 0};
        int flag;
        Graphics g = this.getGraphics();
        pieceStruct pieceStruct;
        Point point;
        for(int i = 1; i <= 19; ++i) {
            for(int j = 1; j <= 19; ++j){
                flag = 0;
                for(int k = 0; i + k <= 19; k++){
                    point = new Point(i+k, j);
                    Point point1 = new Point(i,j);
                    pieceStruct pieceStruct1 = boardHash.get(point1);
                    pieceStruct = boardHash.get(point);
                    if(pieceStruct.color == pieceStruct.BLACK&&pieceStruct.isCount==0){ans[0]++;flag = 1;g.setColor(Color.black);g.fillRect(pieceStruct1.posX-11, pieceStruct1.posY-11,INTERVAL-7,INTERVAL-7);pieceStruct1.isCount=1;break;}
                    if(pieceStruct.color == pieceStruct.WHITE&&pieceStruct.isCount==0){ans[1]++;flag = 1;g.setColor(Color.white);g.fillRect(pieceStruct1.posX-11, pieceStruct1.posY-11,INTERVAL-7,INTERVAL-7);pieceStruct1.isCount=1;break;}
                }
                if(flag == 1){continue;}
                for(int k = 1; i - k >= 1; k++){
                    point = new Point(i - k, j);
                    Point point1 = new Point(i,j);
                    pieceStruct pieceStruct1 = boardHash.get(point1);
                    pieceStruct = boardHash.get(point);
                    if (pieceStruct.color == pieceStruct.BLACK&&pieceStruct.isCount==0){ans[0]++;flag = 1;g.setColor(Color.black);g.fillRect(pieceStruct1.posX-11, pieceStruct1.posY-11,INTERVAL-7,INTERVAL-7);pieceStruct1.isCount=1;break;}
                    if (pieceStruct.color == pieceStruct.WHITE&&pieceStruct.isCount==0){ans[1]++;flag = 1;g.setColor(Color.white);g.fillRect(pieceStruct1.posX-11, pieceStruct1.posY-11,INTERVAL-7,INTERVAL-7);pieceStruct1.isCount=1;break;}
                }
                if(flag == 1)continue;
                for(int k = 0; j + k <= 19; k++){
                    point = new Point(i, j+k);
                    Point point1 = new Point(i,j);
                    pieceStruct pieceStruct1 = boardHash.get(point1);
                    pieceStruct = boardHash.get(point);
                    if(pieceStruct.color == pieceStruct.BLACK&&pieceStruct.isCount==0){ans[0]++;flag = 1;g.setColor(Color.black);g.fillRect(pieceStruct1.posX-11, pieceStruct1.posY-11,INTERVAL-7,INTERVAL-7);pieceStruct1.isCount=1;break;}
                    if(pieceStruct.color == pieceStruct.WHITE&&pieceStruct.isCount==0){ans[1]++;flag = 1;g.setColor(Color.white);g.fillRect(pieceStruct1.posX-11, pieceStruct1.posY-11,INTERVAL-7,INTERVAL-7);pieceStruct1.isCount=1;break;}
                }
                if(flag == 1)continue;
                for(int k = 1; j - k >= 1; k++){
                    point = new Point(i, j-k);
                    Point point1 = new Point(i,j);
                    pieceStruct pieceStruct1 = boardHash.get(point1);
                    pieceStruct = boardHash.get(point);
                    if(pieceStruct.color == pieceStruct.BLACK&&pieceStruct.isCount==0){ans[0]++;flag = 1;g.setColor(Color.black);g.fillRect(pieceStruct1.posX-11, pieceStruct1.posY-11,INTERVAL-7,INTERVAL-7);pieceStruct1.isCount=1;break;}
                    if(pieceStruct.color == pieceStruct.WHITE&&pieceStruct.isCount==0){ans[1]++;flag = 1;g.setColor(Color.white);g.fillRect(pieceStruct1.posX-11, pieceStruct1.posY-11,INTERVAL-7,INTERVAL-7);pieceStruct1.isCount=1;break;}
                }
            }
        }
        return ans;
    }
    void updateValueHash(Point whatPoint, boolean addOrDel, int whatColor){//���ӻ�������
        //���µ�ʱ��һ����£�������Χ�ĵ�Ҳ���Լӽ�ȥ���ȼ�������Ҫ����һ��
        //����valueBoard
        if(addOrDel){
            this.valueHash.put(whatPoint, 0);//�����ϵ��ˣ�����Ϊ0�����ȼ���Ͳ��������
            handleUpdateHash(whatPoint, addOrDel, whatColor==this.AIColor);
        }else {//��ȥ���ˣ��ָ���Ȩֵ
            this.valueHash.put(whatPoint, 1);
            handleUpdateHash(whatPoint, addOrDel, whatColor==this.AIColor);
        }

    }


    //��������������߼���λ��
    //p1Ϊ��ʵ�㣬p2Ϊ���ԭ��
    Point getMousePoint(Point p1,Point p2) {
        this.mousePoint.x=Math.round((float)(p1.x-p2.x)/this.INTERVAL);
        this.mousePoint.y=Math.round((float)(p1.y-p2.y)/this.INTERVAL);
        return this.mousePoint;
    }

    // ������ʱ��
    Timer timer = new Timer();
    // ������ʱ������

    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            repaint();
        }
    };
    void initTimerTask()
    {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                repaint();
            }
        };
    }
    //��ʾ������Ϣ*
    void showError(String errorMessage) {
        Graphics g=this.getGraphics();//ͼ��������
        g.setColor(Color.red);
        g.drawString(errorMessage,60,615);//���ƴ��ı��ĵ�����������
        g.setFont(new Font("Microsoft YaHei", Font.PLAIN, 18));
//        g.fillOval(20,600,30,30);//������ָ�����ο����Բ����Բ���Ͻ����꣬��Բ��ߣ�
        timerTask.cancel();
        initTimerTask();
        timer.schedule(timerTask, 1800);
    }
    private void init() throws Exception {
        this.setBackground(new Color(248, 173, 10));
    }

    boolean judge(Point whatPoint, int AIColor){
        //����������ӣ��򷵻�
        if((boardHash.get(whatPoint)).color!=0) {
            return true;
        }
        if(this.isRob(whatPoint)) {//��ٵ�
            return true;
        }
        //û����
        return !this.isLink(whatPoint, AIColor) && !this.isLink(whatPoint, 0);
    }

    int GetRandomNum(int num1, int num2) {
        Random rand = new Random();
        return rand.nextInt(num2 - num1 + 1) + num1;
    }

    static class keyVal {
        Point point;
        int value;
        public keyVal(){}
        public keyVal(Point p, int v){
            point = p;
            value = v;
        }
    }

    Point GetBestMove(int count) {
        //����ϣ��������ת��������
        ArrayList<keyVal> temp = new ArrayList<>();
        for (Map.Entry<Point, Integer> t : valueHash.entrySet()) {
            Point point = t.getKey();
            Integer value = t.getValue();
            temp.add(new keyVal(point, value));
        }
        //��������
        temp.sort((o1, o2) -> o2.value - o1.value);
        //ȡ�����Ž�
        int max = temp.size();
        int r = GetRandomNum(0, 26);
        count+=r;
        if(count<max){
            return temp.get(count).point;
        }
        return new Point(GetRandomNum(1, 19), GetRandomNum(1, 19));
    }
    void setAIColor(int c){
        this.AIColor = c;
    }

    public void AI(){//AIִ���߲�
        Point AIChoice;
        int count = -1;
        //��ô��AI�����㣿
        do{
            ++count;
            AIChoice = GetBestMove(count);
        }while(judge(AIChoice, AIColor));

        //���㵱ǰ�Ž�
        this.doStep(AIChoice, AIColor);
    }
}
