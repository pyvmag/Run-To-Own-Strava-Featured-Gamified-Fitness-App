let offset = 0;
const countPerPage = 7;

document.addEventListener('DOMContentLoaded', async () => {
    const feed = document.getElementById('feed-container');
    const loadBtn = document.getElementById('load-more-btn');
    const spinner = document.getElementById('loading-spinner');
    offset = 0;

    loadBtn.addEventListener('click', () => loadActivities());

    await loadActivities(); // Load first set

async function loadActivities() {
    spinner.style.display = 'block';
    loadBtn.disabled = true;
    document.getElementById('no-more-msg').style.display = 'none'; // Hide message before loading

    try {
        const [athlete, activities] = await Promise.all([
            fetch('/strava/athlete').then(res => res.json()),
            fetch(`/strava/activities?count=${countPerPage}&offset=${offset}`).then(res => res.json())
        ]);

        spinner.style.display = 'none';
        loadBtn.disabled = false;

        // ✅ Define these now
        const profilePic = athlete.profile_medium || athlete.profile || '/images/default-avatar.png';
        const displayName = athlete.firstname || "You";

        if (!activities || activities.length === 0) {
            loadBtn.style.opacity = 0.5;
            loadBtn.style.cursor = "not-allowed";
            loadBtn.disabled = true;
            document.getElementById('no-more-msg').style.display = 'block'; // Show no more runs
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
                    ${hasPolyline ? `<div class="run-map" id="map-${offset + idx}" style="height: 220px;"></div>` : ''}
                    <div class="run-post-details">
                        <h3>${act.name || "Untitled Run"}</h3>
                        <p>🏃 ${(act.distance / 1000).toFixed(2)} km • ⏱️ ${(act.elapsed_time / 60).toFixed(1)} min</p>
                        <p>📅 ${new Date(act.start_date).toLocaleDateString()}</p>
                    </div>
                `;
                feed.appendChild(post);

                if (hasPolyline) {
                    const coordinates = polyline.decode(act.map.summary_polyline)
                        .map(([lat, lng]) => [lng, lat]);

                    const map = new maplibregl.Map({
                        container: `map-${offset + idx}`,
                        style: 'https://api.maptiler.com/maps/hybrid/style.json?key=dpvZMY5gns5lycvwB2Fb',
                        center: coordinates[0],
                        zoom: 13
                    });

                    map.on('load', () => {
                        map.addSource(`route-${offset + idx}`, {
                            type: 'geojson',
                            data: {
                                type: 'Feature',
                                geometry: { type: 'LineString', coordinates }
                            }
                        });
                        map.addLayer({
                            id: `route-${offset + idx}`,
                            type: 'line',
                            source: `route-${offset + idx}`,
                            paint: { 'line-color': '#fc4c02', 'line-width': 5 }
                        });
                    });
                }
            });

          offset += countPerPage;

                 if (activities.length < countPerPage) {
                     loadBtn.style.opacity = 0.5;
                     loadBtn.style.cursor = "not-allowed";
                     loadBtn.disabled = true;
                     document.getElementById('no-more-msg').style.display = 'block'; // Show no more runs
                 }



            // Reveal animation
            const observer = new IntersectionObserver(
                entries => entries.forEach(entry => {
                    if (entry.isIntersecting) entry.target.classList.add('visible');
                }),
                { threshold: 0.15 }
            );

            const waitForPosts = setInterval(() => {
                const posts = feed.querySelectorAll('.run-post');
                if (posts.length > 0) {
                    posts.forEach(post => observer.observe(post));
                    clearInterval(waitForPosts);
                }
            }, 300);

        }      catch (err) {
                  console.error('Error loading feed:', err);
                  spinner.style.display = 'none';
                  feed.innerHTML += "<p>Failed to load more activities. 🧨</p>";
              }

    }
});