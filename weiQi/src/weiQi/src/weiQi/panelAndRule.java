package weiQi;
import java.awt.*;
import java.sql.Struct;
import java.util.*;

public class panelAndRule extends Panel
{
    int whichStep;
    Hashtable<Point, pieceStruct> boardHash;//棋盘哈希表
    Hashtable<Point, Integer> valueHash = new Hashtable<>();//每个位置的权值的哈希表，越大代表下这里越好。（update，删除...都涉及到要修改）
    Point pointNow;//当前的点
    Point STARTPOINT;//起始点
    int INTERVAL;//等高距
    Vector<Point> vec;//定义矢量
    Point robPoint;//打劫点
    Point mousePoint;//鼠标点
    boolean errorFlag;//行棋错误标志
    int AIColor;//AI的颜色
    Stack<Point> pointNowStack;//储存每一步具体位置


    public panelAndRule() {
        super();
        pointNow=new Point(1000,1000);//把初始红点画在外面
        errorFlag=false;//行棋错误标志
        whichStep=0;
        STARTPOINT=new Point(20,20);
        INTERVAL=30;//棋盘间距
        boardHash= new Hashtable<>();
        robPoint=null;//打劫点
        mousePoint=new Point();//开辟鼠标点内存
        vec=new Vector<>();//存放校验的子
        this.initHash(STARTPOINT, INTERVAL);
        try {
            init();
        }
        catch(Exception e) {
            e.printStackTrace();
        }

    }
    //初始化hashtable
    void initHash(Point startPoint,int interval) {
        pieceStruct pieceStruct;
        Point key;//逻辑点标志
        int i,j;
        for(i=1;i<=19;i++)
            for(j=1;j<=19;j++) {
                key=new Point(i,j);
                pieceStruct =new pieceStruct();
                pieceStruct.posX=startPoint.x+(i-1)*interval;
                pieceStruct.posY=startPoint.y+(j-1)*interval;
                //获取相邻点
                pieceStruct.pointAround[0]=new Point(i,j-1);//上
                pieceStruct.pointAround[1]=new Point(i,j+1);//下
                pieceStruct.pointAround[2]=new Point(i-1,j);//左
                pieceStruct.pointAround[3]=new Point(i+1,j);//右
                if(i==1) pieceStruct.pointAround[2]= pieceStruct.OUT;//左的周围
                if(i==19) pieceStruct.pointAround[3]= pieceStruct.OUT;//右的周围
                if(j==1) pieceStruct.pointAround[0]= pieceStruct.OUT;//上的周围
                if(j==19) pieceStruct.pointAround[1]= pieceStruct.OUT;//下的周围

                boardHash.put(key, pieceStruct);

                //value哈希的初始化
                if((i<9&&j>11)||(i>11&&j>11)||(i>11&&j<9)||(i<9&&j<9)){//边角优先级较高
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


    //更新盘面
    public void paint(Graphics g) {
        Point startPoint=STARTPOINT;
        int interval=INTERVAL;
        this.paintChessboard(g,startPoint,interval);//棋盘的操作
        this.paintChessman(g,startPoint,interval);//棋子的操作
    }
    //画棋盘
    void paintChessboard(Graphics g,Point startPoint,int interval) {
        int pX=startPoint.x;
        int pY=startPoint.y;
        int LINELENGTH=interval*18;
        int i;
        for(i=0;i<19;i++) {
            g.drawLine(pX+i*interval,pY,pX+i*interval,pY+LINELENGTH);
            g.drawLine(pX,pY+i*interval,pX+LINELENGTH,pY+i*interval);//网格
        }
        g.fillOval(pX+interval*3-4,pY+interval*3-4,(int)(interval-22),(int)(interval-22));
        g.fillOval(pX+interval*9-4,pY+interval*3-4,(int)(interval-22),(int)(interval-22));
        g.fillOval(pX+interval*15-4,pY+interval*3-4,(int)(interval-22),(int)(interval-22));
        g.fillOval(pX+interval*3-4,pY+interval*9-4,(int)(interval-22),(int)(interval-22));
        g.fillOval(pX+interval*9-4,pY+interval*9-4,(int)(interval-22),(int)(interval-22));
        g.fillOval(pX+interval*15-4,pY+interval*9-4,(int)(interval-22),(int)(interval-22));
        g.fillOval(pX+interval*3-4,pY+interval*15-4,(int)(interval-22),(int)(interval-22));
        g.fillOval(pX+interval*9-4,pY+interval*15-4,(int)(interval-22),(int)(interval-22));
        g.fillOval(pX+interval*15-4,pY+interval*15-4,(int)(interval-22),(int)(interval-22));//九个点

//        g.drawRect(pX-3,pY-3,546,546);//棋盘外矩形框
    }
    //加棋子*
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
                g.fillOval(pieceStruct.posX-11, pieceStruct.posY-11,interval-8,interval-8);//填充

                g.setColor(Color.black);
                g.drawOval(pieceStruct.posX-11, pieceStruct.posY-11,interval-8,interval-8);//黑圈
            }
        }
        g.setColor(Color.red);//画红点
        g.fillOval(this.pointNow.x*30-18,this.pointNow.y*30-18,16,16);
    }
    //处理每一步
    void doStep(Point whatPoint, int whatColor) {

        //如果点在盘外，返回
        if(whatPoint.x<1||whatPoint.x>19||whatPoint.y<1||whatPoint.y>19) {
            this.showError("不能下在此处");
            this.errorFlag=true;
            return;
        }
        //如果点上有子，则返回
        if((boardHash.get(whatPoint)).color!=0) {
            this.showError("此处已有子");
            this.errorFlag=true;
            return;
        }
        if(this.isRob(whatPoint)) {
            this.showError("已经开劫，请先应劫");
            this.errorFlag=true;
            return;
        }
        this.updateHash(whatPoint, whatColor);
        this.getRival(whatPoint, whatColor);

        //如果没有气也没有己类
        if(!this.isLink(whatPoint,whatColor)&&!this.isLink(whatPoint,0)){
            this.showError("此处不可放子");
            this.errorFlag=true;
            this.singleRemove(whatPoint);
            return;
        }
        this.pointNow.x=whatPoint.x;
        this.pointNow.y=whatPoint.y;//得到当前点
        Point temp = new Point(pointNow.x,pointNow.y);
        this.pointNowStack.push(temp);
        this.repaint();
    }

