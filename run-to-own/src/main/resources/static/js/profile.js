document.addEventListener('DOMContentLoaded', () => {

    // ===== Fetch Athlete Profile =====
    fetch('/strava/athlete')
        .then(res => res.json())
        .then(athlete => {
            if (!athlete) return;
            document.getElementById('athlete-name').innerText =
                `${athlete.firstname || ''} ${athlete.lastname || ''}`;
            document.getElementById('athlete-location').innerText =
                [athlete.city, athlete.country].filter(Boolean).join(', ') || 'Unknown';

            const avatar = document.getElementById('profile-avatar');
            avatar.src = athlete.profile || '/images/default-avatar.png';
            avatar.alt = `${athlete.firstname || 'Athlete'} Profile Picture`;
        })
        .catch(err => console.error('Failed to fetch athlete:', err));

    // ===== Fetch Athlete Gear =====
   fetch('/strava/profile/gear')
       .then(res => res.json())
       .then(gear => {
           const list = document.getElementById('gear-list');
           list.innerHTML = '';

           if (!gear || gear.length === 0) {
               list.innerHTML = '<li>No gear found</li>';
               return;
           }

           gear.forEach(g => {
               const distanceKm = (g.distance ? g.distance / 1000 : 0).toFixed(1);
               const li = document.createElement('li');
               li.textContent = `${g.name} (${distanceKm} km used)`;
               list.appendChild(li);
           });
       })

        .catch(err => console.error("Error fetching gear:", err));

    // ===== Fetch Activities for Stats and Weekly Chart =====
    fetch('/strava/activities')
        .then(res => {
            if (!res.ok) throw new Error("Failed to load activities");
            return res.json();
        })
        .then(activities => {
            if (!activities || activities.length === 0) return;

            let totalDistance = 0;
            let totalTime = 0;
            let totalElevation = 0;
            const weekly = [0, 0, 0, 0, 0, 0, 0]; // Sun → Sat

            activities.forEach(act => {
                const distance = act.distance || 0;
                const time = act.elapsed_time || 0;
                const elevation = act.total_elevation_gain || 0;

                totalDistance += distance;
                totalTime += time;
                totalElevation += elevation;

                // Weekly stats
                const activityDate = new Date(act.start_date);
                const weekday = activityDate.getDay(); // 0=Sun
                weekly[weekday] += distance / 1000;
            });

            // Update Stats in DOM
            document.getElementById('total-distance').innerText = (totalDistance / 1000).toFixed(1) + ' km';
            document.getElementById('total-runs').innerText = activities.length;
            document.getElementById('total-time').innerText = (totalTime / 3600).toFixed(1) + ' h';
            document.getElementById('total-elevation').innerText = totalElevation.toFixed(1) + ' m';

            // ===== Weekly Chart =====
            const ctx = document.getElementById('weeklyChart').getContext('2d');
            new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: ['Sun','Mon','Tue','Wed','Thu','Fri','Sat'],
                    datasets: [{
                        label: 'Distance (km)',
                        data: weekly,
                        backgroundColor: '#fc4c02',
                        borderRadius: 5
                    }]
                },
                options: {
                    responsive: true,
                    plugins: { legend: { display: false } },
                    scales: {
                        x: { grid: { color: 'rgba(255,255,255,0.1)' } },
                        y: { grid: { color: 'rgba(255,255,255,0.1)' } }
                    }
                }
            });
        })
        .catch(err => console.error("Error fetching activities:", err));
});
