package org.blitzortung.android.data.beans;

import java.text.ParseException;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;

public class Stroke extends AbstractStroke {
	
	private Date timestamp;
	
	private int nanoseconds;
	
	private float longitude;
	
	private float latitude;
	
	private float amplitude;
	
	private short stationCount;
	
	private float lateralError;
	
	private short type;
	
	public Stroke(Date referenceTimestamp, JSONArray jsonArray) {
		
		try {
			timestamp = new Date();
			timestamp.setTime(referenceTimestamp.getTime() + 1000 * jsonArray.getInt(0));
			nanoseconds = jsonArray.getInt(1);
			longitude = (float)jsonArray.getDouble(2);
			latitude = (float)jsonArray.getDouble(3);
			lateralError = (float)jsonArray.getDouble(4);
			amplitude = (float)jsonArray.getDouble(4);
			stationCount = (short)jsonArray.getInt(5);
			type = (short)jsonArray.getInt(6);
		} catch (JSONException e) {
			throw new RuntimeException("error with json format while parsing stroke data", e);
		}
	}
	
	public Stroke(String line) {
		String[] fields = line.split(" ");
		String timeString = fields[0].replace("-", "") + "T" + fields[1];
		int len = timeString.length();
		try {
			timestamp = DATE_TIME_FORMATTER.parse(timeString.substring(0, len-6));
			nanoseconds = Integer.valueOf(timeString.substring(len-6));
			latitude = Float.valueOf(fields[2]);
			longitude = Float.valueOf(fields[3]);
			amplitude = Float.valueOf(fields[4].substring(0, fields[4].length()-2));
			type = Short.valueOf(fields[5]);
			lateralError = Float.valueOf(fields[6].substring(0, fields[6].length()-1));
			stationCount = Short.valueOf(fields[7]);
		} catch (ParseException e) {
			throw new RuntimeException("error parsing stroke data", e);
		}
		
	}
	
	@Override
	public Date getTime() {
		return timestamp;
	}
	
	public int getNanoseconds() {
		return nanoseconds;
	}
	
	public float getLongitude() {
		return longitude;
	}
	
	public float getLatitude() {
		return latitude;
	}

	public float getAmplitude() {
		return amplitude;
	}

	public short getStationCount() {
		return stationCount;
	}

	public float getLateralError() {
		return lateralError;
	}
	
	public short getType() {
		return type;
	}

}