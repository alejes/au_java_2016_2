import exceptions.VCSException;


public class Utils {
    public static void checkArgumentsLength(String[] args, int minimumCount, String message) throws VCSException {
        if (args.length < minimumCount) {
            throw new VCSException(message);
        }
    }
}
