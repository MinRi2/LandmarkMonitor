package monitor.ui;

import arc.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;

/**
 * @author minri2
 * Create by 2025/4/3
 */
public class PlayerSelector extends BaseDialog{
    private PlayerSelectCons selectCons;
    private final Seq<Player> players = new Seq<>();

    private int lastPlayerSize = -1;

    public PlayerSelector(){
        super("@player-select");

        shown(() -> {
            if(!cont.hasChildren()){
                setupCont();
                addCloseButton();
            }
        });

        Events.on(ResetEvent.class, e -> {
            lastPlayerSize = -1;
        });
    }

    public void show(PlayerSelectCons selectCons){
        this.selectCons = selectCons;
        super.show();
    }

    private void setupCont(){
        cont.pane(Styles.noBarPane, playersTable -> {
            playersTable.update(() -> {
                int size = Groups.player.size();

                if(lastPlayerSize != size){
                    lastPlayerSize = size;

                    players.clear();
                    Groups.player.copy(players);

                    players.sort(Structs.comps(
                    Structs.comparingInt(p -> p.team().id),
                    Structs.comparingBool(p -> !p.admin)
                    ));

                    playersTable.clear();
                    setupPlayersTable(playersTable);
                }
            });
        }).fillY();
    }

    private void setupPlayersTable(Table table){
        for(Player player : players){
            table.table(Tex.pane, t -> {
                setupPlayerTable(t, player);
            }).growX();

            table.row();
        }
    }


    private void setupPlayerTable(Table table, Player player){
        table.defaults().pad(8);

        table.add(new BorderImage()).size(64f).update(image -> {
            image.setDrawable(player.icon());
        });

        table.table(null, nameTable -> {
            nameTable.left();

            nameTable.add(player.coloredName()).growX();

            nameTable.row();

            nameTable.image().color(player.team().color).growX();
        }).minWidth(256).growX();

        table.button(Icon.ok, Styles.cleari, 48, () -> {
            if(selectCons.get(player)){
                hide();
            }
        }).size(64);
    }

    public interface PlayerSelectCons{
        boolean get(Player player);
    }
}
