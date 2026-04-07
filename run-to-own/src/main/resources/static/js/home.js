// home.js
let offset = 0;
const countPerPage = 7;

document.addEventListener('DOMContentLoaded', async () => {
    // Show the loader immediately, then hide after a delay
    setTimeout(() => {
        const loader = document.getElementById('loader');
        if (loader) loader.style.display = 'none';
        loadActivities();
    }, 2500);

    const feed = document.getElementById('feed-container');
    const loadBtn = document.getElementById('load-more-btn');

    if (loadBtn) {
        loadBtn.addEventListener('click', () => loadActivities());
    }

    async function loadActivities() {
        const spinner = document.getElementById('loading-spinner');
        const noMoreMsg = document.getElementById('no-more-msg');

        if (spinner) spinner.style.display = 'block';
        if (loadBtn) loadBtn.disabled = true;
        if (noMoreMsg) noMoreMsg.style.display = 'none';

        try {
            const [athlete, activities] = await Promise.all([
                fetch('/strava/athlete').then(res => res.json()),
                fetch(`/strava/activities?count=${countPerPage}&offset=${offset}`).then(res => res.json())
            ]);

            if (spinner) spinner.style.display = 'none';
            if (loadBtn) loadBtn.disabled = false;

            const profilePic = athlete.profile_medium || athlete.profile || '/images/default-avatar.png';
            const displayName = athlete.firstname || "You";

            if (!activities || activities.length === 0) {
                if (loadBtn) {
                    loadBtn.style.opacity = 0.5;
                    loadBtn.style.cursor = "not-allowed";
                    loadBtn.disabled = true;
                }
                if (noMoreMsg) noMoreMsg.style.display = 'block';
                return;
            }

            activities.forEach((act, idx) => {
                const hasPolyline = !!act.map?.summary_polyline;
                const post = document.createElement('div');
                post.className = 'run-post';

                post.innerHTML = `
                  <div class="run-post-header">
                    <img class="post-avatar" src="${profilePic}" alt="User Avatar" />
                    <span class="username">${displayName}</span>
                  </div>
                  ${hasPolyline ? `<div class="run-map" id="map-${offset + idx}"></div>` : ''}
                  <div class="run-post-details">
                    <h3>${act.name || "Untitled Activity"}</h3>
                    <p>
                      <span class="pill">🏃 ${(act.distance / 1000).toFixed(2)} km</span>
                      <span class="pill">⏱️ ${(act.elapsed_time / 60).toFixed(1)} min</span>
                      <span class="pill">📅 ${new Date(act.start_date).toLocaleDateString()}</span>
                    </p>
                  </div>`;

                if (feed) feed.appendChild(post);

                if (hasPolyline) {
                    const coordinates = polyline.decode(act.map.summary_polyline).map(c => [c[1], c[0]]);

                    const map = new maplibregl.Map({
                        container: `map-${offset + idx}`,
                        style: 'https://api.maptiler.com/maps/hybrid/style.json?key=dpvZMY5gns5lycvwB2Fb',
                        center: coordinates[0],
                        zoom: 12
                        // ✅ The "interactive: false" line has been removed
                    });

                    map.on('load', () => {
                        map.addSource(`route-${offset + idx}`, {
                            'type': 'geojson',
                            'data': {
                                'type': 'Feature',
                                'properties': {},
                                'geometry': {
                                    'type': 'LineString',
                                    'coordinates': coordinates
                                }
                            }
                        });
                        map.addLayer({
                            'id': `route-${offset + idx}`,
                            'type': 'line',
                            'source': `route-${offset + idx}`,
                            'layout': { 'line-join': 'round', 'line-cap': 'round' },
                            'paint': { 'line-color': '#fc4c02', 'line-width': 4 }
                        });
                    });
                }
            });

            offset += countPerPage;

            const observer = new IntersectionObserver(
                (entries) => {
                    entries.forEach(entry => {
                        if (entry.isIntersecting) {
                            entry.target.classList.add('visible');
                            observer.unobserve(entry.target);
                        }
                    });
                }, { threshold: 0.1 }
            );

            document.querySelectorAll('.run-post:not(.visible)').forEach(post => {
                observer.observe(post);
            });

        } catch (err) {
            console.error('Error loading feed:', err);
            if (spinner) spinner.style.display = 'none';
            if (feed) feed.innerHTML += "<p>Failed to load activities.</p>";
        }
    }
});