package weiQi;
import java.awt.*;

public class pieceStruct
{
    int BLACK=1;
    int WHITE=2;
    int BLANK=0;
    Point OUT=new Point(-1,-1);

    int whichStep;
    int color=BLANK;//��
    boolean isthere=false;
    Point[] pointAround ={OUT,OUT,OUT,OUT};//���������ĸ���
    int posX;//�������ڵ�
    int posY;

    int isCount = 0;
}