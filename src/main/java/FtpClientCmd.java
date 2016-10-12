import models.requests.GetRequest;
import models.requests.ListRequest;
import models.responses.GetResponse;
import models.responses.ListResponse;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class FtpClientCmd {
    public static void main(String[] args) {
        FtpClientImpl ftp = new FtpClientImpl();
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
                    GetResponse getResponse = ftp.executeGet(new GetRequest(s.next()));
                    if (getResponse == null) {
                        System.out.println("We receive empty answer");
                    } else {
                        System.out.println("Enter file destination:");
                        String fileTarget = s.next();
                        try {
                            FileOutputStream targetFile = new FileOutputStream(fileTarget);
                            targetFile.write(getResponse.getBytes());
                            targetFile.close();
                            System.out.println("The file is recorded.");
                        } catch (FileNotFoundException e) {
                            System.out.println("Exception: file not found: " + e.getMessage());
                        } catch (IOException e) {
                            System.out.println("IOException: " + e.getMessage());
                        }

                    }
                    break;
                case "list":
                    ListResponse listResponse = ftp.executeList(new ListRequest(s.next()));
                    if (listResponse != null) {
                        System.out.println("List:\n" + listResponse.toString());
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
