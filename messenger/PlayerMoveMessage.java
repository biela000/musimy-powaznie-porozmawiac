import java.util.List;

public class PlayerMoveMessage extends WsMessage {
    public final List<PlayerPosition> move;

    public PlayerMoveMessage(List<PlayerPosition> move) {
        super("PLAYER_MOVE");
        this.move = move;
    }
}
