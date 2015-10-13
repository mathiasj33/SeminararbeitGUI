package net.softwarepage.seminararbeit.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.scene.control.ListCell;
import net.softwarepage.facharbeit.normalgame.logic.MixedNashEquilibrium;
import net.softwarepage.facharbeit.normalgame.logic.NashEquilibrium;
import net.softwarepage.facharbeit.normalgame.logic.NashEquilibriumType;
import net.softwarepage.facharbeit.normalgame.logic.NormalGame;
import net.softwarepage.facharbeit.normalgame.logic.PureNashEquilibrium;
import net.softwarepage.facharbeit.normalgame.logic.Strategy;

public class NashEquilibriumCell extends ListCell<NashEquilibrium> {

    private final NormalGame game;

    public NashEquilibriumCell(NormalGame game) {
        this.game = game;
    }

    @Override
    public void updateItem(NashEquilibrium item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            return;
        }
        if (item instanceof PureNashEquilibrium) {
            PureNashEquilibrium ne = (PureNashEquilibrium) item;
            String type = ne.getType() == NashEquilibriumType.NotStrict ? "Nicht strikt" : "strikt";
            String text = game.getPlayer1() + ": " + ne.getFirstStrat() + "; " + game.getPlayer2() + ": " + ne.getSecondStrat() + " - " + type;
            setText(text);
        } else {
            Map<Strategy, Float> probabilities = ((MixedNashEquilibrium) item).getProbabilities();
            StringBuilder builder = createString(probabilities);
            setText(builder.toString());
        }
    }

    private StringBuilder createString(Map<Strategy, Float> probabilities) {
        StringBuilder builder = new StringBuilder(game.getPlayer1().getName()).append(": ");
        boolean secondPart = false;
        for (Strategy strategy : probabilities.keySet()) {
            if (!isFirstPlayerStrategy(strategy) && !secondPart) {
                builder.append(game.getPlayer2().getName()).append(": ");
                secondPart = true;
            }
            builder.append(strategy.getName())
                    .append(" ")
                    .append(probabilities.get(strategy))
                    .append("%");
            List<Strategy> list = new ArrayList<>(probabilities.keySet());
            if (list.indexOf(strategy) != list.size() - 1) {
                builder.append("; ");
            }
        }
        return builder;
    }

    private boolean isFirstPlayerStrategy(Strategy strategy) {
        return game.getPlayer1().getStrategies().contains(strategy);
    }

}
