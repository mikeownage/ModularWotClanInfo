package modularwotclaninfo;

/**
 *
 * @author Yoyo117 (johnp)
 */
public class ClanNotFoundException extends Exception implements ProgrammException {
    private final String searchType;
    private final GUI gui;

    public ClanNotFoundException(String searchType, GUI gui) {
        super();
        this.searchType = searchType;
        this.gui = gui;
    }

    @Override
    public void publish() {
        this.gui.errorPanel("Couldn't find clan. Please check input.", " Unknown clan");
    }
}
