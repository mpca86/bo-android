package org.blitzortung.android.map.overlay

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import com.google.android.maps.GeoPoint
import com.google.android.maps.MapView
import com.google.android.maps.Projection
import org.blitzortung.android.data.Coordsys
import org.blitzortung.android.data.beans.RasterParameters
import org.blitzortung.android.data.beans.Strike

class StrikeOverlay(strike: Strike) {
    val timestamp: Long = strike.timestamp
    val multiplicity = strike.multiplicity
    val center: GeoPoint = Coordsys.toMapCoords(strike.longitude, strike.latitude)

    var shape: LightningShape? = null

    fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean, paint: Paint) {
        shape?.run {
            draw(canvas, mapView, paint)
        }
    }

    fun updateShape(rasterParameters: RasterParameters?, projection: Projection, color: Int, textColor: Int, zoomLevel: Int) {
        var shape: LightningShape? = shape
        if (rasterParameters != null) {
            if (shape !is RasterShape) {
                shape = RasterShape(center)
            }

            val lonDelta = rasterParameters.longitudeDelta / 2.0f * 1e6f
            val latDelta = rasterParameters.latitudeDelta / 2.0f * 1e6f

            projection.toPixels(center, centerPoint)
            projection.toPixels(GeoPoint(
                    (center.latitudeE6 + latDelta).toInt(),
                    (center.longitudeE6 - lonDelta).toInt()), topLeft)
            projection.toPixels(GeoPoint(
                    (center.latitudeE6 - latDelta).toInt(),
                    (center.longitudeE6 + lonDelta).toInt()), bottomRight)
            topLeft.offset(-centerPoint.x, -centerPoint.y)
            bottomRight.offset(-centerPoint.x, -centerPoint.y)
            if (shape is RasterShape) {
                shape.update(topLeft, bottomRight, color, multiplicity, textColor)
            }
        } else {
            if (shape == null) {
                shape = StrikeShape(center)
            }
            if (shape is StrikeShape) {
                shape.update((zoomLevel + 1).toFloat(), color)
            }
        }
        this.shape = shape
    }

    fun pointIsInside(point: GeoPoint, projection: Projection): Boolean {
        return shape?.isPointInside(point, projection)
                ?: false
    }

    companion object {
        private val centerPoint: Point = Point()
        private val topLeft: Point = Point()
        private val bottomRight: Point = Point()
    }
}