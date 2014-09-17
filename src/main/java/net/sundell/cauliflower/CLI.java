package net.sundell.cauliflower;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Formatter;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.ParseException;;

public abstract class CLI {
    private boolean noisy = true;

    private SortedMap<String, Class<? extends Command>> commands =
        new TreeMap<String, Class<? extends Command>>();

    /**
     * Return the name of the base CLI program.  This is used for help, usage and
     * informational messages.  By default, it will return the name of the current
     * class. 
     */
    public String getName() {
        return getClass().getName();
    }

    /**
     * Specify the path of the user data file to be used.  If this returns
     * <code>null</code>, no data file will be used.  If a non-null value is
     * returned, it will be used to read and write user data.
     * 
     * The default implementation of this returns null.
     * @return File for the user data, or null 
     */
    protected File getUserDataFile() {
        return null;
    }

    /**
     * Invoke the CLI on arguments.
     * @param args
     */
    public void run(String[] args) {
        registerCommands(commands);
        if (args.length == 0) {
            help();
        }
        String cmd = args[0].toLowerCase();
        if (cmd.equals("help") && args.length == 2) {
            help(args[1]);
        }
        Command command = getCommand(cmd);
        if (args.length > 1) {
            args = Arrays.asList(args).subList(1, args.length)
                                .toArray(new String[args.length - 1]);
        }
        else {
            args = new String[0];
        }
        CommandLineParser parser = new GnuParser();
        try {
            CommandLine cl = parser.parse(command.getOptions(), args);
            File userDataFile= getUserDataFile();
            UserData userData = new UserData();
            if (userDataFile != null) {
                if (!userDataFile.exists()) {
                    noisy("Creating " + userDataFile);
                    userDataFile.createNewFile();
                }
                userData = initializeUserData(UserData.loadProperties(userDataFile));
            }
            else {
                throw new RuntimeException("Unimplemented");
            }
            command.setUserData(userData);
            command.handle(cl);
            if (userDataFile != null && userData.isDirty()) {
                noisy("Saving to " + userDataFile);
                userData.store(userDataFile);
            }
        }
        catch (ParseException e) {
            System.err.println(e.getMessage());
            command.usage(getErrorWriter());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected abstract UserData initializeUserData(Properties properties);
    protected abstract void registerCommands(Map<String, Class<? extends Command>> commands);

    protected void help() {
        Formatter f = new Formatter(System.err);
        f.format("Available commands:\n");
        for (Map.Entry<String, Class<? extends Command>> e : 
                                            commands.entrySet()) {
            Command c = getCommandInstance(e.getValue());
            f.format("%-20s%s\n", e.getKey(), c.getDescription());
        }
        f.flush();
        f.close();
    }

    protected void help(String cmd) {
        Command command = getCommand(cmd);
        if (command == null) {
            help();
        }
        command.setName(cmd);
        command.usage(getErrorWriter());
    }

    public PrintWriter getOutputWriter() {
        return new PrintWriter(System.out, true);
    }

    public PrintWriter getErrorWriter() {
        return new PrintWriter(System.err, true);
    }

    private Command getCommand(String cmd) {
        Command command = getCommandInstance(commands.get(cmd));
        if (command == null) {
            help();
        }
        command.setName(cmd);
        command.setCLI(this);
        return command;
    }

    private static Command getCommandInstance(Class<? extends Command> clazz) {
        if (clazz == null) {
            return null;
        }
        try {
            return clazz.newInstance();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void noisy(String s) {
        if (noisy) {
            System.out.println(s);
        }
    }
}
