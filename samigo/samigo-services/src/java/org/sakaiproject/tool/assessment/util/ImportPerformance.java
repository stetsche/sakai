package org.sakaiproject.tool.assessment.util;

import java.io.PrintStream;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemData;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc.TypeId;
import org.springframework.util.Assert;

public class ImportPerformance {
    

    public static final String COPY_ASSESSMENTS = "copyAssessments";
    public static final String GET_ASSESSMENTS = "copyAssessments";
    public static final String UPDATE_REFS = "updateRefs";
    
    private final Map<Long, ItemData> itemMap = new HashMap<>();
    private final Map<Long, StopWatch> stopWatchMap = new HashMap<>();
    private final Map<String, StopWatch> otherStopWatches = new HashMap<>() {{
            put(COPY_ASSESSMENTS, StopWatch.create());
            put(GET_ASSESSMENTS, StopWatch.create());
            put(UPDATE_REFS, StopWatch.create());
    }};
    private final StopWatch referenceStopWatch = new StopWatch();
    private final ControlStopWatch controlStopWatch = new ControlStopWatch();
    
    private static final String TABLE_ROW_FORMAT = "%-35s %-20s\n";
    private static final String TABLE_ROW_FORMAT_WIDE = "%-55s %-20s\n";


    public void startMeassuring() {
        referenceStopWatch.start();
    }

    public void stopMeassuring() {
        referenceStopWatch.stop();
    }

    public void startMeassuring(String aspect) {
        StopWatch stopWatch = otherStopWatches.get(aspect);

        Assert.notNull(stopWatch, "Stopwatch [" + aspect + "] not initialized");

        controlStopWatch.start();
        stopWatch.start();
    }

    public void stopMeassuring(String aspect) {
        StopWatch stopWatch = otherStopWatches.get(aspect);

        Assert.notNull(stopWatch, "Stopwatch [" + aspect + "] not initialized");

        controlStopWatch.stop();
        stopWatch.stop();
    }

    public void startMeassuringItem(ItemData item) {
        Assert.notNull(item, "Item can't be null");
        Assert.notNull(item.getItemId(), "Item id can't be null");
        Assert.notNull(item.getTypeId(), "Item type can't be null");

        Long itemId = item.getItemId();
        itemMap.putIfAbsent(itemId, item);

        StopWatch itemStopWatch = stopWatchMap.computeIfAbsent(itemId, (id) -> new StopWatch());
        
        controlStopWatch.start();
        itemStopWatch.start();
    }

    public void stopMeassuringItem(ItemData item) {
        Assert.notNull(item, "Item can't be null");
        Assert.notNull(item.getItemId(), "Item id can't be null");
        Assert.notNull(item.getTypeId(), "Item type can't be null");

        Long itemId = item.getItemId();
        StopWatch itemStopWatch = stopWatchMap.get(itemId);
        
        Assert.notNull(itemMap.containsKey(itemId), "Item must be present in item map");
        Assert.notNull(itemStopWatch, "Item stopwatch must be present in stopwatch map");
        
        controlStopWatch.stop();
        itemStopWatch.stop();
    }
    
    public void reset() {
        itemMap.clear();
        stopWatchMap.clear();
        for (StopWatch stopWatch : otherStopWatches.values()) {
           stopWatch.reset(); 
        }
        referenceStopWatch.reset();
        controlStopWatch.reset();
    }
    
