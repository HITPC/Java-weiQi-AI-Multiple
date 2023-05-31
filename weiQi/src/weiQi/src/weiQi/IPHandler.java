package weiQi;
import java.io.*;
import java.net.*;

public class IPHandler extends Thread
{
    Socket socket;
    MainWindow mainWindow;
    public IPHandler(Socket socket, MainWindow mainWindow) {
        this.socket=socket;
        this.mainWindow=mainWindow;
    }
    public void run() {
        try
        {
            this.activeListen(this.socket);
        }catch(IOException ioe){this.mainWindow.panelPAR.showError("“‚Õ‚÷–∂œ");}
    }
    void activeListen(Socket socket) throws IOException {
        BufferedReader reader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String message;
        while(true)
        {
            message=reader.readLine();
            this.mainWindow.doMessage(message);
        }
    }
}