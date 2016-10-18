import java.util.Scanner;

public class FtpServerCmd {
    public static void main(String[] args) {
        FtpServerImpl ftp = new FtpServerImpl();
        String mode;
        Scanner s = new Scanner(System.in);

        boolean serverIsRunnig = true;
        while (serverIsRunnig) {
            System.out.printf("Enter command: ");
            mode = s.next();
            switch (mode) {
                case "start":
                    System.out.println("Server starts");
                    ftp.serverStart();
                    break;
                case "stop":
                    System.out.println("Server stops");
                    ftp.serverStop();
                    serverIsRunnig = false;
                    break;
            }
        }
    }
}
