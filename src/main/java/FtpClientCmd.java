import ftp.FtpClient;
import models.requests.GetRequest;
import models.requests.ListRequest;
import models.responses.GetResponse;
import models.responses.ListResponse;

import java.util.Scanner;

public class FtpClientCmd {
    public static void main(String[] args) {
        FtpClient ftp = new FtpClientImpl();
        String mode;
        Scanner s = new Scanner(System.in);
        boolean activeConnection = true;
        while (activeConnection) {
            System.out.printf("Enter command: ");
            mode = s.next();
            switch (mode) {
                case "connect":
                    ftp.connect();
                    break;
                case "get":
                    System.out.println("Enter file destination:");
                    GetResponse getResponse = ftp.executeGet(new GetRequest(s.next()));

                    String fileTarget = s.next();
                    if (getResponse == null) {
                        System.out.println("We receive empty answer");
                    } else {
                        getResponse.toFile(fileTarget);
                        System.out.println("The file is recorded.");
                    }
                    break;
                case "list":
                    ListResponse listResponse = ftp.executeList(new ListRequest(s.next()));
                    if (listResponse != null) {
                        System.out.println("List:\n" + listResponse);
                    }
                    break;
                case "disconnect":
                    ftp.disconnect();
                    activeConnection = false;
                    break;
                default:
                    System.out.println("unexpected command = " + mode);
            }
        }
    }
}
