import models.requests.GetRequest;
import models.requests.ListRequest;
import models.responses.GetResponse;
import models.responses.ListResponse;

import java.util.Scanner;

public class FtpClientCmd {
    public static void main(String[] args) {
        FtpClientImpl ftp = new FtpClientImpl();
        String mode;
        Scanner s = new Scanner(System.in);
        while (true) {
            System.out.printf("Enter command: ");
            mode = s.next();
            switch (mode) {
                case "connect":
                    ftp.connect();
                    break;
                case "get":
                    GetResponse getResponse = ftp.executeGet(new GetRequest(s.next()));
                    if (getResponse != null) {
                        System.out.println("Get:" + getResponse.toString());
                    }
                    break;
                case "list":
                    ListResponse listResponse = ftp.executeList(new ListRequest(s.next()));
                    System.out.println("List: " + listResponse.toString());
                    break;
                default:
                    System.out.println("command = " + mode);
            }
        }
    }
}
