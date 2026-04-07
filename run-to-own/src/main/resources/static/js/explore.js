document.addEventListener('DOMContentLoaded', () => {
    const map = new maplibregl.Map({
        container: 'map',
        style: 'https://api.maptiler.com/maps/streets-v2-dark/style.json?key=AxOJY4BnQVnKnsedKJCC', // PASTE YOUR KEY HERE
        center: [74.2433, 16.7050],
        zoom: 11
    });

   const syncButton = document.getElementById('sync-runs-btn');
       const loadingIndicator = document.getElementById('loading-indicator');

       // --- SELF-CONTAINED WINDING ORDER FIX ---
       // This function calculates the area of a polygon. A positive area means it's
       // wound clockwise (CW), and a negative area means counter-clockwise (CCW).
       function signedArea(ring) {
           let area = 0;
           for (let i = 0; i < ring.length; i++) {
               const j = (i + 1) % ring.length;
               area += ring[i][0] * ring[j][1];
               area -= ring[j][0] * ring[i][1];
           }
           return area / 2;
       }

       // This function ensures the polygon is wound counter-clockwise, as GeoJSON requires.
       function rewind(polygon) {
           // The GeoJSON spec requires the outer ring to be counter-clockwise.
           if (signedArea(polygon[0]) > 0) {
               polygon[0].reverse();
           }
           return polygon;
       }
       // --- END OF FIX ---

       const loadAndDrawTiles = async () => {
           try {
               const response = await fetch('/api/explore/tiles');
               if (!response.ok) throw new Error('Failed to fetch tiles');
               const tiles = await response.json();

               const features = tiles.map(tile => {
                   const boundaryLatLng = h3.h3ToGeoBoundary(tile.h3Index);
                   const boundaryLngLat = boundaryLatLng.map(coord => [coord[1], coord[0]]);
                   boundaryLngLat.push(boundaryLngLat[0]);

                   return {
                       'type': 'Feature',
                       'geometry': {
                           'type': 'Polygon',
                           // ✅ FIX: Manually correct the winding order of the coordinates
                           'coordinates': rewind([boundaryLngLat])
                       },
                       'properties': {
                           'ownerName': tile.ownerName,
                           'isCurrentUserOwner': tile.isCurrentUserOwner
                       }
                   };
               });

               const geoJsonData = { 'type': 'FeatureCollection', 'features': features };

               const geoJsonSource = map.getSource('tiles');
               if (geoJsonSource) {
                   geoJsonSource.setData(geoJsonData);
               } else {
                   map.addSource('tiles', { 'type': 'geojson', 'data': geoJsonData });
                   map.addLayer({
                       'id': 'tiles-layer',
                       'type': 'fill',
                       'source': 'tiles',
                       'paint': {
                           'fill-color': [
                               'case',
                               ['==', ['get', 'isCurrentUserOwner'], true],
                               '#fc4c02',
                               '#007cbf'
                           ],
                           'fill-opacity': 0.6,
                           'fill-outline-color': '#ffffff'
                       }
                   });
               }
           } catch (error) {
               console.error("Error loading tiles:", error);
           }
       };

       const syncRuns = async () => {
           loadingIndicator.style.display = 'block';
           syncButton.disabled = true;
           try {
               const response = await fetch('/api/explore/process-new-runs', { method: 'POST' });
               if (!response.ok) throw new Error('Sync failed');
               await loadAndDrawTiles();
           } catch (error) {
               console.error("Error processing runs:", error);
           } finally {
               loadingIndicator.style.display = 'none';
               syncButton.disabled = false;
           }
       };

       syncButton.addEventListener('click', syncRuns);
       map.on('load', syncRuns);
   });

