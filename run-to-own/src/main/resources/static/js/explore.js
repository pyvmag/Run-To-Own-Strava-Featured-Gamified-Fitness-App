document.addEventListener('DOMContentLoaded', () => {
    const map = new maplibregl.Map({
        container: 'map',
        style: 'https://api.maptiler.com/maps/streets-v2/style.json?key=Nr1XalmqB4Xp2Bwm202p',
        center: [74.2433, 16.7050], // Centered on Kolhapur
        zoom: 13 // Zooming in a bit more to make the hexagon easy to see
    });

    // We are ignoring the sync button and API calls for this test.

    // When the map loads, we will try to draw ONE hexagon.
    map.on('load', () => {
        console.log("Map loaded. Attempting to draw one hardcoded hexagon.");

        // A single, known H3 index for a tile in the center of Kolhapur.
        const testH3Index = '8928308283bffff';

        // Use the same logic as before to create the shape.
        const boundaryLatLng = h3.cellToBoundary(testH3Index);
        const boundaryLngLat = boundaryLatLng.map(coord => [coord[1], coord[0]]);
        boundaryLngLat.push(boundaryLngLat[0]); // Close the loop

        const feature = {
            'type': 'Feature',
            'geometry': {
                'type': 'Polygon',
                'coordinates': [boundaryLngLat]
            },
            'properties': {}
        };

        console.log("Generated test GeoJSON feature:", feature);

        const geoJsonData = {
            'type': 'FeatureCollection',
            'features': [feature]
        };

        // Add the source and layer to the map.
        map.addSource('test-tile-source', {
            'type': 'geojson',
            'data': geoJsonData
        });

        map.addLayer({
            'id': 'test-tile-layer',
            'type': 'fill',
            'source': 'test-tile-source',
            'paint': {
                'fill-color': '#FF0000', // Bright Red
                'fill-opacity': 0.7
            }
        });

        console.log("Source and layer for test hexagon have been added to the map.");
    });
});