package tech.icey.babel;

import java.util.Comparator;
import java.util.List;

public final class Babel {
    public Babel(List<DataPublisher> dataPublishers,
                 List<DataManipulator> dataManipulators,
                 List<DataConsumer> dataConsumers) {
        dataPublishers = dataPublishers
                .stream()
                .sorted(Comparator.comparingInt(DataPublisher::priority))
                .toList();
        this.dataManipulators = dataManipulators
                .stream()
                .sorted(Comparator.comparingInt(DataManipulator::priority))
                .toList();
        this.dataConsumers = dataConsumers;
        this.bible = new Bible();

        for (DataPublisher publisher : dataPublishers) {
            publisher.initialise(this.bible);
        }

        for (DataManipulator manipulator : this.dataManipulators) {
            manipulator.initialise(this.bible);
        }

        for (DataConsumer consumer : this.dataConsumers) {
            consumer.initialise(this.bible);
        }
    }

    public void runPipeline() {
        for (DataManipulator manipulator : this.dataManipulators) {
            manipulator.manipulate(this.bible);
        }

        for (DataConsumer consumer : this.dataConsumers) {
            consumer.consume(this.bible);
        }
    }

    public void cleanPipeline() {
        bible.clearDirty();
    }

    private final List<DataManipulator> dataManipulators;
    private final List<DataConsumer> dataConsumers;
    private final Bible bible;
}