    //取异类并判断执行吃子*
    void getRival(Point whatPoint,int whatColor) {
        boolean removeFlag=false;//判断这一步到底吃没吃子
        pieceStruct pieceStruct;
        pieceStruct = (this.boardHash.get(whatPoint));
        Point[] otherPoint = pieceStruct.pointAround;
        int i;
        for(i=0;i<4;i++) {
            pieceStruct otherPieceStruct = (this.boardHash.get(otherPoint[i]));//举出异类实例
            if(!otherPoint[i].equals(pieceStruct.OUT))
                if(otherPieceStruct.color!= pieceStruct.BLANK&& otherPieceStruct.color!=whatColor) {
                    if(this.isRemove(otherPoint[i]))//如果有气
                        this.vec.clear();
                    else {
                        this.makeRobber(otherPoint[i]);//吃掉后变成劫点
                        this.doRemove();
                        this.vec.clear();//清空
                        removeFlag=true;//吃了
                    }
                }
        }
        if(!removeFlag)
            this.robPoint=null;//如果没吃子的话消掉打劫点

    }
    //判断是否因打劫不能下
    boolean isRob(Point p) {
        if(this.robPoint==null)
            return false;
        return this.robPoint.x == p.x && this.robPoint.y == p.y;
    }
    //建立打劫点*
    void makeRobber(Point point) {
        if(this.vec.size()==1)
            this.robPoint=point;//建立新打劫点
        else
            this.robPoint=null;//吃多个的话消掉打劫点
    }
    //判断吃子
    boolean isRemove(Point point) {
        if(this.vec.contains(point))
            return false;
        if(this.isLink(point,0))//有气的话
            return true;
        this.vec.add(point);//没有气就加入这个点
        pieceStruct pieceStruct;
        pieceStruct = (this.boardHash.get(point));
        Point[] otherPoint = pieceStruct.pointAround;
        int i;
        for(i=0;i<4;i++) {
            pieceStruct otherPieceStruct = (this.boardHash.get(otherPoint[i]));//举出同类实例
            if(!otherPoint[i].equals(pieceStruct.OUT))
                if(otherPieceStruct.color== pieceStruct.color)
                    if(this.isRemove(otherPoint[i]))//这里递归
                        return true;
        }
        return false;

    }
    //执行消子*
    void doRemove() {
        Enumeration<Point> enum2=this.vec.elements();
        while(enum2.hasMoreElements()) {
            Point point= enum2.nextElement();
            this.singleRemove(point);
        }
    }
    //消单个子
    void singleRemove(Point point) {
        pieceStruct pieceStruct = (this.boardHash.get(point));
        pieceStruct.isthere=false;
        updateValueHash(point, false, pieceStruct.color);//更新权值哈希表
        pieceStruct.color= pieceStruct.BLANK;
        Graphics g=this.getGraphics();
        g.clearRect(point.x*30-21,point.y*30-21,23,23);//删除画面上的子
    }

