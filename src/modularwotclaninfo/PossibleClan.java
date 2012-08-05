package modularwotclaninfo;

import javax.swing.ImageIcon;

/**
 *
 * @author Yoyo117 (johnp)
 */
public class PossibleClan {

    private final String name;
    private final String clanTag;
    private final long ID;
    private final int member_count;

    private final ImageIcon emblem;

    PossibleClan(String name, String clanTag, long ID, int member_count, ImageIcon emblem) {
        this.name = name;
        this.clanTag = clanTag;
        this.ID = ID;
        this.member_count = member_count;
        this.emblem = emblem;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s", clanTag, name);
    }

    public String getName() {
        return name;
    }

    public String getClanTag() {
        return clanTag;
    }

    public long getID() {
        return ID;
    }

    public int getMemberCount() {
        return member_count;
    }

    public ImageIcon getEmblem() {
        return emblem;
    }
}
