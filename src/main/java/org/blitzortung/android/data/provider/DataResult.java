package org.blitzortung.android.data.provider;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.blitzortung.android.data.beans.AbstractStroke;
import org.blitzortung.android.data.beans.Participant;
import org.blitzortung.android.data.beans.Raster;

public class DataResult implements Serializable {

	private static final long serialVersionUID = -2104015890700948020L;

	private final List<List<AbstractStroke>> strokes;
	private List<Participant> participants;
	private Raster raster = null;
    private int[] histogram;

    private boolean fail;
	
	private boolean processWasLocked;
	
	private boolean incremental;

    private int intervalDuration;

    private long referenceTime;

    private int intervalOffset;

    private int region;

    public DataResult() {
        strokes = new ArrayList<List<AbstractStroke>>();
		fail = true;
		
		processWasLocked = false;
		
		incremental = false;
	}
	
	public void setStrokes(List<AbstractStroke> strokes) {
        this.strokes.clear();
		this.strokes.add(strokes);
		fail = false;
	}
	
	public boolean containsStrokes() {
		return ! strokes.isEmpty();
	}
	
	public List<AbstractStroke> getStrokes() {
		return strokes.get(0);
	}
	
	public void setParticipants(List<Participant> participants) {
		this.participants = participants;
	}
	
	public boolean containsParticipants() {
		return participants != null;
	}
	
	public List<Participant> getParticipants() {
		return participants;
	}
	
	public boolean retrievalWasSuccessful() {
		return !fail && !processWasLocked;
	}
	
	public boolean hasFailed() {
		return fail;
	}
	
	public void setProcessWasLocked() {
		processWasLocked = true;
	}
	
	public boolean processWasLocked() {
		return processWasLocked;
	}
	
	public boolean hasRaster() {
		return raster != null;
	}
	
	public Raster getRaster() {
		return raster;
	}
	
	public void setRaster(Raster raster) {
		this.raster = raster;
	}

	public boolean isIncremental() {
		return incremental;
	}
	
	public void setIncremental() {
		incremental = true;
	}

    public void setIntervalDuration(int intervalDuration) {
        this.intervalDuration = intervalDuration;
    }

    public int getIntervalDuration() {
        return intervalDuration;
    }

    public void setHistogram(int[] histogram) {
        this.histogram = histogram;
    }

    public int[] getHistogram() {
        return histogram;
    }

    public void setReferenceTime(long referenceTime) {
        this.referenceTime = referenceTime;
    }

    public long getReferenceTime() {
        return referenceTime;
    }

    public boolean containsRealtimeData() {
        return intervalOffset == 0;
    }

    public void setIntervalOffset(int intervalOffset) {
        this.intervalOffset = intervalOffset;
    }

    public int getIntervalOffset() {
        return intervalOffset;
    }

    public void setRegion(int region) {
        this.region = region;
    }

    public int getRegion() {
        return region;
    }
}