    //判断有气
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

    //每一步更新boardHash
    void updateHash(Point whatPoint, int whatColor) {
        pieceStruct pieceStruct = (this.boardHash.get(whatPoint));
        pieceStruct.isthere=true;
        pieceStruct.color=whatColor;
        this.whichStep=this.whichStep+1;
        pieceStruct.whichStep=this.whichStep;
        //同时更新valueBoard
        //1黑2白
        updateValueHash(whatPoint, true, whatColor);
    }

    void handleUpdateHash(Point whatPoint, boolean isAdd, boolean isAI){//是不是AI的落子
        Point temp;
        int x = whatPoint.x;
        int y = whatPoint.y;
        int leave;//辅助处理权值
        if(isAdd){
            //被放上棋的一圈的权值上升，自己的斜对角线权值高一点
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
                        //给上斜对角线上
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
            } else {//不是AI的情况下，当斜着包对方的优先级高
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
                        //给上斜对角线上
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
        }else{//删除子的话，被删除的子周围降低权值
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
    void updateValueHash(Point whatPoint, boolean addOrDel, int whatColor){//添子还是移子
        //更新的时候，一起更新，黑棋周围的点也可以加进去优先级，但是要特判一下
        //更新valueBoard
        if(addOrDel){
            this.valueHash.put(whatPoint, 0);//被放上点了，就置为0，优先级最低不允许放棋
            handleUpdateHash(whatPoint, addOrDel, whatColor==this.AIColor);
        }else {//被去掉了，恢复其权值
            this.valueHash.put(whatPoint, 1);
            handleUpdateHash(whatPoint, addOrDel, whatColor==this.AIColor);
        }

    }


    //用四舍五入计算逻辑点位置
    //p1为真实点，p2为相对原点
    Point getMousePoint(Point p1,Point p2) {
        this.mousePoint.x=Math.round((float)(p1.x-p2.x)/this.INTERVAL);
        this.mousePoint.y=Math.round((float)(p1.y-p2.y)/this.INTERVAL);
        return this.mousePoint;
    }

    // 创建定时器
    Timer timer = new Timer();
    // 创建定时器任务

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
    //显示错误信息*
    void showError(String errorMessage) {
        Graphics g=this.getGraphics();//图形上下文
        g.setColor(Color.red);
        g.drawString(errorMessage,60,615);//绘制此文本的迭代器，坐标
        g.setFont(new Font("Microsoft YaHei", Font.PLAIN, 18));
//        g.fillOval(20,600,30,30);//填充外接指定矩形框的椭圆（椭圆左上角坐标，椭圆宽高）
        timerTask.cancel();
        initTimerTask();
        timer.schedule(timerTask, 1800);
    }
    private void init() throws Exception {
        this.setBackground(new Color(248, 173, 10));
    }

    boolean judge(Point whatPoint, int AIColor){
        //如果点上有子，则返回
        if((boardHash.get(whatPoint)).color!=0) {
            return true;
        }
        if(this.isRob(whatPoint)) {//打劫点
            return true;
        }
        //没气点
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
        //将哈希表数据其转到数组中
        ArrayList<keyVal> temp = new ArrayList<>();
        for (Map.Entry<Point, Integer> t : valueHash.entrySet()) {
            Point point = t.getKey();
            Integer value = t.getValue();
            temp.add(new keyVal(point, value));
        }
        //降序排序
        temp.sort((o1, o2) -> o2.value - o1.value);
        //取得最优解
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

    public void AI(){//AI执行走步
        Point AIChoice;
        int count = -1;
        //怎么让AI聪明点？
        do{
            ++count;
            AIChoice = GetBestMove(count);
        }while(judge(AIChoice, AIColor));

        //计算当前优解
        this.doStep(AIChoice, AIColor);
    }
}
