import exceptions.VCSException;

public class VCSMain {
    public static void main(String[] args) {
        if (args.length <= 1) {
            System.out.println("Start with creating your own repository by command:\nvcs init");
            return;
        }
        VCS vcs = new VCS();
        try {
            switch (args[1]) {
                case "init":
                    vcs.initRepository();
                    break;

                case "checkout":
                    Utils.checkArgumentsLength(args, 3, "checkout require at least 1 arguments - branch name. " +
                            "You can use -b modificator for create");
                    switch (args.length) {
                        case 3:
                            vcs.checkout(args[2], false);
                            break;
                        case 4:
                            vcs.checkout(args[3], true);
                            break;
                        default:
                            throw new VCSException("Unexpected count of arguments");
                    }
                    break;

                case "branch":
                    Utils.checkArgumentsLength(args, 4, "branch require at least 2 arguments.\n" +
                            "First -b and -d modificators for create and delete branch.\n" +
                            "Second is branch name.");
                    switch (args[2]) {
                        case "-b":
                            vcs.branch(args[3], VCS.MODIFY_ACTION.CREATE);
                            break;
                        case "-d":
                            vcs.branch(args[3], VCS.MODIFY_ACTION.DELETE);
                            break;
                    }
                    break;

                case "commit":
                    Utils.checkArgumentsLength(args, 3, "you must specify commit message");
                    vcs.commit(args[2]);
                    break;

                case "log":
                    vcs.log();
                    break;

                case "merge":
                    Utils.checkArgumentsLength(args, 3, "you must specify source merge branch");
                    vcs.merge(args[2]);
                    break;

                default:
                    System.out.println("Start with creating your own repository by command:\nvcs init");
            }
        } catch (VCSException e) {
            System.out.printf("VCS exception: " + e.getMessage());
        }
    }
}

