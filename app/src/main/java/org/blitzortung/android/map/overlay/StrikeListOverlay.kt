/*

   Copyright 2015 Andreas Würl

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

*/

package org.blitzortung.android.map.overlay

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Style
import android.util.Log
import android.view.ViewGroup
import com.google.android.maps.GeoPoint
import com.google.android.maps.MapView
import com.google.android.maps.Overlay
import org.blitzortung.android.app.Main
import org.blitzortung.android.app.R
import org.blitzortung.android.data.Parameters
import org.blitzortung.android.data.beans.RasterParameters
import org.blitzortung.android.data.beans.Strike
import org.blitzortung.android.map.OwnMapActivity
import org.blitzortung.android.map.OwnMapView
import org.blitzortung.android.map.components.LayerOverlayComponent
import org.blitzortung.android.map.createStrikePopUp
import org.blitzortung.android.map.overlay.color.ColorHandler
import org.blitzortung.android.map.overlay.color.StrikeColorHandler

class StrikeListOverlay(private val mapActivity: OwnMapActivity, val colorHandler: StrikeColorHandler): Overlay(), LayerOverlay {
    private val strikeList = mutableListOf<StrikeOverlay>()

    private val layerOverlayComponent: LayerOverlayComponent
    private var zoomLevel: Int = 0
    var rasterParameters: RasterParameters? = null
    var referenceTime: Long = 0
    var parameters = Parameters()

    init {
        layerOverlayComponent = LayerOverlayComponent(mapActivity.resources.getString(R.string.strikes_layer))
    }

    override fun draw(canvas: Canvas?, mapView: com.google.android.maps.MapView?, shadow: Boolean) {
        if (!shadow) {
            super.draw(canvas, mapView, false)

            if(canvas == null || mapView == null) {
                return
            }

            val paint = Paint()
            if (hasRasterParameters()) {
                paint.color = colorHandler.lineColor
                paint.style = Style.STROKE

                drawDataAreaRect(canvas, mapView, paint)
            }

            paint.style = Style.FILL
            strikeList.forEach {
                it.draw(canvas, mapView, false, paint)
            }
        }
    }

    fun addStrikes(strikes: List<Strike>) {
        strikeList.addAll(strikes.map { StrikeOverlay(it) })

        updateTotalNumberOfStrikes()
    }

    private fun drawDataAreaRect(canvas: Canvas, mapView: MapView, paint: Paint) {
        val clipBounds = canvas.clipBounds
        val currentRasterParameters = rasterParameters
        if (currentRasterParameters != null) {
            val rect = currentRasterParameters.getRect(mapView.projection)

            if (rect.left >= clipBounds.left && rect.left <= clipBounds.right) {
                canvas.drawLine(rect.left, Math.max(rect.top, clipBounds.top.toFloat()), rect.left, Math.min(rect.bottom, clipBounds.bottom.toFloat()), paint)
            }
            if (rect.right >= clipBounds.left && rect.right <= clipBounds.right) {
                canvas.drawLine(rect.right, Math.max(rect.top, clipBounds.top.toFloat()), rect.right, Math.min(rect.bottom, clipBounds.bottom.toFloat()), paint)
            }
            if (rect.bottom <= clipBounds.bottom && rect.bottom >= clipBounds.top) {
                canvas.drawLine(Math.max(rect.left, clipBounds.left.toFloat()), rect.bottom, Math.min(rect.right, clipBounds.right.toFloat()), rect.bottom, paint)
            }
            if (rect.top <= clipBounds.bottom && rect.top >= clipBounds.top) {
                canvas.drawLine(Math.max(rect.left, clipBounds.left.toFloat()), rect.top, Math.min(rect.right, clipBounds.right.toFloat()), rect.top, paint)
            }
        }
    }

    fun expireStrikes() {
        val expireTime = referenceTime - parameters.intervalDuration * 60 * 1000

        val sizeBefore = strikeList.size
        val firstTime = strikeList.firstOrNull()?.timestamp
        val difference = firstTime?.let { it - expireTime }
        strikeList.removeAll { it.timestamp < expireTime }
        Log.v(Main.LOG_TAG, "StrikesListOverlay.expireStrikes() expired ${sizeBefore - strikeList.size} from $sizeBefore (first: $firstTime, difference: $difference, ref: $referenceTime")

        updateTotalNumberOfStrikes()
    }

    fun clear() {
        strikeList.clear()

        updateTotalNumberOfStrikes()
    }

    fun updateZoomLevel(zoomLevel: Int) {
        if (hasRasterParameters() || zoomLevel != this.zoomLevel) {
            this.zoomLevel = zoomLevel
            refresh()
        }
    }

    override fun onTap(point: GeoPoint, map: MapView): Boolean {
        Log.d(Main.LOG_TAG, "Tapped on StrikeList")

        val strikeTapped = strikeList.firstOrNull { it.pointIsInside(point, map.projection) }

        val popup = (map as OwnMapView).popup
        map.removeView(popup)

        if(strikeTapped != null) {
            val newPopup = createStrikePopUp(popup, strikeTapped)

            val mapParams = MapView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                    point, 0, 0, MapView.LayoutParams.BOTTOM_CENTER)

            map.addView(newPopup, mapParams)

            return true
        }

        return false
    }

    fun refresh() {
        val currentSection = -1

        colorHandler.updateTarget()

        var drawable: LightningShape? = null

        for (item in strikeList) {
            val section = colorHandler.getColorSection(
                    if (hasRealtimeData())
                        System.currentTimeMillis()
                    else
                        referenceTime,
                    item.timestamp, parameters.intervalDuration)

            if (hasRasterParameters() || currentSection != section) {
                drawable = updateAndReturnDrawable(item, section, colorHandler)
            } else {
                if (drawable != null) {
                    item.shape = drawable
                }
            }
        }
    }

    // VisibleForTesting
    protected fun updateAndReturnDrawable(item: StrikeOverlay, section: Int, colorHandler: ColorHandler): LightningShape {
        val projection = mapActivity.mapView.projection
        val color = colorHandler.getColor(section)
        val textColor = colorHandler.textColor

        item.updateShape(rasterParameters, projection, color, textColor, zoomLevel)

        return item.shape!!
    }

    fun hasRasterParameters(): Boolean {
        return rasterParameters != null
    }

    fun hasRealtimeData(): Boolean {
        return parameters.isRealtime()
    }

    private fun updateTotalNumberOfStrikes() {
        totalNumberOfStrikes = strikeList.fold(0, { previous, item -> previous + item.multiplicity })
    }

    var totalNumberOfStrikes: Int = 0
        private set

    override val name: String
        get() = layerOverlayComponent.name

    override var enabled: Boolean
        get() = layerOverlayComponent.enabled
        set(value) {
            layerOverlayComponent.enabled = value
        }

    override var visible: Boolean
        get() = layerOverlayComponent.visible
        set(value) {
            layerOverlayComponent.visible = value
        }
}