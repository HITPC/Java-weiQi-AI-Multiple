package weiQi;
import java.awt.*;

public class pieceStruct
{
    int BLACK=1;
    int WHITE=2;
    int BLANK=0;
    Point OUT=new Point(-1,-1);

    int whichStep;
    int color=BLANK;//空
    boolean isthere=false;
    Point[] pointAround ={OUT,OUT,OUT,OUT};//上下左右四个点
    int posX;//定义相邻点
    int posY;

    int isCount = 0;
}