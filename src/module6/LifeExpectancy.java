package module6;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.marker.AbstractMarker;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MultiMarker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.Microsoft;
import de.fhpotsdam.unfolding.utils.MapUtils;
import parsing.ParseFeed;
import processing.core.PApplet;

import java.util.HashMap;
import java.util.List;

/**
 * Visualizes life expectancy in different countries.
 * <p>
 * It loads the country shapes from a GeoJSON file via a data reader, and loads the population density values from
 * another CSV file (provided by the World Bank). The data value is encoded to transparency via a simplistic linear
 * mapping.
 */
public class LifeExpectancy extends PApplet {

    UnfoldingMap map;
    HashMap<String, Float> lifeExpMap;
    List<Feature> countries;
    List<Marker> countryMarkers;
    private Marker lastSelected;

    public void setup() {
        size(800, 600, OPENGL);
        map = new UnfoldingMap(this, 50, 50, 700, 500, new Microsoft.HybridProvider());
        MapUtils.createDefaultEventDispatcher(this, map);

        // Load lifeExpectancy data
        lifeExpMap = ParseFeed.loadLifeExpectancyFromCSV(this, "LifeExpectancyWorldBank.csv");


        // Load country polygons and adds them as markers
        countries = GeoJSONReader.loadData(this, "countries.geo.json");
        countryMarkers = MapUtils.createSimpleMarkers(countries);
        map.addMarkers(countryMarkers);

        // Country markers are shaded according to life expectancy (only once)
        shadeCountries();
    }

    public void draw() {
        // Draw map tiles and country markers
        map.draw();
        if (lastSelected != null) {
            showTitle();
            //pink color
            setHighlightColor(color(255, 105, 180));
        }
    }

    //Helper method to color each country based on life expectancy
    //Red-orange indicates low (near 40)
    //Blue indicates high (near 100)
    private void shadeCountries() {
        for (Marker marker : countryMarkers) {
            // Find data for country of the current marker
            String countryId = marker.getId();
            if (lifeExpMap.containsKey(countryId)) {
                float lifeExp = lifeExpMap.get(countryId);
                // Encode value as brightness (values range: 40-90)
                int colorLevel = (int) map(lifeExp, 40, 90, 10, 255);
                marker.setColor(color(255 - colorLevel, 100, colorLevel));
            } else {
                marker.setColor(color(150, 150, 150));
            }
        }
    }

    /** Event handler that gets called automatically when the
     * mouse moves.
     */
    @Override
    public void mouseMoved() {
        if (lastSelected != null) {
            lastSelected.setSelected(false);
            lastSelected = null;

        }
        selectMarkerIfHover(countryMarkers);
    }

    // If there is a marker selected
    private void selectMarkerIfHover(List<Marker> markers) {
        // Abort if there's already a marker selected
        if (lastSelected != null) {
            return;
        }

        for (Marker marker : markers) {
            if (marker.isInside(map, mouseX, mouseY)) {
                lastSelected = marker;
                marker.setSelected(true);
                return;
            }
        }
    }

    /**
     * Method to change the background of a country
     * @param color
     */
    private void setHighlightColor(int color) {
        if (lastSelected instanceof MultiMarker) {
            List<Marker> markers = ((MultiMarker) lastSelected).getMarkers();
            for (Marker marker : markers) {
                ((AbstractMarker) marker).setHighlightColor(color);
            }
        } else {
            ((AbstractMarker) lastSelected).setHighlightColor(color);
        }
    }

    private void showTitle() {
        String title = (String) lastSelected.getProperty("name");
        String countryId = lastSelected.getId();
        if (lifeExpMap.containsKey(countryId)) {
            float lifeExp = lifeExpMap.get(countryId);
            title += ":" + lifeExp;
        }
        fill(255, 250, 240);

        int xbase = 25;
        int ybase = 50;

        rect(xbase, ybase + 15, textWidth(title) + 6, 18, 5);

        fill(0);
        textAlign(LEFT, CENTER);
        textSize(11);
        text(title, xbase + 3, ybase + 22);
    }
}