    public void evaluate(PrintStream output) {
        Assert.isTrue(isCompleted(), "All timers should have been stopped at evaluation time");
        
        StopWatch evaluationStopWatch = StopWatch.createStarted();
        
        Map<TypeId, List<ItemData>> itemsByType = itemMap.values().stream()
                .collect(Collectors.groupingBy((item) -> TypeId.getInstance(item.getTypeId()))); 
   
        output.append("\n");
        output.append("==============================================\n");
        output.printf(TABLE_ROW_FORMAT, "Copy all assessments",
                formatDuration(otherStopWatches.get(COPY_ASSESSMENTS).getDuration()));

        output.printf(TABLE_ROW_FORMAT, "Get all assessments (query)",
                formatDuration(otherStopWatches.get(GET_ASSESSMENTS).getDuration()));
        output.append("\n");

        output.append("==============================================\n");
        output.append("Item reference updates:\n");
        output.append("\n");

        output.append("Count of items by type:\n");
        itemsByType.forEach((type, items) -> {
            output.append(String.format(TABLE_ROW_FORMAT, formatType(type), items.size()));
        });
        output.append("\n");

        output.append("Total time spent on item type:\n");
        itemsByType.forEach((type, items) -> {
            Duration totalDuration = items.stream()
                    .map(ItemData::getItemId)
                    .map(stopWatchMap::get)
                    .map(StopWatch::getDuration)
                    .reduce(Duration.ZERO, Duration::plus);
            
            output.append(String.format(TABLE_ROW_FORMAT, formatType(type), formatDuration(totalDuration)));            
        });
        output.append("\n");
        
        output.append("Average time spent on item type:\n");
        itemsByType.forEach((type, items) -> {
            Assert.isTrue(items.size() > 0, "That *should* not happen");

            Duration avererageDuration = items.stream()
                    .map(ItemData::getItemId)
                    .map(stopWatchMap::get)
                    .map(StopWatch::getDuration)
                    .reduce(Duration.ZERO, Duration::plus)
                    .dividedBy(items.size());
            
            output.append(String.format(TABLE_ROW_FORMAT, formatType(type),
                    formatDuration(avererageDuration)));            
        });
        output.append("\n");

        Duration updateRefsDuration = otherStopWatches.get(COPY_ASSESSMENTS).getDuration();
        Duration totalItemUpdateRefsDuration = itemsByType.values().stream()
                    .flatMap(Collection::stream)
                    .map(ItemData::getItemId)
                    .map(stopWatchMap::get)
                    .map(StopWatch::getDuration)
                    .reduce(Duration.ZERO, Duration::plus);

        output.printf(TABLE_ROW_FORMAT, "Time not updating items/answers",
                formatDuration(updateRefsDuration.minus(totalItemUpdateRefsDuration)));
        output.printf(TABLE_ROW_FORMAT, "Total update refs duration",
                formatDuration(updateRefsDuration));
        output.append("\n");
        output.append("==============================================\n");
        
        output.printf(TABLE_ROW_FORMAT_WIDE, "Total recorded duration:"
                +  formatDuration(referenceStopWatch.getDuration()));

        Duration missedDuration = referenceStopWatch.getDuration()
                .minus(controlStopWatch.getDuration());
        output.printf(TABLE_ROW_FORMAT_WIDE, "Time that is has not been covered in measurements:"
                +  formatDuration(missedDuration));
        output.append("\n");
        output.append("==============================================\n");

        // Hopefully this are seconds...
        evaluationStopWatch.stop();
        output.append("The evaluation took "
                + evaluationStopWatch.getDuration().toSeconds() + "s\n");
    }
    
    private boolean isCompleted() {
        for (StopWatch stopWatch : stopWatchMap.values()) {
            if (!stopWatch.isStopped()) {
                return false;
            }
        }
        
        return true;
    }
    
    private String formatDuration(Duration duration) {
        return DurationFormatUtils.formatDurationHMS(duration.toMillis());
    }
    
    private String formatType(TypeId type) {
        return type.name();
    }
    

    // Lazy implementation of StopWatch that can be started mulilple times, also after stopping
    private class ControlStopWatch {
        

        private Duration elapsedTime = Duration.ZERO;
        private Stack<StopWatch> stopWatches = new Stack<>();


        public void start() {
            stopWatches.push(StopWatch.createStarted());
        }
        
        public void stop() {
            switch (stopWatches.size()) {
                case 0:
                    throw new IllegalStateException("Cant stop watch that is not running.");
                case 1:
                    StopWatch onlyStopWatch = stopWatches.pop();
                    onlyStopWatch.stop();
                    elapsedTime = elapsedTime.plus(onlyStopWatch.getDuration());
                    break;
                default:
                    stopWatches.pop();
                    break;
            }
        }

        public void reset() {
            elapsedTime = Duration.ZERO;
            stopWatches.clear();
        }

        public Duration getDuration() {
            if (stopWatches.isEmpty()) {
                return elapsedTime;
            } else {
                return elapsedTime.plus(stopWatches.get(0).getDuration());
            }
        }
    }
}
