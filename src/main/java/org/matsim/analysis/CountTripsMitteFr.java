package org.matsim.analysis;

import org.locationtech.jts.geom.Geometry;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.gis.ShapeFileReader;

import java.util.stream.Collectors;


public class CountTripsMitteFr {
    public static void main(String[] args) {
        var shapeFilePath = "/home/asdf/Downloads/Bezirke_Berlin/Berlin_Bezirke.shp";
        var plansFilePath = "/home/asdf/Downloads/Berlin_Scenario/berlin-v5.5.3-1pct.output_plans.xml.gz";
        var networkFilePath = "/home/asdf/Downloads/Berlin_Scenario/berlin-v5.5.3-1pct.output_network.xml.gz";
        var transformation = TransformationFactory.getCoordinateTransformation("EPSG:31468", "EPSG:3857");

        var features = ShapeFileReader.getAllFeatures(shapeFilePath);

        var geometries = features.stream()
                .filter(simpleFeature -> simpleFeature.getAttribute("Gemeinde_n").equals("Mitte") || simpleFeature.getAttribute("Gemeinde_n").equals("Friedrichshain-Kreuzberg"))
                .map(simpleFeature -> (Geometry) simpleFeature.getDefaultGeometry())
                .collect(Collectors.toList());

        var mitte = geometries.get(0);
        var friedrichshain = geometries.get(1);
        var counter = 0;

        var population = PopulationUtils.readPopulation(plansFilePath);
        var network = NetworkUtils.readNetwork(networkFilePath);

        for (Person person : population.getPersons().values()) {
            var plan = person.getSelectedPlan();
            var activities = TripStructureUtils.getActivities(plan, TripStructureUtils.StageActivityHandling.ExcludeStageActivities);

            for (int i = 0; i < activities.size() - 1; i++) {
                var startCoord = transformation.transform(activities.get(i).getCoord());
                var endCoord = transformation.transform(activities.get(i + 1).getCoord());

                var startPoint = MGC.coord2Point(startCoord);
                var endPoint = MGC.coord2Point(endCoord);

                if (mitte.contains(startPoint) && friedrichshain.contains(endPoint)) {
                    counter++;
                }
            }
        }
        System.out.println(counter + " trips from Mitte to Friedrichshain-Kreuzberg.");
    }
}
