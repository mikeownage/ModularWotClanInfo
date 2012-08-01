package modularwotclaninfo;

/**
 *
 * @author Yoyo117 (johnp)
 */
public class ClanAPIException extends Exception implements ProgrammException {
    private final String status_code;
    private final GUI gui;

    public ClanAPIException(String status_code, GUI gui) {
        super();
        this.status_code = status_code;
        this.gui = gui;
    }

    @Override
    public void publish() {
        this.gui.errorPanel("Error while getting clan: "+status_code, " ClanAPIError");
    }
}
