package org.blitzortung.android.map

import android.text.format.DateFormat
import android.view.View
import android.widget.TextView
import org.blitzortung.android.app.R
import org.blitzortung.android.map.overlay.RasterShape
import org.blitzortung.android.map.overlay.StrikeOverlay
import org.blitzortung.android.map.overlay.StrikeShape


fun createStrikePopUp(popUp: View, strikeOverlay: StrikeOverlay): View {
    var result = DateFormat.format("kk:mm:ss", strikeOverlay.timestamp) as String

    if (strikeOverlay.shape is RasterShape) {
        result += ", #%d".format(strikeOverlay.multiplicity)
    } else if (strikeOverlay.shape is StrikeShape) {
        result += " (%.4f %.4f)".format(strikeOverlay.center.longitudeE6 / 1e6, strikeOverlay.center.latitudeE6 / 1e6)
    }

    with(popUp.findViewById(R.id.popup_text) as TextView) {
        setBackgroundColor(-2013265920)
        setPadding(5, 5, 5, 5)
        setText(result)
    }

    return popUp
}