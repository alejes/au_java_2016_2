import exceptions.VCSException;
import models.ModifyAction;
import utils.Cmd;

public class VCSMain {
    public static void main(String[] args) {
        if (args.length <= 0) {
            System.out.println("Start with creating your own repository by command:\nvcs init");
            return;
        }
        VCS vcs = new VCS();
        try {
            switch (args[0]) {
                case "init":
                    vcs.initRepository();
                    break;

                case "checkout":
                    Cmd.checkArgumentsLength(args, 2, "checkout require at least 1 arguments - branch name. " +
                            "You can use -b modificator for create");
                    switch (args.length) {
                        case 2:
                            vcs.checkout(args[1], false);
                            break;
                        case 3:
                            vcs.checkout(args[2], true);
                            break;
                        default:
                            throw new VCSException("Unexpected count of arguments");
                    }
                    break;

                case "branch":
                    Cmd.checkArgumentsLength(args, 3, "branch require at least 2 arguments.\n" +
                            "First -b and -d modificators for create and delete branch.\n" +
                            "Second is branch name.");
                    switch (args[1]) {
                        case "-b":
                            vcs.branch(args[2], ModifyAction.CREATE);
                            break;
                        case "-d":
                            vcs.branch(args[2], ModifyAction.DELETE);
                            break;
                    }
                    break;

                case "commit":
                    Cmd.checkArgumentsLength(args, 2, "you must specify commit message");
                    vcs.commit(args[1]);
                    break;

                case "log":
                    vcs.log();
                    break;

                case "add":
                    Cmd.checkArgumentsLength(args, 2, "you must specify target file");
                    for (int fileId = 1; fileId < args.length; ++fileId) {
                        vcs.addFileToStage(args[fileId]);
                    }
                    break;

                case "reset":
                    Cmd.checkArgumentsLength(args, 2, "you must specify target file");
                    vcs.resetFileInStage(args[1]);
                    break;

                case "status":
                    vcs.status();
                    break;

                case "clean":
                    vcs.clean();
                    break;

                case "rm":
                    Cmd.checkArgumentsLength(args, 2, "you must specify target file");
                    vcs.removeFileInStage(args[1]);
                    break;

                case "merge":
                    Cmd.checkArgumentsLength(args, 2, "you must specify source merge branch");
                    vcs.merge(args[1]);
                    break;

                default:
                    System.out.println("Start with creating your own repository by command:\nvcs init");
            }
        } catch (VCSException e) {
            System.out.printf("VCS exception: " + e.getMessage());
        }
    }
}

