package modularwotclaninfo;

/**
 *
 * @author Yoyo117 (johnp)
 */
public class ClanNotFoundException extends Exception implements ProgrammException {
    private final GUI gui;

    public ClanNotFoundException(GUI gui) {
        super();
        this.gui = gui;
    }

    @Override
    public void publish() {
        this.gui.errorPanel("Couldn't find clan. Please check input.", " Unknown clan");
    }
}
