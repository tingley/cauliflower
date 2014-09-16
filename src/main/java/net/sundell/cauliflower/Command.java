package net.sundell.cauliflower;

import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.HelpFormatter;

public abstract class Command {

    private String name;
    private CLI cli;
    private UserData userData;

    /**
     * Get the {@link CLI} instance in which this command is running.
     * @return active CLI instance
     */
    protected CLI getCLI() {
        return cli;
    }

    void setCLI(CLI cli) {
        this.cli = cli;
    }

    /**
     * Get the {@link UserData} for the current command.
     * @return user data
     */
    protected UserData getUserData() {
        return userData;
    }

    void setUserData(UserData userData) {
        this.userData = userData;
    }

    /**
     * Get the name of this command as it appears in the CLI.  This 
     * value is set automatically based on the implementation of 
     * {@link CLI#registerDefaultCommands}.
     *  
     * @return command name
     */
    protected final String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    public abstract String getDescription();

    private boolean verbose = true; 

    public Options getOptions() {
        return new Options();
    }

    /**
     * Execute this command on the parsed command line.
     * @param a commons-cli {@link CommandLine} instance
     */
    public abstract void handle(CommandLine command);

    /**
     * Print usage for this command to the CLI's error stream.
     */
    protected void usage() {
        usage(getCLI().getErrorWriter());
    }

    /**
     * Print the usage for this command and die.
     * @param out writer to which the help will be printed
     */
    protected void usage(PrintWriter out) {
        HelpFormatter f = new HelpFormatter();
        f.printHelp(out,
                    78,
                    cli.getName() + " " + getUsageLine(),
                    "", // banner
                    getOptions(),
                    4, // left pad
                    0, // description pad
                    "", // footer
                    false); // automatically generate usage
        printExtraHelp(out);
        System.exit(1);
    }

    /**
     * Get the usage line for this command.  The default implementation 
     * returns only the command name, so commands which take parameters
     * should override this method.
     * 
     * @return usage line
     */
    protected String getUsageLine() {
        return getName();
    }

    /**
     * Print any additional help information for this command.  The 
     * default implementation does nothing.
     * @param out writer to which the help should be printed
     */
    protected void printExtraHelp(PrintWriter out) {    
    }

    /**
     * Print an 
     * @param message
     */
    protected void usage(String message) {
        PrintWriter out = cli.getErrorWriter();
        out.println(message);
        // TODO use printUsage() here.  What does app do?
        usage(out);
    }

    protected void die(String message) {
        cli.getErrorWriter().println(message);
        System.exit(1);
    }
    
    protected void verbose(String message) {
        if (verbose) {
            cli.getOutputWriter().println(message);
        }
    }

    protected void out(String message) {
        cli.getOutputWriter().println(message);
    }

    protected void warn(String message) {
        cli.getErrorWriter().println(message);
    }
}
