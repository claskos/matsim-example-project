package org.matsim.analysis;

import org.locationtech.jts.geom.Geometry;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.gis.ShapeFileReader;

import java.util.stream.Collectors;


public class CountActivitiesInMitte {
    public static void main(String[] args) {
        var shapeFilePath = "/home/asdf/Downloads/Bezirke_Berlin/Berlin_Bezirke.shp";
        var plansFilePath = "/home/asdf/Downloads/Berlin_Scenario/berlin-v5.5.3-1pct.output_plans.xml.gz";
        var transformation = TransformationFactory.getCoordinateTransformation("EPSG:31468", "EPSG:3857");

        var features = ShapeFileReader.getAllFeatures(shapeFilePath);

        var geometries = features.stream()
                .filter(simpleFeature -> simpleFeature.getAttribute("Gemeinde_n").equals("Mitte"))
                .map(simpleFeature -> (Geometry) simpleFeature.getDefaultGeometry())
                .collect(Collectors.toList());

        var mitte = geometries.get(0);
        var counter = 0;

        var population = PopulationUtils.readPopulation(plansFilePath);


        for (Person person : population.getPersons().values()) {
            var plan = person.getSelectedPlan();
            var activities = TripStructureUtils.getActivities(plan, TripStructureUtils.StageActivityHandling.ExcludeStageActivities);

            for (Activity activity : activities) {
                var coord = activity.getCoord();
                var transformed_coord = transformation.transform(coord);
                var geotoolsPoint = MGC.coord2Point(transformed_coord);

                if (mitte.contains(geotoolsPoint)) {
                    counter++;
                }
            }
        }
        System.out.println(counter + " activities in Mitte.");
    }
}
